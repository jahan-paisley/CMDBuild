package org.cmdbuild.model.email;

import javax.activation.DataHandler;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.common.Builder;

public class Attachment {

	public static class AttachmentBuilder implements Builder<Attachment> {

		private String name;
		private DataHandler dataHandler;

		private AttachmentBuilder() {
			// prevents instantiation
		}

		@Override
		public Attachment build() {
			return new Attachment(this);
		}

		public AttachmentBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public AttachmentBuilder withDataHandler(final DataHandler dataHandler) {
			this.dataHandler = dataHandler;
			return this;
		}

	}

	public static AttachmentBuilder newInstance() {
		return new AttachmentBuilder();
	}

	private final String name;
	private final DataHandler dataHandler;

	private Attachment(final AttachmentBuilder builder) {
		this.name = builder.name;
		this.dataHandler = builder.dataHandler;
	}

	public String getName() {
		return name;
	}

	public DataHandler getDataHandler() {
		return dataHandler;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) //
				.append("name", name) //
				.toString();
	}

}
