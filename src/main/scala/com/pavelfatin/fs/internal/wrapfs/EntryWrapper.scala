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
package wrapfs

import java.io
import io.IOException
import java.util.Calendar

/** A wrapper for basic file system entry properties.
  *
  * @see [[com.pavelfatin.fs.FileSystemEntry]]
  */
private trait EntryWrapper extends FileSystemEntry {
  protected def root: io.File

  protected def entry: io.File

  def parent = if (entry == root) None else
    Option(entry.getParentFile).map(new DirectoryWrapper(root, _))

  def name = if (entry == root) "" else entry.getName

  def name_=(it: String) {
    if (entry == root) throw new UnsupportedOperationException("Root directory name can't be modified")

    val renamed = entry.renameTo(new io.File(entry.getParent, it))
    if (!renamed) throw new IOException(s"Unable to rename '${entry.getPath}' to '$it'")
  }

  def date = {
    val it = Calendar.getInstance
    it.setTimeInMillis(entry.lastModified)
    it
  }

  def date_=(it: Calendar) {
    if (entry == root) throw new UnsupportedOperationException("Root directory date can't be modified")

    val changed = entry.setLastModified(it.getTimeInMillis)
    if (!changed) throw new IOException(s"Unable to change modification time of '${entry.getPath}'")
  }

  def hidden = entry.isHidden

  // Can be implemented only in Java 7+
  def hidden_=(it: Boolean) {
    if (entry == root) throw new UnsupportedOperationException("Root directory visibility can't be modified")

    // Files.setAttribute(entry, "dos:hidden", it)
  }

  def delete() {
    if (entry == root) throw new UnsupportedOperationException("Root directory can't be deleted")

    val deleted = entry.delete()
    if (!deleted) throw new IOException(s"Unable to delete '${entry.getPath}'")
  }
}
