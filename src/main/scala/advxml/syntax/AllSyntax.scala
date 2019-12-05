package advxml.syntax

private[advxml] trait AllSyntax
    extends XmlTransformerSyntax
    with XmlTraverserAbstractSyntax
    with XmlNormalizerSyntax
    with ConvertersSyntax
    with ValidationSyntax
    with PredicateSyntax
