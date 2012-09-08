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
import java.util.Calendar

class RootDirectoryImplSpec extends FunSpec with ShouldMatchers with Helpers {
  private val DefaultDate = {
    val calendar = Calendar.getInstance()
    calendar.setTimeInMillis(0L)
    calendar
  }

  describe("Root directory") {
    describe("on property reading") {
      it("should return no parent") {
        val directory = makeDirectory()
        directory.parent should equal (None)
      }

      it("should return empty string as name") {
        val directory = makeDirectory()
        directory.name should equal ("")
      }

      it("should return default date as creation date") {
        val directory = makeDirectory()
        directory.date should equal (DefaultDate)
      }

      it("should return false as hidden property value") {
        val directory = makeDirectory()
        directory.hidden should equal (false)
      }
    }

    describe("on property modification") {
      it("should prohibit changing name") {
        val directory = makeDirectory()
        evaluating { directory.name = "readme" } should produce [UnsupportedOperationException]
      }

      it("should prohibit changing creation date") {
        val directory = makeDirectory()
        evaluating { directory.date = Calendar.getInstance } should produce [UnsupportedOperationException]
      }

      it("should prohibit changing visibility") {
        val directory = makeDirectory()
        evaluating { directory.hidden = true } should produce [UnsupportedOperationException]
      }
    }

    it("should prohibit deletion") {
      val directory = makeDirectory()
      evaluating { directory.delete() } should produce [UnsupportedOperationException]
    }
  }

  private def makeDirectory(chunk: Chunk = makeChunk(),
                            chunkStorage: ChunkStorage = makeChunkStorage(),
                            recordStorage: MockRecordStorage = makeRecordStorage()) =
    new RootDirectoryImpl(chunk, chunkStorage, _ => recordStorage)

  private def makeChunk(content: String = "") = new MockChunk(0, content)

  private def makeChunkStorage() = new MockChunkStorage(10)

  private def makeRecordStorage() = new MockRecordStorage(maxNameLength = 8)
}