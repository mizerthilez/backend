package dev.ironduck.backend
package features.guest
package dto

import io.circe.Codec.AsObject
import GuestModel.Gender

case class GuestDto( // It corresponds to the input of the endpoint (JSON -> Scala)
  name: String,
  gender: Gender,
  age: Int,
  job: String,
) derives AsObject

@main def main =
  import io.circe.syntax.*
  println(GuestDto("1", Gender.NonBinary, 2, "a").asJson)
