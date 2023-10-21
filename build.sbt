ThisBuild / organization := "com.example"
ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file(".")).settings(
  name := "cats-playground",
  libraryDependencies ++= Seq(
    // "core" module - IO, IOApp, schedulers
    // This pulls in the kernel and std modules automatically.
    "org.typelevel" %% "cats-effect" % "3.3.12",
    // concurrency abstractions and primitives (Concurrent, Sync, Async etc.)
    "org.typelevel" %% "cats-effect-kernel" % "3.3.12",
    // standard "effect" library (Queues, Console, Random etc.)
    "org.typelevel" %% "cats-effect-std" % "3.3.12",
    // fs2
    "co.fs2" %% "fs2-core" % "3.8.0",
    "co.fs2" %% "fs2-io" % "3.8.0",
    // configurations
    "com.github.pureconfig" %% "pureconfig" % "0.17.4",
    "com.github.pureconfig" %% "pureconfig-cats-effect" % "0.17.4",
    // json
    "io.circe" %% "circe-core" % "0.14.6",
    "io.circe" %% "circe-generic" % "0.14.6",
    "io.circe" %% "circe-parser" % "0.14.6",
    // test libraries
    "org.typelevel" %% "cats-effect-testing-specs2" % "1.4.0" % Test,
    "org.typelevel" %% "munit-cats-effect-3" % "1.0.7" % Test
  )
)
