package scaps.eclipse.ui.view.workingset

import org.eclipse.jdt.ui.StandardJavaElementContentProvider
import org.eclipse.core.resources.IProject
import org.eclipse.jdt.core.IPackageFragment
import org.eclipse.jdt.core.IJavaModel
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.internal.core.PackageFragmentRoot
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot

class ScapsWorkingSetPageContentProvider extends StandardJavaElementContentProvider {
  import StandardJavaElementContentProvider.NO_CHILDREN

  override def hasChildren(element: Any): Boolean = element match {
    case _: IJavaProject => true
    case _               => false
  }

  override def getChildren(parentElement: Any): Array[Object] = parentElement match {
    case _: IJavaModel =>
      super.getChildren(parentElement)
    case _: IJavaProject =>
      super.getChildren(parentElement).filter {
        case library: JarPackageFragmentRoot => library.getSourceAttachmentPath != null
        case _: PackageFragmentRoot          => true
        case _                               => false
      }
    case _ => NO_CHILDREN
  }

}
