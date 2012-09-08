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
package action

import swing.Dialog
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

class Open(context: ActionContext, editable: Boolean)
          (implicit scheme: ColorScheme, format: Format) extends AbstractAction(context) {

  private val MaxFileLength = 1024 * 512

  def apply() {
    active.selection match {
      case Some(file: File) =>
        if (file.length < MaxFileLength || confirmed("The file is too large." +
          " Do you really want to view it?", "File is too large")) {

          val viewer = new FileViewer(context.frame, file.name, loadTextFrom(file), editable)
          viewer.pack()
          viewer.centerOnScreen()
          viewer.open()

          if (viewer.modified) {
            afterConfirmation(s"Do you want to save changes to the ${describe(file)}?", "File has changed") {
              saveTextTo(file, viewer.text)
              active.refresh()
            }
          }
        }
      case Some(directory: Directory) if !editable =>
        val files = filesWithin(directory)
        val size = files.map(_.length).sum
        Dialog.showMessage(parent, s"Files: ${files.length}. Total size: ${format.formatSize(size)} ($size bytes)", describe(directory).capitalize)
      case _ => // do nothing
    }
  }

  private def loadTextFrom(file: File): String = {
    val out = new ByteArrayOutputStream(file.length.toInt)
    file.readIn { in =>
      transferWithProgress(file.length, in, out, s"Reading ${file.name}")
    }
    out.toString
  }

  private def saveTextTo(file: File, text: String) {
    val bytes = text.getBytes
    val in = new ByteArrayInputStream(bytes)
    file.writeIn { out =>
      transferWithProgress(text.length, in, out, s"Writing ${file.name}")
      file.truncate(bytes.length)
    }
  }

  protected def filesWithin(directory: Directory): Seq[File] = {
    val (directories, files) = directory.entries
    files ++ directories.flatMap(filesWithin)
  }
}
