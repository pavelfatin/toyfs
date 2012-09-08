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
package data

import java.io
import io.{Closeable, FileNotFoundException, RandomAccessFile}

/** A file-based `Data` implementation.
  *
  * Non-sequential access time may be not constant (depending on `file` storage medium).
  *
  * @note Must be closed after usage.
  *
  * @param file the underlying file
  * @throws java.io.FileNotFoundException if the file does not exist
  *
  * @see [[com.pavelfatin.fs.Data]]
  */
class FileData(file: io.File) extends AbstractData with Closeable {
  if (!file.exists)
    throw new FileNotFoundException(s"File does not exist: ${file.getPath}")

  private val data = new RandomAccessFile(file, "rw")

  def length = data.length

  protected def doRead(position: Long, length: Int, buffer: Array[Byte], offset: Int) {
    data.seek(position)
    data.read(buffer, offset, length)
  }

  protected def doWrite(position: Long, length: Int, buffer: Array[Byte], offset: Int) {
    if (position != data.getFilePointer) {
      data.seek(position)
    }
    data.write(buffer, offset, length)
  }

  def close() {
    data.close()
  }
}
