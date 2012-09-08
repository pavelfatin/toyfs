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

private class MockIndexTable(contents: Entry*) extends IndexTable {
  private val storage = contents.toArray

  def size = storage.length

  def free = storage.count(_ == Entry.Free)

  def get(n: Int) = storage(n)

  def set(n: Int, entry: Entry) {
    storage(n) = entry
  }

  def allocate() = {
    val n = storage.indexWhere(_ == Entry.Free)
    if (n >= 0) {
      set(n, Entry.End)
      Some(n)
    } else {
      None
    }
  }

  def clear() {
    (0 until storage.size).foreach(n => set(n, Entry.Free))
  }

  def entries: List[Entry] = storage.toList

  override def toString = s"${getClass.getSimpleName}(${contents.mkString(", ")})"
}
