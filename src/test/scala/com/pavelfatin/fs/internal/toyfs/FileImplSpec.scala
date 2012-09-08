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

import org.scalatest._
import org.scalatest.matchers._
import java.util.Calendar

class FileImplSpec extends FunSpec with ShouldMatchers with Helpers {
  private val Date = Calendar.getInstance

  describe("File") {
    describe("on property reading") {
      it("should propagate metadata.name") {
        val file = makeFile(makeMetadata(_.name = "readme"))
        file.name should equal ("readme")
      }

      it("should propagate metadata.length") {
        val file = makeFile(makeMetadata(_.length = 1024))
        file.length should equal (1024)
      }

      it("should propagate metadata.date") {
        val file = makeFile(makeMetadata(_.date = Date))
        file.date should equal (Date)
      }

      it("should propagate metadata.hidden") {
        val file = makeFile(makeMetadata(_.hidden = true))
        file.hidden should equal (true)
      }
    }

    describe("on property modification") {
      it("should update metadata.name") {
        val metadata = makeMetadata()
        val file = makeFile(metadata)
        file.name = "readme"
        metadata.name should equal ("readme")
      }

      it("should update metadata.date") {
        val metadata = makeMetadata()
        val file = makeFile(metadata)
        file.date = Date
        metadata.date should equal (Date)
      }

      it("should update metadata.hidden") {
        val metadata = makeMetadata()
        val file = makeFile(metadata)
        file.hidden = true
        metadata.hidden should equal (true)
      }
    }

    describe("(opening)") {
      it("should be initially closed") {
        val file = makeFile()
        assert (!file.opened)
      }

      it("should become opened after opening") {
        val file = makeFile()
        file.open()
        assert (file.opened)
      }

      it("should become closed after closing") {
        val file = makeFile()
        file.open()
        file.close()
        assert (!file.opened)
      }

      it("should prohibit closing when closed") {
        val file = makeFile()
        evaluating { file.close() } should produce [IllegalStateException]
      }

      it("should prohibit opening when opened") {
        val file = makeFile()
        file.open()
        evaluating { file.open() } should produce [IllegalStateException]
      }
    }

    describe("on reading") {
      it("should prohibit reading when closed") {
        val file = makeFile()
        evaluating { file.read(0, 0, buffer(1)) } should produce [IllegalStateException]
      }

      it("should ensure that arguments are valid") {
        val file = makeFile(makeMetadata(_.length = 2), makeChunk("01 02 03 04"))
        file.open()
        evaluating { file.read(0, 4, buffer(4)) } should produce [IllegalArgumentException]
        info ("assume that it relies on all the AbstractData validations")
        file.close()
      }

      it("should read data from the chunk") {
        val file = makeFile(makeMetadata(_.length = 4), makeChunk("01 02 03 04"))
        file.open()
        file.read(0, 4) should holdBytes("01 02 03 04")
        file.close()
      }
    }

    describe("on writing") {
      it("should prohibit writing when closed") {
        val file = makeFile()
        evaluating { file.write(0, 0, buffer(1)) } should produce [IllegalStateException]
      }

      it("should ensure that arguments are valid") {
        val file = makeFile(makeMetadata(_.length = 2), makeChunk("00 00 00 00"))
        file.open()
        evaluating { file.write(3, 1, bytes("04")) } should produce [IllegalArgumentException]
        info ("assume that it relies on all the AbstractData validations")
        file.close()
      }

      it("should write data to chunk") {
        val chunk = makeChunk("00 00 00 00")
        val file = makeFile(makeMetadata(_.length = 4), chunk)
        file.open()
        file.write(0, 4, bytes("01 02 03 04"))
        chunk should holdData ("01 02 03 04")
        file.close()
      }

      it("should be able to append data to chunk") {
        val chunk = makeChunk("01 02")
        val file = makeFile(makeMetadata(_.length = 2), chunk)
        file.open()
        file.write(2, 2, bytes("03 04"))
        chunk should holdData ("01 02 03 04")
        file.close()
      }

      it("should update metadata.length when file length is changed") {
        val metadata = makeMetadata(_.length = 2)
        val file = makeFile(metadata, makeChunk("01 02"))
        file.open()
        file.write(2, 2, bytes("03 04"))
        metadata.length should equal (4)
        file.close()
      }

      it("should not update metadata.length when file length is not changed") {
        val metadata = makeMetadata(_.length = 4)
        val file = makeFile(metadata, makeChunk("00 00 00 00"))
        file.open()
        file.write(0, 4, bytes("01 02 03 04"))
        metadata.length should equal (4)
        file.close()
      }
    }

    describe("on deletion") {
      it("should delete metadata and chunk") {
        val metadata = makeMetadata()
        val chunk = makeChunk()
        val file = makeFile(metadata, chunk)
        file.delete()
        assert (metadata.deleted)
        assert (chunk.deleted)
      }

      it("should not alter chunk data") {
        val chunk = makeChunk("01 02 03 04")
        val file = makeFile(makeMetadata(), chunk)
        file.delete()
        chunk should holdData ("01 02 03 04")
      }
    }

    describe("on truncation") {
      it("should prohibit truncation when closed") {
        val file = makeFile()
        evaluating { file.truncate(0) } should produce [IllegalStateException]
      }

      it("should truncate chunk") {
        val metadata = makeMetadata(_.length = 4)
        val chunk = makeChunk("01 02 03 04")
        val file = makeFile(metadata, chunk)
        file.open()
        file.truncate(2)
        chunk should holdData ("01 02")
        file.close()
      }

      it("should update metadata.length when data truncation occurs") {
        val record = makeMetadata(_.length = 4)
        val file = makeFile(record, makeChunk("01 02 03 04"))
        file.open()
        file.truncate(2)
        record.length should equal (2)
        file.close()
      }

      it("should not update metadata.length when no data truncation occurs") {
        val metadata = makeMetadata(_.length = 4)
        val file = makeFile(metadata, makeChunk("01 02 03 04"))
        file.open()
        file.truncate(8)
        metadata.length should equal (4)
        file.close()
      }
    }
  }

  private def makeFile(record: Metadata = makeMetadata(), chunk: Chunk = makeChunk()) =
    new FileImpl(null, record, chunk)

  private def makeMetadata(initializer: Metadata => Unit = _ => ()) = {
    val record = new MockMetadata()
    initializer(record)
    record
  }

  private def makeChunk(content: String = "") = new MockChunk(0, content)

  private def holdBytes(bytes: String) = new Matcher[Array[Byte]] with Matchers {
    def apply(array: Array[Byte]) = {
      val presentation = array.map(_.formatted("%02X")).mkString(" ")
      equal(bytes)(presentation)
    }
  }

  private def holdData(data: String) = new Matcher[MockChunk] with Matchers {
    def apply(chunk: MockChunk) = equal(data)(chunk.presentation)
  }
}