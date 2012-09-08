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

import javax.swing.table.DefaultTableCellRenderer
import javax.swing.JTable
import swing.Alignment
import javax.swing.border.EmptyBorder

private class HeaderRenderer(alignment: Alignment.Value)(implicit scheme: ColorScheme) extends DefaultTableCellRenderer {
  setBackground(scheme.background)
  setForeground(scheme.header)
  setFont(scheme.font)
  setHorizontalAlignment(alignment.id)
  setBorder(new EmptyBorder(8, 5, 0, 5))

  override def getTableCellRendererComponent(table: JTable, value: Any, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int) = {
    val property = value.asInstanceOf[String]
    setText(property)
    this
  }
}
