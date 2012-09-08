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
package wrapfs

import java.io
import java.io.RandomAccessFile

/** A wrapper for a host file system file.
  *
  * @param root the root directory of the wrapper file system
  * @param entry the host file
  *
  * @see [[com.pavelfatin.fs.File]]
  * @see [[com.pavelfatin.fs.internal.AbstractData]]
  * @see [[com.pavelfatin.fs.internal.AbstractFile]]
  * @see [[com.pavelfatin.fs.internal.wrapfs.EntryWrapper]]
  */
private class FileWrapper(val root: io.File, val entry: io.File)
  extends AbstractData(extendable = true) with AbstractFile with EntryWrapper {

  private var file: RandomAccessFile = _

  protected def doOpen() {
    file = new RandomAccessFile(entry, "rw")
  }

  protected def doClose() {
    file.close()
  }

  def length = if (opened) file.length else entry.length

  protected def doRead(position: Long, length: Int, buffer: Array[Byte], offset: Int) {
    if (position != file.getFilePointer) {
      file.seek(position)
    }
    file.read(buffer, offset, length)
  }

  protected def doWrite(position: Long, length: Int, buffer: Array[Byte], offset: Int) {
    if (position != file.getFilePointer) {
      file.seek(position)
    }
    file.write(buffer, offset, length)
  }

  protected def doTruncate(length: Long) {
    file.setLength(length)
  }
}
