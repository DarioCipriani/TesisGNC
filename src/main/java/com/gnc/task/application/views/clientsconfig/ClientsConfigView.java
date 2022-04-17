package com.gnc.task.application.views.clientsconfig;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.security.RolesAllowed;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gnc.task.application.data.Role;
import com.gnc.task.application.data.entity.Cliente;
import com.gnc.task.application.data.entity.User;
import com.gnc.task.application.data.service.ClientsService;
import com.gnc.task.application.security.AuthenticatedUser;
import com.gnc.task.application.security.ClientDetailsServiceImpl;
import com.gnc.task.application.utilities.ViewUtils;
import com.gnc.task.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Clientes")
@Route(value = "clientes", layout = MainLayout.class)
@RolesAllowed("admin")
public class ClientsConfigView extends HorizontalLayout {

	public static final String NEW_CLIENT = "Nuevo Cliente";
	public static final String GUARDAR = "Guardar";
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	// creacion de iconos
	private final Icon refreshIcon = new Icon(VaadinIcon.REFRESH);
	private final Icon addIcon = new Icon(VaadinIcon.PLUS);
	private final Icon editIcon = new Icon(VaadinIcon.EDIT);
	private final Icon deleteIcon = new Icon(VaadinIcon.TRASH);
	// creacion de botones
	private final Button refreshButton = new Button(refreshIcon);
	private final Button addButton = new Button(addIcon);
	private final Button editButton = new Button(editIcon);
	private final Button deleteButton = new Button(deleteIcon);
	// creacion de iconos para la ventana de creacion de cliente
	private final Icon check = new Icon(VaadinIcon.CHECK);
	private final Icon close = new Icon(VaadinIcon.CLOSE);
	// creacion de botones para la ventana de creacion de cliente
	private final Button saveButton = new Button(GUARDAR, check);
	private final Button cancelButton = new Button("Cancelar", close);
	//campos que pertenecen a la fila (grid) de clientes en la ventana de clientes
	private Grid<Cliente> clientsGrid;
	private TextField nombre;
	private TextField apellido;
	private TextField dni;
	private TextField direccion;
	private TextField telefono;
	private TextField email;
	// Binder es el encargado de enlazar los campos de front con los campos de la base de datos
	private Binder<Cliente> clientsBinder;
	private ListDataProvider<Cliente> dataProvider;
	private Dialog popUp;
	private FormLayout editorDiv;
	private HorizontalLayout buttonDiv;
	private AuthenticatedUser authenticatedUser;
	private ClientsService clientsService;
	private ClientDetailsServiceImpl clientDetailsServiceImpl;

