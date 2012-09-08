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

import javax.swing.table.JTableHeader
import java.awt.Graphics

private trait HeaderGridPainter extends JTableHeader {
  abstract override def paintComponent(g: Graphics) {
    super.paintComponent(g)

    val xs = List
      .tabulate(getColumnModel.getColumnCount - 1)(getColumnModel.getColumn)
      .map(_.getWidth)
      .scanLeft(0)(_ + _)

    g.setColor(getTable.getGridColor)

    xs.foreach { x =>
      g.drawLine(x - 1, g.getClipBounds.y, x - 1, getHeight)
    }
  }
}
