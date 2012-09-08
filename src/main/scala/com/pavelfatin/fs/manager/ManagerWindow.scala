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
import event.FocusGained
import javax.swing.{KeyStroke, JComponent}
import javax.swing.plaf.basic.BasicSplitPaneUI
import java.awt.event.KeyEvent
import action._

private class ManagerWindow(caption: String, leftFS: FileSystem, rightFS: FileSystem)
                           (implicit scheme: ColorScheme, format: Format) extends MainFrame with ActionContext {
  private val actions = List(
    createAction("View", KeyEvent.VK_F3, new Open(this, editable = false)),
    createAction("Edit", KeyEvent.VK_F4, new Open(this, editable = true)),
    createAction("Copy", KeyEvent.VK_F5, new Copy(this)),
    createAction("Rename", KeyEvent.VK_F6, new Rename(this)),
    createAction("MkFold", KeyEvent.VK_F7, new MakeFolder(this)),
    createAction("Delete", KeyEvent.VK_F8, new Delete(this)),
    createAction("Move", KeyEvent.VK_F9, new Move(this)),
    createAction("Quit", KeyEvent.VK_F10, new Quit(this)))

  def frame = this

  val left = new FolderPanelImpl(leftFS)

  val right = new FolderPanelImpl(rightFS)

  actions.foreach(register)

  listenTo(left.component)
  listenTo(right.component)

  reactions += {
    case _: FocusGained =>
      val hasWindows = peer.getOwnedWindows.exists(_.isVisible)
      if (!hasWindows) {
        left.active = left.component.hasFocus
        right.active = right.component.hasFocus
      }
  }

  preferredSize = new Dimension(1100, 800)

  title = caption

  contents = {
    val splitPane = new SplitPane(Orientation.Vertical, left, right) {
      resizeWeight = 0.5D
      dividerSize = 7
      background = scheme.consoleForeground
      peer.setUI(new BasicSplitPaneUI)
      val inputMap = peer.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
      inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "none")
      inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0), "none")
    }

    val actionsPane = new ActionPanel(actions.init, List(actions.last))

    new BorderPanel() {
      add(splitPane, BorderPanel.Position.Center)
      add(actionsPane, BorderPanel.Position.South)
    }
  }

  private def createAction(title: String, key: Int, delegate: () => Unit): Action = new Action(title) {
    accelerator = Some(KeyStroke.getKeyStroke(key, 0))

    def apply() {
      try {
        delegate()
      } catch {
        case e: Exception =>
          e.printStackTrace(System.err)
          Dialog.showMessage(contents.head, e.toString, s"Error on $title", Dialog.Message.Error)
          left.refresh()
          right.refresh()
      }
    }
  }

  private def register(action: Action) {
    action.accelerator.foreach { keyStroke =>
      val root = peer.getRootPane
      root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, action.title)
      root.getActionMap.put(action.title, action.peer)
    }
  }
}