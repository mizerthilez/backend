package dev.ironduck.`macro`

import scala.collection.mutable
import scala.reflect.ClassTag

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
class CacheSuite extends munit.FunSuite:
  test("LogMapCache should work"):
    val cache = LogMapCache[String, Int]("s1")

    assertEquals(cache.put("a", 1), None)
    assertEquals(cache.get("b"), None)
    assertEquals(cache.put("a", 2), Some(1))
    assertEquals(cache.get("a"), Some(2))

    assert(
      LogMapCache.log.containsSlice(
        List(
          "Create LogMapCache[java.lang.String, Int](name=\"s1\")",
          "LogMapCache[java.lang.String, Int](name=\"s1\").put(a, 1)=None",
          "LogMapCache[java.lang.String, Int](name=\"s1\").get(b)=None",
          "LogMapCache[java.lang.String, Int](name=\"s1\").put(a, 2)=Some(1)",
          "LogMapCache[java.lang.String, Int](name=\"s1\").get(a)=Some(2)",
        )
      )
    )

  import CacheSuite.given

  test("@cache should work on no-arity function"):
    @cached
    def noArity() = 255 * 3

    assertEquals(noArity(), 255 * 3)
    assertEquals(noArity(), 255 * 3)
    assert(
      LogMapCache.log.containsSlice(
        List(
          "Create LogMapCache[scala.Tuple$package$EmptyTuple$, Int](name=\"noArityCache$macro$1\")",
          "LogMapCache[scala.Tuple$package$EmptyTuple$, Int](name=\"noArityCache$macro$1\").get(())=None",
          "LogMapCache[scala.Tuple$package$EmptyTuple$, Int](name=\"noArityCache$macro$1\").put((), 765)=None",
          "LogMapCache[scala.Tuple$package$EmptyTuple$, Int](name=\"noArityCache$macro$1\").get(())=Some(765)",
        )
      )
    )

  test("@cache should work on one-arity function"):
    @cached
    def oneArity(x: Int): Int = x * 2

    assertEquals(oneArity(2), 4)
    assertEquals(oneArity(2), 4)
    assert(
      LogMapCache.log.containsSlice(
        List(
          "Create LogMapCache[scala.Tuple1, Int](name=\"oneArityCache$macro$1\")",
          "LogMapCache[scala.Tuple1, Int](name=\"oneArityCache$macro$1\").get((2))=None",
          "LogMapCache[scala.Tuple1, Int](name=\"oneArityCache$macro$1\").put((2), 4)=None",
          "LogMapCache[scala.Tuple1, Int](name=\"oneArityCache$macro$1\").get((2))=Some(4)",
        )
      )
    )

  test("@cache should work on two-arity function"):
    @cached
    def twoArity(x: Int, y: Int): Int = x * y

    assertEquals(twoArity(1, 2), 2)
    assertEquals(twoArity(1, 2), 2)
    assert(
      LogMapCache.log.containsSlice(
        List(
          "Create LogMapCache[scala.Tuple2, Int](name=\"twoArityCache$macro$1\")",
          "LogMapCache[scala.Tuple2, Int](name=\"twoArityCache$macro$1\").get((1,2))=None",
          "LogMapCache[scala.Tuple2, Int](name=\"twoArityCache$macro$1\").put((1,2), 2)=None",
          "LogMapCache[scala.Tuple2, Int](name=\"twoArityCache$macro$1\").get((1,2))=Some(2)",
        )
      )
    )

  test("@cache should work on three-arity function"):
    @cached
    def threeArity(
      x: Int,
      y: Int,
      z: Int,
    ) = x + y + z

    assertEquals(threeArity(3, 2, 1), 6)
    assertEquals(threeArity(3, 2, 1), 6)
    assert(
      LogMapCache.log.containsSlice(
        List(
          "Create LogMapCache[scala.Tuple3, Int](name=\"threeArityCache$macro$1\")",
          "LogMapCache[scala.Tuple3, Int](name=\"threeArityCache$macro$1\").get((3,2,1))=None",
          "LogMapCache[scala.Tuple3, Int](name=\"threeArityCache$macro$1\").put((3,2,1), 6)=None",
          "LogMapCache[scala.Tuple3, Int](name=\"threeArityCache$macro$1\").get((3,2,1))=Some(6)",
        )
      )
    )
    LogMapCache.log.foreach(println)

object CacheSuite:
  given CacheFactory with
    def apply[K: ClassTag, V: ClassTag](name: String): Cache[K, V] =
      LogMapCache(name)
