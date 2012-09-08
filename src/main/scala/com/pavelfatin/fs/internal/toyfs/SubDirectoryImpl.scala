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

/** A sub-directory implementation.
  *
  * Sub-directory is a directory that has a parent.
  *
  * Manages records and provides metadata for all its child entries.
  * Represents a given metadata as properties of this sub-directory.
  *
  * Empty sub-directories can be deleted.
  *
  * @param owner the parent directory of this sub-directory
  * @param metadata the metadata of this sub-directory
  * @param chunk the chunk that holds data of records for this sub-directory
  * @param chunkStorage the storage of chunks
  * @param recordStorageFactory the function that represents `Chunk` as a `RecordStorage`
  *
  * @see [[com.pavelfatin.fs.Directory]]
  * @see [[com.pavelfatin.fs.internal.toyfs.Chunk]]
  * @see [[com.pavelfatin.fs.internal.toyfs.ChunkStorage]]
  * @see [[com.pavelfatin.fs.internal.toyfs.Metadata]]
  * @see [[com.pavelfatin.fs.internal.toyfs.MetaProperties]]
  * @see [[com.pavelfatin.fs.internal.toyfs.RecordStorage]]
  * @see [[com.pavelfatin.fs.internal.toyfs.AbstractDirectoryImpl]]
  */
private class SubDirectoryImpl(owner: Directory,
                               metadata: Metadata,
                               chunk: Chunk,
                               chunkStorage: ChunkStorage,
                               recordStorageFactory: Chunk => RecordStorage)
  extends AbstractDirectoryImpl(chunk, chunkStorage, recordStorageFactory) with MetaProperties {

  protected def meta = metadata

  def parent = Some(owner)

  def delete() {
    if (entryRecords.nonEmpty)
      throw new IllegalStateException(
        s"Non-empty directory '$name' cannot be deleted")

    metadata.delete()
    chunk.delete()
  }
}
