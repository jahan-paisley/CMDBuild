(function() {
	Ext.define("CMDBuild.view.management.common.widgets.CMDMSAttachmentPickerDelegate", {
		/**
		 * @param {CMDBuild.view.management.common.widgets.CMDMSAttachmentPicker} dmsAttachmentPicker,
		 * @param {Number} classId
		 */
		onCMDMSAttachmentPickerClassDidSelected: function(dmsAttachmentPicker, classId) {},

		/**
		 * @param {CMDBuild.view.management.common.widgets.CMDMSAttachmentPicker} dmsAttachmentPicker,
		 * @param {CMDBuild.management.mail.Model} emailRecord,
		 * @param {CMDBuild.view.management.common.widgets.CMEmailWindow} emailWindow,
		 */
		onCMDMSAttachmentPickerOKButtonClick: function(dmsAttachmentPicker, emailRecord, emailWindow) {},

		/**
		 * @param {CMDBuild.view.management.common.widgets.CMDMSAttachmentPicker} dmsAttachmentPicker,
		 */
		onCMDMSAttachmentPickerCancelButtonClick: function(dmsAttachmentPicker) {},

		/**
		 * @param {CMDBuild.view.management.common.widgets.CMDMSAttachmentPicker} dmsAttachmentPicker
		 * @param {CMDBuild.view.management.common.CMCardGrid} attachmentGrid
		 * @param {CMDBuild.view.management.common.widgets.CMDMSAttachmentModel} record
		 */
		onCMDMSAttachmentPickerCardDidSelected: function(dmsAttachmentPicker, attachmentGrid, record) {},

		/**
		 * @param {CMDBuild.view.management.common.widgets.CMDMSAttachmentPicker} dmsAttachmentPicker
		 * @param {String} fileName
		 * @param {Boolean} checked
		 */
		onCMDMSAttachmentPickerAttachmentCheckChange: function(dmsAttachmentPicker, fileName, checked) {},

		/**
		 * @param {CMDBuild.view.management.common.widgets.CMDMSAttachmentPicker} dmsAttachmentPicker
		 * @param {CMDBuild.view.management.common.widgets.CMDMSAttachmentModel[]} records
		 */
		onCMDMSAttachmentPickerAttachmentsGridDidLoad: function(dmsAttachmentPicker, records) {},

		/**
		 * @param {CMDBuild.view.management.common.widgets.CMDMSAttachmentPicker} dmsAttachmentPicker
		 */
		onCMDMSAttachmentPickerCardDidLoad: function(dmsAttachmentPicker) {}
	});

	Ext.define("CMDBuild.view.management.common.widgets.CMDMSAttachmentPickerState", {
		constructor: function(picker) {
			this.picker = picker;

			this.currentClassName = null;
			this.currentCardId = null;

			/*
			 * the structure is something
			 * like that:
			 * {
			 * 	classA: {
			 * 		card1: {
			 * 			"filea.jpg": true,
						"fileB.jpgh": true
					}
			 * 		card2: {..}
			 * 	},
			 * 	classB: {...}
			 * }
			 */
			this.attachments = {};
		},

		check: function(fileName) {
			ensureExistingCardAttachments(this);
			this.attachments[this.currentClassName][this.currentCardId][fileName] = true;
		},

		uncheck: function(fileName) {
			ensureExistingCardAttachments(this);
			delete this.attachments[this.currentClassName][this.currentCardId][fileName];
		},

		setClassName: function(className) {
			this.currentClassName = className;
		},

		setCardId: function(cardId) {
			this.currentCardId = cardId;
		},

		syncSelection: function(records) {
			ensureExistingCardAttachments(this);
			var currentCardAttachments = this.attachments[this.currentClassName][this.currentCardId];
			if (Ext.Object.isEmpty(currentCardAttachments)) {
				return;
			}

			for (var i=0, l=records.length; i<l; ++i) {
				var r = records[i];
				var fileName = r.get("Filename");
				if (currentCardAttachments[fileName]) {
					r.set("Checked", true);
					r.commit();
				}
			}
		},

		getData: function() {
			var out = [];
			for (var className in this.attachments) {
				var classAttachments = this.attachments[className];
				for (var cardId in classAttachments) {
					var cardAttachments = classAttachments[cardId];
					for (var fileName in cardAttachments) {
						out.push({
							className: className,
							cardId: cardId,
							fileName: fileName
						});
					}
				}
			}

			return out;
		}
	});


	function ensureExistingCardAttachments(me) {
		if (typeof me.attachments[me.currentClassName] == "undefined") {
			me.attachments[me.currentClassName] = {};
		}

		var classAttachments = me.attachments[me.currentClassName];
		if (typeof classAttachments[me.currentCardId] == "undefined") {
			classAttachments[me.currentCardId] = {};
		}
	}

	Ext.define("CMDBuild.view.management.common.widgets.CMDMSAttachmentModel", {
		extend: "Ext.data.Model",
		fields: [{
			name: 'Description',  type: 'string'
		}, {
			name: 'Filename',  type: 'string'
		}, {
			name: 'Checked',  type: 'boolean',
		}]
	});

	Ext.define("CMDBuild.view.management.common.widgets.CMDMSAttachmentGrid", {
		extend: "Ext.grid.Panel",

		// configuration
		delegate: undefined,
		ownerWindow: undefined,
		// configuration

		initComponent: function() {
			var me = this;

			this.store = Ext.create('Ext.data.Store', {
				model: "CMDBuild.view.management.common.widgets.CMDMSAttachmentModel",
				autoLoad: false,
				proxy: {
					type: 'ajax',
					url: 'services/json/attachments/getattachmentlist',
					reader: {
						type: 'json',
						root: 'rows'
					}
				},

			});

			this.columns = [{
				xtype: "checkcolumn",
				dataIndex: "Checked",
				width: 40,
				listeners: {
					checkchange: function(columns, rowIndex, checked) {
						var record = me.store.getAt(rowIndex);
						if (record) {
							me.delegate.onCMDMSAttachmentPickerAttachmentCheckChange( //
									me.ownerWindow, //
									record.get("Filename"), //
									checked //
								);
						}
					}
				}
			}, {
				text: CMDBuild.Translation.file_name,
				dataIndex: "Filename",
				flex: 1
			}, {
				text: CMDBuild.Translation.description_,
				dataIndex: "Description",
				flex: 1
			}];

			this.callParent(arguments);
		},

		loadAttachmentsForClassNameAndCardId: function(className, cardId) {
			var me = this;
			this.store.load({
				params: {
					className: className,
					cardId: cardId
				},

				callback: function(records) {
					me.delegate.onCMDMSAttachmentPickerAttachmentsGridDidLoad(//
							me.ownerWindow, //
							records //
						);
				}
			});
		}

	});

	Ext.define("CMDBuild.view.management.common.widgets.CMDMSAttachmentPicker", {
		extend: "CMDBuild.PopupWindow",

		mixins: {
			cmCardGridDelegate: "CMDBuild.view.management.common.CMCardGridDelegate"
		},

		// configure
		delegate: undefined,
		emailRecord: undefined,
		emailWindow: undefined,
		// configure

		initComponent: function() {

			this.delegate = this.delegate || new CMDBuild.view.management.common.widgets.CMDMSAttachmentPickerDelegate();
			buildTBar(this);
			buildButtons(this);

			this.cardGrid = new CMDBuild.view.management.common.CMCardGrid({
				cmAdvancedFilter: false,
				cmAddGraphColumn: false,
				cmAddPrintButton: false,
				region: "center",
				border: false
			});
			this.cardGrid.addDelegate(this);

			this.attachmentGrid = new CMDBuild.view.management.common.widgets.CMDMSAttachmentGrid({
				region: "south",
				split: true,
				border: false,
				delegate: this.delegate,
				ownerWindow: this,
				height: 200
			});
			this.items = [this.cardGrid, this.attachmentGrid];
			this.layout = "border";

			this.callParent(arguments);

			this.cmState = new CMDBuild.view.management.common.widgets.CMDMSAttachmentPickerState(this);
		},

		updateCardGridForClassId: function(classId) {
			this.cardGrid.updateStoreForClassId(classId);
		},

		loadAttachmentsForClassNameAndCardId: function(className, cardId) {
			this.attachmentGrid.loadAttachmentsForClassNameAndCardId(className, cardId);
		},

		cleanAttachmentGrid: function() {
			this.attachmentGrid.store.removeAll();
		},

		// as CMCardGridDelegate

		/**
		 *
		 * @param {CMDBuild.view.management.common.CMCardGrid} grid
		 * @param {Ext.data.Model} record
		 */
		onCMCardGridSelect: function(grid, record) {
			this.delegate.onCMDMSAttachmentPickerCardDidSelected(this, grid, record);
		},

		/**
		 *
		 * @param {CMDBuild.view.management.common.CMCardGrid} grid
		 */
		onCMCardGridLoad: function(grid) {
			this.delegate.onCMDMSAttachmentPickerCardDidLoad(this);
		}
	});

	function buildTBar(me) {
		me.classComboBox = new CMDBuild.field.CMBaseCombo({
			store: _CMCache.getClassesStore(),
			labelWidth: CMDBuild.LABEL_WIDTH,
			fieldLabel : CMDBuild.Translation.select_a_class,
			labelAlign: "right",
			valueField : 'id',
			displayField : 'description',
			queryMode: "local",
			editable: false
		});

		me.classComboBox.on("change", function(field, newValue, oldValue) {
			me.delegate.onCMDMSAttachmentPickerClassDidSelected(me, newValue);
		}, me);

		me.tbar = [me.classComboBox];
	}

	function buildButtons(me) {
		me.buttonAlign = "center";
		me.buttons = [{
			text: CMDBuild.Translation.common.buttons.confirm,
			handler: function() {
				me.delegate.onCMDMSAttachmentPickerOKButtonClick(me, me.emailRecord, me.emailWindow);
			}
		}, {
			text: CMDBuild.Translation.common.buttons.abort,
			handler: function() {
				me.delegate.onCMDMSAttachmentPickerCancelButtonClick(me);
			}
		}];
	}
})();