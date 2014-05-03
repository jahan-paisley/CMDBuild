(function() {
	var _fields = CMDBuild.model.CMWidgetDefinitionModel._FIELDS;

	Ext.define("CMDBuild.controller.administration.widget.CMWidgetDefinitionController", {

		mixins: {
			observable: 'Ext.util.Observable'
		},

		constructor: function(view) {
			this.view = view;

			this.mon(this.view, "select", onWidgetDefinitionSelect, this);
			this.mon(this.view, "deselect", onWidgetDefinitionDeselect, this);
			this.mon(this.view, "cm-add", onAddClick, this);
			this.mon(this.view, "cm-save", onSaveClick, this);
			this.mon(this.view, "cm-abort", onAbortClick, this);
			this.mon(this.view, "cm-remove", onRemoveClick, this);
			this.mon(this.view, "cm-modify", onModifyClick, this);
			this.mon(this.view, "cm-enable-modify", onEnableModify, this);
		},

		onClassSelected: function(classId) {
			this.classId = classId;
			this.view.reset(removeAllRecords = true);

			var me = this;
			var et = _CMCache.getEntryTypeById(classId);

			// BUSINESS RULE: currently the widgets are not inherited
			// so, deny the definition on superclasses
			if (et.get("superclass")) {
				this.view.disable();
			} else {
				this.view.enable();
				var widgets = et.getWidgets();

				for (var i=0, l=widgets.length, w; i<l; ++i) {
					w = widgets[i];
					addRecordToGrid(w, me);
				}
			}
		},

		onAddClassButtonClick: function() {
			this.view.disable();
		}
	});

	function addRecordToGrid(w, me) {
		me.view.addRecordToGrid(new CMDBuild.model.CMWidgetDefinitionModel(w));
	}

	function onAddClick(widgetName) {
		this.model = undefined;
		this.view.reset();
		buildSubController(this, widgetName, null, this.classId);
		if (this.subController) {
			this.view.enableModify();
			this.subController.setDefaultValues();
		}
		_CMCache.initAddingTranslations();
		var buttonLabel = this.view.query("#ButtonLabel")[0];
		buttonLabel.translationsKeyName = "";
	}

	function onWidgetDefinitionSelect(sm, record, index) {
		this.model = record;
		buildSubController(this, record.get("type"), record, this.classId);
	}

	function onWidgetDefinitionDeselect(sm, record, index) {
		this.model = undefined;
		delete this.subController;
	}

	function onSaveClick() {
		if (!this.subController) {
			return;
		}

		var me = this;
		var widgetDef = me.view.getWidgetDefinition();

		if (this.model) {
			widgetDef.id = this.model.get(_fields.id);
		}

		var params = {};
		params[_CMProxy.parameter.CLASS_NAME] = _CMCache.getEntryTypeNameById(me.classId);
		params[_CMProxy.parameter.WIDGET] = Ext.encode(widgetDef);

		CMDBuild.ServiceProxy.CMWidgetConfiguration.save({
			params: params,
			success: function success(response, operation, responseData) {
				var widgetModel = new CMDBuild.model.CMWidgetDefinitionModel(Ext.apply(responseData.response, {
					type: widgetDef.type
				}));

				_CMCache.onWidgetSaved(me.classId, widgetDef);
				me.view.addRecordToGrid(widgetModel, selectAfter = true);
				me.view.disableModify(enableToolBar = true);
				_CMCache.flushTranslationsToSave(widgetModel.get("id"));
			}
		});
	}

	function onAbortClick() {
		if (this.model) {
			this.view.disableModify(enableToolbar = true);
			this.subController.fillFormWithModel(this.model);
		} else {
			this.view.reset();
		}
	}

	function onRemoveClick() {
		var me = this;

		Ext.Msg.show({
			title: CMDBuild.Translation.common.buttons.remove,
			msg: CMDBuild.Translation.common.confirmpopup.areyousure,
			buttons: Ext.Msg.YESNO,
			fn: function(button) {
				if (button == "yes") {
					if (me.model && me.subController) {
						var id = me.model.get(_fields.id);
						var params = {};
						params[_CMProxy.parameter.CLASS_NAME] = _CMCache.getEntryTypeNameById(me.classId);
						params[_CMProxy.parameter.WIDGET_ID] = id;

						CMDBuild.ServiceProxy.CMWidgetConfiguration.remove({
							params: params,
							success: function() {
								me.view.removeRecordFromGrid(id);
								me.view.reset();
			
								_CMCache.onWidgetDeleted(me.classId, id);
			
								delete me.model;
								delete me.subController;
							}
						});
					}
				}
			}
		});
	}

	function buildSubController(me, widgetName, record, classId) {
		if (me.subController) {
			delete me.subController;
		}

		var subControllerClass = findController(widgetName);
		if (subControllerClass) {
			var subView = me.view.buildWidgetForm(widgetName);
			me.subController = subControllerClass.create({
				view: subView,
				classId: classId
			});

			if (record) {
				me.subController.fillFormWithModel(record);
			}
			me.view.disableModify(enableToolbar = true);
		} else {
			me.view.reset();
		}

		function findController(widgetName) {
			var controller = null;
			for (var key in CMDBuild.controller.administration.widget) {
				if (CMDBuild.controller.administration.widget[key].WIDGET_NAME == widgetName) {
					controller = CMDBuild.controller.administration.widget[key];
					break;
				}
			}

			return controller;
		}
	}

	function onModifyClick() {
		this.view.enableModify();
		_CMCache.initModifyingTranslations();
		var buttonLabel = this.view.query("#ButtonLabel")[0];
		buttonLabel.translationsKeyName = this.model.get("id");
	}

	function onEnableModify() {
		if (this.subController) {
			this.subController.afterEnableEditing()
		}
	}
})();