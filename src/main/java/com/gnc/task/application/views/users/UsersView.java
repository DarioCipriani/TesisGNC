package com.gnc.task.application.views.users;

import com.gnc.task.application.data.Role;
import com.gnc.task.application.data.entity.User;
import com.gnc.task.application.security.AuthenticatedUser;
import com.gnc.task.application.security.UserDetailsServiceImpl;
import com.gnc.task.application.utilities.ViewUtils;
import com.gnc.task.application.views.MainLayout;
import com.vaadin.componentfactory.MultipleSelect;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.security.RolesAllowed;
import java.util.*;

@PageTitle("Users")
@Route(value = "users", layout = MainLayout.class)
@RolesAllowed("admin")
public class UsersView extends Div {

	private ListDataProvider<User> dataProvider;

	private Grid<User> users;

	private PasswordField password;
	private EmailField email;
	private MultipleSelect<Role> userRoleSelect;

	private Binder<User> userBinder;

	private final Icon refreshIcon = new Icon(VaadinIcon.REFRESH);
	private final Icon addIcon = new Icon(VaadinIcon.PLUS);
	private final Icon editIcon = new Icon(VaadinIcon.PENCIL);
	private final Icon deleteIcon = new Icon(VaadinIcon.TRASH);

	private final Button refreshButton = new Button(refreshIcon);
	private final Button addButton = new Button(addIcon);
	private final Button editButton = new Button(editIcon);
	private final Button deleteButton = new Button(deleteIcon);

	private final Dialog popUp = new Dialog();
	private FormLayout editorDiv;
	private HorizontalLayout buttonDiv;

	private final Icon check = new Icon(VaadinIcon.CHECK);
	private final Icon close = new Icon(VaadinIcon.CLOSE);

	private final Button saveButton = new Button("Save", check);
	private final Button cancelButton = new Button("Cancel", close);

	private static final Logger LOGGER = LoggerFactory.getLogger(UsersView.class);

	private AuthenticatedUser authenticatedUser;
	private UserDetailsServiceImpl usersDetailsServiceImpl;
	private PasswordEncoder passwordEncoder;

	private Paragraph getTitleValue(String title, String value) {
		Span spanTitle = new Span(title);
		spanTitle.getStyle().set("font-weight", "bold");
		Span spanValue = new Span(": " + value);
		return new Paragraph(spanTitle, spanValue);
	}

