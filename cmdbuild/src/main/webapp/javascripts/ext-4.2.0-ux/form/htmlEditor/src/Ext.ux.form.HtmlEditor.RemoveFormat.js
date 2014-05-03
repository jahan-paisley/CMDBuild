/**
 * @author Shea Frederick - http://www.vinylfox.com
 * @class Ext.ux.form.HtmlEditor.RemoveFormat
 * @extends Ext.ux.form.HtmlEditor.MidasCommand
 * <p>A plugin that creates a button on the HtmlEditor that will remove all formatting on selected text.</p>
 *
 * ExtJS4 adaptation by René Bartholomay <rene.bartholomay@gmx.de>
 * Localization adaptation by Tecnoteca
 */
Ext.define('Ext.ux.form.HtmlEditor.RemoveFormat', {
	extend : 'Ext.ux.form.HtmlEditor.MidasCommand',
	langToolTip : 'Remove Formatting',
	langTitle : 'Remove Formatting',
	constructor : function(conf) {
		Ext.apply(this, conf);
		this.midasBtns = ['|', {
			enableOnSelection : true,
			cmd : 'removeFormat',
			tooltip : {
				text : this.langToolTip
			},
			overflowText : this.langTitle
		}];

		this.callParent(arguments);
	}
});
