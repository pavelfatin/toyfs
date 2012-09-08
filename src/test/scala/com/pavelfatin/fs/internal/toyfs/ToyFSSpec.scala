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

class ToyFSSpec extends FunSpec with ShouldMatchers with Helpers {
  describe("File system") {
    describe("on initialiazation") {
      it("should ensure that data length is sufficient to accomodate a header") {
        val data = makeData(Header.Length - 1)
        evaluating { makeFileSystem(data) } should produce [IllegalArgumentException]
      }

      it("should not read or write data") {
        val data = makeData(256)
        makeFileSystem(data)
        data.reads should equal (0)
        data.writes should equal (0)
      }

      it("should be not formatted") {
        val fs = makeFileSystem(makeData(256))
        fs.formatted should equal (false)
      }
    }

    describe("on formatting state querying") {
      it("should cache formatting state") {
        val data = makeData(256)
        val fs = makeFileSystem(data)
        fs.formatted
        data.clearStats()
        fs.formatted
        data.reads should equal (0)
      }
    }

    describe("on formatting") {
      it("should write a header and clean the entity storage") {
        val data = makeData(Header.Length + 4 + 27)
        val fs = makeFileSystem(data)
        fs.format(27, 3)
        data should holdBytes(
          "54 6F 79 46 53 01 00 00 00 1B 00 00 00 03 " +
            "FF FF FF FE 01 00 20 00 20 00 20 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
      }

      it("should become formatted") {
        val fs = makeFileSystem(makeData(256))
        fs.format(32, 8)
        fs.formatted should equal (true)
      }

      it("should cache formatting state") {
        val data = makeData(256)
        val fs = makeFileSystem(data)
        fs.format(32, 8)
        data.clearStats()
        fs.formatted
        data.reads should equal (0)
      }

      it("should re-create a file system implementation") {
        val fs = makeFileSystem(makeData(256))
        fs.format(32, 8)
        val rootA = fs.root
        fs.format(32, 8)
        val rootB = fs.root
        rootA should not be theSameInstanceAs (rootB)
      }
    }

    describe("on properties reading") {
      it("should ensure that file system is formatted") {
        val fs = makeFileSystem(makeData(256))

        evaluating { fs.root } should produce [IllegalStateException]
        evaluating { fs.size } should produce [IllegalStateException]
        evaluating { fs.free } should produce [IllegalStateException]

        evaluating { fs.version } should produce [IllegalStateException]
        evaluating { fs.clusterSize } should produce [IllegalStateException]
        evaluating { fs.maxNameLength } should produce [IllegalStateException]
      }

      it("should cache a file system implementation") {
        val fs = makeFileSystem(makeData(256))
        fs.format(32, 8)
        val rootA = fs.root
        val rootB = fs.root
        rootA should be theSameInstanceAs rootB
      }
    }

    describe("on default cluster size calculation") {
      def clusterSizeFor(megabytes: Int) =
        ToyFS.defaultClusterSizeFor(megabytes * 1024L * 1024L)

      it("should ensure that data length is non-negative") {
        evaluating { clusterSizeFor(-1) } should produce [IllegalArgumentException]
      }

      it("should choose 512 bytes cluster size for zero data length") {
        clusterSizeFor(0) should equal (512)
      }

      it("should choose 512 bytes cluster size for (0, 32] MB data length") {
        clusterSizeFor(1) should equal (512)
        clusterSizeFor(32) should equal (512)
      }

      it("should choose 1 KB cluster size for (32, 64] MB data length") {
        clusterSizeFor(33) should equal (1024)
        clusterSizeFor(64) should equal (1024)
      }

      it("should choose 2 KB cluster size for (64, 128] MB data length") {
        clusterSizeFor(65) should equal (2 * 1024)
        clusterSizeFor(128) should equal (2 * 1024)
      }

      it("should choose 4 KB cluster size for (128, 256] MB data length") {
        clusterSizeFor(129) should equal (4 * 1024)
        clusterSizeFor(256) should equal (4 * 1024)
      }

      it("should choose 8 KB cluster size for (256, 512] MB data length") {
        clusterSizeFor(257) should equal (8 * 1024)
        clusterSizeFor(512) should equal (8 * 1024)
      }

      it("should choose 16 KB cluster size for (512, 1024] MB data length") {
        clusterSizeFor(513) should equal (16 * 1024)
        clusterSizeFor(1024) should equal (16 * 1024)
      }

      it("should choose 32 KB cluster size for (1024, 2048] MB data length") {
        clusterSizeFor(1025) should equal (32 * 1024)
        clusterSizeFor(2048) should equal (32 * 1024)
      }

      it("should choose 64 KB cluster size for (2048, 4096] MB data length") {
        clusterSizeFor(2049) should equal (64 * 1024)
        clusterSizeFor(4096) should equal (64 * 1024)
      }

      it("should choose 64KB cluster size for data length that is greater than 4 GB") {
        clusterSizeFor(4097) should equal (64 * 1024)
      }
    }
  }

  private def makeFileSystem(data: ByteData) = new ToyFS(data)

  private def makeData(size: Int) = new ByteData(size) with DataStats

  private def holdBytes(bytes: String) = new Matcher[ByteData] with Matchers {
    def apply(data: ByteData) = equal(bytes)(data.presentation)
  }
}