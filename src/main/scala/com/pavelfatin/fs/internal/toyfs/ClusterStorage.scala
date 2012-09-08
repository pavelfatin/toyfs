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

/** An indexed collection of clusters.
  *
  * Each cluster is represented as `Data` with a fixed length (that is equal to `clusterLength`).
  *
  * @see [[com.pavelfatin.fs.Data]]
  * @see [[com.pavelfatin.fs.internal.toyfs.ClusterStorageImpl]]
  */
private trait ClusterStorage {
  /** Returns the number of clusters in this storage. */
  def size: Int

  /** Returns the length of cluster in this storage. */
  def clusterLength: Int

  /** Returns a sequence of `Data` associated with clusters at the specified indices.
    *
    * This method may return a single joint `Data` instance for several consecutive clusters.
    *
    * @note The notion of "consecutive clusters" is implementation-dependent
    *       (it may not necessarily correlate with the notion of "consecutive indices").
    *
    * @param indices the indices of clusters
    * @return the sequence of data associated with clusters at the specified indices
    * @throws IndexOutOfBoundsException if any index is out of range
    *
    * @see [[com.pavelfatin.fs.Data]]
    */
  def get(indices: Seq[Int]): Seq[Data]
}
