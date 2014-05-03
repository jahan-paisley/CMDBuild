(function() {

	Ext.define('CMDBuild.core.fixes.CMFixCheckboxModel', {
		override: 'Ext.selection.CheckboxModel',

		headerText: '&#160;',
		headerAlign: 'center',
		dataIndex: '',

		/**
		 * Fixed to accept also headerText, align, dataIndex - 18/04/2014
		 */
		getHeaderConfig: function() {
			var me = this,
				showCheck = me.showHeaderCheckbox !== false;

			return {
					isCheckerHd: showCheck,
					text: me.headerText,
					width: me.headerWidth,
					align: me.headerAlign,
					sortable: false,
					draggable: false,
					resizable: false,
					hideable: false,
					menuDisabled: true,
					dataIndex: me.dataIndex,
					cls: showCheck ? Ext.baseCSSPrefix + 'column-header-checkbox ' : '',
					renderer: Ext.Function.bind(me.renderer, me),
					editRenderer: me.editRenderer || me.renderEmpty,
					locked: me.hasLockedHeader()
			};
		},

		/**
		 * Fixed to align center checkboxes - 18/04/2014
		 */
		renderer: function(value, metaData, record, rowIndex, colIndex, store, view) {
			var baseCSSPrefix = Ext.baseCSSPrefix;
			metaData.tdCls = baseCSSPrefix + 'grid-cell-special ' + baseCSSPrefix + 'grid-cell-row-checker';
			return '<div class="' + baseCSSPrefix + 'grid-row-checker" style="margin: 0px auto;">&#160;</div>';
		}
	});

})();
