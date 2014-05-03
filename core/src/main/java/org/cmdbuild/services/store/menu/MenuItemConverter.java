package org.cmdbuild.services.store.menu;

import static org.cmdbuild.services.store.menu.MenuConstants.ELEMENT_CLASS_ATTRIBUTE;
import static org.cmdbuild.services.store.menu.MenuConstants.ELEMENT_OBJECT_ID_ATTRIBUTE;
import static org.cmdbuild.services.store.menu.MenuConstants.GROUP_NAME_ATTRIBUTE;
import static org.cmdbuild.services.store.menu.MenuConstants.MENU_CLASS_NAME;
import static org.cmdbuild.services.store.menu.MenuConstants.NUMBER_ATTRIBUTE;
import static org.cmdbuild.services.store.menu.MenuConstants.PARENT_ID_ATTRIBUTE;
import static org.cmdbuild.services.store.menu.MenuConstants.TYPE_ATTRIBUTE;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.SystemDataAccessLogicBuilder;
import org.cmdbuild.model.View;
import org.cmdbuild.model.dashboard.DashboardDefinition;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.services.store.menu.MenuStore.MenuItem;
import org.cmdbuild.services.store.menu.MenuStore.MenuItemType;
import org.cmdbuild.services.store.menu.MenuStore.ReportExtension;

public class MenuItemConverter {

	private static final String NO_GROUP_NAME = "";
	private static final int NO_INDEX = 0;
	private static final Integer NO_REFERENCED_ELEMENT_ID = 0;

	protected final CMDataView dataView;
	private final DataAccessLogic dataAccessLogic;

	public MenuItemConverter( //
			final CMDataView dataView, //
			final SystemDataAccessLogicBuilder dataAccessLogicBuilder //
	) {
		this(dataView, dataAccessLogicBuilder.build());
	}

	protected MenuItemConverter( //
			final CMDataView dataView, //
			final DataAccessLogic dataAccessLogic) {
		this.dataView = dataView;
		this.dataAccessLogic = dataAccessLogic;
	}

	/**
	 * 
	 * @param groupName
	 * @param menuItem
	 * @return a menuCard for the given menuItem to assign to the given group
	 *         name
	 */
	public CMCardDefinition toMenuCard(final String groupName, final MenuItem menuItem) {
		final MenuItemType type = menuItem.getType();
		final MenuItemConverter converterStrategy;

		if (MenuItemType.REPORT_CSV.equals(type) || MenuItemType.REPORT_PDF.equals(type)) {
			converterStrategy = new ReportConverterStrategy(this);
		} else if (MenuItemType.FOLDER.equals(type)) {
			converterStrategy = new FolderConverterStrategy(this);
		} else if (MenuItemType.DASHBOARD.equals(type)) {
			converterStrategy = new DashboardConverterStrategy(this);
		} else if (MenuItemType.VIEW.equals(type)) {
			converterStrategy = new ViewConverterStrategy(this);
		} else {
			converterStrategy = new EntryTypeConverterStrategy(this);
		}

		return converterStrategy.fromMenuItemToMenuCard(groupName, menuItem);
	}

	/**
	 * 
	 * @param menuCards
	 * @return a MenuItem starting to a list of MenuCard
	 */
	public MenuItem fromMenuCard(final Iterable<CMCard> menuCards) {
		final MenuItem root = new MenuItemDTO();
		root.setType(MenuItemType.ROOT);
		final Map<Number, ConvertingItem> items = new HashMap<Number, ConvertingItem>();
		for (final CMCard menuCard : menuCards) {
			final Number id = menuCard.getId();
			try {
				items.put(id, convertMenuCardToMenuItemBuilder(menuCard));
			} catch (final Exception e) {
				Log.CMDBUILD.debug("Error converting MenuItem");
			}
		}
		for (final ConvertingItem item : items.values()) {
			final Number parentId = (Number) item.menuCard.get(PARENT_ID_ATTRIBUTE);
			if (parentId.longValue() > 0) {
				final ConvertingItem parent = items.get(parentId.longValue());
				parent.menuItem.addChild(item.menuItem);
			} else {
				root.addChild(item.menuItem);
			}
		}

		return root;
	}

