(function() {
	var url = {
		addAttachmentFromNewEmail: "services/json/management/email/uploadattachmentfromnewemail",
		addAttachmentFromExistingEmail: "services/json/management/email/uploadattachmentfromexistingemail",
		copyAttachmentFromCardForNewEmail: "services/json/management/email/copyattachmentsfromcardfornewemail",
		copyAttachmentFromCardForExistingEmail: "services/json/management/email/copyattachmentsfromcardforexistingemail",
		removeAttachmentFromNewEmail: "services/json/management/email/deleteattachmentfromnewemail",
		removeAttachmentFromExistingEmail: "services/json/management/email/deleteattachmentfromexistingemail"
	};

	CMDBuild.ServiceProxy.email = {
		/**
		 * @param {Ext.form.Basic} form
		 * @param {Object} conf.params
		 * @param {String} conf.params.uuid
		 * @param {Function} conf.success
		 */
		addAttachmentFromNewEmail: function(form, conf) {
			conf.url = url.addAttachmentFromNewEmail;
			conf.waitMsg = CMDBuild.Translation.uploading_attachment;

			form.submit(conf);
		},

		/**
		 * @param {Ext.form.Basic} form
		 * @param {Object} conf.params
		 * @param {Number} conf.params.emailId
		 * @param {Function} conf.success
		 */
		addAttachmentFromExistingEmail: function(form, conf) {
			conf.url = url.addAttachmentFromExistingEmail;
			conf.waitMsg = CMDBuild.Translation.uploading_attachment;

			form.submit(conf);
		},

		/**
		 * @param {Object} conf.params
		 * @param {String} conf.params.uuid
		 * @param {String} conf.params.attachments
		 * the encoding of an array like that
		 * [{className: "...", cardId: "...", fileName: "..."}, {...}]
		 * 
		 * @param {Function} conf.params.success
		 */
		copyAttachmentFromCardForNewEmail: function(conf) {
			conf.url = url.copyAttachmentFromCardForNewEmail;
			conf.method = "POST";

			CMDBuild.Ajax.request(conf);
		},

		/**
		 * @param {Object} conf.params
		 * @param {Number} conf.params.id
		 * @param {String} conf.params.attachments
		 * the encoding of an array like that
		 * [{className: "...", cardId: "...", fileName: "..."}, {...}]
		 * 
		 * @param {Function} conf.params.success
		 */
		copyAttachmentFromCardForExistingEmail: function(conf) {
			conf.url = url.copyAttachmentFromCardForExistingEmail;
			conf.method = "POST";

			CMDBuild.Ajax.request(conf);
		},

		/**
		 * @param {Object} conf.params
		 * @param {String} conf.params.uuid
		 * @param {String} conf.params.fileName
		 * @param {Function} conf.success
		 */
		removeAttachmentFromNewEmail: function(conf) {
			conf.url = url.removeAttachmentFromNewEmail;
			conf.method = "POST";

			CMDBuild.Ajax.request(conf);
		},

		/**
		 * @param {Object} conf.params
		 * @param {Number} conf.params.emailId
		 * @param {String} conf.params.fileName
		 * @param {Function} conf.success
		 */
		removeAttachmentFromExistingEmail: function(conf) {
			conf.url = url.removeAttachmentFromExistingEmail;
			conf.method = "POST";

			CMDBuild.Ajax.request(conf);
		}

	};
})();