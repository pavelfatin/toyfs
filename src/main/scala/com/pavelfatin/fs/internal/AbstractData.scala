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

/** A template `Data` implementation.
  *
  * Provides argument validation and projection retrieval.
  *
  * @param extendable whether the length of this data can be increased by writing additional bytes at `length` position
  *                   (writing at positions greater than `length` is always prohibited).
  * @param lazyLength if true, the `length` of this data will not be queried in the process of argument validation
  *
  * @see [[com.pavelfatin.fs.Data]]
  */
abstract class AbstractData(extendable: Boolean = false, lazyLength: Boolean = false) extends Data {
  private def dataLength: Option[Long] = if (lazyLength) None else Some(this.length)

  def read(position: Long, length: Int, buffer: Array[Byte], offset: Int) {
    AbstractData.check(dataLength, buffer.length, position, length, offset, extendable = false)
    doRead(position, length, buffer, offset)
  }

  def write(position: Long, length: Int, buffer: Array[Byte], offset: Int) {
    AbstractData.check(dataLength, buffer.length, position, length, offset, extendable)
    doWrite(position, length, buffer, offset)
  }

  def projection(position: Long, length: Long): Data = {
    AbstractData.check(position, length, dataLength, extendable = false)
    new Projection(this, position, length)
  }

  /** An actual implementation of reading.
    *
    * @see [[com.pavelfatin.fs.Data#read]]
    */
  protected def doRead(position: Long, length: Int, buffer: Array[Byte], offset: Int = 0)

  /** An actual implementation of writing.
    *
    * @see [[com.pavelfatin.fs.Data#write]]
    */
  protected def doWrite(position: Long, length: Int, buffer: Array[Byte], offset: Int = 0)

  override def toString = s"${getClass.getSimpleName}($length)"
}

/** Argument validation methods. */
private object AbstractData {
  def check(dataLength: Option[Long], bufferLength: Int, position: Long, length: Int, offset: Int, extendable: Boolean) {
    check(position, length, dataLength, extendable)

    if (offset < 0)
      throw new IllegalArgumentException(
        s"Offset ia negative: $offset")

    if (offset + length > bufferLength)
      throw new IllegalArgumentException(
        s"Offset + length ($offset + $length) is greater than buffer length ($bufferLength)")
  }

  def check(position: Long, length: Long, dataLength: Option[Long], extendable: Boolean) {
    if (position < 0L)
      throw new IllegalArgumentException(
        s"Position is negative: $position")

    if (length < 0L)
      throw new IllegalArgumentException(
        s"Length is negative: $length")

    dataLength.foreach { total =>
      if (position > total)
        throw new IllegalArgumentException(
          s"Position ($position) is greater than data length ($total)")

      if (extendable) {
        if (length > (Long.MaxValue - position))
          throw new IllegalArgumentException(
            s"Position + length ($position + $length) is greater than the maximum Long value")
      } else {
        if (position + length > total)
          throw new IllegalArgumentException(
            s"Position + length ($position + $length) is greater than data length ($total)")
      }
    }
  }
}

/** A default projected data implementation.
  *
  * Performs position shifting and argument validation.
  *
  * @note The projected data cannot be extended even if the underlying data is extendable.
  *
  * @param data the underlying data
  * @param projectionPosition the projection position
  * @param projectionLength the projection length
  *
  * @see [[com.pavelfatin.fs.Data#projection]]
  * @see [[com.pavelfatin.fs.internal.AbstractData]]
  */
private class Projection(data: Data, projectionPosition: Long, projectionLength: Long) extends AbstractData {
  def length = projectionLength

  protected def doRead(position: Long, length: Int, buffer: Array[Byte], offset: Int) {
    data.read(projectionPosition + position, length, buffer, offset)
  }

  protected def doWrite(position: Long, length: Int, buffer: Array[Byte], offset: Int) {
    data.write(projectionPosition + position, length, buffer, offset)
  }

  override def toString = s"${data.toString}$$[$projectionPosition;${projectionPosition + length}]"
}