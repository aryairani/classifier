val breeze = libraryDependencies ++= Seq(
  // other dependencies here
  "org.scalanlp" %% "breeze" % "0.11.2",
  // native libraries are not included by default. add this if you want them (as of 0.7)
  // native libraries greatly improve performance, but increase jar sizes.
  "org.scalanlp" %% "breeze-natives" % "0.11.2"
)

lazy val commonScalacOptions = Seq(
  "-deprecation",
  "-encoding", "UTF-8",       // yes, this is 2 args
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
//  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",        // N.B. doesn't work well with the ??? hole
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture"
)

lazy val warnUnusedImport = Seq(
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 10)) =>
        Seq()
      case Some((2, n)) if n >= 11 =>
        Seq("-Ywarn-unused-import")
    }
  },
  scalacOptions in (Compile, console) ~= {_.filterNot("-Ywarn-unused-import" == _)},
  scalacOptions in (Test, console) <<= (scalacOptions in (Compile, console))
)

def scalaz(module: String) = "org.scalaz" %% s"scalaz-$module" % "7.1.3"
val scalazStream = "org.scalaz.stream" %% "scalaz-stream" % "0.8"
val scalatest = "org.scalatest" %% "scalatest" % "2.2.4" % "test"
val scalacheck = "org.scalacheck" %% "scalacheck" % "1.12.5" % "test"

val kdtree = "com.thesamet" %% "kdtree" % "1.0.3"

def lib(m: ModuleID) = libraryDependencies += m

val commonSettings = Seq(
  organization := "net.arya.cs231n",
  scalaVersion := "2.11.7",
  scalacOptions ++= commonScalacOptions
) ++ warnUnusedImport

lazy val root = project.in(file("."))
  .settings(commonSettings: _*)
  .aggregate(core,impl,dataset,util,example)
  .dependsOn(core,impl,dataset,util,example)

lazy val core = project
  .settings(commonSettings: _*)
  .settings(lib(scalaz("core")), lib(scalazStream))

lazy val dataset = project
  .settings(commonSettings: _*)
  .dependsOn(core, util)
  .settings(lib("net.arya.cs231n" % "dataset-files" % "0.1-SNAPSHOT"))
  .settings(lib(scalazStream), lib(kdtree % "compile"))

lazy val `dataset-files` = project.settings(commonSettings: _*).settings(
  publishArtifact in packageDoc := false,
  publishArtifact in packageSrc := false,
  autoScalaLibrary := false,
  crossPaths := false
)

lazy val impl = project
  .settings(commonSettings: _*)
  .dependsOn(core)
  .settings(lib(kdtree), lib(scalatest), breeze, lib(scalacheck))

lazy val util = project
  .settings(commonSettings: _*)
  .settings(lib(scalaz("effect")))

lazy val example = project
  .settings(commonSettings: _*)
  .dependsOn(core,impl,dataset)
