import Dependencies.{ io, _ }

ThisBuild / organization := "dev.ironduck"
ThisBuild / scalaVersion := "3.4.0"

lazy val root =
  project
    .in(file("."))
    .aggregate(backend, `macro`)

lazy val backend =
  project
    .in(file("backend"))
    .settings(name := "backend")
    .settings(commonSettings)
    .settings(autoImportSettings)
    .settings(libraryDependencies ++= dep_backend)
    .settings(libraryDependencies ++= dep_munit_test)
    .settings(Compile / run / fork := true)

lazy val `macro` =
  project
    .in(file("macro"))
    .settings(name := "macro")
    .settings(commonSettings)
    .settings(autoImportSettings)
    .settings(libraryDependencies ++= dep_munit_test)
    .settings(scalacOptions += "-Xcheck-macros")

lazy val commonSettings = {
  lazy val commonScalacOptions = Seq(
    Compile / console / scalacOptions := {
      (Compile / console / scalacOptions).value
        .filterNot(_.contains("wartremover"))
        .filterNot(Scalac.Lint.toSet)
        .filterNot(Scalac.FatalWarnings.toSet) :+ "-Wconf:any:silent"
    },
    Test / console / scalacOptions :=
      (Compile / console / scalacOptions).value,
  )

  lazy val otherCommonSettings = Seq(
    update / evictionWarningOptions := EvictionWarningOptions.empty
    // cs launch scalac:3.3.1 -- -Wconf:help
    // src is not yet available for Scala3
    // scalacOptions += s"-Wconf:src=${target.value}/.*:s",
  )

  Seq(
    commonScalacOptions,
    otherCommonSettings,
  ).reduceLeft(_ ++ _)
}

lazy val autoImportSettings = Seq(
  scalacOptions +=
    Seq(
      "java.lang",
      "scala",
      "scala.Predef",
      "scala.annotation",
      "scala.util.chaining",
    ).mkString(start = "-Yimports:", sep = ",", end = ""),
  Test / scalacOptions +=
    Seq(
      "org.scalacheck",
      "org.scalacheck.Prop",
    ).mkString(start = "-Yimports:", sep = ",", end = ""),
)

val dep_backend = Seq(
  org.typelevel.`cats-effect`,
  dev.optics.`monocle-core`,
  // Tapir
  com.softwaremill.sttp.tapir.`tapir-http4s-server`,
  com.softwaremill.sttp.tapir.`tapir-json-circe`,
  com.softwaremill.sttp.tapir.`tapir-swagger-ui-bundle`,
  // Http4s
  org.http4s.`http4s-ember-server`,
  org.http4s.`http4s-circe`,
  org.http4s.`http4s-dsl`,
  org.http4s.`http4s-ember-client`,
  // Circe
  io.circe.`circe-parser`,
  io.circe.`circe-generic`,
  io.circe.`circe-literal`,
)

val dep_munit_test = Seq(
  com.eed3si9n.expecty.expecty,
  org.scalacheck.scalacheck,
  org.scalameta.`munit-scalacheck`,
  org.scalameta.munit,
  org.typelevel.`discipline-munit`,
).map(_ % Test)
