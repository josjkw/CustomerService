ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.13"

val dependencies = Seq(
  "org.typelevel"         %% "cats-effect"         % "3.5.4",
  "org.http4s"            %% "http4s-dsl"          % "0.23.18",
  "org.http4s"            %% "http4s-blaze-server" % "0.23.13",
  "org.http4s"            %% "http4s-blaze-client" % "0.23.13",
  "org.http4s"            %% "http4s-circe"        % "0.23.18",
  "io.circe"              %% "circe-core"          % "0.14.3",
  "io.circe"              %% "circe-generic"       % "0.14.3",
  "io.circe"              %% "circe-parser"        % "0.14.3",
  "ch.qos.logback"         % "logback-classic"     % "1.4.14",
  "io.scalaland"          %% "chimney"             % "0.8.5",
  "com.github.pureconfig" %% "pureconfig"          % "0.17.6",
)

lazy val `customer-service` = project
  .in(file("customer-service"))
  .settings(
    libraryDependencies ++= dependencies
  )
  .dependsOn(`libs`)

lazy val `customer-details` = project
  .in(file("customer-details"))
  .settings(
    libraryDependencies ++= dependencies
  )

lazy val `customer-details-legacy` = project
  .in(file("customer-details-legacy"))
  .settings(
    libraryDependencies ++= dependencies
  )

lazy val `libs` = project
  .in(file("libs"))
  .settings(
    libraryDependencies ++= dependencies
  )
