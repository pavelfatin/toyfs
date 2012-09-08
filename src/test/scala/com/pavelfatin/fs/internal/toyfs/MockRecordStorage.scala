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

private class MockRecordStorage(maxNameLength: Int, content: Record*) extends RecordStorage {
  private val buffer = content.toBuffer

  def nameLength = maxNameLength

  def get(index: Int) = buffer(index)

  def set(index: Int, record: Record) {
    if (index == buffer.size)
      buffer += record
    else
      buffer(index) = record
  }

  def truncate(size: Int) {
    if (size < buffer.size)
      buffer.remove(size, buffer.size - size)
  }

  def records: Seq[Record] = buffer.toList

  override def toString = s"${getClass.getSimpleName}(${buffer.mkString(", ")})"
}