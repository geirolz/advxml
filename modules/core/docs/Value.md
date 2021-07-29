# Value

### Key
Key is a case class that wrap a String instance and is used to describe the key concept. 
It exists to avoid unnamed string.

We can create it directly or using StringContext syntax ops.
```scala
import advxml.core.data.Key
import advxml.syntax.data._

val key : Key = Key("key")
val keySyntax : Key = k"key"
```

### Value
`Value` is an ADT used to define a type different to `String` in order to provide data validation.
Possible values are:
- `SimpleValue`
- `ValidatedValue`

`SimpleValue` represent a simple `String` wrapper, you can use `get` to access to the inner value. 
`ValidatedValue` represent also a simple `String` wrapper but, you can access to his data only validating the 
inner value through `extract[F[_]]` method or `validated`(just and alias for `extract[ValidatedNelThrow]`).

All `Value` instances have a method named `validate` that allows to specify a list of `ValidationRule` to validate the wrapped `String` instance, 
this method returns a `ValidatedValue`.

```scala
import advxml.core.data.{SimpleValue, ValidatedNelThrow, ValidatedValue, ValidationRule}
import advxml.syntax.data._

import scala.util.Try

val value: SimpleValue = v"TEST" // == Value("TEST") // == Value("TEST")
val valueUnboxed: String = value.get // == "TEST"
val valueExtracted: Try[String] = value.extract[Try] // == always Success("TEST") = Applicative[Try].pure(get)
val valueExtracted: Try[String] = value.extract[Try]
val validatedValue: ValidatedValue = value.nonEmpty
  .validate(ValidationRule("MyCustomRule")(validator = _ == "TEST", errorReason = "Not equals to test")) // == always Success("TEST") = Applicative[Try].pure(get)
val validatedValueTry: Try[String] = validatedValue.extract[Try] //Success("TEST")

val validatedValueValidated: ValidatedNelThrow[String] = validatedValue.validated//Valid("TEST")
```

### AttributeData
`AttributeData` is a case class to bind a `Key` and a `SimpleValue` instance. It's used in advxml to contains write information
for attributes.
Advxml provides a fluent syntax to define an `AttributeData` instance.

```scala
import advxml.core.data.{Key, SimpleValue, AttributeData}
import advxml.syntax.data._

val data : AttributeData = AttributeData(Key("key"), SimpleValue("value"))
val dataWithSyntax : AttributeData = k"key" := "value"
```

### Key-Value predicate
`KeyValuePredicate` is a case class to bind a `Key` and a predicate function `Value => Boolean` related to value.
We can create it directly or using fluent syntax.

```scala
import advxml.core.data._
import advxml.syntax.data._

val p : KeyValuePredicate = KeyValuePredicate(Key("key"), _.get == "value")
val pWithSyntax : KeyValuePredicate = k"key" -> (_.get == "value")
```

Moreover, advxml provides multiple syntax method to easily create a `KeyValuePredicate` for the most common predicates.
Each of these methods use an implicit `Converter[Value, Try[T]]` converter to safely convert `SimpleValue` into other compared instance type
and `PartialOrder` from cats for comparisons (except for `===` and `=!=` that use `Eq` from cats).
Please, keep in mind that if conversion fails predicate will result `false`.

```scala
import advxml.core.data._
import advxml.syntax.data._

val eq : KeyValuePredicate = k"key" === 1
val neq : KeyValuePredicate = k"key" =!= 1

val le : KeyValuePredicate = k"key" < 1
val leEq : KeyValuePredicate = k"key" <= 1
val gt : KeyValuePredicate = k"key" > 1
val gtEq : KeyValuePredicate = k"key" >= 1
```
