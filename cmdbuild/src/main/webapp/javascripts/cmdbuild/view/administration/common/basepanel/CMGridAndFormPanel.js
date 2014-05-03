Ext.define("CMDBuild.view.administration.common.basepanel.CMGridAndFormPanel", {
	extend: "Ext.panel.Panel",

	mixins: {
		delegable: "CMDBuild.core.CMDelegable"
	},

	constructor: function() {
		this.mixins.delegable.constructor.call(this,
				"CMDBuild.delegate.administration.common.basepanel.CMGridAndFormPanelDelegate");

		this.callParent(arguments);
	},

	// configuration
	addButtonText: "Add",
	modifyButtonText: "Modify",
	removeButtonText: "Remove",
	withRemoveButton: true,
	withEnableDisableButton: false,
	fileUpload: false,
	withPagingBar: true,
	// configuration

	initComponent : function() {
		var me = this;

		this.addButton = new Ext.Button({
			iconCls: 'add',
			text: this.addButtonText,
			handler: function() {
				me.callDelegates("onGridAndFormPanelAddButtonClick", me);
			}
		});

		this.grid = this.buildGrid();
		this.form = this.buildForm();

		this.tbar = [this.addButton];
		this.frame = false;
		this.border = true;
		this.layout = "border";
		this.items = [this.grid, this.form];

		this.callParent(arguments);

		_CMUtils.forwardMethods(this, this.form, ["buildFields", "disableModify", "enableModify", "updateEnableDisableButton"]);
		_CMUtils.forwardMethods(this, this.grid, ["configureGrid"]);
	},

	buildGrid: function() {
		var gridConfig = {
			region: "center",
			border: false,
			frame: false,
			withPagingBar: this.withPagingBar	
		};

		if (this.withPagingBar) {
			gridConfig.cls = "cmborderbottom";
		}

		return new CMDBuild.view.administration.common.basepanel.CMGrid(gridConfig);
	},

	buildForm: function() {
		var form = new CMDBuild.view.administration.common.basepanel.CMForm({
			modifyButtonText: this.modifyButtonText,
			removeButtonText: this.removeButtonText,
			withRemoveButton: this.withRemoveButton,
			withEnableDisableButton: this.withEnableDisableButton,
			fileUpload: this.fileUpload,

			region: "south",
			height: "70%",
			split: true,
			frame: false,
			border: false,
			cls: "x-panel-body-default-framed cmbordertop",
			bodyCls: 'cmgraypanel'
		});

		return form;
	},

	clearSelection: function() {
		this.grid.getSelectionModel().deselectAll();
	},

	getBasicForm: function() {
		if (this.form) {
			return this.form.getForm();
		} else {
			return null;
		}
	}
});