(function() {

	Ext.define("TestDelegableInterface", {
		methodA: Ext.emptyFn,
		methodB: Ext.emptyFn
	});

	var delegable = null;

	describe("CMDelegable", function() {

		beforeEach(function() {
			delegable = new CMDBuild.core.CMDelegable("TestDelegableInterface");
		});

		afterEach(function() {
			delete delegable;
		});

		it("Fail without interface name", function() {
			expect(function() {
				new CMDBuild.core.CMDelegable();
			}).toThrow(CMDBuild.core.CMDelegable.errors.noInterfaceOnInit);
		});

		it("Start without delegates", function() {
			expect(delegable.countDelegates()).toBe(0);
		});

		it("Check the delegate interface", function() {
			var delegate = {};
			var expectedError = CMDBuild.core.CMDelegable.errors.wrongTypeOnAdd("CMDBuild.core.CMDelegable", "TestDelegableInterface");

			expect(function() {
				delegable.addDelegate(delegate);
			}).toThrow(expectedError);
		});

		it("Add an instance of delegate", function() {
			var delegate = new TestDelegableInterface();
			delegable.addDelegate(delegate);
			expect(delegable.countDelegates()).toBe(1);
		});

		it("Add an subclass of delegate", function() {
			Ext.define("SubTestDelegableInterface", {
				extend: "TestDelegableInterface"
			});

			var delegate = new SubTestDelegableInterface();

			delegable.addDelegate(delegate);
			expect(delegable.countDelegates()).toBe(1);
		});

		it("Add an mixed delegate", function() {
			Ext.define("MixedTestDelegableInterface", {
				mixins: {
					testDelegate: "TestDelegableInterface"
				}
			});

			var delegate = new MixedTestDelegableInterface();

			delegable.addDelegate(delegate);
			expect(delegable.countDelegates()).toBe(1);
		});

		it("Add more than one delegates", function() {
			var delegate1 = new TestDelegableInterface();
			var delegate2 = new TestDelegableInterface();
			var delegate3 = new TestDelegableInterface();

			delegable.addDelegate(delegate1);
			delegable.addDelegate(delegate2);
			delegable.addDelegate(delegate3);

			expect(delegable.countDelegates()).toBe(3);
		});

		it("Call a method for each delegates with right args", function() {
			var delegate = new TestDelegableInterface();
			var s = spyOn(delegate, "methodA");

			delegable.addDelegate(delegate);

			delegable.callDelegates("methodA", [1, 2, 3]);
			expect(s).toHaveBeenCalledWith(1,2,3);
			expect(s.callCount).toBe(1);

			s.reset();
			delegable.callDelegates("methodA", "foo");
			expect(s).toHaveBeenCalledWith("foo");
			expect(s.callCount).toBe(1);
		});

		it("Check method before call it", function() {
			var delegate = new TestDelegableInterface();

			delegable.addDelegate(delegate);
			expect(function() {
				delegable.callDelegates("unknownMethod");
				delegable.callDelegates();
				delegable.callDelegates(null);
			}).not.toThrow();

		});

		it("Remove a delegate", function() {
			var delegate = new TestDelegableInterface();
			var s = spyOn(delegate, "methodA");
			var delegate2 = new TestDelegableInterface();

			delegable.addDelegate(delegate);
			delegable.addDelegate(delegate2);
			expect(delegable.countDelegates()).toBe(2);

			delegable.removeDelegate(delegate);
			expect(delegable.countDelegates()).toBe(1);

			delegable.callDelegates("methodA", [1, 2, 3]);
			expect(s.callCount).toBe(0);
		});
	});


})();