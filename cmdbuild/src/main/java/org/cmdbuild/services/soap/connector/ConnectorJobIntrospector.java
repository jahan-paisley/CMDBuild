package org.cmdbuild.services.soap.connector;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.services.soap.syncscheduler.ConnectorJob;
import org.cmdbuild.services.soap.syncscheduler.ConnectorJob.Action;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;

import com.google.common.collect.Lists;

public class ConnectorJobIntrospector {

	private final Document document;
	private final ConnectorJob masterJob;
	private final List<ConnectorJob> detailJobs;
	private String masterClassName; // name of the class of the master class
	private Long masterCardId; // id of the master card
	private final DataAccessLogic dataAccessLogic;
	private final CMDataView view;
	private final LookupStore lookupStore;
	private static ExecutorService jobQueue = Executors.newSingleThreadExecutor();

	public ConnectorJobIntrospector(final Document document, final DataAccessLogic dataAccessLogic,
			final CMDataView view, final LookupStore lookupStore) {
		this.document = document;
		this.dataAccessLogic = dataAccessLogic;
		this.view = view;
		this.lookupStore = lookupStore;
		masterJob = new ConnectorJob(view, dataAccessLogic, lookupStore);
		this.detailJobs = Lists.newArrayList();
	}

	public String submitJobs() {
		final Element element = document.getRootElement();
		final Iterator cardsIterator = element.elementIterator(SyncAttributeNode.CARDLIST.getAttribute());
		if (cardsIterator.hasNext()) {
			final Element elementCardList = (Element) cardsIterator.next();
			retrieveMasterInfo(masterJob, element);

			// iterate over cardlist
			final Iterator iterElement = elementCardList.elementIterator();
			while (iterElement.hasNext()) {
				/** Card/s to update **/
				final Element elementCard = (Element) iterElement.next();

				// is card master
				if (elementCard.attribute("key") != null) {
					/** Infos about the master card **/
					setMasterJobProeprties(masterJob, elementCard);
					setAction(masterJob, element);
					jobQueue.submit(masterJob); // execute immediately
				} else {
					final ConnectorJob detailJob = new ConnectorJob(view, dataAccessLogic, lookupStore);
					/** Infos about the current detail card **/
					setDetailJobProperties(detailJob, elementCard);
					setAction(detailJob, element);
					detailJobs.add(detailJob);
				}
			}
			for (final ConnectorJob job : detailJobs) {
				jobQueue.submit(job);
			}
		}
		return "success";
	}

	@SuppressWarnings(value = { "unchecked" })
	private void retrieveMasterInfo(final ConnectorJob job, final Element rootElement) {
		/** Infos about the master card **/
		final Iterator<Element> iterMasterCard = rootElement.elementIterator(SyncAttributeNode.MASTER.getAttribute());
		if (iterMasterCard.hasNext()) {
			final Element masterCard = iterMasterCard.next();
			// classname ex. "Computer"
			final Iterator<Element> iterClassName = masterCard.elementIterator(SyncAttributeNode.MASTER_CLASSNAME
					.getAttribute());
			if (iterClassName.hasNext()) {
				this.masterClassName = iterClassName.next().getText();
			}
			// id ex. 504256
			final Iterator<Element> iterCardId = masterCard.elementIterator(SyncAttributeNode.MASTER_CARDID
					.getAttribute());
			if (iterCardId.hasNext()) {
				this.masterCardId = Long.parseLong(iterCardId.next().getText());
			}
		}
	}

	@SuppressWarnings(value = { "unchecked" })
	private void setAction(final ConnectorJob job, final Element element) {
		/** Action to execute **/
		String action = new String();
		final Iterator<Element> iterAction = element.elementIterator(SyncAttributeNode.ACTION.getAttribute());
		if (iterAction.hasNext()) {
			action = iterAction.next().getText();
			try {
				job.setAction(Action.getAction(action));
			} catch (final Exception e) {
				Log.SOAP.error("error setting action");
			}

		}
	}

	private void setMasterJobProeprties(final ConnectorJob job, final Element cardElement) {
		/** Infos about the master card **/
		job.setMasterClassName(this.masterClassName);
		job.setDetailClassName(this.masterClassName);
		job.setMasterCardId(this.masterCardId);
		job.setDetailCardId(this.masterCardId);
		job.setElementCard(cardElement);
		job.setIsMaster(true);
	}

	private void setDetailJobProperties(final ConnectorJob job, final Element element) {
		/** Infos about the detail card **/
		job.setMasterClassName(this.masterClassName);
		job.setMasterCardId(this.masterCardId);
		job.setDomainName(getDomainName(element));
		job.setDomainDirection(DomainDirection.getDirection(getDirectionDomain(element)));
		job.setDetailIdentifiers(getDetailIdentifiers(element));
		job.setIsShared(isSharedDetail(element));
		job.setDetailCardId(getDetailId(element));
		job.setDetailClassName(element.getName());
		job.setElementCard(element);
	}

	private String getDirectionDomain(final Element node) {
		final Attribute directionAttribute = node.attribute(SyncAttributeNode.DOMAINDIRECTION.getAttribute());
		if (directionAttribute != null) {
			return directionAttribute.getStringValue();
		}
		return "";
	}

	private String getDomainName(final Element node) {
		String domain = new String();
		final Attribute domainAttribute = node.attribute(SyncAttributeNode.DOMAIN.getAttribute());
		if (domainAttribute != null) {
			domain = domainAttribute.getStringValue();
		}
		return domain;
	}

	private LinkedList<String> getDetailIdentifiers(final Element node) {
		final LinkedList<String> list = new LinkedList<String>();
		final Attribute idsAttribute = node.attribute(SyncAttributeNode.IDENTIFIERS.getAttribute());
		if (idsAttribute != null) {
			final String ids = idsAttribute.getStringValue();
			final StringTokenizer st = new StringTokenizer(ids, ",");
			while (st.hasMoreTokens()) {
				list.add(st.nextToken().trim());
			}
		}
		return list;
	}

	private boolean isSharedDetail(final Element node) {
		final Attribute isSharedAttribute = node.attribute(SyncAttributeNode.ISSHARED.getAttribute());
		if (isSharedAttribute != null) {
			return Boolean.valueOf(isSharedAttribute.getStringValue());
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private Long getDetailId(final Element node) {
		Long detailCardId = 0L;
		final Iterator<Attribute> attributeIterator = node.attributeIterator();
		while (attributeIterator.hasNext()) {
			final Attribute attribute = attributeIterator.next();
			if (attribute.getName().trim().equals(SyncAttributeNode.DETAIL_CARDID.getAttribute())) {
				final String sDetailCardId = attribute.getStringValue();
				detailCardId = Long.parseLong(sDetailCardId);
			}
		}
		return detailCardId;
	}

}
