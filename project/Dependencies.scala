import sbt.{CrossVersion, _}

/** Advxml
  * Created by geirolad on 30/07/2019.
  *
  * @author geirolad
  */
object Dependencies {

  lazy val common: Seq[ModuleID] = Seq(
    //SCALA
    "org.typelevel" %% "cats-core" % "2.6.1" cross CrossVersion.binary,
    //XML
    "org.scala-lang.modules" %% "scala-xml" % "2.0.0" cross CrossVersion.binary,
    //TEST
    "org.scalactic" %% "scalactic" % "3.2.9" % Test cross CrossVersion.binary,
    "org.typelevel" %% "discipline-scalatest" % "2.1.4" % Test,
    "org.typelevel" %% "cats-laws" % "2.6.1" % Test cross CrossVersion.binary,
    "org.scalatest" %% "scalatest" % "3.2.8" % Test cross CrossVersion.binary,
    "org.scalacheck" %% "scalacheck" % "1.15.4" % Test cross CrossVersion.binary
  )
}
