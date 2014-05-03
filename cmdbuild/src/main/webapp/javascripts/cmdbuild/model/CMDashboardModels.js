Ext.define("CMDBuild.model.CMDashboard", {

	extend: 'Ext.data.Model',

	statics : {
		build: function(d) {
			d.charts = d.charts || {};
			var models = [];
			for (var id in d.charts) {
				var c = d.charts[id];
				c.id = id;
				models.push(CMDBuild.model.CMDashboardChart.build(c));
			}
	
			d.charts = models;

			return new CMDBuild.model.CMDashboard(d);
		}
	},

	fields: [
		{name : 'name', type : "string"},
		{name : 'description', type : "string"},
		{name : 'id', type : "int"},
		{name : 'groups', type: 'auto'},
		{name : 'charts', type: 'auto'},
		{name : 'columns', type: 'auto'}
	],

	constructor: function() {
		this.callParent(arguments);
		if (this.getColumns().length == 0) {
			var chartIDs = [];
			var charts = this.getCharts();
			for (var i=0, l=charts.length, chart; i<l; ++i) {
				chart = charts[i];
				if (chart) {
					chartIDs.push(chart.getId());
				}
			}

			this.setColumns([{
				width: 1,
				charts: chartIDs
			}]);
		}
	},

	getName: function() {
		return this.get("name");
	},

	getDescription: function() {
		return this.get("description");
	},

	getGroups: function() {
		return this.get("groups");
	},

	getCharts: function() {
		return this.get('charts') || [];
	},

	getChartWithId: function(id) {
		var charts = this.getCharts();

		for (var i=0, l=charts.length, chart; i<l; ++i) {
			chart = charts[i];
			if (chart.getId() == id) {
				return chart;
			}
		}

		return null;
	},

	getColumns: function() {
		var c = this.get("columns");
		return c || [];
	},

	setColumns: function(v) {
		this.set("columns", v);
	},

	setName: function(v) {
		this.set("name", v);
	},

	setDescription: function(v) {
		this.set("description", v);
	},

	setGroups: function(v) {
		this.set("groups", v);
	},

	setCharts: function(v) {
		this.set('charts', v);
	},

	addChart: function(chart) {
		this.getCharts().push(chart);

		// add the new chart to the first column
		var columns = this.getColumns();
		if (columns.length > 0) {
			columns[0].charts.push(chart.getId());
		}
	},

	removeChart: function(id) {
		var charts = this.getCharts();

		for (var i=0, l=charts.length, chart; i<l; ++i) {
			chart = charts[i];
			if (chart.getId() == id) {
				Ext.Array.erase(charts, i, 1);
				return;
			}
		}
	},

	replaceChart: function(id, replacement) {
		var charts = this.getCharts();

		for (var i=0, l=charts.length, chart; i<l; ++i) {
			chart = charts[i];
			if (chart.getId() == id) {
				charts[i] = replacement;
				delete chart;
				return;
			}
		}
	},

	toString: function() {
		return Ext.getClassName(this) + " " + this.getName();
	}
});


Ext.define("CMDBuild.model.CMDashboardChart", {

	extend: 'Ext.data.Model',

	statics: {
		build: function(c) {
			var ds = c.dataSource;
			if (ds) {
				c.dataSource = new CMDBuild.model.CMDashboardChartDataSource(ds);
			}

			return new CMDBuild.model.CMDashboardChart(c);
		}
	},

	fields: [
		// generic
		{name : 'id',type : 'string'},
		{name : 'name', type : 'string'},
		{name : 'description', type : 'string'},
		{name : 'active', type: 'boolean'},
		{name : 'autoLoad', type: 'boolean'},
		{name : 'legend', type: 'boolean'},
		{name : 'type',type : 'string'},
		{name : 'height',type : 'int'},
		{name : 'dataSourceName', type: 'string'},
		{name : 'dataSourceParameters', type: 'auto'},
		
		// datasource mapping
		{name : 'singleSeriesField', type : 'string'},
		{name : 'labelField', type : 'string'},
		{name : 'categoryAxisField', type : 'string'},
		{name : 'categoryAxisLabel', type : 'string'},
		{name : 'valueAxisFields', type : 'auto'},
		{name : 'valueAxisLabel', type : 'string'},

		// configuration
		{name : 'maximum',type : 'int'},
		{name : 'minimum',type : 'int'},
		{name : 'steps',type : 'int'},
		{name : 'fgcolor',type : 'string'},
		{name : 'bgcolor',type : 'string'},
		{name : 'classToUseForReferenceWidget',type : 'string'},
		{name : 'chartOrientation', type : 'string'}],

	getName: function() {
		return this.get('name');
	},

	getDescription: function() {
		return this.get('description');
	},

	isActive: function() {
		return this.get('active');
	},

	isAutoload: function() {
		return this.get('autoLoad');
	},

	withLegend: function() {
		return this.get('legend');
	},

	getType: function() {
		return this.get('type');
	},

	getheight: function() {
		return this.get('height');
	},

	getMaximum: function() {
		return this.get('maximum');
	},

	getMinimum: function() {
		return this.get('minimum');
	},

	getSteps: function() {
		return this.get('steps');
	},

	getFgColor: function() {
		return this.get('fgcolor');
	},

	getBgColor: function() {
		return this.get('bgcolor');
	},

	getChartOrientation: function() {
		return this.get("chartOrientation");
	},
	
	getSingleSeriesField: function() {
		return this.get('singleSeriesField');
	},

	getLabelField: function() {
		return this.get('labelField');
	},

	getCategoryAxisField: function() {
		return this.get("categoryAxisField");
	},

	getCategoryAxisLabel: function() {
		return this.get("categoryAxisLabel");
	},

	getValueAxisFields: function() {
		return this.get("valueAxisFields");
	},

	getValueAxisLabel: function() {
		return this.get("valueAxisLabel");
	},

	getDataSourceName: function() {
		return this.get("dataSourceName");
	},

	getDataSourceInputConfiguration: function() {
		return this.get("dataSourceParameters") || [];
	}
});

Ext.define("CMDBuild.model.CMDashboardChartDataSource", {
	extend: 'Ext.data.Model',
	fields: [
		{name : 'name',type : 'string'},
		{name : 'input', type: 'auto'},
		{name : 'output', type: 'auto'}
	],

	getName: function() {
		return this.get("name");
	},

	getInput: function() {
		return this.get("input");
	},

	getOutput: function() {
		return this.get("output");
	}
});