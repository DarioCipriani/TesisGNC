package com.gnc.task.application.mvp.views.listvconfig;

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

import javax.annotation.security.RolesAllowed;
import java.util.*;

@PageTitle("Listado de vehículos")
@Route(value = "listado", layout = MainLayout.class)
@RolesAllowed("admin")
public class ListVConfigView extends HorizontalLayout {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    // creacion de iconos de clientes
    private final Icon refreshIcon = new Icon(VaadinIcon.REFRESH);
    private final Icon close = new Icon(VaadinIcon.CLOSE);
    private final Icon showIcon = new Icon(VaadinIcon.EYE);
    // creacion de iconos de vehiculos
    private final Icon refreshData = new Icon(VaadinIcon.REFRESH);
    private final Icon closeData = new Icon(VaadinIcon.CLOSE);
    private final Icon showDataIcon = new Icon(VaadinIcon.EYE);

    // creacion de botones de clientes
    private final Button refreshButton = new Button(refreshIcon);
    private final Button cancel = new Button("Salir", close);
    private final Button showButton = new Button(showIcon);

    // creacion de botones de vehiculos
    private final Button refreshDataButton = new Button(refreshData);
    private final Button cancelData = new Button("Salir", closeData);
    private final Button showDataButton = new Button(showDataIcon);
    //pop up cliente
    private Dialog popUp;
    //pop up vehiculo
    private Dialog popUpData;
    //campos que pertenecen a la fila (grid) de clientes en la ventana de clientes
    private final Grid<Cliente> clientsGrid = new Grid<>();
    private TextField nombre;
    private TextField apellido;
    private IntegerField dni;
    private TextField direccion;
    private MaskedTextField telefono;
    private EmailField email;
    private final Grid<Vehiculo> vehiculoGrid = new Grid<>();
    private TextField dominio;
    private TextField marca;
    private TextField modelo;
    private IntegerField kilometro;
    private IntegerField año;
    private ListDataProvider<Cliente> dataClienteProvider;
    private ListDataProvider<Vehiculo> dataVehiculoProvider;
    private HorizontalLayout buttonDiv;
    private final AuthenticatedUser authenticatedUser;
    private final ClientsService clientsService;
    private final VehicleService vehicleService;
    private final ClientDetailsServiceImpl clientDetailsServiceImpl;