	public ClientsConfigView(AuthenticatedUser authenticatedUser, ClientsService clientsService,
			ClientDetailsServiceImpl clientDetailsServiceImpl) {
		this.authenticatedUser = authenticatedUser;
		this.clientsService = clientsService;
		this.clientDetailsServiceImpl = clientDetailsServiceImpl;
		Optional<User> userLogged = authenticatedUser.get();
		if (userLogged.isPresent() && userLogged.get().getRoles().contains(Role.ADMIN)) {
			setSizeFull();
			crearPopUp();
			editor("");
			seccionBotones();
			editorDiv.add(buttonDiv);
			popUp.add(editorDiv);
			// creacion de la fila (Grid) con el encabezado con cada uno de los campos de Cliente
			clientsGrid = new Grid<>();
			clientsGrid.setSizeFull();
			clientsGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
			clientsGrid.addThemeName("grid-selection-theme");
			clientsGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.MATERIAL_COLUMN_DIVIDERS);
			clientsGrid.addColumn(Cliente::getNombre).setHeader("Nombre").setResizable(true).setKey("name")
					.setSortable(true);
			clientsGrid.addColumn(Cliente::getApellido).setHeader("Apellido").setResizable(true).setKey("Apellido")
					.setSortable(true);
			clientsGrid.addColumn(Cliente::getDni).setHeader("DNI").setResizable(true).setKey("dni").setSortable(true);
			clientsGrid.addColumn(Cliente::getTelefono).setHeader("Teléfono").setResizable(true).setKey("telefono")
					.setSortable(true);
			clientsGrid.addColumn(Cliente::getDireccion).setHeader("Dirección").setResizable(true).setKey("direccion")
					.setSortable(true);
			clientsGrid.addColumn(Cliente::getEmail).setHeader("Email").setResizable(true).setKey("email")
					.setSortable(true);

			clientsBinder = new Binder<>(Cliente.class);
			clientsBinder.bind(nombre, Cliente::getNombre, Cliente::setNombre);
			clientsBinder.bind(apellido, Cliente::getApellido, Cliente::setApellido);
			clientsBinder.bind(dni, Cliente::getDni, Cliente::setDni);
			clientsBinder.bind(direccion, Cliente::getDireccion, Cliente::setDireccion);
			clientsBinder.bind(telefono, Cliente::getTelefono, Cliente::setTelefono);
			clientsBinder.bind(email, Cliente::getEmail, Cliente::setEmail);

			clientsBinder.bindInstanceFields(this);

			dataProvider = new ListDataProvider<>(clientsService.getAllClients());
			clientsGrid.setDataProvider(dataProvider);

			addFiltersToGrid();
			//Cada vez que se selecciona un elemento de la grilla si el evento no tiene valor (por ejemplo el Crear
			// Cliente) entonces seteamos el boton de guardar con el texto "Nuevo Cliente", mientras que si el evento
			// tiene valor (por ejemplo el Modificar Cliente) entonces seteamos el boton con el texto "Guardar"
			clientsGrid.asSingleSelect().addValueChangeListener(event -> {
				if (event.getValue() != null) {
					saveButton.setText(GUARDAR);
				} else {
					clientsGrid.getDataProvider().refreshAll();
					saveButton.setText(NEW_CLIENT);
				}
			});
			// se agregan las acciones a cada boton
			addButtonListeners();

			VerticalLayout layout = new VerticalLayout();
			layout.setSizeFull();

			createButtonLayout(layout);
			createGridLayout(layout);

			add(layout);

		}

	}
	/*Metodo encargado de crear el grid principal cuando se abre cada sub menu*/
	private void createGridLayout(VerticalLayout layout) {
		Div wrapper = new Div();
		wrapper.setId("wrapper");
		wrapper.setSizeFull();
		layout.add(wrapper);
		wrapper.add(clientsGrid);
	}
	/*Metodo encargado de decir a cada boton que es lo que tiene que hacer*/
	private void addButtonListeners() {
		cancelButton.addClickListener(e -> {
			refreshGrid(clientDetailsServiceImpl);
			popUp.close();
		});

		deleteButton.addClickListener(e -> {
			Cliente client = clientsGrid.asSingleSelect().getValue();
			if (Objects.isNull(client)) {
				ViewUtils.notification("Seleccionar cliente",
						"Usted debe seleccionar el cliente y hacer click en el botón de eliminar si desea eliminar el cliente."
								+ "</br>",
						NotificationVariant.LUMO_PRIMARY, Notification.Position.MIDDLE).open();
			} else {
				String nombre = Objects.nonNull(client)
						? client.getNombre().concat(" ").concat(client.getApellido())
						: "";
				ConfirmDialog dialog = new ConfirmDialog("Confirmar Borrado",
						"Está seguro de eliminar el registro seleccionado? Nombre del Cliente: " + nombre, "Borrar",
						confirmEvent -> {
							if (Objects.nonNull(client) && clientsService.deleteClientByClientID(client.getId())) {
								ViewUtils.notification("Borrado exitoso", "<b>Nombre:</b> " + nombre + "</br>",
										NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE).open();
								refreshGrid(clientDetailsServiceImpl);
								saveButton.setText(NEW_CLIENT);
							} else { // no es posible borrar el cliente
								ViewUtils
										.notification("Cliente no habilitado para la eliminación", "<b>Nombre:</b> "
												+ (Objects.nonNull(client) ? nombre : "<i>No está seleccionado</i>")
												+ "</br>", NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE)
										.open();
							}
						}, "Cancelar", cancelEvent -> {
						});
				dialog.setConfirmButtonTheme(NotificationVariant.LUMO_ERROR.getVariantName());
				dialog.open();
			}
		});

		// Boton de Guardar
		saveButton.addClickListener(e -> {

			Cliente client = clientsGrid.asSingleSelect().getValue();
			if (GUARDAR.equals(saveButton.getText())) {
				client = clientsService.getClientByDni(client.getDni());
				if (Objects.nonNull(client)) {
					client.setNombre(nombre.getValue());
					client.setApellido(apellido.getValue());
					client.setDni(dni.getValue());
					client.setDireccion(direccion.getValue());
					client.setTelefono(telefono.getValue());
					client.setEmail(email.getValue());
					client.setUpdatedAt(new Date());

					try{
						clientsService.updateClient(client);
						popUp.close();
						ViewUtils
								.notification("Cliente Actualizado",
										"<b>Nombre:</b> " + client.getNombre().concat(" ").concat(client.getApellido())
												+ "</br>",
										NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE)
								.open();
						refreshGrid(clientDetailsServiceImpl);
					}catch (Exception exception){
						ViewUtils
								.notification("Cliente No Actualizado",
										"<b>Causa:</b> " + exception.getCause() + "</br>",
										NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE)
								.open();
					}

				} else {
					LOGGER.error("El Cliente no existe o hay un problema para obtener sus datos");
					ViewUtils.notification("Algo esta mal",
							"El cliente no puede ser cargado, intente refrescando la pagina y volviendo a realizar los cambios, si el error continúa, consulte con el administrador. </br>",
							NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE).open();
				}

			} else {
				if (NEW_CLIENT.equals(saveButton.getText())) {
					client = new Cliente();
					client.setNombre(nombre.getValue());
					client.setApellido(apellido.getValue());
					client.setDni(dni.getValue());
					client.setDireccion(direccion.getValue());
					client.setTelefono(telefono.getValue());
					client.setEmail(email.getValue());
					client.setCreatedAt(new Date());
					client.setUpdatedAt(new Date());

					try{
						clientsService.updateClient(client);
						popUp.close();
						refreshGrid(clientDetailsServiceImpl);
						ViewUtils
								.notification("Cliente creado",
										"<b>Cliente:</b> " + client.getNombre().concat(" ").concat(client.getApellido())
												+ "</br>",
										NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE)
								.open();
					}catch (Exception exception){
						ViewUtils
								.notification("Cliente No creado",
										"<b>Causa:</b> " + exception.getCause() + "</br>",
										NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE)
								.open();
					}

				} else
					ViewUtils.notification("Algo esta mal",
							"El cliente no puede ser cargado, intente refrescando la página y volviendo a realizar los cambios, si el error continúa, consulte con el administrador. </br>",
							NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE).open();
			}

		});
		// El boton de refrescar es el encargado de decirle al grid que recargue nuevamente los datos que trae de la
		// base de datos
		refreshButton.addClickListener(event -> refreshGrid(clientDetailsServiceImpl));
		//Es lo que hace el boton +
		addButton.addClickListener(event -> {
			crearPopUp();
			editor("Crear Cliente");
			seccionBotones();
			editorDiv.add(buttonDiv);
			popUp.add(editorDiv);
			clientsGrid.asSingleSelect().clear();
			clearForm();
			saveButton.setText(NEW_CLIENT);
			popUpOpened();
		});

		// Boton de editar
		editButton.addClickListener(event -> {
			crearPopUp();
			editor("Modificar Cliente");
			seccionBotones();
			editorDiv.add(buttonDiv);
			popUp.add(editorDiv);
			Cliente client = clientsGrid.asSingleSelect().getValue();
			if (Objects.nonNull(client)) {
				populateForm(client);
				saveButton.setText(GUARDAR);
				popUpOpened();
			} else {
				ViewUtils.notification("Seleccionar cliente",
						"Usted debe seleccionar el cliente y hacer click en el botón de editar si desea editar el cliente."
								+ "</br>",
						NotificationVariant.LUMO_PRIMARY, Notification.Position.MIDDLE).open();
			}
		});

	}
	/* Crea el PopUp y le setea algunas propiedades */
	private void crearPopUp() {
		popUp = new Dialog();
		popUp.setResizable(true);
		popUp.setCloseOnOutsideClick(false);
		popUp.setCloseOnEsc(true);
	}
	/*Metodo encargado de crear la seccion de botones de los PopUp*/
	private void seccionBotones() {
		buttonDiv = new HorizontalLayout();
		buttonDiv.setId("button-layout");
		buttonDiv.setSizeFull();
		buttonDiv.add(saveButton, cancelButton);
		buttonDiv.setAlignItems(Alignment.CENTER);
		buttonDiv.setVerticalComponentAlignment(Alignment.CENTER);
	}
	/* Creacion de los campos del PopUp */
	private void editor(String titulo) {
		editorDiv = new FormLayout();
		editorDiv.setId("editor");
		editorDiv.setSizeFull();
		editorDiv.setResponsiveSteps(
				new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
				new FormLayout.ResponsiveStep("600px", 1, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
		nombre = ViewUtils.newBasicConfigTextField("Nombre");
		apellido = ViewUtils.newBasicConfigTextField("Apellido");
		dni = ViewUtils.newBasicConfigTextField("DNI");
		direccion = ViewUtils.newBasicConfigTextField("Dirección");
		telefono = ViewUtils.newBasicConfigTextField("Teléfono");
		email = ViewUtils.newBasicConfigTextField("Email");
		//Creacion del titulo del PopUp
		H2 headline = new H2(titulo);
		headline.getStyle().set("margin-top", "0");
		editorDiv.add(headline, nombre, apellido, dni, direccion, telefono, email);
	}
	/* Seteo del titulo a lo que se refiere cada boton al acercar el mouse al boton*/
	private void createButtonLayout(VerticalLayout layout) {
		close.setColor("#BE2123");
		ViewUtils.setButtonAutoMarginAndVariant(saveButton, ButtonVariant.LUMO_TERTIARY);
		ViewUtils.setButtonAutoMarginAndVariant(cancelButton, ButtonVariant.LUMO_ERROR);
		refreshButton.getElement().setProperty("title", "Actualizar Clientes");
		addButton.getElement().setProperty("title", "Agregar Nuevo Cliente");
		editButton.getElement().setProperty("title", "Editar Cliente");
		deleteButton.getElement().setProperty("title", "Borrar Cliente");
		ViewUtils.buttonConfig(layout, addButton, deleteButton, refreshButton, editButton);
	}
	/* Este metodo carga en el PopUp todos los campos del objeto pasado como parametro*/
	private void populateForm(Cliente client) {
		clientsBinder.readBean(client);
		nombre.setValue(client.getNombre());
		apellido.setValue(client.getApellido());
		dni.setValue(client.getDni());
		direccion.setValue(client.getDireccion());
		telefono.setValue(client.getTelefono());
		email.setValue(client.getEmail());
	}
	/* Abre el PopUp y le setea algunas propiedades */
	private void popUpOpened() {
		popUp.setCloseOnEsc(true);
		popUp.setCloseOnOutsideClick(false);
		popUp.open();
	}
	/* Este metodo se encarga de limpiar los campos del PopUp, por ejemplo el PopUp Crear Cliente*/
	private void clearForm() {
		nombre.clear();
		apellido.clear();
		dni.clear();
		direccion.clear();
		telefono.clear();
		email.clear();
	}
	/* Este metodo es el encargado de refrescar los datos de la grilla cada vez que se modifica, se agrega o se borrado
	un dato*/
	private void refreshGrid(ClientDetailsServiceImpl clienDetailsServiceImpl) {
		dataProvider = new ListDataProvider<>(clienDetailsServiceImpl.findAllClient());
		clientsGrid.setItems(dataProvider);
		clientsGrid.getDataProvider().refreshAll();
		clientsGrid.select(null);
		dataProvider.refreshAll();
	}
	/* Este metodo es el encargado de crear los filtros en la grilla*/
	private void addFiltersToGrid() {
		HeaderRow filterRow = clientsGrid.appendHeaderRow();

		TextField apellidoFilter = ViewUtils.createNewFilterForColumnGrid();
		apellidoFilter.addValueChangeListener(event -> dataProvider
				.addFilter(client -> StringUtils.containsIgnoreCase(client.getApellido(), apellidoFilter.getValue())));

		TextField dniFilter = ViewUtils.createNewFilterForColumnGrid();
		dniFilter.addValueChangeListener(event -> dataProvider
				.addFilter(client -> StringUtils.containsIgnoreCase(client.getDni(), dniFilter.getValue())));

		ViewUtils.setFilterInColumnGrid(filterRow, apellidoFilter, clientsGrid.getColumnByKey("Apellido"));
		ViewUtils.setFilterInColumnGrid(filterRow, dniFilter, clientsGrid.getColumnByKey("dni"));

	}

}
