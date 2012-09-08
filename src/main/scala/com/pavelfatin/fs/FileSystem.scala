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

/** А general file system interface.
  *
  * @see [[com.pavelfatin.fs.File]]
  * @see [[com.pavelfatin.fs.Directory]]
  */
trait FileSystem {
  /** Returns the name of this file system implementation. */
  def name: String

  /** Tests whether this file system is formatted.
    *
    * @throws java.io.IOException if an I/O error occurs
    */
  def formatted: Boolean

  /** Formats this file system.
    *
    * When an implementation uses no intermediate data format
    * it simply clears all entries on this file system.
    *
    * @note Does not guarantee that all the content will be securely erased.
    *
    * @throws java.io.IOException if an I/O error occurs
    * @throws com.pavelfatin.fs.NotEnoughSpaceException if there is
    *         not enough space to allocate underlying data structures
    */
  def format()

  /** Returns a root directory in this file system.
    *
    * @throws IllegalStateException if this file system is not formatted
    * @throws java.io.IOException if an I/O error occurs
    * @throws com.pavelfatin.fs.DataFormatException if data format error occurs
    */
  def root: Directory

  /** Returns the total number of bytes in this file system.
    *
    * @note This value excludes the size of overhead data structures.
    *
    * @throws IllegalStateException if this file system is not formatted
    * @throws java.io.IOException if an I/O error occurs
    * @throws com.pavelfatin.fs.DataFormatException if data format error occurs
    */
  def size: Long

  /** Returns the number of unallocated bytes in this file system.
    *
    * @note This value is an estimation. The actual number of available
    *       bytes may be less than the returned value.
    *
    * @throws IllegalStateException if this file system is not formatted
    * @throws java.io.IOException if an I/O error occurs
    * @throws com.pavelfatin.fs.DataFormatException if data format error occurs
    */
  def free: Long
}
