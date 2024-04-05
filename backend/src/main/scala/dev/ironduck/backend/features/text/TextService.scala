package dev.ironduck.backend
package features.text

import cats.effect.IO
import cats.implicits.*

object TextService:
  def countCharacters(text: String): IO[Int] = text.length().pure[IO]
