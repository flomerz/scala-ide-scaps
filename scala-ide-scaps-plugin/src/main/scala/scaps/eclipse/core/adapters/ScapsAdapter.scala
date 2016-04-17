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

class ScapsAdapter(indexDir: String) extends StrictLogging {
  private def compiler(classPath: Seq[String]) = CompilerUtils.createCompiler(classPath)

  private def sourceExtractor(classPath: Seq[String]) = new ScalaSourceExtractor(compiler(classPath))
  private def libraryExtractor(classPath: Seq[String]) = new JarExtractor(compiler(classPath))

  private def searchEngine = {
    val conf = Settings.fromApplicationConf.modIndex { index => index.copy(indexDir = indexDir) }
    SearchEngine(conf).get
  }

  def indexProject(classPath: Seq[String], projectSourceFilePaths: Seq[String]) {
    val sourceFiles = projectSourceFilePaths.map { projectSourcePath =>
      val source = Source.fromFile(projectSourcePath)(Codec.UTF8).toSeq
      new BatchSourceFile(projectSourcePath, source)
    }
    indexDefinitions(sourceExtractor(classPath)(sourceFiles.toList))
  }

  def indexLibrary(classPath: Seq[String], librarySourcePath: String) {
    indexDefinitions(libraryExtractor(classPath)(new File(librarySourcePath)))
  }

  def indexFinalize = searchEngine.finalizeIndex.get

  private def indexDefinitions(definitionStream: Stream[ExtractionError \/ Definition]) {
    def definitions = ExtractionError.logErrors(definitionStream, logger.info(_))
    searchEngine.index(definitions).get
  }

}
