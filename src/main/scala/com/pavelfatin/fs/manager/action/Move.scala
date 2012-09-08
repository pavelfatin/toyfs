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

class Move(context: ActionContext) extends AbstractAction(context) {
  def apply() {
    active.selection.foreach { entry =>
      val path = inactive.path
      afterConfirmation(s"Do you want to move the ${describe(entry)} to $path?", s"Move ${kindOf(entry).capitalize}") {
        val destination = inactive.directory
        afterCheckingClashes(entry.name, destination, path) {
          afterCheckingFreeSpace(entry, inactive.space, path) {
            entry match {
              case file: File =>
                copy(file, destination, path, "Moving")(identity)
                file.delete()
              case directory: Directory =>
                copy(directory, destination, path, "Moving")(identity)
                delete(directory)
            }
            active.refresh()
            inactive.refresh()
          }
        }
      }
    }
  }
}
