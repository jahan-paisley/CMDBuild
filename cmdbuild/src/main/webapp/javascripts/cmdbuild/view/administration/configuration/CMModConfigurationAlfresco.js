(function() {
	var tr = CMDBuild.Translation.administration.setup.dms;

	Ext.define("CMDBuild.view.administration.configuration.CMModConfigurationAlfresco", {
		extend: "CMDBuild.view.administration.configuration.CMBaseModConfiguration",
		title: tr.title,
		configFileName: 'dms',

		constructor: function() {
			this.items = [{
				xtype : 'fieldset',
				title : tr.general,
				autoHeight : true,
				defaultType : 'textfield',
				items : [
				{
					fieldLabel : tr.enabled,
					xtype : 'xcheckbox',
					name : 'enabled'
				},
				{
					fieldLabel : tr.serverUrl,
					allowBlank : false,
					name : 'server.url',
					width: CMDBuild.CFG_BIG_FIELD_WIDTH
				},
				{
					fieldLabel : tr.delay,
					allowBlank : false,
					xtype : 'numberfield',
					name : 'delay'
				}]
			},
			{
				xtype : 'fieldset',
				title : tr.fileserver,
				autoHeight : true,
				defaultType : 'textfield',
				items : [
					{
						fieldLabel : tr.fileserverType,
						allowBlank : false,
						name : 'fileserver.type',
						disabled : true
					},
					{
						fieldLabel : tr.fileserverUrl,
						allowBlank : false,
						name : 'fileserver.url',
						width: CMDBuild.CFG_BIG_FIELD_WIDTH
					},
					{
						fieldLabel : tr.fileserverPort,
						allowBlank : false,
						xtype : 'numberfield',
						name : 'fileserver.port'
					}
				]
			},
			{
				xtype : 'fieldset',
				title : tr.repository,
				autoHeight : true,
				defaultType : 'textfield',
				items : [
				{
					fieldLabel : tr.repositoryFSPath,
					allowBlank : false,
					name : 'repository.fspath',
					width: CMDBuild.CFG_BIG_FIELD_WIDTH
				}, 
				{
					fieldLabel : tr.repositoryWSPath,
					allowBlank : false,
					name : 'repository.wspath',
					width: CMDBuild.CFG_BIG_FIELD_WIDTH
				}, 
				{
					fieldLabel : tr.repositoryApp,
					allowBlank : false,
					name : 'repository.app'
				}]
			}]

			var me = this;

			this.attachmentLookup = null;
			this.attachmentLookupFirstLoad = true;

			this.lookupTypeCombo = new Ext.form.field.ComboBox({
				fieldLabel : tr.categoryLookup,
				allowBlank : false,
				name : 'category.lookup',
				triggerAction : 'all',
				valueField : 'type',
				displayField : 'type',
				triggerAction : 'all',
				store : CMDBuild.Cache.getLookupTypeLeavesAsStore(),
				queryMode : "local"
			});

			updateAttachementLookupAfterSetValueOnLookupTypeCombo(me);

			this.credentialsFieldset = new Ext.form.FieldSet({
				title : tr.credential,
				autoHeight : true,
				defaultType : 'textfield',
				items : [
					{
						fieldLabel : tr.credentialUser,
						allowBlank : false,
						name : 'credential.user'
					},
					{
						fieldLabel : tr.credentialPassword,
						allowBlank : false,
						inputType : 'password',
						name : 'credential.password'
					},
					this.lookupTypeCombo
				]
			});

			this.items.push(this.credentialsFieldset);
			this.callParent(arguments);
		},

		// override
		getValues: function() {
			var values = this.callParent(arguments);
			if (this.attachmentLookup) {
				values["category.lookup.attachments"] = this.attachmentLookup.getRawValue();
			}

			return values;
		},

		// override
		afterSubmit: function() {
			var me = this;
			if (me.valuesFromServer && me.attachmentLookup) {
				me.attachmentLookup.setValue(me.valuesFromServer[me.attachmentLookup.name]);
			}
		}
	});

	function updateAttachementLookupAfterSetValueOnLookupTypeCombo(me) {
		me.lookupTypeCombo.setValue = Ext.Function.createSequence(
			me.lookupTypeCombo.setValue, //
			function(value) {
				me.credentialsFieldset.remove(me.attachmentLookup);
				if (value == null) {
					return;
				}

				var ltype = null;
				if (Ext.isArray(value)) {
					value = value[0];
				}

				if (typeof value == "string") {
					ltype = value;
				} else {
					ltype = value.get("type");
				}
				var lookupchain = _CMCache.getLookupchainForType(ltype);
				if (lookupchain.length == 0) {
					value = "";
					return;
				}
				var conf = {
					description: CMDBuild.Translation.attachmentsLookup,
					name: "category.lookup.attachments",
					isnotnull: false,
					fieldmode: "write",
					type: "LOOKUP",
					lookup: ltype,
					lookupchain: lookupchain
				};

				me.attachmentLookup = CMDBuild.Management.FieldManager.getFieldForAttr(conf, readonly=false, skipSubField=true);
				me.attachmentLookup.labelWidth = CMDBuild.CFG_LABEL_WIDTH;
				me.attachmentLookup.width = CMDBuild.CFG_MEDIUM_FIELD_WIDTH;
				me.attachmentLookup.labelAlign = "left";

				// There is a rendering issue that appear
				// resolved adding a delay to add the
				// combo to the field-set
				Ext.Function.createDelayed( //
					function() {
						me.credentialsFieldset.add(me.attachmentLookup);
						if (me.attachmentLookupFirstLoad) {
							me.attachmentLookupFirstLoad = false;
							me.afterSubmit();
						}
					}, //
					200 //
				)();
		});
	}
})();