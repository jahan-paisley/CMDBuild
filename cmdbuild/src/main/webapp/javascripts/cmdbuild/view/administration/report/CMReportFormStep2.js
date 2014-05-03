Ext.define("CMDBuild.view.administration.report.CMReportFormStep2", {
	extend: "Ext.form.Panel",
	
	mixins : {
		cmFormFunction: "CMDBUild.view.common.CMFormFunctions"
	},
	
	translation: CMDBuild.Translation.administration.modreport.importJRFormStep2,

	initComponent:function() {
		this.fieldsets = [];

		Ext.apply(this, {
			frame: true,
			layout: "vbox",
			encoding: 'multipart/form-data',
			fileUpload: true,
			plugins: [new CMDBuild.CallbackPlugin()],
			autoHeight: true,
			autoScroll: true,
			defaultType: 'textfield'
		});

		this.callParent(arguments);

	},
	
	setFormDetails: function(fd) {
		this.duplicateimages = false; //because it was overridden by the apply only if is true
		this.skipSecondStep = false;
		Ext.apply(this, fd);

		this.fieldsets = [];
		this.removeAll(true);

		if (this.duplicateimages) {
			this.printMsg(this.translation.duplicate_images);
			this.fireEvent('cmdb-importjasper-duplicateimages');
		} else { // show form
			this.buildFields(this.images, "image");
			this.buildFields(this.subreports, "subreport");
		}
	},

	//private
	buildFields: function(refer, namePrefix) {
		if (refer) {
			for (var i=0; i < refer.length; i++) {
				var image = refer[i];
				if (image.name) {
					this.add({
						xtype: 'filefield',
						allowBlank: true,
						anchor: "100%",
						fieldLabel: image.name,
						name: namePrefix+i
					});
				} else {
					CMDBuild.log.error('Import report step 2: ', image, 'has not an attribute called name');
				} 
		  	}
		}
	},
	
	//private
	printMsg: function(msg) {
		var msg = new Ext.form.FieldSet({
			title: this.translation.fieldset_generic,
			autoHeight: true,
			items: [{
				xtype : "label",
				text : msg
			}]
		});
		this.fieldsets.push(msg);
	}
});