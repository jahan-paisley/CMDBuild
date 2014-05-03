package org.cmdbuild.servlets.json.schema;

import static org.cmdbuild.servlets.json.ComunicationConstants.GROUP_NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.MENU;

import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.services.store.menu.MenuStore;
import org.cmdbuild.services.store.menu.MenuStore.MenuItem;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.serializers.MenuSerializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONException;
import org.json.JSONObject;

public class ModMenu extends JSONBaseWithSpringContext {

	/**
	 * 
	 * @param groupName
	 * @return The full menu configuration. All the MenuItems configured for the
	 *         given group name.
	 * @throws JSONException
	 * @throws AuthException
	 * @throws NotFoundException
	 * @throws ORMException
	 */
	@Admin
	@JSONExported
	public JSONObject getMenuConfiguration( //
			@Parameter(GROUP_NAME) final String groupName //
	) throws JSONException, AuthException, NotFoundException, ORMException {

		final MenuStore store = menuStore();
		final MenuItem menu = store.read(groupName);
		final boolean withWrapper = true;
		return MenuSerializer.toClient(menu, withWrapper);
	}

	/**
	 * 
	 * @param groupName
	 *            The group for which we want the items that could be added to
	 *            the menu. This items are Classes, Processes, Reports and
	 *            Dashboards
	 * 
	 * @return the list of available items grouped by type
	 * @throws JSONException
	 */
	@Admin
	@JSONExported
	public JSONObject getAvailableMenuItems( //
			@Parameter(GROUP_NAME) final String groupName //
	) throws JSONException {

		final MenuStore store = menuStore();
		final MenuItem availableMenu = store.getAvailableItems(groupName);
		final boolean withWrapper = true;
		final boolean sortByDescription = true;
		return MenuSerializer.toClient(availableMenu, withWrapper, sortByDescription);
	}

	/**
	 * 
	 * @param groupName
	 *            the group name for which we want save the menu
	 * @param jsonMenuItems
	 *            the list of menu items
	 * @throws Exception
	 */
	@Admin
	@JSONExported
	public void saveMenu( //
			@Parameter(GROUP_NAME) final String groupName, //
			@Parameter(MENU) final JSONObject jsonMenu //
	) throws Exception {

		final MenuStore store = menuStore();
		final MenuItem menu = MenuSerializer.toServer(jsonMenu);
		store.save(groupName, menu);
	}

	/**
	 * 
	 * @param groupName
	 *            the name of the group for which we want delete the menu
	 * @return
	 * @throws JSONException
	 */
	@Admin
	@JSONExported
	public void deleteMenu( //
			@Parameter(GROUP_NAME) final String groupName //
	) throws JSONException {

		final MenuStore store = menuStore();
		store.delete(groupName);
	}

	/**
	 * 
	 * @param groupName
	 * @return the menu defined for the given group. If there are no menu for
	 *         this group, it returns the DefaultMenu (if exists). Note that
	 *         this method has to remove, eventually, the nodes that point to
	 *         something that the user has not the privileges to manage
	 * @throws JSONException
	 * @throws AuthException
	 * @throws NotFoundException
	 * @throws ORMException
	 */
	@JSONExported
	public JSONObject getAssignedMenu( //
			@Parameter(GROUP_NAME) final String groupName //
	) throws JSONException {

		final MenuStore store = menuStore();
		final MenuItem menu = store.getMenuToUseForGroup(groupName);
		final boolean withWrapper = true;
		return MenuSerializer.toClient(menu, withWrapper);

	}

}
