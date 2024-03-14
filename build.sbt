ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.13"

lazy val root = (project in file(".")).settings(
  name := "CustomerService"
)
libraryDependencies += "org.typelevel" %% "cats-effect" % "3.5.4"

val http4sVersion = "0.23.18"
val http4sBlaze = "0.23.13"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sBlaze,
  "org.http4s" %% "http4s-blaze-client" % http4sBlaze,
  "org.http4s" %% "http4s-circe" % http4sVersion
)

val circeVersion = "0.14.3"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

