name := "Degage - web layer"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.8"

fork in run := true

libraryDependencies ++= Seq(
  javaCore,
  javaJdbc,
  javaWs,
  cache
)

// For hikari

resolvers += Resolver.url("Edulify Repository", url("http://edulify.github.io/modules/releases/"))(Resolver.ivyStylePatterns)

libraryDependencies ++= Seq(
  "be.ugent.degage"         % "db"                           % "1.4-SNAPSHOT",
  "be.ugent.caagt"           % "sheeter"                      % "1.1-SNAPSHOT",
  "com.edulify"             %% "play-hikaricp"               % "1.5.0-fork-2",    // 1.5.0 adapted to use hikari 2.3.2
  "org.springframework"     % "spring-beans"                 % "4.0.6.RELEASE", // needed for recursive direct field binding
  "com.typesafe.play"       %% "play-mailer"                 % "2.4.0-RC1",
  // "com.typesafe.akka"       %% "akka-actor"                  % "2.0.3",
  "org.pegdown"             %  "pegdown"                     % "1.4.0",
  "org.webjars"             %  "bootstrap"                   % "3.3.6",
  "org.webjars"             % "jquery"                       % "2.1.1",
  "org.webjars"             % "font-awesome"                 % "4.7.0",
  "org.webjars"             % "bootstrap-datetimepicker"     % "2.2.0", // Timepicker for bootstrap v3
  "org.webjars"             % "leaflet"                      % "0.7.2", // Library for maps
  "org.webjars"             % "jquery_are-you-sure"          % "1.5.0", // checks user does not leave page when form data is changed
  "org.webjars"             % "js-cookie"                    % "2.0.2", // cookie manipulation
  "mysql"                   % "mysql-connector-java"         % "5.1.37",
  "com.itextpdf"            % "itextpdf"                     % "5.5.6",      // most recent version, shoul override version use by next module?
  "it.innove"               % "play2-pdf"                    % "1.1.3-fork", // adapted to iText 5
  "com.typesafe"            % "config"                       % "1.2.1",
  "com.google.code.gson"    % "gson"                         % "2.8.0",
  "com.fasterxml.jackson.core" % "jackson-databind"           % "2.2.2"
  // "org.webjars"             % "jquery-ui"                    % "1.11.0-1", customized version stored in javascript/
)

lazy val root = (project in file(".")).enablePlugins(PlayJava,SbtWeb) // SbtWeb needed for Less plugin

TwirlKeys.templateImports ++= Seq ("snippets._", "be.ugent.degage.db.models._")

PlayKeys.routesImport ++= Seq ("data._", "binders._", "binders.Binders._")

val browserifyTask = taskKey[Seq[File]]("Run browserify")

val browserifyOutputDir = settingKey[File]("Browserify output directory")

browserifyOutputDir := target.value / "web" / "browserify"

browserifyTask := {
  val outputFile = browserifyOutputDir.value / "main.js"
  browserifyOutputDir.value.mkdirs
  val cmd = "./node_modules/.bin/browserify -t [ babelify --presets [ es2015 react stage-2 ] ] app/assets/javascripts/main.jsx -o " + outputFile.getPath
  val stderrBuffer = new scala.collection.mutable.ListBuffer[String]
  //val status = cmd ! new ProcessLogger {
//    def info(s: => String) {}
  //  def error(s: => String) { stderrBuffer.append(s) }
    //def buffer[T](f: => T): T = f
  //}
  //if (status != 0) error(stderrBuffer.mkString("\n", "\n", "\n"))
  List(outputFile)
}

sourceGenerators in Assets <+= browserifyTask

resourceDirectories in Assets += browserifyOutputDir.value
