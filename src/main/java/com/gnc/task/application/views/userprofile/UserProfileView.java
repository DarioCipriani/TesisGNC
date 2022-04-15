package com.gnc.task.application.views.userprofile;

import com.gnc.task.application.security.AuthenticatedUser;
import com.gnc.task.application.data.entity.User;
import com.gnc.task.application.utilities.ViewUtils;
import com.gnc.task.application.views.MainLayout;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import java.util.Optional;

@PageTitle("Profile")
@Route(value = "userprofile", layout = MainLayout.class)
@RolesAllowed("admin")
public class UserProfileView extends HorizontalLayout {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserProfileView.class);

	private final Div divProfile = new Div();

	private AuthenticatedUser authenticatedUser;

	public UserProfileView(AuthenticatedUser authenticatedUser) {
		this.authenticatedUser = authenticatedUser;
		Optional<User> maybeUser = authenticatedUser.get();
		if (maybeUser.isPresent()) {
			User principal = maybeUser.get();
			setAlignItems(Alignment.CENTER);
			setVerticalComponentAlignment(Alignment.START);
			if (principal != null) {
				Avatar avatar = new Avatar(principal.getUsername(), principal.getProfilePictureUrl());
				avatar.getStyle().set("background", "#cccccc");
				avatar.setWidth("15vw");
				avatar.setHeight("15vw");
				avatar.setMinHeight("100px");
				avatar.setMinWidth("100px");
				avatar.setMaxWidth("150px");
				avatar.setMaxHeight("150px");
				avatar.setColorIndex(1);
				divProfile.add(avatar);

				Button changeUrl = new Button("Set picture");
				ViewUtils.setButtonAutoMarginAndVariant(changeUrl, ButtonVariant.LUMO_PRIMARY);
				// UrlValidator urlValidator = new UrlValidator();
				changeUrl.addClickListener(buttonClickEvent -> {
					Dialog popUp = new Dialog();
					FormLayout form = new FormLayout();
					form.setResponsiveSteps(
							new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
							new FormLayout.ResponsiveStep("600px", 1, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
					form.setSizeFull();
					TextField url = ViewUtils.newBasicConfigTextField("Url");
					url.setSizeFull();
					// url.addKeyPressListener(e ->
					// url.setInvalid(!urlValidator.isValid(url.getValue())));
					// url.addValueChangeListener(e ->
					// url.setInvalid(!urlValidator.isValid(url.getValue())));
					Div buttons = new Div();
					Button okButton = new Button("OK", VaadinIcon.ENVELOPE_OPEN.create());
					okButton.setHeightFull();
					okButton.setMaxWidth("40%");
					okButton.getStyle().set("margin-right", "auto");
					okButton.getStyle().set("margin-left", "20%");
					okButton.setWidth("-webkit-fill-available");
					okButton.addClickListener(e -> {
						// if (urlValidator.isValid(url.getValue())) {
						try {
							principal.setProfilePictureUrl(url.getValue());
							avatar.setImage(url.getValue());
							authenticatedUser.saveUser(principal);
							Notification notification = ViewUtils.notification("Profile", "User picture updated.",
									NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE);
							notification.addDetachListener(event -> {
								UI.getCurrent().getPage().reload();
							});
							notification.open();
						} catch (Exception ex) {
							LOGGER.error(ex.getMessage());
							ViewUtils.notification("Profile",
									"User picture could not be updated. Something went wrong, contact the administrator.",
									NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE).open();
						}
						/*
						 * } else { ViewUtils.notification("Profile",
						 * "User picture could not be updated. Invalid URL.",
						 * NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE).open(); }
						 */
						popUp.close();
					});
					okButton.addClickShortcut(Key.ENTER);
					okButton.setAutofocus(true);
					Button cancelButton = new Button("Cancel", VaadinIcon.CLOSE.create());
					cancelButton.setHeightFull();
					cancelButton.setMaxWidth("40%");
					cancelButton.setWidth("-webkit-fill-available");
					cancelButton.addClickListener(e -> {
						popUp.close();
					});
					cancelButton.addClickShortcut(Key.ESCAPE);
					form.add(url);
					buttons.add(cancelButton, okButton);
					form.add(buttons);
					popUp.add(form);
					popUp.open();
				});
				divProfile.add(changeUrl);

				// setUserProfileImage();
				// initUploaderImage(usersDetailsServiceImpl);

				divProfile.getStyle().set("flex", "0").set("padding", "1em");
				Div divDataProfile = new Div();
				divDataProfile.getStyle().set("padding", "1em");
				Paragraph usernameData = ViewUtils.getTitleValue("Username", principal.getUsername());
				Paragraph roleData = ViewUtils.getTitleValue("Role", principal.getRoles().toString());
				usernameData.setWidthFull();
				roleData.setWidthFull();
				divDataProfile.add(usernameData, roleData);
				add(divProfile, divDataProfile);
			}
		}
	}
}
