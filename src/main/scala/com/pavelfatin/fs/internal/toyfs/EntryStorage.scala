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

/** A core file system implementation (without a header).
  *
  * @see [[com.pavelfatin.fs.FileSystem]]
  * @see [[com.pavelfatin.fs.internal.toyfs.EntryStorageImpl]]
  */
private trait EntryStorage {
  /** Returns the total number of bytes in this file system.
    *
    * @note This value excludes the size of overhead data structures.
    *
    * @see [[com.pavelfatin.fs.FileSystem#size]]
    */
  def size: Long

  /** Returns the number of unallocated bytes in this file system.
    *
    * @note This value is an estimation. The actual number of available
    *       bytes may be less than the returned value.
    *
    * @see [[com.pavelfatin.fs.FileSystem#free]]
    */
  def free: Long

  /** Returns a root directory in this file system. */
  def root: Directory
}
