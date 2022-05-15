package com.gnc.task.application.views.clientsconfig;

import java.util.*;

import javax.annotation.security.RolesAllowed;

import com.gnc.task.application.data.dto.ClienteDTO;
import com.gnc.task.application.data.dto.VehiculoDTO;
import com.gnc.task.application.data.entity.Vehiculo;
import com.gnc.task.application.data.service.VehicleService;
import com.gnc.task.application.utilities.ClientUtils;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.textfield.IntegerField;
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
	public static final String NEW_VEHICULO = "Nuevo Vehículo";
	public static final String GUARDAR = "Guardar";
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	// creacion de iconos de clientes
	private final Icon refreshIcon = new Icon(VaadinIcon.REFRESH);
	private final Icon addIcon = new Icon(VaadinIcon.PLUS);
	private final Icon editIcon = new Icon(VaadinIcon.PENCIL);
	private final Icon deleteIcon = new Icon(VaadinIcon.TRASH);
	private final Icon check = new Icon(VaadinIcon.CHECK);
	private final Icon close = new Icon(VaadinIcon.CLOSE);

	// creacion de iconos de vehiculos
	private final Icon refreshData = new Icon(VaadinIcon.REFRESH);
	private final Icon addData = new Icon(VaadinIcon.PLUS);
	private final Icon editData = new Icon(VaadinIcon.PENCIL);
	private final Icon deleteData = new Icon(VaadinIcon.TRASH);
	private final Icon checkData = new Icon(VaadinIcon.CHECK);
	private final Icon closeData = new Icon(VaadinIcon.CLOSE);

	// creacion de botones de clientes
	private final Button refreshButton = new Button(refreshIcon);
	private final Button addButton = new Button(addIcon);
	private final Button editButton = new Button(editIcon);
	private final Button deleteButton = new Button(deleteIcon);

	// creacion de botones de vehiculos
	private final Button refreshDataButton = new Button(refreshData);
	private final Button addDataButton = new Button(addData);
	private final Button editDataButton = new Button(editData);
	private final Button deleteDataButton = new Button(deleteData);

	//pop up cliente
	private Dialog popUp;
	//pop up vehiculo
	private Dialog popUpData;

	// creacion de botones para la ventana de creacion de cliente
	private final Button saveButton = new Button(GUARDAR, check);
	private final Button cancelButton = new Button("Cancelar", close);
	// creacion de botones para la ventana de creacion de vehiculos
	private final Button saveData = new Button("GUARDAR", checkData);
	private final Button cancelData = new Button("Cancelar", closeData);



	//campos que pertenecen a la fila (grid) de clientes en la ventana de clientes
	private Grid<ClienteDTO> clientsGrid = new Grid<>();
	private TextField nombre;
	private TextField apellido;
	private TextField dni;
	private TextField direccion;
	private TextField telefono;
	private TextField email;

	private Grid<VehiculoDTO> vehiculoGrid = new Grid<>();
	private TextField dominio;
	private TextField marca;
	private TextField modelo;
	private IntegerField kilometro;
	private TextField año;


	// Binder es el encargado de enlazar los campos de front con los campos de la base de datos
	private Binder<Cliente> clientsBinder;
	private ListDataProvider<ClienteDTO> dataClienteProvider;
	private ListDataProvider<VehiculoDTO> dataVehiculoProvider;
	private final Details details = new Details();


	private FormLayout editorDiv;
	private HorizontalLayout buttonDiv;
	private AuthenticatedUser authenticatedUser;
	private ClientsService clientsService;

	private VehicleService vehicleService;
	private ClientDetailsServiceImpl clientDetailsServiceImpl;

	public ClientsConfigView(AuthenticatedUser authenticatedUser, ClientsService clientsService,
							 ClientDetailsServiceImpl clientDetailsServiceImpl, VehicleService vehicleService) {
		this.authenticatedUser = authenticatedUser;
		this.clientsService = clientsService;
		this.vehicleService = vehicleService;
		this.clientDetailsServiceImpl = clientDetailsServiceImpl;

		Optional<User> userLogged = authenticatedUser.get();
		if (userLogged.isPresent() && userLogged.get().getRoles().contains(Role.ADMIN)) {
			setSizeFull();
			//crearPopUpCliente();
			//crearPopUpVehiculo();
			//details.setVisible(false);
			VerticalLayout verticalLayout = new VerticalLayout();
			verticalLayout.setSizeFull();
			
			createButtonLayoutCliente(verticalLayout);
			createButtonLayoutVehiculo();
			//crear popUps
			//formPopUpCliente("");
			//formPopUpVehiculo("");

			//limpia popUps
			//limpiarFormCliente();
			//limpiarFormVehiculo();
			//crea estructura de los grids
			crearGridCliente(verticalLayout);
			crearGridVehiculo();

			agregarSeccionVehiculo(verticalLayout);

			cargarClienteGrid(clientsService);
			cargarVehiculoGrid();

			addFiltersToGrid();

			agregarFuncionBoton();



		}

	}

	private void agregarSeccionVehiculo(VerticalLayout verticalLayout) {
		Div wrapper = new Div();
		wrapper.setId("grid-wrapper");
		H4 headline = new H4("Vehículo/s:");
		headline.getStyle().set("margin-top", "0");
		wrapper.setSizeFull();
		wrapper.add(headline);
		verticalLayout.add(wrapper);
		Div layout = new Div();
		layout.setId("div-layout");
		layout.setWidthFull();
		layout.setSizeFull();
		layout.setHeight("45%");
		layout.add(vehiculoGrid);
		Div buttonsData = new Div();
		buttonsData.setId("div-buttons");
		buttonsData.setWidthFull();
		buttonsData.setHeight("15%");
		buttonsData.add(refreshDataButton, addDataButton, editDataButton, deleteDataButton);
		wrapper.add(buttonsData,layout);
		verticalLayout.add(wrapper);
		add(verticalLayout);
	}

	private void cargarVehiculoGrid() {
		//Cada vez que se selecciona un elemento de la grilla si el evento no tiene valor (por ejemplo el Crear
		// Vehiculo) entonces seteamos el boton de guardar con el texto "Nuevo Vehiculo", mientras que si el evento
		// tiene valor (por ejemplo el Modificar Vehiculo) entonces seteamos el boton con el texto "Guardar"
		vehiculoGrid.asSingleSelect().addValueChangeListener(event -> {
			if (event.getValue() != null) {
				saveButton.setText(GUARDAR);
			} else {
				vehiculoGrid.getDataProvider().refreshAll();
				saveButton.setText(NEW_VEHICULO);
			}
		});
		ViewUtils.gridReziseColumns(vehiculoGrid);
	}

	private void cargarClienteGrid(ClientsService clientsService) {
		List<ClienteDTO> clientesClienteDTOList = ClientUtils.getAllClients(clientsService);
		dataClienteProvider = new ListDataProvider<>(clientesClienteDTOList);
		clientsGrid.setDataProvider(dataClienteProvider);
		clientsGrid.getDataProvider().refreshAll();
		clientsGrid.select(null);
		dataClienteProvider.refreshAll();

		clientsGrid.asSingleSelect().addValueChangeListener(event -> {
			ClienteDTO cli = event.getValue();
			if (cli != null) {
				details.setSummaryText(
						"Vehiculos del cliente: " + cli.getNombre() + " - " + cli.getApellido());
				details.addOpenedChangeListener(e -> details.addContent(vehiculoGrid));
				details.setOpened(true);
				details.setVisible(true);

				try {
					refreshGridVehiculo(cli);
				} catch (Exception e) {
					LOGGER.error("Could not get results for the vehiculo , ERROR: {}", e.getMessage());
					vehiculoGrid.setDataProvider(new ListDataProvider<>(new ArrayList<>()));
				}
				refreshDataButton.click();
				saveButton.setText(GUARDAR);
			} else {
				details.setOpened(false);
				details.setVisible(false);
			}
		});
		ViewUtils.gridReziseColumns(clientsGrid);
	}

	private void crearGridVehiculo() {
		// creacion de la fila (Grid) con el encabezado con cada uno de los campos de Cliente
		vehiculoGrid.setSizeFull();
		vehiculoGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
		vehiculoGrid.addThemeName("grid-selection-theme");
		vehiculoGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.MATERIAL_COLUMN_DIVIDERS);

		vehiculoGrid.addColumn(VehiculoDTO::getDominio).setHeader("Dominio").setResizable(true).setKey("dominio")
				.setSortable(true);
		vehiculoGrid.addColumn(VehiculoDTO::getMarca).setHeader("Marca").setResizable(true).setKey("marca")
				.setSortable(true);
		vehiculoGrid.addColumn(VehiculoDTO::getModelo).setHeader("Modelo").setResizable(true).setKey("modelo").setSortable(true);
		vehiculoGrid.addColumn(VehiculoDTO::getKilometro).setHeader("Kilómetros").setResizable(true).setKey("kilometros")
				.setSortable(true);
		vehiculoGrid.addColumn(VehiculoDTO::getAño).setHeader("Año").setResizable(true).setKey("año")
				.setSortable(true);
	}

	private void crearGridCliente(VerticalLayout verticalLayout) {
		// creacion de la fila (Grid) con el encabezado con cada uno de los campos de Cliente
		clientsGrid.setSizeFull();
		clientsGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
		clientsGrid.addThemeName("grid-selection-theme");
		clientsGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.MATERIAL_COLUMN_DIVIDERS);

		clientsGrid.addColumn(ClienteDTO::getNombre).setHeader("Nombre").setResizable(true).setKey("name")
				.setSortable(true);
		clientsGrid.addColumn(ClienteDTO::getApellido).setHeader("Apellido").setResizable(true).setKey("Apellido")
				.setSortable(true);
		clientsGrid.addColumn(ClienteDTO::getDni).setHeader("DNI").setResizable(true).setKey("dni").setSortable(true);
		clientsGrid.addColumn(ClienteDTO::getTelefono).setHeader("Teléfono").setResizable(true).setKey("telefono")
				.setSortable(true);
		clientsGrid.addColumn(ClienteDTO::getDireccion).setHeader("Dirección").setResizable(true).setKey("direccion")
				.setSortable(true);
		clientsGrid.addColumn(ClienteDTO::getEmail).setHeader("Email").setResizable(true).setKey("email")
				.setSortable(true);
		verticalLayout.add(clientsGrid);
	}

	private void limpiarFormVehiculo() {
		//limpiar formulario vehiculo
		dominio.clear();
		marca.clear();
		modelo.clear();
		kilometro.clear();
		año.clear();
	}

	private void limpiarFormCliente() {
		//limpiar formulario cliente
		nombre.clear();
		apellido.clear();
		dni.clear();
		direccion.clear();
		telefono.clear();
		email.clear();
	}

	private void formPopUpVehiculo(String titulo) {
		//formulario popUp vehiculo
		FormLayout editorDiv = new FormLayout();
		editorDiv.setResponsiveSteps(
				new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
				new FormLayout.ResponsiveStep("600px", 1, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
		editorDiv.setSizeFull();
		editorDiv.setId("form-pop-up-vehiculo");
		H2 headlineV = new H2(titulo);
		headlineV.getStyle().set("margin-top", "0");
		//vehiculo
		dominio = ViewUtils.newBasicConfigTextField("Dominio");
		marca = ViewUtils.newBasicConfigTextField("Marca");
		modelo = ViewUtils.newBasicConfigTextField("Modelo");
		kilometro = ViewUtils.newBasicConfigIntegerField("Kilometraje");
		año = ViewUtils.newBasicConfigTextField("Año");
		editorDiv.add(headlineV, dominio, marca, modelo, kilometro, año);
		seccionBotones(cancelData, saveData);
		editorDiv.add(buttonDiv);
		popUpData.add(editorDiv);
	}

	private void formPopUpCliente(String titulo) {
		//formulario popUp cliente
		FormLayout formPopUp = new FormLayout();
		formPopUp.setResponsiveSteps(
				new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
				new FormLayout.ResponsiveStep("600px", 1, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
		formPopUp.setSizeFull();
		formPopUp.setId("form-pop-up-cliente");
		H2 headline = new H2(titulo);
		headline.getStyle().set("margin-top", "0");
		//cliente
		nombre = ViewUtils.newBasicConfigTextField("Nombre");
		apellido = ViewUtils.newBasicConfigTextField("Apellido");
		dni = ViewUtils.newBasicConfigTextField("DNI");
		direccion = ViewUtils.newBasicConfigTextField("Dirección");
		telefono = ViewUtils.newBasicConfigTextField("Teléfono");
		email = ViewUtils.newBasicConfigTextField("Email");
		formPopUp.add(headline, nombre, apellido, dni, direccion, telefono, email);
		seccionBotones(cancelButton, saveButton);
		formPopUp.add(buttonDiv);
		popUp.add(formPopUp);
		//popUp.setResizable(true);
	}

	/*Metodo encargado de crear la seccion de botones de los PopUp*/
	private void seccionBotones(Button cancel, Button save) {
		buttonDiv = new HorizontalLayout();
		buttonDiv.setId("button-layout");
		buttonDiv.setSizeFull();
		cancel.setWidthFull();
		save.setWidthFull();
		buttonDiv.add(save, cancel);
		buttonDiv.setAlignItems(Alignment.CENTER);
		buttonDiv.setVerticalComponentAlignment(Alignment.CENTER);
	}

	private void createButtonLayoutVehiculo() {
		closeData.setColor("#BE2123");
		ViewUtils.setButtonAutoMarginAndVariant(saveData, ButtonVariant.LUMO_TERTIARY);
		ViewUtils.setButtonAutoMarginAndVariant(cancelData, ButtonVariant.LUMO_ERROR);
		//botones de vehiculos
		refreshDataButton.getElement().setProperty("title", "Recargar Vehículos");
		addDataButton.getElement().setProperty("title", "Agregar Nuevo Vehículo");
		editDataButton.getElement().setProperty("title", "Editar  Vehículo");
		deleteDataButton.getElement().setProperty("title", "Borrar Vehículo");
	}

	private void createButtonLayoutCliente(VerticalLayout verticalLayout) {
		close.setColor("#BE2123");
		ViewUtils.setButtonAutoMarginAndVariant(saveButton, ButtonVariant.LUMO_TERTIARY);
		ViewUtils.setButtonAutoMarginAndVariant(cancelButton, ButtonVariant.LUMO_ERROR);
		//botones de clientes
		refreshButton.getElement().setProperty("title", "Recargar Clientes");
		addButton.getElement().setProperty("title", "Agregar Nuevo Cliente");
		editButton.getElement().setProperty("title", "Editar Cliente");
		deleteButton.getElement().setProperty("title", "Borrar Cliente");
		Div buttons = new Div();
		buttons.setId("div-buttons");
		buttons.setHeight("5%");
		buttons.add(refreshButton, addButton, editButton, deleteButton);
		verticalLayout.add(buttons);
	}

	/* Crea el PopUp cliente y le setea algunas propiedades */
	private void crearPopUpCliente() {
		popUp = new Dialog();
		popUp.setResizable(true);
		popUp.setCloseOnOutsideClick(false);
		popUp.setCloseOnEsc(true);
	}

	/* Crea el PopUp vehiculo y le setea algunas propiedades */
	private void crearPopUpVehiculo() {
		popUpData = new Dialog();
		popUpData.setResizable(true);
		popUpData.setCloseOnOutsideClick(false);
		popUpData.setCloseOnEsc(true);
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
	private void agregarFuncionBoton() {
		cancelButton.addClickListener(e -> {
			refreshGrid();
			popUp.close();
		});

		deleteButton.addClickListener(e -> {
			ClienteDTO client = clientsGrid.asSingleSelect().getValue();
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
							if (Objects.nonNull(client) && clientsService.deleteClientByClientID(Math.toIntExact(client.getId()))) {
								ViewUtils.notification("Borrado exitoso", "<b>Nombre:</b> " + nombre + "</br>",
										NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE).open();
								refreshGrid();
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

			ClienteDTO cliente = clientsGrid.asSingleSelect().getValue();
			Cliente	client;
			if (GUARDAR.equals(saveButton.getText())) {
				client = clientsService.getClientByDni(cliente.getDni());
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
						refreshGrid();
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
						refreshGrid();
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
		refreshButton.addClickListener(event -> refreshGrid());
		//Es lo que hace el boton + (Nuevo)
		addButton.addClickListener(event -> {
			crearPopUpCliente();
			formPopUpCliente("Crear Cliente");
			//limpia popUps
			limpiarFormCliente();
			clientsGrid.asSingleSelect().clear();
			saveButton.setText(NEW_CLIENT);
			popUpOpened(popUp);
		});

		// Boton de editar
		editButton.addClickListener(event -> {
			crearPopUpCliente();
			formPopUpCliente("Modificar Cliente");
			ClienteDTO client = clientsGrid.asSingleSelect().getValue();
			if (Objects.nonNull(client)) {
				populateForm(client);
				saveButton.setText(GUARDAR);
				popUpOpened(popUp);
			} else {
				ViewUtils.notification("Seleccionar cliente",
						"Usted debe seleccionar el cliente y hacer click en el botón de editar si desea editar el cliente."
								+ "</br>",
						NotificationVariant.LUMO_PRIMARY, Notification.Position.MIDDLE).open();
			}
		});

		//Botones de vehiculos
		refreshDataButton.addClickListener(event -> {
			ClienteDTO clienteDTO = clientsGrid.asSingleSelect().getValue();
			refreshGridVehiculo(clienteDTO);
		});

		addDataButton.addClickListener(event -> {
			ClienteDTO clienteDTO = clientsGrid.asSingleSelect().getValue();
			if (clienteDTO != null) {
				crearPopUpVehiculo();
				formPopUpVehiculo("Crear Vehículo");
				//limpia popUps
				limpiarFormVehiculo();
				saveData.setText(NEW_VEHICULO);
				popUpOpened(popUpData);
			} else {
				ViewUtils.notification("Seleccionar Cliente",
						"Usted debe seleccionar el cliente para poder asignar un nuevo vehículo al cliente."
								+ "</br>",
						NotificationVariant.LUMO_PRIMARY, Notification.Position.MIDDLE).open();
			}
		});

		deleteDataButton.addClickListener(event -> {
			ClienteDTO clienteDTO = clientsGrid.asSingleSelect().getValue();
			VehiculoDTO vehiculoDTO = vehiculoGrid.asSingleSelect().getValue();
			if (Objects.isNull(clienteDTO)) {
				ViewUtils.notification("Seleccionar vehículo",
						"Usted debe seleccionar el cliente, luego el vehiculo y hacer click en el botón de eliminar si desea eliminar el vehiculo."
								+ "</br>",
						NotificationVariant.LUMO_PRIMARY, Notification.Position.MIDDLE).open();
			} else {

				ConfirmDialog dialog = new ConfirmDialog("Confirmar Borrado",
						"Está seguro de eliminar el registro seleccionado?", "Borrar",
						confirmEvent -> {
							if (Objects.nonNull(vehiculoDTO) && vehicleService.deleteVehiculoByVehiculoID(Math.toIntExact(vehiculoDTO.getId()))) {
								ViewUtils.notification("Borrado exitoso", "<b>Dominio:</b> " + vehiculoDTO.getDominio() + "</br>",
										NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE).open();
								refreshGrid();
								saveButton.setText(NEW_CLIENT);
							} else { // no es posible borrar el cliente
								ViewUtils
										.notification("Vehículo no habilitado para la eliminación", "<b>Dominio:</b> "
												+ (Objects.nonNull(vehiculoDTO) ? vehiculoDTO.getDominio() : "<i>No está seleccionado</i>")
												+ "</br>", NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE)
										.open();
							}
						}, "Cancelar", cancelEvent -> {
				});
				dialog.setConfirmButtonTheme(NotificationVariant.LUMO_ERROR.getVariantName());
				dialog.open();
			}
		});

		editDataButton.addClickListener(event -> {
			ClienteDTO client = clientsGrid.asSingleSelect().getValue();
			if (Objects.nonNull(client)) {
				VehiculoDTO vehiculoDTO= vehiculoGrid.asSingleSelect().getValue();
				if (Objects.nonNull(vehiculoDTO)) {
					populateFormVehiculo(vehiculoDTO);
					saveButton.setText(GUARDAR);
					crearPopUp(popUpData);
				} else {
					ViewUtils.notification("Seleccionar vehículo",
							"Usted debe seleccionar el cliente, luego en el vehiculo y hacer click en el botón de editar si desea editar el vehiculo."
									+ "</br>",
							NotificationVariant.LUMO_PRIMARY, Notification.Position.MIDDLE).open();
				}
			} else {
				ViewUtils.notification("Seleccionar cliente",
						"Usted debe seleccionar el cliente, luego en el vehiculo y hacer click en el botón de editar si desea editar el vehiculo."
								+ "</br>",
						NotificationVariant.LUMO_PRIMARY, Notification.Position.MIDDLE).open();
			}
		});
		saveData.addClickListener(event -> {
			ClienteDTO clienteDTO =  clientsGrid.asSingleSelect().getValue();
			if (clienteDTO != null) {
				Cliente c = clientsService.getClientByDni(clienteDTO.getDni());
				VehiculoDTO vehiculoDTO =  vehiculoGrid.asSingleSelect().getValue();
				Vehiculo v;
				if (vehiculoDTO != null) {
					v = vehicleService.getVehiculoByByDominio(dominio.getValue());
					vehiculoDTO.setMarca(marca.getValue());
					vehiculoDTO.setModelo(modelo.getValue());
					vehiculoDTO.setAño(año.getValue());
					vehiculoDTO.setKilometro(kilometro.getValue());


				} else {
					vehiculoDTO = new VehiculoDTO();
					vehiculoDTO.setDominio(dominio.getValue());
					vehiculoDTO.setMarca(marca.getValue());
					vehiculoDTO.setModelo(modelo.getValue());
					vehiculoDTO.setAño(año.getValue());
					vehiculoDTO.setKilometro(kilometro.getValue());
					vehiculoDTO.setClienteId(clienteDTO.getId());
					clienteDTO.getVehiculoDTOS().add(vehiculoDTO);
					v = new Vehiculo();
					v.setDominio(dominio.getValue());
					v.setMarca(marca.getValue());
					v.setModelo(modelo.getValue());
					v.setAño(año.getValue());
					v.setKilometro(kilometro.getValue());
					v.setCliente(c);
					c.getVehiculos().add(v);

				}
				vehicleService.updateVehiculo(v);
     			refreshGridVehiculo(clienteDTO);

			} else {
				ViewUtils.notification("Seleccionar Cliente",
						"Debes seleccionar un cliente y luego hacer click en el boton de agregar vehiculo para agregar un nuevo vehiculo."
								+ "</br>",
						NotificationVariant.LUMO_PRIMARY, Notification.Position.MIDDLE).open();
			}
			popUpData.close();
		});
		cancelData.addClickListener(event -> {
			ClienteDTO client = clientsGrid.asSingleSelect().getValue();
			refreshGridVehiculo(client);
			popUpData.close();

		});
	}

	private void popUpOpened(Dialog popUp) {
		popUp.setCloseOnEsc(true);
		popUp.setCloseOnOutsideClick(false);
		popUp.open();
	}

	/* Crea el PopUp y le setea algunas propiedades */
	private void crearPopUp(Dialog pUp) {
		popUp = new Dialog();
		pUp.setResizable(true);
		pUp.setCloseOnOutsideClick(false);
		pUp.setCloseOnEsc(true);
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

	/* Este metodo carga en el PopUp todos los campos del objeto pasado como parametro*/
	private void populateForm(ClienteDTO client) {
		nombre.setValue(client.getNombre());
		apellido.setValue(client.getApellido());
		dni.setValue(client.getDni());
		direccion.setValue(client.getDireccion());
		telefono.setValue(client.getTelefono());
		email.setValue(client.getEmail());
	}

	private void populateFormVehiculo(VehiculoDTO vehiculo) {
		dominio.setValue(vehiculo.getDominio());
		dominio.setEnabled(false);
		marca.setValue(vehiculo.getMarca());
		modelo.setValue(vehiculo.getModelo());
		kilometro.setValue(vehiculo.getKilometro());
		año.setValue(vehiculo.getAño());
	}

	/* Este metodo es el encargado de refrescar los datos de la grilla cada vez que se modifica, se agrega o se borrado
	un dato*/
	private void refreshGrid() {
		List<ClienteDTO> clienteDTOListList = ClientUtils.getAllClients(clientsService);
		if (clienteDTOListList != null) {
			dataClienteProvider = new ListDataProvider<>(clienteDTOListList);
		} else {
			dataClienteProvider = new ListDataProvider<>(new ArrayList<>());
		}

		clientsGrid.setDataProvider(dataClienteProvider);
		clientsGrid.getDataProvider().refreshAll();
		clientsGrid.select(null);
		dataClienteProvider.refreshAll();
	}

	private void refreshGridVehiculo(ClienteDTO clienteDTO) {
		if(clienteDTO!=null) {
			List<VehiculoDTO> vehiculoDTOS = clienteDTO.getVehiculoDTOS();
			if (vehiculoDTOS != null) {
				dataVehiculoProvider = new ListDataProvider<>(vehiculoDTOS);
			} else {
				dataVehiculoProvider = new ListDataProvider<>(new ArrayList<>());
			}

			vehiculoGrid.setDataProvider(dataVehiculoProvider);
			vehiculoGrid.getDataProvider().refreshAll();
			vehiculoGrid.select(null);
			dataVehiculoProvider.refreshAll();
		}
	}
	/* Este metodo es el encargado de crear los filtros en la grilla*/
	private void addFiltersToGrid() {
		HeaderRow filterRow = clientsGrid.appendHeaderRow();

		TextField apellidoFilter = ViewUtils.createNewFilterForColumnGrid();
		apellidoFilter.addValueChangeListener(event -> dataClienteProvider
				.addFilter(client -> StringUtils.containsIgnoreCase(client.getApellido(), apellidoFilter.getValue())));

		TextField dniFilter = ViewUtils.createNewFilterForColumnGrid();
		dniFilter.addValueChangeListener(event -> dataClienteProvider
				.addFilter(client -> StringUtils.containsIgnoreCase(client.getDni(), dniFilter.getValue())));

		ViewUtils.setFilterInColumnGrid(filterRow, apellidoFilter, clientsGrid.getColumnByKey("Apellido"));
		ViewUtils.setFilterInColumnGrid(filterRow, dniFilter, clientsGrid.getColumnByKey("dni"));

		//filtros de vehiculo
		HeaderRow filterRowV = vehiculoGrid.appendHeaderRow();

		TextField dominioFilter = ViewUtils.createNewFilterForColumnGrid();
		dominioFilter.addValueChangeListener(event -> dataVehiculoProvider
				.addFilter(vehiculo -> StringUtils.containsIgnoreCase(vehiculo.getDominio(), dominioFilter.getValue())));

		TextField marcaFilter = ViewUtils.createNewFilterForColumnGrid();
		marcaFilter.addValueChangeListener(event -> dataVehiculoProvider
				.addFilter(vehiculo -> StringUtils.containsIgnoreCase(vehiculo.getMarca(), marcaFilter.getValue())));

		TextField modeloFilter = ViewUtils.createNewFilterForColumnGrid();
		modeloFilter.addValueChangeListener(event -> dataVehiculoProvider
				.addFilter(vehiculo -> StringUtils.containsIgnoreCase(vehiculo.getModelo(), modeloFilter.getValue())));

		ViewUtils.setFilterInColumnGrid(filterRowV, dominioFilter, vehiculoGrid.getColumnByKey("dominio"));
		ViewUtils.setFilterInColumnGrid(filterRowV, marcaFilter, vehiculoGrid.getColumnByKey("marca"));
		ViewUtils.setFilterInColumnGrid(filterRowV, modeloFilter, vehiculoGrid.getColumnByKey("modelo"));

	}

}
