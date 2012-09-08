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
import org.scalatest.OptionValues._
import Entry._

class IndexTableImplSpec extends FunSpec with ShouldMatchers with Helpers {
  describe("Index table") {
    describe("on initialization") {
      it("should not alter data") {
        val data = makeData("01 02 03 04 | 05 06 07 08")
        makeTable(data)
        data should holdBytes ("01 02 03 04 | 05 06 07 08")
      }
    }

    describe("on size calculation") {
      it("should use 32 bit per entry") {
        val table = makeTable("01 02 03 04 | 05 06 07 08")
        table.size should equal (2)
      }

      it("should report 0 on data less than 4 bytes") {
        val table = makeTable("01 02 03")
        table.size should equal (0)
      }

      it("should conisder whole entries only") {
        val table = makeTable("01 02 03 04 | 05 06 07")
        table.size should equal (1)
      }
    }

    it("should enusre that entry count is less than or equal to the maximum Int value") {
      val data = new NullData(Int.MaxValue.toLong * 4 + 4)
      evaluating { new IndexTableImpl(data) } should produce [IllegalArgumentException]
    }

    describe("on reading") {
      it("should interpret FF FF FF FF as Entry.Free") {
        val table = makeTable("FF FF FF FF")
        val entry = table.get(0)
        entry should equal (Free)
      }

      it("should interpret FF FF FF FE as Entry.End") {
        val table = makeTable("FF FF FF FE")
        val entry = table.get(0)
        entry should equal (End)
      }

      it("should interpret ordinal with proper endian as Entry.Reference") {
        val table = makeTable("01 02 03 04")
        val entry = table.get(0)
        entry should equal (Reference(0x01020304))
      }

      it("should read entry at correct position") {
        val table = makeTable("01 02 03 04 | 05 06 07 08")
        val entry = table.get(1)
        entry should equal (Reference(0x05060708))
      }

      it("should ensure that entry index is non-negative") {
        val table = makeTable("")
        evaluating { table.get(-1) } should produce [IndexOutOfBoundsException]
      }

      it("should ensure that entry index is less than total size") {
        val table = makeTable("01 02 03 04")
        evaluating { table.get(1) } should produce [IndexOutOfBoundsException]
      }

      describe("with reference validation") {
        it("should read valid reference entry normally") {
          val table = makeTable("00 00 00 01 | FF FF FF FF", validateReferences = true)
          val entry = table.get(0)
          entry should equal (Reference(1))
        }

        it("should ensure that reference entry ordinal is non-negative") {
          val table = makeTable("FF FF FF FD", validateReferences = true)
          evaluating { table.get(0) } should produce [DataFormatException]
        }

        it("should ensure that reference entry ordinal is less than total size") {
          val table = makeTable("00 00 00 01", validateReferences = true)
          evaluating { table.get(0) } should produce [DataFormatException]
        }

        it("should ensure that reference entry ordinal differs from entry index") {
          val table = makeTable("00 00 00 00", validateReferences = true)
          evaluating { table.get(0) } should produce [DataFormatException]
        }
      }
    }

    describe("on writing") {
      it("should store Entry.Free as FF FF FF FF") {
        val data = makeData("01 02 03 04")
        val table = makeTable(data)
        table.set(0, Free)
        data should holdBytes ("FF FF FF FF")
      }

      it("should store Entry.End as FF FF FF FE") {
        val data = makeData("01 02 03 04")
        val table = makeTable(data)
        table.set(0, End)
        data should holdBytes ("FF FF FF FE")
      }

      it("should store Entry.Reference as ordinal with proper endian") {
        val data = makeData("01 02 03 04")
        val table = makeTable(data)
        table.set(0, Reference(0x12345678))
        data should holdBytes ("12 34 56 78")
      }

      it("should store entry at correct position") {
        val data = makeData("01 02 03 04 | 05 06 07 08")
        val table = makeTable(data)
        table.set(1, Free)
        data should holdBytes ("01 02 03 04 | FF FF FF FF")
      }

      it("should ensure that entry index is non-negative") {
        val table = makeTable("")
        evaluating { table.set(-1, Free) } should produce [IndexOutOfBoundsException]
      }

      it("should ensure that entry index is less than total size") {
        val table = makeTable("01 02 03 04")
        evaluating { table.set(1, Free) } should produce [IndexOutOfBoundsException]
      }

      describe("with reference validation") {
        it("should store valid reference entry normally") {
          val data = makeData("FF FF FF FF | FF FF FF FF")
          val table = new IndexTableImpl(data, validateReferences = true)
          table.set(0, Reference(1))
          data should holdBytes ("00 00 00 01 | FF FF FF FF")
        }

        it("should ensure that reference entry ordinal is non-negative") {
          val table = makeTable("FF FF FF FF", validateReferences = true)
          evaluating { table.set(0, Reference(-1)) } should produce [IllegalArgumentException]
        }

        it("should ensure that reference entry ordinal is less than total size") {
          val table = makeTable("FF FF FF FF", validateReferences = true)
          evaluating { table.set(0, Reference(1)) } should produce [IllegalArgumentException]
        }

        it("should ensure that reference entry ordinal differs from entry index") {
          val table = makeTable("FF FF FF FF", validateReferences = true)
          evaluating { table.set(0, Reference(0)) } should produce [IllegalArgumentException]
        }
      }
    }

    describe("on allocation") {
      it("should not allocate entry on empty data") {
        val table = makeTable("")
        val index = table.allocate()
        index should equal (None)
      }

      it("should not allocate entry when there's no free entries available") {
        val table = makeTable("01 02 03 04")
        val index = table.allocate()
        index should equal (None)
      }

      it("should mark Entry.Free as Entry.End") {
        val data = makeData("FF FF FF FF")
        val table = makeTable(data)
        val index = table.allocate()
        index.value should equal (0)
        data should holdBytes ("FF FF FF FE")
      }

      it("should use the first Entry.Free") {
        val data = makeData("01 02 03 04 | FF FF FF FF | FF FF FF FF")
        val table = makeTable(data)
        val index = table.allocate()
        index.value should equal (1)
        data should holdBytes ("01 02 03 04 | FF FF FF FE | FF FF FF FF")
      }

      it("should be able to allocate several entries sequentially") {
        val data = makeData("FF FF FF FF | 01 02 03 04 | FF FF FF FF")
        val table = makeTable(data)
        val indexA = table.allocate()
        val indexB = table.allocate()
        indexA.value should equal (0)
        indexB.value should equal (2)
        data should holdBytes ("FF FF FF FE | 01 02 03 04 | FF FF FF FE")
      }

      describe("(caching)") {
        it("should cache last allocation index") {
          val data = makeData("FF FF FF FF | FF FF FF FF")
          val table = makeTable(data)
          table.allocate()
          data.write(0, 4, bytes("FF FF FF FF")) // revert the allocation via raw data
          val index = table.allocate()
          index.value should equal (1)
          data should holdBytes ("FF FF FF FF | FF FF FF FE")
        }

        it("should cache last allocation index absence") {
          val data = makeData("01 02 03 04")
          val table = makeTable(data)
          table.allocate()
          data.write(0, 4, bytes("FF FF FF FF")) // mark entry as free via raw data
          val index = table.allocate()
          index should equal (None)
        }

        it("should update cached last allocation index on writing") {
          val data = makeData("FF FF FF FF | FF FF FF FF")
          val table = makeTable(data)
          table.allocate()
          table.set(0, Free)
          val index = table.allocate()
          index.value should equal (0)
          data should holdBytes ("FF FF FF FE | FF FF FF FF")
        }

        it("should not update cached last allocation index on writing non-free entries") {
          val data = makeData("FF FF FF FF | FF FF FF FF")
          val table = makeTable(data)
          table.allocate()
          table.set(0, Reference(1))
          table.set(0, End)
          data.write(0, 4, bytes("FF FF FF FF")) // mark first entry as free via raw data
          val index = table.allocate()
          index.value should equal (1)
          data should holdBytes ("FF FF FF FF | FF FF FF FE")
        }

        it("should prefer smallest index when updating cached last allocation index on writing") {
          val data = makeData("FF FF FF FF | FF FF FF FF | 01 02 03 04")
          val table = makeTable(data)
          table.allocate()
          table.set(2, Free)
          val index = table.allocate()
          index.value should equal (1)
          data should holdBytes ("FF FF FF FE | FF FF FF FE | FF FF FF FF")
        }

        it("should use buffered reading to find a free entry") {
          val data = makeData("01 02 03 04 | 05 06 07 08 | FF FF FF FF")
          val table = makeTable(data)
          table.allocate()
          data.reads should equal (1)
        }
      }
    }

    describe("on free entries count calculation") {
      it("should report a number of FF FF FF FF markers") {
        val data = makeData("FF FF FF FF | 05 06 07 08 | FF FF FF FF")
        val table = makeTable(data)
        table.free should equal (2)
      }

      it("should use buffered reading") {
        val data = makeData("FF FF FF FF | 05 06 07 08 | FF FF FF FF")
        val table = makeTable(data)
        table.free
        data.reads should equal (1)
      }
    }

    describe("on clearing") {
      it("should erase data with FF FF FF FF markers") {
        val data = makeData("01 02 03 04 | 05 06 07 08")
        val table = makeTable(data)
        table.clear()
        data should holdBytes ("FF FF FF FF | FF FF FF FF")
      }

      it("should erase whole entries only") {
        val data = makeData("01 02 03 04 | 05 06 07")
        val table = makeTable(data)
        table.clear()
        data should holdBytes ("FF FF FF FF | 05 06 07")
      }

      it("should use buffered writing") {
        val data = makeData("01 02 03 04 | 05 06 07 08")
        val table = makeTable(data)
        table.clear()
        data.writes should equal (1)
      }
    }
  }

  private def makeTable(bytes: String, validateReferences: Boolean = false) =
    new IndexTableImpl(makeData(bytes), validateReferences)

  private def makeTable(data: ByteData) = new IndexTableImpl(data, validateReferences = false)

  private def makeData(bytes: String) = new ByteData(bytes, groupLength = Some(4)) with DataStats

  private def holdBytes(bytes: String) = new Matcher[ByteData] with Matchers {
    def apply(data: ByteData) = equal(bytes)(data.presentation)
  }
}
