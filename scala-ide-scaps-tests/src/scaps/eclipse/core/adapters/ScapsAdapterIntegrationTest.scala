package scaps.eclipse.core.adapters

import java.io.ByteArrayInputStream

import org.eclipse.core.resources.IResource
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.IJavaElement
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.IPackageFragmentRoot
import org.eclipse.jdt.internal.core.PackageFragment
import org.junit.Test
import org.scalaide.core.IScalaProject
import org.scalaide.core.testsetup.TestProjectSetup
import org.junit.After
import org.junit.Before
import org.junit.Assert._
import com.typesafe.scalalogging.StrictLogging
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.internal.core.JavaProject

class ScapsAdapterIntegrationTest extends TestProjectSetup("simple-structure-builder") with StrictLogging {

  @Test
  def testProjectIndexing: Unit = {
    val indexDir = ResourcesPlugin.getWorkspace.getRoot.getLocation.toOSString + "/testing/index"
    logger.info(s"IndexDir: $indexDir")
    val scapsAdapter = new ScapsAdapter(indexDir)
    // setup
    addSourceFile(project)("Calculator.scala", """
      class Home {
        def plus(num1: Int, num2: Int): Int = num1 + num2
      }""")
    val javaProject = project.javaProject
    val classPath = extractClassPath(javaProject)
    val projectSourceFragmentRoots = javaProject.getAllPackageFragmentRoots.filter(_.getKind == IPackageFragmentRoot.K_SOURCE).toList
    val compilationUnits = projectSourceFragmentRoots.flatMap(findSourceFiles)

    // SUT
    val indexResetResult = scapsAdapter.indexReset
    val indexResult = scapsAdapter.indexProject(classPath, compilationUnits)
    val indexFinalizeResult = scapsAdapter.indexFinalize
    val searchResult = scapsAdapter.search("Home")

    // verify
    assertTrue(indexResetResult.isRight)
    assertTrue(indexResult.isRight)
    assertTrue(indexFinalizeResult.isRight)
    assertTrue(searchResult.isRight)
  }

  private def extractClassPath(javaProject: IJavaProject): List[String] = {
    val resolvedClassPath = javaProject.getResolvedClasspath(true)
    resolvedClassPath.map(_.getPath.toString).toList
  }

  private def findSourceFiles(fragmentRoot: IPackageFragmentRoot): Seq[ICompilationUnit] = {
    def recursiveFindSourceFiles(javaElements: Array[IJavaElement]): Array[ICompilationUnit] = {
      val packageFragments = javaElements.collect { case p: PackageFragment => p }
      packageFragments.toList match {
        case Nil => Array()
        case _ =>
          val elements = packageFragments.map { p => (p.getCompilationUnits, p.getChildren) }.unzip
          val sourceFiles = elements._1.flatten
          val subPackageFragments = elements._2.flatten
          sourceFiles ++ recursiveFindSourceFiles(subPackageFragments)
      }
    }
    recursiveFindSourceFiles(fragmentRoot.getChildren)
  }

  def addSourceFile(project: IScalaProject)(name: String, contents: String) = {
    val folder = project.underlying.getFolder("src")
    if (!folder.exists())
      folder.create(IResource.NONE, true, null)
    val file = folder.getFile(name)
    if (!file.exists()) {
      val source = new ByteArrayInputStream(contents.getBytes())
      file.create(source, IResource.FORCE, null)
    }
  }

}
