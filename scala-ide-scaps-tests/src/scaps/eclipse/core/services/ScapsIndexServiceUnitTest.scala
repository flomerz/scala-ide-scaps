/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package scaps.eclipse.core.services

import org.eclipse.core.runtime.IAdaptable
import org.eclipse.core.runtime.IPath
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.IPackageFragmentRoot
import org.eclipse.ui.IWorkingSet
import org.junit.Assert._
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.runners.MockitoJUnitRunner
import scaps.eclipse.core.adapters.ScapsAdapter
import org.junit.Before
import java.io.File
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot

@RunWith(classOf[MockitoJUnitRunner])
class ScapsIndexServiceUnitTest {

  val scapsIndexService = new ScapsIndexService(mock(classOf[ScapsAdapter]))

  val mockedWorkingSet = mock(classOf[IWorkingSet])
  val mockedJavaProject = mock(classOf[IJavaProject])
  val mockedClassPathEntry = mock(classOf[IClasspathEntry])
  val mockedClassPathEntry2 = mock(classOf[IClasspathEntry])
  val mockedClassPath = mock(classOf[IPath])
  val mockedClassPath2 = mock(classOf[IPath])
  val mockedPackageFragment = mock(classOf[IPackageFragmentRoot])
  val mockedPackageFragment2 = mock(classOf[IPackageFragmentRoot])
  val mockedFile = mock(classOf[File])
  val mockedFile2 = mock(classOf[File])
  val classPath = "classpathString"
  val classPath2 = "classpathString2"

  @Before
  def setupRules() {
    when(mockedJavaProject.getResolvedClasspath(anyBoolean)).thenReturn(Array(mockedClassPathEntry, mockedClassPathEntry2))
    when(mockedClassPathEntry.getPath).thenReturn(mockedClassPath)
    when(mockedClassPathEntry2.getPath).thenReturn(mockedClassPath2)
    when(mockedClassPathEntry.getSourceAttachmentPath).thenReturn(mockedClassPath)
    when(mockedClassPath.toString).thenReturn(classPath)
    when(mockedClassPath.toFile).thenReturn(mockedFile)
    when(mockedClassPath2.toString).thenReturn(classPath2)
    when(mockedClassPath2.toFile).thenReturn(mockedFile2)
    when(mockedPackageFragment.getKind).thenReturn(IPackageFragmentRoot.K_SOURCE)
  }

  @Test
  def testExtractOfIJavaProject {
    // setup
    when(mockedWorkingSet.getElements).thenReturn(Array(mockedJavaProject).asInstanceOf[Array[IAdaptable]])
    when(mockedJavaProject.getAllPackageFragmentRoots).thenReturn(Array(mockedPackageFragment, mockedPackageFragment2))

    // SUT
    val result = scapsIndexService.extractElements(mockedWorkingSet)

    // verify
    assertEquals(2, result._1.size)
    assertTrue(result._1.contains(classPath))
    assertTrue(result._1.contains(classPath2))

    assertEquals(1, result._2.size)
    assertTrue(result._2.contains(mockedPackageFragment))
    assertFalse(result._2.contains(mockedPackageFragment2))

    assertEquals(1, result._3.size)
    assertTrue(result._3.contains(mockedFile))
    assertFalse(result._3.contains(mockedFile2))
  }

  @Test
  def testExtractOfJarPackageFragmentRoot {
    // setup
    val mockedJarPackageFragmentRoot = mock(classOf[JarPackageFragmentRoot])

    when(mockedWorkingSet.getElements).thenReturn(Array(mockedJarPackageFragmentRoot).asInstanceOf[Array[IAdaptable]])
    when(mockedJarPackageFragmentRoot.getSourceAttachmentPath).thenReturn(mockedClassPath)
    when(mockedJarPackageFragmentRoot.getJavaProject).thenReturn(mockedJavaProject)

    // SUT
    val result = scapsIndexService.extractElements(mockedWorkingSet)

    // verify
    assertEquals(2, result._1.size)
    assertTrue(result._1.contains(classPath))
    assertTrue(result._1.contains(classPath2))

    assertEquals(0, result._2.size)

    assertEquals(1, result._3.size)
    assertTrue(result._3.contains(mockedFile))
  }

  @Test
  def testExtractOfIPackageFragmentRoot {
    // setup
    val mockedIPackageFragmentRoot = mock(classOf[IPackageFragmentRoot])

    when(mockedWorkingSet.getElements).thenReturn(Array(mockedIPackageFragmentRoot).asInstanceOf[Array[IAdaptable]])
    when(mockedIPackageFragmentRoot.getJavaProject).thenReturn(mockedJavaProject)

    // SUT
    val result = scapsIndexService.extractElements(mockedWorkingSet)

    // verify
    assertEquals(2, result._1.size)
    assertTrue(result._1.contains(classPath))
    assertTrue(result._1.contains(classPath2))

    assertEquals(1, result._2.size)
    assertTrue(result._2.contains(mockedIPackageFragmentRoot))

    assertEquals(0, result._3.size)
  }

}
