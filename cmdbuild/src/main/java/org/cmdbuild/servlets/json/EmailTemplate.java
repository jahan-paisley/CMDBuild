package org.cmdbuild.servlets.json;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.servlets.json.ComunicationConstants.BCC;
import static org.cmdbuild.servlets.json.ComunicationConstants.BODY;
import static org.cmdbuild.servlets.json.ComunicationConstants.CC;
import static org.cmdbuild.servlets.json.ComunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.ComunicationConstants.ELEMENTS;
import static org.cmdbuild.servlets.json.ComunicationConstants.ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.SUBJECT;
import static org.cmdbuild.servlets.json.ComunicationConstants.TO;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.logic.email.EmailTemplateLogic.Template;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class EmailTemplate extends JSONBaseWithSpringContext {

	private static class JsonTemplate implements Template {

		private Long id;
		private String name;
		private String description;
		private String from;
		private String to;
		private String cc;
		private String bcc;
		private String subject;
		private String body;

		@Override
		@JsonProperty(ID)
		public Long getId() {
			return id;
		}

		public void setId(final Long id) {
			this.id = id;
		}

		@Override
		@JsonProperty(NAME)
		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}

		@Override
		@JsonProperty(DESCRIPTION)
		public String getDescription() {
			return description;
		}

		public void setDescription(final String description) {
			this.description = description;
		}

		@Override
		public String getFrom() {
			return from;
		}

		public void setFrom(final String from) {
			this.from = from;
		}

		@Override
		@JsonProperty(TO)
		public String getTo() {
			return to;
		}

		public void setTo(final String to) {
			this.to = to;
		}

		@Override
		@JsonProperty(CC)
		public String getCc() {
			return cc;
		}

		public void setCc(final String cc) {
			this.cc = cc;
		}

		@Override
		@JsonProperty(BCC)
		public String getBcc() {
			return bcc;
		}

		public void setBcc(final String bcc) {
			this.bcc = bcc;
		}

		@Override
		@JsonProperty(SUBJECT)
		public String getSubject() {
			return subject;
		}

		public void setSubject(final String subject) {
			this.subject = subject;
		}

		@Override
		@JsonProperty(BODY)
		public String getBody() {
			return body;
		}

		public void setBody(final String body) {
			this.body = body;
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

	}

	private static class JsonTemplates {

		private List<? super JsonTemplate> elements;

		@JsonProperty(ELEMENTS)
		public List<? super JsonTemplate> getElements() {
			return elements;
		}

		public void setElements(final Iterable<? extends JsonTemplate> elements) {
			this.elements = Lists.newArrayList(elements);
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

	}

	private static Function<Template, JsonTemplate> TEMPLATE_TO_JSON_TEMPLATE = new Function<Template, JsonTemplate>() {

		@Override
		public JsonTemplate apply(final Template input) {
			final JsonTemplate template = new JsonTemplate();
			template.setId(input.getId());
			template.setName(input.getName());
			template.setDescription(input.getDescription());
			template.setFrom(input.getFrom());
			template.setTo(input.getTo());
			template.setCc(input.getCc());
			template.setBcc(input.getBcc());
			template.setSubject(input.getSubject());
			template.setBody(input.getBody());
			return template;
		}

	};

	@JSONExported
	public JsonResponse readTemplates() {
		final Iterable<Template> elements = emailTemplateLogic().readAll();
		final JsonTemplates templates = new JsonTemplates();
		templates.setElements(from(elements) //
				.transform(TEMPLATE_TO_JSON_TEMPLATE));
		return JsonResponse.success(templates);
	}

	@JSONExported
	public JsonResponse readTemplate( //
			@Parameter(NAME) final String name //
	) {
		final Template element = emailTemplateLogic().read(name);
		return JsonResponse.success(TEMPLATE_TO_JSON_TEMPLATE.apply(element));
	}

	@JSONExported
	@Admin
	public JsonResponse createTemplate( //
			@Parameter(NAME) final String name, //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(TO) final String to, //
			@Parameter(CC) final String cc, //
			@Parameter(BCC) final String bcc, //
			@Parameter(SUBJECT) final String subject, //
			@Parameter(BODY) final String body //
	) {
		final JsonTemplate template = new JsonTemplate();
		template.setName(name);
		template.setDescription(description);
		template.setTo(to);
		template.setCc(cc);
		template.setBcc(bcc);
		template.setSubject(subject);
		template.setBody(body);
		final Long id = emailTemplateLogic().create(template);
		return JsonResponse.success(id);
	}

	@JSONExported
	@Admin
	public void updateTemplate( //
			@Parameter(NAME) final String name, //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(TO) final String to, //
			@Parameter(CC) final String cc, //
			@Parameter(BCC) final String bcc, //
			@Parameter(SUBJECT) final String subject, //
			@Parameter(BODY) final String body //
	) {
		final JsonTemplate template = new JsonTemplate();
		template.setName(name);
		template.setDescription(description);
		template.setTo(to);
		template.setCc(cc);
		template.setBcc(bcc);
		template.setSubject(subject);
		template.setBody(body);
		emailTemplateLogic().update(template);
	}

	@JSONExported
	@Admin
	public void deleteTemplate( //
			@Parameter(NAME) final String name //
	) {
		emailTemplateLogic().delete(name);
	}

}
