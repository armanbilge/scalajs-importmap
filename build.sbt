ThisBuild / tlBaseVersion := "0.1"

ThisBuild / organization := "com.armanbilge"
ThisBuild / organizationName := "Arman Bilge"
ThisBuild / developers += tlGitHubDev("armanbilge", "Arman Bilge")
ThisBuild / startYear := Some(2023)

val scala2_12 = "2.12.21"
val scala2_13 = "2.13.18"
val scala3 = "3.8.3"

// Scala 3.8 (and the Scala 3 artifacts of Scala.js) require JDK 17
ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("17"))

lazy val root =
  project
    .in(file("."))
    .aggregate(importmap2_12, importmap2_13, importmap3, sbtPlugin)
    .enablePlugins(NoPublishPlugin)

lazy val importmap = projectMatrix
  .in(file("importmap"))
  .settings(
    name := "scalajs-importmap",
    libraryDependencies ++= Seq(
      "org.scala-js" %% "scalajs-linker-interface" % scalaJSVersion,
    ),
    tlVersionIntroduced := Map("2.13" -> "0.1.1", "3" -> "0.1.2"),
  )
  .defaultAxes(VirtualAxis.jvm)
  .jvmPlatform(scalaVersions = Seq(scala2_12, scala2_13, scala3))

lazy val importmap2_12 = importmap.jvm(scala2_12)
lazy val importmap2_13 = importmap.jvm(scala2_13)
lazy val importmap3 = importmap.jvm(scala3)

lazy val sbtPlugin = project
  .in(file("sbt-plugin"))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-scalajs-importmap",
    addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion),
  )
  .dependsOn(importmap2_12)
