package dev.ironduck.backend

import cats.effect.IO
import cats.effect.unsafe.implicits.global

import sttp.client3.testing.SttpBackendStub
import sttp.client3.{ UriContext, basicRequest }
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.integ.cats.effect.CatsMonadError

import features.text.TextController

class BackendSuite extends munit.FunSuite:
  extension [T](t: IO[T]) def unwrap: T = t.unsafeRunSync()

  test("count characters"):
    // given
    val backendStub = TapirStubInterpreter(SttpBackendStub(CatsMonadError[IO]()))
      .whenServerEndpointRunLogic(TextController.countCharactersServerEndpoint)
      .backend()

    // when
    val response = basicRequest
      .get(uri"http://test.com/count-characters?text=admin")
      .send(backendStub)

    // then
    val value = response.map(_.body).unwrap
    assertEquals(value, Right("5"))
