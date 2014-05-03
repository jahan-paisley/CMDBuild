package org.cmdbuild.dms;

import static com.google.common.collect.Iterables.addAll;

import java.util.Collection;
import java.util.List;

import org.cmdbuild.dao.entrytype.CMClass;

import com.google.common.collect.Lists;

public class DefaultDocumentCreatorFactory implements DocumentCreatorFactory {

	private static final String ROOT_FOR_TEMPORARY = "tmp";

	@Override
	public DocumentCreator createTemporary(final Iterable<String> path) {
		final List<String> pathWithRoot = Lists.newArrayList(ROOT_FOR_TEMPORARY);
		addAll(pathWithRoot, path);
		return new DefaultDocumentCreator(pathWithRoot);
	}

	@Override
	public DocumentCreator create(final CMClass target) {
		return new DefaultDocumentCreator(buildSuperclassesPath(target));
	}

	private Collection<String> buildSuperclassesPath(final CMClass targetClass) {
		final List<String> path = Lists.newArrayList();
		CMClass currentClass = targetClass;
		path.add(currentClass.getIdentifier().getLocalName());
		while (currentClass.getParent() != null && !currentClass.getParent().getName().equals("Class")) {
			currentClass = currentClass.getParent();
			path.add(0, currentClass.getIdentifier().getLocalName());
		}
		return path;
	}

}
