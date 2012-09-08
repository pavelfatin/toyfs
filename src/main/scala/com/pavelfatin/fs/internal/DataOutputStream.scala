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

import java.io.{IOException, OutputStream}

/** An output stream for writing raw bytes to `Data`.
  *
  * @param data the destination data
  *
  * @see [[java.io.OutputStream]]
  * @see [[com.pavelfatin.fs.Data]]
  */
class DataOutputStream(data: Data) extends OutputStream {
  private val buffer = new Array[Byte](1)
  private var position = 0L
  private var closed = false

  def write(b: Int) {
    buffer(0) = b.toByte
    write(buffer, 0, 1)
  }

  override def write(b: Array[Byte], off: Int, len: Int) {
    if (closed)
      throw new IOException(
        "Writing to a closed stream")

    if (b == null)
      throw new NullPointerException(
        "Buffer is null")

    if (off < 0)
      throw new IndexOutOfBoundsException(
        s"Offset is negative: $off")

    if (len < 0)
      throw new IndexOutOfBoundsException(
        s"Length is negative: $len")

    if (off + len > b.length)
      throw new IndexOutOfBoundsException(
        s"Offset + length ($off + $len) is greater than buffer length (${b.length})")

    if (len == 0) return

    data.write(position, len, b, off)

    position += len
  }

  override def close() {
    closed = true
  }
}
