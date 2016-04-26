package scaps.eclipse

import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.core.IPackageFragmentRoot
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.internal.core.PackageFragment
import org.junit.Before
import org.junit.Test
import org.junit.Ignore

class ExampleIntegrationTest {

  @Before
  def setup {
    println("iuu setup test")
  }

  @Test
  def test1 {
    val workspace = ResourcesPlugin.getWorkspace.getRoot
    val workspacePath = workspace.getLocation
    val proj = workspace.getProjects.filter(_.hasNature(JavaCore.NATURE_ID)).head
    val javaProj = JavaCore.create(proj)

    val srcDirs = javaProj.getAllPackageFragmentRoots.filter(_.getKind == IPackageFragmentRoot.K_SOURCE).head.getChildren.last
    val p: PackageFragment = srcDirs.asInstanceOf[PackageFragment]

    val srcs = p.getCompilationUnits.head.getResource

    println("done")
  }

}
