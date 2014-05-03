/**
 * @class Ext.ux.CheckColumn
 * @extends Ext.grid.column.Column
 * <p>A Header subclass which renders a checkbox in each column cell which toggles the truthiness of the associated data field on click.</p>
 * <p><b>Note. As of ExtJS 3.3 this no longer has to be configured as a plugin of the GridPanel.</b></p>
 * <p>Example usage:</p>
 * <pre><code>
// create the grid
var grid = Ext.create('Ext.grid.Panel', {
    ...
    columns: [{
           text: 'Foo',
           ...
        },{
           xtype: 'checkcolumn',
           text: 'Indoor?',
           dataIndex: 'indoor',
           width: 55
        }
    ]
    ...
});
 * </code></pre>
 * In addition to toggling a Boolean value within the record data, this
 * class adds or removes a css class <tt>'x-grid-checked'</tt> on the td
 * based on whether or not it is checked to alter the background image used
 * for a column.
 */
Ext.define('Ext.ux.CheckColumn', {
    extend: 'Ext.grid.column.Column',
    alias: 'widget.checkcolumn',

    constructor: function() {
        this.addEvents(
            /**
             * @event checkchange
             * Fires when the checked state of a row changes
             * @param {Ext.ux.CheckColumn} this
             * @param {Number} rowIndex The row index
             * @param {Boolean} checked True if the box is checked
             */
            'checkchange'
        );
        this.callParent(arguments);
    },

    /**
     * @private
     * Process and refire events routed from the GridView's processEvent method.
     */
    processEvent: function(type, view, cell, recordIndex, cellIndex, e) {
    	if (this.cmReadOnly === true) {
    		return;
    	}

        if (type == 'mousedown' || (type == 'keydown' && (e.getKey() == e.ENTER || e.getKey() == e.SPACE))) {
            var record = view.panel.store.getAt(recordIndex),
                dataIndex = this.dataIndex,
                checked = !record.get(dataIndex);
                
            record.set(dataIndex, checked);
            this.fireEvent('checkchange', this, recordIndex, checked);
            // cancel selection.
            return false;
        } else {
            return this.callParent(arguments);
        }
    },

    // Note: class names are not placed on the prototype bc renderer scope
    // is not in the header.
    renderer : function(value) {
        var cssPrefix = Ext.baseCSSPrefix,
            cls = [cssPrefix + 'grid-checkheader'];

        if (CMDBuild.Utils.evalBoolean(value)) {
            cls.push(cssPrefix + 'grid-checkheader-checked');
        }
        return '<div class="' + cls.join(' ') + '">&#160;</div>';
    }
});


Ext.override(Ext.ux.CheckColumn,{

	/*
	 * Add a flag to have a read-only column
	 * */
	cmReadOnly: false,

	/*
	 * Add a flag to have configure the column as exclusive check
	 * */
	cmExclusive: false,

	/*
	 * Manage the read-only flag cmReadOnly
	 * Manage the exclusivity flag cmExclusive
	 * Manage a TreeStore, not supported from the overridden class
	 * */
	processEvent : function(type, view, cell, recordIndex, cellIndex, e, node) {
		var store = view.panel.store;
		var dataIndex = this.dataIndex;
		var record;
		var checked;

		if (this.cmReadOnly === true) {
			return;
		}

		if (type == 'mousedown'
				|| (type == 'keydown' && (e.getKey() == e.ENTER
				|| e.getKey() == e.SPACE))) {

			if (Ext.getClassName(store) == "Ext.data.TreeStore") {
				record = node;
			} else {
				record = view.panel.store.getAt(recordIndex);
			}

			checked = !record.get(dataIndex);

			if (checked && this.cmExclusive) {
				uncheckAll(store, dataIndex);
			}

			try {
				record.set(dataIndex, checked);
				record.commit();
			} catch (error) {
				// may be rendering issues
			}

			this.fireEvent('checkchange', this, recordIndex, checked);
			return false; // cancel selection.
		} else {
			return this.callParent(arguments);
		}
	}
});

function uncheckAll(store, dataIndex) {
	if (Ext.getClassName(store) == "Ext.data.TreeStore") {
		uncheckTree(store.getRootNode(), dataIndex);
	} else {
		store.each(function(item) {
			item.set(dataIndex, false);
			item.commit();
		});
	}
}

function uncheckTree(root, dataIndex) {
	if (root) {
		root.set(dataIndex, false);
		root.commit();
	}

	var children = root.childNodes || root.children || [];
	for (var i=0, l=children.length; i<l; ++i) {
		uncheckTree(children[i], dataIndex);
	}
}