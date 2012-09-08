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

import java.util.Calendar

/** A common behavior of root directories.
  *
  * Root directory is a directory that has no parent directory.
  *
  * Root directory returns predefined values as its properties. These properties cannot be modified.
  *
  * Root directory cannot be deleted.
  *
  * @see [[com.pavelfatin.fs.Directory]]
  * @see [[com.pavelfatin.fs.FileSystemEntry]]
  */
trait RootDirectory extends FileSystemEntry {
  def parent = None

  def name = ""

  def name_=(it: String) {
    throw new UnsupportedOperationException("Root directory name can't be modified")
  }

  def date = {
    val calendar = Calendar.getInstance()
    calendar.setTimeInMillis(0L)
    calendar
  }

  def date_=(it: Calendar) {
    throw new UnsupportedOperationException("Root directory date can't be modified")
  }

  def hidden = false

  def hidden_=(it: Boolean) {
    throw new UnsupportedOperationException("Root directory visibility can't be modified")
  }

  def delete() {
    throw new UnsupportedOperationException("Root directory can't be deleted")
  }
}
