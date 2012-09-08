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

class SubDirectoryImplSpec extends FunSpec with ShouldMatchers with Helpers {
  describe("Subdirectory") {
    describe("on deletion") {
      it("should report data format error when tail record is not present") {
        val directory = makeDirectory()
        evaluating { directory.delete() } should produce [DataFormatException]
      }

      it("should prohibit deletion when not empty") {
        val directory = makeDirectory(storage = makeStorage(Record(name = "readme"), Record(tail = true)))
        evaluating { directory.delete() } should produce [IllegalStateException]
      }

      it("should delete metadata and chunk") {
        val metadata = makeMetadata()
        val chunk = makeChunk()
        val directory = makeDirectory(metadata, chunk, makeStorage(Record(tail = true)))
        directory.delete()
        assert (metadata.deleted)
        assert (chunk.deleted)
      }

      it("should not alter chunk data") {
        val chunk = makeChunk("01 02 03 04")
        val directory = makeDirectory(makeMetadata(), chunk, makeStorage(Record(tail = true)))
        directory.delete()
        chunk should holdData ("01 02 03 04")
      }
    }
  }

  private def makeDirectory(metadata: Metadata = makeMetadata(),
                            chunk: Chunk = makeChunk(),
                            storage: MockRecordStorage = makeStorage()) = {
    val chunkStorage = new MockChunkStorage(10)
    new SubDirectoryImpl(null, metadata, chunk, chunkStorage, _ => storage)
  }

  private def makeMetadata(initializer: Metadata => Unit = _ => ()) = {
    val record = new MockMetadata()
    initializer(record)
    record
  }

  private def makeChunk(content: String = "") = new MockChunk(0, content)

  private def makeStorage(records: Record*) = new MockRecordStorage(8, records: _*)

  private def holdData(data: String) = new Matcher[MockChunk] with Matchers {
    def apply(chunk: MockChunk) = equal(data)(chunk.presentation)
  }
}