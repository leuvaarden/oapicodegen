package com.gitlab.skibcsit.petstore

import akka.actor.ActorSystem
import akka.stream.Materializer
import endpoints4s.Invalid
import endpoints4s.play.client.Endpoints
import org.scalatest.funsuite.AnyFunSuite
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

class MainTest extends AnyFunSuite {
  test("getPetById") {
    implicit val executionContext: ExecutionContextExecutor = ExecutionContext.global
    implicit val actorSystem: ActorSystem = ActorSystem()
    implicit val materializer: Materializer = Materializer.matFromSystem
    val impl = new Endpoints("https://petstore.swagger.io/v2", AhcWSClient())
      with endpoints4s.play.client.JsonEntitiesFromCodecs
      with SwaggerPetstore
    val requestedId: Long = 3
    val future: Future[Either[Either[Pet, Invalid], Invalid]] = impl.getPetById(requestedId)
    while (!future.isCompleted) {}
    future.value match {
      case Some(value) => value match {
        case Failure(exception) => fail(exception)
        case Success(value) => value match {
          case Left(value) => value match {
            case Left(value) =>
              println("Received: " + value)
              assert(value != null && value.id == requestedId)
            case Right(value) => fail("Received error: " + value)
          }
          case Right(value) => fail("Received error: " + value)
        }
      }
      case None => fail("Expected completed future")
    }
  }
}
