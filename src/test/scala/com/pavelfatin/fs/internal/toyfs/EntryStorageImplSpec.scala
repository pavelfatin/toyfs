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

class EntryStorageImplSpec extends FunSpec with ShouldMatchers with Helpers {
  describe("Entry storage") {
    describe("on mounting") {
      it("should properly determine a number of clusters for a given data length") {
        val data = makeData((4 + 16) * 10)
        val storage = EntryStorageImpl.mount(data, 16, 8)
        storage.clusterCount should equal (10)
      }

      it("should ensure that data length is sufficient to accomodate at least one cluster") {
        val data = makeData(4 + 1024)
        evaluating { EntryStorageImpl.mount(data, 1025, 8) } should produce [IllegalArgumentException]
      }

      it("should ensure that cluster count is less than or equal to the maximum Int value") {
        val data = new NullData((Int.MaxValue.toLong + 1L) * (4L + 2L))
        evaluating { EntryStorageImpl.mount(data, 2, 8) } should produce [IllegalArgumentException]
      }

      it("should not read or write data") {
        val data = makeData(4 + 1024)
        EntryStorageImpl.mount(data, 1024, 8)
        data.reads should equal (0)
        data.writes should equal (0)
      }
    }

    describe("on formatting") {
      it("should properly determine a number of clusters for a given data length") {
        val data = makeData((4 + 16) * 10)
        val storage = EntryStorageImpl.format(data, 16, 8)
        storage.clusterCount should equal (10)
      }

      it("should ensure that data length is sufficient to accomodate at least one cluster") {
        val data = makeData(4 + 1024)
        evaluating { EntryStorageImpl.format(data, 1025, 8) } should produce [NotEnoughSpaceException]
      }

      it("should ensure that data length is sufficient to accomodate a root directory") {
        val data = makeData(4 + 26)
        evaluating { EntryStorageImpl.format(data, 26, 3) } should produce [NotEnoughSpaceException]
      }

      it("should ensure that cluster count is less than or equal to the maximum Int value") {
        val data = new NullData((Int.MaxValue.toLong + 1L) * (4L + 2L))
        evaluating { EntryStorageImpl.format(data, 2, 8) } should produce [IllegalArgumentException]
      }

      it("should clear the index table, allocate a first cluster and initialize a root directory") {
        val data = makeData(4 + 27)
        EntryStorageImpl.format(data, 27, 3)
        data should holdBytes ("FF FF FF FE 01 00 20 00 20 00 20 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00")
      }

      it("should not alter subsequent cluster data") {
        val data = makeData(zeros((4 + 27) * 2 - 1) + " AB")
        EntryStorageImpl.format(data, 27, 3)
        data.read(61, 1) should equal (bytes("AB"))
      }
    }

    it("should report size as index table size multiplied by cluster size") {
      val data = makeData((4 + 16) * 2 + 10)
      val storage = EntryStorageImpl.mount(data, 16, 8)
      storage.size should equal (32)
    }

    it("should report free space as number of index table free entries multiplied by cluster size") {
      val data = makeData("FF FF FF FE | FF FF FF FF | FF FF FF FF | 00 00 00 00 | 00 00 00 00 | 00 00 00 00")
      val storage = EntryStorageImpl.mount(data, 1, 8)
      storage.free should equal (2)
    }

    it("should cache root directory instance") {
      val data = makeData(4 + 16)
      val storage = EntryStorageImpl.mount(data, 16, 8)
      val rootA = storage.root
      val rootB = storage.root
      rootA should be theSameInstanceAs (rootB)
    }
  }

  private def makeData(content: String) = new ByteData(content) with DataStats

  private def makeData(size: Int) = new ByteData(size) with DataStats

  private def holdBytes(bytes: String) = new Matcher[ByteData] with Matchers {
    def apply(data: ByteData) = equal(bytes)(data.presentation)
  }
}