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
package org.olat.course.nodes.gta.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.AssignmentType;
import org.olat.course.nodes.gta.GTAPeerReviewManager;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskReviewAssignment;
import org.olat.course.nodes.gta.TaskReviewAssignmentStatus;
import org.olat.course.nodes.gta.manager.AssignmentCalculator.Participant;
import org.olat.course.nodes.gta.model.SessionParticipationListStatistics;
import org.olat.course.nodes.gta.model.SessionParticipationStatistics;
import org.olat.course.nodes.gta.model.SessionStatistics;
import org.olat.course.nodes.gta.model.TaskImpl;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormProvider;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.EvaluationFormSurveyIdentifier;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.model.jpa.EvaluationFormResponses;
import org.olat.modules.forms.model.xml.Form;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 6 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class GTAPeerReviewManagerImpl implements GTAPeerReviewManager {
	
	private static final Logger log = Tracing.createLoggerFor(GTAPeerReviewManagerImpl.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GTATaskDAO taskDao;
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	@Autowired
	private GTATaskReviewAssignmentDAO taskReviewAssignmentDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;

	@Override
	public EvaluationFormSurvey loadSurvey(Task task) {
		return evaluationFormManager.loadSurveyByKey(task.getSurvey().getKey());
	}

	@Override
	public Task loadOrCreateSurvey(Task task, RepositoryEntry courseEntry, GTACourseNode gtaNode) {
		if(task.getSurvey() == null && gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_PEER_REVIEW)) {
			EvaluationFormSurvey survey = loadOrCreateSurvey(courseEntry, gtaNode, task.getIdentity());
			((TaskImpl)task).setSurvey(survey);
			task = taskDao.updateTask(task);
			dbInstance.commit();
		}
		return task;
	}
	
	@Override
	public EvaluationFormSurvey loadOrCreateSurvey(RepositoryEntry courseEntry, GTACourseNode gtaNode, Identity assessedIdentity) {
		EvaluationFormSurveyIdentifier surveyIdent = getSurveyIdent(courseEntry, gtaNode, assessedIdentity);
		EvaluationFormSurvey survey = evaluationFormManager.loadSurvey(surveyIdent);
		if (survey == null) {
			RepositoryEntry formEntry = GTACourseNode.getPeerReviewEvaluationForm(gtaNode.getModuleConfiguration());
			survey = evaluationFormManager.createSurvey(surveyIdent, formEntry);
		}
		return survey;
	}
	
	@Override
	public EvaluationFormParticipation loadParticipation(EvaluationFormSurvey survey, Identity executor) {
		return evaluationFormManager.loadParticipationByExecutor(survey, executor);
	}

	@Override
	public EvaluationFormParticipation loadOrCreateParticipation(EvaluationFormSurvey survey, Identity executor) {
		EvaluationFormParticipation participation = evaluationFormManager.loadParticipationByExecutor(survey, executor);
		if (participation == null) {
			participation = evaluationFormManager.createParticipation(survey, executor);
		}
		return participation;
	}

	@Override
	public EvaluationFormSession loadOrCreateSession(EvaluationFormSurvey survey, Identity executor) {
		EvaluationFormParticipation participation = loadOrCreateParticipation(survey, executor);
		EvaluationFormSession session = evaluationFormManager.loadSessionByParticipation(participation);
		if (session == null) {
			session = evaluationFormManager.createSession(participation);
		}
		return session;
	}
	
	@Override
	public EvaluationFormSession loadSession(EvaluationFormParticipation participation) {
		return evaluationFormManager.loadSessionByParticipation(participation);
	}

	@Override
	public EvaluationFormSession loadOrCreateSession(EvaluationFormParticipation participation) {
		EvaluationFormSession session = evaluationFormManager.loadSessionByParticipation(participation);
		if (session == null) {
			session = evaluationFormManager.createSession(participation);
		}
		return session;
	}

	@Override
	public TaskReviewAssignment createAssignment(Task task, Identity assignee) {
		return taskReviewAssignmentDao.createAssignment(task, assignee);
	}

	@Override
	public TaskReviewAssignment updateAssignment(TaskReviewAssignment assignment) {
		return taskReviewAssignmentDao.updateAssignment(assignment);
	}
	
	@Override
	public TaskReviewAssignment invalidateAssignment(TaskReviewAssignment assignment, GTACourseNode gtaNode, Identity doer) {
		assignment.setStatus(TaskReviewAssignmentStatus.invalidate);
		assignment = taskReviewAssignmentDao.updateAssignment(assignment);
		dbInstance.commit();
		return assignment;
	}
	
	@Override
	public TaskReviewAssignment reopenAssignment(TaskReviewAssignment assignment, GTACourseNode gtaNode, Identity doer) {
		assignment.setStatus(TaskReviewAssignmentStatus.inProgress);
		assignment = taskReviewAssignmentDao.updateAssignment(assignment);
		
		EvaluationFormParticipation participation = assignment.getParticipation();
		EvaluationFormSession session = loadSession(participation);
		evaluationFormManager.reopenSession(session);
		dbInstance.commit();
		return assignment;
	}

	@Override
	public int deleteAssignments(TaskList taskList) {
		int count = 0;
		List<TaskReviewAssignment> assignments = taskReviewAssignmentDao.getAllAssignments(taskList);
		for(TaskReviewAssignment assignment:assignments) {
			EvaluationFormParticipation participation = assignment.getParticipation();
			dbInstance.getCurrentEntityManager().remove(assignment);
			dbInstance.commit();
			count++;
			if(participation != null) {
				evaluationFormManager.deleteParticipation(participation, true);
				count++;
			}
			dbInstance.commit();
		}
		dbInstance.commit();
		return count;
	}

	@Override
	public List<TaskReviewAssignment> getAssignmentsForTask(Task task, boolean withRemovedOnes) {
		if(task == null || task.getKey() == null) return new ArrayList<>();
		return withRemovedOnes
				? taskReviewAssignmentDao.getAssignmentsWithRemovedOnes(task)
				: taskReviewAssignmentDao.getAssignments(task);
	}
	
	@Override
	public List<TaskReviewAssignment> getAssignmentsForTaskList(TaskList taskList, boolean withRemovedOnes) {
		return withRemovedOnes
				? taskReviewAssignmentDao.getAssignmentsWithRemovedOnes(taskList)
				: taskReviewAssignmentDao.getAssignments(taskList);
	}

	@Override
	public List<TaskReviewAssignment> getAssignmentsOfReviewer(TaskList taskList, Identity reviewer) {
		return taskReviewAssignmentDao.getAssignments(taskList, reviewer);
	}
	
	@Override
	public TaskReviewAssignment loadAssignment(TaskReviewAssignment assignment) {
		return taskReviewAssignmentDao.loadByKey(assignment.getKey());
	}

	@Override
	public List<Identity> getAssigneesToRemind(TaskList taskList, GTACourseNode gtaNode) {
		List<TaskReviewAssignmentStatus> status = List.of(TaskReviewAssignmentStatus.open, TaskReviewAssignmentStatus.inProgress);		
		List<Identity> identities = taskReviewAssignmentDao.findAssignees(taskList, status);
		return new ArrayList<>(new HashSet<>(identities));
	}

	@Override
	public void assign(RepositoryEntry courseEntry, TaskList taskList, GTACourseNode gtaNode) {
		List<Task> allTasks = taskDao.getTasks(taskList, gtaNode);
		List<Identity> allParticipants = repositoryEntryRelationDao.getMembers(courseEntry, RepositoryEntryRelationType.all, GroupRoles.participant.name());
		List<TaskReviewAssignment> allAssignments = taskReviewAssignmentDao.getAssignmentsWithRemovedOnes(taskList);
		Map<TaskReviewAssignmentKey, TaskReviewAssignment> allAssignmentsMap = allAssignments.stream()
				.collect(Collectors.toMap(TaskReviewAssignmentKey::valueOf, assignment -> assignment, (u, v) -> u));
		
		final String numOfReviews = gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_PEER_REVIEW_NUM_OF_REVIEWS,
				GTACourseNode.GTASK_PEER_REVIEW_NUM_OF_REVIEWS_DEFAULT);
		final int numberOfReviews = Integer.parseInt(numOfReviews);
		
		final String typeOfAssignment = gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_PEER_REVIEW_ASSIGNMENT,
				GTACourseNode.GTASK_PEER_REVIEW_ASSIGNMENT_DEFAULT);
		
		final boolean mutual = gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_PEER_REVIEW_MUTUAL_REVIEW);
		
		AssignmentType assignmentType = AssignmentType.keyOf(typeOfAssignment);
		// If assignment is random, and there is no assignment of task, the assignment of reviews can happen
		// quicker in the submit step.
		boolean needSelectedTask = (assignmentType != AssignmentType.RANDOM
				|| gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT));
		AssignmentCalculator assignmentCalculator = new AssignmentCalculator(allParticipants, allTasks, allAssignments);
		List<Participant> participants = assignmentCalculator
				.assign(assignmentType, numberOfReviews, mutual, needSelectedTask);
		
		for(Participant participant:participants) {
			Identity assignee = participant.participant();
			List<Task> tasksToReview = participant.plannedAwardedReviews();
			for(Task taskToReview:tasksToReview) {
				Identity toReview = taskToReview.getIdentity();
				log.debug("Assignment for {} {} to review {} {}", assignee.getUser().getLastName(), assignee.getUser().getFirstName(),
						toReview.getUser().getLastName(), toReview.getUser().getFirstName());
				
				TaskReviewAssignmentKey assignmentkey = new TaskReviewAssignmentKey(taskToReview, assignee);
				TaskReviewAssignment currentAssignment = allAssignmentsMap.get(assignmentkey);
				if(currentAssignment == null) {
					createAssignment(taskToReview, assignee);
				} else {
					currentAssignment.setAssigned(true);
					updateAssignment(currentAssignment);
				}
				log.debug("{} review {}", assignee.getUser().getLastName(), toReview.getUser().getLastName());
			}
		}
		
		dbInstance.commitAndCloseSession();
	}

	@Override
	public SessionParticipationListStatistics loadStatistics(Task task, List<TaskReviewAssignment> assignments,
			GTACourseNode gtaNode, List<TaskReviewAssignmentStatus> status) {
		if(task == null || task.getKey() == null) {
			return new SessionParticipationListStatistics(SessionStatistics.noStatistics(), new ArrayList<>());
		}
		SessionFilter sessionFilter = new TaskSessionFilter(task, status);
		return loadStatistics(gtaNode, assignments, sessionFilter);
	}
	
	@Override
	public SessionParticipationListStatistics loadStatistics(TaskList taskList, List<TaskReviewAssignment> assignments, Identity reviewer,
			GTACourseNode gtaNode, List<TaskReviewAssignmentStatus> status) {
		SessionFilter sessionFilter = new AssigneeSessionFilter(taskList, reviewer, status);
		return loadStatistics(gtaNode,assignments, sessionFilter);
	}
	
	public SessionParticipationListStatistics loadStatistics(GTACourseNode gtaNode, List<TaskReviewAssignment> assignments, SessionFilter sessionFilter) {
		RepositoryEntry formEntry = GTACourseNode.getPeerReviewEvaluationForm(gtaNode.getModuleConfiguration());
		Form form = evaluationFormManager.loadForm(formEntry);
		EvaluationFormResponses responses = evaluationFormManager.loadResponsesBySessions(sessionFilter);
		
		final SessionStatisticsCalculator calculator = new SessionStatisticsCalculator(responses, form);
		
		final Set<EvaluationFormSession> sessions = responses.getSessions();
		List<SessionParticipationStatistics> statistics = new ArrayList<>();
		for(EvaluationFormSession session:sessions) {
			SessionStatistics sessionStatistics = calculator.calculateStatistics(session);
			statistics.add(new SessionParticipationStatistics(session, session.getParticipation(), sessionStatistics));
		}
		
		final Set<EvaluationFormParticipation> doneParticipations = assignments.stream()
				.filter(assignment -> TaskReviewAssignmentStatus.done == assignment.getStatus())
				.map(TaskReviewAssignment::getParticipation)
				.collect(Collectors.toSet());
		
		final List<EvaluationFormSession> doneSessions = responses.getSessions().stream()
				.filter(session -> doneParticipations.contains(session.getParticipation()))
				.toList();
		
		SessionStatistics aggregatedStatistics = calculator.calculateStatistics(doneSessions);
		
		return new SessionParticipationListStatistics(aggregatedStatistics, statistics);
	}

	@Override
	public Map<Task, SessionParticipationListStatistics> loadStatisticsProTask(TaskList taskList,
			List<TaskReviewAssignment> assignments, GTACourseNode gtaNode, List<TaskReviewAssignmentStatus> status) {

		SessionFilter sessionFilter = new TaskListSessionFilter(taskList, status);
		
		RepositoryEntry formEntry = GTACourseNode.getPeerReviewEvaluationForm(gtaNode.getModuleConfiguration());
		Form form = evaluationFormManager.loadForm(formEntry);
		EvaluationFormResponses responses = evaluationFormManager.loadResponsesBySessions(sessionFilter);
		
		final SessionStatisticsCalculator calculator = new SessionStatisticsCalculator(responses, form);
		
		Map<Task,List<TaskReviewAssignment>> taskToAssignments = new HashMap<>();
		for(TaskReviewAssignment assignment:assignments) {
			List<TaskReviewAssignment> taskAssignments = taskToAssignments
					.computeIfAbsent(assignment.getTask(), t -> new ArrayList<>());
			taskAssignments.add(assignment);
		}
		
		final Set<EvaluationFormSession> sessions = responses.getSessions();
		Map<EvaluationFormParticipation,SessionParticipationStatistics> participationToStatistics = new HashMap<>();
		for(EvaluationFormSession session:sessions) {
			SessionStatistics sessionStatistics = calculator.calculateStatistics(session);
			participationToStatistics.put(session.getParticipation(),
					new SessionParticipationStatistics(session, session.getParticipation(), sessionStatistics));
		}
		
		final Set<EvaluationFormParticipation> doneParticipations = assignments.stream()
				.filter(assignment -> TaskReviewAssignmentStatus.done == assignment.getStatus())
				.map(TaskReviewAssignment::getParticipation)
				.collect(Collectors.toSet());
		
		Map<Task, SessionParticipationListStatistics> taskToSessionParticipationListStatistics = new HashMap<>();
		for(Map.Entry<Task, List<TaskReviewAssignment>> entry:taskToAssignments.entrySet()) {
			Task task = entry.getKey();
			List<TaskReviewAssignment> taskAssignments = entry.getValue();
			List<EvaluationFormSession> taskSessions = new ArrayList<>();
			List<SessionParticipationStatistics> statisticsList = new ArrayList<>();
			for(TaskReviewAssignment taskAssignment:taskAssignments) {
				if(taskAssignment.getParticipation() != null) {
					SessionParticipationStatistics statistics = participationToStatistics.get(taskAssignment.getParticipation());
					if(statistics != null) {
						statisticsList.add(statistics);
						taskSessions.add(statistics.getSession());
					}
				}	
			}
			
			List<EvaluationFormSession> doneSessions = taskSessions.stream()
					.filter(session -> doneParticipations.contains(session.getParticipation()))
					.toList();
			
			SessionStatistics aggregatedStatistics = calculator.calculateStatistics(doneSessions);
			SessionParticipationListStatistics participationsStatistics =
					new SessionParticipationListStatistics(aggregatedStatistics, statisticsList);
			
			taskToSessionParticipationListStatistics.put(task, participationsStatistics);
		}

		return taskToSessionParticipationListStatistics;
	}
	
	@Override
	public Map<Identity, SessionParticipationListStatistics> loadStatisticsProAssignee(TaskList taskList,
			List<TaskReviewAssignment> assignments, GTACourseNode gtaNode, List<TaskReviewAssignmentStatus> status) {
		
		SessionFilter sessionFilter = new TaskListSessionFilter(taskList, status);
		
		RepositoryEntry formEntry = GTACourseNode.getPeerReviewEvaluationForm(gtaNode.getModuleConfiguration());
		Form form = evaluationFormManager.loadForm(formEntry);
		EvaluationFormResponses responses = evaluationFormManager.loadResponsesBySessions(sessionFilter);
		
		final SessionStatisticsCalculator calculator = new SessionStatisticsCalculator(responses, form);
		
		Map<Identity,List<TaskReviewAssignment>> assigneeToAssignments = new HashMap<>();
		for(TaskReviewAssignment assignment:assignments) {
			List<TaskReviewAssignment> taskAssignments = assigneeToAssignments
					.computeIfAbsent(assignment.getAssignee(), t -> new ArrayList<>());
			taskAssignments.add(assignment);
		}
		
		final Set<EvaluationFormSession> sessions = responses.getSessions();
		Map<EvaluationFormParticipation,SessionParticipationStatistics> participationToStatistics = new HashMap<>();
		for(EvaluationFormSession session:sessions) {
			SessionStatistics sessionStatistics = calculator.calculateStatistics(session);
			participationToStatistics.put(session.getParticipation(),
					new SessionParticipationStatistics(session, session.getParticipation(), sessionStatistics));
		}
		
		final Set<EvaluationFormParticipation> doneParticipations = assignments.stream()
				.filter(assignment -> TaskReviewAssignmentStatus.done == assignment.getStatus())
				.map(TaskReviewAssignment::getParticipation)
				.collect(Collectors.toSet());
		
		Map<Identity, SessionParticipationListStatistics> taskToSessionParticipationListStatistics = new HashMap<>();
		for(Map.Entry<Identity, List<TaskReviewAssignment>> entry:assigneeToAssignments.entrySet()) {
			Identity assignee = entry.getKey();
			List<TaskReviewAssignment> taskAssignments = entry.getValue();
			List<EvaluationFormSession> taskSessions = new ArrayList<>();
			List<SessionParticipationStatistics> statisticsList = new ArrayList<>();
			for(TaskReviewAssignment taskAssignment:taskAssignments) {
				if(taskAssignment.getParticipation() != null) {
					SessionParticipationStatistics statistics = participationToStatistics.get(taskAssignment.getParticipation());
					if(statistics != null) {
						statisticsList.add(statistics);
						taskSessions.add(statistics.getSession());
					}
				}	
			}
			
			List<EvaluationFormSession> doneSessions = taskSessions.stream()
					.filter(session -> doneParticipations.contains(session.getParticipation()))
					.toList();
			
			SessionStatistics aggregatedStatistics = calculator.calculateStatistics(doneSessions);
			SessionParticipationListStatistics participationsStatistics =
					new SessionParticipationListStatistics(aggregatedStatistics, statisticsList);
			
			taskToSessionParticipationListStatistics.put(assignee, participationsStatistics);
		}

		return taskToSessionParticipationListStatistics;
	}

	private static final EvaluationFormSurveyIdentifier getSurveyIdent(RepositoryEntry courseEntry, GTACourseNode node, Identity assessedIdentity) {
		EvaluationFormProvider provider = GTACourseNode.getPeerReviewEvaluationFormProvider();
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(provider.getSurveyTypeName(), courseEntry.getKey());
		return EvaluationFormSurveyIdentifier.of(ores, node.getIdent(), assessedIdentity.getKey().toString());
	}
	
	public record TaskReviewAssignmentKey(Task task, Identity assignee) {
		
		public static final TaskReviewAssignmentKey valueOf(TaskReviewAssignment assignment) {
			return new TaskReviewAssignmentKey(assignment.getTask(), assignment.getAssignee());
		}
		
		@Override
		public int hashCode() {
			return (task == null ? 23486 : task.hashCode())
					+ (assignee == null ? -54812 : assignee.hashCode());
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof TaskReviewAssignmentKey key) {
				return task != null && Objects.equals(task, key.task())
						&& assignee != null && Objects.equals(assignee, key.assignee());
			}
			return false;
		}	
	}
}
