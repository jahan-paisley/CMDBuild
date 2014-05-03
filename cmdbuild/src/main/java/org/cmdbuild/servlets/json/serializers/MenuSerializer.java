package org.cmdbuild.servlets.json.serializers;

import org.cmdbuild.services.store.menu.MenuItemDTO;
import org.cmdbuild.services.store.menu.MenuStore;
import org.cmdbuild.services.store.menu.MenuStore.MenuItem;
import org.cmdbuild.services.store.menu.MenuStore.MenuItemType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MenuSerializer {

	public static final String MENU = "menu", //
			CHILDREN = "children", //
			CLASS_NAME = "referencedClassName", //
			DESCRIPTION = "description", //
			ELEMENT_ID = "referencedElementId", //
			INDEX = "index", //
			SPECIFIC_TYPE_VALUES = "specificTypeValues", //
			TYPE = "type";

	/**
	 * Serialize the menu as tree
	 * sorting the items by index
	 * 
	 * @param menu
	 * @param withWrapper
	 * @return
	 * @throws JSONException
	 */
	public static JSONObject toClient( //
			final MenuItem menu, //
			final boolean withWrapper //
			) throws JSONException {
	
		final boolean sortByDescription = false;
		return toClient(menu, withWrapper, sortByDescription);
	}

	/**
	 * Serialize the menu as tree.
	 * If sortByDescription is false
	 * sort the items by index
	 * 
	 * @param menu
	 * @param withWrapper
	 * @param sortByDescription
	 * @return
	 * @throws JSONException
	 */
	public static JSONObject toClient( //
			final MenuItem menu, //
			final boolean withWrapper, //
			final boolean sortByDescription //
			) throws JSONException {

		final JSONObject out = singleToClient(menu);
		if (menu.getChildren().size() > 0) {
			final JSONArray children = new JSONArray();

			if (sortByDescription) {
				menu.sortChildByDescription();
			} else {
				menu.sortChildByIndex();
			}

			for (final MenuItem child : menu.getChildren()) {
				children.put(toClient(child, false, sortByDescription));
			}

			out.put(CHILDREN, children);
		}

		if (withWrapper) {
			return new JSONObject() {
				{
					put(MENU, out);
				}
			};
		} else {
			return out;
		}
	}

	public static JSONObject singleToClient(final MenuItem menu) throws JSONException {
		final JSONObject out = new JSONObject();
		out.put(TYPE, menu.getType().getValue());
		out.put(INDEX, menu.getIndex());
		out.put(ELEMENT_ID, menu.getReferencedElementId());
		out.put(DESCRIPTION, menu.getDescription());
		out.put(CLASS_NAME, menu.getReferedClassName());
		out.put(SPECIFIC_TYPE_VALUES, menu.getSpecificTypeValues());

		return out;
	}

	public static MenuItem toServer(final JSONObject jsonMenu) throws JSONException {
		final MenuItem item = singleToServer(jsonMenu);
		if (jsonMenu.has(CHILDREN)) {
			final JSONArray children = jsonMenu.getJSONArray(CHILDREN);
			for (int i = 0; i < children.length(); ++i) {
				final JSONObject child = (JSONObject) children.get(i);
				item.addChild(toServer(child));
			}
		}

		return item;
	}

	private static MenuItem singleToServer(final JSONObject jsonMenu) throws JSONException {
		final MenuItem item = new MenuItemDTO();
		final MenuItemType type = MenuStore.MenuItemType.getType(jsonMenu.getString(TYPE));
		item.setType(type);

		if (!MenuItemType.ROOT.equals(type)) {
			item.setDescription(jsonMenu.getString(DESCRIPTION));
			item.setIndex(jsonMenu.getInt(INDEX));
			item.setReferedClassName(jsonMenu.getString(CLASS_NAME));
			item.setReferencedElementId(getElementId(jsonMenu));
		}

		return item;
	}

	private static Integer getElementId(final JSONObject jsonMenu) throws JSONException {
		Integer elementId = null;
		if (jsonMenu.has(ELEMENT_ID)) {
			final String stringElementId = (String) jsonMenu.get(ELEMENT_ID);
			if (notEmpty(stringElementId)) {
				elementId = Integer.valueOf(stringElementId);
			}
		}

		return elementId;
	}

	private static boolean notEmpty(final String s) {
		return !"".equals(s);
	}
}
