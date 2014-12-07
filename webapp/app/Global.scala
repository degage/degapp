/* Global.scala
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
