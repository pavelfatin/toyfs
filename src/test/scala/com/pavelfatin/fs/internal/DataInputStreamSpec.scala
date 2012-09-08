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

import org.scalatest._
import org.scalatest.matchers._
import java.io.IOException

class DataInputStreamSpec extends FunSpec with ShouldMatchers with Helpers {
  describe("Data input stream") {
    describe("on reading to buffer") {
      it("should ensure that the buffer is not null") {
        val stream = makeStream("")
        evaluating { stream.read(null, 0, 0) } should produce [NullPointerException]
      }

      it("should ensure that offset is non-negative") {
        val stream = makeStream("")
        evaluating { stream.read(buffer(0), -1, 0) } should produce [IndexOutOfBoundsException]
      }

      it("should ensure that length is non-negative") {
        val stream = makeStream("")
        evaluating { stream.read(buffer(0), 0, -1) } should produce [IndexOutOfBoundsException]
      }

      it("should ensure that remaining buffer length is greater than or equal to length argument") {
        val stream = makeStream("")
        evaluating { stream.read(buffer(4), 2, 3) } should produce [IndexOutOfBoundsException]
      }

      it("should do nothing when length is zero") {
        val data = makeData("")
        val stream = makeStream(data)
        stream.read(buffer(0), 0, 0)
        data.reads should equal (0)
      }

      it("should return 0 when length is zero") {
        val stream = makeStream("")
        val count = stream.read(buffer(0), 0, 0)
        count should equal (0)
      }

      it("should do nothing when there are no more bytes to read") {
        val data = makeData("")
        val stream = makeStream(data)
        stream.read(buffer(1), 0, 1)
        data.reads should equal (0)
      }

      it("should return -1 when there are no more bytes to read") {
        val stream = makeStream("")
        val count = stream.read(buffer(1), 0, 1)
        count should equal (-1)
      }

      it("should copy data to buffer") {
        val stream = makeStream("01 02 03 04")
        val b = buffer(4)
        stream.read(b, 0, 4)
        b should holdBytes("01 02 03 04")
      }

      it("should interpret length argument as a number of bytes to read") {
        val stream = makeStream("01 02 03 04")
        val b = buffer(4)
        stream.read(b, 0, 2)
        b should holdBytes("01 02 00 00")
      }

      it("should interpret offset argument as a starting position in buffer") {
        val stream = makeStream("01 02 03 04")
        val b = buffer(4)
        stream.read(b, 2, 2)
        b should holdBytes("00 00 01 02")
      }

      it("should return the number of bytes read") {
        val stream = makeStream("01 02 03 04")
        val b = buffer(4)
        val count = stream.read(b, 0, 3)
        count should equal (3)
      }

      it("should be able to read less bytes that length specifies") {
        val stream = makeStream("01 02")
        val b = buffer(4)
        val count = stream.read(b, 0, 4)
        b should holdBytes("01 02 00 00")
        count should equal (2)
      }

      it("should advance reading position on sequential reading") {
        val stream = makeStream("01 02 03 04")
        val b = buffer(2)
        stream.read(b, 0, 2)
        stream.read(b, 0, 2)
        b should holdBytes("03 04")
      }

      it("should prohibit reading from a closed stream") {
        val stream = makeStream("01")
        stream.close()
        evaluating { stream.read(buffer(1), 0, 1) } should produce [IOException]
      }
    }

    describe("on single byte reading") {
      it("should return a byte read") {
        val stream = makeStream("01")
        stream.read() should equal (1)
      }

      it("should return -1 when there are no more bytes to read") {
        val stream = makeStream("")
        stream.read() should equal (-1)
      }

      it("should advance reading position on sequential reading") {
        val stream = makeStream("01 02")
        stream.read() should equal (1)
        stream.read() should equal (2)
      }

      it("should prohibit reading from a closed stream") {
        val stream = makeStream("01")
        stream.close()
        evaluating { stream.read() } should produce [IOException]
      }
    }

    describe("on available bytes count calculation") {
      it("should return a number of bytes available"){
        val stream = makeStream("01 02 03 04")
        val available = stream.available
        available should equal (4)
      }

      it("should decrease the number of available bytes after reading to buffer"){
        val stream = makeStream("01 02 03 04")
        stream.read(buffer(3), 0, 3)
        val available = stream.available
        available should equal (1)
      }

      it("should decrease the number of available bytes relying on actual read bytes count rather than length argument"){
        val stream = makeStream("01 02 03 04")
        stream.read(buffer(8), 0, 8)
        val available = stream.available
        available should equal (0)
      }

      it("should decrease the number of available bytes after reading a single byte"){
        val stream = makeStream("01 02 03 04")
        stream.read()
        val available = stream.available
        available should equal (3)
      }

      it("should not decrease the number of available bytes after reading past the end of data"){
        val stream = makeStream("01 02 03 04")
        stream.read(buffer(4), 0, 4)
        stream.read()
        val available = stream.available
        available should equal (0)
      }

      it("should prohibit querying available bytes count on a closed stream") {
        val stream = makeStream("01")
        stream.close()
        evaluating { stream.available } should produce [IOException]
      }
    }

    describe("on skipping") {
      it("should advance reading position by n bytes") {
        val stream = makeStream("01 02 03 04")
        stream.skip(2)
        val b = stream.read()
        b should equal (3)
      }

      it("should decrease the number of available bytes by n bytes") {
        val stream = makeStream("01 02 03 04")
        stream.skip(3)
        val available = stream.available
        available should equal (1)
      }

      it("should return the number of bytes skipped") {
        val stream = makeStream("01 02 03 04")
        val count = stream.skip(3)
        count should equal (3)
      }

      it("should be able to skip less bytes than n when there are no more bytes available") {
        val stream = makeStream("01 02 03 04")
        stream.skip(8) should equal (4)
        stream.read() should equal (-1)
      }

      it("should return zero when there's no more bytes to skip") {
        val stream = makeStream("")
        val count = stream.skip(4)
        count should equal (0)
      }

      it("should do nothing when n is 0") {
        val stream = makeStream("01 02 03 04")
        stream.skip(0) should equal (0)
        stream.read() should equal (1)
      }

      it("should do nothing when n is negative") {
        val stream = makeStream("01 02 03 04")
        stream.skip(-1)
        val b = stream.read()
        b should equal (1)
      }

      it("should retrun 0 when n is negative") {
        val stream = makeStream("01 02 03 04")
        val count = stream.skip(-1)
        count should equal (0)
      }

      it("should prohibit skipping on a closed stream") {
        val stream = makeStream("01")
        stream.close()
        evaluating { stream.skip(1) } should produce [IOException]
      }
    }
  }

  private def makeStream(bytes: String): DataInputStream = makeStream(makeData(bytes))

  private def makeStream(data: Data): DataInputStream = new DataInputStream(data)

  private def makeData(bytes: String) = new ByteData(bytes) with DataStats

  private def holdBytes(bytes: String) = new Matcher[Array[Byte]] with Matchers {
    def apply(array: Array[Byte]) = {
      val presentation = array.map(_.formatted("%02X")).mkString(" ")
      equal(bytes)(presentation)
    }
  }
}