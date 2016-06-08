/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package scaps.eclipse;

import org.eclipse.ui.plugin.AbstractUIPlugin
import org.osgi.framework.BundleContext

import scaps.eclipse.core.services.ScapsSettingsService

/**
 * The activator class controls the plug-in life cycle
 */
class ScapsPluginActivator extends AbstractUIPlugin {

  override def start(context: BundleContext) {
    super.start(context);
    ScapsSettingsService.setIndexerRunning(false)
  }

}
