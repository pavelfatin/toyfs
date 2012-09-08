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

/** An index table abstraction that is independent of underlying data format.
  *
  * Index table is an indexed sequence of entries (the indexing is zero-based).
  *
  * @see [[com.pavelfatin.fs.internal.toyfs.Entry]]
  * @see [[com.pavelfatin.fs.internal.toyfs.IndexTableImpl]]
  */
private trait IndexTable {
  /** Returns the number of entries in this table. */
  def size: Int

  /** Returns the number of `Free` entries in this table.
    *
    * This method may either count `Free` entries directly or rely
    * on some pre-computed data (for better performance).
    */
  def free: Int

  /** Returns the entry at the specified index in this table.
    *
    * @param n index of the entry to return
    * @return the entry at the specified index in this table
    * @throws IndexOutOfBoundsException if the index is out of range
    * @throws com.pavelfatin.fs.DataFormatException if data format error occurs
    */
  def get(n: Int): Entry

  /** Replaces the entry at the specified index in this table
    * with the specified entry.
    *
    * @note the index of `Reference` entries must differs from `n` and be within [0, `size`)
    *
    * @param n index of the entry to replace
    * @param entry entry to be stored at the specified index
    * @throws IndexOutOfBoundsException if the index is out of range
    * @throws IllegalArgumentException if the new entry contains invalid data
    */
  def set(n: Int, entry: Entry)

  /** Allocates a new entry in this table.
    *
    * Searches for a `Free` entry and replaces it with `End` entry.
    *
    * This method may either scan for `Free` entries directly or rely
    * on some pre-computed data (for better performance).
    *
    * @note Does not guarantee that the entry will be allocated at the least possible index.
    *
    * @return the index of the allocated entry (if there was `Free` entries in this table)
    *         or None (if there was no free entries available)
    */
  def allocate(): Option[Int]

  /** Replaces all entries in this table with `Free` entries. */
  def clear()
}

/** An index table entry.
  *
  * @see [[com.pavelfatin.fs.internal.toyfs.IndexTable]]
  */
private sealed trait Entry

private object Entry {
  /** A free cluster entry. */
  case object Free extends Entry

  /** An end of cluster chain entry. */
  case object End extends Entry

  /** A reference to the next entry.
    *
    * @param n the index of the next entry
    */
  case class Reference(n: Int) extends Entry
}
