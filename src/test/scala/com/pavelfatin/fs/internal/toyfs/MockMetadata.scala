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

private class MockMetadata extends Metadata {
  private var _deleted = false

  var name = ""

  var length = 0L

  var date = {
    val calendar = Calendar.getInstance()
    calendar.setTimeInMillis(0L)
    calendar
  }

  var hidden = false

  def deleted: Boolean = _deleted

  def delete() {
    _deleted = true
  }

  override def toString = getClass.getSimpleName
}
