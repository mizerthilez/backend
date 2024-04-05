package dev.ironduck.backend
package features.guest

import io.circe.Codec
import io.circe.derivation.*

import GuestModel.Gender
import dto.GuestDto

case class GuestModel( // Returned by the endpoints == "Scala" -> "JSON" (also corresponds to an entity in table)
  id: Long,
  name: String,
  gender: Gender, // A non Scala simple type that needs to be derived manually! (JSON <-> Scala)
  age: Int,
  job: String,
) derives Codec.AsObject

object GuestModel:
  def buildFromDto(id: Long, dto: GuestDto): GuestModel =
    GuestModel(id, dto.name, dto.gender, dto.age, dto.job)

  object Gender:
    given Configuration = Configuration.default.withKebabCaseConstructorNames

  enum Gender derives ConfiguredEnumCodec:
    case Male, Female, NonBinary
