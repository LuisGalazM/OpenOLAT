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
package org.olat.course.assessment.ui.reset;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.id.Identity;
import org.olat.course.certificate.CertificateLight;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.Overridable;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 8 mars 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ResetDataIdentityRow extends UserPropertiesRow {
	
	private final Identity identity;
	
	private BigDecimal score;
	private Boolean passed;
	private Date passedAt;
	private Boolean passedOriginal;
	private Date passedOverridenAt;
	private AssessmentEntryStatus status;
	
	private Date lastModified;
	private Date lastUserModified;
	private Date lastCoachModified;
	private Date initialCourseLaunchDate;
	
	private CertificateLight certificate;
	private SingleSelection certificateResetItem;
	
	public ResetDataIdentityRow(Identity identity, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(identity, userPropertyHandlers, locale);
		this.identity = identity;
	}
	
	public void setAssessmentEntry(AssessmentEntry entry) {
		status = entry.getAssessmentStatus();
		score = entry.getScore();
		Overridable<Boolean> passedOverridable = entry.getPassedOverridable();
		passed = passedOverridable.getCurrent();
		passedAt = passedOverridable.getDate();
		passedOriginal = passedOverridable.getOriginal();
		passedOverridenAt = passedOverridable.getModDate();
		lastModified = entry.getLastModified();
		lastUserModified = entry.getLastUserModified();
		lastCoachModified = entry.getLastCoachModified();
	}
	
	public Identity getIdentity() {
		return identity;
	}

	public BigDecimal getScore() {
		return score;
	}

	public Boolean getPassed() {
		return passed;
	}

	public void setPassed(Boolean passed) {
		this.passed = passed;
	}

	public Date getPassedAt() {
		return passedAt;
	}

	public Boolean getPassedOriginal() {
		return passedOriginal;
	}

	public Date getPassedOverridenAt() {
		return passedOverridenAt;
	}

	public AssessmentEntryStatus getAssessmentStatus() {
		return status;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public Date getLastUserModified() {
		return lastUserModified;
	}

	public Date getLastCoachModified() {
		return lastCoachModified;
	}
	
	public Date getInitialCourseLaunchDate() {
		return initialCourseLaunchDate;
	}
	
	public void setInitialCourseLaunchDate(Date initialCourseLaunchDate) {
		this.initialCourseLaunchDate = initialCourseLaunchDate;
	}

	public CertificateLight getCertificate() {
		return certificate;
	}

	public void setCertificate(CertificateLight certificate) {
		this.certificate = certificate;
	}

	public SingleSelection getCertificateResetItem() {
		return certificateResetItem;
	}

	public void setCertificateResetItem(SingleSelection certificateResetItem) {
		this.certificateResetItem = certificateResetItem;
	}
}
