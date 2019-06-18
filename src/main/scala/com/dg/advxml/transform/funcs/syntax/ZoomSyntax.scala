package com.dg.advxml.transform.funcs.syntax

import com.dg.advxml.transform.funcs.XmlZoom

import scala.xml.NodeSeq

private [funcs] trait ZoomSyntax{
  def zoom(f: NodeSeq => NodeSeq): XmlZoom = f(_)

  implicit class XmlZoomOps(z: XmlZoom){
    def \(that: XmlZoom) : XmlZoom = z.andThen(that)
  }
}