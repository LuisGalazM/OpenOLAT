/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.UserSession;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumSecurityCallbackFactory;
import org.olat.modules.lecture.ui.LectureRoles;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.modules.lecture.ui.LecturesSecurityCallbackFactory;

/**
 * The root controller of the curriculum mamangement site
 * which holds the list controller and the bread crump / toolbar
 * 
 * Initial date: 15 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumManagerController extends BasicController implements Activateable2 {
	
	private final TooledStackedPanel toolbarPanel;

	private CurriculumManagerRootController rootCtrl;
	
	private final CurriculumSecurityCallback secCallback;
	private final LecturesSecurityCallback lecturesSecCallback;
	
	public CurriculumManagerController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		UserSession usess = ureq.getUserSession();
		secCallback = CurriculumSecurityCallbackFactory.createCallback(usess.getRoles());
		lecturesSecCallback = LecturesSecurityCallbackFactory.getSecurityCallback(true, false, false, LectureRoles.lecturemanager);

		toolbarPanel = new TooledStackedPanel("categoriesStackPanel", getTranslator(), this);
		toolbarPanel.setShowCloseLink(false, false);
		toolbarPanel.setInvisibleCrumb(-1);
		toolbarPanel.setToolbarEnabled(false);
		putInitialPanel(toolbarPanel);
		
		rootCtrl = new CurriculumManagerRootController(ureq, getWindowControl(), toolbarPanel,
				secCallback, lecturesSecCallback);
		listenTo(rootCtrl);
		toolbarPanel.pushController(translate("curriculum.overview"), rootCtrl);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		rootCtrl.activate(ureq, entries, state);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
