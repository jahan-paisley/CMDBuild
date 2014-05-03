Ext.ns('CMDBuild.Msg');

CMDBuild.Msg.alert = function(title, text, popup, iconCls) {
	title = title || "&nbsp";
	var win;
	if (popup) {
		win = Ext.Msg.show({
     	   title: title,
     	   msg: text,
     	   width: 300,
     	   buttons: Ext.MessageBox.OK,
     	   icon: iconCls
     	});
	} else {
		win = new Ext.ux.Notification({
				iconCls: iconCls,
				title: title,
				html: text,
				autoDestroy: true,
				hideDelay:  5000,
				shadow: false
			}).show(document);
	}
	return win;
};

CMDBuild.Msg.success = function() {
	CMDBuild.Msg.alert("", CMDBuild.Translation.common.success, false, Ext.MessageBox.INFO);
};

CMDBuild.Msg.info = function(title, text, popup) {
	CMDBuild.Msg.alert(title, text, popup, Ext.MessageBox.INFO);
};

CMDBuild.Msg.warn = function(title, text, popup) {
	title = title || CMDBuild.Translation.errors.warning_message || "Warning";
	CMDBuild.Msg.alert(title, text, popup, Ext.MessageBox.WARNING);
};

CMDBuild.Msg.DetailList= [];

CMDBuild.Msg.error = function(title, body, popup) {
	var text = body;
	title = title || CMDBuild.Translation.errors.error_message || "Error";

	if (typeof body == "object") {
		text = body.text;
		if (body.detail) {
			text += buildDetailLink(CMDBuild.Msg.DetailList.length, body.detail);
		}
	}

	CMDBuild.Msg.alert(title, text, popup, Ext.MessageBox.ERROR);
	// TODO try to remove the stack-trace to the array CMDBuild.Msg.DetailList
	// there are some problems because the Ext.Msg.show object doesn't listen events
};

function buildDetailLink(id, stacktrace) {
	CMDBuild.Msg.DetailList[id] = stacktrace;
	var linkClass = "show_detail_link",
		template = '<p class="{0}" id="detail_{1}" onClick="buildDetaiWindow(this.id)">{2}</p>';

	return Ext.String.format(template, linkClass, id, CMDBuild.Translation.errors.show_detail);
};

function buildDetaiWindow(id) {
	var index = id.split("_")[1];
	var htmlTemplate = '<pre style="padding:5px; font-size: 1.2em">{0}</pre>';

	var win = new CMDBuild.PopupWindow({
		title: CMDBuild.Translation.errors.detail,
		items: [{
			xtype: 'panel',
			border: false,
			autoScroll: true,
			html: Ext.String.format(htmlTemplate, CMDBuild.Msg.DetailList[index])
		}],
		buttonAlign: 'center',
		buttons: [{
			text: CMDBuild.Translation.common.buttons.close,
			handler: function() {
				win.destroy();
			}
		}]
	}).show();
};