package com.gitlab.skibcsit

package object petstore {
  case class Address(city: String, state: String, street: String, zip: String)
  case class ApiResponse(code: Int, message: String, `type`: String)
  case class Category(id: Long, name: String)
  case class Customer(address: Iterable[Address], id: Long, username: String)
  case class Order(complete: Boolean, id: Long, petId: Long, quantity: Int, shipDate: java.time.OffsetDateTime, status: String)
  case class Pet(category: Category, id: Long, name: String, photoUrls: Iterable[String], status: String, tags: Iterable[Tag])
  case class Tag(id: Long, name: String)
  case class User(email: String, firstName: String, id: Long, lastName: String, password: String, phone: String, userStatus: Int, username: String)
}