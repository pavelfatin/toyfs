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

import java.io.Closeable

/** An entity that can be either opened or closed.
  *
  * @define entity entity
  *
  * @see [[com.pavelfatin.fs.File]]
  * @see [[com.pavelfatin.fs.StreamIO]]
  */
trait OpenAndClose extends Closeable {
  /** Tests whether this $entity is opened. */
  def opened: Boolean

  /** Opens this $entity.
    *
    * @throws IllegalStateException if this $entity is already opened
    * @throws java.io.IOException if an I/O error occurs
    */
  def open()

  /** Closes this $entity.
    *
    * @throws IllegalStateException if this $entity is already closed
    * @throws java.io.IOException if an I/O error occurs
    */
  def close()
}
