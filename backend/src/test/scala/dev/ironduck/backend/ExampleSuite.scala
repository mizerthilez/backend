package dev.ironduck
package backend

import com.eed3si9n.expecty.Expecty.expect as exp

final class ExampleSuite extends TestSuite:
  test("hello world"):
    forAll: (int: Int, string: String) =>
      expectEquals(int, int)
      expectEquals(string, string)

  test("not equal"):
    exp(5 != 3, 2 != 1, 3 != 4)
