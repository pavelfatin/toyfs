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

import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer

private class CellRenderer()(implicit scheme: ColorScheme) extends DefaultTableCellRenderer {
  var active: Boolean = false

  setFont(scheme.font)

  override def getTableCellRendererComponent(table: JTable, value: Any, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int) = {
    val property = value.asInstanceOf[CellValue]

    val highlighted = active && isSelected

    val background = if (highlighted) scheme.selectionBackground else scheme.background

    val foreground = (highlighted, property.directory, property.hidden) match {
      case (false, false, false) => scheme.foreground
      case (false, false, true) => scheme.hidden

      case (false, true, false) => scheme.directory
      case (false, true, true) => scheme.hidden

      case (true, false, false) => scheme.selectionForeground
      case (true, false, true) => scheme.selectionForegroundHidden

      case (true, true, false) => scheme.selectionForegroundDirectory
      case (true, true, true) => scheme.selectionForegroundHidden
    }

    setBackground(background)
    setForeground(foreground)
    setText(property.text)
    setHorizontalAlignment(property.alignment.id)

    this
  }
}
