Ext.define("CMDBuild.view.administration.filter.CMGroupFilterPanel", {
	extend: "CMDBuild.view.administration.common.basepanel.CMGridAndFormPanel",

	cmName:'filterdataview',

	title: CMDBuild.Translation.management.findfilter.set_filter,

	addButtonText: CMDBuild.Translation.addFilter,
	modifyButtonText: CMDBuild.Translation.modifyFilter,
	removeButtonText: CMDBuild.Translation.removeFilter
});