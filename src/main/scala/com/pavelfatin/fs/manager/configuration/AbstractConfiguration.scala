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
import java.awt.Dimension
import javax.swing.border.EmptyBorder

private abstract class AbstractConfiguration(name: String) extends BorderPanel with Configuration {
  protected def createColumn(rows: Component*): Component = new BoxPanel(Orientation.Vertical) {
    rows.foreach(_.xLayoutAlignment = 0.0D)
    contents ++= rows
  }

  protected def createRow(title: String, components: Component*): Component = new FlowPanel(FlowPanel.Alignment.Left)() {
    contents += new Label(s"$title:") {
      preferredSize = new Dimension(60, preferredSize.height)
      horizontalAlignment = Alignment.Leading
      peer.setLabelFor(components.head.peer)
      peer.setDisplayedMnemonic(title.head)
    }

    contents ++= components
  }

  protected def createRadioButtonPanel(button: RadioButton, component: Component): Component = new BoxPanel(Orientation.Vertical) {
    button.xLayoutAlignment = 0.0D
    contents += button
    contents += new FlowPanel(FlowPanel.Alignment.Left)() {
      xLayoutAlignment = 0.0D
      border = new EmptyBorder(0, 10, 0, 0)
      contents += component
    }
  }

  protected def enableAllIn(root: Component, enable: Boolean) {
    root match {
      case container: Container =>
        container.contents.foreach(enableAllIn(_, enable))
      case component =>
        component.enabled = enable
    }
  }

  override def toString = name
}
