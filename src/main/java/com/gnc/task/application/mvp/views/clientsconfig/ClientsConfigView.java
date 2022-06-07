package com.gnc.task.application.mvp.views.clientsconfig;

import com.gnc.task.application.mvp.model.entity.Cliente;
import com.gnc.task.application.mvp.model.entity.Role;
import com.gnc.task.application.mvp.model.entity.User;
import com.gnc.task.application.mvp.model.entity.Vehiculo;
import com.gnc.task.application.mvp.presenter.ClientsService;
import com.gnc.task.application.mvp.presenter.VehicleService;
import com.gnc.task.application.mvp.views.MainLayout;
import com.gnc.task.application.security.AuthenticatedUser;
import com.gnc.task.application.security.ClientDetailsServiceImpl;
import com.gnc.task.application.utilities.ViewUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addons.componentfactory.MaskedTextField;
import org.vaadin.gatanaso.MultiselectComboBox;

import javax.annotation.security.RolesAllowed;
import java.util.*;
import java.util.stream.Collectors;

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
    // creacion de botones para la ventana de creacion de cliente
    private final Button saveButton = new Button(GUARDAR, check);
    private final Button cancelButton = new Button("Cancelar", close);
    // creacion de botones para la ventana de creacion de vehiculos
    private final Button saveData = new Button("GUARDAR", checkData);
    private final Button cancelData = new Button("Cancelar", closeData);
    private final Details details = new Details();
    //campos que pertenecen a la fila (grid) de clientes en la ventana de clientes
    private final Grid<Cliente> clientsGrid = new Grid<>();
    private final Grid<Vehiculo> vehiculoGrid = new Grid<>();
    private final AuthenticatedUser authenticatedUser;
    private final ClientsService clientsService;
    private final VehicleService vehicleService;
    private final ClientDetailsServiceImpl clientDetailsServiceImpl;
    TextField filterText = new TextField();
    //pop up cliente
    private Dialog popUp;
    //pop up vehiculo
    private Dialog popUpData;
    private TextField nombre;
    private TextField apellido;
    private IntegerField dni;
    private TextField direccion;
    private MaskedTextField telefono;
    private EmailField email;
    private MultiselectComboBox<Vehiculo> vehiculoMultiselectComboBox;
    private TextField dominio;
    private TextField marca;
    private TextField modelo;
    private IntegerField kilometro;
    private IntegerField año;
    private ListDataProvider<Cliente> dataClienteProvider;
    private ListDataProvider<Vehiculo> dataVehiculoProvider;
    private HorizontalLayout buttonDiv;

    public ClientsConfigView(AuthenticatedUser authenticatedUser, ClientsService clientsService,
                             ClientDetailsServiceImpl clientDetailsServiceImpl, VehicleService vehicleService) {
        this.authenticatedUser = authenticatedUser;
        this.clientsService = clientsService;
        this.vehicleService = vehicleService;
        this.clientDetailsServiceImpl = clientDetailsServiceImpl;

        Optional<User> userLogged = authenticatedUser.get();
        if (userLogged.isPresent() && userLogged.get().getRoles().contains(Role.ADMIN)) {
            setSizeFull();

            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSizeFull();

            createButtonLayoutCliente(verticalLayout);
            createButtonLayoutVehiculo();

            crearGridCliente(verticalLayout);
            vehiculoMultiselectComboBox = new MultiselectComboBox<>();
            vehiculoMultiselectComboBox.setItems(vehicleService.getAllVehiculos());
            crearGridVehiculo();

            agregarSeccionVehiculo(verticalLayout);

            cargarClienteGrid(clientsService, null);
            cargarVehiculoGrid();

            cargarFiltroCliente();
            cargarFiltroVehiculo();
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
        layout.setHeightFull();
        layout.setHeight("45%");
        layout.add(vehiculoGrid);
        Div buttonsData = new Div();
        buttonsData.setId("div-buttons");
        buttonsData.setWidthFull();
        buttonsData.setHeight("15%");
        buttonsData.add(refreshDataButton, addDataButton, editDataButton);
        wrapper.add(buttonsData, layout);
        verticalLayout.add(wrapper);
        add(verticalLayout);
    }

    /* Cada vez que se selecciona un elemento de la grilla si el evento no tiene valor (por ejemplo el Crear
     Vehiculo) entonces seteamos el boton de guardar con el texto "Nuevo Vehiculo", mientras que si el evento
     tiene valor (por ejemplo el Modificar Vehiculo) entonces seteamos el boton con el texto "Guardar" */
    private void cargarVehiculoGrid() {
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

    private void cargarClienteGrid(ClientsService clientsService, String dni) {
        dataClienteProvider = new ListDataProvider<>(clientsService.getAllClients());
        clientsGrid.setDataProvider(dataClienteProvider);
        clientsGrid.getDataProvider().refreshAll();
        clientsGrid.select(null);
        dataClienteProvider.refreshAll();
        clientsGrid.asSingleSelect().addValueChangeListener(event -> {
            refreshGridVehiculo(Objects.nonNull(event.getValue()) ? event.getValue().getDni() : dni);
        });
        ViewUtils.gridReziseColumns(clientsGrid);
        ViewUtils.gridReziseColumns(vehiculoGrid);
    }

    /* creacion de la fila (Grid) con el encabezado con cada uno de los campos de vehiculo */
    private void crearGridVehiculo() {
        vehiculoGrid.setSizeFull();
        vehiculoGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        vehiculoGrid.addThemeName("grid-selection-theme");
        vehiculoGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.MATERIAL_COLUMN_DIVIDERS);

        vehiculoGrid.addColumn(Vehiculo::getDominio).setHeader("Dominio").setResizable(true).setKey("dominio")
                .setSortable(true);
        vehiculoGrid.addColumn(Vehiculo::getMarca).setHeader("Marca").setResizable(true).setKey("marca")
                .setSortable(true);
        vehiculoGrid.addColumn(Vehiculo::getModelo).setHeader("Modelo").setResizable(true).setKey("modelo").setSortable(true);
        vehiculoGrid.addColumn(Vehiculo::getKilometro).setHeader("Kilómetros").setResizable(true).setKey("kilometros")
                .setSortable(true);
        vehiculoGrid.addColumn(Vehiculo::getAño).setHeader("Año").setResizable(true).setKey("año")
                .setSortable(true);
    }

    /* creacion de la fila (Grid) con el encabezado con cada uno de los campos de Cliente */
    private void crearGridCliente(VerticalLayout verticalLayout) {
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
        verticalLayout.add(clientsGrid);
    }

    /* limpiar formulario vehiculo */
    private void limpiarFormVehiculo() {
        dominio.clear();
        marca.clear();
        modelo.clear();
        kilometro.clear();
        año.clear();
    }

    /* limpiar formulario cliente */
    private void limpiarFormCliente() {
        nombre.clear();
        apellido.clear();
        dni.clear();
        direccion.clear();
        telefono.clear();
        email.clear();
    }

    /* formulario popUp vehiculo */
    private void formPopUpVehiculo(String titulo) {
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
        dominio.setRequiredIndicatorVisible(true);
        marca = ViewUtils.newBasicConfigTextField("Marca");
        modelo = ViewUtils.newBasicConfigTextField("Modelo");
        kilometro = ViewUtils.newBasicConfigIntegerField("Kilometraje");
        año = ViewUtils.newBasicConfigIntegerField("Año");
        editorDiv.add(headlineV, dominio, marca, modelo, kilometro, año);
        seccionBotones(cancelData, saveData);
        editorDiv.add(buttonDiv);
        popUpData.add(editorDiv);
    }

    /* formulario popUp cliente */
    private void formPopUpCliente(String titulo) {
        FormLayout formPopUp = new FormLayout();
        formPopUp.getStyle().set("flex-wrap", "wrap");
        formPopUp.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("600px", 1, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));

        formPopUp.setId("form-pop-up-cliente");
        H2 headline = new H2(titulo);
        headline.getStyle().set("margin-top", "0");
        //cliente
        nombre = ViewUtils.newBasicConfigTextField("Nombre");
        nombre.setRequiredIndicatorVisible(true);
        apellido = ViewUtils.newBasicConfigTextField("Apellido");
        apellido.setRequiredIndicatorVisible(true);
        dni = ViewUtils.newBasicConfigIntegerField("DNI");
        dni.setRequiredIndicatorVisible(true);
        direccion = ViewUtils.newBasicConfigTextField("Dirección");
        direccion.setRequiredIndicatorVisible(true);
        telefono = ViewUtils.newBasicConfigMaskedTextField("Teléfono", "+{54}-000000000000");
        email = ViewUtils.newBasicConfigEmailField("Email");

        List<Vehiculo> allVehiculos = vehicleService.getAllVehiculos();
        vehiculoMultiselectComboBox.setLabel("Seleccionar Vehículo/s");
        vehiculoMultiselectComboBox.setAllowCustomValues(false);
        vehiculoMultiselectComboBox.setItems(allVehiculos);
        vehiculoMultiselectComboBox.setItemLabelGenerator(Vehiculo::getDominio);
        add(vehiculoMultiselectComboBox);
        vehiculoMultiselectComboBox.addSelectionListener(event -> {
            event.getAddedSelection(); // get added items
            event.getRemovedSelection(); // get removed items
        });
        vehiculoMultiselectComboBox.setOrdered(true); // ordered values
        vehiculoMultiselectComboBox.setClearButtonVisible(true); // clear button visible

        formPopUp.add(headline, nombre, apellido, dni, direccion, telefono, email, vehiculoMultiselectComboBox);
        seccionBotones(cancelButton, saveButton);
        formPopUp.add(buttonDiv);
        popUp.add(formPopUp);
    }

    /* Metodo encargado de crear la seccion de botones de los PopUp */
    private void seccionBotones(Button cancel, Button save) {
        buttonDiv = new HorizontalLayout();
        buttonDiv.setId("button-layout");
        //buttonDiv.setSizeFull();
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
        editDataButton.getElement().setProperty("title", "Modificar  Vehículo");
    }

    private void createButtonLayoutCliente(VerticalLayout verticalLayout) {
        close.setColor("#BE2123");
        ViewUtils.setButtonAutoMarginAndVariant(saveButton, ButtonVariant.LUMO_TERTIARY);
        ViewUtils.setButtonAutoMarginAndVariant(cancelButton, ButtonVariant.LUMO_ERROR);
        //botones de clientes
        refreshButton.getElement().setProperty("title", "Recargar Clientes");
        addButton.getElement().setProperty("title", "Agregar Nuevo Cliente");
        editButton.getElement().setProperty("title", "Modificar Cliente");
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

    /* Metodo encargado de crear el grid principal cuando se abre cada sub menu */
    private void createGridLayout(VerticalLayout layout) {
        Div wrapper = new Div();
        wrapper.setId("wrapper");
        wrapper.setSizeFull();
        layout.add(wrapper);
        wrapper.add(clientsGrid);
    }

    /* Metodo encargado de decir a cada boton que es lo que tiene que hacer */
    private void agregarFuncionBoton() {
        cancelButton.addClickListener(e -> {
            refreshGridCliente();
            popUp.close();
        });
        //Eliminar Cliente: si el cliente tiene asociado vehiculos no se puede eliminar ni el cliene ni el vehiculo. Debe desactivarse el cliente
        deleteButton.addClickListener(e -> {
            Cliente client = clientsGrid.asSingleSelect().getValue();
            if (Objects.isNull(client)) {
                ViewUtils.notification("Seleccionar cliente",
                        "Usted debe seleccionar el cliente y hacer click en el botón de eliminar si desea eliminar el cliente."
                                + "</br>",
                        NotificationVariant.LUMO_PRIMARY, Notification.Position.MIDDLE, 15000).open();
            } else {
                String nombre = Objects.nonNull(client)
                        ? client.getNombre().concat(" ").concat(client.getApellido())
                        : "";
                ConfirmDialog dialog = new ConfirmDialog("Confirmar Borrado",
                        "Está seguro de eliminar el registro seleccionado? Nombre del Cliente: " + nombre, "Borrar",
                        confirmEvent -> {
                            if (Objects.nonNull(client) && (Objects.isNull(client.getVehiculos()) || (Objects.nonNull(client.getVehiculos()) && client.getVehiculos().isEmpty())) && clientsService.deleteClientByClientID(Math.toIntExact(client.getId()))) {
                                ViewUtils.notification("Borrado exitoso", "<b>Nombre:</b> " + nombre + "</br>",
                                        NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE, 1500).open();
                                refreshGridCliente();
                                saveButton.setText(NEW_CLIENT);
                            } else { // no es posible borrar el cliente
                                ViewUtils
                                        .notification("Cliente no habilitado para la eliminación. Un cliente sólo se puede eliminar si no tiene ningun vehículo asociado", "<b>Nombre:</b> "
                                                + (Objects.nonNull(client) ? nombre : "<i>" + client.getNombre() + "</i>")
                                                + "</br>", NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE, 15000)
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

            Cliente cliente = clientsGrid.asSingleSelect().getValue();
            Cliente client;
            if (GUARDAR.equals(saveButton.getText())) {//Guardado en modificar cliente
                client = clientsService.getClientByDni(cliente.getDni());
                String errors = validarCliente(false);
                if (Objects.nonNull(client) && StringUtils.isEmpty(errors)) {
                    client.setNombre(nombre.getValue());
                    client.setApellido(apellido.getValue());
                    client.setDni(dni.getValue().toString());
                    client.setDireccion(direccion.getValue());
                    client.setTelefono(telefono.getValue());
                    client.setEmail(email.getValue());
                    client.setUpdatedAt(new Date());

                    try {
                        cargarComboBoxVehiculo(client, vehiculoMultiselectComboBox.getSelectedItems().stream().collect(Collectors.toList()));
                        clientsService.updateClient(client);

                        popUp.close();
                        ViewUtils
                                .notification("Cliente Actualizado",
                                        "<b>Nombre:</b> " + client.getNombre().concat(" ").concat(client.getApellido())
                                                + "</br>",
                                        NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE, 1500)
                                .open();
                        refreshGridCliente();
                    } catch (Exception exception) {
                        System.out.println(exception);
                        ViewUtils
                                .notification("Cliente No Actualizado",
                                        "<b>Causa:</b> " + exception.getCause() + "</br>",
                                        NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE, 1500)
                                .open();
                        throw exception;

                    }

                } else {
                    LOGGER.error("Errores: {}", errors);
                    ViewUtils.notification("Errores:",
                            errors + " </br>",
                            NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE, 15000).open();
                }

            } else {
                String errors = validarCliente(true);
                if (NEW_CLIENT.equals(saveButton.getText()) && StringUtils.isEmpty(errors)) {//Guardar cliente nuevo
                    client = new Cliente();
                    client.setNombre(nombre.getValue());
                    client.setApellido(apellido.getValue());
                    client.setDni(dni.getValue().toString());
                    client.setDireccion(direccion.getValue());
                    client.setTelefono(telefono.getValue());
                    client.setEmail(email.getValue());
                    client.setCreatedAt(new Date());
                    client.setUpdatedAt(new Date());

                    try {
                        clientsService.updateClient(client);
                        cargarComboBoxVehiculo(client, vehiculoMultiselectComboBox.getSelectedItems().stream().collect(Collectors.toList()));
                        clientsService.updateClient(client);
                        popUp.close();
                        refreshGridCliente();
                        refreshGridVehiculo(null);
                        ViewUtils
                                .notification("Cliente creado",
                                        "<b>Cliente:</b> " + client.getNombre().concat(" ").concat(client.getApellido())
                                                + "</br>",
                                        NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE, 1500)
                                .open();
                    } catch (Exception exception) {
                        ViewUtils
                                .notification("Cliente No creado",
                                        "<b>Causa:</b> " + exception.getCause() + "</br>",
                                        NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE, 15000)
                                .open();
                    }

                } else
                    ViewUtils.notification("Errores:",
                            errors + " </br>",
                            NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE, 15000).open();
            }

        });
        // El boton de refrescar es el encargado de decirle al grid que recargue nuevamente los datos que trae de la
        // base de datos
        refreshButton.addClickListener(event -> refreshGridCliente());
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
            Cliente client = clientsGrid.asSingleSelect().getValue();
            if (Objects.nonNull(client)) {
                populateForm(client);
                saveButton.setText(GUARDAR);
                popUpOpened(popUp);
            } else {
                ViewUtils.notification("Seleccionar cliente",
                        "Usted debe seleccionar el cliente y hacer click en el botón de editar si desea editar el cliente."
                                + "</br>",
                        NotificationVariant.LUMO_PRIMARY, Notification.Position.MIDDLE, 15000).open();
            }
        });

        //Botones de vehiculos
        refreshDataButton.addClickListener(event -> {
            Cliente cliente = clientsGrid.asSingleSelect().getValue();
            refreshGridVehiculo(cliente.getDni());
        });

        addDataButton.addClickListener(event -> {
            Cliente cliente = clientsGrid.asSingleSelect().getValue();
            if (cliente != null) {
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
                        NotificationVariant.LUMO_PRIMARY, Notification.Position.MIDDLE, 15000).open();
            }
        });

        editDataButton.addClickListener(event -> {
            crearPopUpVehiculo();
            formPopUpVehiculo("Modificar Vehículo");
            Vehiculo vehiculo = vehiculoGrid.asSingleSelect().getValue();
            Cliente client = clientsGrid.asSingleSelect().getValue();

            if (Objects.nonNull(client)) {

                if (Objects.nonNull(vehiculo)) {
                    populateFormVehiculo(vehiculo);
                    saveData.setText(GUARDAR);
                    popUpOpened(popUpData);
                } else {
                    ViewUtils.notification("Seleccionar vehículo",
                            "Usted debe seleccionar el cliente, luego en el vehiculo y hacer click en el botón de editar si desea editar el vehiculo."
                                    + "</br>",
                            NotificationVariant.LUMO_PRIMARY, Notification.Position.MIDDLE, 15000).open();
                }
            } else {
                ViewUtils.notification("Seleccionar cliente",
                        "Usted debe seleccionar el cliente, luego en el vehiculo y hacer click en el botón de editar si desea editar el vehiculo."
                                + "</br>",
                        NotificationVariant.LUMO_PRIMARY, Notification.Position.MIDDLE, 15000).open();
            }
        });
        saveData.addClickListener(event -> {
            Cliente cliente = clientsGrid.asSingleSelect().getValue();
            if (cliente != null) {
                Cliente c = clientsService.getClientByDni(cliente.getDni());
                Vehiculo v = vehiculoGrid.asSingleSelect().getValue();
                String errors = validarVehiculo(false);
                if (v != null && StringUtils.isEmpty(errors)) { //Guardado en modificar vehiculo
                    v = vehicleService.getVehiculoByDominio(dominio.getValue());
                    v.setMarca(marca.getValue());
                    v.setModelo(modelo.getValue());
                    v.setAño(Objects.nonNull(año.getValue()) ? año.getValue().toString() : null);
                    v.setKilometro(kilometro.getValue());
                    v.setCreatedAt(new Date());
                    v.setUpdatedAt(new Date());
                    vehicleService.updateVehiculo(v);
                    popUpData.close();
                } else {
                    //Guardado en crear vehiculo
                    if (v == null && StringUtils.isEmpty(errors)) {
                        v = new Vehiculo();
                        v.setDominio(dominio.getValue());
                        v.setMarca(marca.getValue());
                        v.setModelo(modelo.getValue());
                        v.setAño(Objects.nonNull(año.getValue()) ? año.getValue().toString() : null);
                        v.setKilometro(kilometro.getValue());
                        v.setCreatedAt(new Date());
                        v.setUpdatedAt(new Date());

                        if (Objects.nonNull(v.getClientes())) {
                            v.getClientes().add(c);
                        } else {
                            List<Cliente> clientes = new ArrayList<>();
                            clientes.add(c);
                            v.setClientes(clientes);
                        }
                        if (Objects.nonNull(c.getVehiculos())) {
                            c.getVehiculos().add(v);
                        } else {
                            List<Vehiculo> vehiculos = new ArrayList<>();
                            vehiculos.add(v);
                            c.setVehiculos(vehiculos);
                        }

                        clientsService.updateClient(c);
                        popUpData.close();
                    } else
                        ViewUtils.notification("Error",
                                errors + " </br>",
                                NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE, 15000).open();

                }

                refreshGridVehiculo(cliente.getDni());

            } else {
                ViewUtils.notification("Seleccionar Cliente",
                        "Debes seleccionar un cliente y luego hacer click en el boton de agregar vehiculo para agregar un nuevo vehiculo."
                                + "</br>",
                        NotificationVariant.LUMO_PRIMARY, Notification.Position.MIDDLE, 15000).open();
            }

        });
        cancelData.addClickListener(event -> {
            Cliente client = clientsGrid.asSingleSelect().getValue();
            refreshGridVehiculo(client.getDni());
            popUpData.close();

        });
    }

    private String validarCliente(boolean isNew) {
        String errors = "";
        if (StringUtils.isBlank(nombre.getValue())) {
            errors = errors + "El nombre no debe ser vacío. " + " </br>";
        }
        if (StringUtils.isBlank(apellido.getValue())) {
            errors = errors + "El apellido no debe ser vacío. " + " </br>";
        }
        if (Objects.isNull(dni.getValue())) {
            errors = errors + "El DNI no debe ser vacío. " + " </br>";
        } else if (isNew && Objects.nonNull(clientsService.getClientByDni(dni.getValue().toString()))) {
            errors = errors + "El DNI ingresado ya pertenece a un cliente. Ingrese un nuevo DNI. " + " </br>";
        }
        if (StringUtils.isBlank(direccion.getValue())) {
            errors = errors + "La dirección no debe ser vacía. " + " </br>";
        }
        if (!ViewUtils.emailValido(email.getValue())) {
            errors = errors + "El email no tiene un formato válido. Ejemplo: gncprogreso@hotmail.com. " + " </br>";
        }
        return errors;
    }

    private String validarVehiculo(boolean isNew) {
        String errors = "";
        if (StringUtils.isBlank(dominio.getValue())) {
            errors = errors + "El dominio no debe ser vacío. " + " </br>";
        } else if (isNew && Objects.nonNull(vehicleService.getVehiculoByDominio(dominio.getValue()))) {
            errors = errors + "El Dominio ingresado ya pertenece a un Vehículo. Ingrese un nuevo Dominio. " + " </br>";
        }
        return errors;
    }

    private void cargarComboBoxVehiculo(Cliente client, List<Vehiculo> vehiculoList) {
        vehiculoList.forEach(v -> {
            List<Cliente> listC = new ArrayList<>();
            if (Objects.nonNull(v.getClientes())) {
                if (!(v.getClientes().contains(client))) {
                    v.getClientes().add(client);
                }
            } else {
                listC.add(client);
                v.setClientes(listC);
            }
            vehicleService.updateVehiculo(v);
        });
        //si al modificar el cliente, elimino todos los vehiculos asociados del ComboBox entonces por cada uno
        // esos vehiculos debo desasociarlo del cliente que estoy modificando, de esta manera actualizo a cada vehiculo
        // con la lista de clientes que le corresponden
        if (vehiculoList.isEmpty()) {
            client.getVehiculos().forEach(v -> {

                if (Objects.nonNull(v.getClientes())) {
                    List<Cliente> clienteList = new ArrayList<>();
                    v.getClientes().forEach(c -> {
                        if (!(c.getDni().equals(client.getDni()))) {
                            clienteList.add(c);

                        }
                    });
                    v.setClientes(clienteList);
                    vehicleService.updateVehiculo(v);
                }

            });

        }
        client.setVehiculos(vehiculoList); //Le actualizo la lista de vehiculos al cliente

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

    /* Metodo encargado de crear la seccion de botones de los PopUp */
    private void seccionBotones() {
        buttonDiv = new HorizontalLayout();
        buttonDiv.setId("button-layout");
        buttonDiv.setSizeFull();
        buttonDiv.add(saveButton, cancelButton);
        buttonDiv.setAlignItems(Alignment.CENTER);
        buttonDiv.setVerticalComponentAlignment(Alignment.CENTER);
    }

    /* Este metodo carga en el PopUp todos los campos del objeto pasado como parametro*/
    private void populateForm(Cliente client) {
        nombre.setValue(client.getNombre());
        nombre.setRequiredIndicatorVisible(true);
        apellido.setValue(client.getApellido());
        apellido.setRequiredIndicatorVisible(true);
        dni.setValue(Integer.valueOf(client.getDni()));
        dni.setRequiredIndicatorVisible(true);
        dni.setEnabled(false);
        direccion.setValue(client.getDireccion());
        telefono.setValue(client.getTelefono());
        email.setValue(client.getEmail());
        vehiculoMultiselectComboBox.updateSelection(new HashSet<>(client.getVehiculos()), Collections.emptySet());
    }

    /* Este metodo carga en el PopUp todos los campos del objeto pasado como parametro*/
    private void populateFormVehiculo(Vehiculo vehiculo) {
        dominio.setValue(vehiculo.getDominio());
        dominio.setRequiredIndicatorVisible(true);
        dominio.setEnabled(false);
        marca.setValue(vehiculo.getMarca());
        modelo.setValue(vehiculo.getModelo());
        kilometro.setValue(vehiculo.getKilometro());
        año.setValue(Objects.nonNull(vehiculo.getAño()) ? Integer.valueOf(vehiculo.getAño()) : null);
    }

    /* Este metodo es el encargado de refrescar los datos de la grilla cada vez que se modifica, se agrega o se borrado
    un dato */
    private void refreshGridCliente() {
        List<Cliente> allClients = clientsService.getAllClients();
        if (allClients != null) {
            dataClienteProvider = new ListDataProvider<>(allClients);
        } else {
            dataClienteProvider = new ListDataProvider<>(new ArrayList<>());
        }
        clientsGrid.setItems(allClients);
        clientsGrid.setDataProvider(dataClienteProvider);
        clientsGrid.getDataProvider().refreshAll();
        clientsGrid.select(null);
        dataClienteProvider.refreshAll();
    }

    private void refreshGridVehiculo(String dni) {
        List<Vehiculo> vehiculos = new ArrayList<>();
        if (dni != null) {
            vehiculos = Objects.nonNull(clientsService.getClientByDni(dni)) ? clientsService.getClientByDni(dni).getVehiculos() : new ArrayList<>();
        }
        if (vehiculos.isEmpty()) {
            vehiculos = new ArrayList<>();
        }
        dataVehiculoProvider = DataProvider.ofCollection(vehiculos);
        dataVehiculoProvider.setSortOrder(Vehiculo::getDominio,
                SortDirection.ASCENDING);
        vehiculoGrid.select(null);
        vehiculoGrid.setItems(vehiculos);
        vehiculos.forEach(vehiculo -> {
            vehiculoGrid.getDataProvider().refreshItem(vehiculo, true);
            vehiculoGrid.getElement().executeJs("this.clearCache()");
        });

        vehiculoGrid.getDataProvider().refreshAll();
        dataVehiculoProvider.refreshAll();
    }

    /* Este metodo es el encargado de crear los filtros en Vehiculos */
    private void cargarFiltroVehiculo() {
        HeaderRow filterRowV = vehiculoGrid.appendHeaderRow();

        TextField dominioFilter = ViewUtils.createNewFilterForColumnGrid();
        dominioFilter.addValueChangeListener(event -> {
                    dataVehiculoProvider = (ListDataProvider<Vehiculo>) vehiculoGrid.getDataProvider();
                    dataVehiculoProvider.addFilter(vehiculo -> StringUtils.containsIgnoreCase(vehiculo.getDominio(), dominioFilter.getValue()));
                }
        );

        TextField marcaFilter = ViewUtils.createNewFilterForColumnGrid();
        marcaFilter.addValueChangeListener(event -> {
                    dataVehiculoProvider = (ListDataProvider<Vehiculo>) vehiculoGrid.getDataProvider();
                    dataVehiculoProvider.addFilter(vehiculo -> StringUtils.containsIgnoreCase(vehiculo.getMarca(), marcaFilter.getValue()));
                }
        );

        TextField modeloFilter = ViewUtils.createNewFilterForColumnGrid();
        modeloFilter.addValueChangeListener(event -> {
                    dataVehiculoProvider = (ListDataProvider<Vehiculo>) vehiculoGrid.getDataProvider();
                    dataVehiculoProvider.addFilter(vehiculo -> StringUtils.containsIgnoreCase(vehiculo.getModelo(), modeloFilter.getValue()));
                }
        );

        ViewUtils.setFilterInColumnGrid(filterRowV, dominioFilter, vehiculoGrid.getColumnByKey("dominio"));
        ViewUtils.setFilterInColumnGrid(filterRowV, marcaFilter, vehiculoGrid.getColumnByKey("marca"));
        ViewUtils.setFilterInColumnGrid(filterRowV, modeloFilter, vehiculoGrid.getColumnByKey("modelo"));

    }


    /* Este metodo es el encargado de crear los filtros en Clientes */
    private void cargarFiltroCliente() {
        HeaderRow filterRow = clientsGrid.appendHeaderRow();

        TextField apellidoFilter = ViewUtils.createNewFilterForColumnGrid();
        apellidoFilter.addValueChangeListener(event -> dataClienteProvider
                .addFilter(client -> StringUtils.containsIgnoreCase(client.getApellido(), apellidoFilter.getValue())));

        TextField dniFilter = ViewUtils.createNewFilterForColumnGrid();
        dniFilter.addValueChangeListener(event -> dataClienteProvider
                .addFilter(client -> StringUtils.containsIgnoreCase(client.getDni(), dniFilter.getValue())));

        ViewUtils.setFilterInColumnGrid(filterRow, apellidoFilter, clientsGrid.getColumnByKey("Apellido"));
        ViewUtils.setFilterInColumnGrid(filterRow, dniFilter, clientsGrid.getColumnByKey("dni"));


    }

}
