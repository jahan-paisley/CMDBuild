(function() {
	describe('CMWorkflowState', function() {
		var state = null;

		beforeEach(function() {
			state = new CMDBuild.state.CMWorkflowState();
		});

		afterEach(function() {
			delete state;
		});

		it ('start with no data', function() {
			expect(state.getProcessClassRef()).toBeNull();
			expect(state.getProcessInstance()).toBeNull();
			expect(state.getActivityInstance()).toBeNull();
			expect(state.countDelegates()).toBe(0);
		});

		it ('it could add a delegate', function() {
			var a = {};
			try {
				state.addDelegate(a);
			} catch (e) {}

			expect(state.countDelegates()).toBe(0);

			a = new CMDBuild.state.CMWorkflowStateDelegate();
			state.addDelegate(a);
			expect(state.countDelegates()).toBe(1);
			state.addDelegate(a);
			expect(state.countDelegates()).toBe(2);
		});

		it ('is able to call a method for all delegates', function() {
			var a = new CMDBuild.state.CMWorkflowStateDelegate();
			var b = new CMDBuild.state.CMWorkflowStateDelegate();
			var first = "First parameter", second = "Second parameter";

			var spy = jasmine.createSpy("foo");

			a.foo = spy;
			b.foo = spy;

			state.addDelegate(a);
			state.addDelegate(b);
			state.notifyToDelegates("foo", [first, second]);

			expect(a.foo).toHaveBeenCalledWith(first, second);
			expect(b.foo.callCount).toBe(2);
		});

		it ('set the processClassRef', function() {
			var a = new CMDBuild.state.CMWorkflowStateDelegate();
			var danglingCard = null;
			var processClassRef = {
				name: "I'm the ref",
				getId: function() { return 34;},
				isSuperClass: function() {return false} // TODO: test if is superclass
			};

			spyOn(a, 'onProcessClassRefChange');

			state.addDelegate(a);
			state.setProcessClassRef(processClassRef, danglingCard);

			expect(state.getProcessClassRef()).toBe(processClassRef);
			expect(a.onProcessClassRefChange).toHaveBeenCalledWith(processClassRef,danglingCard);

			a.onProcessClassRefChange.reset();

			state.setProcessClassRef(processClassRef);
			expect(a.onProcessClassRefChange).not.toHaveBeenCalled();
		});

		it ('set the processInstance', function() {
			var a = new CMDBuild.state.CMWorkflowStateDelegate();
			var processInstanece = {
				id: "asdf",
				isNew: function() {return false;}
			};

			spyOn(a, 'onProcessInstanceChange');

			state.addDelegate(a);
			state.setProcessInstance(processInstanece);

			expect(state.getProcessInstance()).toBe(processInstanece);
			expect(a.onProcessInstanceChange).toHaveBeenCalledWith(processInstanece);

			// TODO: maybe is useful to not notify to the delegates
			// if the process instance was already the current

			// TODO: test if is a new processInstance
		});

		it ('set the activityInstance', function() {
			var a = new CMDBuild.state.CMWorkflowStateDelegate();
			var activityInstane = {
				id: "asdf"
			};

			spyOn(a, 'onActivityInstanceChange');

			state.addDelegate(a);
			state.setActivityInstance(activityInstane);

			expect(state.getActivityInstance()).toBe(activityInstane);
			expect(a.onActivityInstanceChange).toHaveBeenCalledWith(activityInstane);

			// TODO: maybe is useful to not notify to the delegates
			// if the process instance was already the current
		});
	});
})();