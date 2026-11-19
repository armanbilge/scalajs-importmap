# scalajs-importmap

An sbt plugin for rewriting `@JSImport`s at linking time in your Scala.js application, à la [`importmap`](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/script/type/importmap). This offers an interesting alternative to using bundlers such as Webpack or Vite: instead, you can lean into browsers' native support for ES Modules while loading your dependencies directly from CDNs such as [jsdelivr](https://www.jsdelivr.com/).

## Usage

```scala
addSbtPlugin("com.armanbilge" % "sbt-scalajs-importmap" % "0.2.0")
```

### Compatibility

| scalajs-importmap | Scala.js      |
| ----------------- | ------------- |
| 0.2.x             | >= 1.23.0     |
| 0.1.x             | < 1.23.0      |

Scala.js 1.23.0 changed the IR (`ClassDef.jsNativeMembers` was replaced by `topLevelImportDefs`), so 0.2.x requires Scala.js >= 1.23.0. Projects on older Scala.js versions should stay on 0.1.x.

```scala
enablePlugins(ScalaJSImportMapPlugin)
scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.ESModule))
scalaJSImportMap := { (rawImport: String) =>
  if (rawImport.startsWith("@shoelace-style/shoelace"))
    "https://cdn.jsdelivr.net/npm/" + rawImport
  else
    rawImport
}
```
