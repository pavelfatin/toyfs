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

private trait IndexTableStats extends IndexTable {
  private var readCount = 0

  private var writeCount = 0

  abstract override def get(n: Int) = {
    readCount += 1
    super.get(n)
  }

  abstract override def set(n: Int, entry: Entry) {
    writeCount += 1
    super.set(n, entry)
  }

  def reads: Int = readCount

  def writes: Int = writeCount

  def clearStats() {
    readCount = 0
    writeCount = 0
  }
}