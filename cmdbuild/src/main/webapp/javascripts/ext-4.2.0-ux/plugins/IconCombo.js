Ext.ns('Ext.ux.plugins');
 
/**
 * Ext.ux.plugins.IconCombo plugin for Ext.form.Combobox
 *
 * Modified to allow a class prefix
 *
 * @author  Ing. Jozef Sakalos
 * @date    January 7, 2008
 *
 * @class Ext.ux.plugins.IconCombo
 * @extends Ext.util.Observable
 */
Ext.ux.plugins.IconCombo = function(config) {
    Ext.apply(this, config);
};

Ext.extend(Ext.ux.plugins.IconCombo, Ext.util.Observable, {
    init: function(combo) {
    	var iconClsPrefix = combo.iconClsPrefix || '';
        Ext.apply(combo, {
            tpl:  '<tpl for=".">'
                + '<div class="x-combo-list-item ux-icon-combo-item '
                + iconClsPrefix + '{' + combo.iconClsField + '}">'
                + '{' + combo.displayField + '}'
                + '</div></tpl>',
 
            onRender: combo.onRender.createSequence(function(ct, position) {
                // adjust styles
                this.wrap.applyStyles({position:'relative'});
                this.el.addClass('ux-icon-combo-input');
                // add div for icon
                this.icon = Ext.DomHelper.append(this.el.up('div.x-form-field-wrap'), {
                    tag: 'div', style:'position:absolute'
                });
            }),
 
            setIconCls: function() {
                var rec = this.store.query(this.valueField, this.getValue()).itemAt(0);
                if(rec) {
                    this.icon.className = 'ux-icon-combo-icon ' + iconClsPrefix + rec.get(this.iconClsField);
                }
            },
 
            setValue: combo.setValue.createSequence(function(value) {
                this.setIconCls();
            })
        });
    }
});
