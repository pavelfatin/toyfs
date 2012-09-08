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

/** A core ToyFS implementation (without a header).
  *
  * @param clusterSize the cluster size in this storage
  * @param indexTable the index table
  * @param rootDirectory the root directory
  *
  * @see [[com.pavelfatin.fs.Directory]]
  * @see [[com.pavelfatin.fs.internal.toyfs.EntryStorage]]
  * @see [[com.pavelfatin.fs.internal.toyfs.IndexTable]]
  */
private class EntryStorageImpl(clusterSize: Int, indexTable: IndexTable, rootDirectory: Directory) extends EntryStorage {
  def clusterCount: Int = indexTable.size

  def size = indexTable.size.toLong * clusterSize

  def free = indexTable.free.toLong * clusterSize

  def root = rootDirectory
}

private object EntryStorageImpl {
  private val IndexTableEntryLength = 4

  def mount(data: Data, clusterSize: Int, maxNameLength: Int): EntryStorageImpl = {
    if (data.length < IndexTableEntryLength + clusterSize)
      throw new IllegalArgumentException("Not enough space even for a single cluster")

    val (indexTable, rootDirectory) = create(data, clusterSize, maxNameLength, clearIndexTable = false) { chunkStorage =>
      chunkStorage.get(0)
    }

    new EntryStorageImpl(clusterSize, indexTable, rootDirectory)
  }

  def format(data: Data, clusterSize: Int, maxNameLength: Int): EntryStorageImpl = {
    if (data.length < IndexTableEntryLength + clusterSize)
      throw new NotEnoughSpaceException("Not enough space even for a single cluster")

    val (indexTable, rootDirectory) = create(data, clusterSize, maxNameLength, clearIndexTable = true) { chunkStorage =>
      chunkStorage.allocate().getOrElse {
        throw new NotEnoughSpaceException(
          "Not enough space to allocate a first cluster for the root directory")
      }
    }

    try {
      rootDirectory.init()
    } catch {
      case _: NotEnoughSpaceException =>
        throw new NotEnoughSpaceException(
          "Not enough space to allocate subsequent clusters for the root directory")
    }

    new EntryStorageImpl(clusterSize, indexTable, rootDirectory)
  }

  private def create(data: Data, clusterSize: Int, maxNameLength: Int, clearIndexTable: Boolean)
                    (rootChunkFactory: ChunkStorage => Chunk): (IndexTableImpl, RootDirectoryImpl) = {
    val clusterCount = data.length / (IndexTableEntryLength + clusterSize)

    if (clusterCount > Int.MaxValue)
      throw new IllegalArgumentException(
        s"Cluster count is greater than the maximum Int value: $clusterCount")

    val (tableData, clusterData) = splitAt(data, clusterCount * IndexTableEntryLength)

    val indexTable = new IndexTableImpl(tableData)

    if (clearIndexTable) {
      indexTable.clear()
    }

    val rootDirectory = {
      val chunkStorage = {
        val chainStorage = new IndexChainStorageImpl(indexTable)
        val clusterStorage = new ClusterStorageImpl(clusterData, clusterSize)
        new ChunkStorageImpl(chainStorage, clusterStorage)
      }
      val recordStorageFactory = (chunk: Chunk) => new RecordStorageImpl(chunk, maxNameLength)
      val chunk = rootChunkFactory(chunkStorage)
      new RootDirectoryImpl(chunk, chunkStorage, recordStorageFactory)
    }

    (indexTable, rootDirectory)
  }

  private def splitAt(data: Data, position: Long): (Data, Data) = {
    (data.projection(0L, position), data.projection(position, data.length - position))
  }
}
