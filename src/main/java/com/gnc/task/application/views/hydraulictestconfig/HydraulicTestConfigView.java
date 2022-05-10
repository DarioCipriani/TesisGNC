package com.gnc.task.application.views.hydraulictestconfig;

import com.gnc.task.application.data.Role;
import com.gnc.task.application.data.entity.PruebaHidraulica;
import com.gnc.task.application.data.entity.User;
import com.gnc.task.application.data.service.HydraulicTestService;
import com.gnc.task.application.security.AuthenticatedUser;
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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@PageTitle("PruebaHidraulica")
@Route(value = "pruebasHidraulicas", layout = MainLayout.class)
@RolesAllowed("admin")
public class HydraulicTestConfigView extends HorizontalLayout {

    public static final String NEW_PRUEBAHIDRAULICA = "Nueva Prueba Hidráulica";
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
    // creacion de iconos para la ventana de creacion de Prueba Hidraulica
    private final Icon check = new Icon(VaadinIcon.CHECK);
    private final Icon close = new Icon(VaadinIcon.CLOSE);
    // creacion de botones para la ventana de creacion de Prueba Hidraulica
    private final Button saveButton = new Button(GUARDAR, check);
    private final Button cancelButton = new Button("Cancelar", close);
    //campos que pertenecen a la fila (grid) de pruebas hidraulicas en la ventana de prueba hidraulica
    private Grid<PruebaHidraulica> pruebaHidraulicaGrid;
    private TextField numeroCertificado;
    private TextField pasoPrueba;
    private TextField descripcion;
    private TextField fechaVencimientoPH;
    // Binder es el encargado de enlazar los campos de front con los campos de la base de datos
    private Binder<PruebaHidraulica> pruebaHidraulicaBinder;
    private ListDataProvider<PruebaHidraulica> dataProvider;
    private Dialog popUp;
    private FormLayout editorDiv;
    private HorizontalLayout buttonDiv;
    private AuthenticatedUser authenticatedUser;
    private HydraulicTestService hydraulicTestService;

