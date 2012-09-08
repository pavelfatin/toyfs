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

class DataOutputStreamSpec extends FunSpec with ShouldMatchers with Helpers {
  describe("Data output stream") {
    describe("on writing a buffer") {
      it("should ensure that the buffer is not null") {
        val stream = makeStream("")
        evaluating { stream.write(null, 0, 0) } should produce [NullPointerException]
      }

      it("should ensure that offset is non-negative") {
        val stream = makeStream("")
        evaluating { stream.write(bytes(""), -1, 0) } should produce [IndexOutOfBoundsException]
      }

      it("should ensure that length is non-negative") {
        val stream = makeStream("")
        evaluating { stream.write(bytes(""), 0, -1) } should produce [IndexOutOfBoundsException]
      }

      it("should ensure that offset + length is less than or equal to buffer length") {
        val stream = makeStream("")
        evaluating { stream.write(bytes("01 02 03 04"), 2, 3) } should produce [IndexOutOfBoundsException]
      }

      it("should do nothing when length is zero") {
        val data = makeData("")
        val stream = makeStream(data)
        stream.write(bytes("01 02 03 04"), 0, 0)
        data.writes should equal (0)
      }

      it("should copy buffer content to data") {
        val data = makeData("00 00 00 00")
        val stream = makeStream(data)
        stream.write(bytes("01 02 03 04"), 0, 4)
        data should holdBytes ("01 02 03 04")
      }

      it("should interpret length argument as a number of bytes to wrte") {
        val data = makeData("00 00 00 00")
        val stream = makeStream(data)
        stream.write(bytes("01 02 03 04"), 0, 2)
        data should holdBytes ("01 02 00 00")
      }

      it("should interpret offset argument as a starting position in buffer") {
        val data = makeData("00 00 00 00")
        val stream = makeStream(data)
        stream.write(bytes("01 02 03 04"), 2, 2)
        data should holdBytes ("03 04 00 00")
      }

      it("should advance position on sequential writing") {
        val data = makeData("00 00 00 00")
        val stream = makeStream(data)
        stream.write(bytes("01 02"), 0, 2)
        stream.write(bytes("03 04"), 0, 2)
        data should holdBytes ("01 02 03 04")
      }

      it("should be able to append buffer content to data") {
        val data = makeData("")
        val stream = makeStream(data)
        stream.write(bytes("01 02 03 04"), 0, 4)
        data should holdBytes ("01 02 03 04")
      }

      it("should prohibit writing to a closed stream") {
        val stream = makeStream("")
        stream.close()
        evaluating { stream.write(bytes("01"), 0, 1) } should produce [IOException]
      }
    }

    describe("on single byte writing") {
      it("should copy byte to data") {
        val data = makeData("00")
        val stream = makeStream(data)
        stream.write(1)
        data should holdBytes ("01")
      }

      it("should advance position on sequential writing") {
        val data = makeData("00 00")
        val stream = makeStream(data)
        stream.write(1)
        stream.write(2)
        data should holdBytes ("01 02")
      }

      it("should be able to append a byte to data") {
        val data = makeData("")
        val stream = makeStream(data)
        stream.write(1)
        data should holdBytes ("01")
      }

      it("should prohibit writing to a closed stream") {
        val stream = makeStream("")
        stream.close()
        evaluating { stream.write(1) } should produce [IOException]
      }
    }
  }

  private def makeStream(bytes: String): DataOutputStream = makeStream(makeData(bytes))

  private def makeStream(data: Data): DataOutputStream = new DataOutputStream(data)

  private def makeData(bytes: String) = new ByteData(bytes, extendable = true) with DataStats

  private def holdBytes(bytes: String) = new Matcher[ByteData] with Matchers {
    def apply(data: ByteData) = equal(bytes)(data.presentation)
  }
}