/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package scaps.eclipse.ui.handlers.util

import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.ui.ide.IDE
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot
import org.eclipse.ui.PlatformUI
import org.eclipse.core.filesystem.EFS
import scaps.api.PosSource
import scaps.api.FileSource
import org.eclipse.ui.IEditorPart
import org.eclipse.jdt.core.JavaCore
import org.eclipse.ui.texteditor.ITextEditor
import org.eclipse.core.runtime.Path
import scaps.api.Source
import org.eclipse.jdt.ui.JavaUI

object OpenSourceHelper {

  def apply(source: Source): Unit = {
    val startPos = source.startPos.getOrElse(0)
    val endPos = source.endPos.getOrElse(0)
    source match {
      case fileSource @ FileSource(_, _: PosSource) => openProjectSourceFileInEditor(fileSource, startPos, endPos)
      case jarFileSource @ FileSource(_, fileInJarSource @ FileSource(_, _: PosSource)) => openLibarySourceFileInEditor(jarFileSource, fileInJarSource, startPos, endPos)
      case _ =>
    }
  }

  private def openProjectSourceFileInEditor(fileSource: FileSource, startPos: Int, endPos: Int): Unit = {
    val path = new Path(fileSource.artifactPath)
    val fileStore = EFS.getLocalFileSystem.getStore(path)
    val editor = IDE.openEditorOnFileStore(PlatformUI.getWorkbench.getActiveWorkbenchWindow.getActivePage, fileStore)
    selectLineInEditor(editor, startPos, endPos)
  }

  private def openLibarySourceFileInEditor(jarFileSource: FileSource, fileInJarSource: FileSource, startPos: Int, endPos: Int): Unit =
    for {
      jarRoot <- findJarPackageFragmentRoot(jarFileSource)
      (packagePath, classFile) <- convertToCompiledPath(fileInJarSource)
      packageFragment <- Option(jarRoot.getPackageFragment(packagePath))
      classFileJavaElement <- Option(packageFragment.getClassFile(classFile))
    } {
      val editor = JavaUI.openInEditor(classFileJavaElement)
      selectLineInEditor(editor, startPos, endPos)
    }

  private def convertToCompiledPath(fileInJarSource: FileSource): Option[(String, String)] = {
    val path = fileInJarSource.artifactPath
    if (path.contains("/") && path.endsWith(".scala")) {
      val elements = fileInJarSource.artifactPath.split("/")
      Some(elements.init.mkString("."), elements.last.replace(".scala", ".class"))
    } else {
      None
    }
  }

  private def findJarPackageFragmentRoot(jarFileSource: FileSource): Option[JarPackageFragmentRoot] = {
    def equals(jarPackageFragmentRoot: JarPackageFragmentRoot, jarPath: String): Boolean = {
      val isEqual = for {
        sourcePath <- Option(jarPackageFragmentRoot.getSourceAttachmentPath)
      } yield {
        sourcePath.toOSString.equals(jarPath)
      }
      isEqual.getOrElse(false)
    }

    val javaProjects = ResourcesPlugin.getWorkspace.getRoot.getProjects.filter(_.hasNature(JavaCore.NATURE_ID)).map(JavaCore.create(_))
    javaProjects.flatMap(_.getAllPackageFragmentRoots)
      .collect { case j: JarPackageFragmentRoot => j }
      .filter(equals(_, jarFileSource.artifactPath))
      .headOption
  }

  private def selectLineInEditor(editor: IEditorPart, startPos: Int, endPos: Int): Unit = editor match {
    case textEditor: ITextEditor =>
      for {
        documentProvider <- Option(textEditor.getDocumentProvider)
        editorInput <- Option(textEditor.getEditorInput)
        _ <- Option(documentProvider.getDocument(editorInput))
      } {
        textEditor.selectAndReveal(startPos, endPos - startPos)
      }
    case _ =>
  }

}
