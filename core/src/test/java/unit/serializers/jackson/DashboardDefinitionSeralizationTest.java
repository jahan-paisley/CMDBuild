package unit.serializers.jackson;

import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static utils.JsonMatchers.containsArrayWithKey;
import static utils.JsonMatchers.containsKey;
import static utils.JsonMatchers.containsObjectWithKey;
import static utils.JsonMatchers.containsPair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.cmdbuild.model.dashboard.ChartDefinition;
import org.cmdbuild.model.dashboard.ChartDefinition.ChartInput;
import org.cmdbuild.model.dashboard.DashboardDefinition;
import org.cmdbuild.model.dashboard.DashboardDefinition.DashboardColumn;
import org.cmdbuild.model.dashboard.DashboardObjectMapper;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Test;

public class DashboardDefinitionSeralizationTest {

	private static final ObjectMapper mapper = new DashboardObjectMapper();

	/*
	 * ChartInput
	 */
	@Test
	public void chartInputSerialization() throws JsonGenerationException, JsonMappingException, IOException {
		final String name = "the name", type = "the type", fieldType = "the field type", defaultValue = "the default value", lookupType = "the lookup type", className = "the class name";

		final ChartInput ci = new ChartInput();

		String jw = mapper.writeValueAsString(ci);

		assertThat(jw, not(containsKey("name")));
		assertThat(jw, not(containsKey("type")));
		assertThat(jw, not(containsKey("fieldType")));
		assertThat(jw, not(containsKey("defaultValue")));
		assertThat(jw, not(containsKey("lookupType")));
		assertThat(jw, not(containsKey("className")));

		ci.setName(name);
		ci.setType(type);
		ci.setFieldType(fieldType);
		ci.setDefaultValue(defaultValue);
		ci.setLookupType(lookupType);
		ci.setClassName(className);

		jw = mapper.writeValueAsString(ci);

		assertThat(jw, containsPair("name", name));
		assertThat(jw, containsPair("type", type));
		assertThat(jw, containsPair("fieldType", fieldType));
		assertThat(jw, containsPair("defaultValue", defaultValue));
		assertThat(jw, containsPair("lookupType", lookupType));
		assertThat(jw, containsPair("className", className));
	}

	@Test
	public void chartInputDeserialization() throws JsonGenerationException, JsonMappingException, IOException {
		String jci = "{}";
		ChartInput ci = mapper.readValue(jci, ChartInput.class);

		assertNull(ci.getName());
		assertNull(ci.getDefaultValue());
		assertNull(ci.getFieldType());
		assertNull(ci.getLookupType());
		assertNull(ci.getClassName());
		assertNull(ci.getType());

		jci = "{" + "\"name\": \"the name\"," + "\"type\": \"the type\"," + "\"fieldType\": \"the fieldType\","
				+ "\"defaultValue\": \"the defaultValue\"," + "\"lookupType\": \"the lookupType\","
				+ "\"className\": \"the className\"" + "}";

		ci = mapper.readValue(jci, ChartInput.class);

		assertEquals(ci.getName(), "the name");
		assertEquals(ci.getDefaultValue(), "the defaultValue");
		assertEquals(ci.getFieldType(), "the fieldType");
		assertEquals(ci.getLookupType(), "the lookupType");
		assertEquals(ci.getClassName(), "the className");
		assertEquals(ci.getType(), "the type");
	}

	/*
	 * DashboardColumn
	 */
	@Test
	public void dashboardColumnSerialization() throws JsonGenerationException, JsonMappingException, IOException {
		final int width = 100;
		final ArrayList<String> charts = new ArrayList<String>();

		charts.add("a");
		charts.add("b");

		final DashboardColumn dc = new DashboardColumn();

		String jDc = mapper.writeValueAsString(dc);

		assertThat(jDc, containsPair("width", 0));
		assertThat(jDc, not(containsKey("charts")));

		dc.setWidth(width);
		dc.setCharts(charts);

		jDc = mapper.writeValueAsString(dc);

		assertThat(jDc, containsPair("width", width));
		assertThat(jDc, containsArrayWithKey("[\"a\",\"b\"]", "charts"));
	}

