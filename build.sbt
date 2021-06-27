name := "oapicodegen"
organizationName := "com.gitlab.skibcsit"
version := "0.1"

scalaVersion := "2.12.8"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

// https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

// https://mvnrepository.com/artifact/com.eed3si9n/treehugger
libraryDependencies += "com.eed3si9n" %% "treehugger" % "0.4.4"

// https://mvnrepository.com/artifact/commons-cli/commons-cli
libraryDependencies += "commons-cli" % "commons-cli" % "1.4"

// https://mvnrepository.com/artifact/io.swagger.parser.v3/swagger-parser
libraryDependencies += "io.swagger.parser.v3" % "swagger-parser" % "2.0.25"

// https://mvnrepository.com/artifact/org.endpoints4s/sttp-client
libraryDependencies += "org.endpoints4s" %% "sttp-client" % "2.0.0" % Test

// https://mvnrepository.com/artifact/com.softwaremill.sttp.client/json4s
libraryDependencies += "com.softwaremill.sttp.client" %% "json4s" % "2.2.9" % Test

// https://mvnrepository.com/artifact/org.json4s/json4s-jackson
libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.6.11" % Test

// https://mvnrepository.com/artifact/org.endpoints4s/json-schema-generic
libraryDependencies += "org.endpoints4s" %% "json-schema-generic" % "1.3.0" % Test

// https://mvnrepository.com/artifact/org.endpoints4s/algebra
libraryDependencies += "org.endpoints4s" %% "algebra" % "1.3.0" % Test

// https://mvnrepository.com/artifact/org.endpoints4s/algebra-json-schema
libraryDependencies += "org.endpoints4s" %% "algebra-json-schema" % "1.3.0" % Test

// https://mvnrepository.com/artifact/org.endpoints4s/algebra-circe
libraryDependencies += "org.endpoints4s" %% "algebra-circe" % "1.3.0" % Test

// https://mvnrepository.com/artifact/io.circe/circe-generic
libraryDependencies += "io.circe" %% "circe-generic" % "0.13.0" % Test

// https://mvnrepository.com/artifact/org.scalatest/scalatest
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % Test

// https://mvnrepository.com/artifact/com.fasterxml.jackson.module/jackson-module-scala
libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.12.3" % Test
