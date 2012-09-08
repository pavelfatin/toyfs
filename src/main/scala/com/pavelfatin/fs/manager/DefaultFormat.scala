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
package manager

import java.util.Calendar
import annotation.tailrec
import java.text.DateFormat

private class DefaultFormat extends Format {
  private val DateFormatter = DateFormat.getDateInstance

  def formatDate(date: Calendar) = DateFormatter.format(date.getTime)

  def formatSize(size: Long) = DefaultFormat.format(size, 6)

  val datePrototype = s"(${formatDate(Calendar.getInstance)})"

  val sizePrototype = "(123456)"
}

private object DefaultFormat {
  private val Units = List("", " K", " M", " G", " T")

  def format(x: Long, limit: Int): String = {
    @tailrec
    def format(x: Long, units: List[String]): String = units match {
      case h :: t =>
        val s = x.toString + h
        if (s.length < limit) s else {
          val next = (x.toDouble / 1024.0D).round
          format(next, t)
        }
      case _ => "inf"
    }
    format(x, Units)
  }
}