	@Test
	public void dashboardColumnDeserialization() throws JsonGenerationException, JsonMappingException, IOException {
		String jdc = "{}";
		DashboardColumn dc = mapper.readValue(jdc, DashboardColumn.class);

		assertEquals(new Float(0), new Float(dc.getWidth()));
		assertEquals(dc.getCharts().size(), 0);

		jdc = "{" + "\"width\": 0.6," + "\"charts\": [\"asdf\", \"fdsa\"]" + "}";

		dc = mapper.readValue(jdc, DashboardColumn.class);
		assertEquals(new Float(0.6), new Float(dc.getWidth()));
		assertEquals(2, dc.getCharts().size());

		jdc = "[" + jdc + "]";

		final ArrayList<DashboardColumn> columns = mapper.readValue(jdc,
				new TypeReference<ArrayList<DashboardColumn>>() {
				}); // to say to the object matter that the
					// contains DashboardColumns

		assertEquals(new Float(0.6), new Float(columns.get(0).getWidth()));
		assertEquals(2, columns.get(0).getCharts().size());
	}

	/*
	 * ChartDefinition
	 */
	@Test
	public void chartStringAttributesSerialization() throws JsonGenerationException, JsonMappingException, IOException {
		final String name = "theName", description = "theDescription", dataSourceName = "theDataSourceName", type = "theType", singleSeriesField = "theSingleSeriesField", labelField = "theLabelField", categoryAxisField = "theCategoryAxisField", categoryAxisLabel = "theCategoryAxisLabel", valueAxisLabel = "theValueAxisLabel", fgcolor = "theFgColor", bgcolor = "theBgColor", chartOrientation = "theChartOrientation";

		final ChartDefinition c = new ChartDefinition();
		String jw = mapper.writeValueAsString(c);

		assertThat(jw, not(containsKey("name")));
		assertThat(jw, not(containsKey("description")));
		assertThat(jw, not(containsKey("dataSourceName")));
		assertThat(jw, not(containsKey("type")));
		assertThat(jw, not(containsKey("singleSeriesField")));
		assertThat(jw, not(containsKey("labelField")));
		assertThat(jw, not(containsKey("categoryAxisField")));
		assertThat(jw, not(containsKey("categoryAxisLabel")));
		assertThat(jw, not(containsKey("valueAxisLabel")));
		assertThat(jw, not(containsKey("fgcolor")));
		assertThat(jw, not(containsKey("bgcolor")));
		assertThat(jw, not(containsKey("chartOrientation")));

		c.setName(name);
		c.setDescription(description);
		c.setDataSourceName(dataSourceName);
		c.setType(type);
		c.setSingleSeriesField(singleSeriesField);
		c.setLabelField(labelField);
		c.setCategoryAxisField(categoryAxisField);
		c.setCategoryAxisLabel(categoryAxisLabel);
		c.setValueAxisLabel(valueAxisLabel);
		c.setFgcolor(fgcolor);
		c.setBgcolor(bgcolor);
		c.setChartOrientation(chartOrientation);

		jw = mapper.writeValueAsString(c);

		assertThat(jw, containsPair("name", name));
		assertThat(jw, containsPair("description", description));
		assertThat(jw, containsPair("dataSourceName", dataSourceName));
		assertThat(jw, containsPair("type", type));
		assertThat(jw, containsPair("singleSeriesField", singleSeriesField));
		assertThat(jw, containsPair("labelField", labelField));
		assertThat(jw, containsPair("categoryAxisField", categoryAxisField));
		assertThat(jw, containsPair("categoryAxisLabel", categoryAxisLabel));
		assertThat(jw, containsPair("valueAxisLabel", valueAxisLabel));
		assertThat(jw, containsPair("fgcolor", fgcolor));
		assertThat(jw, containsPair("bgcolor", bgcolor));
		assertThat(jw, containsPair("chartOrientation", chartOrientation));
	}

	@Test
	public void chartBoolaenAttributesSerialization() throws JsonGenerationException, JsonMappingException, IOException {
		final ChartDefinition c = new ChartDefinition();

		String jw = mapper.writeValueAsString(c);

		assertThat(jw, containsPair("active", false));
		assertThat(jw, containsPair("autoLoad", false));
		assertThat(jw, containsPair("legend", false));

		c.setLegend(true);
		c.setActive(true);
		c.setAutoLoad(true);

		jw = mapper.writeValueAsString(c);

		assertThat(jw, containsPair("active", true));
		assertThat(jw, containsPair("autoLoad", true));
		assertThat(jw, containsPair("legend", true));
	}

