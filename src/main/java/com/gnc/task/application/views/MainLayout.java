package com.gnc.task.application.views;

import com.gnc.task.application.data.entity.User;
import com.gnc.task.application.data.entity.UserConfig;
import com.gnc.task.application.data.service.UserConfigRepository;
import com.gnc.task.application.security.AuthenticatedUser;
import com.gnc.task.application.views.clientsconfig.ClientsConfigView;
import com.gnc.task.application.views.helloworld.HelloWorldView;
import com.gnc.task.application.views.public_.PublicView;
import com.gnc.task.application.views.userprofile.UserProfileView;
import com.gnc.task.application.views.users.UsersView;
import com.vaadin.componentfactory.ToggleButton;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.Lumo;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The main view is a top-level placeholder for other views.
 */
@PageTitle("Main")
public class MainLayout extends AppLayout {

	public static class MenuItemInfo {

		private String text;
		private String iconClass;
		private Class<? extends Component> view;

		public MenuItemInfo(String text, String iconClass, Class<? extends Component> view) {
			this.text = text;
			this.iconClass = iconClass;
			this.view = view;
		}

		public String getText() {
			return text;
		}

		public String getIconClass() {
			return iconClass;
		}

		public Class<? extends Component> getView() {
			return view;
		}

	}

	private H1 viewTitle;
	ToggleButton toggleDark;

	private AuthenticatedUser authenticatedUser;
	private AccessAnnotationChecker accessChecker;
	private UserConfigRepository userConfigRepository;

	public MainLayout(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker,
			UserConfigRepository userConfigRepository) {
		this.authenticatedUser = authenticatedUser;
		this.accessChecker = accessChecker;
		this.userConfigRepository = userConfigRepository;

		setPrimarySection(Section.DRAWER);
		addToNavbar(false, createHeaderContent());
		addToDrawer(createDrawerContent());
	}

	private Component createHeaderContent() {
		DrawerToggle toggle = new DrawerToggle();
		toggle.addClassName("text-secondary");
		toggle.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
		toggle.getElement().setAttribute("aria-label", "Menu toggle");

		viewTitle = new H1();
		viewTitle.addClassNames("m-0", "text-l");
		viewTitle.setWidthFull();

		toggleDark = new ToggleButton();
		toggleDark.getStyle().set("margin-right", "1em");
		toggleDark.addClickListener(toggleButtonClickEvent -> {
			Optional<User> maybeUser = authenticatedUser.get();
			ThemeList themeList = UI.getCurrent().getElement().getThemeList();
			if (maybeUser.isPresent()) {
				UserConfig userConfig = userConfigRepository.findByUser_Id(maybeUser.get().getId());
				if (Objects.isNull(userConfig)) {
					userConfig = new UserConfig();
					userConfig.setUser(maybeUser.get());
					userConfig.setTheme(Lumo.LIGHT);
				}
				if (themeList.contains(Lumo.DARK)) { // (2)
					userConfig.setTheme(Lumo.LIGHT);
					themeList.remove(Lumo.DARK);
				} else {
					userConfig.setTheme(Lumo.DARK);
					themeList.add(Lumo.DARK);
				}
				userConfigRepository.save(userConfig);
			} else {
				if (themeList.contains(Lumo.DARK)) {
					themeList.remove(Lumo.DARK);
				} else {
					themeList.add(Lumo.DARK);
				}
			}
		});

		HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setSizeFull();
		horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
		horizontalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.START);
		horizontalLayout.add(viewTitle, toggleDark);

