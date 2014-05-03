package org.cmdbuild.bim.mapper;

public interface Parser {

	/**
	 * @param entryPath: a string which somehow allows to identify the correct entry to read
	 * @return the number of entities nested in the selected entry
	 * */
	int getNumberOfNestedEntities(String entityPath);

	/**
	 * @param entityPath: a string which somehow allows to identify the correct entry to read
	 * @return the name of the chosen entity
	 * */
	String getEntityName(String entityPath);
	
	/**
	 * @param entityPath: a string which somehow allows to identify the correct entry to read
	 * @return the label of the chosen entity
	 * */
	String getEntityLabel(String entityPath);
	
	/**
	 * @param entryPath: a string which somehow allows to identify the correct entry to read
	 * @return a list of the names of all the attributes of the chosen entry
	 * */
	Iterable<String> getAttributesNames(String entityPath);
	
	/**
	 * @param entryPath: a string which somehow allows to identify the correct entry to read
	 * @param attributeName: the name of the attribute whose type is required
	 * @return the type of the chosen attribute
	 * */
	String getAttributeType(String entityPath, String attributeName);
	
	/**
	 * @param entityPath: a string which somehow allows to identify the correct entity to read
	 * @param attributeName: the name of the attribute to read
	 * @return the label of the selected attribute
	 * */
	public String getAttributeLabel(String entityPath, String attributeName);
	
	/**
	 * @param entityPath: a string which somehow allows to identify the correct entity to read
	 * @param attributeName: the name of the attribute to read
	 * @return the value of the selected attribute
	 * */
	String getAttributeValue(String entityPath, String attributeName);

	String getEntityShape(String path);

	String getEntityContainerAttribute(String path);

	int getNumberOfAttributes(String entityPath);

	String getAttributeType(String entityPath, int i);

	String getAttributeLabel(String entityPath, int i);

	String getAttributeValue(String entityPath, int i);

	String getAttributeName(String path, int i);

	

	
}