	@Test
	public void chartIntAttributesSerialization() throws JsonGenerationException, JsonMappingException, IOException {
		final int height = 10, maximum = 100, minimum = 1, steps = 20;

		final ChartDefinition c = new ChartDefinition();

		String jw = mapper.writeValueAsString(c);

		assertThat(jw, containsPair("height", 0));
		assertThat(jw, containsPair("maximum", 0));
		assertThat(jw, containsPair("minimum", 0));
		assertThat(jw, containsPair("steps", 0));

		c.setHeight(height);
		c.setMaximum(maximum);
		c.setMinimum(minimum);
		c.setSteps(steps);

		jw = mapper.writeValueAsString(c);

		assertThat(jw, containsPair("height", height));
		assertThat(jw, containsPair("maximum", maximum));
		assertThat(jw, containsPair("minimum", minimum));
		assertThat(jw, containsPair("steps", steps));
	}

	@Test
	public void chartArrayListAttributesSerialization() throws JsonGenerationException, JsonMappingException,
			IOException {
		final ChartDefinition cd = new ChartDefinition();

		String jdc = mapper.writeValueAsString(cd);

		assertThat(jdc, not(containsKey("dataSourceParameters")));
		assertThat(jdc, not(containsKey("valueAxisFields")));

		final ChartInput ci = new ChartInput();
		ci.setName("inputName");

		cd.addValueAxisField("a field");
		cd.addValueAxisField("a second field");
		cd.addDataSourceParameter(ci);

		jdc = mapper.writeValueAsString(cd);

		assertThat(jdc, containsArrayWithKey("[\"a field\",\"a second field\"]", "valueAxisFields"));
		assertThat(jdc, containsArrayWithKey("[{\"name\":\"inputName\",\"required\":false}]", "dataSourceParameters"));
	}

	@Test
	public void chartDefinitionDeserialization() throws JsonGenerationException, JsonMappingException, IOException {
		String jcd = "{}";
		ChartDefinition cd = mapper.readValue(jcd, ChartDefinition.class);

		assertNull(cd.getName());
		assertNull(cd.getDescription());
		assertNull(cd.getDataSourceName());
		assertNull(cd.getType());
		assertNull(cd.getSingleSeriesField());
		assertNull(cd.getLabelField());
		assertNull(cd.getCategoryAxisField());
		assertNull(cd.getCategoryAxisLabel());
		assertNull(cd.getValueAxisLabel());
		assertNull(cd.getBgcolor());
		assertNull(cd.getFgcolor());
		assertNull(cd.getChartOrientation());

		assertFalse(cd.isActive());
		assertFalse(cd.isAutoLoad());
		assertFalse(cd.isLegend());

		assertEquals(0, cd.getHeight());
		assertEquals(0, cd.getMaximum());
		assertEquals(0, cd.getMinimum());
		assertEquals(0, cd.getSteps());

		assertEquals(0, cd.getDataSourceParameters().size());
		assertEquals(0, cd.getValueAxisFields().size());

		jcd = "{" + "\"name\": \"the name\"," + "\"description\": \"the descr\","
				+ "\"dataSourceName\": \"the dsName\"," + "\"type\": \"the type\","
				+ "\"singleSeriesField\": \"the single serie field\"," + "\"labelField\": \"the label field\","
				+ "\"categoryAxisField\": \"the category axis field\","
				+ "\"categoryAxisLabel\": \"the category axis label\","
				+ "\"valueAxisLabel\": \"the value axis label\"," + "\"fgcolor\": \"the fgcolor\","
				+ "\"bgcolor\": \"the bgcolor\"," + "\"chartOrientation\": \"the chart orientation\","
				+ "\"active\": \"true\"," + "\"autoLoad\": \"true\"," + "\"legend\": \"true\"," + "\"height\": 100,"
				+ "\"maximum\": \"100\"," + "\"minimum\": \"100\"," + "\"steps\": \"100\","
				+ "\"valueAxisFields\": [\"Foo\",\"Bar\"]," + "\"dataSourceParameters\": [{\"name\": \"Foo\"}]" + "}";

		cd = mapper.readValue(jcd, ChartDefinition.class);

		assertEquals(cd.getName(), "the name");
		assertEquals(cd.getDescription(), "the descr");
		assertEquals(cd.getDataSourceName(), "the dsName");
		assertEquals(cd.getType(), "the type");
		assertEquals(cd.getSingleSeriesField(), "the single serie field");
		assertEquals(cd.getLabelField(), "the label field");
		assertEquals(cd.getCategoryAxisField(), "the category axis field");
		assertEquals(cd.getCategoryAxisLabel(), "the category axis label");
		assertEquals(cd.getValueAxisLabel(), "the value axis label");
		assertEquals(cd.getFgcolor(), "the fgcolor");
		assertEquals(cd.getBgcolor(), "the bgcolor");
		assertEquals(cd.getChartOrientation(), "the chart orientation");

		assertTrue(cd.isActive());
		assertTrue(cd.isAutoLoad());
		assertTrue(cd.isLegend());

		assertEquals(100, cd.getHeight());
		assertEquals(100, cd.getMaximum());
		assertEquals(100, cd.getMinimum());
		assertEquals(100, cd.getSteps());
		assertEquals(100, cd.getSteps());

		assertEquals(2, cd.getValueAxisFields().size());
		assertTrue(cd.getValueAxisFields().contains("Foo"));
		assertTrue(cd.getValueAxisFields().contains("Bar"));

		assertEquals(1, cd.getDataSourceParameters().size());
		assertEquals(ChartInput.class, cd.getDataSourceParameters().get(0).getClass());
		assertEquals("Foo", cd.getDataSourceParameters().get(0).getName());
	}

