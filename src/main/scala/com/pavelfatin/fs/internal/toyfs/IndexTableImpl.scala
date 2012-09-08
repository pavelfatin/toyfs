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

import Entry._
import java.io.{BufferedOutputStream, BufferedInputStream}

/** An `IndexTable` implementation that uses `Data` as storage medium.
  *
  * The implementation uses 4 bytes per entry.
  *
  * The maximum table size is equal to `Int.MaxValue`.
  *
  * @note Maintains a starting index for the next `Free` entry scan.
  * @note Uses buffered I/O (for better performance).
  *
  * @param data the data used to store entries
  * @param validateReferences whether to check `Reference` entry indices
  * @throws IllegalArgumentException if entry count is greater than the maximum Int value
  *
  * @see [[com.pavelfatin.fs.Data]]
  * @see [[com.pavelfatin.fs.internal.toyfs.Entry]]
  * @see [[com.pavelfatin.fs.internal.toyfs.IndexTable]]
  */
private class IndexTableImpl(data: Data, validateReferences: Boolean = true) extends IndexTable {
  private val FreeMarker = 0xFFFFFFFF // -1
  private val EndMarker = 0xFFFFFFFE // -2

  private val EntrySize = 4

  private val buffer = new Array[Byte](EntrySize)

  private var fromIndex = 0

  if (entryCount > Int.MaxValue)
    throw new IllegalArgumentException(
      s"Entry count is greater than the maximum Int value: ${data.length}")

  private def entryCount: Long = data.length / EntrySize

  val size = entryCount.toInt

  def free = {
    val input = new BufferedInputStream(new DataInputStream(data))
    val count = Range(0, size).count { _ =>
      input.read(buffer)
      decodeIntFrom(buffer) == FreeMarker
    }
    input.close()
    count
  }

  def allocate() = {
    val index = freeEntryIndex(fromIndex)
    index.foreach(n => set(n, Entry.End))
    fromIndex = index.map(_ + 1).getOrElse(size)
    index
  }

  private def freeEntryIndex(from: Int): Option[Int] = {
    val input = new BufferedInputStream(new DataInputStream(data))
    input.skip(from.toLong * EntrySize)
    val count = Range(from, size).find { _ =>
      input.read(buffer)
      decodeIntFrom(buffer) == FreeMarker
    }
    input.close()
    count
  }

  def get(n: Int) = {
    checkIndex(n)

    val entry = entryFrom(readInt(n * EntrySize))

    if (validateReferences) {
      entry match {
        case Reference(target) =>
          checkReadReference(target, n)
        case _ => // OK
      }
    }

    entry
  }

  private def checkIndex(n: Int) {
    if (n < 0)
      throw new IndexOutOfBoundsException(
        s"Entry index is negative: $n")

    if (n >= size)
      throw new IndexOutOfBoundsException(
        s"Entry index ($n) is greater than or equal to table size ($size)")
  }

  private def checkReadReference(target: Int, n: Int) {
    if (target < 0) {
      throw new DataFormatException(
        s"Stored reference target is negative: $target")
    }

    if (target >= size) {
      throw new DataFormatException(
        s"Stored reference target ($target) is greater than or equal to table size ($size)")
    }

    if (target == n) {
      throw new DataFormatException(
        s"Stored reference target is equal to reference index: $target")
    }
  }

  def set(n: Int, entry: Entry) {
    checkIndex(n)

    entry match {
      case Reference(target) =>
        if (validateReferences) checkReferenceToWrite(n, target)
      case Free =>
        fromIndex = fromIndex.min(n)
      case _ => // OK
    }

    writeInt(n * EntrySize, numberFrom(entry))
  }

  private def checkReferenceToWrite(n: Int, target: Int) {
    if (target < 0) {
      throw new IllegalArgumentException(
        s"Reference target is negative: $target")
    }

    if (target >= size) {
      throw new IllegalArgumentException(
        s"Reference target is greater than or equal to table size: $size")
    }

    if (target == n) {
      throw new IllegalArgumentException(
        s"Reference target is equal to reference index: $target")
    }
  }

  def clear() {
    encodeIntTo(buffer, FreeMarker)
    val output = new BufferedOutputStream(new DataOutputStream(data))
    for (_ <- Range(0, size)) output.write(buffer, 0, EntrySize)
    output.close()
  }

  private def numberFrom(entry: Entry): Int = entry match {
    case Entry.Free => FreeMarker
    case Entry.End => EndMarker
    case Entry.Reference(index) => index
  }

  private def entryFrom(number: Int): Entry = number match {
    case FreeMarker => Entry.Free
    case EndMarker => Entry.End
    case it => Entry.Reference(it)
  }

  private def readInt(position: Int): Int = {
    data.read(position, EntrySize, buffer)
    decodeIntFrom(buffer)
  }

  private def decodeIntFrom(buffer: Array[Byte]): Int = {
    (toUnsignedInt(buffer(0)) << 24) +
      (toUnsignedInt(buffer(1)) << 16) +
      (toUnsignedInt(buffer(2)) << 8) +
      (toUnsignedInt(buffer(3)) << 0)
  }

  private def toUnsignedInt(byte: Byte): Int = if (byte < 0) byte + 0x100 else byte

  private def writeInt(position: Int, v: Int) {
    encodeIntTo(buffer, v)
    data.write(position, EntrySize, buffer)
  }

  private def encodeIntTo(buffer: Array[Byte], v: Int) {
    buffer(0) = ((v >>> 24) & 0xFF).toByte
    buffer(1) = ((v >>> 16) & 0xFF).toByte
    buffer(2) = ((v >>> 8) & 0xFF).toByte
    buffer(3) = ((v >>> 0) & 0xFF).toByte
  }
}
