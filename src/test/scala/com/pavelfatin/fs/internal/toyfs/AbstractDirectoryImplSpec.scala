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

class AbstractDirectoryImplSpec extends FunSpec with ShouldMatchers with Inside with Helpers {
  private val Date = Calendar.getInstance
  private val MaxNameLength = 8

  describe("Directory") {
    describe("on initialization") {
      it("should store a tail record at the beginning") {
        val storage = makeStorage()
        val directory = makeDirectory(storage)
        directory.init()
        storage should holdRecords (Record(tail = true))
      }
    }

    describe("on property reading") {
      it("should propagate metadata.name") {
        val directory = makeDirectory(makeMetadata(_.name = "info"))
        directory.name should equal ("info")
      }

      it("should propagate metadata.date") {
        val directory = makeDirectory(makeMetadata(_.date = Date))
        directory.date should equal (Date)
      }

      it("should propagate metadata.hidden") {
        val directory = makeDirectory(makeMetadata(_.hidden = true))
        directory.hidden should equal (true)
      }
    }

    describe("on property modification") {
      it("should update metadata.name") {
        val metadata = makeMetadata()
        val directory = makeDirectory(metadata)
        directory.name = "info"
        metadata.name should equal ("info")
      }

      it("should update metadata.date") {
        val metadata = makeMetadata()
        val directory = makeDirectory(metadata)
        directory.date = Date
        metadata.date should equal (Date)
      }

      it("should update metadata.hidden") {
        val metadata = makeMetadata()
        val directory = makeDirectory(metadata)
        directory.hidden = true
        metadata.hidden should equal (true)
      }
    }

    describe("on listing") {
      it("should report data format error when tail record is not present") {
        val directory = makeDirectory(makeStorage())
        evaluating { directory.entries } should produce [DataFormatException]
      }

      it("should skip deleted entries") {
        val directory = makeDirectory(makeStorage(
          Record(directory = true, name = "info", deleted = true),
          Record(directory = false, name = "readme", deleted = true),
          Record(tail = true)))
        directory.entries should equal (Seq.empty, Seq.empty)
      }

      it("should list file records as files") {
        val directory = makeDirectory(makeStorage(
          Record(directory = false, name = "readme"),
          Record(tail = true)))
        inside (directory.entries) {
          case (Seq(), Seq(file)) =>
            file.name should equal ("readme")
        }
      }

      it("should list directory records as directories") {
        val directory = makeDirectory(makeStorage(
          Record(directory = true, name = "info"),
          Record(tail = true)))
        inside (directory.entries) {
          case (Seq(subdirectory), Seq()) =>
            subdirectory.name should equal ("info")
        }
      }

      it("should not cache entries") {
        val storage: MockRecordStorage = makeStorage(
          Record(name = "foo"),
          Record(tail = true))
        val directory = makeDirectory(storage)
        directory.entries._2.toList
        storage.set(0, Record(name = "bar"))
        val (_, Seq(file)) = directory.entries
        file.name should equal ("bar")
      }
    }

    describe("on entry creation") {
      it("should ensure that name is not empty") {
        val directory = makeDirectory(makeStorage(
          Record(tail = true)))
        evaluating { directory.createFile("", Date) } should produce [IllegalArgumentException]
        evaluating { directory.createDirectory("", Date) } should produce [IllegalArgumentException]
      }

      it("should ensure that name length is less that or equal to storage max name length") {
        val directory = makeDirectory(makeStorage(
          Record(tail = true)))
        val longName = (1 to (MaxNameLength + 1)).mkString
        evaluating { directory.createFile(longName, Date) } should produce [IllegalArgumentException]
        evaluating { directory.createDirectory(longName, Date) } should produce [IllegalArgumentException]
      }

      it("should ensure that name does not contain backward slash(es)") {
        val directory = makeDirectory(makeStorage(
          Record(tail = true)))
        evaluating { directory.createFile("\\", Date) } should produce [IllegalArgumentException]
        evaluating { directory.createDirectory("\\", Date) } should produce [IllegalArgumentException]
      }

      it("should ensure that name does not contain forward slash(es)") {
        val directory = makeDirectory(makeStorage(
          Record(tail = true)))
        evaluating { directory.createFile("/", Date) } should produce [IllegalArgumentException]
        evaluating { directory.createDirectory("/", Date) } should produce [IllegalArgumentException]
      }

      it("should ensure that name does not contain leading whitespace(s)") {
        val directory = makeDirectory(makeStorage(
          Record(tail = true)))
        evaluating { directory.createFile(" foo", Date) } should produce [IllegalArgumentException]
        evaluating { directory.createDirectory(" foo", Date) } should produce [IllegalArgumentException]
      }

      it("should ensure that name does not contain trailing whitespaces") {
        val directory = makeDirectory(makeStorage(
          Record(tail = true)))
        evaluating { directory.createFile("bar ", Date) } should produce [IllegalArgumentException]
        evaluating { directory.createDirectory("bar ", Date) } should produce [IllegalArgumentException]
      }

      it("should ensure that name does not clashes with existing file names") {
        val directory = makeDirectory(makeStorage(
          Record(name = "foo"),
          Record(tail = true)))
        evaluating { directory.createFile("foo", Date) } should produce [IllegalArgumentException]
        evaluating { directory.createDirectory("foo", Date) } should produce [IllegalArgumentException]
      }

      it("should ensure that new name does not clashes with existing directory names") {
        val directory = makeDirectory(makeStorage(
          Record(directory = true, name = "foo"),
          Record(tail = true)))
        evaluating { directory.createFile("foo", Date) } should produce [IllegalArgumentException]
        evaluating { directory.createDirectory("foo", Date) } should produce [IllegalArgumentException]
      }

      it("should report data format error when tail record is not present") {
        val directory = makeDirectory(makeStorage())
        evaluating { directory.createFile("readme", Date) } should produce [DataFormatException]
        evaluating { directory.createDirectory("readme", Date) } should produce [DataFormatException]
      }

      it("should throw NotEnoughSpaceException when a new chunk cannot be allocated") {
        val directory = makeDirectory(makeMetadata(), makeChunkStorage(1), makeStorage(Record(tail = true)))
        evaluating { directory.createFile("readme", Date) } should produce [NotEnoughSpaceException]
        evaluating { directory.createDirectory("readme", Date) } should produce [NotEnoughSpaceException]
      }

      it("should properly create a new file record") {
        val storage: MockRecordStorage = makeStorage(
          Record(tail = true))
        val directory = makeDirectory(storage)
        directory.createFile("readme", Date)
        storage should holdRecords (Record(directory = false, name = "readme", date = Date, chunk = 1), Record(tail = true))
      }

      it("should properly create a new directory record") {
        val storage: MockRecordStorage = makeStorage(
          Record(tail = true))
        val directory = makeDirectory(storage, makeStorage())
        directory.createDirectory("info", Date)
        storage should holdRecords (Record(directory = true, name = "info", date = Date, chunk = 1), Record(tail = true))
      }

      it("should initialize created directory") {
        val storageA: MockRecordStorage = makeStorage(Record(tail = true))
        val storageB: MockRecordStorage = makeStorage()
        val directory = makeDirectory(storageA, storageB)
        directory.createDirectory("info", Date)
        storageB should holdRecords (Record(tail = true))
      }

      it("should re-use deleted records") {
        val storage: MockRecordStorage = makeStorage(
          Record(deleted = true, name = "foo"),
          Record(tail = true))
        val directory = makeDirectory(storage)
        directory.createFile("bar", Date)
        storage should holdRecords (Record(name = "bar", date = Date, chunk = 1), Record(tail = true))
      }

      it("should append a new record when no deleted records available") {
        val storage: MockRecordStorage = makeStorage(
          Record(name = "foo"),
          Record(tail = true))
        val directory = makeDirectory(storage)
        directory.createFile("bar", Date)
        storage should holdRecords (Record(name = "foo"), Record(name = "bar", date = Date, chunk = 1), Record(tail = true))
      }
    }

    describe("(metadata)") {
      it("should present record as metadata") {
        val directory = makeDirectory(makeStorage(
          Record(name = "readme", date = Date, hidden = true),
          Record(tail = true)))
        val (_, Seq(file)) = directory.entries
        file.name should equal ("readme")
        file.date should equal (Date)
        file.hidden should equal (true)
      }

      it("should be associated with a record at a proper index") {
        val directory = makeDirectory(makeStorage(
          Record(name = "foo"),
          Record(name = "bar"),
          Record(tail = true)))
        val (_, Seq(_, file)) = directory.entries
        file.name should equal ("bar")
      }

      it("should cache record on reading") {
        val storage: MockRecordStorage = makeStorage(
          Record(name = "foo"),
          Record(tail = true))
        val directory = makeDirectory(storage)
        val (_, Seq(file)) = directory.entries
        storage.set(0, Record(name = "bar"))
        file.name should equal ("foo")
      }

      it("should update record on child entry properties change") {
        val storage: MockRecordStorage = makeStorage(
          Record(name = "foo"),
          Record(tail = true))
        val directory = makeDirectory(storage)
        val (_, Seq(file)) = directory.entries
        file.name = "bar"
        file.date = Date
        file.hidden = true
        storage should holdRecords (Record(name = "bar", date = Date, hidden = true), Record(tail = true))
      }

      it("should check entry name validity") {
        val storage: MockRecordStorage = makeStorage(
          Record(name = "foo"),
          Record(tail = true))
        val directory = makeDirectory(storage)
        val (_, Seq(file)) = directory.entries
        evaluating { file.name = "" } should produce [IllegalArgumentException]
        info("assume that it uses the same name checking as in entity creation")
      }

      it("should cache record on writing") {
        val storage: MockRecordStorage = makeStorage(
          Record(name = "foo"),
          Record(tail = true))
        val directory = makeDirectory(storage)
        val (_, Seq(file)) = directory.entries
        file.name = "readme"
        storage.set(0, Record(name = "bar"))
        file.name should equal ("readme")
      }

      describe("on deletion") {
        it("should report data format error when a tail record is not present") {
          val storage: MockRecordStorage = makeStorage(
            Record(name = "foo"),
            Record(tail = true))
          val directory = makeDirectory(storage)
          val (_, Seq(file)) = directory.entries
          storage.set(1, Record(name = "bar"))
          evaluating { file.delete() } should produce [DataFormatException]
        }

        it("should mark record as deleted") {
          val storage: MockRecordStorage = makeStorage(
            Record(name = "A"),
            Record(name = "B"),
            Record(tail = true))
          val directory = makeDirectory(storage)
          val (_, Seq(fileA, _)) = directory.entries
          fileA.delete()
          storage should holdRecords (Record(deleted = true, name = "A"), Record(name = "B"), Record(tail = true))
        }

        it("should prohibit repeated deletion after the record was marked as deleted") {
          val storage: MockRecordStorage = makeStorage(Record(name = "A"), Record(name = "B"), Record(tail = true))
          val directory = makeDirectory(storage)
          val (_, Seq(fileA, _)) = directory.entries
          fileA.delete()
          evaluating { fileA.delete() } should produce [IllegalStateException]
        }

        it("should truncate the record when there is no records after it") {
          val storage: MockRecordStorage = makeStorage(
            Record(name = "A"),
            Record(name = "B"),
            Record(tail = true))
          val directory = makeDirectory(storage)
          val (_, Seq(_, fileB)) = directory.entries
          fileB.delete()
          storage should holdRecords (Record(name = "A"), Record(tail = true))
        }

        it("should prohibit repeated deletion after the record was truncated") {
          val storage: MockRecordStorage = makeStorage(
            Record(name = "A"),
            Record(name = "B"),
            Record(tail = true))
          val directory = makeDirectory(storage)
          val (_, Seq(_, fileB)) = directory.entries
          fileB.delete()
          evaluating { fileB.delete() } should produce [IllegalStateException]
        }

        it("should be able to truncate a single record") {
          val storage: MockRecordStorage = makeStorage(
            Record(name = "A"),
            Record(tail = true))
          val directory = makeDirectory(storage)
          val (_, Seq(fileA)) = directory.entries
          fileA.delete()
          storage should holdRecords (Record(tail = true))
        }

        it("should truncate deleted records after the record being deleted") {
          val storage: MockRecordStorage = makeStorage(
            Record(name = "A"),
            Record(name = "B"),
            Record(deleted = true, name = "C"),
            Record(tail = true))
          val directory = makeDirectory(storage)
          val (_, Seq(_, fileB)) = directory.entries
          fileB.delete()
          storage should holdRecords (Record(name = "A"), Record(tail = true))
        }

        it("should be able to truncate all records") {
          val storage: MockRecordStorage = makeStorage(
            Record(name = "A"),
            Record(name = "B", deleted = true),
            Record(tail = true))
          val directory = makeDirectory(storage)
          val (_, Seq(fileA)) = directory.entries
          fileA.delete()
          storage should holdRecords (Record(tail = true))
        }

        it("should truncate deleted records before the record being deleted") {
          val storage: MockRecordStorage = makeStorage(
            Record(name = "A"),
            Record(deleted = true, name = "B"),
            Record(name = "C"),
            Record(tail = true))
          val directory = makeDirectory(storage)
          val (_, Seq(_, fileC)) = directory.entries
          fileC.delete()
          storage should holdRecords (Record(name = "A"), Record(tail = true))
        }

        it("should be able to handle before/after truncation simultaneously") {
          val storage: MockRecordStorage = makeStorage(
            Record(name = "A"),
            Record(name = "B", deleted = true),
            Record(name = "C"),
            Record(name = "D", deleted = true),
            Record(tail = true))
          val directory = makeDirectory(storage)
          val (_, Seq(_, fileC)) = directory.entries
          fileC.delete()
          storage should holdRecords (Record(name = "A"), Record(tail = true))
        }

        it("should not perform truncation when there are non-deleted records after the record being deleted") {
          val storage: MockRecordStorage = makeStorage(
            Record(name = "A"),
            Record(name = "B", deleted = true),
            Record(name = "C"),
            Record(name = "D", deleted = true),
            Record(name = "E"),
            Record(tail = true))
          val directory = makeDirectory(storage)
          val (_, Seq(_, fileC, _)) = directory.entries
          fileC.delete()
          storage should holdRecords (
            Record(name = "A"),
            Record(name = "B", deleted = true),
            Record(name = "C", deleted = true),
            Record(name = "D", deleted = true),
            Record(name = "E"),
            Record(tail = true))
        }
      }
    }
  }

