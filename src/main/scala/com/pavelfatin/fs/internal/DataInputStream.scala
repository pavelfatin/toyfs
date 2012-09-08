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

import java.io.{IOException, InputStream}

/** An input stream for reading raw bytes from `Data`.
  *
  * @param data the source data
  *
  * @see [[java.io.InputStream]]
  * @see [[com.pavelfatin.fs.Data]]
  */
class DataInputStream(data: Data) extends InputStream {
  private val buffer = new Array[Byte](1)
  private var position = 0L
  private var closed = false

  def read() = {
    if (available == 0) -1 else {
      read(buffer, 0, 1)
      buffer(0)
    }
  }

  override def read(b: Array[Byte], off: Int, len: Int): Int = {
    if (closed)
      throw new IOException(
        "Reading from a closed stream")

    if (b == null)
      throw new NullPointerException(
        "Buffer is null")

    if (off < 0)
      throw new IndexOutOfBoundsException(
        s"Offset is negative: $off")

    if (len < 0)
      throw new IndexOutOfBoundsException(
        s"Length is negative: $len")

    if (len > b.length - off)
      throw new IndexOutOfBoundsException(
        s"Length ($len) is greater than b.length - offset ($b.length - $off)")

    if (len == 0) return 0

    val remaining = available

    if (remaining <= 0) return -1

    val toRead = len.min(remaining)
    data.read(position, toRead, b, off)
    position += toRead
    toRead
  }

  override def available = {
    if (closed)
      throw new IOException(
        "Available bytes querying on a closed stream")

    longAvailable.min(Int.MaxValue).toInt
  }

  private def longAvailable: Long = data.length - position

  override def skip(n: Long): Long = {
    if (closed)
      throw new IOException(
        "Skipping on a closed stream")

    if (n < 0L) return 0L
    val delta = n.min(longAvailable)
    position += delta
    delta
  }

  override def close() {
    closed = true
  }
}
