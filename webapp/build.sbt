name := "Degage - web layer"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.1"

fork in run := true

libraryDependencies ++= Seq(
  javaCore,
  javaJdbc,
  javaWs,
  cache
)

// For hikari                                                                      exit

resolvers += Resolver.url("Edulify Repository", url("http://edulify.github.io/modules/releases/"))(Resolver.ivyStylePatterns)

libraryDependencies ++= Seq(
  "be.ugent.degage"         % "db"                           % "1.1-SNAPSHOT",
  "com.edulify"             %% "play-hikaricp"               % "1.5.0-fork-2",    // 1.5.0 adapted to use hikari 2.3.2
  "org.springframework"     % "spring-beans"                 % "4.0.6.RELEASE", // needed for recursive direct field binding
  "com.typesafe.play"       %% "play-mailer"                 % "2.4.0-RC1",
  "com.typesafe.akka"       %% "akka-actor"                  % "2.3.0",
  "org.webjars"             %  "bootstrap"                   % "3.3.1",
  "org.webjars"             % "jquery"                       % "2.1.1",
  "org.webjars"             % "bootstrap-datetimepicker"     % "2.2.0", // Timepicker for bootstrap v3
  "org.webjars"             % "leaflet"                      % "0.7.2", // Library for maps
  "mysql"                   % "mysql-connector-java"         % "5.1.34",
  "org.apache.poi"          % "poi"                          % "3.8",
  "org.apache.poi"          % "poi-ooxml"                    % "3.9",
  "com.itextpdf"            % "itextpdf"                     % "5.1.3",
  "com.typesafe"            % "config"                       % "1.2.1"
  // "org.webjars"             % "jquery-ui"                    % "1.11.0-1", customized version stored in javascript/
)

lazy val root = (project in file(".")).enablePlugins(PlayJava,SbtWeb) // SbtWeb needed for Less plugin

TwirlKeys.templateImports ++= Seq ("snippets._", "be.ugent.degage.db.models._")

