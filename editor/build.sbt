import sbt._
import sbt.Project.projectToRef
import Keys._

// import trafficland.opensource.sbt.plugins._
// seq(StandardPluginSet.plugs : _*)


// resolvers += sbt.Resolver.bintrayRepo("denigma", "denigma-releases") //add resolver


lazy val clients = Seq(scalajsclient)
lazy val scalaV = "2.11.8"


lazy val playserver = (project in file("play")).
  settings(
  scalaVersion := scalaV,
  scalaJSProjects := clients,
  libraryDependencies ++= Seq(
    "com.lihaoyi" %% "scalatags" % "0.6.0"
  )
).enablePlugins(PlayScala, SbtOsgi).
  aggregate(clients.map(projectToRef): _*).
  dependsOn(sharedJvm)



lazy val scalajsclient = (project in file("scalajs")).settings(
  scalaVersion := scalaV,
  persistLauncher := true,
  persistLauncher in Test := false,
  //  sourceMapsDirectories += sharedJs.base / "..",
  unmanagedSourceDirectories in Compile := Seq((scalaSource in Compile).value),
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.8.2",
    // "org.webjars.npm" % "bootstrap" % "3.3.6",
    "org.webjars.npm" % "codemirror" % "5.16.0",
    "org.singlespaced" %%% "scalajs-d3" % "0.3.3",
    "com.github.karasiq" %%% "scalajs-bootstrap" % "1.1.2",
    "com.lihaoyi" %%% "scalatags" % "0.6.0"
  )
).enablePlugins(ScalaJSPlugin, ScalaJSPlay).
  dependsOn(sharedJs)


lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared")).
  settings(scalaVersion := scalaV).
  enablePlugins(SbtOsgi).
  jsConfigure(_ enablePlugins ScalaJSPlay)
//  jsSettings(sourceMapsBase := baseDirectory.value / "..")

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

// loads the Play project at sbt startup
onLoad in Global := (Command.process("project playserver", _: State)) compose (onLoad in Global).value

// for Eclipse users
EclipseKeys.skipParents in ThisBuild := false


fork in run := true
