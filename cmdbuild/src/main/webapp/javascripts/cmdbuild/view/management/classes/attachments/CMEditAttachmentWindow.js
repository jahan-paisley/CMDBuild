(function() {

	Ext.define("CMDBuild.view.management.CMEditAttachmentWindow", {
		extend: "Ext.window.Window",

		translation: CMDBuild.Translation.management.modcard.add_attachment_window,

		delegate: undefined, // set on creation
		attachmentRecord: undefined, // could be set on creation

		initComponent: function() {
			buildComponent(this);
			fillFields(this);
			this.callParent(arguments);

			updateTitle(this);
		},

		updateMetadataFieldsForCategory: function(category) {

			this.metadataContainer.removeAll();
			this.metadataContainer.hide();
			this.center();

			if (Ext.isArray(category)) {
				category = category[0];
			}

			if (!category) {
				return;
			}

			// if set by a user selection of the combo, category is a record
			// otherwise category is a string
			var categoryName = (typeof category == "string") ? category : category.get("name");

			if (categoryName) {
				var categoryModel = _CMCache.getAttachmentCategoryWithName(categoryName);
				if (categoryModel) {
					var metadataByGroups = categoryModel.getMetadataGroups();
					for (var i=0, group=null; i<metadataByGroups.length; ++i) {
						var fields = getFieldsForMetadataGroup(metadataByGroups[i], this.metadataValues);
						if (fields 
								&& Ext.isArray(fields)
								&& fields.length > 0) {
							this.metadataContainer.show();
							this.metadataContainer.add(fields);
							this.center();
						}
					}
				}
			}
		},

		getMetadataValues: function() {
			return this.metadataContainer.getMetadataValues();
		}
	});

	Ext.define("CMDBuild.view.management.CMEditAttachmentWindowDelegate", {
		/*
		 * called when click on the confirm button of the attachment window
		 */
		onConfirmButtonClick: Ext.emptyFn
	});

	Ext.define("CMDBuild.view.management.CMEditAttachmentWindowFieldContainer", {
		extend: "Ext.container.Container",

		cls: "cm_attachement_metadata_container",

		getMetadataValues: function() {
			var fields = this.items.getRange();
			var out = {};

			for (var i=0, f=null; i<fields.length; ++i) {
				f = fields[i];

				// create the group if necessary
				if (typeof out[f.cmMetadataGroupName] == "undefined") {
					out[f.cmMetadataGroupName] = {};
				}

				// add the value to the group
				out[f.cmMetadataGroupName][f.name] = f.getValue();
			}

			return out;
		}
	});

	/*
	 * Take a CMDBuild.model.CMMetadataGroup as input
	 * and return an array of Ext.form.field derived
	 * from the metadataDefinitions in the input
	 */
	function getFieldsForMetadataGroup(metadataGroup, metadataValues) {
		if (!metadataGroup) {
			return null;
		}

		var definitions = metadataGroup.getMetadataDefinitions();
		var groupName = metadataGroup.getName();
		var fields = [];

		for (var i=0, definition=null; i<definitions.length; ++i) {
			definition = definitions[i];
			var field = CMDBuild.Management.FieldManager.getFieldForAttr(
				adaptMetadataDefinitionForFieldManager(definition)
			);

			if (field) {
				field.cmMetadataGroupName = groupName;
				field.submitValue = false;

				// if present some values
				// for the metadata set it
				// this could happen if there are
				// autocompletion rules or if
				// we are editing an existing attachment
				if (metadataValues
						&& metadataValues[groupName]) {

					var value = metadataValues[groupName][field.name];
					if (value) {
						field.setValue(value);
					}
				}

				fields.push(field);
			}
		}

		return fields;
	};

	/*
	 * Take the serialization of a metadataDefinition
	 * as the server returns it, and adapt it to be used
	 * by the fieldManager
	 */
	function adaptMetadataDefinitionForFieldManager(definition) {
		var adapted = {
			name: definition.name,
			description: definition.description,
			isnotnull: definition.mandatory,
			type: definition.type
		};

		if (definition.type == "DECIMAL") {
			// Unbound the decimal digits
			adapted.scale = undefined;
			adapted.precision = undefined;
		}

		if (definition.type == "LIST") {
			adapted.values = definition.values;
		}

		return adapted;
	};

	function buildComponent(me) {
		me.metadataContainer = new CMDBuild.view.management.CMEditAttachmentWindowFieldContainer();

		var avaiableCategoryCombo = new Ext.form.ComboBox({
			labelAlign: "right",
			fieldLabel: me.translation.category,
			labelWidth: CMDBuild.LABEL_WIDTH,
			emptyText: me.translation.select_category,
			name: 'Category',
			store: _CMCache.getAttechmentCategoryStore(),
			valueField: "description",
			displayField: "description",
			triggerAction: 'all',
			allowBlank: false,
			forceSelection: true,
			queryMode: 'local',
			setValue: function(v) {
				Ext.form.ComboBox.prototype.setValue.call(this, v);
				me.updateMetadataFieldsForCategory(v);
			}
		});

		me.form = new Ext.form.Panel({
			encoding: 'multipart/form-data',
			fileUpload:true,
			frame: true,
			items: [
				avaiableCategoryCombo,
				{
					xtype: 'filefield',
					width: CMDBuild.BIG_FIELD_ONLY_WIDTH,
					labelAlign: "right",
					fieldLabel: me.translation.load_attachment,
					labelWidth: CMDBuild.LABEL_WIDTH,
					allowBlank: false,
					name: 'File'
				},{
					xtype: 'textarea',
					fieldLabel: me.translation.description,
					labelAlign: "right",
					labelWidth: CMDBuild.LABEL_WIDTH,
					name: 'Description',
					allowBlank: false,
					width: CMDBuild.BIG_FIELD_ONLY_WIDTH
				},
				me.metadataContainer
			]
		});

		me.title = me.translation.window_title;
		me.items = [me.form];
		me.autoScroll = true;
		me.autoHeight = true;
		me.modal = true;
		me.frame = false;
		me.border = false;
		me.buttonAlign = 'center';

		me.buttons = [
			new CMDBuild.buttons.ConfirmButton({
				scope: me,
				handler: function() {
					if (me.delegate && 
							typeof me.delegate.onConfirmButtonClick == "function") {

						me.delegate.onConfirmButtonClick(me);
					}
				}
			}), new CMDBuild.buttons.AbortButton({
				scope: me,
				handler: function() {
					me.destroy();
				}
			})
		];

		// auto width does not work for upload field
		if (Ext.isGecko) {
			me.width = 450;
		}
	}

	/*
	 * If there is the attachment record
	 * we are editing an existing attachment.
	 * Fill the category, filename and description.
	 * Disable the category and filename, because
	 * that fields can not be changed. 
	 */
	function fillFields(me) {
		if (!me.attachmentRecord) {
			return;
		} else {
			var form = me.form.getForm();

			var category = form.findField("Category");
			if (category) {
				category.setValue(me.attachmentRecord.get("Category"));
				category.disable();
			}

			var filename = form.findField("File");
			if (filename) {
				filename.setValue(me.attachmentRecord.get("Filename"));
				filename.disable();
			}

			var description = form.findField("Description");
			if (description) {
				description.setValue(me.attachmentRecord.get("Description"));
			}
		}
	}

	/*
	 * If there is the attachment record
	 * we are editing an existing attachment.
	 * Notify this to the user changing
	 * the title of the window and showing the
	 * file name
	 */
	function updateTitle(me) {
		if (!me.attachmentRecord) {
			return;
		}
		var title = CMDBuild.Translation.management.modcard.edit_attachment;
		var fileName = me.attachmentRecord.getFileName();

		if (fileName) {
			me.setTitle(Ext.String.format("{0}: {1}",title, fileName));
		}
	}
})();