		Header header = new Header(toggle, horizontalLayout);
		header.addClassNames("bg-base", "border-b", "border-contrast-10", "box-border", "flex", "h-xl", "items-center",
				"w-full");
		return header;
	}

	private Component createDrawerContent() {
		H2 appName = new H2("GNC UI");
		appName.addClassNames("flex", "items-center", "h-xl", "m-0", "px-m", "text-m");

		com.vaadin.flow.component.html.Section section = new com.vaadin.flow.component.html.Section(appName,
				createNavigation(), createFooter());
		section.addClassNames("flex", "flex-col", "items-stretch", "max-h-full", "min-h-full");
		return section;
	}

	private Nav createNavigation() {
		Nav nav = new Nav();
		nav.addClassNames("border-b", "border-contrast-10", "flex-grow", "overflow-auto");
		nav.getElement().setAttribute("aria-labelledby", "views");

		// Wrap the links in a list; improves accessibility
		UnorderedList list = new UnorderedList();
		list.addClassNames("list-none", "m-0", "p-0");
		nav.add(list);

		for (RouterLink link : createLinks()) {
			ListItem item = new ListItem(link);
			list.add(item);
		}
		return nav;
	}

	private List<RouterLink> createLinks() {

		MenuItemInfo[] menuItems = new MenuItemInfo[]{
				new MenuItemInfo("Principal", "la la-file", PublicView.class),
				new MenuItemInfo("Clientes", "la la-file", ClientsConfigView.class),
				new MenuItemInfo("Vehículos", "la la-file", HelloWorldView.class),
				new MenuItemInfo("Obleas y PH", "la la-file", HelloWorldView.class),
				new MenuItemInfo("Mantenimientos", "la la-file", HelloWorldView.class),
				new MenuItemInfo("Manual de Fallas", "la la-file",  HelloWorldView.class),
				new MenuItemInfo("Productos", "la la-file",  HelloWorldView.class),
				new MenuItemInfo("Presupuestos", "la la-file",  HelloWorldView.class),
				new MenuItemInfo("Usuarios", "la la-key", UsersView.class)

		};
		List<RouterLink> links = new ArrayList<>();
		for (MenuItemInfo menuItemInfo : menuItems) {
			if (accessChecker.hasAccess(menuItemInfo.getView())) {
				links.add(createLink(menuItemInfo));
			}



		}
		return links;
	}

	private static RouterLink createLink(MenuItemInfo menuItemInfo) {
		return createLink(menuItemInfo.getView(), menuItemInfo.getIconClass(), menuItemInfo.getText());

	}

	private static RouterLink createLink(Class<? extends Component> classView, String iconClass, String name) {
		RouterLink link = new RouterLink();
		link.addClassNames("flex", "mx-s", "p-s", "relative", "text-secondary");
		link.setRoute(classView);

		Span icon = new Span();
		icon.addClassNames("me-s", "text-l");
		if (!iconClass.isEmpty()) {
			icon.addClassNames(iconClass);
		}

		Span text = new Span(name);
		text.addClassNames("font-medium", "text-s");

		link.add(icon, text);
		return link;
	}

	public static Div icontList(String iconClass, String name) {
		Div div = new Div();
		Span icon = new Span();
		icon.addClassNames("me-s", "text-l");
		if (StringUtils.isNotBlank(iconClass)) {
			icon.addClassNames(iconClass);
		}

		Span text = new Span(name);
		text.addClassNames("font-medium", "text-s");

		div.add(icon, text);
		return div;
	}

	private Footer createFooter() {
		Footer layout = new Footer();
		layout.addClassNames("flex", "items-center", "my-s", "px-m", "py-xs");

		Optional<User> maybeUser = authenticatedUser.get();
		if (maybeUser.isPresent()) {
			User user = maybeUser.get();

			UserConfig userConfig = userConfigRepository.findByUser_Id(user.getId());
			if (Objects.nonNull(userConfig)) {
				ThemeList themeList = UI.getCurrent().getElement().getThemeList();
				if (!themeList.contains(userConfig.getTheme())) { // (2)
					themeList.add(userConfig.getTheme());
				} else {
					themeList.remove(Lumo.DARK);
				}
				toggleDark.setValue(userConfig.getTheme().equals(Lumo.DARK));
			}
			Avatar avatar = new Avatar(user.getName(), user.getProfilePictureUrl());
			avatar.addClassNames("me-xs");

			ContextMenu userMenu = new ContextMenu(avatar);
			userMenu.setOpenOnClick(true);
			userMenu.addItem(icontList("la la-user", "Profile"), e -> {
				(new Div()).add(createLink(UserProfileView.class, "", "Nombre"));
			}).addClickListener(event -> {
				UI.getCurrent().navigate(UserProfileView.class);
			});
			userMenu.addItem(icontList("la la-user-slash", "Logout"), e -> {
				authenticatedUser.logout();
			});

			Span name = new Span(Objects.nonNull(user.getName()) ? user.getName() : user.getUsername());
			name.addClassNames("font-medium", "text-s", "text-secondary");

			layout.add(avatar, name);
		} else {
			Anchor loginLink = new Anchor("login", "Iniciar sesión");
			layout.add(loginLink);
		}

		return layout;
	}

	@Override
	protected void afterNavigation() {
		super.afterNavigation();
		viewTitle.setText(getCurrentPageTitle());
	}

	private String getCurrentPageTitle() {
		PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
		return title == null ? "" : title.value();
	}
}
