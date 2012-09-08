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
package action

import swing.{ScrollPane, Window, TextArea, Dialog}
import swing.event.{ValueChanged, Key, KeyPressed}
import javax.swing.{JComponent, KeyStroke}
import java.awt.event.KeyEvent

private class FileViewer(parent: Window, name: String, s: String, editable: Boolean = false)
                        (implicit scheme: ColorScheme) extends Dialog(parent) {
  private val originalText = s

  private val area = new TextArea(s) {
    font = scheme.font
    background = scheme.background
    foreground = scheme.foreground
    columns = 80
    rows = 25
    tabSize = 4
    peer.setSelectionColor(scheme.selectionBackground)
    peer.setSelectedTextColor(scheme.selectionForeground)
    peer.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
      .put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "none")
  }

  area.editable = editable

  listenTo(area)
  listenTo(area.keys)

  reactions += {
    case ValueChanged(_) => updateTitle()
    case KeyPressed(_, Key.Escape, _, _) => close()
  }

  contents = new ScrollPane(area) {
    border = null
  }

  modal = true

  updateTitle()

  def text: String = area.text

  def modified: Boolean = editable && text != originalText

  private def updateTitle() {
    title = {
      val action = if (editable) "edit" else "view"
      val suffix = if (modified) "*" else ""
      s"$action $name $suffix"
    }
  }
}
