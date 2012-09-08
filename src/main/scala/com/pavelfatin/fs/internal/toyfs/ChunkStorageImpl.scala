/*
 * Copyright (C) 2012 Pavel Fatin <http://pavelfatin.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.pavelfatin.fs
package internal
package toyfs

/** A `ChunkStorage` implementation that uses `IndexChainStorage` to store
  * chunk as a chain of clusters in `ClusterStorage`.
  *
  * @param chains the chain storage
  * @param clusters the cluster storage
  *
  * @see [[com.pavelfatin.fs.internal.toyfs.ChunkStorage]]
  * @see [[com.pavelfatin.fs.internal.toyfs.IndexChainStorage]]
  * @see [[com.pavelfatin.fs.internal.toyfs.ClusterStorage]]
  */
private class ChunkStorageImpl(chains: IndexChainStorage, clusters: ClusterStorage) extends ChunkStorage {
  if (chains.size > clusters.size)
    throw new IllegalArgumentException(
      s"Index chain count (${chains.size}) is greater than cluster count (${clusters.size})")

  def size = chains.size

  def get(n: Int): Chunk = {
    if (n < 0)
      throw new IndexOutOfBoundsException(
        s"Chunk index is negative: $n")

    if (n >= size)
      throw new IndexOutOfBoundsException(
        s"Chunk index ($n) is greater than or equal to index size (${chains.size})")

    new ChunkImpl(chains.get(n))
  }

  def allocate(): Option[Chunk] = chains.allocate().map(new ChunkImpl(_))

  private class ChunkImpl(chain: IndexChain) extends AbstractData(extendable = true, lazyLength = true) with Chunk {
    val id = chain.id

    def truncate(threshold: Long) {
      if (threshold < 0L)
        throw new IllegalArgumentException(
          s"Truncation threshold is negative: $threshold")

      val chainSize = (threshold.toDouble / clusters.clusterLength.toDouble).ceil.toInt

      chain.truncate(1.max(chainSize))
    }

    def delete() {
      chain.delete()
    }

    def length = throw new UnsupportedOperationException("Chunk length must not be queried")

    protected def doRead(position: Long, length: Int, buffer: Array[Byte], offset: Int) {
      Reading.perform(position, length, buffer, offset)
    }

    protected def doWrite(position: Long, length: Int, buffer: Array[Byte], offset: Int) {
      Writing.perform(position, length, buffer, offset)
    }

    private trait IO {
      def perform(position: Long, length: Int, buffer: Array[Byte], offset: Int) {
        if (length == 0) return

        val projections: List[Data] = {
          val beginOffset: Int = (position / clusters.clusterLength).toInt
          val endOffset: Int = ((position + length - 1L) / clusters.clusterLength).toInt

          val indices: Seq[Int] = (beginOffset to endOffset).map(offset => indexAt(chain, offset))

          project(clusters.get(indices).toList, position - beginOffset.toLong * clusters.clusterLength, length)
        }

        val offsets: List[Int] = {
          val lengths: List[Int] = projections.map(_.length.toInt)

          lengths.scanLeft(offset)(_ + _)
        }

        for ((part, offset) <- projections.zip(offsets)) {
          transfer(part, 0L, part.length.toInt, buffer, offset)
        }
      }

      private def project(parts: List[Data], position: Long, length: Int): List[Data] = {
        val part: Data = parts.head

        if (position + length <= part.length) {
          List(part.projection(position, length))
        } else {
          val partRemainder: Long = part.length - position
          part.projection(position, partRemainder) :: project(parts.tail, 0L, (length.toLong - partRemainder).toInt)
        }
      }

      protected def transfer(data: Data, position: Long, length: Int, buffer: Array[Byte], offset: Int)

      protected def indexAt(chain: IndexChain, offset: Int): Int
    }

    private object Reading extends IO {
      def transfer(data: Data, position: Long, length: Int, buffer: Array[Byte], offset: Int) {
        data.read(position, length, buffer, offset)
      }

      protected def indexAt(chain: IndexChain, offset: Int) = {
        chain.get(offset, canAllocate = false).getOrElse {
          throw new IllegalArgumentException(s"Cannot retrieve an index at offset $offset")
        }
      }
    }

    private object Writing extends IO {
      def transfer(data: Data, position: Long, length: Int, buffer: Array[Byte], offset: Int) {
        data.write(position, length, buffer, offset)
      }

      protected def indexAt(chain: IndexChain, offset: Int) = {
        chain.get(offset, canAllocate = true).getOrElse {
          throw new NotEnoughSpaceException(s"Cannot allocate a new index at offset $offset")
        }
      }
    }
  }
}