	public UsersView(AuthenticatedUser authenticatedUser, UserDetailsServiceImpl userDetailsServiceImpl,
			PasswordEncoder passwordEncoder) {
		this.authenticatedUser = authenticatedUser;
		this.usersDetailsServiceImpl = userDetailsServiceImpl;
		this.passwordEncoder = passwordEncoder;
		Optional<User> userLogged = authenticatedUser.get();
		if (userLogged.isPresent() && Objects.nonNull(userLogged.get())
				&& userLogged.get().getRoles().contains(Role.ADMIN)) {
			setSizeFull();
			setId("adminusers-view");
			userRoleSelect = new MultipleSelect<>();
			// Configure Grid
			users = new Grid<>();
			users.addThemeVariants(GridVariant.LUMO_NO_BORDER);
			users.setSizeFull();

			users.addColumn(User::getId).setWidth("2em").setHeader("IDs").setResizable(true).setKey("id");

			users.addColumn(User::getUsername).setHeader("Email").setResizable(true).setKey("email").setSortable(true);
			users.addColumn(u -> u.getRoles().toString()).setHeader("Roles").setResizable(true).setKey("roles")
					.setSortable(true);
			users.addThemeName("grid-selection-theme");
			users.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

			// when a row is selected populate form
			users.asSingleSelect().addValueChangeListener(event -> {
				if (event.getValue() != null) {
					saveButton.setText("Save");
					password.setHelperText("Insert data here only if you want to change the user's password.");
				} else {
					refreshGrid(userDetailsServiceImpl);
					saveButton.setText("New User");
					password.setHelperText("Enter a password for the user.");
				}
			});

			// Configure Form
			userBinder = new Binder<>(User.class);
			// Bind fields. This where you'd define e.g. validation rules
			userBinder.bind(userRoleSelect, User::getRoles, User::setRoles);
			userBinder.bindInstanceFields(this);

			// the grid valueChangeEvent will clear the form too
			deleteButton.addClickListener(e -> {
				User user = users.asSingleSelect().getValue();
				ConfirmDialog dialog = new ConfirmDialog("Confirm delete",
						"Are you sure you want to delete the user? Username: " + user.getUsername(), "Delete",
						confirmEvent -> {
							if (user != null && usersDetailsServiceImpl.deleteByUsername(user.getUsername())) {
								ViewUtils.notification("Success delete",
										"<b>Username:</b> " + user.getUsername() + "</br>",
										NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE).open();
								// if current user deletes himself
								if (user.getUsername().equals(userLogged.get().getUsername())) {
									authenticatedUser.logout();
								}
								refreshGrid(usersDetailsServiceImpl);
								saveButton.setText("New User");
							} else { // is unable to delete this user
								ViewUtils.notification("Unable delete", "<b>Username:</b> "
										+ ((user != null) ? user.getUsername() : "<i>not selected</i>") + "</br>",
										NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE).open();
							}
						}, "Cancel", cancelEvent -> {
						});
				dialog.setConfirmButtonTheme(NotificationVariant.LUMO_ERROR.getVariantName());
				dialog.open();
			});

			cancelButton.addClickListener(e -> popUp.close());

			saveButton.addClickListener(e -> {
				User user = users.asSingleSelect().getValue();
				if (user != null) {
					if ("Save".equals(saveButton.getText())) {
						user = usersDetailsServiceImpl.getUserById(user.getId()).orElse(null);
						if (user != null) {
							if (userBinder.hasChanges() || user.getId() == null || (user.getId() != null)) {
								// body
								user.setUsername(this.email.getValue());
								user.setRoles(this.userRoleSelect.getValue());
								if (!this.password.getValue().isEmpty()) {
									user.setHashedPassword(passwordEncoder.encode(this.password.getValue()));
								}
								user.setUpdatedAt(new Date());

								usersDetailsServiceImpl.updateUser(user);
								popUp.close();
								ViewUtils.notification("Updated user",
										"<b>Username:</b> " + user.getUsername() + "</br>",
										NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE).open();

								// if current user is updating his e-mail (username) or roles
								// (admin/user)

								if (Objects.nonNull(users.asSingleSelect().getValue())
										&& users.asSingleSelect().getValue().getUsername()
												.equals(userLogged.get().getUsername())
										&& (!user.getUsername().equals(userLogged.get().getUsername()) || // email
																											// change
								!user.getRoles().equals(users.asSingleSelect().getValue().getRoles()))) {
									authenticatedUser.logout();
								}
								// else, e.g. current user is updating another user
								else {
									refreshGrid(usersDetailsServiceImpl);
								}
							} else { // no changes (ex. same email, password etc.)
								popUp.close();
								ViewUtils.notification("There are no changes",
										"<b>Username:</b> " + user.getUsername() + "</br>",
										NotificationVariant.LUMO_PRIMARY, Notification.Position.MIDDLE).open();
							}
						} else {
							LOGGER.error("the user no longer exists or there was a problem trying to get it");
							ViewUtils.notification("Something went wrong",
									"The user could not be obtained, try to refresh and do the process again, if something goes wrong consult with the administrator. </br>",
									NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE).open();
						}
					}
				} else {
					if ("New User".equals(saveButton.getText())) {
						user = new User();
						user.setUsername(this.email.getValue());
						user.setRoles(this.userRoleSelect.getValue());
						user.setHashedPassword(passwordEncoder.encode(this.password.getValue()));
						user.setUpdatedAt(new Date());
						user.setCreatedAt(new Date());

						usersDetailsServiceImpl.updateUser(user);
						popUp.close();
						refreshGrid(usersDetailsServiceImpl);
						ViewUtils.notification("Created user", "<b>Username:</b> " + user.getUsername() + "</br>",
								NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE).open();
					}
				}

			});

			refreshButton.addClickListener(event -> refreshGrid(usersDetailsServiceImpl));

			addButton.addClickListener(event -> {
				users.asSingleSelect().clear();
				clearForm();
				saveButton.setText("New User");
				popUpOpened();
			});

			editButton.addClickListener(event -> {
				User user = users.asSingleSelect().getValue();
				if (user != null) {
					populateForm(user);
					saveButton.setText("Save");
					popUpOpened();
				} else {
					ViewUtils.notification("Select user",
							"You must select a user and then click the edit button if you want to edit the user."
									+ "</br>",
							NotificationVariant.LUMO_PRIMARY, Notification.Position.MIDDLE).open();
				}
			});

			VerticalLayout layout = new VerticalLayout();
			layout.setSizeFull();

			createButtonLayout(layout);
			createGridLayout(layout);
			createEditorLayout();

			add(layout);
			dataProvider = new ListDataProvider<>(usersDetailsServiceImpl.findAllUsers());
			users.setDataProvider(dataProvider);
			addFiltersToGrid();
			ViewUtils.gridReziseColumns(users);
		}
	}

