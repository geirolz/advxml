
lazy val global = project
  .in(file("."))
  .settings(settings)

lazy val settings = Seq(
  name := "Adxml",
  organization := "com.dg",
  scalaVersion := "2.12.0",
  version := "0.0.1",
  scalacOptions ++= scalacSettings,
  resolvers ++= resolversSettings,
  libraryDependencies ++= libsSettings,
  addSbtPlugin("com.artima.supersafe" % "sbtplugin" % "1.1.3")  
)

lazy val scalacSettings = Seq(
  "-encoding",
  "UTF-8",
  "-deprecation",
  "-unchecked",
  "-feature",
  "-explaintypes",
  "-opt-warnings",
  "-language:existentials",
  "-language:higherKinds",
  "-Ypartial-unification",
  "-Yrangepos",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Ywarn-extra-implicit",
  "-Ywarn-inaccessible",
  "-Ywarn-infer-any",
  "-Ywarn-nullary-override",
  "-Ywarn-nullary-unit",
  "-Ywarn-unused:_,-imports",
  "-Xsource:2.13",
  "-Xlint:_,-type-parameter-shadow",
  "-Xfuture",
  "-Xfatal-warnings"
)

lazy val resolversSettings = Seq(
  Resolver.sonatypeRepo("public"),
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases"),
  "Maven repo1" at "http://repo1.maven.org/",
  "Maven repo2" at "http://mvnrepository.com/artifact",
  "Artima Maven Repository" at "http://repo.artima.com/releases"
)

lazy val libsSettings = Seq(
  "org.scala-lang.modules" %% "scala-xml" % "1.2.0",

  //TEST
  "org.scalactic" %% "scalactic" % "3.0.7",
  "org.scalatest" %% "scalatest" % "3.0.7" % "test"
)