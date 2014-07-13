name := "Degage - web layer"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaCore,
  javaJdbc,
  javaWs,
  cache
)

// TODO: remove dependency on webjars-play. Play 2.3 has internal webjars support

libraryDependencies ++= Seq(
  "be.ugent.degage"         % "db"                           % "1.0-SNAPSHOT",
  "org.webjars"             %% "webjars-play"                % "2.3.0",  
  "com.typesafe.play.plugins" %% "play-plugins-mailer"       % "2.3.0",
  "org.webjars"             %  "bootstrap"                   % "3.1.1",
  "org.webjars"             % "jquery-ui"                    % "1.10.3",
  "org.webjars"             % "bootstrap-datetimepicker"     % "2.2.0", // Timepicker for bootstrap v3
  "org.mindrot"             % "jbcrypt"                      % "0.3m", // Library for secure password storage
  "org.webjars"             % "leaflet"                      % "0.7.2", // Library for maps
  "mysql"                   % "mysql-connector-java"         % "5.1.29",
  "com.typesafe.akka"       %% "akka-actor"                  % "2.3.0",
  "org.apache.poi"          % "poi"                          % "3.8",
  "org.apache.poi"          % "poi-ooxml"                    % "3.9",
  "com.itextpdf"            % "itextpdf"                     % "5.1.3",
  "com.typesafe"            % "config"                       % "1.2.1"
)

lazy val root = (project in file(".")).enablePlugins(PlayJava)

TwirlKeys.templateImports ++= Seq ("snippets._", "be.ugent.degage.db.models._")
