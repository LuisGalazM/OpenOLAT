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
package org.olat.modules.curriculum.ui;


import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.catalog.ui.BookedEvent;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.ui.AccessEvent;
import org.olat.resource.accesscontrol.ui.OffersController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Jan 15, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementInfosController extends BasicController implements Controller {
	
	private CurriculumElementInfosHeaderController headerCtrl;
	private OffersController offersCtrl;

	private final CurriculumElement element;

	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private ACService acService;

	public CurriculumElementInfosController(UserRequest ureq, WindowControl wControl, CurriculumElement element) {
		super(ureq, wControl);
		this.element = element;
		VelocityContainer mainVC = createVelocityContainer("curriculum_element_infos");
		putInitialPanel(mainVC);
		
		Boolean webPublish = Boolean.TRUE;
		Boolean isMember = Boolean.FALSE;
		if (getIdentity() != null) {
			webPublish = null;
			isMember = !curriculumService.getCurriculumElementMemberships(List.of(element), List.of(getIdentity())).isEmpty();
		}
		
		headerCtrl = new CurriculumElementInfosHeaderController(ureq, getWindowControl(), element, isMember);
		listenTo(headerCtrl);
		mainVC.put("header", headerCtrl.getInitialComponent());
		
		AccessResult acResult = acService.isAccessible(element, getIdentity(), isMember, false, webPublish, false);
		if (acResult.isAccessible() || acService.tryAutoBooking(getIdentity(), element, acResult)) {
			fireEvent(ureq, new BookedEvent(element));
		} else {
			offersCtrl = new OffersController(ureq, getWindowControl(), acResult.getAvailableMethods(), false);
			listenTo(offersCtrl);
			mainVC.put("offers", offersCtrl.getInitialComponent());
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == offersCtrl) {
			if (event == AccessEvent.ACCESS_OK_EVENT) {
				fireEvent(ureq, new BookedEvent(element));
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
