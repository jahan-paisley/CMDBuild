(function() {
	describe('CMDBuild.LoginPanel', function() {

		var loginPanel;
		var server;

		beforeEach(function() {
			addNeededDomStuff();

			server = CMDBuild.test.CMServer.create();

			CMDBuild.Config.cmdbuild.languageprompt = true;

			loginPanel = new CMDBuild.LoginPanel();

			this.addMatchers({
				toBeEnabled : function(expected) {
					return !this.actual.disabled;
				}
			});

		});

		afterEach(function () {
			server.restore();

			delete server;
			delete loginPanel;
		});

		it('Is well shown', function() {
			expect(loginPanel.user).toBeEnabled();
			expect(loginPanel.password).toBeEnabled();
			expect(loginPanel.role).not.toBeEnabled();
		});

		it('Enable the role combo the user has multiple groups', function() {
			var group1 = "b";
			var group2 = "a";

			server.bindUrl("services/json/login/login", function(params) {
				return {
					success:false,
					reason: "AUTH_MULTIPLE_GROUPS",
					groups:[
						{"name":123,"value":group1},
						{"name":456,"value":group2}
					]
				};
			});

			// fill the form to be valid
			loginPanel.user.setValue("Foo");
			loginPanel.password.setValue("Bar");

			expect(loginPanel.role).not.toBeEnabled();
			loginPanel.doLogin();

			// expectations after server response
			expect(loginPanel.role).toBeEnabled();
			var loadedGroups = loginPanel.role.store.data.items;
			expect(loadedGroups.length).toBe(2);
			expect(loadedGroups[0].data.value).toEqual(group1);
			expect(loadedGroups[1].data.value).toEqual(group2);
		});

	});

	function addNeededDomStuff() {
		var loginDiv = document.createElement("div");
		loginDiv.id = "login_box";

		var releaseDiv = document.createElement("div");
		releaseDiv.id = "release_box";

		document.body.appendChild(loginDiv);
		document.body.appendChild(releaseDiv);
	}

})();