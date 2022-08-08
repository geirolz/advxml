import sbt.{CrossVersion, _}

/** Advxml Created by geirolad on 30/07/2019.
  *
  * @author
  *   geirolad
  */
object Dependencies {

  lazy val common: Seq[ModuleID] = Seq(
    // SCALA
    "org.typelevel" %% "cats-core" % "2.7.0" cross CrossVersion.binary,
    // XML
    "org.scala-lang.modules" %% "scala-xml" % "2.1.0" cross CrossVersion.binary,
    // TEST
    "org.scalactic"  %% "scalactic"            % "3.2.12" % Test cross CrossVersion.binary,
    "org.typelevel"  %% "discipline-scalatest" % "2.1.5"  % Test,
    "org.typelevel"  %% "cats-laws"            % "2.7.0"  % Test cross CrossVersion.binary,
    "org.scalatest"  %% "scalatest"            % "3.2.13" % Test cross CrossVersion.binary,
    "org.scalacheck" %% "scalacheck"           % "1.15.4" % Test cross CrossVersion.binary
  )

  object XPath {
    lazy val dedicated: Seq[ModuleID] = Seq(parser).flatten

    private val parser: Seq[ModuleID] = Seq(
      "eu.cdevreeze.xpathparser" %% "xpathparser" % "0.8.0"
    )
  }

  object Plugins {
    lazy val compilerPluginsFor2: Seq[ModuleID] = Seq(
      compilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3" cross CrossVersion.binary)
    )
    lazy val compilerPluginsFor3: Seq[ModuleID] = Nil
  }

  lazy val extraDependenciesForScala2_13: Seq[ModuleID] = Nil

  lazy val extraDependenciesForScala3: Seq[ModuleID] = Nil
}
