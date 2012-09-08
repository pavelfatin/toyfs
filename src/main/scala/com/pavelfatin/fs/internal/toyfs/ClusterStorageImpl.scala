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

/** An implementation of `ClusterStorage` that presents a single source `Data`
  * as a collection of clusters (which are projections on the underlying data).
  *
  * This implementation combines clusters with consecutive indices into a single `Data`.
  *
  * @param data the source data
  * @param cluster the length of cluster
  * @throws IllegalArgumentException if cluster length is less than 1
  * @throws IllegalArgumentException if cluster count is greater than the maximum Int value
  *
  * @see [[com.pavelfatin.fs.Data]]
  * @see [[com.pavelfatin.fs.internal.toyfs.ChunkStorage]]
  */
private class ClusterStorageImpl(data: Data, cluster: Int) extends ClusterStorage {
  if (cluster < 1)
    throw new IllegalArgumentException(
      s"Cluster length ($cluster) is less than 1")

  if (clusterCount > Int.MaxValue)
    throw new IllegalArgumentException(
      s"Cluster count is greater than the maximum Int value: $clusterCount")

  private def clusterCount: Long = data.length / cluster

  val size = clusterCount.toInt

  val clusterLength = cluster

  def get(indices: Seq[Int]): Seq[Data] = {
    for (i <- indices) {
      if (i < 0)
        throw new IndexOutOfBoundsException(
          s"Cluster index is negative: $i")

      if (i >= size)
        throw new IndexOutOfBoundsException(
          s"Cluster index ($i) is greater than or equal to storage size ($size)")
    }

    for (group <- groupsIn(indices.toList);
         position = group.head.toLong * cluster;
         length = group.size.toLong * cluster)
    yield data.projection(position, length)
  }

  private def groupsIn(indices: List[Int]): List[List[Int]] = {
    val (prefix, suffix) = spanIn(indices)
    if (prefix.isEmpty) Nil else prefix :: groupsIn(suffix)
  }

  private def spanIn(indices: List[Int]): (List[Int], List[Int]) = indices match {
    case Nil => (Nil, Nil)
    case x :: Nil => (List(x), Nil)
    case x :: (ys @ (y :: _)) =>
      if (x == y - 1) {
        val (prefix, suffix) = spanIn(ys)
        (x :: prefix, suffix)
      } else {
        (List(x), ys)
      }
  }
}
