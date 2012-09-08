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

/** A ToyFS header data. */
private case class Header(name: String, version: Int, clusterSize: Int, maxNameLength: Int) {
  def writeTo(data: Data) {
    checkProperties()

    val array = {
      val bytes = new ByteArrayOutputStream(Header.Length)
      val out = new DataOutputStream(bytes)
      writeTo(out)
      out.close()
      bytes.toByteArray
    }

    data.write(0L, array.length, array)
  }

  private def checkProperties() {
    if (name.isEmpty)
      throw new IllegalStateException(
        "Name is empty")

    if (name.length > 5)
      throw new IllegalStateException(
        s"Name length (${name.length}) is greater than 5: '$name'")

    if (name.exists(c => c < 'A' || c > 'z'))
      throw new IllegalStateException(
        s"Name contains non A-z character(s): '$name'")

    if (version < 0)
      throw new IllegalStateException(
        s"Version is negative: $version")

    if (clusterSize < 0)
      throw new IllegalStateException(
        s"Cluster size is negative: $clusterSize")

    if (maxNameLength < 0)
      throw new IllegalStateException(
        s"Maximum name length is negative: $maxNameLength")
  }

  private def writeTo(out: DataOutputStream) {
    out.writeBytes(name.padTo(5, " ").mkString)
    out.writeByte(version)
    out.writeInt(clusterSize)
    out.writeInt(maxNameLength)
  }
}

private object Header {
  def Length: Int = 14

  def readFrom(data: Data): Header = {
    val in = {
      val array = new Array[Byte](Length)
      data.read(0L, array.length, array)
      new DataInputStream(new ByteArrayInputStream(array))
    }
    val header = readFrom(in)
    in.close()
    header
  }

  private def readFrom(in: DataInput): Header = {
    Header(name = Iterator.fill(5)(in.readByte().toChar).mkString.trim,
      version = in.readByte(),
      clusterSize = in.readInt(),
      maxNameLength = in.readInt())
  }
}
