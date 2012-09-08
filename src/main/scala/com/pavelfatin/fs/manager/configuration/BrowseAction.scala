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

import java.io
import swing.{FileChooser, Action, TextField}
import FileChooser.SelectionMode

private class BrowseAction(field: TextField,
                           message: String,
                           selectionMode: SelectionMode.Value = SelectionMode.FilesOnly) extends Action("Browse") {
  def apply() {
    val chooser = new FileChooser(new io.File(field.text)) {
      title = message
      fileSelectionMode = selectionMode
    }

    chooser.showDialog(field, "Select") match {
      case FileChooser.Result.Approve =>
        val path = chooser.selectedFile.getPath
        field.text = path
        field.caret.position = path.length
      case _ => // do nothing
    }
  }
}
