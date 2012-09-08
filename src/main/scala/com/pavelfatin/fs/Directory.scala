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

/** A base directory interface.
  *
  * Directory is a file system entry that can contain children entries.
  *
  * @see [[com.pavelfatin.fs.FileSystemEntry]]
  */
trait Directory extends FileSystemEntry {
  /** Returns files and directories in this directory.
    *
    * @throws java.io.IOException if an I/O error occurs
    * @throws com.pavelfatin.fs.DataFormatException if data format error occurs
    */
  def entries: (Seq[Directory], Seq[File])

  /** Creates a new file in this directory.
    *
    * @param name the name of the file
    * @param date the date of the file
    * @return a created file
    * @throws IllegalArgumentException if the name is a duplicate or has a wrong format
    * @throws com.pavelfatin.fs.NotEnoughSpaceException if there's not enough free space to to allocate a file
    * @throws java.io.IOException if an I/O error occurs
    */
  def createFile(name: String, date: Calendar): File

  /** Creates a new directory in this directory.
    *
    * @param name the name of the directory
    * @param date the date of the directory
    * @return a created directory
    * @throws IllegalArgumentException if the name is a duplicate or has a wrong format
    * @throws com.pavelfatin.fs.NotEnoughSpaceException if there's not enough free space to to allocate a directory
    * @throws java.io.IOException if an I/O error occurs
    */
  def createDirectory(name: String, date: Calendar): Directory
}
