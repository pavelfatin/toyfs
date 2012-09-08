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
import swing.event._
import javax.swing.border.EmptyBorder

private class FolderPanelImpl(fs: FileSystem)
                             (implicit scheme: ColorScheme, format: Format) extends BorderPanel with FolderPanel {
  private var steps: List[Step] = Nil

  private val model = new FolderTableModel(format)

  private val renderer = new CellRenderer()

  private val table = new FolderTable(new FolderColumnModel(), model, renderer)

  private val pathLabel = new PanelLabel() {
    background = scheme.selectionBackground
    foreground = scheme.selectionForeground
    horizontalAlignment = Alignment.Trailing
  }

  private val leftLabel, rightLabel, spaceLabel = new PanelLabel()

  private val content = {
    val contentPane = new BorderPanel {
      border = new BoxBorder(9, 12, 2)
      background = scheme.background

      val scrollPane = new ScrollPane(table) {
        border = new EmptyBorder(0, 3, 0, 3)
      }

      add(scrollPane, BorderPanel.Position.Center)
      add(new StatusPanel(leftLabel, rightLabel), BorderPanel.Position.South)
    }

    new OverlayPanel(contentPane, pathLabel, spaceLabel, 18)
  }

  listenTo(table)
  listenTo(table.keys)
  listenTo(table.mouse.clicks)
  listenTo(table.selection)

  reactions += {
    case _: FocusGained => if (table.selection.rows.isEmpty) selectRowIfPresent(0)
    case KeyPressed(_, Key.Enter, _, _) => navigate()
    case MouseClicked(_, _, _, 2, _) => navigate()
    case _: TableRowsSelected => updateStatusLabels()
  }

  add(content, BorderPanel.Position.Center)

  display(fs.root)
  selectRowIfPresent(0)
  updatePathLabel()
  updateSpaceLabel()

  def component: Component = table

  def active: Boolean = renderer.active

  def active_=(b: Boolean) {
    renderer.active = b
    table.repaint()
  }

  def selection = selectedIndex.flatMap { index =>
    if (steps.nonEmpty && index == 0) None else model.entryAt(index)
  }

  def directory = model.directory

  def path = steps.map(_.name).reverse.mkString(s"${fs.name}/", "/", "")

  def space = fs.free

  def refresh() {
    val selection = selectedIndex
    model.refresh()
    selection.foreach { index =>
      selectRowIfPresent(index.min(table.rowCount - 1))
    }
    updateSpaceLabel()
  }

  private def updateStatusLabels() {
    leftLabel.text = selection.map(_.name).mkString
    rightLabel.text = selection match {
      case Some(file: File) => file.length.toString
      case Some(_: Directory) => "Folder"
      case _ => ""
    }
  }

  private def selectedIndex: Option[Int] = table.selection.rows.headOption

  private def updatePathLabel() {
    pathLabel.text = s" $path "
  }

  private def updateSpaceLabel() {
    val total = format.formatSize(fs.size)
    val free = format.formatSize(fs.free)
    spaceLabel.text = s"Total: $total ($free free)"
  }

  private def navigate() {
    selectedIndex.foreach { index =>
      model.entryAt(index) match {
        case Some(directory: Directory) =>
          navigate(index, directory)
          updatePathLabel()
        case _ => // do nothing
      }
    }
  }

  private def navigate(index: Int, directory: Directory) {
    val back = index == 0 && model.directory.parent.isDefined

    display(directory)

    val newIndex = if (back) {
      val h :: t = steps
      steps = t
      h.index
    } else {
      steps ::= Step(index, directory.name)
      0
    }

    selectRowIfPresent(newIndex)
  }

  private def display(directory: Directory) {
    model.directory = directory
  }

  private def selectRowIfPresent(index: Int) {
    if (index >= 0 && table.rowCount > index) {
      table.selection.rows.clear()
      table.selection.rows += index
    }
  }
}
