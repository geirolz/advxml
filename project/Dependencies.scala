import sbt.{CrossVersion, _}

/**
  * Advxml
  * Created by geirolad on 30/07/2019.
  *
  * @author geirolad
  */
object Dependencies {

  lazy val all: Seq[ModuleID] = Seq(
    //SCALA
    "org.typelevel" %% "cats-core" % "2.0.0" cross CrossVersion.binary,
    "org.scalactic" %% "scalactic" % "3.0.8" cross CrossVersion.binary,
    //XML
    "org.scala-lang.modules" %% "scala-xml" % "1.2.0" cross CrossVersion.binary,
    //TEST
    "org.scalatest" %% "scalatest" % "3.0.8" % Test cross CrossVersion.binary,
    "org.scalatest" %% "scalatest" % "3.0.8" % Test cross CrossVersion.binary,
    "org.scalacheck" %% "scalacheck" % "1.14.3" % Test cross CrossVersion.binary
  )
}
