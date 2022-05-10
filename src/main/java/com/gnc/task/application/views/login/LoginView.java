package com.gnc.task.application.views.login;

import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;

import javax.annotation.security.PermitAll;

@PageTitle("Iniciar sesión")
@Route(value = "login")
@PermitAll
public class LoginView extends LoginOverlay {
	public LoginView() {
		setAction("login");

		LoginI18n i18n = LoginI18n.createDefault();
		i18n.setHeader(new LoginI18n.Header());
		i18n.getHeader().setTitle("GNC PROGRESO");
		i18n.getHeader().setDescription("Iniciar sesión admin@gncsolutions.net/GNCAdmin2021");
		i18n.setAdditionalInformation(null);
		setI18n(i18n);

		setForgotPasswordButtonVisible(false);
		setOpened(true);
	}

}
