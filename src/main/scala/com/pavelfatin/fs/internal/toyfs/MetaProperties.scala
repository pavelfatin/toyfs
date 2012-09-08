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

/** A partial implementation of `FileSystemEntry` that delegates reading and
  * writing of `name`, `data` and `hidden` properties to a given `Metadata`.
  *
  * @see [[com.pavelfatin.fs.FileSystemEntry]]
  * @see [[com.pavelfatin.fs.internal.toyfs.Metadata]]
  * @see [[com.pavelfatin.fs.internal.toyfs.FileImpl]]
  * @see [[com.pavelfatin.fs.internal.toyfs.SubDirectoryImpl]]
  */
private trait MetaProperties extends FileSystemEntry {
  /** An actual holder of the properties. */
  protected def meta: Metadata

  def name = meta.name

  def name_=(s: String) {
    meta.name = s
  }

  def date = meta.date

  def date_=(c: Calendar) {
    meta.date = c
  }

  def hidden = meta.hidden

  def hidden_=(b: Boolean) {
    meta.hidden = b
  }
}
