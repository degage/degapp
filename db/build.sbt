name := "Degage - db layer"

normalizedName := "db"

version := "1.0-SNAPSHOT"

organization := "be.ugent.degage"

crossPaths := false

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.34"

libraryDependencies += "com.google.guava" % "guava" % "17.0"

libraryDependencies += "com.novocode" % "junit-interface" % "0.10" % "test"

// TODO: use java.security instead ?
libraryDependencies += "org.mindrot"  % "jbcrypt"  % "0.3m"

autoScalaLibrary := false

parallelExecution in Test := false

