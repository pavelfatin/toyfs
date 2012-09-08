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

import java.io.{BufferedOutputStream, BufferedInputStream, OutputStream, InputStream}

/** Control structure-like API for reading from- and writing to streams.
  *
  * Provides resource opening/closing and a proper exception handling.
  *
  * @note The provided streams are buffered.
  *
  * @define entity entity
  *
  * @see [[com.pavelfatin.fs.File]]
  * @see [[com.pavelfatin.fs.Streams]]
  * @see [[com.pavelfatin.fs.OpenAndClose]]
  */
trait StreamIO { self: Streams with OpenAndClose =>
  /** Performs input stream reading with automatic resource management.
    *
    * Both this $entity and a new input stream are opened before `f` invocation
    * and closed afterwards (including a case when an exception is thrown in `f`).
    *
    * @note The provided input stream is buffered.
    *
    * @example {{{
    * val content = file.readIn { stream =>
    *   Iterator.continually(stream.read()).takeWhile(_ != -1).mkString
    * }
    * }}}
    *
    * @param f a function that actually performs reading
    * @tparam T the type of the result value
    * @return the result value of `f`
    * @throws java.io.IOException if an I/O error occurs
    */
  def readIn[T](f: (InputStream) => T): T = {
    open()
    val in = new BufferedInputStream(createInputStream())
    try {
      f(in)
    } finally {
      try {
        in.close()
      } finally {
        close()
      }
    }
  }

  /** Performs output stream writing with automatic resource management.
    *
    * Both this $entity and a new output stream are opened before `f` invocation
    * and closed afterwards (including a case when an exception is thrown in `f`).
    *
    * @note The provided output stream is buffered.
    *
    * @example {{{
    * file.writeIn { stream =>
    *   words.foreach { word =>
    *     stream.write(word.getBytes))
    *   }
    * }
    * }}}
    *
    * @param f a function that actually performs writing
    * @throws java.io.IOException if an I/O error occurs
    */
  def writeIn(f: (OutputStream) => Unit) {
    open()
    val out = new BufferedOutputStream(createOutputStream())
    try {
      f(out)
      out.flush()
    } finally {
      try {
        out.close()
      } finally {
        close()
      }
    }
  }
}
