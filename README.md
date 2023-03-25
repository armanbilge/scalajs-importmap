# scalajs-importmap

An sbt plugin for rewriting `@JSImport`s at linking time in your Scala.js application, à la [`importmap`](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/script/type/importmap). This offers an interesting alternative to using bundlers such as Webpack or Vite: instead, you can lean into browsers' native support for ES Modules while loading your dependencies directly from CDNs such as [jsdelivr](https://www.jsdelivr.com/).

## Usage

```scala
addSbtPlugin("com.armanbilge" % "sbt-scalajs-importmap" % "0.1.0")
```

```scala
enablePlugins(ScalaJSImportMapPlugin)
scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.ESModule))
scalaJSImportMap := { (rawImport: String) =>
  if (rawImport.startsWith("@shoelace-style/shoelace")
    "https://cdn.jsdelivr.net/npm/" + rawImport
  else
    rawImport
}
```
