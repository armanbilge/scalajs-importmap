ThisBuild / tlBaseVersion := "0.0"

ThisBuild / organization := "com.armanbilge"
ThisBuild / organizationName := "Arman Bilge"
ThisBuild / developers += tlGitHubDev("armanbilge", "Arman Bilge")
ThisBuild / startYear := Some(2023)

ThisBuild / tlSonatypeUseLegacyHost := false

val scala2_12 = "2.12.17"
val scala2_13 = "2.13.10"

lazy val root =
  project
    .in(file("."))
    .aggregate(importmap2_12, importmap2_13, sbtPlugin)
    .enablePlugins(NoPublishPlugin)

lazy val importmap = projectMatrix
  .in(file("importmap"))
  .settings(
    name := "scalajs-importmap",
    libraryDependencies ++= Seq(
      "org.scala-js" %% "scalajs-linker-interface" % scalaJSVersion,
    ),
  )
  .defaultAxes(VirtualAxis.jvm)
  .jvmPlatform(scalaVersions = Seq(scala2_12, scala2_13))

lazy val importmap2_12 = importmap.jvm(scala2_12)
lazy val importmap2_13 = importmap.jvm(scala2_12)

lazy val sbtPlugin = project
  .in(file("sbt-plugin"))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-scalajs-importmap",
    addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion),
  )
  .dependsOn(importmap2_12)
