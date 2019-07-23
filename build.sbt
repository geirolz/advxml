inThisBuild(
  List(
    organization := "com.github.geirolz",
    homepage := Some(url("https://github.com/geirolz/advxml")),
    licenses := List(
        "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
      ),
    developers := List(
        Developer(
          "DavidGeirola",
          "David Geirola",
          "david.geirola@gmail.com",
          url("https://github.com/geirolz")
        )
      )
  )
)

lazy val global = project
  .in(file("."))
  .settings(settings ++ compilePlugins)

lazy val settings = Seq(
  name := "Advxml",
  crossScalaVersions := List("2.12.8", "2.13.0"),
  coverageEnabled.in(Test, test) := true,
  libraryDependencies ++= libsSettings,
  scalacOptions ++= scalacSettings(scalaVersion.value),
  scalacOptions in (Compile, console) --= Seq(
      "-Ywarn-unused:imports",
      "-Xfatal-warnings"
    )
)
lazy val compilePlugins = Seq(
  addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3")
)
lazy val libsSettings = Seq(
  //SCALA
  "org.typelevel" %% "cats-core" % "2.0.0-M4" cross CrossVersion.binary,
  //XML
  "org.scala-lang.modules" %% "scala-xml" % "1.2.0" cross CrossVersion.binary,
  //TEST
  "org.scalatest" %% "scalatest" % "3.0.8" % Test cross CrossVersion.binary,
  "org.scalatest" %% "scalatest" % "3.0.8" % Test cross CrossVersion.binary,
  "org.scalacheck" %% "scalacheck" % "1.14.0" % Test cross CrossVersion.binary
)

def scalacSettings(scalaVersion: String) =
  Seq(
    "-deprecation", // Emit warning and location for usages of deprecated APIs.
    "-encoding",
    "utf-8", // Specify character encoding used by source files.
    "-explaintypes", // Explain type errors in more detail.
    "-feature", // Emit warning and location for usages of features that should be imported explicitly.
    "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
    "-language:experimental.macros", // Allow macro definition (besides implementation and application)
    "-language:higherKinds", // Allow higher-kinded types
    "-language:implicitConversions", // Allow definition of implicit functions called views
    "-unchecked", // Enable additional warnings where generated code depends on assumptions.
    "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
    "-Xfatal-warnings", // Fail the compilation if there are any warnings.
    "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
    "-Xlint:constant", // Evaluation of a constant arithmetic expression results in an error.
    "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
    "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
    "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
    "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
    "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
    "-Xlint:nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
    "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
    "-Xlint:option-implicit", // Option.apply used implicit view.
    "-Xlint:package-object-classes", // Class or object defined in package object.
    "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
    "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
    "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
    "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
    "-Ywarn-dead-code", // Warn when dead code is identified.
    "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
    "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
    "-Ywarn-numeric-widen", // Warn when numerics are widened.
    "-Ywarn-value-discard", // Warn when non-Unit expression results are unused.
    "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
    "-Xlint:nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
    "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
    "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
    "-Ywarn-unused:imports", // Warn if an import selector is not referenced.
    "-Ywarn-unused:locals", // Warn if a local definition is unused.
    "-Ywarn-unused:params", // Warn if a value parameter is unused.
    "-Ywarn-unused:patvars", // Warn if a variable bound in a pattern is unused.
    "-Ywarn-unused:privates" // Warn if a private member is unused.
  ) ++ {
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, 12)) =>
        Seq(
          "-Ypartial-unification", // Enable partial unification in type constructor inference
          "-Xlint:unsound-match", // Pattern match may not be typesafe.
          "-Xlint:by-name-right-associative", // By-name parameter of right associative operator.
          "-Yno-adapted-args" // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
        )
      case _ => Nil
    }
  }
