(function() {

	var tr = CMDBuild.Translation.management.findfilter;

	Ext.define('CMDBuild.view.management.common.filter.CMFilterAttributes.AttributeFieldsetDelegate', {
		/**
		 * @param {CMDBuild.view.management.common.filter.CMFilterAttributes.AttributeFieldset} fieldset the fieldset that is empty
		 */
		onAttributeFieldsetIsEmpty: Ext.emptyFn
	});

	Ext.define('CMDBuild.view.management.common.filter.CMFilterAttributes', {
		extend: 'Ext.form.Panel',

		mixins: {
			attributeFieldsetDelegate: 'CMDBuild.view.management.common.filter.CMFilterAttributes.AttributeFieldsetDelegate'
		},

		title: tr.attributes,
		autoScroll: true,

		// configuration
			attributes: {}, // the attributes to use in the menu to set filtering over attribute values
			readOnly: false, // set true to have no menu with attributes and use the panel to only display the current filter
		// configuration

		initComponent:function() {
			this.fieldsetCategory = {};

			var tbar = [];

			if (!this.readOnly) {
				this.menu = Ext.create('Ext.menu.Menu');

				fillMenu(this);
				tbar.push({
					text: tr.title,
					iconCls: 'add',
					menu: this.menu
				});
			}

			if (this.filterButton) {
				tbar.push('->');
				tbar.push(this.filterButton);
				this.resetFilterButton = Ext.create('Ext.button.Button', {
					text: CMDBuild.Translation.management.findfilter.clear_filter,
					iconCls: 'delete'
				});
				tbar.push(this.resetFilterButton);
			}

			this.bodyCls = 'x-panel-default-framed';

			this.layout = {
				type: 'vbox',
				align: 'stretch'
			};

			this.defaults = {
				padding: '5 5 0 5'
			};

			this.tbar = tbar;
			this.items = [];

			this.mon(this, 'added', function(me) {
				// Needed because the zIndexParent is not set for the menu, because when created is not owned in a floating element
				if (me.menu)
					me.menu.registerWithOwnerCt();
			});

			this.callParent(arguments);
		},

		updateMenuForClassId: function(classId) {
			if (this.readOnly)
				return;

			this.currentClassId = classId;
			_CMCache.getAttributeList(classId, Ext.bind(function(attributes) {
				this.attributes = attributes;
				fillMenu(this);
			}, this));
		},

		removeAllFieldsets: function() {
			this.removeAll();
			this.fieldsetCategory = {};
		},

		cleanFildsetCategory: function() {
			this.fieldsetCategory = {};
		},

		getData: function() {
			var data = [];
			var out = {};

			this.items.each(function(i) {
				if (typeof i.getData == 'function')
					data.push(i.getData());
			});

			if (data.length == 1) {
				out =  data[0];
			} else if (data.length > 1) {
				out = { and: data };
			}

			return out;
		},

		/*
		 * Data is an object with a single key [simple | and | or]
		 * simple is the object that actually contains the configuration of
		 * a filter chunk, and and or are array of other object with the
		 * same configuration
		 *
		 * example
		 * 	{
		 *		and: [{
		 *			or: [{
		 *				simple: {
		 *					attribute: 'Code',
		 *					operator: 'contain',
		 *					value: ['01']
		 *				}
		 *			},{
		 *				simple: {
		 *					attribute: 'Code',
		 *					operator: 'contain',
		 *					value: ['02']
		 *				}
		 *			}]
		 *		}, {
		 *			simple: {
		 *				attribute: 'Description',
		 *				operator: 'contain',
		 *				value: ['The']
		 *			}
		 *		}]
		 *	}
		 */
		setData: function(data) {
			addData(this, data);
		},

		// as attributeFieldsetDelegate
		onAttributeFieldsetIsEmpty: function(fieldset) {
			this.remove(fieldset);

			delete this.fieldsetCategory[fieldset.attributeName];
		}
	});

	function addData(me, data) {
		if (data.simple) {
			addSimpleData(me, data);
		} else {
			addCompositeData(me, data);
		}
	}

	function addSimpleData(me, data) {
		if (!data || !data.simple)
			return;

		var attributeName = data.simple.attribute || '';
		var attribute = _CMUtils.arraySearchByFunction(me.attributes, function(currentAttribute) {
			return currentAttribute.name == attributeName;
		});

		if (attribute)
			addFilterCondition(me, attribute, data.simple);
	}

	function addCompositeData(me, compositeData) {
		var data = compositeData.or || compositeData.and || [];

		for (var i = 0, l = data.length; i < l; ++i)
			addData(me, data[i]);
	}

	function fillMenu(me) {
		var submenues = buildSubMenues(me);

		me.menu.removeAll();

		if (submenues.length == 1) {
			me.menu.add(submenues[0].menu);
		} else {
			me.menu.add(submenues);
		}
	}

	function buildSubMenues(me) {
		var submenues = [];
		var groupedAttr = CMDBuild.Utils.groupAttributes(me.attributes, allowNoteFiled = false);

		for (var group in groupedAttr) {
			var items = [];
			var attrs = groupedAttr[group];

			for (var i = 0, l = attrs.length; i < l; ++i) {
				items.push({
					text: attrs[i].description,
					attribute: attrs[i],
					handler: function() {
						addFilterCondition(me, this.attribute);
					}
				});
			}

			submenues.push({
				text: group,
				menu: items
			});
		}

		return submenues;
	}

	function addFilterCondition(me, attribute, data) {
		var category = attribute.name;

		Ext.suspendLayouts();

		if (typeof me.fieldsetCategory[category] == 'undefined' ) {
			var fieldset = Ext.create('CMDBuild.view.management.common.filter.CMFilterAttributes.AttributeFieldset', {
				title: attribute.description,
				attributeName: category
			});

			fieldset.addDelegate(me);
			me.fieldsetCategory[category] = fieldset;
			me.add(fieldset);
		}

		var filterCondition = Ext.create('CMDBuild.Management.FieldManager.getFieldSetForFilter', attribute);
		me.fieldsetCategory[category].addCondition(filterCondition);
		filterCondition.setData(data);

		Ext.resumeLayouts();
		me.doLayout();
	}

	Ext.define('CMDBuild.view.management.common.filter.CMFilterAttributes.AttributeFieldset', {
		extend: 'Ext.form.FieldSet',

		// configuration
			attributeName: '',
		// configuration

		mixins: {
			delegable: 'CMDBuild.core.CMDelegable',
			conditionDelegate: 'CMDBuild.view.management.common.filter.CMFilterAttributeConditionPanelDelegate'
		},

		constructor: function() {
			this.mixins.delegable.constructor.call(this, 'CMDBuild.view.management.common.filter.CMFilterAttributes.AttributeFieldsetDelegate');

			this.callParent(arguments);
		},

		initComponent: function() {
			this.defaults = { padding: '0 0 5 0' };
			this.callParent(arguments);
		},

		addCondition: function(condition) {
			condition.addDelegate(this);

			if (this.items.length >= 1)
				this.items.last().showOr();

			Ext.suspendLayouts();
			this.add(condition);
			Ext.resumeLayouts();
		},

		getData: function() {
			var data = [];
			var out = {};

			this.items.each(function(i) {
				if (typeof i.getData == 'function')
					data.push(i.getData());
			});

			if (data.length == 1) {
				out = data[0];
			} else if (data.length > 1) {
				out = {
					or: data
				};
			}

			return out;
		},

		// as conditionDelegate
		onFilterAttributeConditionPanelRemoveButtonClick: function(condition) {
			Ext.suspendLayouts();
			this.remove(condition);
			Ext.resumeLayouts();

			var count = this.items.length;

			if (count > 0) {
				this.items.last().hideOr();
			} else {
				this.callDelegates('onAttributeFieldsetIsEmpty', this);
			}
		}
	});

})();