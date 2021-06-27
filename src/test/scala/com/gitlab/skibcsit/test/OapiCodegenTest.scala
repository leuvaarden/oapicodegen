package com.gitlab.skibcsit.test

import com.gitlab.skibcsit.petstore.SwaggerPetstoreOpenapi30
import endpoints4s.sttp.client.Endpoints
import org.scalatest.funsuite.AnyFunSuite
import sttp.client.{HttpURLConnectionBackend, Identity}

class OapiCodegenTest extends AnyFunSuite with RandomFunctions {
  val client = new Endpoints("https://petstore3.swagger.io/api/v3", HttpURLConnectionBackend())
    with endpoints4s.sttp.client.JsonEntitiesFromCodecs[Identity]
    with SwaggerPetstoreOpenapi30

  test("addPet") {
    client.addPet(randomPet()) match {
      case Left(value) => println(value)
      case Right(value) => println(value)
    }
  }

  test("createUser") {
    println(client.createUser(randomUser()))
  }

  test("createUsersWithListInput") {
    println(client.createUsersWithListInput(List(randomUser(), randomUser())))
  }

  test("deleteOrder") {
    client.deleteOrder(randomInt()) match {
      case Left(value) => value match {
        case Left(value) => println(value)
        case Right(value) => println(value)
      }
      case Right(value) => println(value)
    }
  }

  test("deletePet") {
    client.deletePet(randomInt()) match {
      case Left(value) => println(value)
      case Right(value) => println(value)
    }
  }

  test("deleteUser") {
    client.deleteUser(randomString()) match {
      case Left(value) => value match {
        case Left(value) => println(value)
        case Right(value) => println(value)
      }
      case Right(value) => println(value)
    }
  }

  test("findPetsByStatus") {
    client.findPetsByStatus(randomString()) match {
      case Left(value) => value.foreach(println)
      case Right(value) => println(value)
    }
  }

  test("findPetsByTags") {
    client.findPetsByTags(List(randomString(), randomString())) match {
      case Left(value) => value.foreach(println)
      case Right(value) => println(value)
    }
  }
  test("getInventory") {
    println(client.getInventory())
  }

  test("getOrderById") {
    client.getOrderById(randomInt()) match {
      case Left(value) => value match {
        case Left(value) => println(value)
        case Right(value) => println(value)
      }
      case Right(value) => println(value)
    }
  }

  test("getPetById") {
    client.getPetById(randomInt()) match {
      case Left(value) => value match {
        case Left(value) => println(value)
        case Right(value) => println(value)
      }
      case Right(value) => println(value)
    }
  }

  test("getUserByName") {
    client.getUserByName(randomString()) match {
      case Left(value) => value match {
        case Left(value) => println(value)
        case Right(value) => println(value)
      }
      case Right(value) => println(value)
    }
  }

  test("loginUser") {
    client.loginUser((randomString(), randomString())) match {
      case Left(value) => println(value)
      case Right(value) => println(value)
    }
  }
  test("logoutUser") {
    println(client.logoutUser())
  }


  test("placeOrder") {
    client.placeOrder(randomOrder()) match {
      case Left(value) => println(value)
      case Right(value) => println(value)
    }
  }

  test("updatePet") {
    client.updatePet(randomPet()) match {
      case Left(value) => value match {
        case Left(value) => value match {
          case Left(value) => println(value)
          case Right(value) => println(value)
        }
        case Right(value) => println(value)
      }
      case Right(value) => println(value)
    }
  }

  test("updatePetWithForm") {
    client.updatePetWithForm((randomInt(), randomString(), randomInt().toString)) match {
      case Left(value) => println(value)
      case Right(value) => println(value)
    }
  }

  test("updateUser") {
    println(client.updateUser((randomString(), randomUser())))
  }

  test("uploadFile") {
    println(client.uploadFile((randomInt(), randomString(), randomFile())))
  }
}
