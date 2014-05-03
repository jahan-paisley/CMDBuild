package org.cmdbuild.api.fluent;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ExistingCard extends ActiveCard {

	public static class Attachment {

		private final String url;
		private final String name;
		private final String category;
		private final String description;

		public Attachment(final String url, final String name, final String category, final String description) {
			this.url = url;
			this.name = name;
			this.category = category;
			this.description = description;
		}

		public String getUrl() {
			return url;
		}

		public String getName() {
			return name;
		}

		public String getCategory() {
			return category;
		}

		public String getDescription() {
			return description;
		}

	}

	public static Attachment attachment(final String url, final String name, final String category,
			final String description) {
		return new Attachment(url, name, category, description);
	}

	private final Set<String> requestedAttributes;
	private final Set<String> unmodifiableRequestedAttributes;
	private final Map<String, Attachment> attachments;

	ExistingCard(final FluentApi api, final String className, final Integer id) {
		super(api, className, id);
		requestedAttributes = Sets.newHashSet();
		unmodifiableRequestedAttributes = unmodifiableSet(requestedAttributes);
		attachments = Maps.newHashMap();
	}

	public ExistingCard withCode(final String value) {
		super.setCode(value);
		return this;
	}

	public ExistingCard withDescription(final String value) {
		super.setDescription(value);
		return this;
	}

	public ExistingCard with(final String name, final Object value) {
		return withAttribute(name, value);
	}

	public ExistingCard withAttribute(final String name, final Object value) {
		super.set(name, value);
		return this;
	}

	public ExistingCard limitAttributes(final String... names) {
		requestedAttributes.addAll(asList(names));
		return this;
	}

	public Set<String> getRequestedAttributes() {
		return unmodifiableRequestedAttributes;
	}

	public Iterable<Attachment> getAttachments() {
		return attachments.values();
	}

	public ExistingCard withAttachment(final String url, final String name, final String category,
			final String description) {
		return with(attachment(url, name, category, description));
	}

	public ExistingCard with(final Attachment attachment) {
		attachments.put(attachment.getName(), attachment);
		return this;
	}

	public void update() {
		api().getExecutor().update(this);
	}

	public void delete() {
		api().getExecutor().delete(this);
	}

	public Card fetch() {
		return api().getExecutor().fetch(this);
	}

}
