package org.cmdbuild.logic.data;

import java.util.List;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.logic.data.DefaultDataDefinitionLogic.MetadataActions.Create;
import org.cmdbuild.logic.data.DefaultDataDefinitionLogic.MetadataActions.Delete;
import org.cmdbuild.logic.data.DefaultDataDefinitionLogic.MetadataActions.Update;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.model.data.ClassOrder;
import org.cmdbuild.model.data.Domain;
import org.cmdbuild.model.data.EntryType;

public interface DataDefinitionLogic extends Logic {
	
	interface MetadataAction {

		interface Visitor {

			void visit(Create action);

			void visit(Update action);

			void visit(Delete action);
		}

		void accept(Visitor visitor);

	}
	
	interface FunctionItem {

		String name();

	}

	CMDataView getView();

	/**
	 * if forceCreation is true, check if already exists a table with the same
	 * name of the given entryType
	 */
	CMClass createOrUpdate(EntryType entryType, boolean forceCreation);

	CMClass createOrUpdate(EntryType entryType);

	/**
	 * TODO: delete also privileges that refers to the deleted class
	 */
	void deleteOrDeactivate(String className);

	CMAttribute createOrUpdate(Attribute attribute);

	void deleteOrDeactivate(Attribute attribute);

	void reorder(Attribute attribute);

	void changeClassOrders(String className, List<ClassOrder> classOrders);

	/**
	 * @deprecated use the create method and update methods only
	 */
	CMDomain createOrUpdate(Domain domain);

	CMDomain create(Domain domain);

	CMDomain update(Domain domain);

	void deleteDomainIfExists(String name);

	Iterable<FunctionItem> functions();

}