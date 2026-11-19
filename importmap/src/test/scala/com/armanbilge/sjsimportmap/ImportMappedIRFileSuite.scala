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

package com.armanbilge.sjsimportmap

import org.scalajs.ir.ClassKind
import org.scalajs.ir.Names.ClassName
import org.scalajs.ir.Names.MethodName
import org.scalajs.ir.OriginalName
import org.scalajs.ir.Position
import org.scalajs.ir.Trees._
import org.scalajs.ir.Types
import org.scalajs.ir.Version
import org.scalajs.linker.interface.unstable.IRFileImpl

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class ImportMappedIRFileSuite extends munit.FunSuite {

  private implicit val pos: Position = Position.NoPosition
  private implicit val ec: ExecutionContext = munitExecutionContext

  private val mapper: String => String = {
    case "foo" => "https://cdn.example.com/foo.js"
    case other => other
  }

  private def classDef(
      kind: ClassKind,
      jsNativeLoadSpec: Option[JSNativeLoadSpec],
      topLevelImportDefs: List[TopLevelImportDef],
  ): ClassDef =
    ClassDef(
      ClassIdent(ClassName("test.Foo$")),
      OriginalName.NoOriginalName,
      kind,
      None,
      None,
      Nil,
      None,
      jsNativeLoadSpec,
      Nil,
      Nil,
      None,
      Nil,
      topLevelImportDefs,
      Nil,
    )(OptimizerHints.empty)

  private def irFile(classDef: ClassDef): IRFileImpl =
    new IRFileImpl("test.sjsir", Version.Unversioned) {
      def entryPointsInfo(implicit ec: ExecutionContext) =
        Future.failed(new UnsupportedOperationException)
      def tree(implicit ec: ExecutionContext) = Future.successful(classDef)
    }

  private def mapped(tree: ClassDef): Future[ClassDef] =
    IRFileImpl
      .fromIRFile(ImportMappedIRFile.fromIRFile(irFile(tree))(mapper))
      .tree

  private val staticFlags =
    MemberFlags.empty.withNamespace(MemberNamespace.PublicStatic)

  // @js.native @JSImport("foo", "bar") def baz(): Int = js.native
  private def nativeMember(spec: JSNativeLoadSpec) =
    JSNativeMemberDef(
      staticFlags,
      MethodIdent(MethodName("baz", Nil, Types.IntRef)),
      spec,
    )

  private val wasmImport =
    WasmImportedMethodDef(
      staticFlags,
      MethodIdent(MethodName("qux", Nil, Types.IntRef)),
      Nil,
      Types.IntType,
      "foo",
      "qux",
    )

  test("remaps module specifier of top-level native members") {
    val tree = classDef(
      ClassKind.ModuleClass,
      None,
      List(nativeMember(JSNativeLoadSpec.Import("foo", List("bar")))),
    )
    mapped(tree).map { result =>
      assertEquals(
        result.topLevelImportDefs.collect { case m: JSNativeMemberDef => m.jsNativeLoadSpec },
        List(JSNativeLoadSpec.Import("https://cdn.example.com/foo.js", List("bar"))),
      )
    }
  }

  test("remaps ImportWithGlobalFallback in top-level native members") {
    val global = JSNativeLoadSpec.Global("Foo", Nil)
    val tree = classDef(
      ClassKind.ModuleClass,
      None,
      List(
        nativeMember(
          JSNativeLoadSpec.ImportWithGlobalFallback(JSNativeLoadSpec.Import("foo", Nil), global),
        ),
      ),
    )
    mapped(tree).map { result =>
      assertEquals(
        result.topLevelImportDefs.collect { case m: JSNativeMemberDef => m.jsNativeLoadSpec },
        List(
          JSNativeLoadSpec.ImportWithGlobalFallback(
            JSNativeLoadSpec.Import("https://cdn.example.com/foo.js", Nil),
            global,
          ),
        ),
      )
    }
  }

  test("remaps module specifier of native JS classes") {
    val tree =
      classDef(ClassKind.NativeJSClass, Some(JSNativeLoadSpec.Import("foo", List("Bar"))), Nil)
    mapped(tree).map { result =>
      assertEquals(
        result.jsNativeLoadSpec,
        Some(JSNativeLoadSpec.Import("https://cdn.example.com/foo.js", List("Bar"))),
      )
    }
  }

  test("does not remap Wasm imports") {
    val tree = classDef(
      ClassKind.ModuleClass,
      None,
      List(nativeMember(JSNativeLoadSpec.Import("foo", Nil)), wasmImport),
    )
    mapped(tree).map { result =>
      assert(result ne tree)
      assert(result.topLevelImportDefs(1) eq wasmImport)
    }
  }

  test("returns the original tree when there is nothing to remap") {
    val onlyWasm = classDef(ClassKind.ModuleClass, None, List(wasmImport))
    val empty = classDef(ClassKind.Class, None, Nil)
    for {
      r1 <- mapped(onlyWasm)
      r2 <- mapped(empty)
    } yield {
      assert(r1 eq onlyWasm)
      assert(r2 eq empty)
    }
  }

}
