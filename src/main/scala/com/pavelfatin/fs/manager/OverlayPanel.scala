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

import java.awt
import awt.{Container, LayoutManager}
import swing.{Panel, Component}
import javax.swing.JPanel

private class OverlayPanel(content: Component, header: Component, footer: Component, margin: Int) extends Panel {
  override lazy val peer = {
    new JPanel with SuperMixin {
      setLayout(new MyLayout())

      add(header.peer)
      add(footer.peer)
      add(content.peer)

      override def isOptimizedDrawingEnabled = false
    }
  }

  private class MyLayout extends LayoutManager {
    def addLayoutComponent(name: String, comp: awt.Component) {}

    def removeLayoutComponent(comp: awt.Component) {}

    def preferredLayoutSize(parent: Container) = parent.getSize

    def minimumLayoutSize(parent: Container) = content.minimumSize

    def layoutContainer(parent: Container) {
      content.peer.setBounds(0, 0, parent.getWidth, parent.getHeight)

      val maxWidth = parent.getWidth - margin * 2

      val headerWidth = header.preferredSize.width
      val headerHeight = header.preferredSize.height
      val headerMargin = (parent.getWidth - headerWidth) / 2
      header.peer.setBounds(headerMargin.max(margin), 0, headerWidth.min(maxWidth), headerHeight)

      val footerWidth = footer.preferredSize.width
      val footerHeight = footer.preferredSize.height
      val footerMargin = (parent.getWidth - footerWidth) / 2
      val footerX = footerMargin.max(margin)
      footer.peer.setBounds(footerX, parent.getHeight - footerHeight, footerWidth.min(maxWidth), footerHeight)
    }
  }
}