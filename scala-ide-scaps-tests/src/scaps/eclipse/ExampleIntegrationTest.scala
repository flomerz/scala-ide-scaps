/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package scaps.eclipse

import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.jdt.core.IPackageFragmentRoot
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.internal.core.PackageFragment
import org.junit.Before
import org.junit.Test
import org.junit.Ignore
import org.scalaide.core.testsetup.TestProjectSetup

class ExampleIntegrationTest extends TestProjectSetup("simple-structure-builder") {

  @Before
  def setup {
    println("iuu setup test")
  }

  @Test
  @Ignore
  def test1 {
    val javaProj = project.javaProject

    val srcDirs = javaProj.getAllPackageFragmentRoots.filter(_.getKind == IPackageFragmentRoot.K_SOURCE).head.getChildren.last
    val p: PackageFragment = srcDirs.asInstanceOf[PackageFragment]

    val srcs = p.getCompilationUnits.head.getResource

    println("done")
  }

}
