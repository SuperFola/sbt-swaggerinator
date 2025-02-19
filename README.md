# Sbt-swaggerinator

How to use?

*plugins.sbt*
```scala
addSbtPlugin("lt.lexp.sbt" % "sbt-swaggerinator" % "1.4.0")
addSbtPlugin("com.github.eikek" % "sbt-openapi-schema"  % "0.13.1")
```

Create a new module `my-api-generated` in your project.

*Dependencies.scala*
```scala
object Dependencies {
  lazy val swagger = "com.example" % "my-api-swagger_2.13" % "1.0.0"
}
```

*build.sbt*
```scala
lazy val `my-api-generated` = (project in file("modules/my-api-generated"))
  .enablePlugins(OpenApiSchema)
  .enablePlugins(SwaggerinatorSbt)
  .settings(swaggerinatorDependency := Dependencies.swagger)
  .settings(swaggerinatorPackage := Pkg("com.example.my-api.generated"))
  .settings(libraryDependencies ++= Dependencies.circe)

lazy val infrastructure = (project in file("modules/infrastructure"))
  // ...
  .dependsOn(`my-api-generated` % "compile->compile")
```

## Keys

- `swaggerinatorDependency`: the swagger artifact, published in a zip like format (zip, jar...)
- `swaggerinatorPackage`: under which package the generated code should be placed

