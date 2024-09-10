package dev.ironduck.`macro`

object AccessAnnotation:
  import scala.quoted.*

  inline def annosFor[T]: List[StaticAnnotation] = ${ annosForImpl[T] }

  private def annosForImpl[T: Type](using Quotes): Expr[List[StaticAnnotation]] =
    import quotes.reflect.*
    val annot = TypeRepr.of[StaticAnnotation]
    val exprs = TypeRepr
      .of[T]
      .typeSymbol
      .annotations
      .collect:
        case term if term.tpe <:< annot => term.asExprOf[StaticAnnotation]

    Expr.ofList(exprs)

  inline def fieldAnnosFor[T]: Map[String, List[StaticAnnotation]] = ${ fieldAnnosForImpl[T] }

  private def fieldAnnosForImpl[T: Type](using q: Quotes): Expr[Map[String, List[StaticAnnotation]]] =
    import quotes.reflect.*
    val annot = TypeRepr.of[StaticAnnotation]
    val tuples: Seq[Expr[(String, List[StaticAnnotation])]] = TypeRepr
      .of[T]
      .typeSymbol
      .primaryConstructor
      .paramSymss
      .flatten
      .map: sym =>
        val fieldNameExpr = Expr(sym.name)
        val exprs = sym.annotations.collect:
          case term if term.tpe <:< annot => term.asExprOf[StaticAnnotation]
        '{ ($fieldNameExpr, ${ Expr.ofList(exprs) }) }

    '{ ${ Expr.ofSeq(tuples) }.toMap }
