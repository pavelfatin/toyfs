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

import scala.annotation._

/** An implementation of `IndexChainStorage` that represents `IndexTable` as a collection of index chains.
  *
  * @param table the index table
  *
  * @see [[com.pavelfatin.fs.internal.toyfs.IndexTable]]
  * @see [[com.pavelfatin.fs.internal.toyfs.IndexChainStorage]]
  */
private class IndexChainStorageImpl(table: IndexTable) extends IndexChainStorage {
  def size = table.size

  def get(n: Int): IndexChain = {
    if (n < 0)
      throw new IndexOutOfBoundsException(
        s"Chain index is negative: $n")

    if (n >= table.size)
      throw new IndexOutOfBoundsException(
        s"Chain index ($n) is greater than or equal to table size (${table.size})")

    new IndexChainImpl(n)
  }

  def allocate(): Option[IndexChain] = table.allocate().map(new IndexChainImpl(_))

  /** An implementation of `IndexChain` that represent index table entries as a sequence of integers.
    *
    * The implementation caches last table lookup for faster sequential reading.
    *
    * @param id the identifier of this chain (the index of this chain in the parent storage)
    *
    * @see [[com.pavelfatin.fs.internal.toyfs.IndexChain]]
    * @see [[com.pavelfatin.fs.internal.toyfs.IndexTable]]
    */
  private class IndexChainImpl(val id: Int) extends IndexChain {
    private case class Result(offset: Int, index: Option[Int])

    private var lastResult: Option[Result] = None

    private def nextIndexAfter(index: Int): Option[Int] = table.get(index) match {
      case Entry.End => None
      case Entry.Reference(target) => Some(target)
      case Entry.Free => throw new DataFormatException(s"Free entry at index $index referenced in the index table")
    }

    def get(offset: Int, canAllocate: Boolean): Option[Int] = {
      if (offset < 0) throw new IllegalArgumentException(s"Offset is negative: $offset")

      lastResult match {
        case Some(Result(o, Some(i))) if o == offset => Some(i)
        case Some(Result(o, Some(i))) if o <= offset => caching(offset, indexFrom(i, offset - o, canAllocate))
        case _ => caching(offset, indexFrom(id, offset, canAllocate))
      }
    }

    private def caching(offset: Int, index: Option[Int]): Option[Int] = {
      lastResult = Some(Result(offset, index))
      index
    }

    @tailrec
    private def indexFrom(index: Int, offset: Int, canAllocate: Boolean): Option[Int] = {
      if (offset == 0) Some(index) else nextIndexAfter(index) match {
        case Some(next) => indexFrom(next, offset - 1, canAllocate)
        case None => if (canAllocate) allocateIndexFrom(index, offset) else None
      }
    }

    @tailrec
    private def allocateIndexFrom(index: Int, offset: Int): Option[Int] = {
      if (offset == 0) Some(index) else allocateNextIndexAfter(index) match {
        case Some(next) => allocateIndexFrom(next, offset - 1)
        case None => None
      }
    }

    private def allocateNextIndexAfter(index: Int): Option[Int] = {
      table.allocate().map { next =>
        table.set(index, Entry.Reference(next))
        next
      }
    }

    def truncate(size: Int) {
      if (size < 1)
        throw new IllegalArgumentException(
          s"Truncation size ($size) is less than 1")

      for (index <- get(size - 1, canAllocate = false);
           nextIndex <- nextIndexAfter(index)) {
        table.set(index, Entry.End)
        deleteFrom(nextIndex)
      }
    }

    def delete() {
      deleteFrom(id)
    }

    @tailrec
    private def deleteFrom(index: Int) {
      val nextIndex = nextIndexAfter(index)
      table.set(index, Entry.Free)
      nextIndex match {
        case Some(next) => deleteFrom(next)
        case None =>
      }
    }
  }
}
