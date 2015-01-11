/* foreach.scala
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Copyright Ⓒ 2014-2015 Universiteit Gent
 * 
 * This file is part of the Degage Web Application
 * 
 * Corresponding author (see also AUTHORS.txt)
 * 
 * Kris Coolsaet
 * Department of Applied Mathematics, Computer Science and Statistics
 * Ghent University 
 * Krijgslaan 281-S9
 * B-9000 GENT Belgium
 * 
 * The Degage Web Application is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The Degage Web Application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with the Degage Web Application (file LICENSE.txt in the
 * distribution).  If not, see http://www.gnu.org/licenses/.
 */

package views.txt.snippets

import play.twirl.api.Txt

import scala.collection.JavaConversions

/**
 * Provides a more readable way to iterate over a Java Iterable
 */
object foreach {

  // mostly here because twirl needs a 'snippet' object to exist in views.txt

  /**
   * Iterate over the given Java iterable
   */
  def apply[T](iterable: java.lang.Iterable[T])(block: T => Txt): Txt = {
    new Txt (JavaConversions.iterableAsScalaIterable(iterable)
      .map (block)
      .to[scala.collection.immutable.Seq])
  }

  /**
   * Iterate over the given Java iterable including an index
   */
  def withIndex[T](iterable: java.lang.Iterable[T])(block: (T, Int) => Txt): Txt = {
    new Txt (JavaConversions.iterableAsScalaIterable(iterable)
      .zipWithIndex
      .map {t => block(t._1,t._2)}
      .to[scala.collection.immutable.Seq])
  }

  /**
   * Iterate over the given Java iterable including the strings "odd" and "even" alternately
   */
  def withOddEven[T](iterable: java.lang.Iterable[T])(block: (T, String) => Txt): Txt = {
    withIndex(iterable)((e,i) => block (e, if (i % 2 == 0) "odd" else "even"))
  }

  /**
   * Same as apply, but allows an alternative if the list is empty
   */
  def orElse[T](iterable: java.lang.Iterable[T])(block: T => Txt)(elseBlock: => Txt): Txt = {
    if (iterable != null && iterable.iterator().hasNext) {
      apply(iterable)(block)
    } else {
      elseBlock
    }
  }

}
