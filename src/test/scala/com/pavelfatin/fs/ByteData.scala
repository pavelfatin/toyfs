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

import internal.AbstractData
import ByteData._

class ByteData(bytes: Array[Byte], extendable: Boolean,
               lazyLength: Boolean, groupLength: Option[Int]) extends AbstractData(extendable, lazyLength) {

  def this(bytes: String, extendable: Boolean = false,
           lazyLength: Boolean = false, groupLength: Option[Int] = None) {
    this(parse(bytes), extendable, lazyLength, groupLength)
  }

  def this(size: Int) {
    this(new Array[Byte](size), false, false, None)
  }

  private val storage = bytes.toBuffer

  def length = storage.length

  protected def doRead(position: Long, length: Int, buffer: Array[Byte], offset: Int) {
    if (position + length - 1 >= storage.length)
      throw new IllegalArgumentException("Reading past the data boundary")
    for (i <- (0 until length))
      buffer(offset + i) = storage(position.toInt + i)
  }

  protected def doWrite(position: Long, length: Int, buffer: Array[Byte], offset: Int) {
    for (i <- (0 until length)) {
      val b = buffer(offset + i)
      val index = position.toInt + i
      if (index > storage.size) throw new IllegalArgumentException("Writing past the data boundary")
      if (extendable && index == storage.size) storage += b else storage(index) = b
    }
  }

  def truncate(threshold: Long) {
    if (threshold < storage.size)
      storage.remove(threshold.toInt, storage.size - threshold.toInt)
  }

  def clear() {
    val buffer = new Array[Byte](storage.length)
    write(0L, storage.length, buffer)
  }

  def presentation: String = {
    val groups = groupLength.map(storage.grouped).getOrElse(Iterator(storage))
    groups.map(_.map(_.formatted("%02X")).mkString(" ")).mkString(" | ")
  }

  override def toString = s"${getClass.getSimpleName}($presentation)"
}

object ByteData {
  def parse(bytes: String): Array[Byte] =
    bytes.split("[\\s|]+").filter(_.nonEmpty).map(Integer.parseInt(_, 16).toByte)
}