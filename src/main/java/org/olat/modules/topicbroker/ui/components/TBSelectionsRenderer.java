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
package org.olat.modules.topicbroker.ui.components;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.topicbroker.TBSelection;
import org.olat.modules.topicbroker.TBSelectionStatus;
import org.olat.modules.topicbroker.ui.TBParticipantRow;
import org.olat.modules.topicbroker.ui.TBUIFactory;

/**
 * 
 * Initial date: 7 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBSelectionsRenderer implements FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if (cellValue instanceof TBParticipantRow participantRow) {
			List<String> formatedLabels = new ArrayList<>(participantRow.getMaxSelections());
			for (TBSelection selection : participantRow.getSelections()) {
				TBSelectionStatus status = TBUIFactory.getSelectionStatus(participantRow.getBroker(),
						participantRow.getRequiredEnrollments(), participantRow.getNumEnrollments(),
						true, selection.isEnrolled(), selection.getSortOrder());
				formatedLabels.add(TBUIFactory.getPriorityLabel(translator, status, selection.getSortOrder(), selection.getTopic().getTitle()));
			}
			for (int i = participantRow.getNumSelections() + 1; i <= participantRow.getMaxSelections(); i++) {
				formatedLabels.add(TBUIFactory.getPriorityLabel(translator, TBSelectionStatus.fillIn, i, null));
			}
			
			
			target.append(TBUIFactory.getPriorityLabelsAsRow(formatedLabels));
		}
	}

}
