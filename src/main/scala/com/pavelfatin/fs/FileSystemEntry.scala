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

import java.util.Calendar

/** А base interface for files and directories.
  *
  * Declares common properties and actions for file system entries.
  *
  * @see [[com.pavelfatin.fs.File]]
  * @see [[com.pavelfatin.fs.Directory]]
  */
trait FileSystemEntry {
  /** Returns either a parent directory of this entry, or `None` if this entry is a root directory. */
  def parent: Option[Directory]

  /** The name of this entry.
    *
    * @throws UnsupportedOperationException (on modification) if this entry is a root directory
    * @throws IllegalArgumentException (on modification) if the name is a duplicate or has a wrong format
    * @throws java.io.IOException if an I/O error occurs
    */
  var name: String

  /** The date of this entry.
    *
    * @throws UnsupportedOperationException (on modification) if this entry is a root directory
    * @throws java.io.IOException if an I/O error occurs
    */
  var date: Calendar

  /** The "hidden" attribute of this entry.
    *
    * @throws UnsupportedOperationException (on modification) if this entry is a root directory
    * @throws java.io.IOException if an I/O error occurs
    */
  var hidden: Boolean

  /** Deletes this entry from the file system.
    *
    * @note Does not guarantee that the entry content will be securely erased.
    *
    * @throws UnsupportedOperationException if this entry is a root directory
    * @throws IllegalStateException if this entry is a non-empty directory
    * @throws java.io.IOException if an I/O error occurs
    */
  def delete()
}
