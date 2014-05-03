(function() {

var tr = CMDBuild.Translation.management.modcard;

Ext.define("CMDBuild.view.management.classes.attachments.CMCardAttachmentsPanel", {
	extend: "Ext.grid.Panel",
	translation : CMDBuild.Translation.management.modcard,
	eventtype: 'card',
	eventmastertype: 'class',
	hideMode: "offsets",

	initComponent: function() {
		var col_tr = CMDBuild.Translation.management.modcard.attachment_columns;

		this.addAttachmentButton = new Ext.button.Button({
			iconCls : 'add',
			text : this.translation.add_attachment
		});

		this.store = buildStore();

		Ext.apply(this, {
			loadMask: false,
			tbar:[this.addAttachmentButton],
			features: [{
				groupHeaderTpl: '{name} ({rows.length} {[values.rows.length > 1 ? CMDBuild.Translation.management.modcard.attachment_columns.items : CMDBuild.Translation.management.modcard.attachment_columns.item]})',
				ftype: 'groupingsummary'
			}],
			columns: [
				{header: col_tr.category, dataIndex: 'Category', hidden: true},
				{header: col_tr.creation_date, sortable: true, dataIndex: 'CreationDate', renderer: Ext.util.Format.dateRenderer('d/m/Y H:i:s'), flex: 2},
				{header: col_tr.modification_date, sortable: true, dataIndex: 'ModificationDate', renderer: Ext.util.Format.dateRenderer('d/m/Y H:i:s'), flex: 2},
				{header: col_tr.author, sortable: true, dataIndex: 'Author', flex: 2},
				{header: col_tr.version, sortable: true, dataIndex: 'Version', flex: 1},
				{header: col_tr.filename, sortable: true, dataIndex: 'Filename', flex: 4},
				{header: col_tr.description, sortable: true, dataIndex: 'Description', flex: 4},
				{header: '&nbsp;', width: 80, sortable: false, renderer: this.renderAttachmentActions, align: 'center', tdCls: 'grid-button', dataIndex: 'Fake'}
			]
		});

		this.callParent(arguments);
	},

	reloadCard: function() {
		this.loaded = false;
		if (this.ownerCt.layout.getActiveItem) { 
			if (this.ownerCt.layout.getActiveItem().id == this.id) {
				this.loadCardAttachments();
			}
		} else {
			// it is not in a tabPanel
			this.loadCardAttachments();
		}
	},

	loadCardAttachments: function() {
		if (this.loaded) {
			return;
		}

		this.getStore().load();

		this.loaded = true;
	},

	setExtraParams: function(p) {
		this.store.proxy.extraParams = p;
	},

	clearStore: function() {
		this.store.removeAll();
	},

	renderAttachmentActions: function() {
		var tr = CMDBuild.Translation.management.modcard,
			out = '<img style="cursor:pointer" title="'+tr.download_attachment+'" class="action-attachment-download" src="images/icons/bullet_go.png"/>&nbsp;';

			if (this.writePrivileges) {
				out += '<img style="cursor:pointer" title="'+tr.edit_attachment+'" class="action-attachment-edit" src="images/icons/modify.png"/>&nbsp;'
				+ '<img style="cursor:pointer" title="'+tr.delete_attachment+'" class="action-attachment-delete" src="images/icons/delete.png"/>';
			}

			return out;
	},

	updateWritePrivileges: function(priv_write) {
		this.writePrivileges = priv_write;
		this.addAttachmentButton.setDisabled(!priv_write);
	},

	// DEPRECATED

	onAddCardButtonClick: function() { _deprecated();
		this.disable();
	},

	onCardSelected: function(card) { _deprecated();
		this.updateWritePrivileges(card.raw.priv_write);
	}
});

function buildStore() {
	var s =  new Ext.data.Store({
		model: "CMDBuild.model.CMAttachment",
		proxy: {
			type: 'ajax',
			url: 'services/json/attachments/getattachmentlist',
			reader: {
				type: 'json',
				root: 'rows'
			}
		},

		remoteSort: false,
		groupField: 'Category',
		sorters: {property: 'Category', direction: "ASC"}
	});

	return s;
}

})();