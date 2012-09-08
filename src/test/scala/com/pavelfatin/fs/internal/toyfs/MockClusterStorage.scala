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

private class MockClusterStorage(clusters: String) extends ClusterStorage {
  private val parts: Array[ByteData] = clusters.split("\\s*\\|\\s").filter(_.nonEmpty).map(new ByteData(_))

  def size = parts.length

  def clusterLength = parts.headOption.map(_.length.toInt).getOrElse(0)

  def get(indices: Seq[Int]) = indices.map(parts)

  def clear() {
    parts.foreach(_.clear())
  }

  def presentation: String = parts.map(_.presentation).mkString(" | ")

  override def toString = s"${getClass.getSimpleName}($presentation)"
}
