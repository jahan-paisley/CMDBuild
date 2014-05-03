(function () {
	var reader = CMDBuild.management.model.widget.ManageEmailConfigurationReader;
	var fields = reader.FIELDS;

	Ext.define("CMDBuild.controller.management.common.widgets.CMManageEmailController", {

		mixins: {
			observable: "Ext.util.Observable",
			widgetcontroller: "CMDBuild.controller.management.common.widgets.CMWidgetController",
			emailgriddelegate: "CMDBuild.view.management.common.widgets.CMEmailGridDelegate",
			attachmentPickerDelegate: "CMDBuild.view.management.common.widgets.CMDMSAttachmentPickerDelegate"
		},

		statics: {
			WIDGET_NAME: ".ManageEmail"
		},

		TEMPLATE_FIELDS: ['toAddresses','ccAddresses','subject','content','condition', 'notifyWith', 'fromAddress'],
		TEMPLATE_CONDITION: 'condition',

		constructor: function(view, supercontroller, widget, clientForm, card) {

			this.mixins.observable.constructor.call(this);
			this.mixins.widgetcontroller.constructor.apply(this, arguments);

			this.reader = CMDBuild.management.model.widget.ManageEmailConfigurationReader;

			this.emailsWereGenerated = false;
			this.gridStoreWasLoaded = false;

			this.emailTemplatesData = _extractVariablesForTemplateResolver(this);
			this.readWrite = !this.reader.readOnly(widget);

			var xavars = Ext.apply({}, this.reader.templates(this.widgetConf), this.emailTemplatesData);

			this.templateResolver = new CMDBuild.Management.TemplateResolver({
				clientForm: clientForm,
				xaVars: xavars,
				serverVars: this.getTemplateResolverServerVars()
			});

			this.view.setDelegate(this);
		},

		// override
		/*
		 * If the grid is already loaded add the emails generated from the templates
		 * (if there are templates, and if the email are not already generated).
		 * Otherwise, load the grid before. 
		 */
		beforeActiveView: function() {
			var pi = _CMWFState.getProcessInstance();
			if (!this.gridStoreWasLoaded) {
				this.view.getEl().mask(CMDBuild.Translation.common.wait_title);
				this.view.emailGrid.store.load({
					params: {
						ProcessId: pi.getId()
					},
					scope: this,
					callback: function(records, operation, success) {
						this.gridStoreWasLoaded = true;
						this.view.getEl().unmask();
						this.addEmailFromTemplateIfNeeded();
					}
				});
			} else {
				this.addEmailFromTemplateIfNeeded();
			}
		},

		/*
		 * Resolve the template only if there are
		 * no draft mails, because the draft mails are
		 * saved from this step, and assume that
		 * the user has already modified the template
		 * for this step.
		 */
		addEmailFromTemplateIfNeeded: function() {
			if (this.emailsWereGenerated) {
				return;
			}

			if (this.readWrite
					&& this.thereAreTemplates()
					&& !this.view.hasDraftEmails()) {

				_createEmailFromTemplate(this);
			}
		},

		thereAreTemplates: function() {
			return this.countTemplates() > 0;
		},

		countTemplates: function() {
			var t = this.reader.emailTemplates(this.widgetConf) || [];
			return t.length;
		},

		removeUnsentEmails: function() {
			var emailToRemove = [].concat(this.view.getNewEmails())
				.concat(this.view.getDraftEmails());

			for (var i=0, l=emailToRemove.length, e=null; i<l; ++i) {
				e = emailToRemove[i];
				this.view.removeRecord(e);
			}
		},

		// override
		getData: function(isAdvance) {
			return {
				Updated: this.view.getOutgoing(true),
				Deleted: this.view.getDeletedEmails()
			};
		},

		// override
		isValid: function() {
			return !(this.reader.required(this.widgetConf)
				&& this.getOutgoing().length == 0);
		},

		// override
		isBusy: function() {
			this.addEmailFromTemplateIfNeeded();
			return this.busy;
		},

		// as emailgriddelegate

		onUpdateTemplatesButtonClick: function() {
			this.removeUnsentEmails(); // New and Draft
			this.emailsWereGenerated = false;
			this.addEmailFromTemplateIfNeeded();
		},

		onAddEmailButtonClick: function(emailGrid, emailRecord) {
			new CMDBuild.view.management.common.widgets.CMEmailWindow({
				emailGrid: emailGrid,
				delegate: this,
				record: emailRecord
			}).show();
		},

		onModifyEmailIconClick: function(emailGrid, emailRecord) {
			new CMDBuild.view.management.common.widgets.CMEmailWindow({
				emailGrid: emailGrid,
				delegate: this,
				readOnly: false,
				record: emailRecord
			}).show();
		},

		// as CMEmailWindow Delegate

		onCMEmailWindowAttachFileChanged: function(emailWindow, form, emailRecord) {
			if (emailRecord.isNew()) {
				var temporaryId = emailRecord.get("temporaryId");
				var params = {};
				if (temporaryId) {
					params.temporaryId = temporaryId;
				}
				CMDBuild.ServiceProxy.email.addAttachmentFromNewEmail(form, {
					params: params,
					success: function(fp, o) {
						emailRecord.set("temporaryId", o.result.temporaryId);
						emailWindow.addAttachmentPanel(o.result.fileName, emailRecord);
					}
				});
			} else {
				CMDBuild.ServiceProxy.email.addAttachmentFromExistingEmail(form, {
					params: {
						emailId: emailRecord.getId()
					},
					success: function(fp, o) {
						emailWindow.addAttachmentPanel(o.result.fileName, emailRecord);
					}
				});
			}
		},

		onCMEmailWindowRemoveAttachmentButtonClick: function(attachmentPanel) {
			var proxyFn = null;
			var emailRecord = attachmentPanel.referredEmail;
			var params = {
				fileName: attachmentPanel.fileName
			};

			if (emailRecord.isNew()) {
				params.temporaryId = emailRecord.get("temporaryId");
				proxyFn = CMDBuild.ServiceProxy.email.removeAttachmentFromNewEmail;
			} else {
				params.emailId = emailRecord.getId();
				proxyFn = CMDBuild.ServiceProxy.email.removeAttachmentFromExistingEmail;
			}

			proxyFn({
				params: params,
				success: function() {
					attachmentPanel.removeFromEmailWindow();
				}
			});
		},

		onAddAttachmentFromDmsButtonClick: function(emailWindow, emailRecord) {
			new CMDBuild.view.management.common.widgets.CMDMSAttachmentPicker({
				title: CMDBuild.Translation.choose_attachment_from_db,
				emailRecord: emailRecord,
				emailWindow: emailWindow,
				delegate: this
			}).show();
		},

		beforeCMEmailWindowDestroy: function(emailWindow) {
			updateRecord( //
				emailWindow.form, //
				emailWindow.record, //
				emailWindow.attachmentPanelsContainer.getFileNames() //
			);

			this.view.addToStoreIfNotInIt(emailWindow.record);
		},

		// as CMDMSAttachmentPickerDelegate

		onCMDMSAttachmentPickerClassDidSelected: function(dmsAttachmentPicker, classId) {
			var className = _CMCache.getEntryTypeNameById(classId);
			dmsAttachmentPicker.cmState.setClassName(className);

			dmsAttachmentPicker.updateCardGridForClassId(classId);
		},

		/**
		 * @param {CMDBuild.view.management.common.widgets.CMDMSAttachmentPicker} dmsAttachmentPicker,
		 * @param {CMDBuild.management.mail.Model} emailRecord,
		 * @param {CMDBuild.view.management.common.widgets.CMEmailWindow} emailWindow,
		 */
		onCMDMSAttachmentPickerOKButtonClick: function(dmsAttachmentPicker, emailRecord, emailWindow) {
			var data = dmsAttachmentPicker.cmState.getData();
			if (data && data.length == 0) {
				return;
			}

			var encodedAttachments = Ext.JSON.encode(data);
			var params = {
				attachments: encodedAttachments
			};

			if (emailRecord.isNew()) {
				var temporaryId = emailRecord.get("temporaryId");
				if (temporaryId) {
					params.temporaryId = temporaryId;
				}
				CMDBuild.ServiceProxy.email.copyAttachmentFromCardForNewEmail({
					params: params,
					success: function(fp, request, response) {
						emailRecord.set("temporaryId", response.temporaryId);
						updateAttachmentList(response.attachments, emailWindow, emailRecord);
						dmsAttachmentPicker.destroy();
					}
				});
			} else {
				params.emailId = emailRecord.getId();
				CMDBuild.ServiceProxy.email.copyAttachmentFromCardForExistingEmail({
					params: params,
					success: function(fp, request, response) {
						updateAttachmentList(response.attachments, emailWindow, emailRecord);
						dmsAttachmentPicker.destroy();
					}
				});
			}
		},

		/**
		 * @param {CMDBuild.view.management.common.widgets.CMDMSAttachmentPicker} dmsAttachmentPicker,
		 */
		onCMDMSAttachmentPickerCancelButtonClick: function(dmsAttachmentPicker) {
			dmsAttachmentPicker.destroy();
		},

		// as CMCardGridDelegate

		/**
		 * @param {CMDBuild.view.management.common.widgets.CMDMSAttachmentPicker} dmsAttachmentPicker
		 * @param {CMDBuild.view.management.common.CMCardGrid} attachmentGrid
		 * @param {CMDBuild.view.management.common.widgets.CMDMSAttachmentModel} record
		 */
		onCMDMSAttachmentPickerCardDidSelected: function(dmsAttachmentPicker, attachmentGrid, record) {
			var className = record.get("IdClass_value");
			var cardId = record.get("id");

			dmsAttachmentPicker.cmState.setCardId(cardId);
			dmsAttachmentPicker.loadAttachmentsForClassNameAndCardId(className, cardId);
		},

		/**
		 * @param {CMDBuild.view.management.common.widgets.CMDMSAttachmentPicker} dmsAttachmentPicker
		 * @param {String} fileName
		 * @param {Boolean} checked
		 */
		onCMDMSAttachmentPickerAttachmentCheckChange: function(dmsAttachmentPicker, fileName, checked) {
			if (checked) {
				dmsAttachmentPicker.cmState.check(fileName);
			} else {
				dmsAttachmentPicker.cmState.uncheck(fileName);
			}

			console.log(dmsAttachmentPicker.cmState.getData());
		},

		/**
		 * @param {CMDBuild.view.management.common.widgets.CMDMSAttachmentPicker} dmsAttachmentPicker
		 * @param {CMDBuild.view.management.common.widgets.CMDMSAttachmentModel[]} records
		 */
		onCMDMSAttachmentPickerAttachmentsGridDidLoad: function(dmsAttachmentPicker, records) {
			dmsAttachmentPicker.cmState.syncSelection(records);
		},

		/**
		 * @param {CMDBuild.view.management.common.widgets.CMDMSAttachmentPicker} dmsAttachmentPicker
		 */
		onCMDMSAttachmentPickerCardDidLoad: function(dmsAttachmentPicker) {
			dmsAttachmentPicker.cleanAttachmentGrid();
		}
	});

	function updateAttachmentList(attachmentNames, emailWindow, emailRecord) {
		if (Ext.isArray(attachmentNames)) {
			for (var i=0, l=attachmentNames.length; i<l; ++i) {
				var fileName = attachmentNames[i];
				emailWindow.addAttachmentPanel(fileName, emailRecord);
			}
		}
	}

	function updateRecord(form, record, attachments) {
		var formValues = form.getValues();
		for (var key in formValues) {
			record.set(key, formValues[key]);
		}
		record.set("Description", formValues[fields.TO_ADDRESS]);
		record.set("attachments", attachments);
		record.commit();
	}

	function _createEmailFromTemplate(me) {
		if (me.busy) {
			return;
		}

		me.busy = true;
		me.view.removeTemplatesFromStore();
		me.emailsWereGenerated = true;

		me.templateResolver.resolveTemplates({
			attributes: Ext.Object.getKeys(me.emailTemplatesData),
			callback: function onTemlatesWereSolved(values) {
				for (var i=1; i<=me.countTemplates(); ++i) {
					var v = {};
					var conditionExpr = values[me.TEMPLATE_CONDITION+i];
					if (!conditionExpr || me.templateResolver.safeJSEval(conditionExpr)) {
						for (var j=0, l=me.TEMPLATE_FIELDS.length, field=null; j<l; ++j) {
							field = me.TEMPLATE_FIELDS[j];
							v[field] = values[field+i];
						}

						me.view.addTemplateToStore(v);
					}
				}

				me.templateResolver.bindLocalDepsChange(function() {
					if (me.emailsWereGenerated) {
						me.emailsWereGenerated = false;
						new CMDBuild.Msg.warn(null, CMDBuild.Translation.management.modworkflow.extattrs.manageemail.mailsAreChanged);
					}
				});

				me.busy = false;
			}
		});
	}

	/*
	 * Extract the variables of each EmailTemplate,
	 * add a suffix to them with the index,
	 * and put them all in the templates map.
	 * 
	 * This is needed to be passed as a unique map to the
	 * template resolver.
	 */
	function _extractVariablesForTemplateResolver(me) {
		var emailTemplates = me.reader.emailTemplates(me.widgetConf) || [];
		var variables = {};

		for (var i=0, l=emailTemplates.length, t=null; i<l; ++i) {
			t = emailTemplates[i];
			for (var key in t) {
				variables[key + (i+1)] = t[key];
			}
		}

		return variables;
	}
})();