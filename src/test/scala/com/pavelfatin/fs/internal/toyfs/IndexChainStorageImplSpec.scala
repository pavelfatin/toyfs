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
import org.scalatest.OptionValues._
import Entry._

class IndexChainStorageImplSpec extends FunSpec with ShouldMatchers with Helpers {
  describe("Index chain storage") {
    describe("on initialization") {
      it("should not alter index table") {
        val table = makeTable(Reference(1), End, Free)
        makeStorage(table)
        table should holdEntries(Reference(1), End, Free)
      }
    }

    it("should return size as index table size") {
      val storage = makeStorage(Reference(1), End, Free)
      storage.size should equal (3)
    }

    describe("on chain retrieval") {
      it("should return a chain by index with a proper id") {
        val storage = makeStorage(End)
        val chain = storage.get(0)
        chain.id should equal(0)
      }

      it("should be able to return multiple chains") {
        val storage = makeStorage(End, End)
        val chainA = storage.get(0)
        val chainB = storage.get(1)
        chainA.id should equal (0)
        chainB.id should equal (1)
      }

      it("should return different chain instances for the same index") {
        val storage = makeStorage(End)
        val chainA = storage.get(0)
        val chainB = storage.get(0)
        chainB should not be theSameInstanceAs (chainA)
      }

      it("should ensure that chain index is non-negative") {
        val storage = makeStorage(End)
        evaluating {storage.get(-1) } should produce[IndexOutOfBoundsException]
      }

      it("should ensure that chain index is less than index table size") {
        val storage = makeStorage(End)
        evaluating { storage.get(1) } should produce[IndexOutOfBoundsException]
      }
    }

    describe("on allocation") {
      it("should allocate a new chain when a new index table entry can be allocated") {
        val table = makeTable(Free)
        val storage = makeStorage(table)
        val chainOption = storage.allocate()
        chainOption should be ('defined)
        chainOption.value.id should equal(0)
        table should holdEntries (End)
      }

      it("should not allocate a new chain when a new table entry cannot be allocated") {
        val table = makeTable(End)
        val storage = makeStorage(table)
        val chainOption = storage.allocate()
        chainOption should equal (None)
        table should holdEntries(End)
      }
    }
  }

  describe("Index chain") {
    describe("on index retrieval") {
      it("should ensure that offset is non-negative") {
        val storage = makeStorage(End)
        val chain = storage.get(0)
        evaluating { chain.get(-1, canAllocate = false) } should produce [IllegalArgumentException]
      }

      it("should return a chain id as an index at zero offset") {
        val storage = makeStorage(End)
        val chain = storage.get(0)
        val indexOption = chain.get(0, canAllocate = false)
        indexOption.value should equal (0)
      }

      it("should return no next index when table hold End entry at ID") {
        val table = makeTable(End)
        val storage = makeStorage(table)
        val chain = storage.get(0)
        val indexOption = chain.get(1, canAllocate = false)
        indexOption should equal (None)
      }

      it("should not allocate a next index when no next index available") {
        val table = makeTable(End, Free)
        val storage = makeStorage(table)
        val chain = storage.get(0)
        chain.get(1, canAllocate = false)
        table should holdEntries(End, Free)
      }

      it("should return a referenced enty index as index at the next offset") {
        val storage = makeStorage(Reference(2), Free, End)
        val chain = storage.get(0)
        val indexOption = chain.get(1, canAllocate = false)
        indexOption.value should equal (2)
      }

      it("should throw a DataFormatException when Free entry used as reference") {
        val storage = makeStorage(Free, End)
        val chain = storage.get(0)
        evaluating { chain.get(1, canAllocate = false) } should produce [DataFormatException]
      }

      it("should be able to follow table references sequentially") {
        val storage = makeStorage(Reference(2), End, Reference(1))
        val chain = storage.get(0)
        val indexOption = chain.get(2, canAllocate = false)
        indexOption.value should equal (1)
      }

      it("should return no index when offset is out of the upper chain bound") {
        val storage = makeStorage(Reference(1), End)
        val chain = storage.get(0)
        val indexOption = chain.get(2, canAllocate = false)
        indexOption should equal (None)
      }
    }

    describe("on index allocation") {
      it("should not allocate indices when querying index at zero offset") {
        val table = makeTable(End, Free)
        val storage = makeStorage(table)
        val chain = storage.get(0)
        val indexOption = chain.get(0, canAllocate = true)
        indexOption.value should equal (0)
        table should holdEntries(End, Free)
      }

      it("should allocate a next index when a new table entry can be allocated") {
        val table = makeTable(End, Free)
        val storage = makeStorage(table)
        val chain = storage.get(0)
        val indexOption = chain.get(1, canAllocate = true)
        indexOption.value should equal (1)
        table should holdEntries(Reference(1), End)
      }

      it("should be able to allocate a series of indices") {
        val table = makeTable(End, Free, Free)
        val storage = makeStorage(table)
        val chain = storage.get(0)
        val indexOption = chain.get(2, canAllocate = true)
        indexOption.value should equal (2)
        table should holdEntries(Reference(1), Reference(2), End)
      }

      it("should not allocate a next index when a new table entry cannot be allocated") {
        val table = makeTable(End)
        val storage = makeStorage(table)
        val chain = storage.get(0)
        val indexOption = chain.get(1, canAllocate = true)
        indexOption should equal (None)
        table should holdEntries(End)
      }
    }

    describe("(IO)") {
      it("should not query the index table on initialization") {
        val table = makeTable(End)
        val storage = makeStorage(table)
        storage.get(0)
        table.reads should equal (0)
      }

      it("should not query the index table for the index at zero offset") {
        val table = makeTable(End)
        val storage = makeStorage(table)
        val chain = storage.get(0)
        chain.get(0, canAllocate = false)
        table.reads should equal (0)
      }

      it("should not prefetch next indices") {
        val table = makeTable(Reference(1), Reference(2), End)
        val storage = makeStorage(table)
        val chain = storage.get(0)
        chain.get(1, canAllocate = false)
        table.reads should equal (1)
      }

      it("should cache previously read index") {
        val table = makeTable(Reference(1), End)
        val storage = makeStorage(table)
        val chain = storage.get(0)
        chain.get(1, canAllocate = false)
        table.clearStats()
        chain.get(1, canAllocate = false)
        table.reads should equal (0)
      }

      it("should cache previously allocated index") {
        val table = makeTable(End, Free)
        val storage = makeStorage(table)
        val chain = storage.get(0)
        chain.get(1, canAllocate = true)
        table.clearStats()
        chain.get(1, canAllocate = false)
        table.reads should equal (0)
      }

      it("should continue reading from cached index") {
        val table = makeTable(Reference(1), Reference(2), End)
        val storage = makeStorage(table)
        val chain = storage.get(0)
        chain.get(1, canAllocate = false)
        table.clearStats()
        chain.get(2, canAllocate = false)
        table.reads should equal (1)
      }

      it("should continue allocating from cached index") {
        val table = makeTable(End, Free, Free)
        val storage = makeStorage(table)
        val chain = storage.get(0)
        chain.get(1, canAllocate = true)
        table.clearStats()
        chain.get(2, canAllocate = true)
        table.reads should equal (1)
      }

      it("should cache only the last index") {
        val table = makeTable(Reference(1), Reference(2), End)
        val storage = makeStorage(table)
        val chain = storage.get(0)
        chain.get(2, canAllocate = false)
        table.clearStats()
        chain.get(1, canAllocate = false)
        table.reads should equal (1)
      }
    }

    describe("on deletion") {
      it("should mark all chain entries as Free") {
        val table = makeTable(Reference(1), Reference(2), End)
        val storage = makeStorage(table)
        val chain = storage.get(0)
        chain.delete()
        table should holdEntries(Free, Free, Free)
      }

      it("should leave other table entries untouched") {
        val table = makeTable(Reference(2), End, Reference(0))
        val storage = makeStorage(table)
        val chain = storage.get(1)
        chain.delete()
        table should holdEntries(Reference(2), Free, Reference(0))
      }
    }

    describe("on truncation") {
      it("should ensure that truncation size is greater than or equal to 1") {
        val table = makeTable(Reference(1), End)
        val storage = makeStorage(table)
        val chain = storage.get(0)
        evaluating { chain.truncate(0) } should produce [IllegalArgumentException]
      }

      it("should mark new tail entry as End and remaining chain entries as Free") {
        val table = makeTable(Reference(1), Reference(2), Reference(3), End)
        val storage = makeStorage(table)
        val chain = storage.get(0)
        chain.truncate(2)
        table should holdEntries(Reference(1), End, Free, Free)
      }

      it("should do nothing when truncation size is greater than chain size") {
        val table = makeTable(End)
        val storage = makeStorage(table)
        val chain = storage.get(0)
        chain.truncate(2)
        table should holdEntries(End)
      }

      it("should leave other table entries untouched") {
        val table = makeTable(End, Reference(2), End)
        val storage = makeStorage(table)
        val chain = storage.get(0)
        chain.truncate(3)
        table should holdEntries(End, Reference(2), End)
      }

      it("should clear last used index cache when needed") {
        val table = makeTable(Reference(1), End)
        val storage = makeStorage(table)
        val chain = storage.get(0)
        chain.get(1, canAllocate = false)
        chain.truncate(1)
        val indexOption = chain.get(1, canAllocate = false)
        indexOption should equal (None)
      }
    }
  }

  private def makeStorage(table: MockIndexTable): IndexChainStorageImpl = new IndexChainStorageImpl(table)

  private def makeStorage(entries: Entry*): IndexChainStorageImpl = makeStorage(makeTable(entries: _*))

  private def makeTable(entries: Entry*) = new MockIndexTable(entries: _*) with IndexTableStats

  private def holdEntries(entries: Entry*) = new Matcher[MockIndexTable] with Matchers {
    def apply(table: MockIndexTable) = equal(entries.toList)(table.entries)
  }
}
