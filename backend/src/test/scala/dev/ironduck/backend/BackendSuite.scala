package dev.ironduck.backend

import cats.effect.IO
import cats.effect.unsafe.implicits.global

import sttp.client3.circe.*
import sttp.client3.HttpError
import sttp.client3.SttpBackend
import sttp.client3.testing.SttpBackendStub
import sttp.client3.{ UriContext, basicRequest }
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.integ.cats.effect.CatsMonadError

import com.eed3si9n.expecty.Expecty.expect

class BackendSuite extends munit.FunSuite:
  extension [T](t: IO[T]) def unwrap: T = t.unsafeRunSync()

  extension [T](t: HttpError[T])
    def matchMessage(msg: String): Boolean =
      t.body.toString().contains(s""""message":"$msg"""")
    def matchType(typ: String): Boolean =
      t.body.toString().contains(s""""type":"$typ"""")

  var backendStub: SttpBackend[IO, Nothing] = scala.compiletime.uninitialized

  override def beforeAll(): Unit =
    backendStub = TapirStubInterpreter(SttpBackendStub(CatsMonadError[IO]()))
      .whenServerEndpointsRunLogic(config.BackendServerConf.apiEndPoints)
      .backend()

  override def afterAll(): Unit =
    backendStub.close().unwrap

  test("count characters"):
    // when
    val response = basicRequest
      .get(uri"http://test.com/count-characters?text=admin")
      .send(backendStub)

    // then
    val value = response.map(_.body).unwrap
    assertEquals(value, Right("5"))

  test("pet contest"):
    // given
    import features.pet.dto.PetDto, PetDto.*
    val pets: Vector[PetDto] = Vector(
      Dog(name = "Max", age = 3),
      Fish(name = "Poissy", age = 2),
      Dog(name = "Doggy", age = 1),
    )

    // when
    import io.circe.syntax.*
    val response = basicRequest
      .body(pets.asJson)
      .post(uri"http://test.com/pets/contest")
      .response(asJson[PetDto])
      .send(backendStub)

    // then
    val value = response.map(_.body).unwrap
    value.foreach(assertEquals(_, Dog(name = "Doggy", age = 1)))

  test("pet contest - empty list"):
    // given
    import features.pet.dto.PetDto
    val pets: Vector[PetDto] = Vector.empty

    // when
    import io.circe.syntax.*
    val response = basicRequest
      .body(pets.asJson)
      .post(uri"http://test.com/pets/contest")
      .response(asJson[PetDto])
      .send(backendStub)

    // then
    val value = response.map(_.body).unwrap
    value.left.foreach:
      case e: HttpError[t] =>
        expect(
          e.statusCode.code == 400,
          e.matchMessage("Where are the pets?! 😡"),
          e.matchType("BadRequestException"),
        )
      case _ =>
        fail("wrong exception")

  test("guests - under 18"):
    // given
    import features.guest.dto.GuestDto
    import features.guest.GuestModel
    import GuestModel.Gender, Gender.*
    val marry = GuestDto("marry", Female, 16, "student")

    // when
    import io.circe.syntax.*
    val response = basicRequest
      .body(marry.asJson)
      .post(uri"http://test.com/guests")
      .response(asJson[GuestModel])
      .send(backendStub)

    // then
    val value = response.map(_.body).unwrap
    value.left.foreach:
      case e: HttpError[t] =>
        expect(
          e.statusCode.code == 400,
          e.matchMessage("You are not an adult!"),
          e.matchType("BadRequestException"),
        )
      case _ =>
        fail("wrong exception")

  test("guests"):
    // given
    import features.guest.dto.GuestDto
    import features.guest.GuestModel
    import GuestModel.Gender, Gender.*
    val marry = GuestDto("marry", Female, 18, "student")

    // when
    import io.circe.syntax.*
    val response = basicRequest
      .body(marry.asJson)
      .post(uri"http://test.com/guests")
      .response(asJson[GuestModel])
      .send(backendStub)

    // then
    val value = response.map(_.body).unwrap
    value.foreach(assertEquals(_, GuestModel(id = 0, "marry", Female, 18, "student")))

    // given
    val john = GuestDto("john", Male, 32, "teacher")

    // when
    val response2 = basicRequest
      .body(john.asJson)
      .post(uri"http://test.com/guests")
      .response(asJson[GuestModel])
      .send(backendStub)

    // then
    val value2 = response2.map(_.body).unwrap
    value2.foreach(assertEquals(_, GuestModel(id = 1, "john", Male, 32, "teacher")))

    // when
    val response3 = basicRequest
      .body(john.asJson)
      .get(uri"http://test.com/guests")
      .response(asJson[Vector[GuestModel]])
      .send(backendStub)

    // then
    val value3 = response3.map(_.body).unwrap
    value3.foreach(
      assertEquals(
        _,
        Vector(
          GuestModel(id = 0, "marry", Female, 18, "student"),
          GuestModel(id = 1, "john", Male, 32, "teacher"),
        ),
      )
    )
