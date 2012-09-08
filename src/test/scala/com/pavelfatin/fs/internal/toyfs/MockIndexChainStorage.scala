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

import collection.mutable

private class MockIndexChainStorage(content: String) extends IndexChainStorage {
  private val Array(capacityText, chainsText) = content.split(":\\s*", 2)

  private var chains: mutable.Buffer[MockIndexChain] =
    chainsText.split("\\s*\\|\\s*")
      .filterNot(_.isEmpty)
      .map(it => new MockIndexChain(it.split('-').map(Integer.parseInt(_, 16))))
      .toBuffer

  val size = capacityText.toInt

  def get(n: Int): IndexChain = chains.find(_.id == n).get

  def allocate(): Option[IndexChain] = if (indices.length < size) {
    val chain = new MockIndexChain(freeIndices(1))
    chains += chain
    Some(chain)
  } else {
    None
  }

  private def indices: Array[Int] = chains.flatMap(_.indices).toArray

  private def freeIndices(count: Int): Seq[Int] = {
    val all = indices
    (0 until size).filterNot(all.contains).take(count)
  }

  def presentation: String = s"$size: ${chains.map(_.presentation).mkString(" | ")}"

  override def toString = s"${getClass.getSimpleName}($presentation)"

  private class MockIndexChain(xs: Seq[Int]) extends IndexChain {
    var indices: mutable.Buffer[Int] = xs.toBuffer

    val id = indices.head

    def get(offset: Int, canAllocate: Boolean) = indices.lift(offset).orElse {
      if (canAllocate) {
        indices ++= freeIndices(offset - indices.length + 1)
        indices.lift(offset)
      } else {
        None
      }
    }

    def truncate(size: Int) {
      indices = indices.take(size)
    }

    def delete() {
      chains -= this
    }

    def presentation: String = indices.mkString("-")

    override def toString = s"${getClass.getSimpleName}($presentation)"
  }
}
