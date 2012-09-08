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

/** A simple FAT-like file system implementation.
  *
  * @param data the storage medium
  *
  * @see [[http://en.wikipedia.org/wiki/File_Allocation_Table]]
  * @see [[com.pavelfatin.fs]]
  */
class ToyFS(data: Data) extends FileSystem {
  private val FileSystemName = "ToyFS"
  private val FileSystemVersion = 1

  private val DefaultMaxNameLength = 128

  if (data.length < Header.Length)
    throw new IllegalArgumentException("Not enough space for header")

  private val (headerData, storageData) = splitAt(data, Header.Length)

  private var cachedHeader: Option[Header] = None
  private var cachedStorage: Option[EntryStorage] = None

  def name = FileSystemName

  def formatted = {
    val header = getHeader
    header.name == FileSystemName &&
      header.version == FileSystemVersion
  }

  def version: Int = if (formatted) getHeader.version else
    throw new IllegalStateException("Filesystem is not formatted")

  def clusterSize: Int = if (formatted) getHeader.clusterSize else
    throw new IllegalStateException("Filesystem is not formatted")

  def maxNameLength: Int = if (formatted) getHeader.maxNameLength else
    throw new IllegalStateException("Filesystem is not formatted")

  private def getHeader: Header = {
    cachedHeader.getOrElse {
      val header = Header.readFrom(headerData)
      cachedHeader = Some(header)
      header
    }
  }

  def format() {
    format(ToyFS.defaultClusterSizeFor(data.length), DefaultMaxNameLength)
  }

  def format(clusterSize: Int, maxNameLength: Int) {
    writeHeader(clusterSize, maxNameLength)
    val storage = EntryStorageImpl.format(storageData, clusterSize, maxNameLength)
    cachedStorage = Some(storage)
  }

  private def writeHeader(clusterSize: Int, maxNameLength: Int) {
    val header = Header(FileSystemName, FileSystemVersion, clusterSize, maxNameLength)
    header.writeTo(headerData)
    cachedHeader = Some(header)
  }

  def root = getStorage.root

  def size = getStorage.size

  def free = getStorage.free

  private def getStorage: EntryStorage = {
    cachedStorage.getOrElse {
      if (formatted) {
        val header = getHeader
        val storage = EntryStorageImpl.mount(storageData, header.clusterSize, header.maxNameLength)
        cachedStorage = Some(storage)
        storage
      } else {
        throw new IllegalStateException("Filesystem is not formatted")
      }
    }
  }

  private def splitAt(data: Data, position: Int): (Data, Data) = {
    (data.projection(0L, position), data.projection(position, data.length - position))
  }
}

private object ToyFS {
  def defaultClusterSizeFor(length: Long): Int = {
    if (length < 0L)
      throw new IllegalArgumentException(
        s"Length is negative: $length")

    val megabytes: Long = (length.toDouble / 1024.0D / 1024.0D).round

    megabytes match {
      case n if n <= 32L => 512
      case n if n >= 4096L => 64 * 1024
      case n =>
        val p: Double = math.log(n.toDouble) / math.log(2.0D)
        math.pow(2.0D, p.ceil - 5.0D).round.toInt * 512
    }
  }
}
