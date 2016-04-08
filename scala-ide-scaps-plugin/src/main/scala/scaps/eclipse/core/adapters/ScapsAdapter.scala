package scaps.eclipse.core.adapters

import java.io.File

import scala.io.Codec
import scala.io.Source
import scala.reflect.internal.util.BatchSourceFile

import org.eclipse.core.resources.ResourcesPlugin

import com.typesafe.scalalogging.StrictLogging

import scalaz.{ \/ => \/ }
import scalaz.std.stream.streamInstance
import scaps.api.Definition
import scaps.scala.featureExtraction.CompilerUtils
import scaps.scala.featureExtraction.ExtractionError
import scaps.scala.featureExtraction.JarExtractor
import scaps.scala.featureExtraction.ScalaSourceExtractor
import scaps.searchEngine.SearchEngine
import scaps.settings.Settings

class ScapsAdapter extends StrictLogging {
  private def compiler(classPath: Seq[String]) = CompilerUtils.createCompiler(classPath)

  private def sourceExtractor(classPath: Seq[String]) = new ScalaSourceExtractor(compiler(classPath))
  private def libraryExtractor(classPath: Seq[String]) = new JarExtractor(compiler(classPath))

  private def searchEngine(indexDir: String) = {
    var conf = Settings.fromApplicationConf.modIndex { index => index.copy(indexDir = indexDir) }
    SearchEngine(conf).get
  }

  def indexProject(indexDir: String, classPath: Seq[String], projectSourcePaths: List[String]) {
    val sourceFiles = projectSourcePaths.map { projectSourcePath =>
      val source = Source.fromFile(projectSourcePath)(Codec.UTF8).toSeq
      new BatchSourceFile(projectSourcePath, source)
    }
    indexDefinitions(indexDir, sourceExtractor(classPath)(sourceFiles))
  }

  def indexLibrary(indexDir: String, classPath: Seq[String], librarySourcePath: String) {
    indexDefinitions(indexDir, libraryExtractor(classPath)(new File(librarySourcePath)))
  }

  private def indexDefinitions(indexDir: String, definitionStream: Stream[ExtractionError \/ Definition]) {
    def definitions = ExtractionError.logErrors(definitionStream, logger.info(_))
    searchEngine(indexDir).index(definitions).get
  }

}
