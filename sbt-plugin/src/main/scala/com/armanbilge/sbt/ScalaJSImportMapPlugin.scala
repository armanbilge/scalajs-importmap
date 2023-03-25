/*
 * Copyright 2023 Arman Bilge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.armanbilge.sjsimportmap.sbt

import com.armanbilge.sjsimportmap.ImportMappedIRFile
import org.scalajs.sbtplugin.ScalaJSPlugin
import sbt._

import ScalaJSPlugin.autoImport._

object ScalaJSImportMapPlugin extends AutoPlugin {

  override def requires = ScalaJSPlugin

  object autoImport {
    lazy val scalaJSImportMap = settingKey[String => String]("Import map transform function")
  }

  import autoImport._

  override def buildSettings: Seq[Setting[_]] = Seq(
    scalaJSImportMap := { s => s },
  )

  override def projectSettings: Seq[Setting[_]] =
    inConfig(Compile)(configSettings) ++ inConfig(Test)(configSettings)

  private val configSettings = Seq(
    scalaJSIR := {
      scalaJSIR.value.map(_.map(ImportMappedIRFile.fromIRFile(_)(scalaJSImportMap.value)))
    },
  )

}
