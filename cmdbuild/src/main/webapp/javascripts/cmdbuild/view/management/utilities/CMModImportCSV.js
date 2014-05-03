(function() {

	var tr = CMDBuild.Translation.management.modutilities.csv;

	Ext.define("CMDBuild.view.management.utilities.CMModImportCSV", {
		extend: "Ext.panel.Panel",
		cmName: 'importcsv',
		layout: 'border',
		hideMode:  'offsets',
		frame: true,
		border: false,
	
		initComponent: function() {
			this.form = new CMDBuild.view.management.utilities.CMModImportCSV.UploadForm({
				region: "center",
				frame: true
			});

			this.grid = new CMDBuild.view.management.utilities.CMModImportCSV.Grid({
				region: "south",
				height: "60%",
				split: true
			});

			Ext.apply(this, {
				title: tr.title,
				items:[this.form, this.grid],
				buttonAlign: "center",
				buttons: [
					this.updateButton = new CMDBuild.buttons.UpdateButton(),
					this.confirmButton = new CMDBuild.buttons.ConfirmButton(),
					this.abortButton = new CMDBuild.buttons.AbortButton()
				]
			});

			this.callParent(arguments);
		}
	});
	
	
	Ext.define("CMDBuild.view.management.utilities.CMModImportCSV.UploadForm", {
		extend: "Ext.form.Panel",
		mixins: {
			cmFormFunctions: "CMDBUild.view.common.CMFormFunctions"
		},
		constructor: function() {
	
			this.classList = new CMDBuild.field.CMBaseCombo({
				store: _CMCache.getClassesStore(),
				labelWidth: CMDBuild.LABEL_WIDTH,
				fieldLabel : tr.selectaclass,
				width: 260,
				name : 'idClass',
				valueField : 'id',
				displayField : 'description',
				queryMode: "local",
				allowBlank : false,
				editable: false
			});

			this.uploadButton = new Ext.Button({
				 text: CMDBuild.Translation.common.buttons.upload,
				 scope: this
			 });

			Ext.apply(this, {
				encoding: 'multipart/form-data',
				fileUpload: true,
				monitorValid: true,
				items: [
					this.classList,
				{
					xtype: 'filefield',
					width: CMDBuild.BIG_FIELD_ONLY_WIDTH,
					labelWidth: CMDBuild.LABEL_WIDTH,
					fieldLabel: tr.csvfile,
					allowBlank: false,
					name: 'filecsv'
				},

				new Ext.form.ComboBox({ 
					name: 'separator',
					fieldLabel: tr.separator,
					labelWidth: CMDBuild.LABEL_WIDTH,
					valueField: 'value',
					displayField: 'value',
					hiddenName: 'separator',
					store: new Ext.data.SimpleStore({
						fields: ['value'],
						data : [[';'],[','],['|']]
					}),
					width: 200,
					value: ";",
					queryMode: 'local',
					editable: false,
					allowBlank: false
				})],
				buttonAlign: "center",
				buttons: [this.uploadButton]
			});

			this.callParent(arguments);
		}
	});

	var OBJECT_VALUES = "__objectValues__",
		ID = "Id",
		CLASS_ID = "IdClass",
		CLASS_DESCRIPTION = "IdClass_value",
		WRONG_FIELDS = "not_valid_values",
		CARD = "card";

	Ext.define("CMDBuild.view.management.utilities.CMModImportCSV.Grid", {
		extend: "CMDBuild.view.management.common.CMCardGrid",

		cmPaginate: false,
		cmAddGraphColumn: false,

		constructor: function() {
			this.cellEditing = Ext.create('Ext.grid.plugin.CellEditing', {
				clicksToEdit : 1,
				listeners: {
					/*
					 * eventObj.record = the Ext.model.Model for the row
					 * eventObj.field = the name of the column
					 * eventObj.value = the value to pass to the editor
					 */
					beforeedit: function(editor, eventObj) {
						var storedValue = eventObj.record.get(eventObj.field);
						if (!storedValue) {
							eventObj.value = null;
						}
					},
					edit: function(editor, eventObj) {
						var value = eventObj.value;
						var oldValue = eventObj.originalValue;

						// To deny to set a string as value
						// if enter in editing for a ComboBox and
						// then leave the field without select an item
						if (typeof oldValue == "object"
							&& typeof value != "object"
							&& oldValue.id == value) {

								eventObj.record.set(eventObj.field, oldValue);
								return;
							}

						// prevent the red triangle if enter in
						// editing for a date and leave the field
						// without change something
						if (isADate(value) 
							&& typeof oldValue == "string"
							&& formatDate(value) == oldValue) {

								eventObj.record.set(eventObj.field, oldValue);
								return;
							}
					}
				}
			});

			this.validFlag = new Ext.form.Checkbox({
				hideLabel: true,
				boxLabel: tr.shownonvalid,
				checked: false,
				scope: this,
				handler: function(obj, checked) {
					this.filterStore();
				}
			});

			var me = this;
			this.searchField = new CMDBuild.field.LocaleSearchField({
				grid: me,
				onTrigger1Click: function() {
					me.filterStore();
				}
			});

			this.store = new Ext.data.Store({
				fields:[],
				data: []
			});

			this.columns = [];
			this.bbar = [this.searchField, "-", this.validFlag];
			this.plugins = [this.cellEditing];
			this.callParent(arguments);
		},

		/*
		 * rawData is an array of object
		 * {
		 * 	card: {...}
		 * 	not_valid_fields: {...}
		 * }
		 */
		loadData: function(rawData) {
			var records = [];
			for (var	i=0,
						l=rawData.length,
						r=null,
						card=null; i<l; ++i) {

				r = rawData[i];
				card = r[CARD];
				card[WRONG_FIELDS] = r[WRONG_FIELDS];

				records.push(new CMDBuild.DummyModel(card));
			}

			Ext.suspendLayouts();
			this.store.loadRecords(records);
			Ext.resumeLayouts(true);
		},

		filterStore: function() {
			var me = this;

			this.store.clearFilter(false);
			var nonValid = this.validFlag.getValue();
			var query = this.searchField.getRawValue().toUpperCase();

			if (query == "") {
				if (nonValid) {
					this.store.filterBy(isInvalid, me);
				}
			} else {
				if (nonValid) {
					this.store.filterBy(isInvalidAndFilterQuery, me);
				} else {
					this.store.filterBy(filterQuery, me);
				}
			}
		},

		//override
		loadAttributes: function(classId, cb) {
			var me = this;
			var parameterNames = CMDBuild.ServiceProxy.parameter;
			var params = {};
			params[parameterNames.ACTIVE] = true;
			params[parameterNames.CLASS_NAME] = _CMCache.getEntryTypeNameById(classId);

			CMDBuild.ServiceProxy.attributes.read({
				params: params,
				success: function(response, options, result) {
					var attributes = result.attributes;
					attributes.sort( //
						function(a,b) { //
							return a.index - b.index;
						} //
					);

					if (cb) {
						cb(attributes);
					}

				}
			});
		},

		//override
		setColumnsForClass: function(classAttributes) {
			this.classAttributes = classAttributes;
		},

		//override
		getStoreForFields: function(fields) {
			fields.push(ID);
			fields.push(CLASS_ID);
			fields.push(CLASS_DESCRIPTION);

			return new Ext.data.Store({
				fields: fields,
				data: [],
				autoLoad: false
			});
		},

		configureHeadersAndStore: function(headersToShow) {
			var grid = this;
			var headers = [];
			var fields = [];

			for (var i=0, l=headersToShow.length; i<l; i++) {

				var a = getClassAttributeByName(this, headersToShow[i]);

				if (a != null) {
					var _attribute = Ext.apply({}, a);
					_attribute.fieldmode = "write";
					var header = CMDBuild.Management.FieldManager.getHeaderForAttr(_attribute);
					var editor = CMDBuild.Management.FieldManager.getCellEditorForAttribute(_attribute);
					editor.hideLabel = true;

					if (a.type == "REFERENCE"
							|| a.type == "LOOKUP") {

						editor.on("select", updateStoreRecord, this);
						editor.on("cmdbuild-reference-selected", function(record, field) {
							updateStoreRecord.call(this, field, record);
						}, this);
					}

					if (header) {
						header.field = editor;
						header.hidden = false;
						header.renderer = Ext.Function.bind(renderer, a,[header.dataIndex, grid],true);
						headers.push(header);
						fields.push(header.dataIndex);
					}
				}
			}

			// Add a field to use to read the real value set by
			// the editors.
			fields.push(OBJECT_VALUES);

			this.reconfigure(this.getStoreForFields(fields), headers);
		},

		getRecordToUpload: function() {
			var data = [];
			var records = this.store.data.items || [];

			for (var i=0, l=records.length, r=null; i<l; ++i) {
				r = records[i];

				var currentData = {};
				var objectValues = r.data[OBJECT_VALUES] || {};
				var wrongFields = r.get(WRONG_FIELDS);

				for (var j=0; j<this.classAttributes.length; j++) {
					var name = this.classAttributes[j].name;
					var value = objectValues[name] || r.data[name] || wrongFields[name];

					if (value) {
						currentData[name] = value;
					}
				}

				currentData[ID] = r.get(ID);
				currentData[CLASS_ID] = r.get(CLASS_ID);
				currentData[CLASS_DESCRIPTION] = r.get(CLASS_DESCRIPTION);

				data.push(currentData);
			}

			return data;
		},

		removeAll: function() {
			this.store.removeAll();
		}
	});

	function getClassAttributeByName(me, name) {
		for (var i=0, l=me.classAttributes.length; i<l; i++) {
			var classAttr = me.classAttributes[i];
			if (classAttr.name == name) {
				return classAttr;
			}
		}

		return null;
	}

	function isInvalid(record, id) {
		var invalidFields = record.get(WRONG_FIELDS);
		// return true if there are some invalid fields
		for (var i in invalidFields) {
			return true;
		}
		return false;
	}

	function filterQuery(record, id) {
		var query = this.searchField.getRawValue().toUpperCase();
		var data = Ext.apply({}, record.get(WRONG_FIELDS), record.data);
		var objectValues = record.data[OBJECT_VALUES] || {};

		for (var attributeName in data) {
			var value = objectValues[attributeName] || data[attributeName];
			var attributeAsString = "";
			var searchIndex = -1;

			if (typeof value == "object") {
				value = value.description;
			}
			attributeAsString = (value+"").toUpperCase();
			searchIndex = attributeAsString.search(query);
			if (searchIndex != -1) {
				return true;
			}
		}

		return false;
	}

	function isInvalidAndFilterQuery(record, id) {
		if (isInvalid(record, id)) {
			return filterQuery.call(this, record, id);
		} else {
			return false;
		}
	}

	function renderer(value, metadata, record, rowindex, collindex, store, grid, colName) {
		// look before if there is a object value, if not search it as simple value;
		var v = null;
		if (typeof value == "object") {
			v = value;
		} else {
			var objectValues = record.get(OBJECT_VALUES) || {};
			v = objectValues[colName]|| record.get(colName);
		}

		if (v && typeof v == "object") {
			if (isADate(v)) {
				v = formatDate(v); 
			} else {
				v = v.description;
			}
		}

		if (v) {
			return v;
		} else {
			var wrongs = record.get(WRONG_FIELDS);
			if (wrongs[colName]) {
				return	'<span class="importcsv-invalid-cell">' + wrongs[colName] + '</span>';
			} else {
				return	'<span class="importcsv-empty-cell"></span>';
			}
		}
	}

	function updateStoreRecord(field, selectedValue) {
		if (Ext.isArray(selectedValue)) {
			selectedValue = selectedValue[0];
		}

		var record = this.getSelectionModel().getSelection()[0];
		var objectValues = record.get(OBJECT_VALUES) || {};

		objectValues[field.name] = {
			description: selectedValue.get("Description"),
			id: selectedValue.get("Id")
		};

		record.set(OBJECT_VALUES, objectValues);

		return false; // to block the set value of the editor;
	}

	function formatDate(date) {
		var toString = "";

		var day = date.getDate();
		if (day < 10) {
			day = "0" + day;
		}
		toString += day + "/";

		var month = date.getMonth() + 1; // getMonth return 0-11
		if (month < 10) {
			month = "0"+month;
		}
		toString += month + "/" + date.getFullYear();

		return toString;
	}

	function isADate(v) {
		return (v && v.constructor && v.constructor.name == "Date");
	}
})();