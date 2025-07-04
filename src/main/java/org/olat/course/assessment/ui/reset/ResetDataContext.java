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

import java.util.List;

import org.olat.core.id.Identity;
import org.olat.course.nodes.CourseNode;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 8 mars 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ResetDataContext {
	
	private ResetCourse resetCourse;
	private ResetParticipants resetParticipants;
	private final RepositoryEntry repositoryEntry;

	private boolean resetEmptyNodes;
	private List<CourseNode> courseNodes;
	private List<Identity> selectParticipants;
	private List<Identity> participantsResetPasedOverridden;
	private List<Identity> participantsResetPassed;
	private List<Identity> participantsArchiveCertificate;
	
	public ResetDataContext(RepositoryEntry repositoryEntry) {
		this.repositoryEntry = repositoryEntry;
	}

	public RepositoryEntry getRepositoryEntry() {
		return repositoryEntry;
	}
	
	public List<Identity> getSelectedParticipants() {
		return selectParticipants == null ? List.of() : selectParticipants;
	}

	public void setSelectedParticipants(List<Identity> participants) {
		this.selectParticipants = participants;
	}
	
	public List<Identity> getParticipantsResetPasedOverridden() {
		return participantsResetPasedOverridden;
	}

	public void setParticipantsResetPasedOverridden(List<Identity> participantsResetPasedOverridden) {
		this.participantsResetPasedOverridden = participantsResetPasedOverridden;
	}

	public List<Identity> getParticipantsResetPassed() {
		return participantsResetPassed;
	}

	public void setParticipantsResetPassed(List<Identity> participantsResetPassed) {
		this.participantsResetPassed = participantsResetPassed;
	}

	public List<Identity> getParticipantsArchiveCertificate() {
		return participantsArchiveCertificate;
	}

	public void setParticipantsArchiveCertificate(List<Identity> participantsArchiveCertificate) {
		this.participantsArchiveCertificate = participantsArchiveCertificate;
	}

	public ResetCourse getResetCourse() {
		return resetCourse;
	}

	public void setResetCourse(ResetCourse resetCourse) {
		this.resetCourse = resetCourse;
	}

	public ResetParticipants getResetParticipants() {
		return resetParticipants;
	}

	public void setResetParticipants(ResetParticipants resetParticipants) {
		this.resetParticipants = resetParticipants;
	}

	public boolean isResetEmptyNodes() {
		return resetEmptyNodes;
	}

	public void setResetEmptyNodes(boolean resetEmptyNodes) {
		this.resetEmptyNodes = resetEmptyNodes;
	}

	public List<CourseNode> getCourseNodes() {
		return courseNodes == null ? List.of() : courseNodes;
	}

	public void setCourseNodes(List<CourseNode> courseNodes) {
		this.courseNodes = courseNodes;
	}

	public enum ResetCourse {
		all,
		elements
	}

	public enum ResetParticipants {
		all,
		selected
	}
	
}
