Ext.ns('CMDBuild.data');

CMDBuild.data.XGroupingStore = Ext.extend(Ext.data.GroupingStore, {
	loadGroup: function(groupStartingRow) {
		var groupValue = this.getAt(groupStartingRow).get(this.groupField);
		var params = {};
		params[this.groupParameter] = groupValue;
		this.suspendEvents();
		this.remove(this.getAt(groupStartingRow));
		this.resumeEvents();
		this.load({
				params : params,
				add: true,
				position: groupStartingRow
			});
	},

	// private
    // Called as a callback by the Reader during a load operation.
    loadRecords : function(o, options, success){
        if(!o || success === false){
            if(success !== false){
                this.fireEvent("load", this, [], options);
            }
            if(options.callback){
                options.callback.call(options.scope || this, [], options, false);
            }
            return;
        }
        var r = o.records, t = o.totalRecords || r.length;
        if(!options || options.add !== true){
            if(this.pruneModifiedRecords){
                this.modified = [];
            }
            for(var i = 0, len = r.length; i < len; i++){
                r[i].join(this);
            }
            if(this.snapshot){
                this.data = this.snapshot;
                delete this.snapshot;
            }
            this.data.clear();
            this.data.addAll(r);
            this.totalLength = t;
            this.applySort();
            this.fireEvent("datachanged", this);
        }else{
            this.totalLength = Math.max(t, this.data.length+r.length);
            if (typeof options.position === "undefined")
            	this.add(r);
            else
            	this.insert(options.position, r);
        }
        this.fireEvent("load", this, r, options);
        if(options.callback){
            options.callback.call(options.scope || this, r, options, true);
        }
    }
});
