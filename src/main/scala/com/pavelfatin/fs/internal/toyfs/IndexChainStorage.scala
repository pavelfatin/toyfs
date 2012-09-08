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

/** An indexed collection of `IndexChain` elements.
  *
  * @see [[com.pavelfatin.fs.internal.toyfs.IndexChain]]
  * @see [[com.pavelfatin.fs.internal.toyfs.IndexChainStorageImpl]]
  */
private trait IndexChainStorage {
  /** Returns the number of chains in this storage. */
  def size: Int

  /** Returns the chain at the specified index in this storage.
    *
    * @param n index of the chain to return
    * @return the chain at the specified index in this storage
    * @throws IndexOutOfBoundsException if the index is out of range
    */
  def get(n: Int): IndexChain

  /** Allocates a new chain in this storage.
    *
    * @return the allocated chain or `None` if the chain cannot be allocated
    */
  def allocate(): Option[IndexChain]
}

/** An indexed collection of integers.
  *
  * @see [[com.pavelfatin.fs.internal.toyfs.IndexChainStorage]]
  */
private trait IndexChain {
  /** Returns the identifier of this chain (the index of this chain in the parent storage). */
  def id: Int

  /** Returns or allocates the index at the specified offset in this chain.
    *
    * This method will allocate intermediate indices if needed.
    *
    * @param offset the offset of the index to return or allocate
    * @param canAllocate whether to allocate new indices
    * @return the index at the specified offset or `None` if there is no index
    *         at the specified offset (or if the index cannot be allocated)
    * @throws IllegalArgumentException if `offset` is negative
    */
  def get(offset: Int, canAllocate: Boolean): Option[Int]

  /** Truncates this chain.
    *
    * If the present size of this chain is less than the `size` argument
    * then no action will be performed.
    *
    * @note Truncation size must be greater than or equal to 1.
    *
    * @param size the new size of this chain
    * @throws IllegalArgumentException if `size` argument is less than 1
    */
  def truncate(size: Int)

  /** Deletes this chain from the parent storage.
    *
    * @see [[com.pavelfatin.fs.internal.toyfs.IndexChainStorage]]
    */
  def delete()
}
