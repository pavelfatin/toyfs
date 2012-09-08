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

/** An indexed collection of records.
  *
  * @see [[com.pavelfatin.fs.internal.toyfs.Record]]
  * @see [[com.pavelfatin.fs.internal.toyfs.RecordStorageImpl]]
  */
private trait RecordStorage {
  /** Returns the maximum allowed name length in this storage. */
  def nameLength: Int


  /** Returns the record at the specified index in this storage.
    *
    * @param index index of the record to return
    * @return the record at the specified index in this storage
    * @throws IndexOutOfBoundsException if the index is out of range
    * @throws com.pavelfatin.fs.DataFormatException if data format error occurs
    */
  def get(index: Int): Record

  /** Replaces the record at the specified index in this storage
    * with the specified record.
    *
    * @param index index of the record to replace
    * @param record record to be stored at the specified index
    * @throws IndexOutOfBoundsException if the index is out of range
    * @throws IllegalArgumentException if the new record contains invalid data
    */
  def set(index: Int, record: Record)

  /** Truncates this storage.
    *
    * If the present size of this storage is less than the `size` argument
    * then no action will be performed.
    *
    * @note Does not guarantee that the truncated records will be securely erased.
    *
    * @param size the new size of this chunk
    * @throws IllegalArgumentException if `size` argument is negative
    */
  def truncate(size: Int)
}

/** A directory record data.
  *
  * @see [[com.pavelfatin.fs.internal.toyfs.RecordStorage]]
  */
private case class Record(tail: Boolean = false,
                          deleted: Boolean = false,
                          directory: Boolean = false,
                          hidden: Boolean = false,
                          name: String = "",
                          length: Long = 0L,
                          date: Calendar = {
                            val calendar = Calendar.getInstance()
                            calendar.setTimeInMillis(0L)
                            calendar
                          },
                          chunk: Int = 0)
