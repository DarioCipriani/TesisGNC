package com.gnc.task.application.views.helloworld;

import com.gnc.task.application.views.MainLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import javax.annotation.security.RolesAllowed;

@PageTitle("Hello World")
@Route(value = "hello", layout = MainLayout.class)
@RolesAllowed("admin")
public class HelloWorldView extends VerticalLayout {
	public HelloWorldView() {
		setSpacing(false);

		Image img = new Image("images/logo.png", "placeholder plant");
		img.setWidth("200px");
		add(img);

		add(new H1("Bienvenidos a GNC PROGRESO!"));
		add(new H2("Empresa familiar dedicada a la venta e instalación de equipos de GNC"));

		setSizeFull();
		setJustifyContentMode(JustifyContentMode.CENTER);
		setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
		getStyle().set("text-align", "center");
	}
}
