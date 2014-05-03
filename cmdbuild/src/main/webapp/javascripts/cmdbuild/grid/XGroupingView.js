Ext.ns('CMDBuild.grid');

CMDBuild.grid.XGroupingView = Ext.extend(Ext.grid.GroupingView, {

    toggleGroup : function(group, expanded){
		var gel = Ext.fly(group);
        if (gel.hasClass('x-grid-group-notloaded')) {
        	var iel = Ext.fly(group.childNodes[1].firstChild);
        	iel.addClass('x-grid-row-loading');
        	var groupStartingRow = this.findRowIndex(iel.dom);
        	this.ds.loadGroup(groupStartingRow);
        }
        CMDBuild.grid.XGroupingView.superclass.toggleGroup.call(this, group, expanded);
    },

    // private
    doRender : function(cs, rs, ds, startRow, colCount, stripe){
        if(rs.length < 1){
            return '';
        }
        var groupField = this.getGroupField();
        var colIndex = this.cm.findColumnIndex(groupField);

        this.enableGrouping = !!groupField;

        if(!this.enableGrouping || this.isUpdating){
            return CMDBuild.grid.XGroupingView.superclass.doRender.apply(
                    this, arguments);
        }
        var gstyle = 'width:'+this.getTotalWidth()+';';

        var gidPrefix = this.grid.getGridEl().id;
        var cfg = this.cm.config[colIndex];
        var groupRenderer = cfg.groupRenderer || cfg.renderer;
        var prefix = this.showGroupName ?
                     (cfg.groupName || cfg.header)+': ' : '';

        var groups = [], curGroup, i, len, gid;
        for(i = 0, len = rs.length; i < len; i++){
            var rowIndex = startRow + i;
            var r = rs[i],
                gvalue = r.data[groupField],
                g = this.getGroup(gvalue, r, groupRenderer, rowIndex, colIndex, ds);
            if(!curGroup || curGroup.group != g){
                gid = gidPrefix + '-gp-' + groupField + '-' + Ext.util.Format.htmlEncode(g);
               	// if state is defined use it, however state is in terms of expanded
				// so negate it, otherwise use the default.
				var isCollapsed  = typeof this.state[gid] !== 'undefined' ? !this.state[gid] : this.startCollapsed;
				var gcls = isCollapsed ? 'x-grid-group-collapsed' : '';
                curGroup = {
                    group: g,
                    gvalue: gvalue,
                    text: prefix + g,
                    groupId: gid,
                    startRow: rowIndex,
                    rs: [r],
                    cls: gcls,
                    style: gstyle
                };
                groups.push(curGroup);
            }else{
                curGroup.rs.push(r);
            }
            r._groupId = gid;
        }

        var buf = [];
        for(i = 0, len = groups.length; i < len; i++){
            var g = groups[i];
            // added for group lazy loading
            var groupTotal = g.rs[0].data[this.grid.store.groupTotalField];
            var groupLoaded = g.rs.length;
            var groupLoaded = (groupTotal == groupLoaded);
			if (!groupLoaded) {
				g.cls = 'x-grid-group-collapsed x-grid-group-notloaded';
				this.state[g.groupId] = false;
			}
            this.doGroupStart(buf, g, cs, ds, colCount);
            buf[buf.length] = Ext.grid.GridView.prototype.doRender.call(
	                    this, cs, g.rs, ds, g.startRow, colCount, stripe);
            this.doGroupEnd(buf, g, cs, ds, colCount);
        }
        return buf.join('');
    },

	resetState : function() {
		this.state = [];
	}
});