  private def makeDirectory(storages: MockRecordStorage*): SubDirectoryImpl =
    makeDirectory(makeMetadata(), makeChunkStorage(), storages: _*)

  private def makeDirectory(metadata: Metadata): SubDirectoryImpl =
    makeDirectory(metadata, makeChunkStorage(), makeStorage())

  private def makeDirectory(metadata: Metadata, chunkStorage: ChunkStorage, storages: MockRecordStorage*): SubDirectoryImpl = {
    val chunk = chunkStorage.allocate().getOrElse {
      throw new RuntimeException("Cannot allocate an entry chunk")
    }
    val recordStorageFactory = (chunk: Chunk) => storages.lift(chunk.id).getOrElse {
      throw new RuntimeException(s"No record storage at index ${chunk.id}")
    }
    new SubDirectoryImpl(null, metadata, chunk, chunkStorage, recordStorageFactory)
  }

  private def makeMetadata(initializer: Metadata => Unit = _ => ()) = {
    val record = new MockMetadata()
    initializer(record)
    record
  }

  private def makeStorage(records: Record*) = new MockRecordStorage(MaxNameLength, records: _*)

  private def makeChunkStorage(capacity: Int = 10) = new MockChunkStorage(capacity)

  private def holdRecords(records: Record*) = new Matcher[MockRecordStorage] with Matchers {
    def apply(storage: MockRecordStorage) = equal(records)(storage.records)
  }
}