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
package toyfs

import java.io._
import java.util.GregorianCalendar

/** A `RecordStorage` implementation that stores its records
  * as a sequence of bytes in a `Chunk`.
  *
  * @param chunk the chunk to use as a storage medium
  * @param maxNameLength the maximum allowed name length in this storage
  *
  * @see [[com.pavelfatin.fs.internal.toyfs.Chunk]]
  * @see [[com.pavelfatin.fs.internal.toyfs.RecordStorage]]
  */
private class RecordStorageImpl(chunk: Chunk, maxNameLength: Int) extends RecordStorage {
  if (maxNameLength < 1)
    throw new IllegalArgumentException(
      s"Maximum name length ($maxNameLength) is less than 1")

  private val RecordLength = 21 + maxNameLength * 2

  val nameLength = maxNameLength

  def get(index: Int) = {
    if (index < 0)
      throw new IndexOutOfBoundsException(
        s"Index must be non-negative: $index")

    val in = {
      val array = new Array[Byte](RecordLength)
      try {
        dataAt(index).read(0L, array.length, array)
      } catch {
        case e: IllegalArgumentException =>
          throw new IndexOutOfBoundsException(
            s"Record index is outside of chunk data: $index")
      }
      new DataInputStream(new ByteArrayInputStream(array))
    }

    val record = readFrom(in)
    in.close()

    if (record.length < 0L)
      throw new DataFormatException(
        s"Record length is negative: ${record.length}")

    if (record.chunk < 0)
      throw new DataFormatException(
        s"Record chunk is negative: ${record.chunk}")

    record
  }

  private def dataAt(index: Int): Data = chunk.projection(index.toLong * RecordLength, RecordLength)

  private def readFrom(in: DataInput): Record = {
    val flags = in.readByte()

    Record(
      tail = (flags & 1) > 0,
      deleted = (flags & 2) > 0,
      directory = (flags & 4) > 0,
      hidden = (flags & 8) > 0,
      name = Iterator.fill(maxNameLength)(in.readChar()).mkString.trim,
      length = in.readLong(),
      date = {
        val calendar = new GregorianCalendar()
        calendar.setTimeInMillis(in.readLong())
        calendar
      },
      chunk = in.readInt())
  }

  def set(index: Int, record: Record) {
    if (index < 0)
      throw new IndexOutOfBoundsException(
        s"Index is negative: $index")

    if (record.name.endsWith(" "))
      throw new IllegalArgumentException(
        s"Record name contains training whitespace(s): '${record.name}'")

    if (record.name.length > nameLength)
      throw new IllegalArgumentException(
        s"Record name length (${record.name.length}) is greater than maximum name length ($nameLength): '${record.name}'")

    if (record.length < 0L)
      throw new IllegalArgumentException(
        s"Record length is negative: ${record.length}")

    if (record.chunk < 0)
      throw new IllegalArgumentException(
        s"Record chunk is negative: ${record.chunk}")

    val array = {
      val bytes = new ByteArrayOutputStream(RecordLength)
      val out = new DataOutputStream(bytes)
      writeTo(out, record)
      out.close()
      bytes.toByteArray
    }

    val data = dataAt(index)

    try {
      data.write(0L, array.length, array)
    } catch {
      case e: IllegalArgumentException =>
        throw new IndexOutOfBoundsException(
          s"Record index is outside of chunk data: $index")
    }
  }

  private def writeTo(out: DataOutput, record: Record) {
    val flags = (if (record.tail) 1 else 0) +
      (if (record.deleted) 2 else 0) +
      (if (record.directory) 4 else 0) +
      (if (record.hidden) 8 else 0)

    out.writeByte(flags)
    out.writeChars(record.name.padTo(maxNameLength, " ").mkString)
    out.writeLong(record.length)
    out.writeLong(record.date.getTimeInMillis)
    out.writeInt(record.chunk)
  }

  def truncate(size: Int) {
    if (size < 0)
      throw new IllegalArgumentException(
        s"Truncation size is negative: $size")

    chunk.truncate(size.toLong * RecordLength)
  }
}
