val breeze = libraryDependencies ++= Seq(
  // other dependencies here
  "org.scalanlp" %% "breeze" % "0.11.2",
  // native libraries are not included by default. add this if you want them (as of 0.7)
  // native libraries greatly improve performance, but increase jar sizes.
  "org.scalanlp" %% "breeze-natives" % "0.11.2"
)

//resolvers += "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/"



scalaVersion in ThisBuild := "2.11.6"

scalacOptions in ThisBuild ++= Seq(
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
  "-Xfuture",
  "-Ywarn-unused-import"     // 2.11 only
)

resolvers += "Scalaz Bintray Repo" at "https://dl.bintray.com/scalaz/releases"

def scalaz(module: String) = "org.scalaz" %% s"scalaz-$module" % "7.1.3"
val scalazStream = "org.scalaz.stream" %% "scalaz-stream" % "0.7.1a"
val scalatest = "org.scalatest" %% "scalatest" % "2.2.4" % "test"

val kdtree = "com.thesamet" %% "kdtree" % "1.0.3"

def lib(m: ModuleID) = libraryDependencies += m

lazy val root = project.in(file(".")).aggregate(core,dataset,impl,util,example)

lazy val core = project
  .settings(lib(scalaz("core")))

lazy val dataset = project
  .dependsOn(core, util)
  .settings(lib(scalazStream), lib(kdtree % "compile"))

lazy val impl = project
  .dependsOn(core)
  .settings(lib(kdtree))

lazy val util = project.settings(lib(scalaz("effect")))

lazy val example = project
  .dependsOn(core, dataset, impl)
