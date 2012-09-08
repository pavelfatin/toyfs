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
package internal.toyfs

import org.scalatest._
import org.scalatest.matchers._

class HeaderSpec extends FunSpec with ShouldMatchers with Helpers {
  describe("Header") {
    describe("on writing") {
      it("should ensure that data length is sufficient") {
        val data = new ByteData("")
        val header = Header("abcde", 1, 2, 3)
        evaluating { header.writeTo(data) } should produce [IllegalArgumentException]
      }

      it("should ensure that name length is less than or equal to 5") {
        val data = makeData()
        val header = Header("abcdef", 1, 2, 3)
        evaluating { header.writeTo(data) } should produce [IllegalStateException]
      }

      it("should ensure that name is not empty") {
        val data = makeData()
        val header = Header("", 1, 2, 3)
        evaluating { header.writeTo(data) } should produce [IllegalStateException]
      }

      it("should ensure that name contains only A-z characters") {
        val data = makeData()
        val header = Header(".", 1, 2, 3)
        evaluating { header.writeTo(data) } should produce [IllegalStateException]
      }

      it("should ensure that version is non-negative") {
        val data = makeData()
        val header = Header("abcde", -1, 2, 3)
        evaluating { header.writeTo(data) } should produce [IllegalStateException]
      }

      it("should ensure that cluster size is non-negative") {
        val data = makeData()
        val header = Header("abcde", 1, -2, 3)
        evaluating { header.writeTo(data) } should produce [IllegalStateException]
      }

      it("should ensure that max name length is non-negative") {
        val data = makeData()
        val header = Header("abcde", 1, 2, -3)
        evaluating { header.writeTo(data) } should produce [IllegalStateException]
      }

      it("should output Header.Length bytes of data") {
        val data = makeData()
        val header = Header("abcde", 1, 2, 3)
        header.writeTo(data)
        data.length should equal (Header.Length)
      }

      it("should properly save its properties") {
        val data = makeData()
        val header = Header("abcde", 1, 2, 3)
        header.writeTo(data)
        data should holdBytes ("61 62 63 64 65 01 00 00 00 02 00 00 00 03")
      }

      it("should names that are shorter than 5 with spaces") {
        val data = makeData()
        val header = Header("abc", 1, 2, 3)
        header.writeTo(data)
        data should holdBytes ("61 62 63 20 20 01 00 00 00 02 00 00 00 03")
      }
    }

    describe("on reading") {
      it("should ensure that data length is sufficient") {
        val data = makeData()
        evaluating { Header.readFrom(data) } should produce [IllegalArgumentException]
      }

      it("should properly load its properties") {
        val data = makeData("61 62 63 64 65 01 00 00 00 02 00 00 00 03")
        val header = Header.readFrom(data)
        header should equal (Header("abcde", 1, 2, 3))
      }

      it("should be able to load invalid properties") {
        val data = makeData("61 62 63 64 65 FF FF FF FF FE FF FF FF FD")
        val header = Header.readFrom(data)
        header should equal (Header("abcde", -1, -2, -3))
      }

      it("should trim trailing whitespaces in the name") {
        val data = makeData("61 62 63 20 20 01 00 00 00 02 00 00 00 03")
        val header = Header.readFrom(data)
        header should equal (Header("abc", 1, 2, 3))
      }
    }
  }

  private def makeData(content: String = "") = new ByteData(content, extendable = true)

  private def holdBytes(bytes: String) = new Matcher[ByteData] with Matchers {
    def apply(data: ByteData) = equal(bytes)(data.presentation)
  }
}