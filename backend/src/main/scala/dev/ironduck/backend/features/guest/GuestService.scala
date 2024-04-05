package dev.ironduck.backend
package features.guest

import cats.effect.IO

import dto.GuestDto
import shared.BackendException.BadRequestException

object GuestService:
  def letEnterAdultGuest(dto: GuestDto): IO[GuestModel] = for
    _ <- IO.raiseUnless(dto.age >= 18)(
      BadRequestException("You are not an adult!") // Exception of "BadRequestException" raised
    )
    guest <- GuestRepository.insert(dto)
  yield guest

  def listGuests(): IO[Vector[GuestModel]] = GuestRepository.list()
