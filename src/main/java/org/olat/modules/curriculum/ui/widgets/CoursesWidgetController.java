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
package org.olat.modules.curriculum.ui.widgets;

import java.util.ArrayList;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.panel.EmptyPanelItem;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.CourseModule;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.CurriculumComposerController;
import org.olat.modules.curriculum.ui.CurriculumListManagerController;
import org.olat.modules.curriculum.ui.event.ActivateEvent;
import org.olat.modules.curriculum.ui.widgets.CoursesWidgetDataModel.EntriesCols;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.olat.repository.controllers.RepositorySearchController.Can;
import org.olat.repository.ui.RepositoryEntryImageMapper;
import org.olat.repository.ui.author.AccessRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 oct. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoursesWidgetController extends FormBasicController implements FlexiTableComponentDelegate {
	
	private FormLink coursesLink;
	private FormLink addResourcesButton;
	private EmptyPanelItem emptyList;
	private FlexiTableElement entriesTableEl;
	private CoursesWidgetDataModel entriesTableModel;

	private final boolean resourcesManaged;
	private final MapperKey mapperThumbnailKey;
	private final CurriculumElement curriculumElement;
	private final CurriculumSecurityCallback secCallback;
	private final CurriculumElementType curriculumElementType;
	
	private CloseableModalController cmc;
	private ReferencableEntriesSearchController repoSearchCtr;

	@Autowired
	private MapperService mapperService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private CurriculumService curriculumService;
	
	public CoursesWidgetController(UserRequest ureq, WindowControl wControl,
			CurriculumElement curriculumElement, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl, "courses_widget", Util.createPackageTranslator(CurriculumComposerController.class, ureq.getLocale()));
		mapperThumbnailKey = mapperService.register(null, "repositoryentryImage", new RepositoryEntryImageMapper(900, 600));
		
		this.secCallback = secCallback;
		this.curriculumElement = curriculumElement;
		this.curriculumElementType = curriculumElement.getType();
		resourcesManaged = CurriculumElementManagedFlag.isManaged(curriculumElement, CurriculumElementManagedFlag.resources);
		
		initForm(ureq);
		loadModel();
	}
	
	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		if(rowObject instanceof CourseWidgetRow entryRow && entryRow.getOpenLink() != null) {
			return List.of(entryRow.getOpenLink().getComponent());
		}
		return List.of();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		coursesLink = uifactory.addFormLink("curriculum.courses", formLayout);
		coursesLink.setIconRightCSS("o_icon o_icon-fw o_icon_course_next");
		
		if(!resourcesManaged && secCallback.canManagerCurriculumElementResources(curriculumElement)
				&& (curriculumElementType == null || curriculumElementType.getMaxRepositoryEntryRelations() != 0)) {
			addResourcesButton = uifactory.addFormLink("add.resources", "", null, formLayout, Link.LINK | Link.NONTRANSLATED);
			addResourcesButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
			addResourcesButton.setTitle("add.resources");
		}

		emptyList = uifactory.addEmptyPanel("course.empty", null, formLayout);
		emptyList.setTitle(translate("curriculum.no.course.assigned.title"));
		emptyList.setIconCssClass("o_icon o_icon-lg o_CourseModule_icon");

		initFormTable(formLayout);
	}
	
	private void initFormTable(FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, EntriesCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EntriesCols.displayName, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EntriesCols.externalRef));
		
		entriesTableModel = new CoursesWidgetDataModel(columnsModel);
		entriesTableEl = uifactory.addTableElement(getWindowControl(), "entriesTable", entriesTableModel, 25, false, getTranslator(), formLayout);
		entriesTableEl.setCustomizeColumns(false);
		entriesTableEl.setNumOfRowsEnabled(false);
		entriesTableEl.setSelection(true, false, false);
		entriesTableEl.setAvailableRendererTypes(FlexiTableRendererType.custom);
		entriesTableEl.setRendererType(FlexiTableRendererType.custom);
		entriesTableEl.setNumOfRowsEnabled(false);
		entriesTableEl.setCssDelegate(new EntriesDelegate());
		
		VelocityContainer row = new VelocityContainer(null, "vc_row1", velocity_root + "/entry_1.html",
				getTranslator(), this);
		row.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		entriesTableEl.setRowRenderer(row, this);
	}
	
	public void loadModel() {
		List<RepositoryEntry> repositoryEntries = curriculumService.getRepositoryEntries(curriculumElement);
		
		AccessRenderer renderer = new AccessRenderer(getLocale());
		List<CourseWidgetRow> rows = new ArrayList<>();
		for(RepositoryEntry entry:repositoryEntries) {
			rows.add(forgeRow(entry, renderer));
		}
		entriesTableModel.setObjects(rows);
		entriesTableEl.reset(true, true, true);
		
		boolean empty = rows.isEmpty();
		entriesTableEl.setVisible(!empty);
		emptyList.setVisible(empty);
		
		int maxRelations = curriculumElementType == null ? -1 : curriculumElementType.getMaxRepositoryEntryRelations();
		if(addResourcesButton != null) {
			addResourcesButton.setVisible(maxRelations == -1 || maxRelations > rows.size());
		}
	}
	
	private CourseWidgetRow forgeRow(RepositoryEntry entry, AccessRenderer renderer) {
		String displayName = StringHelper.escapeHtml(entry.getDisplayname());
		FormLink openLink = uifactory.addFormLink("open_" + entry.getKey(), "open", displayName, null, flc, Link.NONTRANSLATED);
		final String url = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString("[RepositoryEntry:" + entry.getKey() + "]");
		openLink.setUrl(url);
		
		VFSLeaf image = repositoryManager.getImage(entry.getKey(), entry.getOlatResource());
		String thumbnailUrl = null;
		if(image != null) {
			thumbnailUrl = RepositoryEntryImageMapper.getImageUrl(mapperThumbnailKey.getUrl(), image);
		}
		String status = renderer.renderEntryStatus(entry);
		CourseWidgetRow row = new CourseWidgetRow(entry, openLink, url, thumbnailUrl, status);
		openLink.setUserObject(row);
		return row;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(repoSearchCtr == source) {
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				doAddRepositoryEntry(repoSearchCtr.getSelectedEntry());
				loadModel();
			} else if(event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRIES_SELECTED) {
				doAddRepositoryEntry(repoSearchCtr.getSelectedEntries());
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(repoSearchCtr);
		removeAsListenerAndDispose(cmc);
		repoSearchCtr = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(coursesLink == source) {
			List<ContextEntry> entries = BusinessControlFactory.getInstance()
					.createCEListFromResourceType(CurriculumListManagerController.CONTEXT_RESOURCES);
			fireEvent(ureq, new ActivateEvent(entries));
		} else if(addResourcesButton == source) {
			doChooseResources(ureq);
		} else if(source instanceof FormLink link && "open".equals(link.getCmd())
				&& link.getUserObject() instanceof CourseWidgetRow row) {
			doOpen(ureq, row);
		} else if(source instanceof FormLayoutContainer) {
			String entryKey = ureq.getParameter("select_entry");
			if(StringHelper.isLong(entryKey)) {
				doOpen(ureq, Long.valueOf(entryKey));
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpen(UserRequest ureq, CourseWidgetRow row) {
		doOpen(ureq, row.getKey());
	}
	
	private void doOpen(UserRequest ureq, Long entryKey) {
		String businessPath = "[RepositoryEntry:" + entryKey + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doChooseResources(UserRequest ureq) {
		if(guardModalController(repoSearchCtr)) return;
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean adminSearch = roles.hasRole(OrganisationRoles.administrator)
				|| roles.hasRole(OrganisationRoles.learnresourcemanager)
				|| roles.hasRole(OrganisationRoles.curriculummanager);
		boolean orgSearch = secCallback.canEditCurriculumElement(curriculumElement) && !adminSearch;
		repoSearchCtr = new ReferencableEntriesSearchController(getWindowControl(), ureq,
				new String[]{ CourseModule.getCourseTypeName() }, null, null, translate("add.resources"),
				false, false, true, orgSearch, adminSearch, false, Can.referenceable);
		listenTo(repoSearchCtr);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), repoSearchCtr.getInitialComponent(), true, translate("add.resources"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddRepositoryEntry(RepositoryEntry entry) {
		doAddRepositoryEntry(List.of(entry));
	}
	
	private void doAddRepositoryEntry(List<RepositoryEntry> entries) {
		boolean moveLectureBlocks = false;
		if(entries.size() == 1 && entriesTableModel.getRowCount() == 0) {
			moveLectureBlocks = true;
		}
		
		for(RepositoryEntry entry:entries) {
			curriculumService.addRepositoryEntry(curriculumElement, entry, moveLectureBlocks);
		}
	}
	
	private static class EntriesDelegate implements FlexiTableCssDelegate {

		@Override
		public String getWrapperCssClass(FlexiTableRendererType type) {
			return null;
		}

		@Override
		public String getTableCssClass(FlexiTableRendererType type) {
			return "o_cards";
		}

		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			return null;
		}
	}
}
