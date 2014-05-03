(function() {
	Ext.namespace('CMDBuild.grid');
	CMDBuild.grid.CMCheckboxSelectionModel = Ext.extend(Ext.selection.CheckboxModel, {
		constructor: function(){
			CMDBuild.grid.CMCheckboxSelectionModel.superclass.constructor.apply(this, arguments);
			
			this.persistentSelections = {};
			
			this.CM_MODES = {
	        	"NORMAL": "normal",
	        	"INVERTED": "inverted"
	        };
	        
	        this.CMSelectionMode = this.CM_MODES.NORMAL;
	        
	        this.on("rowselect", function(sm, index, rec) {
	        	var key = rec.json.Id;
	        	if (this.CMSelectionMode == this.CM_MODES.NORMAL) {
	        		this.persistentSelections[key] = rec;
	        	} else {
	        		delete this.persistentSelections[key];
	        	}
	        }, this);
	        
	        this.on("rowdeselect", function(sm, index, rec) {
	        	var key = rec.json.Id;
	        	if (this.CMSelectionMode == this.CM_MODES.NORMAL) {
	        		delete this.persistentSelections[key];
	        	} else {
	        		this.persistentSelections[key] = rec;
	        	}
	        }, this);
	    },
	    
	    isInverted: function() {
	    	return this.CMSelectionMode == this.CM_MODES.INVERTED;
	    },
	    
	    getFromPersistentSelection: function(key) {
	    	return this.persistentSelections[key];
	    },
	    
		onHdMouseDown: function(e, t) {	    	
	    	CMDBuild.grid.CMCheckboxSelectionModel.superclass.onHdMouseDown.apply(this, arguments);	    	
	    	toggleCMSelectionMode.call(this);
	    	this.persistentSelections = {};
		},
		
		onRefresh: function() {
			CMDBuild.grid.CMCheckboxSelectionModel.superclass.onRefresh.apply(this, arguments);
			
			var ds = this.grid.store, index, key;
			this.silent = true;
	        this.clearSelections(true);
	        ds.each(function checkRecordOnRefresh(r) {
	        	var isToCheck = false;
	        	key = r.json.Id;
	        	isToCheck = (this.isInverted() && !this.getFromPersistentSelection(key)) ||
	        				(!this.isInverted() && this.getFromPersistentSelection(key));
	        	if (isToCheck) {
	        		index = ds.indexOfId(r.id);
	        		this.selectRow(index, true);
	        	}
	        }, this);
	        this.silent = false;
			
			if (this.isInverted()) {
				checkHeader.call(this);
			}
			
		},
		
		getPersistentSelections: function() {
			return Ext.apply({}, this.persistentSelections);
		},
		
		getPersistentSelectionsAsArray: function() {
			var out = [];
			for (var key in this.persistentSelections) {
				out.push(this.persistentSelections(key));
			}
			return out;
		},
		
		clearPersistentSelections: function() {
			this.persistentSelections = {};
			this.CMSelectionMode = this.CM_MODES.NORMAL;
		}
	});
	
	function toggleCMSelectionMode() {
		if (this.isInverted()) {		
			this.CMSelectionMode = this.CM_MODES.NORMAL;
		} else {
			this.CMSelectionMode = this.CM_MODES.INVERTED;
		}
	}
	
	function checkHeader() {
		var gv = this.grid.getView();
		var cm = this.grid.getColumnModel();
		var hd = gv.getHeaderCell(cm.getIndexById(this.id));
		
		hd = Ext.fly(hd.firstChild);
		hd.addClass("x-grid3-hd-checker-on");
	}
})();