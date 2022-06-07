package com.gnc.task.application.mvp.views.vehicleconfig;

import com.gnc.task.application.mvp.model.entity.Role;
import com.gnc.task.application.mvp.model.entity.User;
import com.gnc.task.application.mvp.model.entity.Vehiculo;
import com.gnc.task.application.mvp.presenter.VehicleService;
import com.gnc.task.application.security.AuthenticatedUser;
import com.gnc.task.application.utilities.ViewUtils;
import com.gnc.task.application.mvp.views.MainLayout;
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
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@PageTitle("Vehiculo")
@Route(value = "vehiculos", layout = MainLayout.class)
@RolesAllowed("admin")
public class VehicleConfigView extends HorizontalLayout {

    public static final String NEW_VEHICULO = "Nuevo Vehículo";
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
    // creacion de iconos para la ventana de creacion de Vehiculo
    private final Icon check = new Icon(VaadinIcon.CHECK);
    private final Icon close = new Icon(VaadinIcon.CLOSE);
    // creacion de botones para la ventana de creacion de Vehiculo
    private final Button saveButton = new Button(GUARDAR, check);
    private final Button cancelButton = new Button("Cancelar", close);
    //campos que pertenecen a la fila (grid) de vehiculos en la ventana de vehiculos
    private Grid<Vehiculo> vehiculoGrid;
    private TextField dominio;
    private TextField marca;
    private TextField modelo;
    private IntegerField Kilometro;
    private TextField año;
    // Binder es el encargado de enlazar los campos de front con los campos de la base de datos
    private Binder<Vehiculo> vehiculoBinder;
    private ListDataProvider<Vehiculo> dataProvider;

    private Dialog popUp;
    private FormLayout editorDiv;
    private HorizontalLayout buttonDiv;
    private AuthenticatedUser authenticatedUser;
    private VehicleService vehicleService;

    public VehicleConfigView(AuthenticatedUser authenticatedUser, VehicleService vehicleService) {
        this.authenticatedUser = authenticatedUser;
        this.vehicleService = vehicleService;
        Optional<User> userLogged = authenticatedUser.get();
        if (userLogged.isPresent() && userLogged.get().getRoles().contains(Role.ADMIN)) {
            setSizeFull();
            crearPopUp();
            editor("");
            seccionBotones();
            editorDiv.add(buttonDiv);
            popUp.add(editorDiv);
            // creacion de la fila (Grid) con el encabezado con cada uno de los campos de producto
            vehiculoGrid = new Grid<>();
            vehiculoGrid.setSizeFull();
            vehiculoGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
            vehiculoGrid.addThemeName("grid-selection-theme");
            vehiculoGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.MATERIAL_COLUMN_DIVIDERS);
            vehiculoGrid.addColumn(Vehiculo::getDominio).setHeader("Dominio").setResizable(true).setKey("dominio")
                    .setSortable(true);
            vehiculoGrid.addColumn(Vehiculo::getMarca).setHeader("Marca").setResizable(true).setKey("marca")
                    .setSortable(true);
            vehiculoGrid.addColumn(Vehiculo::getModelo).setHeader("Modelo").setResizable(true).setKey("modelo").setSortable(true);
            vehiculoGrid.addColumn(Vehiculo::getKilometro).setHeader("Kilómetros").setResizable(true).setKey("kilometro")
                    .setSortable(true);
            vehiculoGrid.addColumn(Vehiculo::getAño).setHeader("Año").setResizable(true).setKey("año")
                    .setSortable(true);

            vehiculoBinder = new Binder<>(Vehiculo.class);
            vehiculoBinder.bind(dominio, Vehiculo::getDominio, Vehiculo::setDominio);
            vehiculoBinder.bind(marca, Vehiculo::getMarca, Vehiculo::setMarca);
            vehiculoBinder.bind(modelo, Vehiculo::getModelo, Vehiculo::setModelo);
            vehiculoBinder.bind(Kilometro, Vehiculo::getKilometro, Vehiculo::setKilometro);
            vehiculoBinder.bind(año, Vehiculo::getAño, Vehiculo::setAño);

            vehiculoBinder.bindInstanceFields(this);

            dataProvider = new ListDataProvider<>(vehicleService.getAllVehiculos());
            vehiculoGrid.setDataProvider(dataProvider);

            addFiltersToGrid();
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
        wrapper.add(vehiculoGrid);
    }

