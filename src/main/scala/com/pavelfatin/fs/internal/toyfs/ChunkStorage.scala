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

/** An indexed collection of chunks.
  *
  * @see [[com.pavelfatin.fs.internal.toyfs.Chunk]]
  * @see [[com.pavelfatin.fs.internal.toyfs.ChunkStorageImpl]]
  */
private trait ChunkStorage {
  /** Returns the number of chunks in this storage. */
  def size: Int

  /** Returns the chunk at the specified index in this storage.
    *
    * @param n index of the chunk to return
    * @return the chunk at the specified index in this storage
    * @throws IndexOutOfBoundsException if the index is out of range
    */
  def get(n: Int): Chunk

  /** Allocates a new chunk in this storage.
    *
    * @return the allocated chunk or `None` if the chunk cannot be allocated
    */
  def allocate(): Option[Chunk]
}

/** A `Data` that can be truncated and deleted.
  *
  * @note The implementation may have undefined `length`.
  *
  * @see [[com.pavelfatin.fs.Data]]
  * @see [[com.pavelfatin.fs.internal.toyfs.ChunkStorage]]
  */
private trait Chunk extends Data {
  /** Returns the identifier of this chunk (the index of this chunk in the parent storage). */
  def id: Int

  /** Truncates this chunk.
    *
    * If the present length of this chunk is less than the `threshold` argument
    * then no action will be performed.
    *
    * @note The new length of this chunk may still be greater than `threshold`.
    * @note Does not guarantee that the truncated content will be securely erased.
    *
    * @param threshold the new length of this chunk
    * @throws IllegalArgumentException if `threshold` argument is negative
    */
  def truncate(threshold: Long)

  /** Deletes this chunk from the parent storage.
    *
    * @note Does not guarantee that the chunk content will be securely erased.
    *
    * @see [[com.pavelfatin.fs.internal.toyfs.ChunkStorage]]
    */
  def delete()
}
