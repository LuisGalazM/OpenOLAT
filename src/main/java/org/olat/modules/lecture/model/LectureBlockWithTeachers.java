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
package org.olat.modules.lecture.model;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;

/**
 * 
 * Initial date: 12 mai 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockWithTeachers {
	
	private final boolean assessmentMode;
	private final Reference entryRef;
	private final LectureBlock lectureBlock;
	private final Reference curriculumElementRef;
	private final RepositoryEntryLectureConfiguration lecturesConfiguration;
	
	private final long leadTime;
	private final long followupTime;
	private final long numOfParticipants;
	private final List<Identity> teachers = new ArrayList<>(3);
	
	public LectureBlockWithTeachers(LectureBlock lectureBlock, RepositoryEntryLectureConfiguration lecturesConfiguration,
			Reference curriculumElementRef, Reference entryRef,
			long numOfParticipants, long leadTime, long followupTime, boolean assessmentMode) {
		this.entryRef = entryRef;
		this.lectureBlock = lectureBlock;
		this.assessmentMode = assessmentMode;
		this.leadTime = leadTime;
		this.followupTime = followupTime;
		this.numOfParticipants = numOfParticipants;
		this.curriculumElementRef = curriculumElementRef;
		this.lecturesConfiguration = lecturesConfiguration;
	}
	
	public boolean isAssessmentMode() {
		return assessmentMode;
	}
	
	public LectureBlock getLectureBlock() {
		return lectureBlock;
	}
	
	public long getNumOfParticipants() {
		return numOfParticipants;
	}

	public long getLeadTime() {
		return leadTime;
	}

	public long getFollowupTime() {
		return followupTime;
	}

	public Reference getEntryRef() {
		return entryRef;
	}

	public Reference getCurriculumElementRef() {
		return curriculumElementRef;
	}

	public List<Identity> getTeachers() {
		return teachers;
	}
	
	public RepositoryEntryLectureConfiguration getLecturesConfigurations() {
		return lecturesConfiguration;
	}
}