	/**
	 * 
	 * @param cmClass
	 * @return a MenuItem that is the menu representation of a CMClass
	 */
	public MenuItem fromCMClass(final CMClass cmClass, final CMDataView dataView) {
		final MenuItem menuItem = new MenuItemDTO();

		final CMClass activity = dataView.findClass(Constants.BASE_PROCESS_CLASS_NAME);
		if (activity != null && activity.isAncestorOf(cmClass)) {

			menuItem.setType(MenuItemType.PROCESS);
		} else {
			menuItem.setType(MenuItemType.CLASS);
		}

		menuItem.setReferedClassName(cmClass.getIdentifier().getLocalName());
		menuItem.setReferencedElementId(NO_REFERENCED_ELEMENT_ID);
		menuItem.setDescription(cmClass.getDescription());
		menuItem.setGroupName(NO_GROUP_NAME);
		menuItem.setIndex(NO_INDEX);
		return menuItem;
	}

	// FIXME when implement new ReportCard use it as parameter
	public MenuItem fromCMReport(final CMCard report, final ReportExtension extension) {
		final MenuItem menuItem = new MenuItemDTO();
		if (ReportExtension.CSV.equals(extension)) {
			menuItem.setType(MenuItemType.REPORT_CSV);
		} else if (ReportExtension.PDF.equals(extension)) {
			menuItem.setType(MenuItemType.REPORT_PDF);
		}

		menuItem.setReferedClassName(report.getType().getIdentifier().getLocalName());
		menuItem.setReferencedElementId(Integer.valueOf(report.getId().toString()));
		menuItem.setDescription((String) report.getDescription());
		menuItem.setGroupName(NO_GROUP_NAME);
		menuItem.setIndex(NO_INDEX);
		return menuItem;
	}

	public MenuItem fromDashboard(final DashboardDefinition dashboardDefinition, final Integer id) {
		final MenuItem menuItem = new MenuItemDTO();

		menuItem.setType(MenuItemType.DASHBOARD);
		menuItem.setReferedClassName("_Dashboard");
		menuItem.setReferencedElementId(id);
		menuItem.setDescription(dashboardDefinition.getDescription());
		menuItem.setGroupName(NO_GROUP_NAME);
		menuItem.setIndex(NO_INDEX);
		return menuItem;
	}

	public MenuItem fromView(final View view) {
		final MenuItem menuItem = new MenuItemDTO();

		menuItem.setType(MenuItemType.VIEW);
		menuItem.setReferedClassName("_View");
		menuItem.setReferencedElementId(view.getId());
		menuItem.setDescription(view.getDescription());
		menuItem.setGroupName(NO_GROUP_NAME);
		menuItem.setIndex(NO_INDEX);

		return menuItem;
	}

	private ConvertingItem convertMenuCardToMenuItemBuilder(final CMCard menuCard) {
		final MenuItem menuItem = new MenuItemDTO();
		menuItem.setId(new Long(menuCard.getId()));
		menuItem.setType(MenuItemType.getType((String) menuCard.getCode()));
		menuItem.setDescription((String) menuCard.getDescription());
		menuItem.setParentId((Integer) menuCard.get(PARENT_ID_ATTRIBUTE));
		menuItem.setIndex((Integer) menuCard.get(NUMBER_ATTRIBUTE));

		if (!MenuItemType.FOLDER.equals(menuItem.getType())) {
			final Long etr = menuCard.get(ELEMENT_CLASS_ATTRIBUTE, Long.class);
			final String className = dataView.findClass(etr).getIdentifier().getLocalName();
			menuItem.setReferedClassName(className);
			menuItem.setReferencedElementId((Integer) menuCard.get(ELEMENT_OBJECT_ID_ATTRIBUTE));

			if (MenuItemType.VIEW.equals(menuItem.getType())) {
				final Card viewCard = dataAccessLogic.fetchCard(menuItem.getReferedClassName(), //
						menuItem.getReferencedElementId().longValue() //
						);

				// it's late
				final Map<String, Object> specificTypeValues = new HashMap<String, Object>();
				specificTypeValues.put("type", viewCard.getAttribute("Type"));
				specificTypeValues.put("filter", viewCard.getAttribute("Filter"));
				specificTypeValues.put("sourceFunction", viewCard.getAttribute("SourceFunction"));
				final Long classId = (Long) viewCard.getAttribute("IdSourceClass");
				final CMClass cmClass = dataView.findClass(classId);
				if (cmClass != null) {
					specificTypeValues.put("sourceClassName", cmClass.getIdentifier().getLocalName());
				}

				menuItem.setSpecificTypeValues(specificTypeValues);
			}
		}

		menuItem.setGroupName((String) menuCard.get(GROUP_NAME_ATTRIBUTE));
		return new ConvertingItem(menuCard, menuItem);
	}

