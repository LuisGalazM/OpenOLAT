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
package org.olat.modules.appointments.ui;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.DateUtils;
import org.olat.modules.appointments.Appointment;

/**
 * 
 * Initial date: 14 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AppointmentDataModel extends DefaultFlexiTableDataModel<AppointmentRow>
implements SortableFlexiTableDataModel<AppointmentRow>{
	
	static final AppointmentCols[] COLS = AppointmentCols.values();
	public static final String FILTER_ALL = "all";
	public static final String FILTER_STATUS = "status";
	public static final String FILTER_PARTICIPATED = "participated";
	public static final String FILTER_OCCUPANCY_STATUS = "occupancy.status";
	public static final String FILTER_FUTURE = "future";
	
	private final Translator translator;
	
	public AppointmentDataModel(FlexiTableColumnModel columnsModel, Translator translator) {
		super(columnsModel);
		this.translator = translator;
	}

	@Override
	public void sort(SortKey orderBy) {
		List<AppointmentRow> rows = new AppointmentSortDelegate(orderBy, this, translator.getLocale()).sort();
		super.setObjects(rows);
	}

	@Override
	public Object getValueAt(int row, int col) {
		AppointmentRow appointment = getObject(row);
		return getValueAt(appointment, col);
	}

	@Override
	public Object getValueAt(AppointmentRow row, int col) {
		switch(AppointmentCols.values()[col]) {
			case id: return row.getAppointment().getKey();
			case status: return row.getAppointment().getStatus();
			case deadline: return getDeadline(row.getAppointment());
			case start: return row.getAppointment().getStart();
			case end: return row.getAppointment().getEnd();
			case location: return AppointmentsUIFactory.getDisplayLocation(translator, row.getAppointment());
			case details: return row.getAppointment().getDetails();
			case maxParticipations: return row.getAppointment().getMaxParticipations();
			case freeParticipations: return row.getFreeParticipations();
			case participants: return row.getParticipationsEl();
			case recordings: return row.getRecordingLinks();
			case select: return row.getSelectLink();
			case confirm: return row.getConfirmLink();
			case commands: return row.getCommandDropdown();
			default: return null;
		}
	}

	private Date getDeadline(Appointment appointment) {
		if (!appointment.isUseEnrollmentDeadline()) {
			return null;
		}
		Long deadlineMinutes = appointment.getEnrollmentDeadlineMinutes();
		if (deadlineMinutes == null) {
			return null;
		}
		return DateUtils.addMinutes(appointment.getStart(), -deadlineMinutes.intValue());
	}

	public enum AppointmentCols implements FlexiSortableColumnDef {
		id("appointment.id"),
		status("appointment.status"),
		deadline("appointment.deadline"),
		start("appointment.start"),
		end("appointment.end"),
		location("appointment.location"),
		details("appointment.details"),
		maxParticipations("appointment.max.participations"),
		freeParticipations("appointment.free.participations"),
		participants("participants"),
		recordings("recordings"),
		select("select"),
		confirm("confirm"),
		commands("action.more");
		
		private final String i18nKey;
		
		private AppointmentCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
