/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.lecture.ui.addwizard;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

/**
 * 
 * Initial date: 7 oct. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AddLectureBlock1ResourcesStep extends BasicStep {
	
	private final AddLectureContext addLecture;
	private SelectCurriculumElementsAndResourcesController selectCtrl;
	
	public AddLectureBlock1ResourcesStep(UserRequest ureq, AddLectureContext addLecture) {
		super(ureq);
		this.addLecture = addLecture;
		
		setNextStep(new AddLectureBlock2SettingsStep(ureq, addLecture));
		setI18nTitleAndDescr("wizard.resources.title", "wizard.resources.title");
	}
	
	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(false, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl,
			StepsRunContext runContext, Form form) {
		selectCtrl = new SelectCurriculumElementsAndResourcesController(ureq, wControl, form, addLecture, runContext);
		return selectCtrl;
	}
	
	public boolean canJumpStep() {
		return selectCtrl != null
				&& selectCtrl.getNumOfCurriculumElements() == 1
				&& selectCtrl.getNumOfRepositoryEntries() == 1;
	}
}
