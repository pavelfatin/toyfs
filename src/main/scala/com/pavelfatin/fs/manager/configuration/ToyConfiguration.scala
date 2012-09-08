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
import java.io
import event.{ValueChanged, ButtonClicked}
import io.{FileOutputStream, BufferedOutputStream}
import javax.swing.{SpinnerNumberModel, JSpinner}
import internal.toyfs.ToyFS
import internal.data.{ArrayData, FileData}

private class ToyConfiguration extends AbstractConfiguration("ToyFS (a custom FAT-like file system") {
  private val DefaultContainerPath = new io.File(System.getProperty("user.home"), "volume.toyfs").getPath
  private val DefaultFileSize = 32
  private val DefaultMemorySize = 16

  private val pathField = new TextField(15)

  private val radioButtonA = new RadioButton("File-based:")

  private val radioButtonB = new RadioButton("Memory-based:")

  private val spinnerA = new JSpinner(new SpinnerNumberModel(DefaultFileSize, 1, 10000, 1))

  private val spinnerB = new JSpinner(new SpinnerNumberModel(DefaultMemorySize, 1, 1000, 1))

  private val label = new Label()

  private val panelA = createColumn(
    createRow("File", pathField, new Button(new BrowseAction(pathField, "Container file path"))),
    createRow("Size, MB", Component.wrap(spinnerA), label))

  private val panelB = createRow("Size, MB", Component.wrap(spinnerB))

  add(createColumn(createRadioButtonPanel(radioButtonA, panelA),
    createRadioButtonPanel(radioButtonB, panelB)), BorderPanel.Position.West)

  new ButtonGroup(radioButtonA, radioButtonB)

  radioButtonA.selected = true

  pathField.text = DefaultContainerPath
  pathField.caret.position = pathField.text.length

  radioButtonA.reactions += {
    case ButtonClicked(_) => updatePanels()
  }

  radioButtonB.reactions += {
    case ButtonClicked(_) => updatePanels()
  }

  pathField.reactions += {
    case ValueChanged(_) => updateFileInfo()
  }

  updatePanels()
  updateFileInfo()

  private def updateFileInfo() {
    val file = new io.File(pathField.text)
    val exists = file.exists && file.isFile
    label.text = if (exists) "(container file exists)" else ""
    spinnerA.setEnabled(!exists)
    spinnerA.setValue(if (exists) 1.max((file.length / 1024L / 1024L).toInt) else DefaultFileSize)
  }

  private def updatePanels() {
    enableAllIn(panelA, radioButtonA.selected)
    enableAllIn(panelB, radioButtonB.selected)
    if (radioButtonA.selected) updateFileInfo()
  }

  def createFileSystem() = {
    val size = {
      val spinner = if (radioButtonA.selected) spinnerA else spinnerB
      spinner.getValue.asInstanceOf[Integer].toLong * 1024L * 1024L
    }

    val data = if (radioButtonA.selected) {
      val file = new io.File(pathField.text)
      if (file.exists) new FileData(file) else allocateFileData(file, size)
    } else {
      new ArrayData(size.toInt)
    }

    val fs = new ToyFS(data)

    if (!fs.formatted) fs.format()

    fs
  }

  private def allocateFileData(file: io.File, size: Long): FileData = {
    val out = new BufferedOutputStream(new FileOutputStream(file))
    ProgressDialog.withProgress(null, "Allocating the container file", size) { callback =>
      try {
        var i = 0L
        while (i < size) {
          out.write(0)
          i += 1L
          callback(i)
        }
        out.flush()
      } finally {
        out.close()
      }
    }
    new FileData(file)
  }
}
