package dev.ironduck.`macro`

import scala.collection.mutable
import scala.quoted.Quotes
import scala.quoted.Expr
import scala.reflect.ClassTag

trait Cache[K, V]:
  def put(key: K, vale: V): Option[V]
  def get(key: K): Option[V]

trait CacheFactory:
  def apply[K: ClassTag, V: ClassTag](name: String): Cache[K, V]

class MapCache[K, V] extends Cache[K, V]:
  private val map = mutable.Map.empty[K, V]
  override def put(key: K, value: V): Option[V] = map.put(key, value)

  override def get(key: K): Option[V] = map.get(key)

@experimental
class cached extends MacroAnnotation:
  override def transform(
    using quotes: Quotes
  )(
    tree: quotes.reflect.Definition,
    companion: Option[quotes.reflect.Definition],
  ): List[quotes.reflect.Definition] =
    import quotes.reflect.*

    tree match
      case DefDef(name, params, returnType, Some(rhs)) =>
        val paramRefs = params
          .flatMap(_.params)
          .map:
            case vdef: ValDef => Ref(vdef.symbol).asExpr
        val paramTuple = Expr.ofTupleFromSeq(paramRefs)

        val paramTupleTypeRepr = paramTuple match
          case '{ $p: parTupleType } =>
            TypeRepr.of[parTupleType] match
              case t: AppliedType =>
                val args = t.typeArgs.map(_.widen)
                t.tycon.appliedTo(args)
              case _: TypeRepr =>
                TypeRepr.of[EmptyTuple]

        (paramTupleTypeRepr.asType, returnType.tpe.asType) match
          case ('[paramTupleType], '[rhsType]) =>
            val cacheName = Symbol.freshName(name + "Cache")
            val cacheType = TypeRepr.of[Cache[paramTupleType, rhsType]]

            val cacheRhs = Expr.summon[CacheFactory] match
              case Some(cacheFactory) =>
                val ktag = Expr.summon[ClassTag[paramTupleType]].get
                val vtag = Expr.summon[ClassTag[rhsType]].get
                '{
                  $cacheFactory
                    .apply[paramTupleType, rhsType](${ Expr(cacheName) })(using $ktag, $vtag)
                }.asTerm
              case None =>
                '{ MapCache[paramTupleType, rhsType] }.asTerm

            val cacheSymbol = Symbol.newVal(
              tree.symbol.owner,
              cacheName,
              cacheType,
              Flags.Private,
              Symbol.noSymbol,
            )
            val cache = ValDef(cacheSymbol, Some(cacheRhs))
            val cacheRef = Ref(cacheSymbol).asExprOf[Cache[paramTupleType, rhsType]]

            def buildNewRhs(using q: Quotes) =
              '{
                val key = ${ paramTuple.asExprOf[paramTupleType] }
                $cacheRef.get(key) match
                  case Some(value) =>
                    value
                  case None =>
                    val result = ${ rhs.asExprOf[rhsType] }
                    $cacheRef.put(key, result)
                    result
              }

            val newRhs = buildNewRhs(using tree.symbol.asQuotes).asTerm
            val expandedMethod = DefDef.copy(tree)(name, params, returnType, Some(newRhs))

            List(cache, expandedMethod)
      case _ =>
        report.error("Annottee must be a method")
        List(tree)
