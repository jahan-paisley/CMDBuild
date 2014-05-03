Ext.define("CMDBuild.view.administration.dataview.CMSqlDataView", {
	extend: "CMDBuild.view.administration.common.basepanel.CMGridAndFormPanel",

	cmName:'sqldataview',

	title: CMDBuild.Translation.views + " - " + CMDBuild.Translation.sqlView,

	addButtonText: CMDBuild.Translation.addView,
	modifyButtonText: CMDBuild.Translation.modifyView,
	removeButtonText: CMDBuild.Translation.removeView
});
