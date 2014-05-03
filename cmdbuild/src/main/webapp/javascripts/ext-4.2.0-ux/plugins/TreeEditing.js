// http://www.sencha.com/forum/showthread.php?138056-TreeEditor-plugin-until-Ext-releases-native

/**
 * <p>
 * Provides edit capabilities for a tree node.
 * </p>
 * <pre><code>
   var tree = new Ext.tree.Panel({
      plugins:[{
         pluginId: 'edit-plug'
         ,ptype: 'treeediting'
      }]
      ...
   });
 * </code></pre>
 * @class Ext.ux.tree.TreeEditing
 * @extends Ext.grid.plugin.CellEditing
 * @license Licensed under the terms of the Open Source <a href="http://www.gnu.org/licenses/lgpl.html">LGPL 3.0 license</a>.  Commercial use is permitted to the extent that the code/component(s) do NOT become part of another Open Source or Commercially licensed development library or toolkit without explicit permission.
 *  
 * @version 0.1 (June 22, 2011)
 * @constructor
 * @param {Object} config 
 */
Ext.define('Ext.ux.tree.TreeEditing', {
    alias: 'plugin.treeediting'
    ,extend: 'Ext.grid.plugin.CellEditing'
    
    
    /**
     * @override
     * @private Collects all information necessary for any subclasses to perform their editing functions.
     * @param record
     * @param columnHeader
     * @returns {Object} The editing context based upon the passed record and column
     */
    ,getEditingContext: function(record, columnHeader) {
        var me = this,
            grid = me.grid,
            store = grid.store,
            rowIdx,
            colIdx,
            view = grid.getView(),
            root = grid.getRootNode(),
            value;

        // If they'd passed numeric row, column indices, look them up.
        if (Ext.isNumber(record)) {
            rowIdx = record;
            record = root.getChildAt(rowIdx);
        } else {
            rowIdx = root.indexOf(record);
        }
        if (Ext.isNumber(columnHeader)) {
            colIdx = columnHeader;
            columnHeader = grid.headerCt.getHeaderAtIndex(colIdx);
        } else {
            colIdx = columnHeader.getIndex();
        }

        value = record.get(columnHeader.dataIndex);
        return {
            grid: grid,
            record: record,
            field: columnHeader.dataIndex,
            value: value,
            row: view.getNode(rowIdx),
            column: columnHeader,
            rowIdx: rowIdx,
            colIdx: colIdx
        };
    }
});
