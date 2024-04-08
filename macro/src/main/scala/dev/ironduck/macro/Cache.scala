package dev.ironduck.`macro`

import scala.collection.*
import scala.reflect.ClassTag
import scala.quoted.Quotes
import scala.quoted.Expr

trait Cache[K, V]:
  def put(key: K, vale: V): Option[V]
  def get(key: K): Option[V]

class MapCache[K, V] extends Cache[K, V]:
  private val map = mutable.Map.empty[K, V]
  override def put(key: K, value: V): Option[V] = map.put(key, value)

  override def get(key: K): Option[V] = map.get(key)

class LogMapCache[K, V](n: String)(using ktag: ClassTag[K], vtag: ClassTag[V]) extends Cache[K, V]:
  private val name = s"LogMapCache[$ktag, $vtag](name=\"$n\")"
  private val map = mutable.Map.empty[K, V]

  LogMapCache.log.append(s"Create $name")

  override def put(key: K, value: V): Option[V] =
    val result = map.put(key, value)
    LogMapCache.log.append(s"$name.put($key, $value)=$result")
    result
  override def get(key: K): Option[V] =
    val result = map.get(key)
    LogMapCache.log.append(s"$name.get($key)=$result")
    result

object LogMapCache:
  val log = mutable.Buffer.empty[String]

@experimental
class cached extends MacroAnnotation:
  override def transform(
    using quotes: Quotes
  )(
    tree: quotes.reflect.Definition
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
            val t = TypeRepr.of[parTupleType].asInstanceOf[AppliedType]
            val args = t.typeArgs.map(_.widen)
            t.tycon.appliedTo(args)

        (paramTupleTypeRepr.asType, returnType.tpe.asType) match
          case ('[paramTupleType], '[rhsType]) =>
            val cacheName = Symbol.freshName(name + "Cache")
            val cacheType = TypeRepr.of[Cache[paramTupleType, rhsType]]
            val ktag = Expr.summon[ClassTag[paramTupleType]].get
            val vtag = Expr.summon[ClassTag[rhsType]].get

            val cacheSummoned = Expr.summon[Cache[paramTupleType, rhsType]]
            println(cacheSummoned.map(_.asTerm.show(using Printer.TreeCode)))

            val cacheRhs = '{
              new LogMapCache[paramTupleType, rhsType](${ Expr(cacheName) })(using $ktag, $vtag)
            }.asTerm

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

            println(cache.show(using Printer.TreeCode))
            println(expandedMethod.show(using Printer.TreeCode))
            List(cache, expandedMethod)
      case _ =>
        report.error("Annottee must be a method")
        List(tree)