    /*Metodo encargado de decir a cada boton que es lo que tiene que hacer*/
    private void addButtonListeners() {
        cancelButton.addClickListener(e -> {
            refreshGrid();
            popUp.close();
        });

        deleteButton.addClickListener(e -> {
            Vehiculo vehiculo = vehiculoGrid.asSingleSelect().getValue();
            if (Objects.isNull(vehiculo)) {
                ViewUtils.notification("Seleccionar vehículo",
                        "Usted debe seleccionar el vehículo y hacer click en el botón de eliminar si desea eliminar el vehículo."
                                + "</br>",
                        NotificationVariant.LUMO_PRIMARY, Notification.Position.MIDDLE,15).open();
            } else {
                String detalle = Objects.nonNull(vehiculo)
                        ? vehiculo.getDominio().concat(" - ").concat(vehiculo.getMarca()).concat(" - ").concat(vehiculo.getModelo())
                        : "";
                ConfirmDialog dialog = new ConfirmDialog("Confirmar Borrado",
                        "Está seguro de eliminar el registro seleccionado? Vehículo: " + detalle, "Borrar",
                        confirmEvent -> {
                            if (Objects.nonNull(vehiculo) && vehicleService.deleteVehiculoByVehiculoID(vehiculo.getId())) {
                                ViewUtils.notification("Borrado exitoso", "<b>Vehículo " + detalle + "</br>",
                                        NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE,5).open();
                                refreshGrid();
                                saveButton.setText(NEW_VEHICULO);
                            } else { // no es posible borrar el vehiculo
                                ViewUtils
                                        .notification("Vehículo no habilitado para la eliminación", "<b>Vehículo:</b> "
                                                + (Objects.nonNull(vehiculo) ? detalle : "<i>No está seleccionado</i>")
                                                + "</br>", NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE,15)
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

            Vehiculo vehiculo = vehiculoGrid.asSingleSelect().getValue();
            if (GUARDAR.equals(saveButton.getText())) {
                vehiculo = vehicleService.getVehiculoByDominio(vehiculo.getDominio());
                if (Objects.nonNull(vehiculo)) {
                    vehiculo.setDominio(dominio.getValue());
                    vehiculo.setMarca(marca.getValue());
                    vehiculo.setModelo(modelo.getValue());
                    vehiculo.setKilometro(Kilometro.getValue());
                    vehiculo.setAño(año.getValue());
                    vehiculo.setUpdatedAt(new Date());
                    try {
                        vehicleService.updateVehiculo(vehiculo);
                        popUp.close();
                        ViewUtils
                                .notification("Vehículo Actualizado",
                                        "<b>Vehículo:</b> " + vehiculo.getDominio().concat(" - ").concat(vehiculo.getMarca()).concat(" - ").concat(vehiculo.getModelo())
                                                + "</br>",
                                        NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE,5)
                                .open();
                        refreshGrid();
                    } catch (Exception exception) {
                        ViewUtils
                                .notification("Vehículo No Actualizado",
                                        "<b>Causa:</b> " + exception.getCause() + "</br>",
                                        NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE,15)
                                .open();
                    }


                } else {
                    LOGGER.error("El Vehículo no existe o hay un problema para obtener sus datos");
                    ViewUtils.notification("Algo esta mal",
                            "El vehiculo no puede ser cargado, intente refrescando la pagina y volviendo a realizar los cambios, si el error continúa, consulte con el administrador. </br>",
                            NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE,15).open();
                }

            } else {
                if (NEW_VEHICULO.equals(saveButton.getText())) {
                    vehiculo = new Vehiculo();
                    vehiculo.setDominio(dominio.getValue());
                    vehiculo.setMarca(marca.getValue());
                    vehiculo.setModelo(modelo.getValue());
                    vehiculo.setKilometro(Kilometro.getValue());
                    vehiculo.setAño(año.getValue());
                    vehiculo.setUpdatedAt(new Date());
                    vehiculo.setCreatedAt(new Date());
                    try {
                        vehicleService.updateVehiculo(vehiculo);
                        popUp.close();
                        refreshGrid();
                        ViewUtils
                                .notification("Vehículo creado",
                                        "<b>Vehículo:</b> " + vehiculo.getDominio().concat(" - ").concat(" - ").concat(vehiculo.getMarca()).concat(" - ").concat(vehiculo.getModelo())
                                                + "</br>",
                                        NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE,5)
                                .open();
                    } catch (Exception exception) {
                        ViewUtils
                                .notification("Vehículo No creado",
                                        "<b>Causa:</b> " + exception.getCause() + "</br>",
                                        NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE,15)
                                .open();
                    }


                } else
                    ViewUtils.notification("Algo esta mal",
                            "El vehiculo no puede ser cargado, intente refrescando la página y volviendo a realizar los cambios, si el error continúa, consulte con el administrador. </br>",
                            NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE,15).open();
            }

        });
        // El boton de refrescar es el encargado de decirle al grid que recargue nuevamente los datos que trae de la
        // base de datos
        refreshButton.addClickListener(event -> refreshGrid());
        //Es lo que hace el boton +
        addButton.addClickListener(event -> {
            crearPopUp();
            editor("Crear Vehículo");
            seccionBotones();
            editorDiv.add(buttonDiv);
            popUp.add(editorDiv);
            vehiculoGrid.asSingleSelect().clear();
            clearForm();
            saveButton.setText(NEW_VEHICULO);
            popUpOpened();
        });

        // Boton de editar
        editButton.addClickListener(event -> {
            crearPopUp();
            editor("Modificar Vehículo");
            seccionBotones();
            editorDiv.add(buttonDiv);
            popUp.add(editorDiv);
            Vehiculo vehiculo = vehiculoGrid.asSingleSelect().getValue();
            if (Objects.nonNull(vehiculo)) {
                populateForm(vehiculo);
                saveButton.setText(GUARDAR);
                popUpOpened();
            } else {
                ViewUtils.notification("Seleccionar Vehículo",
                        "Usted debe seleccionar el vehículo y hacer click en el botón de editar si desea editar el vehículo."
                                + "</br>",
                        NotificationVariant.LUMO_PRIMARY, Notification.Position.MIDDLE,15).open();
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
        dominio = ViewUtils.newBasicConfigTextFieldUppercase("Dominio");
        marca = ViewUtils.newBasicConfigTextField("Marca");
        modelo = ViewUtils.newBasicConfigTextField("Modelo");
        Kilometro = ViewUtils.newBasicConfigIntegerField("Kilómetro");
        año = ViewUtils.newBasicConfigTextField("Año");

        //Creacion del titulo del PopUp
        H2 headline = new H2(titulo);
        headline.getStyle().set("margin-top", "0");
        editorDiv.add(headline, dominio, marca, modelo, Kilometro, año);
    }

    /* Seteo del titulo a lo que se refiere cada boton al acercar el mouse al boton*/
    private void createButtonLayout(VerticalLayout layout) {
        close.setColor("#BE2123");
        ViewUtils.setButtonAutoMarginAndVariant(saveButton, ButtonVariant.LUMO_TERTIARY);
        ViewUtils.setButtonAutoMarginAndVariant(cancelButton, ButtonVariant.LUMO_ERROR);
        refreshButton.getElement().setProperty("title", "Actualizar Vehículo");
        addButton.getElement().setProperty("title", "Agregar Nuevo Vehículo");
        editButton.getElement().setProperty("title", "Editar Vehículo");
        deleteButton.getElement().setProperty("title", "Borrar Vehículo");
        ViewUtils.buttonConfig(layout, addButton, deleteButton, refreshButton, editButton);
    }

    /* Este metodo carga en el PopUp todos los campos del objeto pasado como parametro*/
    private void populateForm(Vehiculo vehiculo) {
        vehiculoBinder.readBean(vehiculo);
        dominio.setValue(vehiculo.getDominio());
        marca.setValue(vehiculo.getMarca());
        modelo.setValue(vehiculo.getModelo());
        Kilometro.setValue(vehiculo.getKilometro());
        año.setValue(vehiculo.getAño());
    }

    /* Abre el PopUp y le setea algunas propiedades */
    private void popUpOpened() {
        popUp.setCloseOnEsc(true);
        popUp.setCloseOnOutsideClick(false);
        popUp.open();
    }

    /* Este metodo se encarga de limpiar los campos del PopUp, por ejemplo el PopUp Crear Producto*/
    private void clearForm() {
        dominio.clear();
        marca.clear();
        modelo.clear();
        Kilometro.clear();
        año.clear();
    }

    /* Este metodo es el encargado de refrescar los datos de la grilla cada vez que se modifica, se agrega o se borra
    un dato*/
    private void refreshGrid() {
        dataProvider = new ListDataProvider<>(vehicleService.getAllVehiculos());
        vehiculoGrid.setItems(dataProvider);
        vehiculoGrid.getDataProvider().refreshAll();
        vehiculoGrid.select(null);
        dataProvider.refreshAll();
    }

    /* Este metodo es el encargado de crear los filtros en la grilla de Vehiculo*/
    private void addFiltersToGrid() {
        HeaderRow filterRow = vehiculoGrid.appendHeaderRow();

        TextField dominioFilter = ViewUtils.createNewFilterForColumnGrid();
        dominioFilter.addValueChangeListener(event -> dataProvider
                .addFilter(vehiculo -> StringUtils.containsIgnoreCase(vehiculo.getDominio(), dominioFilter.getValue())));

        TextField marcaFilter = ViewUtils.createNewFilterForColumnGrid();
        marcaFilter.addValueChangeListener(event -> dataProvider
                .addFilter(vehiculo -> StringUtils.containsIgnoreCase(vehiculo.getMarca(), marcaFilter.getValue())));

        TextField modeloFilter = ViewUtils.createNewFilterForColumnGrid();
        modeloFilter.addValueChangeListener(event -> dataProvider
                .addFilter(vehiculo -> StringUtils.containsIgnoreCase(vehiculo.getModelo(), modeloFilter.getValue())));

        ViewUtils.setFilterInColumnGrid(filterRow, dominioFilter, vehiculoGrid.getColumnByKey("dominio"));
        ViewUtils.setFilterInColumnGrid(filterRow, marcaFilter, vehiculoGrid.getColumnByKey("marca"));
        ViewUtils.setFilterInColumnGrid(filterRow, modeloFilter, vehiculoGrid.getColumnByKey("modelo"));

    }

}
