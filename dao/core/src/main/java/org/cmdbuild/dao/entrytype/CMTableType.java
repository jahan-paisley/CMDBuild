package org.cmdbuild.dao.entrytype;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CharAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;

/**
 * NOTE: the commented lines are the old reserved attributes (used by web
 * services only) FIX IT when soap layer will be integrated with the new dao
 */
public enum CMTableType {
	CLASS("class"), SIMPLECLASS("simpleclass"), DOMAIN("domain");

	private List<CMAttributeType<?>> classAttributes;

	@SuppressWarnings("serial")
	CMTableType(final String type) {
		classAttributes = new ArrayList<CMAttributeType<?>>() {
			{
				add(new BooleanAttributeType());
				add(new IntegerAttributeType());
				add(new DecimalAttributeType());
				add(new DoubleAttributeType());
				add(new DateAttributeType());
				add(new DateTimeAttributeType());
				add(new CharAttributeType());
				add(new StringAttributeType());
				add(new TextAttributeType());
				add(new LookupAttributeType("")); // FIXME
				add(new IpAddressAttributeType());
				add(new TimeAttributeType());
				// add(AttributeType.REGCLASS);
				// add(AttributeType.BINARY);
				// add(AttributeType.INTARRAY);
				// add(AttributeType.STRINGARRAY);
				if ("class".equals(type)) {
					add(new ReferenceAttributeType("")); // FIXME if it needs
				} else if ("simpleclass".equals(type)) {
					add(new ForeignKeyAttributeType("")); // FIXME
					// add(AttributeType.POINT);
					// add(AttributeType.LINESTRING);
					// add(AttributeType.POLYGON);
				}
			}
		};
	}

	public List<CMAttributeType<?>> getAvaiableAttributeList() {
		return this.classAttributes;
	}
}
