name := "Degage - db layer"

normalizedName := "db"

version := "1.0-SNAPSHOT"

organization := "be.ugent.degage"

crossPaths := false

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.29"

libraryDependencies += "com.google.guava" % "guava" % "17.0"

// TODO: remove
libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.1"

// TODO: remove
libraryDependencies += "joda-time" % "joda-time" % "2.3"

libraryDependencies += "com.novocode" % "junit-interface" % "0.10" % "test"

// TODO: use java.security instead ?
libraryDependencies += "org.mindrot"  % "jbcrypt"  % "0.3m"


autoScalaLibrary := false

parallelExecution in Test := false

