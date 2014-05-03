Ext.define("CMDBuild.management.model.widget.LinkCardsConfigurationReader", {
	statics: {
		id: function(w) {
			return w.id;
		},
		filter: function(w) {
			return w.filter;
		},
		className: function(w) {
			return w.className;
		},
		defaultSelection: function(w) {
			return w.defaultSelection;
		},
		readOnly: function(w) {
			return w.readOnly;
		},
		singleSelect: function(w) {
			return w.singleSelect;
		},
		allowCardEditing: function(w) {
			return w.allowCardEditing;
		},
		required: function(w) {
			return w.required;
		},
		enableMap: function(w) {
			return w.enableMap;
		},
		mapLatitude: function(w) {
			return w.mapLatitude;
		},
		mapLongitude: function(w) {
			return w.mapLongitude;
		},
		mapZoom: function(w) {
			return w.mapZoom;
		},
		label: function(w) {
			return w.label;
		},
		templates: function(w) {
			return w.templates || {};
		}
	}
});


Ext.define("CMDBuild.management.model.widget.ManageRelationConfigurationReader", {
	statics: {
		id: function(w) {
			return w.id;
		},
		domainName: function(w) {
			return w.domainName;
		},
		className: function(w) {
			return w.className;
		},
		cardCQLSelector: function(w) {
			return w.cardCQLSelector;
		},
		required: function(w) {
			return w.required;
		},
		multiSelection: function(w) {
			return w.multiSelection;
		},
		singleSelection: function(w) {
			return w.singleSelection;
		},
		canCreateRelation: function(w) {
			return w.canCreateRelation;
		},
		canModifyARelation: function(w) {
			return w.canModifyARelation;
		},
		canRemoveARelation: function(w) {
			return w.canRemoveARelation;
		},
		canCreateAndLinkCard: function(w) {
			return w.canCreateAndLinkCard;
		},
		canModifyALinkedCard: function(w) {
			return w.canCreateAndLinkCard;
		},
		canDeleteALinkedCard: function(w) {
			return w.canDeleteALinkedCard;
		},
		source: function(w) {
			return w.source;
		},
		label: function(w) {
			return w.label;
		}
	}
});

Ext.define("CMDBuild.management.model.widget.ManageEmailConfigurationReader", {
	statics: {
		FIELDS: {
			ID: 'id',
			STATUS: 'status',
			BEGIN_DATE: 'date',
			FROM_ADDRESS: 'fromAddress',
			TO_ADDRESS: 'toAddresses',
			CC_ADDRESS: 'ccAddresses',
			SUBJECT: 'subject',
			CONTENT: 'content',
			CONDITION: 'condition'
		},

		id: function(w) {
			return w.id;
		},
		required: function(w) {
			return w.required;
		},
		readOnly: function(w) {
			return w.readOnly;
		},
		label: function(w) {
			return w.label;
		},
		templates: function(w) {
			return w.templates || {};
		},
		emailTemplates: function(w) {
			return w.emailTemplates || {};
		}
	}
});

Ext.define("CMDBuild.controller.management.common.widgets.CMCalendarControllerWidgetReader", {
	getStartDate : function(w) {
		return w.startDate;
	},
	getEndDate : function(w) {
		return w.endDate;
	},
	getTitle : function(w) {
		return w.eventTitle;
	},
	getEventClass : function(w) {
		return w.eventClass;
	},
	getFilterVarName : function(w) {
		return "filter";
	},
	getDefaultDate : function(w) {
		return w.defaultDate;
	}
});