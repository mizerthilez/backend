package dev.ironduck.backend
package features.guest

import cats.effect.*

import dto.GuestDto

object GuestRepository: // This layer is not important. It's an in-memory table for the example to work.
  def insert(dto: GuestDto): IO[GuestModel] = guestsTable.modify: table =>
    val id = table.length
    val guest = GuestModel.buildFromDto(id, dto)
    (table :+ guest, guest) // (Updated table, Returned class)

  def list(): IO[Vector[GuestModel]] = guestsTable.get

  private val guestsTable = Ref.unsafe[IO, Vector[GuestModel]](Vector.empty[GuestModel])

  // private val guestsTable: Ref[IO, Vector[GuestModel]] =
  // Ref[IO].of(Vector.empty[GuestModel]).unsafeRunSync() // A concurrent safe in memory table
