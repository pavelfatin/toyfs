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
package configuration

import swing._
import swing.Dialog.Result
import java.awt.event.KeyEvent
import javax.swing.{JComponent, KeyStroke}
import javax.swing.border.EmptyBorder

class ConfigurationWindow(caption: String) extends Frame {
  private var _result = Dialog.Result.Cancel

  private val okAction = new Action("OK") {
    def apply() {
      _result = Result.Ok
      dispose()
    }
  }

  private val cancelAction = new Action("Cancel") {
    accelerator = Some(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0))

    def apply() {
      dispose()
    }
  }

  private val cancelButton = new Button(cancelAction) {
    preferredSize = new Dimension(80, preferredSize.height)
  }

  private val okButton = new Button(okAction) {
    preferredSize = new Dimension(80, preferredSize.height)
  }

  private val leftPanel = new ConfigurationPanel("Left Panel", createConfigurations(), 1)

  private val rightPanel = new ConfigurationPanel("Right Panel", createConfigurations(), 0)

  title = caption

  contents = new BorderPanel {
    border = new EmptyBorder(5, 5, 5, 5)

    add(new BorderPanel {
      add(leftPanel, BorderPanel.Position.West)
      add(rightPanel, BorderPanel.Position.Center)
    }, BorderPanel.Position.Center)

    add(new BorderPanel {
      add(new FlowPanel {
        contents += okButton
        contents += cancelButton
      }, BorderPanel.Position.East)
    }, BorderPanel.Position.South)
  }

  defaultButton = okButton

  register(cancelAction)

  def result: Result.Value = _result

  def createLeftFileSystem(): FileSystem = leftPanel.configuration.createFileSystem()

  def createRightFileSystem(): FileSystem = rightPanel.configuration.createFileSystem()

  private def createConfigurations() = List(new ToyConfiguration(), new WrapConfiguration(), new TmpConfiguration())

  private def register(action: Action) {
    action.accelerator.foreach { keyStroke =>
      val root = peer.getRootPane
      root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, action.title)
      root.getActionMap.put(action.title, action.peer)
    }
  }

  override def closeOperation() {
    dispose()
  }
}
