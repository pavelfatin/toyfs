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

/** A file system implementation that uses JVM heap as a storage medium.
  *
  * Since it implements `FileSystem` directly, there's no need for an intermediate
  * data format, as opposed to a custom file system (like `ToyFS`)
  * on top of `ArrayData`.
  *
  * Employs dynamic memory allocation/freeing.
  *
  * @see [[com.pavelfatin.fs.FileSystem]]
  */
class TmpFS extends FileSystem {
  private val rootDirectory = new HeapRootDirectory()

  def name = "TmpFS"

  def formatted = true

  def format() {
    rootDirectory.clear()
  }

  def root: Directory = rootDirectory

  def size = free + rootDirectory.size

  def free = Runtime.getRuntime.freeMemory()
}
