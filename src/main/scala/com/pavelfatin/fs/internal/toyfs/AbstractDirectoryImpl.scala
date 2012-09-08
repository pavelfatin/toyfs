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

import java.util.Calendar

/** A template `Directory` implementation.
  *
  * This implementation stores all directory records in a `Chunk` represented as a `RecordStorage`.
  *
  * A "tail" record is used to denote the last record in the storage.
  *
  * Orphan records are marked as "deleted". All trailing deleted records are always truncated.
  *
  * @param chunk the chunk that holds data of records for this directory
  * @param chunkStorage the storage of chunks
  * @param recordStorageFactory the function that represents `Chunk` as a `RecordStorage`
  *
  * @see [[com.pavelfatin.fs.Directory]]
  * @see [[com.pavelfatin.fs.internal.toyfs.Chunk]]
  * @see [[com.pavelfatin.fs.internal.toyfs.ChunkStorage]]
  * @see [[com.pavelfatin.fs.internal.toyfs.RecordStorage]]
  * @see [[com.pavelfatin.fs.internal.toyfs.RootDirectoryImpl]]
  * @see [[com.pavelfatin.fs.internal.toyfs.SubDirectoryImpl]]
  */
private abstract class AbstractDirectoryImpl(chunk: Chunk,
                                             chunkStorage: ChunkStorage,
                                             recordStorageFactory: Chunk => RecordStorage) extends Directory {

  protected val recordStorage: RecordStorage = recordStorageFactory(chunk)

  def init() {
    recordStorage.set(0, Record(tail = true))
  }

  def entries = {
    val (directoryRecords, fileRecords) = {
      val metadata = entryRecords.map(p => MetadataImpl(p._1, p._2)).toList
      metadata.partition(_.record.directory)
    }

    val directories = directoryRecords.map { meta =>
      new SubDirectoryImpl(this, meta, chunkStorage.get(meta.record.chunk), chunkStorage, recordStorageFactory)
    }

    val files = fileRecords.map { meta =>
      new FileImpl(this, meta, chunkStorage.get(meta.record.chunk))
    }

    (directories, files)
  }

  protected def records: Iterator[(Int, Record)] = Iterator.from(0).takeWhile(_ < Int.MaxValue).map { i =>
    val record = try {
      recordStorage.get(i)
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new DataFormatException(
          s"No tail record in directory '$name'")
    }

    (i, record)
  }

  protected def entryRecords: Iterator[(Int, Record)] = records.takeWhile(!_._2.tail).filter(!_._2.deleted)

  def createFile(name: String, date: Calendar) = {
    check(name)
    val chunk = chunkStorage.allocate().getOrElse {
      throw new NotEnoughSpaceException("Cannot create a new file entry")
    }
    val record = Record(directory = false, name = name, date = date, chunk = chunk.id)
    new FileImpl(this, allocate(record), chunk)
  }

  def createDirectory(name: String, date: Calendar) = {
    check(name)
    val chunk = chunkStorage.allocate().getOrElse {
      throw new NotEnoughSpaceException("Cannot create a new directory entry")
    }
    val record: Record = Record(directory = true, name = name, date = date, chunk = chunk.id)
    val directory = new SubDirectoryImpl(this, allocate(record), chunk, chunkStorage, recordStorageFactory)
    directory.init()
    directory
  }

  private def check(name: String) {
    if (name.isEmpty)
      throw new IllegalArgumentException("Name is empty")

    if (name.length > recordStorage.nameLength)
      throw new IllegalArgumentException(
        s"Name length (${name.length}) is greater than maximum name length (${recordStorage.nameLength}): '$name'")

    if (name.contains("\\"))
      throw new IllegalArgumentException(s"Name contains '\\' character: '$name'")

    if (name.contains("/"))
      throw new IllegalArgumentException(s"Name contains '/' character: '$name'")

    if (name.startsWith(" "))
      throw new IllegalArgumentException(s"Name contains leading whitespace(s): '$name'")

    if (name.endsWith(" "))
      throw new IllegalArgumentException(s"Name contains trailing whitespace(s): '$name'")

    val names = entryRecords.map(_._2.name.toLowerCase)

    if (names.contains(name.toLowerCase)) {
      throw new IllegalArgumentException(s"Duplicate name: '$name'")
    }
  }

  private def allocate(record: Record): Metadata = {
    val (index, oldRecord) = records.find(p => p._2.deleted || p._2.tail).getOrElse {
      throw new DataFormatException(s"No tail record in directory '$name'")
    }

    if (oldRecord.tail) {
      if (index == Int.MaxValue)
        throw new NotEnoughSpaceException(s"Cannot alocate a new record in directory '$name'.")

      recordStorage.set(index + 1, new Record(tail = true))
    }

    recordStorage.set(index, record)

    MetadataImpl(index, record)
  }

  /** A `Metadata` implementation that stores its properties in a `Record`.
    *
    * @param index the index of the record in the parent `RecordStorage`
    * @param record the record
    */
  protected case class MetadataImpl(index: Int, var record: Record) extends Metadata {
    private var deleted = false

    def name = record.name

    def name_=(it: String) {
      check(it)
      record = record.copy(name = it)
      save()
    }

    def length = record.length

    def length_=(it: Long) {
      record = record.copy(length = it)
      save()
    }

    def date = record.date

    def date_=(it: Calendar) {
      record = record.copy(date = it)
      save()
    }

    def hidden = record.hidden

    def hidden_=(it: Boolean) {
      record = record.copy(hidden = it)
      save()
    }

    def delete() {
      if (deleted)
        throw new IllegalStateException(
          s"The record '${record.name}' metadata is already deleted")

      doDelete()

      deleted = true
    }

    private def doDelete() {
      val lastEntryIndex = {
        val otherEntryIndices = entryRecords.map(_._1).filterNot(_ == index)
        lastIn(otherEntryIndices).getOrElse(-1)
      }

      if (lastEntryIndex < index) {
        recordStorage.set(lastEntryIndex + 1, Record(tail = true))
        recordStorage.truncate(lastEntryIndex + 2)
      } else {
        record = record.copy(deleted = true)
        save()
      }
    }

    private def save() {
      recordStorage.set(index, record)
    }
  }

  private def lastIn[T](it: Iterator[T]): Option[T] = {
    var result: Option[T] = None
    while (it.hasNext) {
      result = Some(it.next())
    }
    result
  }
}
