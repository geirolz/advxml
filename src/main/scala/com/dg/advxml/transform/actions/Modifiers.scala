package com.dg.advxml.transform.actions


import scala.xml._

sealed trait XmlModifier{
	def apply(v1: NodeSeq): NodeSeq
	def andThen(that: XmlModifier) : XmlModifier = Modifiers.Generic(xml => that(this(xml)))
}

private [transform] trait Modifiers {

	case class Append(ns: NodeSeq) extends AbstractModifier(seq => seq.flatMap{
		case elem: Elem => elem.copy(child = elem.child ++ ns)
		case g: Group => g.copy(nodes = g.nodes ++ ns)
		case other => other
	})

	case class Replace(ns: NodeSeq) extends AbstractModifier(_ => ns)

	case object Remove extends AbstractModifier(_ => Seq.empty)

	case class SetAttrs(values: (String, String)*) extends AbstractModifier(seq => seq.flatMap {
		case elem: Elem =>
			elem.copy(attributes = values.foldRight(elem.attributes)((value, metadata) =>
				new UnprefixedAttribute(value._1, Text(value._2), metadata)))
		case other => other
	})

	case class RemoveAttrs(keys: String*) extends AbstractModifier(seq => seq.flatMap {
		case elem: Elem =>
			elem.copy(attributes = elem.attributes.filter(attr => keys.contains(attr.key)))
		case other => other
	})

	private [XmlModifier] case class Generic(f: NodeSeq => NodeSeq) extends AbstractModifier(f)

	private abstract class AbstractModifier(f: NodeSeq => NodeSeq) extends XmlModifier{
		override def apply(ns: NodeSeq): NodeSeq = f(ns)
	}
}

object Modifiers extends Modifiers