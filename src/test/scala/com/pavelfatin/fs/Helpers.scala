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

trait Helpers {
  def buffer(size: Int): Array[Byte] = new Array[Byte](size)

  def bytes(s: String): Array[Byte] = {
    s.split("[\\s|]+").filter(_.nonEmpty).map(Integer.parseInt(_, 16).toByte)
  }

  def zeros(count: Int): String = Iterator.fill(count)("00").mkString(" ")

  implicit def toRichData(data: Data) = new {
    def read(position: Long, length: Int, offset: Int = 0): Array[Byte] = {
      val buffer = new Array[Byte](offset + length)
      data.read(position, length, buffer, offset)
      buffer
    }

    def presentation: String = {
      val buffer = new Array[Byte](data.length.toInt)
      data.read(0L, data.length.toInt, buffer)
      buffer.map(_.formatted("%02X")).mkString(" ")
    }
  }
}
