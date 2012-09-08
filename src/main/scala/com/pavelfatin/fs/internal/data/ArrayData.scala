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
package data

/** An array-based `Data` implementation.
  *
  * Non-sequential access time is constant.
  *
  * @param size the size of the underlying array
  * @throws OutOfMemoryError if there's not enough heap memory to allocate the underlying array
  *
  * @see [[com.pavelfatin.fs.Data]]
  */
class ArrayData(size: Int) extends AbstractData {
  private val data = new Array[Byte](size)

  def length = size

  protected def doRead(position: Long, length: Int, buffer: Array[Byte], offset: Int) {
    Array.copy(data, position.toInt, buffer, offset, length)
  }

  protected def doWrite(position: Long, length: Int, buffer: Array[Byte], offset: Int) {
    Array.copy(buffer, offset, data, position.toInt, length)
  }
}
