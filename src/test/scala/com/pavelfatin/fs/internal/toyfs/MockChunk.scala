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

private class MockChunk(val id: Int, content: String) extends Data with Chunk {
  val data: ByteData with DataStats = new ByteData(content, extendable = true, lazyLength = true) with DataStats

  private var _deleted = false

  def length: Long = throw new UnsupportedOperationException()

  def read(position: Long, length: Int, buffer: Array[Byte], offset: Int) {
    data.read(position, length, buffer, offset)
  }

  def write(position: Long, length: Int, buffer: Array[Byte], offset: Int) {
    data.write(position, length, buffer, offset)
  }

  def projection(position: Long, length: Long) = data.projection(position, length)

  def truncate(threshold: Long) {
    data.truncate(threshold)
  }

  def deleted: Boolean = _deleted

  def delete() {
    _deleted = true
  }

  def presentation: String = data.presentation

  override def toString = s"${getClass.getSimpleName}($id: $presentation)"
}
