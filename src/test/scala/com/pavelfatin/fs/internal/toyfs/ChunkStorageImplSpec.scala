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

class ChunkStorageImplSpec extends FunSpec with ShouldMatchers with Helpers {
  describe("Chunk storage") {
    describe("on initialization") {
      it("should accept valid arguments") {
        makeStorage(makeClusters("01"), makeChains("1: 0"))
      }

      it("should ensure that index size is less than or equal to custer count") {
        evaluating { makeStorage(makeClusters("01"), makeChains("2:")) } should produce [IllegalArgumentException]
      }

      it("should accept empty chain storage") {
        makeStorage(makeClusters("01"), makeChains("0:"))
      }

      it("should accept empty cluster storage") {
        makeStorage(makeClusters(""), makeChains("0:"))
      }
    }

    describe("on chunk retrieval") {
      it("should return a chunk (with a proper id) by index") {
        val storage = makeStorage(makeClusters("01"), makeChains("1: 0"))
        val chunk = storage.get(0)
        chunk.id should equal (0)
      }

      it("should ensure that chunk index is non-negative") {
        val storage = makeStorage(makeClusters("01"), makeChains("1: 0"))
        evaluating { storage.get(-1) } should produce [IndexOutOfBoundsException]
      }

      it("should ensure that chunk index is less than index size") {
        val storage = makeStorage(makeClusters("01 | 02"), makeChains("1: 0"))
        evaluating { storage.get(1) } should produce [IndexOutOfBoundsException]
      }
    }

    describe("on chunk allocation") {
      it("should allocate a chunk with a new id") {
        val storage = makeStorage(makeClusters("01 02 03 | 04 05 06"), makeChains("2: 0"))
        val chunk = storage.allocate()
        chunk.value.id should equal (1)
      }

      it("should add a new index chain") {
        val chains = makeChains("2: 0")
        val storage = makeStorage(makeClusters("01 | 02"), chains)
        storage.allocate()
        chains should holdChains ("2: 0 | 1")
      }

      it("should not alter cluster data") {
        val clusters = makeClusters("01 | 02")
        val storage = makeStorage(clusters, makeChains("2: 0"))
        storage.allocate()
        clusters should holdData ("01 | 02")
      }

      it("should not allocate a chunk when a new index chain can't be allocated") {
        val storage = makeStorage(makeClusters("01 | 02"), makeChains("2: 0-1"))
        val chunk = storage.allocate()
        chunk should equal (None)
      }
    }

    it("should report size as index chain storage size") {
      val storage = makeStorage(makeClusters("01 | 02 | 03 | 04 | 05"), makeChains("3: 0-1"))
      storage.size should equal (3)
    }

    describe("Chunk") {
      it("should prohibit querying chunk size") {
        val storage = makeStorage(makeClusters("01 02 03 | 04 05 06 | 07 08 09"), makeChains("3: 0-1 | 2"))
        val chunk = storage.get(0)
        evaluating { chunk.length } should produce [UnsupportedOperationException]
      }

      describe("on reading") {
        it("should ensure that arguments are valid") {
          val storage = makeStorage(makeClusters("01"), makeChains("1: 0"))
          val chunk = storage.get(0)
          evaluating { chunk.read(-1, 1) } should produce [IllegalArgumentException]
          info ("assume that it relies on all the AbstractData validations")
        }

        it("should ensure that position is less than or equal to (lazy) data length") {
          val storage = makeStorage(makeClusters("01 02 03 | 04 05 06"), makeChains("2: 0-1"))
          val chunk = storage.get(0)
          val b = buffer(0)
          evaluating { chunk.read(7, 1, b) } should produce [IllegalArgumentException]
        }

        it("should ensure that position + length is less than or equal to (lazy) data length") {
          val storage = makeStorage(makeClusters("01 02 03 | 04 05 06"), makeChains("2: 0-1"))
          val chunk = storage.get(0)
          val b = buffer(5)
          evaluating { chunk.read(2, 5, b) } should produce [IllegalArgumentException]
        }

        it("should do nothing when length argument is zero") {
          val storage = makeStorage(makeClusters("01"), makeChains("1: 0"))
          val chunk = storage.get(0)
          val b = buffer(0)
          chunk.read(10, 0, b)
        }

        it("should not copy preceeding clusters to buffer before throwing an exception on validation") {
          val storage = makeStorage(makeClusters("01 02 03 | 04 05 06"), makeChains("2: 0-1"))
          val chunk = storage.get(0)
          val b = bytes("09 08 07 06 05 04 03 02 01")
          intercept { chunk.read(0, 9, b) }
          b should holdBytes("09 08 07 06 05 04 03 02 01")
        }

        it("should not allocate new index chain") {
          val chains = makeChains("2: 0")
          val storage = makeStorage(makeClusters("01 02 03 | 04 05 06"), chains)
          val chunk = storage.get(0)
          intercept { chunk.read(0, 6) }
          chains should holdChains ("2: 0")
        }

        it("should properly read from the first cluster in chain") {
          val storage = makeStorage(makeClusters("01 02 03"), makeChains("1: 0"))
          val chunk = storage.get(0)
          chunk.read(0, 3) should holdBytes ("01 02 03")
          chunk.read(0, 2) should holdBytes ("01 02")
          chunk.read(1, 2) should holdBytes ("02 03")
          chunk.read(1, 1) should holdBytes ("02")
          chunk.read(1, 2, 2) should holdBytes ("00 00 02 03")
        }

        it("should properly read from the second cluster in chain") {
          val storage = makeStorage(makeClusters("01 02 03 | 04 05 06"), makeChains("2: 0-1"))
          val chunk = storage.get(0)
          chunk.read(3, 3) should holdBytes ("04 05 06")
          chunk.read(3, 2) should holdBytes ("04 05")
          chunk.read(4, 2) should holdBytes ("05 06")
          chunk.read(4, 1) should holdBytes ("05")
          chunk.read(4, 2, 2) should holdBytes ("00 00 05 06")
        }

        it("should properly read from two contiguous clusters") {
          val storage = makeStorage(makeClusters("01 02 03 | 04 05 06 | 07 08 09"), makeChains("2: 0-2"))
          val chunk = storage.get(0)
          chunk.read(1, 4) should holdBytes ("02 03 07 08")
          chunk.read(1, 3) should holdBytes ("02 03 07")
          chunk.read(2, 3) should holdBytes ("03 07 08")
          chunk.read(2, 2) should holdBytes ("03 07")
          chunk.read(2, 2, 2) should holdBytes ("00 00 03 07")
        }

        it("should read from clusters in proper order") {
          val storage = makeStorage(makeClusters("01 02 03 | 04 05 06 | 07 08 09"), makeChains("3: 1-0-2"))
          val chunk = storage.get(1)
          chunk.read(0, 9) should holdBytes ("04 05 06 01 02 03 07 08 09")
        }

        it("should rely on the length argument rather than buffer length") {
          val storage = makeStorage(makeClusters("01 02 03"), makeChains("1: 0"))
          val chunk = storage.get(0)
          val array = buffer(3)
          chunk.read(0, 2, array)
          array should holdBytes ("01 02 00")
        }
      }

      describe("on writing") {
        it("should ensure that the arguments are valid") {
          val storage = makeStorage(makeClusters("01"), makeChains("1: 0"))
          val chunk = storage.get(0)
          evaluating { chunk.write(-1, 1, bytes("00")) } should produce [IllegalArgumentException]
          info ("assume that it relies on all the AbstractData validations")
        }

        it("should ensure that position is less than or equal to (lazy) data length") {
          val storage = makeStorage(makeClusters("01 02 03 | 04 05 06"), makeChains("2: 0-1"))
          val chunk = storage.get(0)
          evaluating { chunk.write(7, 1, bytes("")) } should produce [IllegalArgumentException]
        }

        it("should properly write to the first cluster in chain") {
          val clusters = makeClusters("00 00 00")
          val storage = makeStorage(clusters, makeChains("1: 0"))
          val chunk = storage.get(0)

          chunk.write(0, 3, bytes("01 02 03"))
          clusters should holdData ("01 02 03")

          clusters.clear()
          chunk.write(0, 2, bytes("01 02"))
          clusters should holdData ("01 02 00")

          clusters.clear()
          chunk.write(1, 2, bytes("02 03"))
          clusters should holdData ("00 02 03")

          clusters.clear()
          chunk.write(1, 1, bytes("02"))
          clusters should holdData ("00 02 00")

          clusters.clear()
          chunk.write(1, 2, bytes("01 02 03 04"), 2)
          clusters should holdData ("00 03 04")
        }

        it("should properly write to the second cluster in chain") {
          val clusters = makeClusters("00 00 00 | 00 00 00")
          val storage = makeStorage(clusters, makeChains("2: 0-1"))
          val chunk = storage.get(0)

          chunk.write(3, 3, bytes("04 05 06"))
          clusters should holdData ("00 00 00 | 04 05 06")

          clusters.clear()
          chunk.write(3, 2, bytes("04 05"))
          clusters should holdData ("00 00 00 | 04 05 00")

          clusters.clear()
          chunk.write(4, 2, bytes("05 06"))
          clusters should holdData ("00 00 00 | 00 05 06")

          clusters.clear()
          chunk.write(4, 1, bytes("05"))
          clusters should holdData ("00 00 00 | 00 05 00")

          chunk.write(4, 2, bytes("01 02 03 04"), 2)
          clusters should holdData ("00 00 00 | 00 03 04")
        }

        it("should properly write to two contiguous clusters") {
          val clusters = makeClusters("00 00 00 | 00 00 00 | 00 00 00")
          val storage = makeStorage(clusters, makeChains("2: 0-2"))
          val chunk = storage.get(0)

          chunk.write(1, 4, bytes("02 03 07 08"))
          clusters should holdData ("00 02 03 | 00 00 00 | 07 08 00")

          clusters.clear()
          chunk.write(1, 3, bytes("02 03 07"))
          clusters should holdData ("00 02 03 | 00 00 00 | 07 00 00")

          clusters.clear()
          chunk.write(2, 3, bytes("03 07 08"))
          clusters should holdData ("00 00 03 | 00 00 00 | 07 08 00")

          clusters.clear()
          chunk.write(2, 2, bytes("03 07"))
          clusters should holdData ("00 00 03 | 00 00 00 | 07 00 00")

          chunk.write(2, 2, bytes("01 02 03 04"), 2)
          clusters should holdData ("00 00 03 | 00 00 00 | 04 00 00")
        }

        it("should write to clusters in proper order") {
          val clusters = makeClusters("00 00 00 | 00 00 00 | 00 00 00")
          val storage = makeStorage(clusters, makeChains("3: 1-0-2"))
          val chunk = storage.get(1)
          chunk.write(0, 9, bytes("01 02 03 04 05 06 07 08 09"))
          clusters should holdData ("04 05 06 | 01 02 03 | 07 08 09")
        }

        it("should combine written data") {
          val clusters = makeClusters("00 00 00 00")
          val storage = makeStorage(clusters, makeChains("1: 0"))
          val chunk = storage.get(0)
          chunk.write(0, 2, bytes("01 02"))
          chunk.write(2, 2, bytes("03 04"))
          clusters should holdData ("01 02 03 04")
        }

        it("should be able to read previously written data") {
          val clusters = makeClusters("00 00 00")
          val storage = makeStorage(clusters, makeChains("1: 0"))
          val chunk = storage.get(0)
          chunk.write(0, 3, bytes("01 02 03"))
          chunk.read(0, 3) should holdBytes ("01 02 03")
        }

        it("should do nothing when length argument is zero") {
          val clusters = makeClusters("01 02 03")
          val storage = makeStorage(clusters, makeChains("1: 0"))
          val chunk = storage.get(0)
          chunk.write(10, 0, bytes(""))
          clusters should holdData ("01 02 03")
        }

        it("should rely on the length argument rather than buffer length") {
          val clusters = makeClusters("00 00 00")
          val storage = makeStorage(clusters, makeChains("1: 0"))
          val chunk = storage.get(0)
          val array = bytes("01 02 03")
          chunk.write(0, 2, array)
          clusters should holdData ("01 02 00")
        }

        describe("when extending") {
          it("should store appended data in clusters") {
            val clusters = makeClusters("01 02 03 | 00 00 00")
            val storage = makeStorage(clusters, makeChains("2: 0"))
            val chunk = storage.get(0)
            chunk.write(3, 3, bytes("04 05 06"))
            clusters should holdData ("01 02 03 | 04 05 06")
          }

          it("should allocate a next index in chain") {
            val chains = makeChains("2: 0")
            val storage = makeStorage(makeClusters("01 02 03 | 00 00 00"), chains)
            val chunk = storage.get(0)
            chunk.write(3, 3, bytes("04 05 06"))
            chains should holdChains ("2: 0-1")
          }

          it("should not alter remaining data in the new clusters") {
            val clusters = makeClusters("01 02 03 | 00 05 06")
            val storage = makeStorage(clusters, makeChains("2: 0"))
            val chunk = storage.get(0)
            chunk.write(3, 1, bytes("04"))
            clusters should holdData ("01 02 03 | 04 05 06")
          }

          it("should be able to append multiple clusters at once") {
            val clusters = makeClusters("01 02 03 | 00 00 00 | 00 00 00")
            val chains = makeChains("3: 0")
            val storage = makeStorage(clusters, chains)
            val chunk = storage.get(0)
            chunk.write(3, 6, bytes("04 05 06 07 08 09"))
            clusters should holdData ("01 02 03 | 04 05 06 | 07 08 09")
            chains should holdChains ("3: 0-1-2")
          }

          it("should be able to extend several existing clusters") {
            val clusters = makeClusters("01 02 03 | 04 05 06 | 00 00 00")
            val chains = makeChains("3: 0-1")
            val storage = makeStorage(clusters, chains)
            val chunk = storage.get(0)
            chunk.write(6, 3, bytes("07 08 09"))
            clusters should holdData ("01 02 03 | 04 05 06 | 07 08 09")
            chains should holdChains ("3: 0-1-2")
          }

          it("should be able to split data between existing and new clusters") {
            val clusters = makeClusters("01 00 00 | 00 00 06")
            val chains = makeChains("2: 0")
            val storage = makeStorage(clusters, chains)
            val chunk = storage.get(0)
            chunk.write(1, 4, bytes("02 03 04 05"))
            clusters should holdData ("01 02 03 | 04 05 06")
            chains should holdChains ("2: 0-1")
          }

          it("should be able to read appended data") {
            val clusters = makeClusters("01 02 03 | 00 00 00")
            val storage = makeStorage(clusters, makeChains("2: 0"))
            val chunk = storage.get(0)
            chunk.write(3, 3, bytes("04 05 06"))
            chunk.read(3, 3) should holdBytes ("04 05 06")
          }

          it("should throw an exception when a new index can't be allocated") {
            val clusters = makeClusters("01 02 03 | 00 00 00")
            val storage = makeStorage(clusters, makeChains("1: 0"))
            val chunk = storage.get(0)
            evaluating { chunk.write(3, 3, bytes("04 05 06")) } should produce [NotEnoughSpaceException]
          }

          it("should not write data to preceeding clusters before throwing an exception") {
            val clusters = makeClusters("01 02 03 | 04 05 06 | 00 00 00")
            val storage = makeStorage(clusters, makeChains("2: 0-1"))
            val chunk = storage.get(0)
            intercept { chunk.write(0, 9, bytes("09 08 07 06 05 04 03 02 01")) }
            clusters should holdData ("01 02 03 | 04 05 06 | 00 00 00")
          }
        }
      }

      describe("on truncation") {
        it("should ensure that threshold is non-negative") {
          val storage = makeStorage(makeClusters("01"), makeChains("1: 0"))
          val chunk = storage.get(0)
          evaluating { chunk.truncate(-1) } should produce [IllegalArgumentException]
        }

        it("should truncate the index chain") {
          val chains = makeChains("5: 0-1-2-3-4")
          val storage = makeStorage(makeClusters("01 | 02 | 03 | 04 | 05"), chains)
          val chunk = storage.get(0)
          chunk.truncate(2)
          chains should holdChains("5: 0-1")
        }

        it("should use ceiling rather than rounding down") {
          val chains = makeChains("3: 0-1-2")
          val storage = makeStorage(makeClusters("01 02 03 | 04 05 06 | 07 08 09"), chains)
          val chunk = storage.get(0)
          chunk.truncate(4)
          chains should holdChains("3: 0-1")
        }

        it("should retain the root index when truncation size is zero") {
          val chains = makeChains("2: 0-1")
          val storage = makeStorage(makeClusters("01 | 02"), chains)
          val chunk = storage.get(0)
          chunk.truncate(0)
          chains should holdChains("2: 0")
        }

        it("should do nothing when threshold is greater than chunk length") {
          val chains = makeChains("2: 0-1")
          val storage = makeStorage(makeClusters("01 02 03 | 04 05 06"), chains)
          val chunk = storage.get(0)
          chunk.truncate(7)
          chains should holdChains("2: 0-1")
        }

        it("should not alter cluster data") {
          val clusters = makeClusters("01 02 03 | 04 05 06")
          val storage = makeStorage(clusters, makeChains("2: 0-1"))
          val chunk = storage.get(0)
          chunk.truncate(3)
          clusters should holdData ("01 02 03 | 04 05 06")
        }
      }

      describe("on deletion") {
        it("should delete the index chain") {
          val chains = makeChains("4: 0-1 | 2-3")
          val storage = makeStorage(makeClusters("01 | 02 | 03 | 04"), chains)
          val chunk = storage.get(0)
          chunk.delete()
          chains should holdChains("4: 2-3")
        }

        it("should not alter cluster data") {
          val clusters = makeClusters("01 02 03 | 04 05 06")
          val storage = makeStorage(clusters, makeChains("2: 0-1"))
          val chunk = storage.get(0)
          chunk.delete()
          clusters should holdData ("01 02 03 | 04 05 06")
        }

        it("should retain id") {
          val storage = makeStorage(makeClusters("01 | 02"), makeChains("2: 0 | 1"))
          val chunk = storage.get(1)
          chunk.delete()
          chunk.id should equal (1)
        }
      }
    }
  }

  private def makeChains(content: String) = new MockIndexChainStorage(content)

  private def makeClusters(clusters: String) = new MockClusterStorage(clusters)

  private def makeStorage(clusters: ClusterStorage, chains: IndexChainStorage) = new ChunkStorageImpl(chains, clusters)

  private def holdBytes(bytes: String) = new Matcher[Array[Byte]] with Matchers {
    def apply(array: Array[Byte]) = {
      val presentation = array.map(_.formatted("%02X")).mkString(" ")
      equal(bytes)(presentation)
    }
  }

  private def holdData(clusters: String) = new Matcher[MockClusterStorage] with Matchers {
    def apply(storage: MockClusterStorage) = equal(clusters)(storage.presentation)
  }

  private def holdChains(chains: String) = new Matcher[MockIndexChainStorage] with Matchers {
    def apply(storage: MockIndexChainStorage) = equal(chains)(storage.presentation)
  }
}