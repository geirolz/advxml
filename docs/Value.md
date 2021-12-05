# Value

Given

```scala
import advxml.data.*
import advxml.implicits.*
```

### Key
Key is a case class that wrap a String instance and is used to describe the key concept. 
It exists to avoid unnamed string.

We can create it directly or using StringContext syntax ops.

```scala
val key: Key = Key("key")
// key: Key = Key(value = "key")
val keySyntax: Key = k"key"
// keySyntax: Key = Key(value = "key")
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
import scala.util.Try

val value: SimpleValue = v"TEST" // == Value("TEST") // == Value("TEST")
// value: SimpleValue = "TEST" // == Value("TEST") // == Value("TEST")
val valueUnboxed: String = value.get // == "TEST"
// valueUnboxed: String = TEST // == "TEST"
val valueExtracted: Try[String] = value.extract[Try] // == always Success("TEST") = Applicative[Try].pure(get)
// valueExtracted: Try[String] = Success(TEST) // == always Success("TEST") = Applicative[Try].pure(get)
val validatedValue: ValidatedValue = value.nonEmpty
  .validate(ValidationRule("MyCustomRule")(validator = _ == "TEST", errorReason = "Not equals to test")) // == always Success("TEST") = Applicative[Try].pure(get)
// validatedValue: ValidatedValue = "TEST" // == always Success("TEST") = Applicative[Try].pure(get)
val validatedValueTry: Try[String] = validatedValue.extract[Try] //Success("TEST")
// validatedValueTry: Try[String] = Success(TEST) //Success("TEST")

val validatedValueValidated: ValidatedNelThrow[String] = validatedValue.validated //Valid("TEST")
// validatedValueValidated: ValidatedNelThrow[String] = Valid(TEST)
```

### AttributeData
`AttributeData` is a case class to bind a `Key` and a `SimpleValue` instance. It's used in advxml to contains write information
for attributes.
Advxml provides a fluent syntax to define an `AttributeData` instance.

```scala
val data: AttributeData = AttributeData(Key("key"), SimpleValue("value"))
// data: AttributeData = AttributeData(
//   key = Key(value = "key"),
//   value = SimpleValue(data = "value", ref = None)
// )
val dataWithSyntax: AttributeData = k"key" := "value"
// dataWithSyntax: AttributeData = AttributeData(
//   key = Key(value = "key"),
//   value = SimpleValue(data = "value", ref = None)
// )
```

### Key-Value predicate
`KeyValuePredicate` is a case class to bind a `Key` and a predicate function `Value => Boolean` related to value.
We can create it directly or using fluent syntax.

```scala
val p: KeyValuePredicate = KeyValuePredicate(Key("key"), _.get == "value")
// p: KeyValuePredicate = KeyValuePredicate(
//   key = Key(value = "key"),
//   valuePredicate = <function1>
// )
val pWithSyntax: KeyValuePredicate = k"key" -> (_.get == "value")
// pWithSyntax: KeyValuePredicate = KeyValuePredicate(
//   key = Key(value = "key"),
//   valuePredicate = <function1>
// )
```

Moreover, advxml provides multiple syntax method to easily create a `KeyValuePredicate` for the most common predicates.
Each of these methods use an implicit `Converter[Value, Try[T]]` converter to safely convert `SimpleValue` into other compared instance type
and `PartialOrder` from cats for comparisons (except for `===` and `=!=` that use `Eq` from cats).
Please, keep in mind that if conversion fails predicate will result `false`.

```scala
val eq: KeyValuePredicate = k"key" === 1
// eq: KeyValuePredicate = KeyValuePredicate(
//   key = Key(value = "key"),
//   valuePredicate = === [1]
// )
val neq: KeyValuePredicate = k"key" =!= 1
// neq: KeyValuePredicate = KeyValuePredicate(
//   key = Key(value = "key"),
//   valuePredicate = =!= [1]
// )

val le: KeyValuePredicate = k"key" < 1
// le: KeyValuePredicate = KeyValuePredicate(
//   key = Key(value = "key"),
//   valuePredicate = < [1]
// )
val leEq: KeyValuePredicate = k"key" <= 1
// leEq: KeyValuePredicate = KeyValuePredicate(
//   key = Key(value = "key"),
//   valuePredicate = <= [1]
// )
val gt: KeyValuePredicate = k"key" > 1
// gt: KeyValuePredicate = KeyValuePredicate(
//   key = Key(value = "key"),
//   valuePredicate = > [1]
// )
val gtEq: KeyValuePredicate = k"key" >= 1
// gtEq: KeyValuePredicate = KeyValuePredicate(
//   key = Key(value = "key"),
//   valuePredicate = >= [1]
// )
```