    public ListVConfigView(AuthenticatedUser authenticatedUser, ClientsService clientsService,
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

            createButtonLayoutVehiculo(verticalLayout);
            createButtonLayoutCliente();

            crearGridVehiculo(verticalLayout);
            crearGridCliente();

            agregarSeccionCliente(verticalLayout);

            cargarVehiculoGrid(vehicleService, null);
            cargarClienteGrid();

            cargarFiltroVehiculo();

            agregarFuncionBoton();

        }

    }

    private void agregarSeccionCliente(VerticalLayout verticalLayout) {
        Div wrapper = new Div();
        wrapper.setId("grid-wrapper");
        H4 headline = new H4("Cliente/s:");
        headline.getStyle().set("margin-top", "0");
        wrapper.setSizeFull();
        wrapper.add(headline);
        verticalLayout.add(wrapper);
        Div layout = new Div();
        layout.setId("div-layout");
        layout.setWidthFull();
        layout.setSizeFull();
        layout.setHeight("45%");
        cargarFiltroCliente();
        layout.add(clientsGrid);
        Div buttonsData = new Div();
        buttonsData.setId("div-buttons");
        buttonsData.setWidthFull();
        buttonsData.setHeight("15%");
        buttonsData.add(refreshButton,showButton);
        wrapper.add(buttonsData, layout);
        verticalLayout.add(wrapper);
        add(verticalLayout);
    }

    private void cargarClienteGrid() {
        //Cada vez que se selecciona un elemento de la grilla si el evento no tiene valor (por ejemplo el Crear
        // Vehiculo) entonces seteamos el boton de guardar con el texto "Nuevo Vehiculo", mientras que si el evento
        // tiene valor (por ejemplo el Modificar Vehiculo) entonces seteamos el boton con el texto "Guardar"
        clientsGrid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() == null) {
                clientsGrid.getDataProvider().refreshAll();
            }
        });
        ViewUtils.gridReziseColumns(clientsGrid);
    }

    private void cargarVehiculoGrid(VehicleService vehicleService, String dominio) {
        dataVehiculoProvider = new ListDataProvider<>(vehicleService.getAllVehiculos());
        vehiculoGrid.setDataProvider(dataVehiculoProvider);
        vehiculoGrid.getDataProvider().refreshAll();
        vehiculoGrid.select(null);
        dataVehiculoProvider.refreshAll();
        vehiculoGrid.asSingleSelect().addValueChangeListener(event -> {
            refreshGridCliente(Objects.nonNull(event.getValue()) ? event.getValue().getDominio() : dominio);
        });
        ViewUtils.gridReziseColumns(vehiculoGrid);
        ViewUtils.gridReziseColumns(clientsGrid);

    }

    private void crearGridCliente() {
        // creacion de la fila (Grid) con el encabezado con cada uno de los campos de Cliente
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
    }

    private void crearGridVehiculo(VerticalLayout verticalLayout) {
        // creacion de la fila (Grid) con el encabezado con cada uno de los campos de Cliente
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
        verticalLayout.add(vehiculoGrid);
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
        dominio.setRequiredIndicatorVisible(true);
        marca = ViewUtils.newBasicConfigTextField("Marca");
        modelo = ViewUtils.newBasicConfigTextField("Modelo");
        kilometro = ViewUtils.newBasicConfigIntegerField("Kilometraje");
        año = ViewUtils.newBasicConfigIntegerField("Año");
        editorDiv.add(headlineV, dominio, marca, modelo, kilometro, año);
        seccionBotones(cancelData);
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
        nombre.setRequiredIndicatorVisible(true);
        apellido = ViewUtils.newBasicConfigTextField("Apellido");
        apellido.setRequiredIndicatorVisible(true);
        dni = ViewUtils.newBasicConfigIntegerField("DNI");
        dni.setRequiredIndicatorVisible(true);
        direccion = ViewUtils.newBasicConfigTextField("Dirección");
        telefono = ViewUtils.newBasicConfigMaskedTextField("Teléfono", "+{54}-000000000000");
        email = ViewUtils.newBasicConfigEmailField("Email");
        formPopUp.add(headline, nombre, apellido, dni, direccion, telefono, email);
        seccionBotones(cancel);
        formPopUp.add(buttonDiv);
        popUp.add(formPopUp);
    }

    /*Metodo encargado de crear la seccion de botones de los PopUp*/
    private void seccionBotones(Button cancel) {
        buttonDiv = new HorizontalLayout();
        buttonDiv.setId("button-layout");
        buttonDiv.setSizeFull();

        buttonDiv.add(cancel);
        buttonDiv.setAlignItems(Alignment.CENTER);
        buttonDiv.setVerticalComponentAlignment(Alignment.CENTER);
    }

    private void createButtonLayoutCliente() {
        closeData.setColor("#BE2123");
        ViewUtils.setButtonAutoMarginAndVariant(cancelData, ButtonVariant.LUMO_ERROR);
        //botones de clientes
        refreshButton.getElement().setProperty("title", "Recargar Clientes");
        showButton.getElement().setProperty("title", "Mostrar Clientes");

    }

    private void createButtonLayoutVehiculo(VerticalLayout verticalLayout) {
        close.setColor("#BE2123");
        ViewUtils.setButtonAutoMarginAndVariant(cancel, ButtonVariant.LUMO_ERROR);
        //botones de vehiculos
        refreshDataButton.getElement().setProperty("title", "Recargar Vehículos");
        showDataButton.getElement().setProperty("title", "Mostrar Vehículos");
        Div buttons = new Div();
        buttons.setId("div-buttons");
        buttons.setHeight("5%");
        buttons.add(refreshDataButton,showDataButton);
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
        cancel.addClickListener(e -> {
            Vehiculo vehiculo = vehiculoGrid.asSingleSelect().getValue();
            refreshGridCliente(vehiculo.getDominio());
            popUp.close();
        });

        // El boton de refrescar es el encargado de decirle al grid que recargue nuevamente los datos que trae de la
        // base de datos
        refreshButton.addClickListener(event -> {
            Vehiculo v = vehiculoGrid.asSingleSelect().getValue();
            refreshGridCliente(v.getDominio());
        });
        // Boton de mostrar
        showButton.addClickListener(event -> {
            crearPopUpCliente();
            formPopUpCliente("Ver detalles del Cliente");
            Cliente client = clientsGrid.asSingleSelect().getValue();
            if (Objects.nonNull(client)) {
                populateForm(client);
                popUpOpened(popUp);
            } else {
                ViewUtils.notification("Seleccionar cliente",
                        "Usted debe seleccionar el cliente y hacer click en el botón de mostrar si desea mostrar el cliente."
                                + "</br>",
                        NotificationVariant.LUMO_PRIMARY, Notification.Position.MIDDLE, 15000).open();
            }

        });

        //Botones de vehiculos
        refreshDataButton.addClickListener(event -> {
            Vehiculo v = vehiculoGrid.asSingleSelect().getValue();
            refreshGridCliente(v.getDominio());
        });

        showDataButton.addClickListener(event -> {
            crearPopUpVehiculo();
            formPopUpVehiculo("Ver detalles del Vehículo");
            Vehiculo vehiculo = vehiculoGrid.asSingleSelect().getValue();
                if (Objects.nonNull(vehiculo)) {
                    populateFormVehiculo(vehiculo);
                    popUpOpened(popUpData);
                } else {
                    ViewUtils.notification("Seleccionar vehículo",
                            "Usted debe seleccionar el cliente, luego en el vehiculo y hacer click en el botón de mostrar si desea mostrar el vehiculo."
                                    + "</br>",
                            NotificationVariant.LUMO_PRIMARY, Notification.Position.MIDDLE, 15000).open();
                }

        });
        cancelData.addClickListener(event -> {
            Vehiculo vehiculo = vehiculoGrid.asSingleSelect().getValue();
            refreshGridCliente(vehiculo.getDominio());
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
        buttonDiv.add(showButton);
        buttonDiv.setAlignItems(Alignment.CENTER);
        buttonDiv.setVerticalComponentAlignment(Alignment.CENTER);
    }

    /* Este metodo carga en el PopUp todos los campos del objeto pasado como parametro*/
    private void populateForm(Cliente client) {
        nombre.setValue(client.getNombre());
        nombre.setEnabled(false);
        nombre.setRequiredIndicatorVisible(true);
        apellido.setValue(client.getApellido());
        apellido.setRequiredIndicatorVisible(true);
        apellido.setEnabled(false);
        dni.setValue(Integer.valueOf(client.getDni()));
        dni.setRequiredIndicatorVisible(true);
        dni.setEnabled(false);
        direccion.setValue(client.getDireccion());
        direccion.setEnabled(false);
        telefono.setValue(client.getTelefono());
        telefono.setEnabled(false);
        email.setValue(client.getEmail());
        email.setEnabled(false);
    }

    private void populateFormVehiculo(Vehiculo vehiculo) {
        dominio.setValue(vehiculo.getDominio());
        dominio.setRequiredIndicatorVisible(true);
        dominio.setEnabled(false);
        marca.setValue(vehiculo.getMarca());
        marca.setEnabled(false);
        modelo.setValue(vehiculo.getModelo());
        modelo.setEnabled(false);
        kilometro.setValue(vehiculo.getKilometro());
        kilometro.setEnabled(false);
        año.setValue(Objects.nonNull(vehiculo.getAño())?Integer.valueOf(vehiculo.getAño()):null);
        año.setEnabled(false);
    }

    /* Este metodo es el encargado de refrescar los datos de la grilla cada vez que se modifica, se agrega o se borrado
    un dato*/
    private void refreshGridVehiculo() {
        List<Vehiculo> allVehiculos = vehicleService.getAllVehiculos();
        if (allVehiculos != null) {
            dataVehiculoProvider = new ListDataProvider<>(allVehiculos);
        } else {
            dataVehiculoProvider = new ListDataProvider<>(new ArrayList<>());
        }
        vehiculoGrid.setItems(allVehiculos);
        vehiculoGrid.setDataProvider(dataVehiculoProvider);
        vehiculoGrid.getDataProvider().refreshAll();
        vehiculoGrid.select(null);
        dataVehiculoProvider.refreshAll();
    }

    private void refreshGridCliente(String dominio) {
        List<Cliente> clientes = new ArrayList<>();
        if (dominio != null) {
            clientes = Objects.nonNull(vehicleService.getAllClientsByDominio(dominio)) ? vehicleService.getAllClientsByDominio(dominio) : new ArrayList<>();
        }
        if (clientes.isEmpty()) {
            clientes = new ArrayList<>();
        }
        dataClienteProvider = DataProvider.ofCollection(clientes);
        dataClienteProvider.setSortOrder(Cliente::getDni,
                SortDirection.ASCENDING);
        clientsGrid.select(null);
        clientsGrid.setItems(clientes);
        clientes.forEach(cliente -> {
            clientsGrid.getDataProvider().refreshItem(cliente, true);
            clientsGrid.getElement().executeJs("this.clearCache()");
        });

        clientsGrid.getDataProvider().refreshAll();
        dataClienteProvider.refreshAll();
    }

    /* Este metodo es el encargado de crear los filtros en Vehiculos*/
    private void cargarFiltroVehiculo() {
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

    /* Este metodo es el encargado de crear los filtros en Clientes*/
    /* Este metodo es el encargado de crear los filtros en Clientes*/
    private void cargarFiltroCliente() {
        HeaderRow filterRow = clientsGrid.appendHeaderRow();

        TextField apellidoFilter = ViewUtils.createNewFilterForColumnGrid();
        apellidoFilter.addValueChangeListener(event -> {
                    dataClienteProvider = (ListDataProvider<Cliente>) clientsGrid.getDataProvider();
                    dataClienteProvider.addFilter(cliente -> StringUtils.containsIgnoreCase(cliente.getApellido(), apellidoFilter.getValue()));
                }
        );
        TextField dniFilter = ViewUtils.createNewFilterForColumnGrid();
        dniFilter.addValueChangeListener(event -> {
                    dataClienteProvider = (ListDataProvider<Cliente>) clientsGrid.getDataProvider();
                    dataClienteProvider.addFilter(cliente -> StringUtils.containsIgnoreCase(cliente.getDni(), dniFilter.getValue()));
                }
        );

        ViewUtils.setFilterInColumnGrid(filterRow, apellidoFilter, clientsGrid.getColumnByKey("Apellido"));
        ViewUtils.setFilterInColumnGrid(filterRow, dniFilter, clientsGrid.getColumnByKey("dni"));



    }

}
