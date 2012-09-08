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

import swing.{BorderPanel, FlowPanel, ComboBox}
import javax.swing.border.{TitledBorder, EmptyBorder}
import swing.event.SelectionChanged
import java.awt.Dimension

private class ConfigurationPanel(title: String, configurations: Seq[Configuration], index: Int) extends BorderPanel {
  private val comboBox = new ComboBox(configurations)

  private val settingsPanel = new FlowPanel(FlowPanel.Alignment.Left)()

  border = new TitledBorder(title)

  add(new BorderPanel {
    border = new EmptyBorder(0, 0, 10, 0)
    add(comboBox, BorderPanel.Position.North)
  }, BorderPanel.Position.North)

  add(settingsPanel, BorderPanel.Position.Center)

  settingsPanel.preferredSize = configurations.map(_.preferredSize).foldLeft(new Dimension(0, 0)) { (max, it) =>
    if (it.width > max.width || it.height > max.height) it else max
  }

  comboBox.selection.index = index

  listenTo(comboBox.selection)

  reactions += {
    case SelectionChanged(_) => updateSettingsPanel()
  }

  updateSettingsPanel()

  def configuration: Configuration = comboBox.selection.item

  private def updateSettingsPanel() {
    settingsPanel.contents.clear()
    settingsPanel.contents += comboBox.selection.item
    settingsPanel.peer.validate()
    settingsPanel.repaint()
  }
}