	private void createGridLayout(VerticalLayout layout) {
		Div wrapper = new Div();
		wrapper.setId("wrapper");
		wrapper.setSizeFull();
		layout.add(wrapper);
		wrapper.add(users);
	}

	private void createEditorLayout() {
		popUp.setResizable(true);
		editorDiv = new FormLayout();
		editorDiv.setId("editor");
		editorDiv.setSizeFull();
		editorDiv.setResponsiveSteps(
				new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
				new FormLayout.ResponsiveStep("600px", 1, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));

		password = ViewUtils.newBasicConfigPasswordField("Password");
		password.setHelperText("Enter a password for the user.");
		password.setRevealButtonVisible(true);
		email = ViewUtils.newBasicConfigEmailField("Email");
		// Roles
		userRoleSelect.setItems(EnumSet.allOf(Role.class));
		userRoleSelect.setValue(EnumSet.of(Role.USER));
		userRoleSelect.select(Role.USER);
		userRoleSelect.setItemLabelGenerator(Enum::toString);
		userRoleSelect.setWidthFull();
		userRoleSelect.setLabel("Role");
		userRoleSelect.getElement().getClassList().add("full-width");
		userRoleSelect.setEmptySelectionAllowed(false);

		editorDiv.add(email, password, userRoleSelect);
		buttonDiv = new HorizontalLayout();
		buttonDiv.setId("button-layout");
		buttonDiv.setSizeFull();
		buttonDiv.add(cancelButton, saveButton);
		buttonDiv.setAlignItems(FlexComponent.Alignment.CENTER);
		buttonDiv.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
		editorDiv.add(buttonDiv);
		popUp.add(editorDiv);

	}

	private void populateForm(User user) {
		userBinder.readBean(user);
		email.setValue(user.getUsername());
		password.setValue("");
	}

	private void clearForm() {
		// Value can be null as well, that clears the form
		email.clear();
		password.clear();
		userBinder.readBean(null);
		userRoleSelect.setValue(EnumSet.of(Role.USER));
	}

	private void refreshGrid(UserDetailsServiceImpl userDetailsServiceImpl) {
		dataProvider = new ListDataProvider<>(userDetailsServiceImpl.findAllUsers());
		users.setDataProvider(dataProvider);
		users.getDataProvider().refreshAll();
		users.select(null);
	}

	private void popUpOpened() {
		popUp.setCloseOnEsc(true);
		popUp.setCloseOnOutsideClick(false);
		popUp.open();
	}

	private void createButtonLayout(VerticalLayout layout) {
		close.setColor("#BE2123");
		ViewUtils.setButtonAutoMarginAndVariant(saveButton, ButtonVariant.LUMO_TERTIARY);
		ViewUtils.setButtonAutoMarginAndVariant(cancelButton, ButtonVariant.LUMO_ERROR);
		refreshButton.getElement().setProperty("title", "Refresh users");
		addButton.getElement().setProperty("title", "Add new user");
		editButton.getElement().setProperty("title", "Edit user");
		deleteButton.getElement().setProperty("title", "Delete user");
		ViewUtils.buttonConfig(layout, addButton, deleteButton, refreshButton, editButton);
	}

	private void addFiltersToGrid() {
		HeaderRow filterRow = users.appendHeaderRow();

		TextField usernameFilter = ViewUtils.createNewFilterForColumnGrid();
		usernameFilter.addValueChangeListener(event -> dataProvider
				.addFilter(client -> StringUtils.containsIgnoreCase(client.getUsername(), usernameFilter.getValue())));
		ViewUtils.setFilterInColumnGrid(filterRow, usernameFilter, users.getColumnByKey("email"));
	}

}
