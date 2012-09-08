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

/** A base random access data interface.
  *
  * Data is an indexed sequence of bytes that has a specific length and can be randomly read or written.
  *
  * `Data` implementation may allow to increase the data length (i.e. to extend the data)
  * by writing additional bytes at `length` position.
  *
  * @note Non-sequential access time may be not constant (depending on implementation).
  *
  * @define entity data
  *
  * @see [[com.pavelfatin.fs.File]]
  */
trait Data {
  /** Returns the length of this $entity (in bytes). */
  def length: Long

  /** Reads `length` bytes from this $entity at `position` into `buffer` at `offset`.
    *
    * @param position the start position in this $entity
    * @param length the number of bytes to read
    * @param buffer the buffer into which the data is read
    * @param offset the start offset in the buffer
    * @throws IllegalArgumentException if (position, length, offset) combination are invalid or conflicts
    *                                  either with this $entity length or with the buffer length
    * @throws java.io.IOException if an I/O error occurs
    */
  def read(position: Long, length: Int, buffer: Array[Byte], offset: Int = 0)

  /** Writes `length` bytes of `buffer` bytes at `offset` into this $entity at `position`.
    *
    * `Data` implementations may allow to extend this $entity by writing
    * additional bytes at the `length` position.
    *
    * @note Writing at positions greater than this $entity `length` is always prohibited.
    *
    * @param position the start position in this $entity
    * @param length the number of bytes to write
    * @param buffer the data to be written
    * @param offset the start offset in the data
    * @throws IllegalArgumentException if (position, length, offset) combination are invalid or conflicts
    *                                  either with this $entity length or with the buffer length
    * @throws java.io.IOException if an I/O error occurs
    */
  def write(position: Long, length: Int, buffer: Array[Byte], offset: Int = 0)

  /** Returns a new `Data` that is a projection on this $entity.
    *
    * @note The projected data cannot be extended even if this $entity is extendable.
    *
    * @param position the projection position
    * @param length the projection length
    * @throws IllegalArgumentException if (position, length) combination are invalid or conflicts
    *                                  with this $entity length
    */
  def projection(position: Long, length: Long): Data
}
