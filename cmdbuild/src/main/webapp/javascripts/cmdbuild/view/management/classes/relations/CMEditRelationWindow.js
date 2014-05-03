(function() {
	var NO_SELECTION = "No selection";
	var parameterNames = CMDBuild.ServiceProxy.parameter;

	Ext.define("CMDBuild.view.management.classes.relations.CMEditRelationWindow", {
		successCb: Ext.emptyFn,

		// To choose the card for the relation
		extend: "CMDBuild.Management.CardListWindow",

		// configuration
		relation: undefined, // {dst_id: "", dst_cid: "", dom_id: "", rel_id: "", masterSide: "_1", slaveSide: "_2", rel_attr: []}
		sourceCard: undefined, // the source of the relation
		// configuration

		// override
		initComponent: function() {
			if (this.relation == undefined) {
				throw "You must pass a relation to the CMEditRelationWindow";
			} else {
				this.idClass = this.relation.dst_cid;
			}

			this.saveButton = new CMDBuild.buttons.SaveButton({
				scope: this,
				handler: onSaveButtonClick
			});

			this.abortButton = new CMDBuild.buttons.AbortButton({
				scope: this,
				handler: function() {
					this.close();
				}
			});

			this.buttonAlign = "center";
			this.buttons = [this.saveButton, this.abortButton];

			this.callParent(arguments);
		},

		// override
		setItems: function() {
			var attributes = _CMCache.getDomainById(this.relation.dom_id).get("attributes");

			this.attributesPanel = CMDBuild.Management.EditablePanel.build({
				autoScroll: true,
				region: "south",
				height: "30%",
				attributes: attributes,
				split: true,
				frame: false,
				border: false,
				bodyCls: "x-panel-body-default-framed",
				bodyStyle: {
					padding: "5px"
				}
			});

			this.callParent(arguments);

			if (this.attributesPanel != null) {
				this.layout = "border";
				this.grid.region = "center";
				this.grid.addCls("cmborderbottom");
				this.items.push(this.attributesPanel);
			} else {
				this.attributesPanel = buildNullObject();
			}
		},

		// override
		show: function() {
			this.callParent(arguments);
			this.attributesPanel.editMode();
			var fields = this.attributesPanel.getFields();
			var rel_attrs = this.relation.rel_attr || {};
			for (var i = 0, l=fields.length; i<l; ++i) {
				var f = fields[i];
				var name;

				if (f.CMAttribute) {
					name = f.CMAttribute.name;
				} else {
					name = f.name;
				}

				var val = rel_attrs[name];
				f.setValue(val.id || val);
				if (val) {
					if (f.CMAttribute.type == "LOOKUP") {
						var store = _CMCache.getLookupStore(f.CMAttribute.lookup);
						for (var j = 0; j < 4; j++) {
							Ext.Function.createDelayed(function() {
								for (var y = 0; y < store.data.items.length; y++) {
									if (val == store.data.items[y].raw.Description) {
										val = store.data.items[y].raw.Id;
										f.setValue(val);
										return;
									}
								}
							}, 500)();
						}
					}
				}
			}
		}
	});

	function onSaveButtonClick() {
		var p = buildSaveParams(this);
		if (p) {
			if (p[parameterNames.RELATION_ID ] == -1) { // creation
				delete p[parameterNames.RELATION_ID];
				CMDBuild.ServiceProxy.relations.add({
					params: p,
					scope: this,
					success: function() {
						this.successCb();
						this.close();
					}
				});
			} else { // modify
				CMDBuild.ServiceProxy.relations.modify({
					params: p,
					scope: this,
					success: function() {
						this.successCb();
						this.close();
					}
				});
			}
		}
	}

	/**
	 * @return {
	 * 	domainName: string,
	 * 	relationId: int,
	 *  master: string, "_1" | "_2" the side of the domain to consider as master
	 *  
	 *  // assuming the master is "_1"
	 *  attributes: {
	 *  	_1: [{
	 *  		className: string,
	 *  		cardId: int
	 *  	}],
	 *  	_2: [{
	 *  		className: string,
	 *  		cardId: int
	 *  	}, {
	 *  		eventually other card objects
	 *  	}],
	 *  	
	 *  	// the attribute defined for the domain as key/value pairs
	 *  	...
	 *  	attributeName1: value,
	 *  	attributeName2: value
	 *  	...
	 *  }
	 * }
	 */
	function buildSaveParams(me) {
		var domain = _CMCache.getDomainById(me.relation.dom_id);
		var params = {};
		var attributes = {};

		params[parameterNames.DOMAIN_NAME] = domain.getName();
		params[parameterNames.RELATION_ID] = me.relation.rel_id;

		params[parameterNames.RELATION_MASTER_SIDE] = me.relation.masterSide;
		attributes[me.relation.masterSide] = [getCardAsParameter(me.sourceCard)];

		try {
			attributes[me.relation.slaveSide] = getSelections(me);
		} catch (e) {
			if (e == NO_SELECTION) {
				var msg = Ext.String.format("<p class=\"{0}\">{1}</p>", CMDBuild.Constants.css.error_msg, CMDBuild.Translation.errors.no_selections);
				CMDBuild.Msg.error(CMDBuild.Translation.common.failure, msg, false);
			}
			return;
		}

		try {
			attributes = Ext.apply(attributes, getData(me.attributesPanel));
		} catch (e) {
			var msg = Ext.String.format("<p class=\"{0}\">{1}</p>", CMDBuild.Constants.css.error_msg, CMDBuild.Translation.errors.invalid_attributes);
			CMDBuild.Msg.error(null, msg + e, false);
			return;
		}

		params[parameterNames.ATTRIBUTES] = Ext.encode(attributes);
		return params;
	}

	function getSelections(me) {
		var selection = me.grid.getSelectionModel().getSelection();
		var l = selection.length;
		var selectedCards = [];

		if (l>0) {
			for (var i=0; i<l; ++i) {
				var cardAsParameter = getCardAsParameter(selection[i]);
				selectedCards.push(cardAsParameter);
			}
		} else {
			if (me.relation.rel_id == -1) {
				// we are add a new relation, the selection is mandatory
				throw NO_SELECTION;
			} else {
				// is editing a relations
				// and there are relations selected
				// it could be that are updating
				// only the attributes. Retrieve
				// the already related card
				var relatedCardData = {
					Id: me.relation.dst_id,
					IdClass: me.relation.dst_cid
				};

				selectedCards.push( //
						// mock a card to use the
						// same function to have
						// the parameters
						getCardAsParameter({ //
							get: function(key) { //
								return relatedCardData[key];
							}
						})
					);
			}
		}

		return selectedCards;
	}

	function getCardAsParameter(card) {
		var parameter = {};
		parameter[parameterNames.CARD_ID] = card.get("Id");
		parameter[parameterNames.CLASS_NAME] = _CMCache.getEntryTypeNameById(card.get("IdClass"));

		return parameter;
	}

	function getData(attributesPanel) {
		var data = {},
			nonValid = "",
			ff = attributesPanel.getFields(),
			f;

		for (var i=0, l=ff.length; i<l; ++i) {
			 f = ff[i];
			if (f.isValid()) {
				data[f.name] = f.getValue();
			} else {
				nonValid += "<p><b>" + f.fieldLabel + "</b></p>";
			}
		}

		if (nonValid) {
			throw nonValid;
		} else {
			return data;
		}
	}

	function buildNullObject() {
		return {
			editMode: Ext.emptyFn,
			getFields: function() {
				return {};
			}
		};
	}

})();
