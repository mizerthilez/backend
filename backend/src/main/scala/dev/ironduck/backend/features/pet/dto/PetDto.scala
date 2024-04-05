package dev.ironduck.backend
package features.pet.dto

import io.circe.derivation.*

object PetDto:
  given Configuration = Configuration.default.withDiscriminator("type")

enum PetDto derives ConfiguredCodec:
  case Dog(name: String, age: Int)
  case Cat(name: String, age: Int)
  case Fish(name: String, age: Int)
  case Bird(name: String, age: Int)
