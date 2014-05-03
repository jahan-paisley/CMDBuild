package unit.serializers.jackson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static utils.JsonMatchers.containsPair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.model.widget.OpenReport;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.model.widget.WidgetVisitor;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

public class WidgetSerializationTest {

	private static ObjectMapper mapper = new ObjectMapper();

	private static class EmptyWidget extends Widget {
		public static String TYPE = ".WidgetSerializationTest$EmptyWidget";

		@Override
		public void accept(final WidgetVisitor visitor) {
			// nothing to do
		}
	}

	@Test
	public void widgetsAreCreatedActiveWithEmptyLabel() {
		final Widget w = new EmptyWidget();
		assertEquals(StringUtils.EMPTY, w.getLabel());
		assertTrue(w.isActive());
	}

	@Test
	public void basicWidgetSerializationContainsBasicAttributesAndType() throws JsonParseException,
			JsonMappingException, IOException {
		final Long ID = 50L;
		final String LABEL = "Do Something Awesome";

		final Widget w = new EmptyWidget();
		w.setId(ID);
		w.setLabel(LABEL);
		w.setActive(false);

		final String jw = mapper.writeValueAsString(w);
		assertThat(jw, containsPair("type", EmptyWidget.TYPE));
		assertThat(jw, containsPair("id", Long.toString(ID)));
		assertThat(jw, containsPair("label", LABEL));
		assertThat(jw, containsPair("active", Boolean.FALSE));
	}

	@Test
	public void widgetListSerializationContansType() throws JsonParseException, JsonMappingException, IOException {
		final List<Widget> wl = new ArrayList<Widget>();
		wl.add(new EmptyWidget());

		final String jw = mapper.writeValueAsString(wl);
		assertThat(jw, containsPair("type", EmptyWidget.TYPE));
	}

	@Test
	public void reportSerialization() throws JsonParseException, JsonMappingException, IOException {
		final String FORMAT = "CSV";
		final String CODE = "BrilliantReport";
		final Map<String, Object> PRESET = new HashMap<String, Object>();
		PRESET.put("K1", "V1");
		PRESET.put("K2", "V2");
		final String jw = createOpenReportJson(CODE, FORMAT, PRESET);

		final Widget w = mapper.readValue(jw, Widget.class);
		assertEquals(OpenReport.class, w.getClass());
		final OpenReport orw = (OpenReport) w;
		assertEquals(FORMAT, orw.getForceFormat());
		assertEquals(CODE, orw.getReportCode());
		assertEquals(PRESET, orw.getPreset());

		assertEquals(jw, mapper.writeValueAsString(orw));
	}

	private String createOpenReportJson(final String CODE, final String FORMAT, final Map<String, Object> PRESET)
			throws IOException, JsonGenerationException, JsonMappingException {
		final OpenReport w = new OpenReport();
		w.setForceFormat(FORMAT);
		w.setReportCode(CODE);
		w.setPreset(PRESET);
		return mapper.writeValueAsString(w);
	}

}
