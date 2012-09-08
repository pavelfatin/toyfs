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

/** A root directory implementation.
  *
  * Root directory is a directory that has no parent.
  *
  * Manages records and provides metadata for all its child entries.
  *
  * Does not require a parent metadata (as all the properties of
  * root directory are predefined and cannot be modified).
  *
  * @param chunk the chunk that holds data of records for this directory
  * @param chunkStorage the storage of chunks
  * @param recordStorageFactory the function that represents `Chunk` as a `RecordStorage`
  *
  * @see [[com.pavelfatin.fs.Directory]]
  * @see [[com.pavelfatin.fs.internal.RootDirectory]]
  * @see [[com.pavelfatin.fs.internal.toyfs.Chunk]]
  * @see [[com.pavelfatin.fs.internal.toyfs.ChunkStorage]]
  * @see [[com.pavelfatin.fs.internal.toyfs.RecordStorage]]
  * @see [[com.pavelfatin.fs.internal.toyfs.AbstractDirectoryImpl]]
  */
private class RootDirectoryImpl(chunk: Chunk,
                                chunkStorage: ChunkStorage,
                                recordStorageFactory: Chunk => RecordStorage)
  extends AbstractDirectoryImpl(chunk, chunkStorage, recordStorageFactory) with RootDirectory
