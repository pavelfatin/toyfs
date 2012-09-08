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

class AbstractDataSpec extends FunSpec with ShouldMatchers with Helpers {
  private type IO = (Data, Int, Int, Array[Byte], Int) => Unit

  describe("Data") {
    describe("on reading") {
      it should behave like checkedIO {
        (data, position, length, buffer, offset) => data.read(position, length, buffer, offset)
      }

      describe("when extendable") {
        it("should ensure that position + length is less than or equal to data length") {
          val data = makeData(5, extendable = true)
          evaluating {
            data.read(3, 3, buffer(5), 0)
          } should produce[IllegalArgumentException]
        }
      }
    }

    describe("on writing") {
      it should behave like checkedIO {
        (data, position, length, buffer, offset) => data.write(position, length, buffer, offset)
      }

      describe("when extendable") {
        it("should allow position + length to be greater than data length") {
          val data = makeData(5, extendable = true)
          data.write(3, 3, buffer(5), 0)
        }

        it("should ensure that position is less than or equal to data length") {
          val data = makeData(5, extendable = true)
          evaluating {
            data.write(6, 0, buffer(5), 0)
          } should produce[IllegalArgumentException]
        }

        it("should ensure that position + length is less than or equal to maximum Long value") {
          val data = makeData(Long.MaxValue, extendable = true)
          evaluating {
            data.write(Long.MaxValue - 1, 2, buffer(2), 0)
          } should produce[IllegalArgumentException]
        }
      }
    }

    describe("on projecting") {
      it("should accept valid arguments") {
        val data = makeData(5)
        data.projection(0, 5)
      }

      it("should ensure that position is non-negative") {
        val data = makeData(5)
        evaluating {
          data.projection(-1, 1)
        } should produce[IllegalArgumentException]
      }

      it("should ensure that length is non-negative") {
        val data = makeData(5)
        evaluating {
          data.projection(1, -1)
        } should produce[IllegalArgumentException]
      }

      it("should ensure that position + length is less than or equal to data length") {
        val data = makeData(5)
        evaluating {
          data.projection(3, 3)
        } should produce[IllegalArgumentException]
      }

      it("should ensure that projected data arguments are validated") {
        val data = makeData(5)
        evaluating {
          data.projection(2, 3).read(-1, 0, buffer(5))
        } should produce[IllegalArgumentException]
        evaluating {
          data.projection(2, 3).write(-1, 0, buffer(5))
        } should produce[IllegalArgumentException]
        info("assume that projected data relies on all the AbstractData validations")
      }

      it("should ensure that projection data length is not lazy") {
        val data = makeData(5)
        evaluating {
          data.projection(0, 3).read(0, 4, buffer(4))
        } should produce[IllegalArgumentException]
      }

      it("should ensure that projection data is not extendable") {
        val data = makeData(5)
        evaluating {
          data.projection(0, 3).write(0, 4, buffer(5))
        } should produce[IllegalArgumentException]
      }

      it("should report proper projection length") {
        val data = makeData("01 02 03 04 05 06 07 08 09")
        val projection = data.projection(2, 5)
        projection.length should equal(5)
      }

      it("should properly shift projected reading") {
        val data = makeData("01 02 03 04 05 06 07 08 09")
        val projection = data.projection(2, 5)
        projection.read(1, 3) should holdBytes("04 05 06")
      }

      it("should properly shift projected writing") {
        val data = makeData("01 02 03 04 05 06 07 08 09")
        val projection = data.projection(2, 5)
        projection.write(1, 3, buffer(3))
        data.presentation should equal("01 02 03 00 00 00 07 08 09")
      }

      describe("when extendable") {
        it("should ensure that position + length is less than or equal to data length") {
          val data = makeData(5, extendable = true)
          evaluating {
            data.projection(3, 3)
          } should produce[IllegalArgumentException]
        }
      }
    }
  }

  private def checkedIO(io: IO) {
    it("should accept valid arguments") {
      val data = makeData(5)
      io(data, 0, 5, buffer(5), 0)
    }

    it("should work with zero-length data") {
      val data = makeData(0)
      io(data, 0, 0, buffer(0), 0)
    }

    it("should ensure that position is non-negative") {
      val data = makeData(5)
      evaluating {
        io(data, -1, 1, buffer(5), 0)
      } should produce[IllegalArgumentException]
    }

    it("should ensure that length is non-negative") {
      val data = makeData(5)
      evaluating {
        io(data, 1, -1, buffer(5), 0)
      } should produce[IllegalArgumentException]
    }

    it("should ensure that offset is non-negative") {
      val data = makeData(5)
      evaluating {
        io(data, 0, 0, buffer(5), -1)
      } should produce[IllegalArgumentException]
    }

    it("should ensure that position is less than or equal to data length") {
      val data = makeData(0)
      evaluating {
        io(data, 6, 0, buffer(0), 0)
      } should produce[IllegalArgumentException]
    }

    it("should ensure that position + length is less than or equal to data length") {
      val data = makeData(5)
      evaluating {
        io(data, 3, 3, buffer(5), 0)
      } should produce[IllegalArgumentException]
    }

    it("should ensure that offset + length is less than or equal to buffer length") {
      val data = makeData(10)
      evaluating {
        io(data, 0, 3, buffer(5), 3)
      } should produce[IllegalArgumentException]
    }

    describe("when length is lazy") {
      val data = new NullData(0, lazyLength = true) {
        override def length = fail("Length must not be queried")
      }

      it("should not query data length") {
        io(data, 0, 5, buffer(5), 0)
      }
    }
  }

  private def makeData(length: Long, extendable: Boolean = false) = new NullData(length, extendable)

  private def makeData(bytes: String) = new ByteData(bytes)

  private def holdBytes(bytes: String) = new Matcher[Array[Byte]] with Matchers {
    def apply(array: Array[Byte]) = {
      val presentation = array.map(_.formatted("%02X")).mkString(" ")
      equal(bytes)(presentation)
    }
  }
}