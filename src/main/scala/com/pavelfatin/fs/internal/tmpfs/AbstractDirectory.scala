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

import java.util.Calendar

/** A skeleton implementation of an in-memory directory.
  *
  * Stores files and sub-directories in lists.
  *
  * @see [[com.pavelfatin.fs.Directory]]
  * @see [[com.pavelfatin.fs.FileSystemEntry]]
  */
private abstract class AbstractDirectory extends Directory {
  private var directories: List[AbstractDirectory] = Nil
  private var files: List[HeapFile] = Nil

  def entries = (directories, files)

  def createDirectory(name: String, date: Calendar) = {
    val directory = new HeapSubDirectory(this)(onDelete = delete)

    directory.name = name
    directory.date = date

    directories ::= directory

    directory
  }

  def createFile(name: String, date: Calendar) = {
    val file = new HeapFile(this)(onDelete = delete)

    file.name = name
    file.date = date

    files ::= file

    file
  }

  /** Returns a total size of all files within this directory and all its sub-directories (recursively). */
  def size: Long = files.map(_.length).sum + directories.map(_.size).sum

  /** Deletes all files and directories in this directory. */
  def clear() {
    files = Nil
    directories = Nil
  }

  private def delete(file: File) {
    files = files.filterNot(_ == file)
  }

  private def delete(directory: Directory) {
    directories = directories.filterNot(_ == directory)
  }
}
