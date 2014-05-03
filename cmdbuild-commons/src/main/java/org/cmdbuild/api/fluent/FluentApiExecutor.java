package org.cmdbuild.api.fluent;

import java.util.List;
import java.util.Map;

public interface FluentApiExecutor {

	enum AdvanceProcess {
		YES, NO
	}

	CardDescriptor create(NewCard card);

	void update(ExistingCard card);

	void delete(ExistingCard card);

	Card fetch(ExistingCard card);

	List<Card> fetchCards(QueryClass card);

	void create(NewRelation relation);

	void delete(ExistingRelation relation);

	List<Relation> fetch(RelationsQuery query);

	Map<String, Object> execute(FunctionCall function);

	DownloadedReport download(CreateReport report);

	ProcessInstanceDescriptor createProcessInstance(NewProcessInstance processCard, AdvanceProcess advance);

	void updateProcessInstance(ExistingProcessInstance processCard, AdvanceProcess advance);

	Iterable<Lookup> fetch(QueryAllLookup queryLookup);

	Lookup fetch(QuerySingleLookup querySingleLookup);

}
