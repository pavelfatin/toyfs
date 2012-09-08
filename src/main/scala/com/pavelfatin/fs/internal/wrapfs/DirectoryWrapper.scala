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

/** A wrapper for a host file system directory.
  *
  * @param root the root directory of the wrapper file system
  * @param entry the host directory
  *
  * @see [[com.pavelfatin.fs.Directory]]
  * @see [[com.pavelfatin.fs.internal.wrapfs.EntryWrapper]]
  */
private class DirectoryWrapper(val root: io.File, val entry: io.File) extends Directory with EntryWrapper {
  def entries = {
    // Java 6 is unable to properly handle junction points
    val list = Option(entry.listFiles()).getOrElse(throw new IOException(s"Not a valid path: '${entry.getPath}'"))
    val (directories, files) = list.partition(_.isDirectory)
    (directories.map(new DirectoryWrapper(root, _)), files.map(new FileWrapper(root, _)))
  }

  def createFile(name: String, date: Calendar) = {
    val file = new io.File(entry, name)
    val created = file.createNewFile()
    if (!created) throw new IOException(s"Unable to create a file '$name' in '${entry.getPath}'")
    file.setLastModified(date.getTimeInMillis)
    new FileWrapper(root, file)
  }

  def createDirectory(name: String, date: Calendar) = {
    val directory = new io.File(entry, name)
    val created = directory.mkdir()
    if (!created) throw new IOException(s"Unable to create a directory '$name' in '${entry.getPath}'")
    directory.setLastModified(date.getTimeInMillis)
    new DirectoryWrapper(root, directory)
  }
}
