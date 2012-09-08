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

class RecordStorageImplSpec extends FunSpec with ShouldMatchers with Helpers {
  private val Date = {
    val calendar = Calendar.getInstance
    calendar.setTimeInMillis(0x0102030405060708L)
    calendar
  }

  describe("Record storage") {
    describe("on initiaization") {
      it("should ensure that maximum name length is positive") {
        evaluating { new RecordStorageImpl(makeChunk(), 0) } should produce [IllegalArgumentException]
      }
    }

    describe("on writing") {
      it("should ensure that index is non-negative") {
        val chunk = makeChunk()
        val storage = makeStorage(chunk)
        evaluating { storage.set(-1, Record()) } should produce [IndexOutOfBoundsException]
      }

      it("should ensure that index is not greater than storage size") {
        val chunk = makeChunk()
        val storage = makeStorage(chunk)
        evaluating { storage.set(1, Record()) } should produce [IndexOutOfBoundsException]
      }

      it("should properly save a default record") {
        val chunk = makeChunk()
        val storage = makeStorage(chunk)
        storage.set(0, Record())
        chunk should holdData ("00 00 20 00 20 00 20 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
      }

      it("should properly save record.tail property") {
        val chunk = makeChunk()
        val storage = makeStorage(chunk)
        storage.set(0, Record(tail = true))
        chunk should holdData ("01 00 20 00 20 00 20 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
      }

      it("should properly save record.deleted property") {
        val chunk = makeChunk()
        val storage = makeStorage(chunk)
        storage.set(0, Record(deleted = true))
        chunk should holdData ("02 00 20 00 20 00 20 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
      }

      it("should properly save record.directory property") {
        val chunk = makeChunk()
        val storage = makeStorage(chunk)
        storage.set(0, Record(directory = true))
        chunk should holdData ("04 00 20 00 20 00 20 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
      }

      it("should properly save record.hidden property") {
        val chunk = makeChunk()
        val storage = makeStorage(chunk)
        storage.set(0, Record(hidden = true))
        chunk should holdData ("08 00 20 00 20 00 20 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
      }

      it("should properly save multiple flag bits") {
        val chunk = makeChunk()
        val storage = makeStorage(chunk)
        storage.set(0, Record(tail = true, deleted = true, directory = true, hidden = true))
        chunk should holdData ("0F 00 20 00 20 00 20 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
      }

      it("should properly save record.name property") {
        val chunk = makeChunk()
        val storage = makeStorage(chunk)
        storage.set(0, Record(name = "abc"))
        chunk should holdData ("00 00 61 00 62 00 63 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
      }

      it("should append whitespaces to names that are shorter than maximum name length") {
        val chunk = makeChunk()
        val storage = makeStorage(chunk)
        storage.set(0, Record(name = "ab"))
        chunk should holdData ("00 00 61 00 62 00 20 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
      }

      it("should prohibit saving names with length that are greater than maximum name length") {
        val chunk = makeChunk()
        val storage = makeStorage(chunk)
        evaluating { storage.set(0, Record(name = "abcd")) } should produce [IllegalArgumentException]
      }

      it("should prohibit saving names with trailing whitespaces") {
        val chunk = makeChunk()
        val storage = makeStorage(chunk)
        evaluating { storage.set(0, Record(name = "ab ")) } should produce [IllegalArgumentException]
      }

      it("should properly save record.length property") {
        val chunk = makeChunk()
        val storage = makeStorage(chunk)
        storage.set(0, Record(length = 0x0102030405060708L))
        chunk should holdData ("00 00 20 00 20 00 20 01 02 03 04 05 06 07 08 00 00 00 00 00 00 00 00 00 00 00 00")
      }

      it("should ensure that record.length is non-negative") {
        val chunk = makeChunk()
        val storage = makeStorage(chunk)
        evaluating { storage.set(0, Record(length = -1)) } should produce [IllegalArgumentException]
      }

      it("should properly save record.date property") {
        val chunk = makeChunk()
        val storage = makeStorage(chunk)
        storage.set(0, Record(date = Date))
        chunk should holdData ("00 00 20 00 20 00 20 00 00 00 00 00 00 00 00 01 02 03 04 05 06 07 08 00 00 00 00")
      }

      it("should properly save record.chunk property") {
        val chunk = makeChunk()
        val storage = makeStorage(chunk)
        storage.set(0, Record(chunk = 0x01020304))
        chunk should holdData ("00 00 20 00 20 00 20 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 02 03 04")
      }

      it("should ensure that record.chunk is non-negative") {
        val chunk = makeChunk()
        val storage = makeStorage(chunk)
        evaluating { storage.set(0, Record(chunk = -1)) } should produce [IllegalArgumentException]
      }

      it("should write subsequent record at record length offset") {
        val prefix = zeros(27)
        val chunk = makeChunk(prefix)
        val storage = makeStorage(chunk)
        storage.set(1, Record())
        chunk should holdData (prefix + " 00 00 20 00 20 00 20 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
      }

      it("should save a record in a single write") {
        val chunk = makeChunk()
        val storage = makeStorage(chunk)
        storage.set(0, Record())
        chunk.data.writes should equal (1)
      }
    }

    describe("on reading") {
      it("should ensure that index is non-negative") {
        val chunk = makeChunk()
        val storage = makeStorage(chunk)
        evaluating { storage.get(-1) } should produce [IndexOutOfBoundsException]
      }

      it("should ensure that index is less than storage size") {
        val chunk = makeChunk()
        val storage = makeStorage(chunk)
        evaluating { storage.get(0) } should produce [IndexOutOfBoundsException]
      }

      it("should ensure that a whole record length can be read") {
        val chunk = makeChunk("00 00 00 00 00")
        val storage = makeStorage(chunk)
        evaluating { storage.get(0) } should produce [IndexOutOfBoundsException]
      }

      it("should properly load a default record") {
        val chunk = makeChunk("00 00 20 00 20 00 20 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
        val storage = makeStorage(chunk)
        val record = storage.get(0)
        record should equal (Record())
      }

      it("should properly load record.tail property") {
        val chunk = makeChunk("01 00 20 00 20 00 20 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
        val storage = makeStorage(chunk)
        val record = storage.get(0)
        record should equal (Record(tail = true))
      }

      it("should properly load record.deleted property") {
        val chunk = makeChunk("02 00 20 00 20 00 20 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
        val storage = makeStorage(chunk)
        val record = storage.get(0)
        record should equal (Record(deleted = true))
      }

      it("should properly load record.directory property") {
        val chunk = makeChunk("04 00 20 00 20 00 20 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
        val storage = makeStorage(chunk)
        val record = storage.get(0)
        record should equal (Record(directory = true))
      }

      it("should properly load record.hidden property") {
        val chunk = makeChunk("08 00 20 00 20 00 20 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
        val storage = makeStorage(chunk)
        val record = storage.get(0)
        record should equal (Record(hidden = true))
      }

      it("should properly load multiple flag bits") {
        val chunk = makeChunk("0F 00 20 00 20 00 20 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
        val storage = makeStorage(chunk)
        val record = storage.get(0)
        record should equal (Record(tail = true, deleted = true, directory= true, hidden = true))
      }

      it("should properly load record.name property") {
        val chunk = makeChunk(" 00 00 61 00 62 00 63 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
        val storage = makeStorage(chunk)
        val record = storage.get(0)
        record should equal (Record(name = "abc"))
      }

      it("should properly load record.length property") {
        val chunk = makeChunk("00 00 20 00 20 00 20 01 02 03 04 05 06 07 08 00 00 00 00 00 00 00 00 00 00 00 00")
        val storage = makeStorage(chunk)
        val record = storage.get(0)
        record should equal (Record(length = 0x0102030405060708L))
      }

      it("should ensure that record.length is non-negative") {
        val chunk = makeChunk("00 00 20 00 20 00 20 FF FF FF FF FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00")
        val storage = makeStorage(chunk)
        evaluating { storage.get(0) } should produce [DataFormatException]
      }

      it("should properly load record.date property") {
        val chunk = makeChunk("00 00 20 00 20 00 20 00 00 00 00 00 00 00 00 01 02 03 04 05 06 07 08 00 00 00 00")
        val storage = makeStorage(chunk)
        val record = storage.get(0)
        record should equal (Record(date = Date))
      }

      it("should properly load record.chunk property") {
        val chunk = makeChunk("00 00 20 00 20 00 20 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 02 03 04")
        val storage = makeStorage(chunk)
        val record = storage.get(0)
        record should equal (Record(chunk = 0x01020304))
      }

      it("should ensure that record.chunk is non-negative") {
        val chunk = makeChunk("00 00 20 00 20 00 20 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 FF FF FF FF")
        val storage = makeStorage(chunk)
        evaluating { storage.get(0) } should produce [DataFormatException]
      }

      it("should read subsequent record at record length offset") {
        val chunk = makeChunk(zeros(27) + " 00 00 20 00 20 00 20 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
        val storage = makeStorage(chunk)
        val record = storage.get(1)
        record should equal (Record())
      }

      it("should load a record in a single read") {
        val chunk = makeChunk("00 00 20 00 20 00 20 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
        val storage = makeStorage(chunk)
        storage.get(0)
        chunk.data.reads should equal (1)
      }
    }

    describe("on truncation") {
      it("should ensure that truncation size is non-negative") {
        val chunk = makeChunk("")
        val storage = makeStorage(chunk)
        evaluating { storage.truncate(-1) } should produce [IllegalArgumentException]
      }

      it("should truncate chunk using multiples of record length") {
        val chunk = makeChunk(zeros(27 * 2))
        val storage = makeStorage(chunk)
        storage.truncate(1)
        chunk.presentation should equal (zeros(27))
      }
    }
  }

  private def makeStorage(chunk: MockChunk) = new RecordStorageImpl(chunk, 3)

  private def makeChunk(content: String = "") = new MockChunk(0, content)

  private def holdData(data: String) = new Matcher[MockChunk] with Matchers {
    def apply(chunk: MockChunk) = equal(data)(chunk.presentation)
  }
}