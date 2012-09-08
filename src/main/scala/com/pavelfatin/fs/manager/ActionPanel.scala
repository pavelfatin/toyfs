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
import javax.swing.plaf.basic.BasicButtonUI

private class ActionPanel(left: Seq[Action], right: Seq[Action])(implicit scheme: ColorScheme) extends BorderPanel {
  background = scheme.consoleBackground

  add(ActionPanel.createButtons(left), BorderPanel.Position.West)
  add(ActionPanel.createButtons(right), BorderPanel.Position.East)
}

private object ActionPanel {
  private val KeyNumberPattern = "pressed F(\\d+)".r
  private val ButtonTextWidth = 8

  private def createButtons(actions: Seq[Action])(implicit scheme: ColorScheme) = {
    new BoxPanel(Orientation.Horizontal) {
      opaque = false

      for ((action, i) <- actions.zipWithIndex) {
        if (i > 0) contents += createLabel(" ")
        contents += createLabel(textFor(action))
        contents += createButton(action, ButtonTextWidth)
      }
    }
  }

  private def createButton(action: Action, width: Int)(implicit scheme: ColorScheme) = {
    new Button(action) {
      font = scheme.font
      background = scheme.selectionBackground
      foreground = scheme.selectionForeground
      text = action.title.padTo(width, ' ')
      focusable = false
      peer.setUI(new BasicButtonUI)
    }
  }

  private def textFor(action: Action) = action.accelerator.mkString match {
    case KeyNumberPattern(n) => n
    case _ => ""
  }

  private def createLabel(text: String)(implicit scheme: ColorScheme) = {
    new Label(text) {
      font = scheme.font
      background = scheme.consoleBackground
      foreground = scheme.consoleForeground
    }
  }
}
