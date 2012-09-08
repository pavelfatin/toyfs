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

import swing.{Frame, Component, Dialog}
import java.io.{OutputStream, InputStream}
import java.util.Calendar

abstract class AbstractAction(context: ActionContext) extends Function0[Unit] {
  protected val frame: Frame = context.frame

  protected val parent: Component = Component.wrap(frame.peer.getRootPane)

  protected def active: FolderPanel = if (context.left.active) context.left else context.right

  protected def inactive: FolderPanel = if (context.left.active) context.right else context.left

  protected def kindOf(entry: FileSystemEntry): String = entry match {
    case _: File => "file"
    case _: Directory => "folder"
  }

  protected def describe(entry: FileSystemEntry): String =
    s"""${kindOf(entry)} "${entry.name}""""

  protected def lengthOf(entry: FileSystemEntry): Long = entry match {
    case file: File => file.length
    case directory: Directory =>
      val (directories, files) = directory.entries
      (files ++ directories).map(lengthOf).sum
  }

  protected def afterConfirmation[T](message: String, title: String)(block: => T): Option[T] =
    if (confirmed(message, title)) Some(block) else None

  protected def confirmed(message: String, title: String): Boolean =
    Dialog.showConfirmation(parent, message, title) == Dialog.Result.Yes

  protected def afterEditing(text: String, message: String, title: String)(block: String => Unit) {
    Dialog.showInput(parent, message, title, initial = text).foreach(block)
  }

  protected def afterCheckingClashes(name: String, directory: Directory, path: String)(block: => Unit) {
    val (directories, files) = directory.entries
    val clashes = (directories ++ files).exists(_.name == name)
    if (clashes) {
      val message = s"""Entry with name "$name" already exists in $path"""
      Dialog.showMessage(parent, message, "Unable to proceed", Dialog.Message.Warning)
    } else {
      block
    }
  }

  protected def afterCheckingFreeSpace(entry: FileSystemEntry, free: Long, path: String)(block: => Unit) {
    if (lengthOf(entry) > free) {
      val message = s"""Not enoug space for ${describe(entry)} in $path"""
      Dialog.showMessage(parent, message, "Unable to proceed", Dialog.Message.Warning)
    } else {
      block
    }
  }

  protected def transferWithProgress(total: Long, in: InputStream, out: OutputStream, message: String) {
    withProgress(message, total) { callback =>
      val numbers = Iterator.iterate(1L)(_ + 1)
      Iterator.continually(in.read()).takeWhile(_ != -1).zip(numbers).foreach { case (b, i) =>
        out.write(b)
        callback(i)
      }
    }
  }

  protected def withProgress(message: String, total: Long)(block: (Long => Unit) => Unit) {
    ProgressDialog.withProgress(context.frame, message, total)(block)
  }

  protected def delete(directory: Directory) {
    val (directories, files) = directory.entries
    files.foreach(_.delete())
    directories.foreach(delete)
    directory.delete()
  }

  protected def copy(source: File, destination: Directory, path: String, action: String)
                    (transform: Calendar => Calendar) {
    val date = transform(source.date)
    val file = destination.createFile(source.name, date)
    file.hidden = source.hidden
    source.readIn { in =>
      file.writeIn { out =>
        transferWithProgress(source.length, in, out, s"$action ${source.name} to $path")
      }
    }
    file.date = date // by-pass date changes performed by OS
  }

  protected def copy(source: Directory, destination: Directory, path: String, action: String)
                    (transform: Calendar => Calendar) {
    val date = transform(source.date)
    val directory = destination.createDirectory(source.name, date)
    directory.hidden = source.hidden
    val (directories, files) = source.entries
    val directoryPath = path + s"${directory.name}/"
    files.foreach(copy(_, directory, directoryPath, action)(transform))
    directories.foreach(copy(_, directory, directoryPath, action)(transform))
    directory.date = date // by-pass date changes performed by OS
  }
}
