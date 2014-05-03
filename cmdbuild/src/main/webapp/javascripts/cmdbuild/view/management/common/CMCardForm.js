(function() {
	var tr = CMDBuild.Translation.management.modcard;

	Ext.define("CMDBuild.view.management.classes.CMCardForm", {
		extend: "Ext.form.Panel",

		mixins: {
			cmFormFunctions: "CMDBUild.view.common.CMFormFunctions"
		},

		_lastCard: null, // to sync the editable panel when goes in edit mode

		_isInEditMode: false,

		constructor: function(conf) {

			Ext.apply(this, conf);

			this.CMEVENTS = {
				saveCardButtonClick: "cm-save",
				abortButtonClick: "cm-abort",
				removeCardButtonClick: "cm-remove",
				modifyCardButtonClick: "cm-modify",
				cloneCardButtonClick: "cm-clone",
				printCardButtonClick: "cm-print",
				openGraphButtonClick: "cm-graph",
				formFilled: "cmFormFilled",
				editModeDidAcitvate: "cmeditmode",
				displayModeDidActivate: "cmdisplaymode"
			};

			this.addEvents([
				this.CMEVENTS.saveCardButtonClick,
				this.CMEVENTS.abortButtonClick,
				this.CMEVENTS.removeCardButtonClick,
				this.CMEVENTS.modifyCardButtonClick,
				this.CMEVENTS.cloneCardButtonClick,
				this.CMEVENTS.printCardButtonClick,
				this.CMEVENTS.openGraphButtonClick,
				this.CMEVENTS.editModeDidAcitvate,
				this.CMEVENTS.displayModeDidActivate
			]);

			this.buildTBar();
			this.buildButtons();

			this.callParent(arguments);
		},

		initComponent: function() {
			Ext.apply(this, {
				frame: false,
				border: false,
				hideMode: "offsets",
				bodyCls: "x-panel-body-default-framed cmbordertop",
				bodyStyle: {
					padding: "5px 5px 0 5px"
				},
				cls: "x-panel-body-default-framed",
				autoScroll: false,
				tbar: this.cmTBar,
				buttonAlign: 'center',
				buttons: this.cmButtons,
				layout: {
					type: 'hbox',
					align:'stretch'
				}
			});

			this.callParent(arguments);
		},

		editMode: function() {
			if (this._isInEditMode) {
				return;
			}

//			this.suspendLayouts();
			this.ensureEditPanel();

			if (this.tabPanel) {
				this.tabPanel.editMode();
			}

			this.disableCMTbar();
			this.enableCMButtons();

			//http://www.sencha.com/forum/showthread.php?261407-4.2.0-HTML-editor-SetValue-does-not-work-when-component-is-not-rendered
			//This function for fixing the above bug
			//To delete when upgrade at extjs 4.2.1
			this.tabPanel.showAll();
			//-------------------------------------------------
//			this.resumeLayouts(true);
			this.fireEvent(this.CMEVENTS.editModeDidAcitvate);
			this._isInEditMode = true;
		},

		displayMode: function(enableCmBar) {
			this.suspendLayouts();
			if (this.tabPanel) {
				this.tabPanel.displayMode();
			}

			if (enableCmBar) {
				this.enableCMTbar();
			} else {
				this.disableCMTbar();
			}

			this.disableCMButtons();

			this.resumeLayouts(true);

			this.fireEvent(this.CMEVENTS.displayModeDidActivate);
			this._isInEditMode = false;
		},

		displayModeForNotEditableCard: function() {
			this.displayMode(enableCMBar = false);
			if (this.printCardMenu) {
				this.printCardMenu.enable();
			}
			if (this.graphButton) {
				this.graphButton.enable();
			}
		},

		reset: function() {
			this.suspendLayouts();
			this._isInEditMode = false;
			this.mixins.cmFormFunctions.reset.apply(this);
			this.resumeLayouts(true);
		},

		// fill the form with the data in the card
		loadCard: function(card, bothPanels) {
			this._lastCard = card;
			this.reset();

			if (!card) { return; }

			if (typeof card == "object") {
				var data = card.raw || card.data;

				if (bothPanels) {
					_fillFields(this, data);
				} else if (this._isInEditMode) {
					_fillEditableFields(this, data);
				} else {
					_fillDisplayFields(this, data);
				}

			} else {
				throw "Card must be an object";
			}
		},

		canReconfigureTheForm: function() {
			var out = true;
			try {
				out = this.isVisible(true);
			} catch (e) {
				// if fails, the panel is not in a TabPanel, so don't defer the call
			}

			return out;
		},

		ensureEditPanel: function() {
			if (this.tabPanel
					&& !this._isInEditMode) {

				this.tabPanel.ensureEditPanel();

				if (this._lastCard) {
					this.loadCard(this._lastCard, bothPanels=true);
					this.callFieldTemplateResolverIfNeeded();
				}
			}
		},

		// popolate the form with the right subpanels and fields
		fillForm: fillForm,

		// private, allow simply configuration is subclassing
		buildTBar: buildTBar,

		// private, allow simply configuration is subclassing
		buildButtons: buildButtons,

		hasDomainAttributes: function() {
			var fields = this.getForm().getFields().items;

			for (var i=0, l=fields.length; i<l; ++i) {
				if (fields[i].cmDomainAttribute) {
					return true;
				}
			};

			return false;
		},

		callFieldTemplateResolverIfNeeded: function() {
			var fields = this.getForm().getFields().items;
			for (var i=0;  i<fields.length; ++i) {
				var field = fields[i];
				if (field && field.resolveTemplate) {
					field.resolveTemplate();
				}
			}
		},

		isInEditing: function() {
			return this._isInEditMode;
		},

		toString: function() {
			return "CMCardForm";
		}
	});

	function _fillDisplayFields(me, data, referenceAttributes) {
		_fillFields(me, data, referenceAttributes, function(f) {
			return !f._belongToEditableSubpanel;
		});
	}

	function _fillEditableFields(me, data, referenceAttributes) {
		_fillFields(me, data, referenceAttributes, function(f) {
			return f._belongToEditableSubpanel;
		});
	}

	function _fillFields(me, data, referenceAttributes, fieldSelector) {
		var fields = me.getForm().getFields();
		addReferenceAttrsToData(data, referenceAttributes);

		if (fields) {

			if (Ext.getClassName(fields) == "Ext.util.MixedCollection") {
				fields = fields.items;
			}

			for (var i=0, l=fields.length; i<l; ++i) {
				var f = fields[i];

				if (typeof fieldSelector == "function"
						&& !fieldSelector(f)) {

					continue;
				}

				if (f.xtype == "displayfield")
					a = 1;
				try {
					f.setValue(data[f.name]);
					if (typeof f.isFiltered == "function"
						&& f.isFiltered()) {

						f.setServerVarsForTemplate(data);
					}
				} catch (e) {
					_debug("I can not set the value for " + f.name);
				}
			}
		}

		me.fireEvent(me.CMEVENTS.formFilled);
	}

	// FIXME: probably never reached 'couse the reference's attributes are added
	// in the controller
	function addReferenceAttrsToData(data, referenceAttributes) {
		for (var referenceName in referenceAttributes || {}) {
			var attributes = referenceAttributes[referenceName];

			for (var attributeName in attributes) {
				var fullName = "_" + referenceName + "_" + attributeName,
					value = attributes[attributeName];

				data[fullName] = value;
			}
		}
	}

	function loadCard(card) {
		if (this.loadRemoteData || this.hasDomainAttributes()) {
			this.loadCard(card.get("Id"), card.get("IdClass"));
		} else {
			this.loadCard(card);
		}

		this.loadRemoteData = false;
	}

	function fillForm(attributes, editMode) {

		this._lastCard = null;

		var panels = [],
			groupedAttr = CMDBuild.Utils.groupAttributes(attributes, false);

		this.suspendLayouts();

		this.removeAll(autoDestroy = true);

		// The fields of sub-panels are not
		// removed from the Ext.form.Basic
		// Do it by hand
		var basicForm = this.getForm();
		var basicFormFields = basicForm.getFields(); // a Ext.util.MixedCollection
		basicFormFields.clear();

		for (var group in groupedAttr) {
			var attributes = groupedAttr[group];
			var p = CMDBuild.Management.EditablePanel.build({
				attributes: attributes,
				frame: false,
				border: false,
				title: group,
				bodyCls: "x-panel-body-default-framed",
				bodyStyle: {
					padding: "5px"
				}
			});

			if (p) {
				panels.push(p);
			}
		}

		if (this.tabPanel) {
			delete this.tabPanel;
		}

		if (panels.length == 0) {
			// hack to have a framed panel
			// if there are no attributes
			panels = [new CMDBuild.Management.EditablePanel({
				attributes: [],
				frame: false,
				border: false,
				title: "",
				bodyCls: "x-panel-body-default-framed",
				bodyStyle: {
					padding: "5px"
				}
			})];
		}

		this.tabPanel = new CMDBuild.view.management.common.CMTabPanel({
			items: panels,
			frame: false,
			flex: 1
		});

		this.add(this.tabPanel);

		// Resume the layouts when end
		// to add the fields
		this.resumeLayouts(true);
		this.doLayout();

		if (this.danglingCard) {
			loadCard.call(this, this.danglingCard);
			this.danglingCard = null;
		}

		if (editMode || this.forceEditMode) {
			this.editMode();
			this.forceEditMode = false;
		}

	};

	function buildTBar() {
		if (this.withToolBar) {
			var me = this;
			this.deleteCardButton = new Ext.button.Button({
				iconCls : "delete",
				text : tr.delete_card,
				handler: function() {
					me.fireEvent(me.CMEVENTS.removeCardButtonClick);
				}
			});

			this.cloneCardButton = new Ext.button.Button({
				iconCls : "clone",
				text : tr.clone_card,
				handler: function() {
					me.fireEvent(me.CMEVENTS.cloneCardButtonClick);
				}
			});

			this.modifyCardButton = new Ext.button.Button({
				iconCls : "modify",
				text : tr.modify_card,
				handler: function() {
					me.fireEvent(me.CMEVENTS.modifyCardButtonClick);
				}
			});

			this.printCardMenu = new CMDBuild.PrintMenuButton({
				text : CMDBuild.Translation.common.buttons.print+" "+CMDBuild.Translation.management.modcard.tabs.card.toLowerCase(),
				callback : function() { this.fireEvent("click");},
				formatList: ["pdf", "odt"]
			});

			this.mon(this.printCardMenu, "click", function(format) {
				me.fireEvent(me.CMEVENTS.printCardButtonClick, format);
			});

			this.cmTBar = [
				this.modifyCardButton,
				this.deleteCardButton,
				this.cloneCardButton
			];

			this.graphButton = new Ext.button.Button({
				iconCls : "graph",
				text : CMDBuild.Translation.management.graph.action,
				handler: function() {
					me.fireEvent(me.CMEVENTS.openGraphButtonClick);
				}
			});

			if (CMDBuild.Config.graph.enabled=="true") {
				this.cmTBar.push(this.graphButton);
			}

			this.cmTBar.push(this.printCardMenu);
		}
	};

	function buildButtons() {
		if (this.withButtons) {
			var me = this;
			this.saveButton = new Ext.button.Button({
				text: CMDBuild.Translation.common.buttons.save,
				handler: function() {
					me.fireEvent(me.CMEVENTS.saveCardButtonClick);
				}
			});

			this.cancelButton = new Ext.button.Button( {
				text: this.readOnlyForm ? CMDBuild.Translation.common.buttons.close : CMDBuild.Translation.common.buttons.abort,
				handler: function() {
					me.fireEvent(me.CMEVENTS.abortButtonClick);
				}
			});

			this.cmButtons = [this.saveButton,this.cancelButton];
		}
	};
})();
