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

import swing.Table
import javax.swing.{KeyStroke, JComponent}
import javax.swing.table.{TableColumnModel, JTableHeader, TableCellRenderer}
import java.awt.KeyboardFocusManager
import java.awt.event.KeyEvent

private class FolderTable(columnModel: TableColumnModel, tableModel: FolderTableModel, renderer: TableCellRenderer)
                         (implicit scheme: ColorScheme, format: Format) extends Table with TableGridPainter {
  showGrid = false
  gridColor = scheme.foreground
  background = scheme.background
  rowHeight = scheme.fontSize
  selection.intervalMode = Table.IntervalMode.Single

  peer.setAutoCreateColumnsFromModel(false)
  peer.setFillsViewportHeight(true)
  peer.setDefaultRenderer(classOf[CellValue], renderer)
  peer.setColumnModel(columnModel)
  peer.setTableHeader(new JTableHeader(columnModel) with HeaderGridPainter)

  model = tableModel

  restoreDefaultFocusTraversalKeys()
  configureKeyBindings()

  private def restoreDefaultFocusTraversalKeys() {
    val focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager

    peer.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
      focusManager.getDefaultFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS))

    peer.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
      focusManager.getDefaultFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS))
  }

  private def configureKeyBindings() {
    val inputMap = peer.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "none")
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0), "none")

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), "selectFirstRow")
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0), "selectLastRow")

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "selectFirstRow")
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "selectLastRow")
  }
}
