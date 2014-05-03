(function() {
	Ext.define("CMDBuild.controller.management.common.widgets.CMCreateModifyCardController", {
		extend: "CMDBuild.controller.management.classes.CMBaseCardPanelController",

		mixins : {
			observable: "Ext.util.Observable",
			widgetcontroller: "CMDBuild.controller.management.common.widgets.CMWidgetController"
		},

		statics: {
			WIDGET_NAME: CMDBuild.view.management.common.widgets.CMCreateModifyCard.WIDGET_NAME
		},


		constructor: function(view, supercontroller, widget, clientForm, card) {
			var widgetControllerManager = new CMDBuild.controller.management.common.CMWidgetManagerController(view.getWidgetManager());
			this.callParent([view, supercontroller, widgetControllerManager]);

			this.mixins.observable.constructor.call(this);
			this.mixins.widgetcontroller.constructor.apply(this, arguments);

			this.templateResolverIsBusy = false;
			this.idClassToAdd = undefined;
			this.savedCardId = undefined;
			this.clientForm = clientForm;

			this.templateResolver = new CMDBuild.Management.TemplateResolver({
				clientForm: clientForm,
				xaVars: widget,
				serverVars: this.getTemplateResolverServerVars()
			});

			this.mon(this.view.addCardButton, "cmClick", onAddCardClick, this);
		},

		// override
		onSaveSuccess: function(form, operation) {
			this.callParent(arguments);
			this.savedCardId = operation.result.id || this.cardId;

			if (typeof this.superController.hideWidgetsContainer == "function") {
				this.superController.hideWidgetsContainer();
				updateLocalDepsIfReferenceToModifiedClass(this);
			}
		},

		// override
		loadCardStandardCallBack: function(card) {
			var me = this;
			this.card = card;
			this.loadFields(card.get("IdClass"), function() {
				me.view.loadCard(card, bothpanel = true);
				if (me.isEditable(card)) {
					me.view.editMode();
				}
			});
		},

		getCQLOfTheCardId: function() {
			return this.widgetConf.idcardcqlselector;
		},

		isWidgetEditable: function(controller) {
			return !this.widgetConf.readonly 
				&& this.clientForm.owner._isInEditMode; // Ugly, but in the world there are also ugly stuff
		},

		// override
		beforeActiveView: function() {
			var me = this;
			this.card = null;
			this.targetClassName = this.widgetConf.targetClass;
			this.entryType = _CMCache.getEntryTypeByName(this.targetClassName);

			if (this.entryType != null) {
				this.view.initWidget(this.entryType, this.isWidgetEditable());

				this.templateResolver.resolveTemplates({
					attributes: ["idcardcqlselector"],
					callback: function(o) {
						me.cardId = normalizeIdCard(o["idcardcqlselector"]);
						if (me.cardId == null 
							&& me.entryType.isSuperClass()) {

							// could not add a card for a superclass

						} else {
							loadAndFillFields(me);
						}
					}
				});
			}
		},

		// override
		getData: function() {
			var out = null;
			if (this.savedCardId) {
				out = {};
				out["output"] = this.savedCardId;
			}

			return out;
		},

		// override
		isEditable: function() {
			return this.callParent(arguments) && this.isWidgetEditable();
		},

		/*
		 * Does not need to listen the
		 * cardModule state events
		 */
		// override
		buildCardModuleStateDelegate: function() {}
	});

	function loadAndFillFields(me, classId) {
		classId = classId || me.entryType.getId();
		var isANewCard = me.cardId == null;

		if (isANewCard) {
			/*
			 * presets is a map like this:
			 * {
			 * 		nameOfActivityAttribute: nameOfCardAttribute,
			 * 		nameOfActivityAttribute: nameOfCardAttribute,
			 * 		...
			 * }
			 */
			var presets = me.widgetConf.attributeMappingForCreation || {};
			var fields = me.clientForm.getFields();

			var values = {
				Id: -1, // to have a new card
				IdClass: classId
			}

			fields.each(function(field) {
				if (field._belongToEditableSubpanel
						&& presets[field.name]) {

					var cardAttributeName = presets[field.name];
					var cardAttributePresetValue = field.getValue();
					if (typeof cardAttributePresetValue != "undefined") {
						values[cardAttributeName] = cardAttributePresetValue;
					}
				}
			})

			_debug("Create card with presets", values);

			me.card = new CMDBuild.DummyModel(values);
			me.loadCard();
		} else {
			me.loadCard(loadRemoteData = true, {
				Id: me.cardId,
				IdClass: classId
			});
		}
	}

	function normalizeIdCard(idCard) {
		if (typeof idCard == "string") {
			idCard = parseInt(idCard);
			if (isNaN(idCard)) {
				return null;
			}

			return idCard;
		}

		return null;
	}

	function updateLocalDepsIfReferenceToModifiedClass(me) {
		// we will synch the id of the modifyed
		// card with the reference that points to it
		// This is allowed only if the CQL used to get the id
		// of the card to modify is a simple pointer to a form field,
		// es {client:field_name}

		var referenceRX = /^\{client:(\w+)\}$/;
		var cql = me.getCQLOfTheCardId();
		var match = referenceRX.exec(cql);
		if (match != null) {
			var referenceName = match[1];
			if (referenceName) {
				var field = getFieldByName(me, referenceName);
				if (field &&
					field.CMAttribute) {

					field.store.load({
						callback: function() {
							field.setValue(me.savedCardId);
						}
					});
				}
			}
		}
	}

	function getFieldByName(me, name) {
		return me.clientForm.getFields().findBy(
			function findCriteria(f) {
				if (!f.CMAttribute) {
					return false;
				} else {
					return f.CMAttribute.name == name;
				}
			}
		);
	}

	function onAddCardClick(o) {
		this.cardId = null;
		loadAndFillFields(this, o.classId);
	}
})();