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

/** A template `File` implementation.
  *
  * Provides open/close state handling, argument validation and I/O streams.
  *
  * @see [[com.pavelfatin.fs.File]]
  */
trait AbstractFile extends File {
  private var _opened = false

  def opened = _opened

  def open() {
    if (_opened)
      throw new IllegalStateException(
        s"File '$name' is already opened")

    doOpen()

    _opened = true
  }

  def close() {
    if (!_opened)
      throw new IllegalStateException(
        s"File '$name' is already closed")

    doClose()

    _opened = false
  }

  abstract override def read(position: Long, length: Int, buffer: Array[Byte], offset: Int) {
    if (!opened) throw new IllegalStateException(s"Reading from a closed file '$name'")

    super.read(position, length, buffer, offset)
  }

  abstract override def write(position: Long, length: Int, buffer: Array[Byte], offset: Int) {
    if (!opened) throw new IllegalStateException(s"Writing to a closed file '$name'")

    super.write(position, length, buffer, offset)
  }

  override def truncate(length: Long) {
    if (!opened) throw new IllegalStateException(s"Truncating a closed file '$name'")

    if (length < 0L) throw new IllegalArgumentException(s"Truncation length is negative: $length")

    if (length < this.length) doTruncate(length)
  }

  /** An actual implementation of this $entity opening (may be empty).
    *
    * @see [[com.pavelfatin.fs.File#open]]
    */
  protected def doOpen()

  /** An actual implementation of this $entity closing (may be empty).
    *
    * @see [[com.pavelfatin.fs.File#open]]
    */
  protected def doClose()

  /** An actual implementation of this $entity truncation.
    *
    * @see [[com.pavelfatin.fs.File#truncate]]
    */
  protected def doTruncate(length: Long)

  def createInputStream() = new DataInputStream(this)

  def createOutputStream() = new DataOutputStream(this)
}
