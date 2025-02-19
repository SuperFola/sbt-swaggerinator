import sbt.librarymanagement.Resolver

credentials += Credentials(Path.userHome / ".sbt" / ".credentials")

addSbtPlugin("com.github.sbt"         % "sbt-native-packager" % "1.11.1")
addSbtPlugin("com.github.eikek"       % "sbt-openapi-schema"  % "0.13.1")

ThisBuild / organization := "lt.lexp.sbt"
ThisBuild / homepage     := Some(url("https://github.com/SuperFola/sbt-swaggerinator"))

githubOwner       := "SuperFola"
githubRepository  := "sbt-swaggerinator"
publishMavenStyle := true

lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-swaggerinator",
    organization := "lt.lexp.sbt",
    version := "1.4.0",
    publishConfiguration := publishConfiguration.value.withOverwrite(true),
    sbtPlugin := true,
    scriptedBufferLog := false
  )
