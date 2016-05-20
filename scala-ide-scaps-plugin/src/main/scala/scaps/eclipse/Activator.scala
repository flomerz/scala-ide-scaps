/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package scaps.eclipse;

import org.eclipse.jface.resource.ImageDescriptor
import org.eclipse.ui.plugin.AbstractUIPlugin
import org.osgi.framework.BundleContext;
import scaps.eclipse.core.services.ScapsService

/**
 * The activator class controls the plug-in life cycle
 */
// Wird dieser gebraucht? Falls keine Referenz darauf -> löschen.
class Activator extends AbstractUIPlugin {
  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  override def start(context: BundleContext) {
    super.start(context);
    Activator.plugin = this;
    ScapsService.setIndexerRunning(false)
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
   */
  override def stop(context: BundleContext) {
    Activator.plugin = null;
    super.stop(context);
  }

  /**
   * Returns the shared instance
   *
   * @return the shared instance
   */
  def getDefault = Activator.plugin

  /**
   * Returns an image descriptor for the image file at the given
   * plug-in relative path
   *
   * @param path the path
   * @return the image descriptor
   */
  def getImageDescriptor(path: String) {
    AbstractUIPlugin.imageDescriptorFromPlugin(ScapsPlugin.PLUGIN_ID, path);
  }
}

object Activator {
  // The shared instance
  private var plugin: Activator = _

}
