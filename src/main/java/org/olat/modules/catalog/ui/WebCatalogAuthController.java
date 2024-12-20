/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.catalog.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.StringHelper;
import org.olat.dispatcher.AuthenticatedDispatcher;
import org.olat.login.PublicLoginAuthProvidersController;

/**
 * 
 * Initial date: 27 Sep 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class WebCatalogAuthController extends BasicController {

	public WebCatalogAuthController(UserRequest ureq, WindowControl wControl, String businessPath) {
		super(ureq, wControl);

		// add path to session, so the user gets redirected to the specific catalog page after successful login
		if (StringHelper.containsNonWhitespace(businessPath)) {
			String redirectPath = BusinessControlFactory.getInstance().getAsRestPart(BusinessControlFactory.getInstance().createFromString(businessPath).getEntriesDownTheControls(), true);
			ureq.getUserSession().putEntryInNonClearedStore(AuthenticatedDispatcher.AUTHDISPATCHER_REDIRECT_PATH, redirectPath);
		}

		PublicLoginAuthProvidersController loginAuthProvidersCtrl = new PublicLoginAuthProvidersController(ureq, wControl);
		listenTo(loginAuthProvidersCtrl);
		putInitialPanel(loginAuthProvidersCtrl.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source instanceof PublicLoginAuthProvidersController) {
			fireEvent(ureq, event);
		}
	}

}
