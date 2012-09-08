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

class ClusterStorageImplSpec extends FunSpec with ShouldMatchers with Helpers {
  describe("Cluster storage") {
    it("should allow data to be empty") {
      makeStorage("", 1)
    }

    it("should ensure that cluster size is greater than 0") {
      evaluating { makeStorage("01 02 03 04", 0) } should produce [IllegalArgumentException]
    }

    it("should report size as data length divided by cluster size") {
      val storage = makeStorage("01 02 03 04 05 06 07 08", 2)
      storage.size should equal (4)
    }

    it("should conisder only whole clusters on size calculation") {
      val storage = makeStorage("01 02 03 04 05 06 07", 4)
      storage.size should equal (1)
    }

    it("should ensure that cluster count is less than or equal to the maximum Int value") {
      val data = new NullData((Int.MaxValue.toLong + 1L) * 2L)
      evaluating { new ClusterStorageImpl(data, 2) } should produce [IllegalArgumentException]
    }

    describe("on data retrieval") {
      it("should return no data when index count is zero") {
        val storage = makeStorage("01 02 03 04 05 06 07 08", 2)
        val data = storage.get(Seq.empty)
        data.length should equal (0)
      }

      it("should ensure that all indices are non-negative") {
        val storage = makeStorage("01 02 03 04 05 06 07 08", 2)
        evaluating { storage.get(Seq(-1)) } should produce [IndexOutOfBoundsException]
      }

      it("should ensure that all indices are less than storage size") {
        val storage = makeStorage("01 02 03 04 05 06 07 08", 2)
        evaluating { storage.get(Seq(4)) } should produce [IndexOutOfBoundsException]
      }

      it("should return a single cluster data by index") {
        val storage = makeStorage("01 02 03 04 05 06 07 08", 2)
        storage.get(Seq(1)) should holdBytes ("03 04")
      }

      it("should group two joint clusters data") {
        val storage = makeStorage("01 02 03 04 05 06 07 08", 2)
        storage.get(Seq(1, 2)) should holdBytes ("03 04 05 06")
      }

      it("should group any number of joint clusters data") {
        val storage = makeStorage("01 02 03 04 05 06 07 08", 2)
        storage.get(Seq(0, 1, 2, 3)) should holdBytes ("01 02 03 04 05 06 07 08")
      }

      it("should separate disjoint clusters data") {
        val storage = makeStorage("01 02 03 04 05 06 07 08", 2)
        storage.get(Seq(0, 2)) should holdBytes ("01 02 | 05 06")
      }

      it("should perform grouping and separation independently") {
        val storage = makeStorage("01 02 03 04 05 06 07 08", 2)
        storage.get(Seq(0, 2, 3)) should holdBytes ("01 02 | 05 06 07 08")
      }
    }
  }

  private def makeStorage(data: String, clusterLength: Int) =
    new ClusterStorageImpl(new ByteData(data), clusterLength)

  private def holdBytes(bytes: String) = new Matcher[Seq[Data]] with Matchers {
    def apply(data: Seq[Data]) = equal(bytes)(data.map(_.presentation).mkString(" | "))
  }
}
