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

import swing.Alignment
import javax.swing.JLabel
import javax.swing.table.{TableColumn, DefaultTableColumnModel}

private class FolderColumnModel(implicit scheme: ColorScheme, format: Format) extends DefaultTableColumnModel {
  private val (column0, column1, column2) =
    (createColumn(0, "n", Alignment.Left),
      createColumn(1, "Size", Alignment.Center),
      createColumn(2, "Date", Alignment.Center))


  private val (sizeWidth, dateWidth) = {
    val label = new JLabel().getFontMetrics(scheme.font)
    (label.stringWidth(format.sizePrototype), label.stringWidth(format.datePrototype))
  }

  column0.setMaxWidth(Int.MaxValue)
  column1.setMaxWidth(sizeWidth)
  column1.setPreferredWidth(sizeWidth)
  column2.setMaxWidth(dateWidth)
  column2.setPreferredWidth(dateWidth)

  addColumn(column0)
  addColumn(column1)
  addColumn(column2)

  private def createColumn(index: Int, name: String, alignment: Alignment.Value) = {
    new TableColumn(index) {
      setHeaderValue(name)
      setHeaderRenderer(new HeaderRenderer(alignment))
      setCellRenderer(new CellRenderer())
    }
  }
}
