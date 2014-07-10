/* foreach.scala
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Copyright (C) 2013 Universiteit Gent
 *
 * This file is part of the Rasbeb project, an interactive web
 * application for the Belgian version of the international Bebras
 * competition.
 *
 * Corresponding author:
 *
 * Kris Coolsaet
 * Department of Applied Mathematics, Computer Science and Statistics
 * Ghent University
 * Krijgslaan 281-S9
 * B-9000 GENT Belgium
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * A copy of the GNU General Public License can be found in the file
 * LICENSE.txt provided with the source distribution of this program (see
 * the META-INF directory in the source jar). This license can also be
 * found on the GNU website at http://www.gnu.org/licenses/gpl.html.
 *
 * If you did not receive a copy of the GNU General Public License along
 * with this program, contact the lead developer, or write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package views.html.snippets

import play.twirl.api.Html

import scala.collection.{immutable, JavaConversions}

/**
 * Provides a more readable way to iterate over a Java Iterable
 */
object foreach {

  // borrowed from 'rasbeb' project (and converted to 2.3)

  // cannot be programmed as a template, see https://groups.google.com/forum/#!topic/play-framework/hWfgx7IwuCw

  /**
   * Iterate over the given Java iterable
   */
  def apply[T](iterable: java.lang.Iterable[T])(block: T => Html): Html = {
    new Html (JavaConversions.iterableAsScalaIterable(iterable)
      .map (block)
      .to[scala.collection.immutable.Seq])
  }

  /**
   * Iterate over the given Java iterable including an index
   */
  def withIndex[T](iterable: java.lang.Iterable[T])(block: (T, Int) => Html): Html = {
    new Html (JavaConversions.iterableAsScalaIterable(iterable)
      .zipWithIndex
      .map {t => block(t._1,t._2)}
      .to[scala.collection.immutable.Seq])
  }

  /**
   * Iterate over the given Java iterable including the strings "odd" and "even" alternately
   */
  def withOddEven[T](iterable: java.lang.Iterable[T])(block: (T, String) => Html): Html = {
    withIndex(iterable)((e,i) => block (e, if (i % 2 == 0) "odd" else "even"))
  }

  /**
   * Iterate over the given Java iterable generating a tr-object of alternating "odd" and "even" class.
   */
  def tr[T](iterable: java.lang.Iterable[T])(block: T => Html): Html = {
    var fragments = List[Html]()
    var odd = true
    for (el <- JavaConversions.iterableAsScalaIterable(iterable)) {
      fragments = fragments :+ Html("<tr class='" + (if (odd) "odd" else "even") + "'>")
      fragments = fragments :+ block(el)
      fragments = fragments :+ Html("</tr>")
      odd = !odd
    }
    new Html(fragments)
  }

  /**
   * Same as apply, but allows an alternative if the list is empty
   */
  def orElse[T](iterable: java.lang.Iterable[T])(block: T => Html)(elseBlock: => Html): Html = {
    if (iterable != null && iterable.iterator().hasNext) {
      apply(iterable)(block)
    } else {
      elseBlock
    }
  }

  /**
   * Same as apply, but allows an alternative if the list is empty
   */
  def trOrElse[T](iterable: java.lang.Iterable[T])(block: T => Html)(elseBlock: => Html): Html = {
    if (iterable != null && iterable.iterator().hasNext) {
      tr(iterable)(block)
    } else {
      elseBlock
    }
  }

  // TODO: ulOrElse everything in <ul>, elements enclosed in <li></li>

}