	CMCardDefinition fromMenuItemToMenuCard(final String groupName, final MenuItem menuItem) {
		final CMCardDefinition menuCard = dataView.createCardFor(dataView.findClass(MENU_CLASS_NAME));

		final String typeAsString = menuItem.getType().getValue();
		// In the menu card, the code stores the node type.
		// The column Type store an info that could be used
		// for the report, but there are four years that is not implemented,
		// so does not consider this column!!
		menuCard.setCode(typeAsString);
		menuCard.set(TYPE_ATTRIBUTE, typeAsString); // FIXME ?
		menuCard.setDescription(menuItem.getDescription());
		menuCard.set(NUMBER_ATTRIBUTE, menuItem.getIndex());
		menuCard.set(GROUP_NAME_ATTRIBUTE, groupName);
		return menuCard;
	}

	private static class FolderConverterStrategy extends MenuItemConverter {

		public FolderConverterStrategy(final MenuItemConverter main) {
			super(main.dataView, main.dataAccessLogic);
		}

		@Override
		public CMCardDefinition fromMenuItemToMenuCard(final String groupName, final MenuItem menuItem) {
			return super.fromMenuItemToMenuCard(groupName, menuItem);
		}

	}

	private static class EntryTypeConverterStrategy extends MenuItemConverter {

		public EntryTypeConverterStrategy(final MenuItemConverter main) {
			super(main.dataView, main.dataAccessLogic);
		}

		@Override
		public CMCardDefinition fromMenuItemToMenuCard(final String groupName, final MenuItem menuItem) {
			final CMCardDefinition menuCard = super.fromMenuItemToMenuCard(groupName, menuItem);
			menuCard.set(ELEMENT_OBJECT_ID_ATTRIBUTE,
					(menuItem.getReferencedElementId() == null) ? 0 : menuItem.getReferencedElementId());
			final Long referedClassId = dataView.findClass(menuItem.getReferedClassName()).getId();
			menuCard.set(ELEMENT_CLASS_ATTRIBUTE, referedClassId);
			return menuCard;
		}

	}

	private static class ReportConverterStrategy extends MenuItemConverter {

		public ReportConverterStrategy(final MenuItemConverter main) {
			super(main.dataView, main.dataAccessLogic);
		}

		@Override
		public CMCardDefinition fromMenuItemToMenuCard(final String groupName, final MenuItem menuItem) {
			final CMCardDefinition menuCard = super.fromMenuItemToMenuCard(groupName, menuItem);
			menuCard.set(ELEMENT_OBJECT_ID_ATTRIBUTE, menuItem.getReferencedElementId());
			final Long reportClassId = dataView.findClass("Report").getId();
			menuCard.set(ELEMENT_CLASS_ATTRIBUTE, reportClassId);
			return menuCard;
		}

	}

	private static class DashboardConverterStrategy extends MenuItemConverter {

		public DashboardConverterStrategy(final MenuItemConverter main) {
			super(main.dataView, main.dataAccessLogic);
		}

		@Override
		public CMCardDefinition fromMenuItemToMenuCard(final String groupName, final MenuItem menuItem) {
			final CMCardDefinition menuCard = super.fromMenuItemToMenuCard(groupName, menuItem);
			menuCard.set(ELEMENT_OBJECT_ID_ATTRIBUTE, menuItem.getReferencedElementId());
			final Long dashboardClassId = dataView.findClass("_Dashboards").getId();
			menuCard.set(ELEMENT_CLASS_ATTRIBUTE, dashboardClassId);
			return menuCard;
		}

	}

	private static class ViewConverterStrategy extends MenuItemConverter {

		public ViewConverterStrategy(final MenuItemConverter main) {
			super(main.dataView, main.dataAccessLogic);
		}

		@Override
		public CMCardDefinition fromMenuItemToMenuCard(final String groupName, final MenuItem menuItem) {
			final CMCardDefinition menuCard = super.fromMenuItemToMenuCard(groupName, menuItem);
			menuCard.set(ELEMENT_OBJECT_ID_ATTRIBUTE, menuItem.getReferencedElementId());
			final Long viewClassId = dataView.findClass("_View").getId();
			menuCard.set(ELEMENT_CLASS_ATTRIBUTE, viewClassId);
			return menuCard;
		}

	}

	private static class ConvertingItem {
		public final CMCard menuCard;
		public final MenuItem menuItem;

		public ConvertingItem(final CMCard menuCard, final MenuItem menuItemBuilder) {
			this.menuCard = menuCard;
			this.menuItem = menuItemBuilder;
		}
	}
}
