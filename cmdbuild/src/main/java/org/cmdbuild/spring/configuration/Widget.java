package org.cmdbuild.spring.configuration;

import static org.cmdbuild.spring.util.Constants.PROTOTYPE;

import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.logic.widget.WidgetLogic;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@ConfigurationComponent
public class Widget {

	@Autowired
	private DBDataView systemDataView;

	@Bean
	@Scope(PROTOTYPE)
	public WidgetLogic widgetLogic() {
		return new WidgetLogic(systemDataView);
	}

}
