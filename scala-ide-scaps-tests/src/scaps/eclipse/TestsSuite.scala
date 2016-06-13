/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package scaps.eclipse

import org.junit.runner.RunWith
import org.junit.runners.Suite
import scaps.eclipse.core.adapters.ScapsAdapterIntegrationTest
import scaps.eclipse.core.services.ScapsServiceUnitTest
import scaps.eclipse.core.services.ScapsIndexServiceUnitTest
import scaps.eclipse.core.services.ScapsServiceIntegrationTest
import scaps.eclipse.core.services.ScapsIndexServiceUnitTest

@RunWith(classOf[Suite])
@Suite.SuiteClasses(Array(

  // Unit
  classOf[ScapsIndexServiceUnitTest],
  classOf[ScapsServiceUnitTest],

  // Integration
  classOf[ScapsServiceIntegrationTest],
  classOf[ScapsAdapterIntegrationTest] //
  ))
class TestsSuite {}
