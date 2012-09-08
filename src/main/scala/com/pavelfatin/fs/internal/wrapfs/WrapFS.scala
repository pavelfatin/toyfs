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

/** A wrapper file system implementation.
  *
  * Transparently wraps a host (OS) file system in a `FileSystem` interface.
  *
  * @param directory the root directory
  *
  * @see [[com.pavelfatin.fs.FileSystem]]
  */
class WrapFS(directory: io.File) extends FileSystem {
  def name = "WrapFS"

  val formatted = true

  def format() {
    // It's too dangerous to delete host file system entries in batch
    throw new UnsupportedOperationException("Not implemented out of safety considerations")
  }

  val root: Directory = new DirectoryWrapper(directory, directory)

  def size = directory.getTotalSpace

  def free = directory.getFreeSpace
}
