(function() {
	Ext.define("CMDBuild.setup.CMSetupController", {
		constructor: function(view) {
			this.view = view;
			this.s1 = this.view.step1;
			this.s2 = this.view.step2;
			this.s3 = this.view.step3;

			this.currentStep = this.s1;

			this.s1.on("show", onS1Show, this);
			this.s2.on("show", onS2Show, this);
			this.s3.on("show", onS3Show, this);

			this.view.nextButton.on("click", onNextClick, this);
			this.view.prevButton.on("click", onPrevClick, this);
			this.view.finishButton.on("click", onFinishClick, this);

			this.s2.connectionButton.on("click", onConnectionButtonClick, this);
			this.s2.dbType.on('select', onDbTypeSelect, this);

			setLinkMap.call(this);
		},

		takeData: function() {
			var data = {};
			this.view.cardPanel.cascade(function(item) {
				if (item 
					&& (item instanceof Ext.form.Field)
					&& !item.disabled
					) {
					data[item.name] = item.getValue();
				}
			});
			
			return data;
		},
		
		getNonValidFields: function() {
			var data = []
			
			this.view.cardPanel.cascade(function(item) {
				if (item 
					&& (item instanceof Ext.form.Field)
					&& !item.disabled
					) {
					
					if (!item.isValid()) { 
						data.push(item);
					}
				}
			});
			
			return data;
		}
	});

	function onS1Show() {
		this.view.showNextButton(true);
		this.view.prevButton.disable();
	}
	
	function onS2Show() {
		var value = this.s2.dbType.getValue();
		this.view.prevButton.enable();
		showNextButtonByDbTypeValue.call(this, value);
	}
	
	function onS3Show() {
		this.view.showNextButton(false);
	}
	
	function onNextClick() {
		var next = this.linkMap[this.currentStep.id].next;
		if (next != null) {
			this.view.bringToFront(next);
			this.currentStep = next;
		}
	}
	
	function onPrevClick() {
		var prev = this.linkMap[this.currentStep.id].prev;
		if (prev != null) {
			this.view.bringToFront(prev);
			this.currentStep = prev;
		}
	}
	
	function onFinishClick() {
		var nonValidFields = this.getNonValidFields();
		if (nonValidFields.length > 0) {
			CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);
			return;
		} else {
			CMDBuild.LoadMask.get().show();
			CMDBuild.ServiceProxy.setup.applySetup({
				params: this.takeData(),
				scope: this,
				success: function() {
					Ext.Msg.show({
						title: CMDBuild.Translation.configure.success.title,
						msg: CMDBuild.Translation.configure.success.text,
						buttons: Ext.MessageBox.OK,
						fn: function() { window.location = 'administration.jsp'; }
					});
				},
				callback: function() {			
					CMDBuild.LoadMask.get().hide();
				}
			});
		}
	}
	
	function onConnectionButtonClick() {
		CMDBuild.LoadMask.get().show();
		CMDBuild.ServiceProxy.setup.testDBConnection({
			params: {
				host: this.s2.host.getValue(),
				port: this.s2.port.getValue(),
				user: this.s2.user.getValue(),
				password: this.s2.password.getValue()
			},
			success:function(){
				Ext.Msg.show({
					title: CMDBuild.Translation.configure.step2.msg.title, 
					msg: CMDBuild.Translation.configure.step2.msg.msg, 
					buttons: Ext.MessageBox.OK 
				});
			},
			callback:function(form, action){
				CMDBuild.LoadMask.get().hide()
			}		
		});
	}

	function onDbTypeSelect(combo, record, index) {
		var name = record[0].get("name");
		showNextButtonByDbTypeValue.call(this, name);
		this.s2.onDbTypeSelect(name);
	}

	function showNextButtonByDbTypeValue(value) {
		this.view.showNextButton(value == "empty");
	}

	function setLinkMap() {
		this.linkMap = {}
		this.linkMap[this.s1.id] = {
			next: this.s2,
			prev: null
		}
		
		this.linkMap[this.s2.id] = {
			next: this.s3,
			prev: this.s1
		}
		
		this.linkMap[this.s3.id] = {
			next: null,
			prev: this.s2
		}
	}

})();