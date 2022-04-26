package com.gnc.task.application.ui.views.logged;

import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.gnc.task.application.ui.views.MainLayout;
import javax.annotation.security.PermitAll;

@PageTitle("Iniciar sesión")
@Route(value = "logged", layout = MainLayout.class)
@PermitAll
public class LoggedView extends VerticalLayout {

	public LoggedView() {
		setSpacing(false);

		Image img = new Image("images/logo.png", "placeholder plant");
		img.setWidth("200px");
		add(img);
		setSizeFull();
		setJustifyContentMode(JustifyContentMode.CENTER);
		setDefaultHorizontalComponentAlignment(Alignment.CENTER);
		getStyle().set("text-align", "center");
	}

}
