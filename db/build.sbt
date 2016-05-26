name := "Degage - db layer"

normalizedName := "db"

version := "1.3-SNAPSHOT"

organization := "be.ugent.degage"

crossPaths := false

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.37"

libraryDependencies += "com.google.guava" % "guava" % "18.0"

libraryDependencies += "com.novocode" % "junit-interface" % "0.10" % "test"

// TODO: use java.security instead ?
libraryDependencies += "org.mindrot"  % "jbcrypt"  % "0.3m"

autoScalaLibrary := false

parallelExecution in Test := false

