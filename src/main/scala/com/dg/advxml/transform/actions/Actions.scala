package com.dg.advxml.transform.actions


import scala.xml._

sealed trait XmlAction extends (NodeSeq => NodeSeq){
	def andThen(that: NodeSeq => NodeSeq) : XmlAction = new AbstractAction(xml => that(this(xml))){}
}

private [transform] abstract class AbstractAction(f: NodeSeq => NodeSeq) extends XmlAction{
	override def apply(v1: NodeSeq): NodeSeq = f(v1)
}

private [transform] trait Actions {

	case class Append(ns: NodeSeq) extends AbstractAction(seq => seq.flatMap{
		case elem: Elem => elem.copy(child = elem.child ++ ns)
		case g: Group => g.copy(nodes = g.nodes ++ ns)
		case other => other
	})

	case class Replace(ns: NodeSeq) extends AbstractAction(_ => ns)

	case object Remove extends AbstractAction(_ => Seq.empty)

	case class SetAttrs(values: (String, String)*) extends AbstractAction(seq => seq.flatMap {
		case elem: Elem =>
			elem.copy(attributes = values.foldRight(elem.attributes)((value, metadata) =>
				new UnprefixedAttribute(value._1, Text(value._2), metadata)))
		case other => other
	})

	case class RemoveAttrs(keys: String*) extends AbstractAction(seq => seq.flatMap {
		case elem: Elem =>
			elem.copy(attributes = elem.attributes.filter(attr => keys.contains(attr.key)))
		case other => other
	})
}

object Actions extends Actions