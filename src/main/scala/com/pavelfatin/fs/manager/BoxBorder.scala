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

import javax.swing.border.EmptyBorder
import java.awt._

private class BoxBorder(hInset: Int, vInset: Int, offset: Int)
                       (implicit scheme: ColorScheme) extends EmptyBorder(vInset, hInset, vInset, hInset) {
  override def paintBorder(c: Component, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
    g.setColor(scheme.background)

    g.fillRect(0, 0, left, height)
    g.fillRect(width - right, 0, right, height)
    g.fillRect(0, 0, width, top)
    g.fillRect(0, height - bottom, width, bottom)

    g.setColor(scheme.foreground)

    def drawRect(hGap: Int, vGap: Int) {
      g.drawRect(hGap, vGap, width - hGap * 2 - 1, height - vGap * 2 - 1)
    }

    drawRect(left - offset - 1, top - offset - 1)
    drawRect(left - 1, top - 1)
  }
}
