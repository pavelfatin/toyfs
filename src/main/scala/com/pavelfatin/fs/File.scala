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

/** A base file interface.
  *
  * File is a file system entry that contains data (in addition to its metadata properties).
  *
  * Files must be opened before performing I/O and closed afterwards.
  *
  * `File` interface includes a data streaming API for convenient data reading and writing.
  *
  * @define entity file
  *
  * @see [[com.pavelfatin.fs.FileSystemEntry]]
  * @see [[com.pavelfatin.fs.Data]]
  * @see [[com.pavelfatin.fs.OpenAndClose]]
  * @see [[com.pavelfatin.fs.Streams]]
  * @see [[com.pavelfatin.fs.StreamIO]]
  */
trait File extends FileSystemEntry with Data with OpenAndClose with Streams with StreamIO {
  /** Truncates this file.
    *
    * If the present length of this file is less than the `length` argument
    * then no action will be performed.
    *
    * @note Does not guarantee that the truncated content will be securely erased.
    *
    * @param length the new length of this file
    * @throws IllegalStateException if this file is closed
    * @throws IllegalArgumentException if 'length' argument is negative
    * @throws java.io.IOException if an I/O error occurs
    */
  def truncate(length: Long)
}