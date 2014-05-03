(function() {

	Ext.define("CMDBuild.controller.management.common.CMAddDetailWindowController", {
		extend: "CMDBuild.controller.management.common.CMDetailWindowController",

		//override
		buildSaveParams: function() {
			var p = this.callParent(arguments);

			if (this.referenceToMaster) {
				// set the value to the field that was hidden
				var r = this.referenceToMaster;
				p[r.name] = r.value;
				// Then set the save relation to emptyFn because
				// the relation is automatically 
				this.saveRelationAction = Ext.emptyFn; //this.view.hasRelationAttributes ? this.updateRelation : Ext.emptyFn;
			} else {
				// set the function to create the relation
				// after the card
				this.saveRelationAction = this.addRelation;
			}

			return p;
		},

		//override
		onSaveSuccess: function(form, res) {
			if (this.saveRelationAction) {
				this.saveRelationAction(form, res);
			}
			this.view.destroy();
			CMDBuild.LoadMask.get().hide();
		},

		addRelation: function(form, res) {
			var detailData = {
				cardId: res.result.id,
				className: res.params.className
			};

			var p = this.buildParamsToSaveRelation(detailData);

			CMDBuild.ServiceProxy.relations.add({
				params: p
			});
		}
	});

})();