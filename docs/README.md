# Advxml
[![Build Status](https://github.com/geirolz/advxml/actions/workflows/cicd.yml/badge.svg)](https://github.com/geirolz/advxml/actions)
[![codecov](https://img.shields.io/codecov/c/github/geirolz/advxml)](https://codecov.io/gh/geirolz/advxml)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/db3274b55e0c4031803afb45f58d4413)](https://www.codacy.com/manual/david.geirola/advxml?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=geirolz/advxml&amp;utm_campaign=Badge_Grade)
[![Sonatype Nexus (Releases)](https://img.shields.io/nexus/r/com.github.geirolz/advxml-core_2.13?server=https%3A%2F%2Foss.sonatype.org)](https://mvnrepository.com/artifact/com.github.geirolz/advxml-core)
[![javadoc.io](https://javadoc.io/badge2/com.github.geirolz/advxml-core_2.13/javadoc.io.svg)](https://javadoc.io/doc/com.github.geirolz/advxml-core_2.13)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)
[![GitHub license](https://img.shields.io/github/license/geirolz/advxml)](https://github.com/geirolz/advxml/blob/master/LICENSE)

A lightweight, simple and functional library DSL to work with XML in Scala using native scala xml library and cats core.
 
## How to import

Advxml supports 2.13 and 3

**Sbt**
```sbt mdoc
  libraryDependencies ++= Seq(
    "com.github.geirolz" %% "advxml-core" % "2.5.0",
    "com.github.geirolz" %% "advxml-xpath" % "2.5.0" //optional, for xpath support
  )
```

## Structure
The idea behind this library is offer a fluent syntax to edit and read xml.

*Features:*
- [data/Value](docs/Value.md) Data types for value, keys and attributes.
- [data/Convert](docs/Convert.md) Allow instances conversion (not automatically yet)
- [transform/Transform](docs/Transform.md) Allows to edit the XML document.
- [transform/Zoom](docs/Zoom.md) Allows to traverse an XML do get nodes, attributes and text.
- [transform/Normalize](docs/Normalize.md) Allows normalizing xml docs, removing white spaces and collapse empty nodes

 
 ## Contributors
 - [@pawelkaczor](https://github.com/pawelkaczor)
 - [@dcsobral](https://github.com/dcsobral)
 - [@liff](https://github.com/liff)
 - [@argast](https://github.com/argast)
 - [@danslapman](https://github.com/argast)
