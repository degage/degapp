/* Binders.scala
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

package binders

import data.Referrer
import play.api.mvc.{JavascriptLitteral, QueryStringBindable}

/**
 * Defines implicit binders for existing types.
 */
object Binders {
  // Note: this was easier to implement here in Scala, than make Referrer to implement QueryStringBindable<Referrer>
  // in Java. (One requirement seems to be that there must be a no arg constructor.)

  implicit def queryStringBinder = new QueryStringBindable[Referrer] with JavascriptLitteral[Referrer] {

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Referrer]] = {
      params.get(key).flatMap(_.headOption) match {
        case Some(string) => Some(Right(Referrer.get(string)))
        case _ => None
      }
    }

    override def unbind(key: String, ref: Referrer): String = {
      key + "=" + ref.getKey()
    }

    override def to(ref: Referrer) = ref.getKey()

  }
}
