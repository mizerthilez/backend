package dev.ironduck.backend
package features.pet

import cats.effect.IO

import dto.PetDto
import dto.PetDto.Dog
import shared.BackendException.BadRequestException

object PetService:
  def petContest(dto: Vector[PetDto]): IO[PetDto] = for
    _ <- IO.raiseWhen(dto.isEmpty)(BadRequestException("Where are the pets?! 😡"))
    pets = dto.sortWith:
      case (pet0: Dog, pet1: Dog) => pet0.age < pet1.age
      case (_: Dog, _) => true
      case _ => false
  yield pets.head
