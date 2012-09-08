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
import swing.{FileChooser, Button, TextField, BorderPanel}
import internal.wrapfs.WrapFS

private class WrapConfiguration extends AbstractConfiguration("WrapFS (a host file system wrapper)") {
  private val DefaultPath = new io.File(System.getProperty("user.home")).getPath

  private val pathField = new TextField(15)

  add(createRow("Directory", pathField, new Button(new BrowseAction(pathField,
    "Root directory path", FileChooser.SelectionMode.DirectoriesOnly))), BorderPanel.Position.North)

  pathField.text = DefaultPath
  pathField.caret.position = pathField.text.length

  def createFileSystem() = new WrapFS(new io.File(pathField.text))
}