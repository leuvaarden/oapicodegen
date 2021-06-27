package com.gitlab.skibcsit.test

import com.gitlab.skibcsit.petstore._

import java.time.OffsetDateTime
import scala.util.Random

trait RandomFunctions {
  def randomInt(): Int = Random.nextInt(100)

  def randomBoolean(): Boolean = Random.nextBoolean()

  def randomString(): String = Random.alphanumeric.take(10).mkString

  def randomFile(): Array[Byte] = Array.fill(20)((Random.nextInt(256) - 128).toByte)

  def randomPet(): Pet = Pet(randomCategory(), randomInt(), randomString(), List(randomString(), randomString()), randomInt().toString, List(randomTag(), randomTag()))

  def randomTag(): Tag = Tag(randomInt(), randomString())

  def randomCategory(): Category = Category(randomInt(), randomString())

  def randomUser(): User = User(randomString(), randomString(), randomInt(), randomString(), randomString(), randomString(), randomInt(), randomString())

  def randomOrder(): Order = Order(randomBoolean(), randomInt(), randomInt(), randomInt(), OffsetDateTime.now().minusDays(1), randomString())
}
