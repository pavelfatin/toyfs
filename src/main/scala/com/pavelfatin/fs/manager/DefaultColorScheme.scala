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

import java.awt.{Color, Font}

private class DefaultColorScheme extends ColorScheme {
  val fontSize = 18

  val font = new Font("Monospaced", Font.BOLD, fontSize)

  val background = new Color(0x000080)

  val foreground = new Color(0x00FFFF)

  val header = new Color(0xFFFF00)

  val directory = new Color(0xFFFFFF)

  val hidden = new Color(0x008080)

  val selectionBackground = new Color(0x008080)

  val selectionForeground = new Color(0x000000)

  val selectionForegroundDirectory = new Color(0xFFFFFF)

  val selectionForegroundHidden = new Color(0x808080)

  val consoleBackground = new Color(0x000000)

  val consoleForeground = new Color(0xC0C0C0)
}
