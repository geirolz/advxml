# Advxml

[![Build Status](https://img.shields.io/travis/geirolz/advxml/master)](https://travis-ci.org/geirolz/advxml)
[![codecov](https://img.shields.io/codecov/c/github/geirolz/advxml)](https://codecov.io/gh/geirolz/advxml)
[![Sonatype Nexus (Releases)](https://img.shields.io/nexus/r/com.github.geirolz/advxml_2.13?server=https%3A%2F%2Foss.sonatype.org)](https://mvnrepository.com/artifact/com.github.geirolz/advxml)
[![javadoc.io](https://javadoc.io/badge2/com.github.geirolz/advxml_2.13/javadoc.io.svg)](https://javadoc.io/doc/com.github.geirolz/advxml_2.13)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)
[![GitHub license](https://img.shields.io/github/license/geirolz/advxml)](https://github.com/geirolz/advxml/blob/master/LICENSE)

A lightweight, simple and functional library to work with XML in Scala using native scala xml library and cats core.
 
## How to import

Supported Scala 2.12 and 2.13

**Maven** for 2.12
```xml
<dependency>
    <groupId>com.github.geirolz</groupId>
    <artifactId>advxml_2.12</artifactId>
    <version>version</version>
</dependency>
```

**Maven** for 2.13
```xml
<dependency>
    <groupId>com.github.geirolz</groupId>
    <artifactId>advxml_2.13</artifactId>
    <version>version</version>
</dependency>
```

**Sbt**
```sbt
  libraryDependencies += "com.github.geirolz" %% "advxml" % version
```

## Structure
The idea behind this library is offer a fluent syntax to edit and read xml.

*Features:*
- [Transformation](docs/Transform.md) Allows to edit the XML document.
- [Traverse](docs/Traverse.md) read node/attributes mandatory or optional, based on Cats [ValidatedNel](https://typelevel.org/cats/datatypes/validated.html))
- [Convert](docs/Convert.md) to Model and vice versa
- [Normalize](docs/Normalize.md) remove white spaces and collapse empty nodes

 
