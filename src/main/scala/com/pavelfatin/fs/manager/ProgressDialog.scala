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

import swing._

private class ProgressDialog(owner: Window, message: String, total: Long) extends Dialog(owner) {
  private val step = 1L.max(total / 100L)

  private val denominator = if (total == 0L) 1 else (total.toDouble / Int.MaxValue.toDouble).ceil.toInt

  private val bar = new ProgressBar {
    max = (total / denominator).toInt
  }

  preferredSize = new Dimension(350, 60)

  contents = bar

  def update(value: Long) {
    if (value % step == 0L) {
      title = {
        val percent = if (total == 0L) 100 else (value.toDouble / total.toDouble * 100.0D).round.toInt
        s"$message (${percent.toString}%)"
      }
      bar.value = (value / denominator).toInt
      bar.peer.paintImmediately(bar.peer.getVisibleRect)
    }
  }
}

object ProgressDialog {
  def withProgress(owner: Window, message: String, total: Long)(block: (Long => Unit) => Unit) {
    val progress = new ProgressDialog(owner, message, total)
    progress.pack()
    progress.centerOnScreen()
    progress.open()
    try {
      block(progress.update)
    } finally {
      progress.dispose()
    }
  }
}