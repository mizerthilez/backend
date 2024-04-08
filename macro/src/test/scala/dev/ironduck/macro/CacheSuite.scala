package dev.ironduck.`macro`

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

  @cached
  def f(x: Int, y: Int): Int = x * y

  test("cache annotation should work"):
    assertEquals(f(1, 2), 2)
    assertEquals(f(1, 2), 2)
    LogMapCache.log.foreach(println)

object CacheSuite:
  given Cache[(Int, Int), Int] = new MapCache[(Int, Int), Int]
