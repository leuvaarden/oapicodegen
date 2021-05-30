package com.gitlab.skibcsit
package oapicodegen

import com.gitlab.skibcsit.oapicodegen.endpoint.{dto, endpoints4s, toTraitName}
import com.gitlab.skibcsit.oapicodegen.lang.TreeHugger

import java.io.{File, FileWriter}

object Main {
  def main(args: Array[String]): Unit = {
    // gather data
    val packageName = "com.gitlab.skibcsit.petstore"
    val outputDir = "src/test/scala/com/gitlab/skibcsit/petstore/";
    val openAPI = openapi.parse("https://petstore.swagger.io/v2/swagger.json")

    // create interpreter
    val langAlg = new TreeHugger {}

    // create dirs
    val dir = new File(outputDir)
    if (!dir.exists()) {
      dir.mkdirs()
    }

    // generate dto
    val packageObject = dto.generate(packageName, openAPI)(langAlg)
    val packageString = langAlg.makeString(packageObject)
    val packageOutput = outputDir + "package.scala"
    val packageWriter = new FileWriter(packageOutput)
    packageWriter.write(packageString)
    packageWriter.flush()

    // generate endpoints
    val traitObject = endpoints4s.generate(packageName, openAPI)(langAlg)
    val traitString = langAlg.makeString(traitObject)
    val traitOutput = outputDir + toTraitName(openAPI.getInfo.getTitle) + ".scala"
    val traitWriter = new FileWriter(traitOutput)
    traitWriter.write(traitString)
    traitWriter.flush()
  }
}
