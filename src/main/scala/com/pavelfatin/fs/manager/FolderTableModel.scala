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

import javax.swing.table.DefaultTableModel
import swing.Alignment

private class FolderTableModel(format: Format) extends DefaultTableModel {
  private var entries = Seq.empty[FileSystemEntry]

  private var hasParent: Boolean = _

  private var _directory: Directory = _

  def directory: Directory = _directory

  def directory_=(it: Directory) {
    _directory = it
    refresh()
  }

  def refresh() {
    val (directories, files) = _directory.entries
    entries = _directory.parent.toSeq ++ directories.sortBy(_.name.toLowerCase) ++ files.sortBy(_.name.toLowerCase)
    hasParent = _directory.parent.isDefined
    fireTableDataChanged()
  }

  def entryAt(index: Int): Option[FileSystemEntry] = entries.lift(index)

  override def getRowCount = Option(entries).map(_.length).getOrElse(0)

  override def getValueAt(row: Int, column: Int) = {
    val entry = entries(row)

    val parent = hasParent && row == 0

    val text = column match {
      case 0 => if (parent) ".." else entry.name
      case 1 => entry match {
        case file: File => format.formatSize(file.length)
        case _: Directory => if (parent) "Up" else "Folder"
      }
      case 2 => format.formatDate(entry.date)
    }

    CellValue(text,
      if (parent && column == 1) Alignment.Center else if (column == 0) Alignment.Left else Alignment.Right,
      !parent && entry.isInstanceOf[Directory],
      !parent && entry.hidden)
  }

  override def isCellEditable(row: Int, column: Int) = false
}
