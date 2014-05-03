package org.cmdbuild.dao.view.user;

import java.util.List;

import org.cmdbuild.dao.entrytype.CMFunctionCall;
import org.cmdbuild.dao.function.CMFunction;

public class UserFunctionCall extends UserEntryType implements CMFunctionCall {

	static UserFunctionCall newInstance(final UserDataView view, final CMFunctionCall inner) {
		return new UserFunctionCall(view, inner);
	}

	private final CMFunctionCall inner;

	private UserFunctionCall(final UserDataView view, final CMFunctionCall inner) {
		super(inner, view);
		this.inner = inner;
	}

	@Override
	public CMFunction getFunction() {
		return inner.getFunction();
	}

	@Override
	public List<Object> getParams() {
		return inner.getParams();
	}

}