	/*
	 * DashboardDefinition
	 */
	@Test
	public void dashboardDefinitionSerialization() throws JsonGenerationException, JsonMappingException, IOException {
		final DashboardDefinition dd = new DashboardDefinition();

		String jDd = mapper.writeValueAsString(dd);

		assertThat(jDd, not(containsKey("name")));
		assertThat(jDd, not(containsKey("description")));
		assertThat(jDd, not(containsKey("charts")));
		assertThat(jDd, not(containsKey("columns")));
		assertThat(jDd, not(containsKey("groups")));

		dd.setName("the name");
		dd.setDescription("the description");
		dd.addChart("key", new ChartDefinition());
		dd.addColumn(new DashboardColumn());
		dd.addGroup("the group");

		jDd = mapper.writeValueAsString(dd);

		assertThat(jDd, containsPair("name", "the name"));
		assertThat(jDd, containsPair("description", "the description"));
		assertThat(jDd, containsObjectWithKey("{" + "\"key\":{" + "\"active\":false," + "\"autoLoad\":false,"
				+ "\"legend\":false," + "\"height\":0," + "\"maximum\":0," + "\"minimum\":0," + "\"steps\":0" + "}"
				+ "}", "charts"));

		assertThat(jDd, containsArrayWithKey("[{\"width\":0.0}]", "columns"));
		assertThat(jDd, containsArrayWithKey("[\"the group\"]", "groups"));
	}

	@Test
	public void dashboardDefinitionDeserialization() throws JsonGenerationException, JsonMappingException, IOException {
		String jdd = "{}";
		DashboardDefinition dd = mapper.readValue(jdd, DashboardDefinition.class);

		assertNull(dd.getName());
		assertNull(dd.getDescription());
		assertEquals(0, dd.getGroups().size());
		assertEquals(0, dd.getColumns().size());
		assertEquals(0, dd.getCharts().size());

		jdd = "{" + "\"name\":\"the name\"," + "\"description\":\"the description\","
				+ "\"charts\":{\"key\":{\"name\":\"a chart\"}}," + "\"groups\":[\"a group\", \"another group\"],"
				+ "\"columns\":[{\"width\":0.3,\"charts\":[\"key\"]}]" + "}";

		dd = mapper.readValue(jdd, DashboardDefinition.class);
		assertEquals("the name", dd.getName());
		assertEquals("the description", dd.getDescription());

		final ArrayList<String> groups = dd.getGroups();
		assertEquals(2, groups.size());
		assertEquals("a group", groups.get(0));
		assertEquals("another group", groups.get(1));

		final ArrayList<DashboardColumn> columns = dd.getColumns();
		assertEquals(1, columns.size());
		assertEquals(new Float(0.3), new Float(columns.get(0).getWidth()));

		final LinkedHashMap<String, ChartDefinition> charts = dd.getCharts();
		assertEquals(1, charts.size());
		assertEquals("a chart", charts.get("key").getName());

	}

}
