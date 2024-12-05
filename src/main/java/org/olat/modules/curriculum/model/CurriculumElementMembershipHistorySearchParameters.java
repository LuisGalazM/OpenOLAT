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
package org.olat.modules.curriculum.model;

import java.util.List;

import org.olat.core.id.Identity;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElementRef;

/**
 * 
 * Initial date: 5 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementMembershipHistorySearchParameters {
	
	private List<Identity> identities;

	private Curriculum curriculum;
	private List<? extends CurriculumElementRef> elements;
	
	private boolean excludeMembers;
	private boolean excludeReservations;
	
	public List<Identity> getIdentities() {
		return identities;
	}
	
	public void setIdentities(List<Identity> identities) {
		this.identities = identities;
	}
	
	public Curriculum getCurriculum() {
		return curriculum;
	}
	
	public void setCurriculum(Curriculum curriculum) {
		this.curriculum = curriculum;
	}
	
	public List<? extends CurriculumElementRef> getElements() {
		return elements;
	}
	
	public void setElements(List<? extends CurriculumElementRef> elements) {
		this.elements = elements;
	}

	public boolean isExcludeMembers() {
		return excludeMembers;
	}

	public void setExcludeMembers(boolean excludeMembers) {
		this.excludeMembers = excludeMembers;
	}

	public boolean isExcludeReservations() {
		return excludeReservations;
	}

	public void setExcludeReservations(boolean excludeReservations) {
		this.excludeReservations = excludeReservations;
	}
}
