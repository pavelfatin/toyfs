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
import Dialog.Result
import event.WindowClosed
import javax.swing.UIManager
import com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel
import configuration.ConfigurationWindow

object Application extends App {
  private implicit val Title = "Toy File Manager"
  private implicit val ColorScheme = new DefaultColorScheme()
  private implicit val Format = new DefaultFormat()

  Swing.onEDT {
    UIManager.setLookAndFeel(new NimbusLookAndFeel())

    val configuration = new ConfigurationWindow(s"$Title: Configuration")

    configuration.reactions += {
      case WindowClosed(_) if configuration.result == Result.Ok =>
        open(new ManagerWindow(Title, configuration.createLeftFileSystem(), configuration.createRightFileSystem()))
    }

    open(configuration)
  }

  private def open(frame: Frame) {
    frame.pack()
    frame.centerOnScreen()
    frame.open()
  }
}
