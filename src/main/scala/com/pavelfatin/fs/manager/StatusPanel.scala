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
import javax.swing.border.EmptyBorder

private class StatusPanel(left: Component, right: Component)
                         (implicit scheme: ColorScheme) extends BorderPanel {
  background = scheme.background
  border = new EmptyBorder(3, 5, 7, 5)

  add(left, BorderPanel.Position.West)
  add(new PanelLabel(), BorderPanel.Position.Center)
  add(right, BorderPanel.Position.East)

  override protected def paintBorder(g: Graphics2D) {
    g.setColor(scheme.foreground)
    g.drawLine(0, 0, peer.getWidth, 0)
  }
}
