package lt.lexp.sbt

import sbt.Keys.*
import sbt.librarymanagement.ModuleID
import com.github.eikek.sbt.openapi.*
import sbt.{Attributed, AutoPlugin, Compile, Def, IO, TaskKey, settingKey}

import java.io.File

object SwaggerinatorSbt extends AutoPlugin {

  object autoImport {
    val swaggerinatorDependency = settingKey[ModuleID]("The dependency carrying the swagger file")
    val swaggerinatorPackage = settingKey[Pkg]("The package under which the generated code should be placed")

    lazy val getSwagger =
      TaskKey[Seq[File]]("copyResourcesFromJars", "Copy specific resources to be used by this project")

    lazy val getSwaggerSetting = getSwagger := {
      def work(classpathEntry: Attributed[File], jarName: String, resourceName: String, outputPath: File): Seq[File] =
        classpathEntry.get(artifact.key) match {
          case Some(entryArtifact) =>
            // searching artifact
            if (entryArtifact.name.startsWith(jarName)) {
              // unpack artifact's jar to tmp directory
              val jarFile = classpathEntry.data
              IO.withTemporaryDirectory { tmpDir =>
                IO.unzip(jarFile, tmpDir)
                // copy to project's target directory
                // Instead of copying you can do any other stuff here
                IO.copyFile(new File(tmpDir, resourceName), outputPath)
                outputPath :: Nil
              }
            } else Nil
          case _ => Nil
        }

      streams.value.log.info(s"Swaggerinating ${swaggerinatorDependency.value.name}")
      val outputPath = swaggerPath.value
      (Compile / dependencyClasspath).value.flatMap(entry =>
        work(entry, swaggerinatorDependency.value.name, swaggerFilename, outputPath)
      )
    }
  }

  import autoImport.*
  import OpenApiSchema.autoImport.*

  private val swaggerFilename = "swagger.yaml"
  val swaggerPath = Def.setting(new File(baseDirectory.value, s"target/$swaggerFilename"))

  override def requires = OpenApiSchema

  override def projectSettings =
    Seq(
      getSwaggerSetting,
      Compile / resourceGenerators += (Compile / getSwagger).taskValue,
      openapiCodegen := openapiCodegen.dependsOn(getSwagger).value,
      scalacOptions := Seq(
        "-deprecation:false",
        "-language:higherKinds",
        "-Ymacro-annotations"
      ),
      openapiSpec := swaggerPath.value,
      openapiPackage := swaggerinatorPackage.value,
      openapiTargetLanguage := Language.Scala,
      openapiScalaConfig := ScalaConfig()
        .withJson(ScalaJson.circeSemiautoExtra)
        .addMapping(CustomMapping.forType { case TypeDef("LocalDateTime", _) =>
          TypeDef("OffsetDateTime", Imports("java.time.OffsetDateTime"))
        })
        .addMapping(CustomMapping.forName { case s =>
          "Gen" + s
        })
        .addMapping(CustomMapping.forField {
          case s if s.prop.name == "type" => s.copy(prop = s.prop.copy(name = """`type`"""))
          case s                          => s
        }),
      libraryDependencies += swaggerinatorDependency.value
    )
}