    public HydraulicTestConfigView(AuthenticatedUser authenticatedUser, HydraulicTestService hydraulicTestService) {
        this.authenticatedUser = authenticatedUser;
        this.hydraulicTestService = hydraulicTestService;
        Optional<User> userLogged = authenticatedUser.get();
        if (userLogged.isPresent() && userLogged.get().getRoles().contains(Role.ADMIN)) {
            setSizeFull();
            crearPopUp();
            editor("");
            seccionBotones();
            editorDiv.add(buttonDiv);
            popUp.add(editorDiv);
            // creacion de la fila (Grid) con el encabezado con cada uno de los campos de Prueba Hidraulica
            pruebaHidraulicaGrid = new Grid<>();
            pruebaHidraulicaGrid.setSizeFull();
            pruebaHidraulicaGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
            pruebaHidraulicaGrid.addThemeName("grid-selection-theme");
            pruebaHidraulicaGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.MATERIAL_COLUMN_DIVIDERS);
            pruebaHidraulicaGrid.addColumn(PruebaHidraulica::getNumeroCertificado).setHeader("Número de Certificado").setResizable(true).setKey("numeroCerti")
                    .setSortable(true);
            pruebaHidraulicaGrid.addColumn(PruebaHidraulica::getPasoPrueba).setHeader("Pasó Prueba Hidráulica").setResizable(true).setKey("pasoPH")
                    .setSortable(true);
            pruebaHidraulicaGrid.addColumn(PruebaHidraulica::getDescripcion).setHeader("Descripción").setResizable(true).setKey("descripcion").setSortable(true);
            pruebaHidraulicaGrid.addColumn(PruebaHidraulica::getFechaVencimientoPH).setHeader("Fecha de Vencimiento de PH").setResizable(true).setKey("vtoPH")
                    .setSortable(true);

            pruebaHidraulicaBinder = new Binder<>(PruebaHidraulica.class);
            pruebaHidraulicaBinder.bind(numeroCertificado, PruebaHidraulica::getNumeroCertificado, PruebaHidraulica::setNumeroCertificado);
            pruebaHidraulicaBinder.bind(pasoPrueba, PruebaHidraulica::getPasoPrueba, PruebaHidraulica::setPasoPrueba);
            pruebaHidraulicaBinder.bind(descripcion, PruebaHidraulica::getDescripcion, PruebaHidraulica::setDescripcion);
            pruebaHidraulicaBinder.bind(fechaVencimientoPH, PruebaHidraulica::getFechaVencimientoPH, PruebaHidraulica::setFechaVencimientoPH);

            pruebaHidraulicaBinder.bindInstanceFields(this);

            dataProvider = new ListDataProvider<>(hydraulicTestService.getAllHydraulicTesting());
            pruebaHidraulicaGrid.setDataProvider(dataProvider);

            addFiltersToGrid();
            //Cada vez que se selecciona un elemento de la grilla si el evento no tiene valor (por ejemplo el Crear
            // Prueba Hidraulica) entonces seteamos el boton de guardar con el texto "Nueva Prueba Hidraulica", mientras que si el evento
            // tiene valor (por ejemplo el Modificar Prueba Hidraulica) entonces seteamos el boton con el texto "Guardar"
            pruebaHidraulicaGrid.asSingleSelect().addValueChangeListener(event -> {
                if (event.getValue() != null) {
                    saveButton.setText(GUARDAR);
                } else {
                    pruebaHidraulicaGrid.getDataProvider().refreshAll();
                    saveButton.setText(NEW_PRUEBAHIDRAULICA);
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
        wrapper.add(pruebaHidraulicaGrid);
    }

    /*Metodo encargado de decir a cada boton que es lo que tiene que hacer*/
    private void addButtonListeners() {
        cancelButton.addClickListener(e -> {
            refreshGrid();
            popUp.close();
        });

        deleteButton.addClickListener(e -> {
            PruebaHidraulica ph = pruebaHidraulicaGrid.asSingleSelect().getValue();
            if (Objects.isNull(ph)) {
                ViewUtils.notification("Seleccionar Prueba Hidráulica",
                        "Usted debe seleccionar la Prueba Hidráulica y hacer click en el botón de eliminar si desea eliminar la Prueba Hidráulica."
                                + "</br>",
                        NotificationVariant.LUMO_PRIMARY, Notification.Position.MIDDLE).open();
            } else {
                String detalle = Objects.nonNull(ph)
                        ? ph.getNumeroCertificado(): "";
                ConfirmDialog dialog = new ConfirmDialog("Confirmar Borrado",
                        "Está seguro de eliminar el registro seleccionado? Prueba Hidráulica: " + detalle, "Borrar",
                        confirmEvent -> {
                            if (Objects.nonNull(ph) && hydraulicTestService.deleteHydraulicTestByHydraulicTestID(ph.getId())) {
                                ViewUtils.notification("Borrado exitoso", "<b>Prueba Hidráulica:</b> " + detalle + "</br>",
                                        NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE).open();
                                refreshGrid();
                                saveButton.setText(NEW_PRUEBAHIDRAULICA);
                            } else { // no es posible borrar la Prueba Hidraulica
                                ViewUtils
                                        .notification("Prueba Hidráulica no habilitada para la eliminación", "<b>Producto:</b> "
                                                + (Objects.nonNull(ph) ? detalle : "<i>No está seleccionado</i>")
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

            PruebaHidraulica ph = pruebaHidraulicaGrid.asSingleSelect().getValue();
            if (GUARDAR.equals(saveButton.getText())) {
                ph = hydraulicTestService.getHydraulicTestByNumeroCertificado(ph.getNumeroCertificado());
                if (Objects.nonNull(ph)) {
                    ph.setNumeroCertificado(numeroCertificado.getValue());
                    ph.setPasoPrueba(pasoPrueba.getValue());
                    ph.setDescripcion(descripcion.getValue());
                    ph.setFechaVencimientoPH(fechaVencimientoPH.getValue());
                    ph.setUpdatedAt(new Date());
                    try {
                        hydraulicTestService.updateHydraulicTest(ph);
                        popUp.close();
                        ViewUtils
                                .notification("Prueba Hidráulica Actualizada",
                                        "<b>Prueba Hidráulica:</b> " + ph.getNumeroCertificado() + "</br>",
                                        NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE)
                                .open();
                        refreshGrid();

                    } catch (Exception exception) {
                        ViewUtils
                                .notification("Prueba Hidráulica No Actualizado",
                                        "<b>Causa:</b> " + exception.getCause() + "</br>",
                                        NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE)
                                .open();
                    }

                } else {
                    LOGGER.error("La Prueba Hidráulica no existe o hay un problema para obtener sus datos");
                    ViewUtils.notification("Algo esta mal",
                            "La Prueba Hidráulica no puede ser cargado, intente refrescando la página y volviendo a realizar los cambios, si el error continúa, consulte con el administrador. </br>",
                            NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE).open();
                }

            } else {
                if (NEW_PRUEBAHIDRAULICA.equals(saveButton.getText())) {
                    ph = new PruebaHidraulica();
                    ph.setNumeroCertificado(numeroCertificado.getValue());
                    ph.setPasoPrueba(pasoPrueba.getValue());
                    ph.setDescripcion(descripcion.getValue());
                    ph.setFechaVencimientoPH(fechaVencimientoPH.getValue());
                    ph.setCreatedAt(new Date());
                    ph.setUpdatedAt(new Date());
                    try {
                        hydraulicTestService.updateHydraulicTest(ph);
                        popUp.close();
                        refreshGrid();
                        ViewUtils
                                .notification("Prueba Hidráulica creada",
                                        "<b>Prueba Hidráulica:</b> " + ph.getNumeroCertificado() + "</br>",
                                        NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE)
                                .open();
                    } catch (Exception exception) {
                        ViewUtils
                                .notification("Prueba Hidráulica No creada",
                                        "<b>Causa:</b> " + exception.getCause() + "</br>",
                                        NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE)
                                .open();
                    }
                } else
                    ViewUtils.notification("Algo esta mal",
                            "La Prueba Hidráulica no puede ser cargado, intente refrescando la pagina y volviendo a realizar los cambios, si el error continúa, consulte con el administrador. </br>",
                            NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE).open();
            }

        });
        // El boton de refrescar es el encargado de decirle al grid que recargue nuevamente los datos que trae de la
        // base de datos
        refreshButton.addClickListener(event -> refreshGrid());
        //Es lo que hace el boton +
        addButton.addClickListener(event -> {
            crearPopUp();
            editor("Crear Prueba Hidráulica");
            seccionBotones();
            editorDiv.add(buttonDiv);
            popUp.add(editorDiv);
            pruebaHidraulicaGrid.asSingleSelect().clear();
            clearForm();
            saveButton.setText(NEW_PRUEBAHIDRAULICA);
            popUpOpened();
        });

        // Boton de editar
        editButton.addClickListener(event -> {
            crearPopUp();
            editor("Modificar Prueba Hidráulica");
            seccionBotones();
            editorDiv.add(buttonDiv);
            popUp.add(editorDiv);
            PruebaHidraulica ph = pruebaHidraulicaGrid.asSingleSelect().getValue();
            if (Objects.nonNull(ph)) {
                populateForm(ph);
                saveButton.setText(GUARDAR);
                popUpOpened();
            } else {
                ViewUtils.notification("Seleccionar Prueba Hidráulica",
                        "Usted debe seleccionar la Prueba Hidráulica y hacer click en el botón de editar si desea editar la Prueba Hidráulica."
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
        numeroCertificado = ViewUtils.newBasicConfigTextField("Número de Certificado");
        pasoPrueba = ViewUtils.newBasicConfigTextField("Paso la Prueba Hidráulica");
        descripcion = ViewUtils.newBasicConfigTextField("Descripción");
        fechaVencimientoPH = ViewUtils.newBasicConfigTextField("Fecha de Vencimiento de la PH");

        //Creacion del titulo del PopUp
        H2 headline = new H2(titulo);
        headline.getStyle().set("margin-top", "0");
        editorDiv.add(headline, numeroCertificado, pasoPrueba, descripcion, fechaVencimientoPH);
    }

    /* Seteo del titulo a lo que se refiere cada boton al acercar el mouse al boton*/
    private void createButtonLayout(VerticalLayout layout) {
        close.setColor("#BE2123");
        ViewUtils.setButtonAutoMarginAndVariant(saveButton, ButtonVariant.LUMO_TERTIARY);
        ViewUtils.setButtonAutoMarginAndVariant(cancelButton, ButtonVariant.LUMO_ERROR);
        refreshButton.getElement().setProperty("title", "Actualizar Prueba Hidráulica");
        addButton.getElement().setProperty("title", "Agregar Nueva Prueba Hidráulica");
        editButton.getElement().setProperty("title", "Editar Prueba Hidráulica");
        deleteButton.getElement().setProperty("title", "Borrar Prueba Hidráulica");
        ViewUtils.buttonConfig(layout, addButton, deleteButton, refreshButton, editButton);
    }

    /* Este metodo carga en el PopUp todos los campos del objeto pasado como parametro*/
    private void populateForm(PruebaHidraulica ph) {
        pruebaHidraulicaBinder.readBean(ph);
        numeroCertificado.setValue(ph.getNumeroCertificado());
        pasoPrueba.setValue(ph.getPasoPrueba());
        descripcion.setValue(ph.getDescripcion());
        fechaVencimientoPH.setValue(ph.getFechaVencimientoPH());
    }

    /* Abre el PopUp y le setea algunas propiedades */
    private void popUpOpened() {
        popUp.setCloseOnEsc(true);
        popUp.setCloseOnOutsideClick(false);
        popUp.open();
    }

    /* Este metodo se encarga de limpiar los campos del PopUp, por ejemplo el PopUp Crear Prueba Hidraulica*/
    private void clearForm() {
        numeroCertificado.clear();
        pasoPrueba.clear();
        descripcion.clear();
        fechaVencimientoPH.clear();
    }

    /* Este metodo es el encargado de refrescar los datos de la grilla cada vez que se modifica, se agrega o se borra
    un dato*/
    private void refreshGrid() {
        dataProvider = new ListDataProvider<>(hydraulicTestService.getAllHydraulicTesting());
        pruebaHidraulicaGrid.setItems(dataProvider);
        pruebaHidraulicaGrid.getDataProvider().refreshAll();
        pruebaHidraulicaGrid.select(null);
        dataProvider.refreshAll();
    }

    /* Este metodo es el encargado de crear los filtros en la grilla*/
    private void addFiltersToGrid() {
        HeaderRow filterRow = pruebaHidraulicaGrid.appendHeaderRow();

        TextField numeroFilter = ViewUtils.createNewFilterForColumnGrid();
        numeroFilter.addValueChangeListener(event -> dataProvider
                .addFilter(ph -> StringUtils.containsIgnoreCase(ph.getNumeroCertificado(), numeroFilter.getValue())));

        TextField pasoPHFilter = ViewUtils.createNewFilterForColumnGrid();
        pasoPHFilter.addValueChangeListener(event -> dataProvider
                .addFilter(ph -> StringUtils.containsIgnoreCase(ph.getPasoPrueba(), pasoPHFilter.getValue())));

        TextField vtoPHFilter = ViewUtils.createNewFilterForColumnGrid();
        vtoPHFilter.addValueChangeListener(event -> dataProvider
                .addFilter(ph -> StringUtils.containsIgnoreCase(ph.getFechaVencimientoPH(), vtoPHFilter.getValue())));

        ViewUtils.setFilterInColumnGrid(filterRow, numeroFilter, pruebaHidraulicaGrid.getColumnByKey("numeroCerti"));
        ViewUtils.setFilterInColumnGrid(filterRow, pasoPHFilter, pruebaHidraulicaGrid.getColumnByKey("pasoPH"));
        ViewUtils.setFilterInColumnGrid(filterRow, vtoPHFilter, pruebaHidraulicaGrid.getColumnByKey("vtoPH"));

    }

}
