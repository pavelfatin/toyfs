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
package tmpfs

/** An in-memory file implementation that holds data in a byte array.
  *
  * The array is extended on-demand (via array copy) using a growth factor of 2.
  *
  * The excessive array capacity is trimmed on file closing.
  *
  * @param owner the parent directory
  * @param onDelete the actual deletion handler
  *
  * @see [[com.pavelfatin.fs.File]]
  * @see [[com.pavelfatin.fs.internal.AbstractData]]
  * @see [[com.pavelfatin.fs.internal.AbstractFile]]
  * @see [[com.pavelfatin.fs.internal.tmpfs.EntryProperties]]
  */
private class HeapFile(owner: Directory)(onDelete: File => Unit)
  extends AbstractData(extendable = true) with AbstractFile with EntryProperties {

  private var array = new Array[Byte](32)

  private var size = 0

  def parent = Some(owner)

  def delete() {
    onDelete(this)
  }

  protected def doOpen() {}

  protected def doClose() {
    resizeArrayTo(size)
  }

  def length = size

  protected def doRead(position: Long, length: Int, buffer: Array[Byte], offset: Int) {
    System.arraycopy(array, position.toInt, buffer, offset, length)
  }

  protected def doWrite(position: Long, length: Int, buffer: Array[Byte], offset: Int) {
    if (position + length > Int.MaxValue)
      throw new IllegalArgumentException(
        s"Position + length ($position + $length) is larger than the maximum Int value")

    val requiredSize = position.toInt + length
    ensureCapacity(requiredSize)
    System.arraycopy(buffer, offset, array, position.toInt, length)
    if (requiredSize > size) {
      size = requiredSize
    }
  }

  protected def doTruncate(length: Long) {
    resizeArrayTo(length.toInt)
    size = length.toInt
  }

  private def ensureCapacity(minCapacity: Int) {
    val oldCapacity = array.length
    if (minCapacity > oldCapacity) {
      resizeArrayTo(minCapacity.max(oldCapacity * 2))
    }
  }

  private def resizeArrayTo(capacity: Int) {
    val newArray = new Array[Byte](capacity)
    System.arraycopy(array, 0, newArray, 0, array.length.min(capacity))
    array = newArray
  }
}
