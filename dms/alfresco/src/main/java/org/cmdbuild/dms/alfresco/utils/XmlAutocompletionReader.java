package org.cmdbuild.dms.alfresco.utils;

import static com.google.common.collect.Collections2.transform;

import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.cmdbuild.dms.DmsService.LoggingSupport;
import org.cmdbuild.dms.MetadataAutocompletion.AutocompletionRules;
import org.cmdbuild.dms.MetadataAutocompletion.Reader;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

public class XmlAutocompletionReader implements Reader, LoggingSupport {

	private final String content;

	public XmlAutocompletionReader(final String content) {
		this.content = content;
	}

	@Override
	public AutocompletionRules read() {
		try {
			logger.warn("parsing autocompletion rules");
			final Autocompletion data = parseAutocompletion();
			return toAutocompletionRules(data);
		} catch (final Exception e) {
			logger.warn("error parsing content", e);
			throw new RuntimeException(e);
		}
	}

	private Autocompletion parseAutocompletion() throws JAXBException {
		final JAXBContext context = JAXBContext.newInstance(Autocompletion.class);
		final Unmarshaller unmarshaller = context.createUnmarshaller();
		final JAXBElement<Autocompletion> element = unmarshaller.unmarshal(contentStreamSource(), Autocompletion.class);
		return element.getValue();
	}

	private AutocompletionRules toAutocompletionRules(final Autocompletion data) {
		return new AutocompletionRulesAdapter(data);
	}

	private Source contentStreamSource() {
		return new StreamSource(new StringReader(content));
	}

	private static class AutocompletionRulesAdapter implements AutocompletionRules {

		private final Autocompletion autocompletion;

		public AutocompletionRulesAdapter(final Autocompletion autocompletion) {
			this.autocompletion = autocompletion;
		}

		@Override
		public Iterable<String> getMetadataGroupNames() {
			return transform(autocompletion.getGroups(), new Function<Group, String>() {
				@Override
				public String apply(final Group input) {
					return input.getName();
				}
			});
		}

		@Override
		public Iterable<String> getMetadataNamesForGroup(final String groupName) {
			return transform(group(groupName).getMetadata(), new Function<Metadata, String>() {
				@Override
				public String apply(final Metadata input) {
					return input.getName();
				}
			});
		}

		@Override
		public Map<String, String> getRulesForGroupAndMetadata(final String groupName, final String metadataName) {
			final Map<String, String> rules = Maps.newHashMap();
			for (final Rule rule : metadata(groupName, metadataName).getRules()) {
				rules.put(rule.getClassName(), rule.getValue());
			}
			return rules;
		}

		private Metadata metadata(final String groupName, final String metadataName) {
			for (final Metadata metadata : group(groupName).getMetadata()) {
				if (metadata.getName().equals(metadataName)) {
					return metadata;
				}
			}
			return Metadata.from(metadataName);
		}

		private Group group(final String groupName) {
			for (final Group group : autocompletion.getGroups()) {
				if (group.getName().equals(groupName)) {
					return group;
				}
			}
			return Group.from(groupName);
		}

	}

	/*
	 * Classes for JAXB
	 */

	@XmlRootElement
	@XmlType
	private static class Autocompletion {

		@XmlElementWrapper(name = "metadataGroups")
		@XmlElement(name = "metadataGroup")
		private List<Group> groups;

		public List<Group> getGroups() {
			return groups;
		}

	}

	private static class Group {

		@XmlAttribute(required = true)
		private String name;

		@XmlElement
		private List<Metadata> metadata;

		public String getName() {
			return name;
		}

		public List<Metadata> getMetadata() {
			return metadata;
		}

		public static Group from(final String groupName) {
			final Group group = new Group();
			group.name = groupName;
			group.metadata = Collections.emptyList();
			return group;
		}

	}

	private static class Metadata {

		@XmlAttribute(required = true)
		private String name;

		@XmlElementWrapper(name = "rules")
		@XmlElement(name = "rule")
		private List<Rule> rules;

		public String getName() {
			return name;
		}

		public List<Rule> getRules() {
			return rules;
		}

		public static Metadata from(final String metadataName) {
			final Metadata metadata = new Metadata();
			metadata.name = metadataName;
			metadata.rules = Collections.emptyList();
			return metadata;
		}

	}

	private static class Rule {

		@XmlAttribute(required = true)
		private String classname;

		@XmlAttribute(required = true)
		private String value;

		public String getClassName() {
			return classname;
		}

		public String getValue() {
			return value;
		}

	}

}
