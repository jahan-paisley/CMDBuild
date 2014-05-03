package org.cmdbuild.services.bim;

import com.google.common.collect.Lists;

public interface RelationPersistence {

	public interface ProjectRelations {

		Long getProjectCardId();

		Iterable<String> getBindedCards();
		
	}
	
	public static ProjectRelations NULL_RELATIONS = new ProjectRelations() {
		
		@Override
		public Long getProjectCardId() {
			return (long) -1;
		}
		
		@Override
		public Iterable<String> getBindedCards() {
			return Lists.newArrayList();
		}
	};
	
	
	
	ProjectRelations readRelations(final Long projectCardId, final String rootClassName);
	
	void removeRelations(final Long projectCardId, final String rootClassName);

	void writeRelations(Long projectCardId, Iterable<String> cardBinding, String className);
	
}
