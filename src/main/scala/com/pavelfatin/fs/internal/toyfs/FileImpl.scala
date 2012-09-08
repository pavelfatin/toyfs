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

/** A `File` implementation.
  *
  * All the file properties (including `length`) are stored in a given `Metadata` instance.
  *
  * The content of this file is stored in a given `Chunk`.
  *
  * @param owner the parent directory of this file
  * @param metadata the metadata of this file
  * @param chunk the chunk for storing the content of this file
  *
  * @see [[com.pavelfatin.fs.File]]
  * @see [[com.pavelfatin.fs.Directory]]
  * @see [[com.pavelfatin.fs.internal.AbstractFile]]
  * @see [[com.pavelfatin.fs.internal.toyfs.Metadata]]
  * @see [[com.pavelfatin.fs.internal.toyfs.MetaProperties]]
  * @see [[com.pavelfatin.fs.internal.toyfs.Chunk]]
  * @see [[com.pavelfatin.fs.internal.toyfs.Metadata]]
  */
private class FileImpl(owner: Directory,
                       metadata: Metadata,
                       chunk: Chunk) extends AbstractData(extendable = true) with AbstractFile with MetaProperties {
  protected def meta = metadata

  def parent = Some(owner)

  def delete() {
    metadata.delete()
    chunk.delete()
  }

  def length = metadata.length

  protected def doOpen() {}

  protected def doClose() {}

  protected def doRead(position: Long, length: Int, buffer: Array[Byte], offset: Int) {
    chunk.read(position, length, buffer, offset)
  }

  protected def doWrite(position: Long, length: Int, buffer: Array[Byte], offset: Int) {
    chunk.write(position, length, buffer, offset)

    val delta = position - metadata.length + length

    if (delta > 0L) {
      metadata.length += delta
    }
  }

  protected def doTruncate(length: Long) {
    chunk.truncate(length)
    metadata.length = length
  }
}
