//package org.cmdbuild.services.bim.connector;
//
//import java.util.Iterator;
//
//import org.cmdbuild.bim.model.Entity;
//import org.cmdbuild.bim.service.BimError;
//import org.cmdbuild.dao.entry.CMCard;
//import org.cmdbuild.dao.view.CMDataView;
//import org.cmdbuild.logic.data.lookup.LookupLogic;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * This class compares source and target sets and notify a listener the C-U-D
// * action to be performed.
// * */
//public class CollectionsDiffer implements Differ {
//
//	private final Iterable<Entity> source;
//	private final MapperSupport support;
//	private static final Logger logger = LoggerFactory.getLogger(CollectionsDiffer.class);
//
//	public CollectionsDiffer(final Iterable<Entity> source, CMDataView dataView, MapperSupport support) {
//		if (source == null) {
//			throw new BimError("source not initialised!");
//		}
//		this.source = source;
//		this.support = support;
//	}
//
//	@Override
//	public void findDifferences(DifferListener listener) {
//		logger.info("Find entities to create or update...");
//		for (Iterator<Entity> it = source.iterator(); it.hasNext();) {
//			Entity sourceElement = it.next();
//			String className = sourceElement.getTypeName();
//			String key = sourceElement.getKey();
//			CMCard destinationElement = support.fetchCardWithKey(key, className);
//			if (destinationElement != null) {
//				listener.updateTarget(sourceElement, destinationElement);
//			} else {
//				listener.createTarget(sourceElement);
//			}
//		}
//		logger.info("Done");
//
//		logger.info("Find entities to delete...");
//		// TODO
//		logger.info("Done");
//	}
//
// }