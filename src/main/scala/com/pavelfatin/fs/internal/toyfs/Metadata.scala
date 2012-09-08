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

import java.util.Calendar

/** A holder of file system entry properties (for both files and directories).
  *
  * @see [[com.pavelfatin.fs.FileSystemEntry]]
  */
private trait Metadata {
  var name: String

  var length: Long

  var date: Calendar

  var hidden: Boolean

  /** Deletes this metadata from the parent storage.
    *
    * @note Does not guarantee that the metadata content will be securely erased.
    *
    * @throws IllegalStateException if this metadata is already deleted
    */
  def delete()
}
