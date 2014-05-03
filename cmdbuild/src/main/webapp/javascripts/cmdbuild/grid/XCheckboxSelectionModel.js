Ext.ns('CMDBuild.grid');

CMDBuild.grid.XCheckboxSelectionModel = Ext.extend(Ext.selection.RowModel, {
	header: '<div class="x-grid3-hd-checker">&#160;</div>',
	/**
	 * @cfg {Number} width The default width in pixels of the checkbox column (defaults to 22).
	 */
	width: 22,
	/**
	 * @cfg {Boolean} sortable True if the checkbox column is sortable (defaults to false).
	 */
	sortable: false,
	
	// private
	menuDisabled: true,
	hideable: false,
	fixed:true,
	dataIndex: '',
	id: 'checker',
	
	// private
	initEvents : function() {
	 	CMDBuild.grid.XCheckboxSelectionModel.superclass.initEvents.call(this);
	 	this.mon(this.grid, 'render', function(){
	        var view = this.grid.getView();
	        this.mon(view.mainBody, 'mousedown', this.onMouseDown, this);
	        Ext.fly(view.innerHd).on('mousedown', this.onHdMouseDown, this);
	    }, this);
	},
	
	// private
	onMouseDown : function(e, t){
	    if(e.button === 0 && t.className == 'x-grid3-row-checker'){ // Only fire if left-click
	        e.stopEvent();
	        var row = e.getTarget('.x-grid3-row');
	        if(row){
	            var index = row.rowIndex;
	            if(this.isSelected(index)){
	                this.deselectRow(index);
	            }else{
	                this.selectRow(index, true);
	            }
	        }
	    }
	},
	
	// private
    onHdMouseDown : function(e, t){
        if(t.className == 'x-grid3-hd-checker'){
            e.stopEvent();
            var hd = Ext.fly(t.parentNode);
            var isChecked = hd.hasClass('x-grid3-hd-checker-on');
            if(isChecked){
                hd.removeClass('x-grid3-hd-checker-on');
                this.clearSelections();
            }else{
                hd.addClass('x-grid3-hd-checker-on');
                this.selectAll();
            }
        }
    },
	
	// private
	renderer : function(v, p, record){
	    return '<div class="x-grid3-row-checker">&#160;</div>';
	}
});