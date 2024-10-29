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
package org.olat.course.nodes.gta.ui.peerreview;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.boxplot.BoxPlot;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskReviewAssignment;
import org.olat.course.nodes.gta.model.SessionParticipationStatistics;
import org.olat.course.nodes.gta.model.SessionStatistics;
import org.olat.course.nodes.gta.ui.GTAParticipantController;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractParticipantPeerReviewsAwardedListController extends FormBasicController {
	
	protected int counter = 0;
	protected final boolean withYesNoRating;
	protected final boolean withStarsRating;
	protected final GTACourseNode gtaNode;
	
	@Autowired
	private UserManager userManager;
	
	public AbstractParticipantPeerReviewsAwardedListController(UserRequest ureq, WindowControl wControl, String pageName, GTACourseNode gtaNode) {
		super(ureq, wControl, pageName, Util.createPackageTranslator(GTAParticipantController.class, ureq.getLocale()));
		this.gtaNode = gtaNode;
		
		boolean qualityFeedback = gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_PEER_REVIEW_QUALITY_FEEDBACK, false);
		String qualityFeedbackType = gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_PEER_REVIEW_QUALITY_FEEDBACK_TYPE,
					GTACourseNode.GTASK_VALUE_PEER_REVIEW_QUALITY_FEEDBACK_YES_NO);
		withYesNoRating = qualityFeedback && GTACourseNode.GTASK_VALUE_PEER_REVIEW_QUALITY_FEEDBACK_YES_NO.equals(qualityFeedbackType);
		withStarsRating = qualityFeedback && GTACourseNode.GTASK_VALUE_PEER_REVIEW_QUALITY_FEEDBACK_STARS.equals(qualityFeedbackType);
	}
	
	protected ParticipantPeerReviewAssignmentRow forgeRow(TaskReviewAssignment assignment, Task task,
			SessionParticipationStatistics sessionStatistics, int pos) {
		String executorFullName = null;
		String assessedIdentityFullName = null;
		
		Identity executor = assignment.getAssignee();
		Identity assessedIdentity = assignment.getTask().getIdentity();		
		String formReview = gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_PEER_REVIEW_FORM_OF_REVIEW,
				GTACourseNode.GTASK_PEER_REVIEW_DOUBLE_BLINDED_REVIEW);
		if(GTACourseNode.GTASK_PEER_REVIEW_SINGLE_BLINDED_REVIEW.equals(formReview)) {
			executorFullName = translate("review.executor", Integer.toString(pos));
			assessedIdentityFullName = userManager.getUserDisplayName(assessedIdentity);
		} else if(GTACourseNode.GTASK_PEER_REVIEW_OPEN_REVIEW.equals(formReview)) {
			executorFullName = userManager.getUserDisplayName(executor);
			assessedIdentityFullName = userManager.getUserDisplayName(assessedIdentity);
		} else {
			executorFullName = translate("review.executor", Integer.toString(pos));
			assessedIdentityFullName = translate("review.assessed", Integer.toString(pos));
		}
		
		ParticipantPeerReviewAssignmentRow row = new ParticipantPeerReviewAssignmentRow(assignment, task,
				executorFullName, assessedIdentityFullName);
		forgeBoxPlot(row, sessionStatistics);
		forgeRating(row, assignment);
		return row;
	}
	
	protected ParticipantPeerReviewAssignmentRow forgeRating(ParticipantPeerReviewAssignmentRow row,
			TaskReviewAssignment assignment) {
		FormItem ratingItem = null;
		float rating = assignment.getRating() == null ? 0.0f : assignment.getRating().floatValue();
		if(withYesNoRating) {
			ratingItem = uifactory.addRatingItemYesNo("rating-" + (++counter), null, rating, 5, false, null);
		} else if(withStarsRating) {
			ratingItem = uifactory.addRatingItem("rating-" + (++counter), null, rating, 5, false, null);
		}
		row.setRatingItem(ratingItem);
		return row;
	}
	
	protected ParticipantPeerReviewAssignmentRow forgeBoxPlot(ParticipantPeerReviewAssignmentRow row,
			SessionParticipationStatistics sessionStatistics) {
	// Some loading
		
		double progressVal = 0.0d;
		
		if(sessionStatistics != null && sessionStatistics.statistics() != null
				&& sessionStatistics.statistics() != SessionStatistics.NO_STATISTICS) {
			progressVal = sessionStatistics.statistics().progress() * 100f;
		}

		if(progressVal > 0.0d) {
			double firstQuartile = 0.0d;
			double thirdQuartile = 0.0d;
			double median = 0.0d;
			
			double min = sessionStatistics.statistics().min();
			double max = sessionStatistics.statistics().max();
			double average = sessionStatistics.statistics().average();
			if(sessionStatistics.statistics().numOfQuestions() > 10) {
				firstQuartile = sessionStatistics.statistics().firstQuartile();
				median = sessionStatistics.statistics().median();
				thirdQuartile = sessionStatistics.statistics().thirdQuartile();
			}

			String id = Integer.toString(counter++);
			BoxPlot assessmentsPlot = new BoxPlot("plot-assessments-".concat(id), (int)sessionStatistics.statistics().maxStepsValue(),
					(float)min, (float)max, (float)average,
					(float)firstQuartile, (float)thirdQuartile, (float)median, null);
			row.setBoxPlot(assessmentsPlot);
		}

		return row;
	}
}
