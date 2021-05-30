package com.gitlab.skibcsit
package oapicodegen

import com.gitlab.skibcsit.oapicodegen.endpoint.{dto, endpoints4s, toTraitName}
import com.gitlab.skibcsit.oapicodegen.lang.TreeHugger
import org.apache.commons.cli.{CommandLine, DefaultParser, Options}

import java.io.{File, FileWriter}

object Main {
  private final val PackageNameOption: String = "pn"
  private final val OutputDirOption: String = "od"
  private final val OpenAPIOption: String = "oapi"

  def main(args: Array[String]): Unit = {
    // gather data
    val cmd = parse(args)
    val packageName = cmd.getOptionValue(PackageNameOption)
    val outputDir = cmd.getOptionValue(OutputDirOption)
    val openAPI = openapi.parse(OpenAPIOption)

    // create dirs
    val dir = new File(outputDir)
    if (!dir.exists()) {
      dir.mkdirs()
    }

    // create interpreter
    val langAlg = new TreeHugger {}

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

  private def parse(args: Array[String]): CommandLine = {
    val parser = new DefaultParser()
    val options = new Options()
    options.addOption(PackageNameOption, true, "package name")
    options.addOption(OutputDirOption, true, "output dir")
    options.addOption(OpenAPIOption, true, "openapi location")
    parser.parse(options, args)
  }
}
