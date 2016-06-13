/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package scaps.eclipse.core.services

import java.io.ByteArrayInputStream

import org.eclipse.core.resources.IResource
import org.eclipse.core.runtime.jobs.IJobChangeListener
import org.eclipse.ui.internal.WorkingSet
import org.junit.Assert._
import org.junit.Test
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalaide.core.IScalaProject
import org.scalaide.core.testsetup.TestProjectSetup
import org.junit.Ignore

class ScapsServiceIntegrationTest extends TestProjectSetup("simple-structure-builder") {

  val scapsIndexService = ScapsService.createIndexService
  val scapsSearchService = ScapsService.createSearchService

  @Test
  @Ignore
  def testIndexProject {
    addSourceFile(project)("Calculator.scala", """
      class Home {
        def plus(num1: Int, num2: Int): Int = num1 + num2
      }""")
    val workingSet = new WorkingSet("testing", "testing", Array(project.javaProject))

    // SUT
    val job = scapsIndexService(workingSet)
    job.addJobChangeListener(new IJobChangeListener {
      def aboutToRun(x$1: org.eclipse.core.runtime.jobs.IJobChangeEvent): Unit = {}
      def awake(x$1: org.eclipse.core.runtime.jobs.IJobChangeEvent): Unit = {}
      def done(x$1: org.eclipse.core.runtime.jobs.IJobChangeEvent): Unit = {
        val result = scapsSearchService("Home")
        assertTrue(result.isRight)
      }
      def running(x$1: org.eclipse.core.runtime.jobs.IJobChangeEvent): Unit = {}
      def scheduled(x$1: org.eclipse.core.runtime.jobs.IJobChangeEvent): Unit = {}
      def sleeping(x$1: org.eclipse.core.runtime.jobs.IJobChangeEvent): Unit = {}
    })
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
