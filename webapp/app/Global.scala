/* Global.scala
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
 * distribution).  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.File

import com.typesafe.config.ConfigFactory
import play.api.{Application, Configuration, GlobalSettings, Mode}

/**
 * Adapts the global settings object to our needs. Most of the functionality is
 * delegated to  {@link JavaGlobal}
 */
object Global extends GlobalSettings {

  // we have chosen the Scala version because the Java version does not seem
  // to allow to change the Configuration in onLoadConfig

  /**
   * Merge additional config files into the application, depending on the current mode
   */
  override def onLoadConfig(config: Configuration, path: File, classloader: ClassLoader, mode: Mode.Mode): Configuration = {

    // see http://stackoverflow.com/questions/9723224/how-to-manage-application-conf-in-several-environments-with-play-2-0

    config ++ Configuration(ConfigFactory.load(mode.toString.toLowerCase + ".conf"))
  }

  /**
   * Initialize the data access provider for this application
   * and the mail server
   */
  override def onStart(app: Application) {
    app.mode match {
      case Mode.Dev =>
        JavaGlobal.onStartDev()
      case Mode.Prod =>
        JavaGlobal.onStartProd()
      case Mode.Test =>
        JavaGlobal.onStartTest()
    }

  }

  /**
   * Stop the mail server
   */
  override def onStop(app: Application) {
    app.mode match {
      case Mode.Dev =>
        JavaGlobal.onStopDev()
      case Mode.Prod =>
        JavaGlobal.onStopProd()
      case Mode.Test =>
        JavaGlobal.onStopTest()
    }
  }

}
