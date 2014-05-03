(function() {

Ext.define("CMDBuild.Administration.AttributeSortingGrid", {
	extend: "Ext.grid.Panel",
	translation: CMDBuild.Translation.administration.modClass.attributeProperties,
	filtering: false,

	initComponent:function() {

		this.store = new Ext.data.Store({
			fields: [ // TODO
				"name", "description", "absoluteClassOrder", "classOrderSign"
			],
			autoLoad : false,
			proxy : {
				type : 'ajax',
				url : 'services/json/schema/modclass/getattributelist',
				reader : {
					type : 'json',
					root : 'attributes'
				}
			},
			sorters : [ {
				property : 'absoluteClassOrder',
				direction : "ASC"
			}]
		});
 
		var tr = this.translation;
		var comboOrderSign = new Ext.form.field.ComboBox({
			typeAhead : true,
			triggerAction : 'all',
			selectOnTab : true,
			valueField : "value",
			displayField : "description",
			listClass : 'x-combo-list-small',
			queryMode: "local",
			store : Ext.create('Ext.data.Store', {
				fields: ["value", "description"],
				data: [
					{value: 1, description: tr.direction.asc },
					{value:-1, description: tr.direction.desc },
					{value:0, description:  tr.not_in_use }
				]
			})
		});

		this.columns = [
			{
				id : 'absoluteClassOrder',
				hideable : false,
				hidden : true,
				dataIndex : 'absoluteClassOrder'
			},
			{
				id : 'name',
				header : this.translation.name,
				dataIndex : 'name',
				flex: 1
			},
			{
				id : 'description',
				header : this.translation.description,
				dataIndex : 'description',
				flex: 1
			},
			{
				header : this.translation.criterion,
				dataIndex : 'classOrderSign',
				renderer : Ext.Function.bind(comboRender, this, [], true),
				flex: 1,
				field: comboOrderSign
			}
		];

		this.plugins = [Ext.create('Ext.grid.plugin.CellEditing', {
			clicksToEdit: 1
		})];

		this.viewConfig = {
			plugins : {
				ptype : 'gridviewdragdrop',
				dragGroup : 'dd',
				dropGroup : 'dd'
			}
		};

		this.callParent(arguments);

		var parameterNames = CMDBuild.ServiceProxy.parameter;
		var params = {};
		params[parameterNames.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.idClass);

		this.getStore().load({
			params: params
		});

		this.getEditor = function() {
			return comboOrderSign;
		};
	}
});

function comboRender(value, meta, record, rowIndex, colIndex, store) {
	if (value > 0) {
		return '<span>'+ this.translation.direction.asc +'</span>';
	} else if (value < 0) {
		return '<span>'+ this.translation.direction.desc +'</span>';
	} else {
		return '<span>'+ this.translation.not_in_use +'</span>';
	}
}

})();