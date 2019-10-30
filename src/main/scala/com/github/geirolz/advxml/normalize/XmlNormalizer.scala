package com.github.geirolz.advxml.normalize

import com.github.geirolz.advxml.normalize.XmlNormalizer.normalizedEquals
import org.scalactic.Equality

import scala.util.Try
import scala.xml.{Elem, Node, NodeSeq, Text}

/**
  * Advxml
  * Created by geirolad on 22/07/2019.
  *
  * @author geirolad
  */
object XmlNormalizer {

  def normalizedEquals(ns1: NodeSeq, ns2: NodeSeq): Boolean =
    XmlNormalizer.normalize(ns1) xml_sameElements XmlNormalizer.normalize(ns2)

  def normalize(ns: NodeSeq): NodeSeq = {

    def trimTextZappingEmpty(node: Node): Seq[Node] =
      node match {
        case Text(text) if text.trim.isEmpty => Nil
        case Text(text)                      => List(Text(text.trim))
        case Elem(pre, lab, md, scp, children @ _*) =>
          Elem(pre, lab, md, scp, false, children.flatMap(trimTextZappingEmpty): _*)
        case _ => List(node)
      }

    def mergeTextNode(children: Seq[Node]): Seq[Node] =
      children.foldRight(List.empty[Node]) { (ele, acc) =>
        ele match {
          case eleTxt: Text =>
            acc.headOption match {
              case Some(accTxt: Text) => Text(accTxt.text + eleTxt.text) :: acc.tail
              case _                  => ele :: acc
            }
          case _ => ele :: acc
        }
      }

    ns flatMap {
      case el: Elem =>
        List(
          el.copy(
            child = normalize(mergeTextNode(el.child).flatMap(trimTextZappingEmpty)),
            minimizeEmpty = true
          )
        )
      case text: Text => trimTextZappingEmpty(text)
      case x          => List(x)
    }
  }
}

private[advxml] trait XmlNormalizerInstances {
  implicit def streamlinedXmlNormalizedEquality[T <: NodeSeq]: Equality[T] = (a: T, b: Any) => {
    Try(normalizedEquals(a, b.asInstanceOf[NodeSeq])).getOrElse(false)
  }
}

private[advxml] trait XmlNormalizerSyntax {

  implicit class NodeSeqNormalizationAndEqualityOps(ns: NodeSeq) {

    def normalize: NodeSeq =
      XmlNormalizer.normalize(ns)

    def normalizedEquals(ns2: NodeSeq): Boolean =
      XmlNormalizer.normalizedEquals(ns, ns2)

    def |==|(ns2: NodeSeq): Boolean =
      XmlNormalizer.normalizedEquals(ns, ns2)

    def |!=|(ns2: NodeSeq): Boolean =
      !XmlNormalizer.normalizedEquals(ns, ns2)
  }
}
