package scaps.eclipse.core.adapters

import java.io.File

import org.junit.Test

class ScapsAdapterIntegrationTest {

  @Test
  def testProjectIndexing {
    val classPath = new File("target/libs").listFiles.toList.map(_.getPath.toString)
    new ScapsAdapter("target/scaps/index").indexProject(classPath, List("test-workspace/scala-ide-scaps-testproject/src/main/scala/edu/scaps/Hello.scala"))
  }

}
