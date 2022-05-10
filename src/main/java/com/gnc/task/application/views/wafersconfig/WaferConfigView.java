package com.gnc.task.application.views.wafersconfig;

import com.gnc.task.application.data.Role;
import com.gnc.task.application.data.entity.Oblea;
import com.gnc.task.application.data.entity.User;
import com.gnc.task.application.data.service.WaferService;
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

@PageTitle("Oblea")
@Route(value = "obleas", layout = MainLayout.class)
@RolesAllowed("admin")
public class WaferConfigView extends HorizontalLayout {

    public static final String NEW_OBLEA = "Nueva Oblea";
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
    // creacion de iconos para la ventana de creacion de oblea
    private final Icon check = new Icon(VaadinIcon.CHECK);
    private final Icon close = new Icon(VaadinIcon.CLOSE);
    // creacion de botones para la ventana de creacion de oblea
    private final Button saveButton = new Button(GUARDAR, check);
    private final Button cancelButton = new Button("Cancelar", close);
    //campos que pertenecen a la fila (grid) de obleas en la ventana de obleas
    private Grid<Oblea> obleaGrid;
    private TextField numeroDeOblea;
    private TextField fechaVencimiento;
    private TextField obleaVigente;
    // Binder es el encargado de enlazar los campos de front con los campos de la base de datos
    private Binder<Oblea> obleaBinder;
    private ListDataProvider<Oblea> dataProvider;
    private Dialog popUp;
    private FormLayout editorDiv;
    private HorizontalLayout buttonDiv;
    private AuthenticatedUser authenticatedUser;
    private WaferService waferService;

    public WaferConfigView(AuthenticatedUser authenticatedUser, WaferService waferService) {
        this.authenticatedUser = authenticatedUser;
        this.waferService = waferService;
        Optional<User> userLogged = authenticatedUser.get();
        if (userLogged.isPresent() && userLogged.get().getRoles().contains(Role.ADMIN)) {
            setSizeFull();
            crearPopUp();
            editor("");
            seccionBotones();
            editorDiv.add(buttonDiv);
            popUp.add(editorDiv);
            // creacion de la fila (Grid) con el encabezado con cada uno de los campos de la oblea
            obleaGrid = new Grid<>();
            obleaGrid.setSizeFull();
            obleaGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
            obleaGrid.addThemeName("grid-selection-theme");
            obleaGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.MATERIAL_COLUMN_DIVIDERS);
            obleaGrid.addColumn(Oblea::getNumeroDeOblea).setHeader("Número de Oblea").setResizable(true).setKey("numero")
                    .setSortable(true);
            obleaGrid.addColumn(Oblea::getFechaVencimiento).setHeader("Fecha de Vencimiento").setResizable(true).setKey("fechaVencimiento")
                    .setSortable(true);
            obleaGrid.addColumn(Oblea::getObleaVigente).setHeader("Oblea Vigente").setResizable(true).setKey("obleaVigente")
                    .setSortable(true);

            obleaBinder = new Binder<>(Oblea.class);
            obleaBinder.bind(numeroDeOblea, Oblea::getNumeroDeOblea, Oblea::setNumeroDeOblea);
            obleaBinder.bind(fechaVencimiento, Oblea::getFechaVencimiento, Oblea::setFechaVencimiento);
            obleaBinder.bind(obleaVigente, Oblea::getObleaVigente, Oblea::setObleaVigente);

            obleaBinder.bindInstanceFields(this);

            dataProvider = new ListDataProvider<>(waferService.getAllWafers());
            obleaGrid.setDataProvider(dataProvider);

            addFiltersToGrid();
            //Cada vez que se selecciona un elemento de la grilla si el evento no tiene valor (por ejemplo el Crear
            // Oblea) entonces seteamos el boton de guardar con el texto "Nueva Oblea", mientras que si el evento
            // tiene valor (por ejemplo el Modificar Oblea) entonces seteamos el boton con el texto "Guardar"
            obleaGrid.asSingleSelect().addValueChangeListener(event -> {
                if (event.getValue() != null) {
                    saveButton.setText(GUARDAR);
                } else {
                    obleaGrid.getDataProvider().refreshAll();
                    saveButton.setText(NEW_OBLEA);
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
        wrapper.add(obleaGrid);
    }

    /*Metodo encargado de decir a cada boton que es lo que tiene que hacer*/
    private void addButtonListeners() {
        cancelButton.addClickListener(e -> {
            refreshGrid();
            popUp.close();
        });

        deleteButton.addClickListener(e -> {
            Oblea oblea = obleaGrid.asSingleSelect().getValue();
            if (Objects.isNull(oblea)) {
                ViewUtils.notification("Seleccionar Oblea",
                        "Usted debe seleccionar la oblea y hacer click en el botón de eliminar si desea eliminar la oblea."
                                + "</br>",
                        NotificationVariant.LUMO_PRIMARY, Notification.Position.MIDDLE).open();
            } else {
                String detalle = Objects.nonNull(oblea)
                        ? oblea.getNumeroDeOblea() : "";
                ConfirmDialog dialog = new ConfirmDialog("Confirmar Borrado",
                        "Está seguro de eliminar el registro seleccionado? Oblea: " + detalle, "Borrar",
                        confirmEvent -> {
                            if (Objects.nonNull(oblea) && waferService.deleteWaferByWaferID(oblea.getId())) {
                                ViewUtils.notification("Borrado exitoso", "<b>Oblea:</b> " + detalle + "</br>",
                                        NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE).open();
                                refreshGrid();
                                saveButton.setText(NEW_OBLEA);
                            } else { // no es posible borrar la oblea
                                ViewUtils
                                        .notification("Oblea no habilitada para la eliminación", "<b>Oblea:</b> "
                                                + (Objects.nonNull(oblea) ? detalle : "<i>No está seleccionado</i>")
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

            Oblea oblea = obleaGrid.asSingleSelect().getValue();
            if (GUARDAR.equals(saveButton.getText())) {
                oblea = waferService.getWaferByNumeroDeOblea(oblea.getNumeroDeOblea());
                if (Objects.nonNull(oblea)) {
                    oblea.setNumeroDeOblea(numeroDeOblea.getValue());
                    oblea.setFechaVencimiento(fechaVencimiento.getValue());
                    oblea.setObleaVigente(obleaVigente.getValue());
                    oblea.setUpdatedAt(new Date());
                    try {
                        waferService.updateWafer(oblea);
                        popUp.close();
                        ViewUtils
                                .notification("Oblea Actualizada",
                                        "<b>Oblea:</b> " + oblea.getNumeroDeOblea() + "</br>",
                                        NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE)
                                .open();
                        refreshGrid();

                    } catch (Exception exception) {
                        ViewUtils
                                .notification("Oblea No Actualizada",
                                        "<b>Causa:</b> " + exception.getCause() + "</br>",
                                        NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE)
                                .open();
                    }

                } else {
                    LOGGER.error("La Oblea no existe o hay un problema para obtener sus datos");
                    ViewUtils.notification("Algo esta mal",
                            "La oblea no puede ser cargada, intente refrescando la página y volviendo a realizar los cambios, si el error continúa, consulte con el administrador. </br>",
                            NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE).open();
                }

            } else {
                if (NEW_OBLEA.equals(saveButton.getText())) {
                    oblea = new Oblea();
                    oblea.setNumeroDeOblea(numeroDeOblea.getValue());
                    oblea.setFechaVencimiento(fechaVencimiento.getValue());
                    oblea.setObleaVigente(obleaVigente.getValue());
                    oblea.setCreatedAt(new Date());
                    oblea.setUpdatedAt(new Date());
                    try {
                        waferService.updateWafer(oblea);
                        popUp.close();
                        refreshGrid();
                        ViewUtils
                                .notification("Oblea creada",
                                        "<b>Oblea:</b> " + oblea.getNumeroDeOblea() + "</br>",
                                        NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE)
                                .open();
                    } catch (Exception exception) {
                        ViewUtils
                                .notification("Oblea No creada",
                                        "<b>Causa:</b> " + exception.getCause() + "</br>",
                                        NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE)
                                .open();
                    }
                } else
                    ViewUtils.notification("Algo esta mal",
                            "La oblea no puede ser cargada, intente refrescando la pagina y volviendo a realizar los cambios, si el error continúa, consulte con el administrador. </br>",
                            NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE).open();
            }

        });
        // El boton de refrescar es el encargado de decirle al grid que recargue nuevamente los datos que trae de la
        // base de datos
        refreshButton.addClickListener(event -> refreshGrid());
        //Es lo que hace el boton +
        addButton.addClickListener(event -> {
            crearPopUp();
            editor("Crear Oblea");
            seccionBotones();
            editorDiv.add(buttonDiv);
            popUp.add(editorDiv);
            obleaGrid.asSingleSelect().clear();
            clearForm();
            saveButton.setText(NEW_OBLEA);
            popUpOpened();
        });

        // Boton de editar
        editButton.addClickListener(event -> {
            crearPopUp();
            editor("Modificar Oblea");
            seccionBotones();
            editorDiv.add(buttonDiv);
            popUp.add(editorDiv);
            Oblea oblea = obleaGrid.asSingleSelect().getValue();
            if (Objects.nonNull(oblea)) {
                populateForm(oblea);
                saveButton.setText(GUARDAR);
                popUpOpened();
            } else {
                ViewUtils.notification("Seleccionar Oblea",
                        "Usted debe seleccionar la oblea y hacer click en el botón de editar si desea editar la oblea."
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
        numeroDeOblea = ViewUtils.newBasicConfigTextField("Número de Oblea");
        fechaVencimiento = ViewUtils.newBasicConfigTextField("Fecha de Vencimiento");
        obleaVigente = ViewUtils.newBasicConfigTextField("Oblea Vigente");

        //Creacion del titulo del PopUp
        H2 headline = new H2(titulo);
        headline.getStyle().set("margin-top", "0");
        editorDiv.add(headline, numeroDeOblea, fechaVencimiento, obleaVigente);
    }

    /* Seteo del titulo a lo que se refiere cada boton al acercar el mouse al boton*/
    private void createButtonLayout(VerticalLayout layout) {
        close.setColor("#BE2123");
        ViewUtils.setButtonAutoMarginAndVariant(saveButton, ButtonVariant.LUMO_TERTIARY);
        ViewUtils.setButtonAutoMarginAndVariant(cancelButton, ButtonVariant.LUMO_ERROR);
        refreshButton.getElement().setProperty("title", "Actualizar Obleas");
        addButton.getElement().setProperty("title", "Agregar Nueva Oblea");
        editButton.getElement().setProperty("title", "Editar Oblea");
        deleteButton.getElement().setProperty("title", "Borrar Oblea");
        ViewUtils.buttonConfig(layout, addButton, deleteButton, refreshButton, editButton);
    }

    /* Este metodo carga en el PopUp todos los campos del objeto pasado como parametro*/
    private void populateForm(Oblea oblea) {
        obleaBinder.readBean(oblea);
        numeroDeOblea.setValue(oblea.getNumeroDeOblea());
        fechaVencimiento.setValue(oblea.getFechaVencimiento());
        obleaVigente.setValue(oblea.getObleaVigente());
    }

    /* Abre el PopUp y le setea algunas propiedades */
    private void popUpOpened() {
        popUp.setCloseOnEsc(true);
        popUp.setCloseOnOutsideClick(false);
        popUp.open();
    }

    /* Este metodo se encarga de limpiar los campos del PopUp, por ejemplo el PopUp Crear Oblea*/
    private void clearForm() {
        numeroDeOblea.clear();
        fechaVencimiento.clear();
        obleaVigente.clear();
    }

    /* Este metodo es el encargado de refrescar los datos de la grilla cada vez que se modifica, se agrega o se borra
    un dato*/
    private void refreshGrid() {
        dataProvider = new ListDataProvider<>(waferService.getAllWafers());
        obleaGrid.setItems(dataProvider);
        obleaGrid.getDataProvider().refreshAll();
        obleaGrid.select(null);
        dataProvider.refreshAll();
    }

    /* Este metodo es el encargado de crear los filtros en la grilla*/
    private void addFiltersToGrid() {
        HeaderRow filterRow = obleaGrid.appendHeaderRow();

        TextField numeroDeObleaFilter = ViewUtils.createNewFilterForColumnGrid();
        numeroDeObleaFilter.addValueChangeListener(event -> dataProvider
                .addFilter(oblea -> StringUtils.containsIgnoreCase(oblea.getNumeroDeOblea(), numeroDeObleaFilter.getValue())));

        TextField fechaVencimientoFilter = ViewUtils.createNewFilterForColumnGrid();
        fechaVencimientoFilter.addValueChangeListener(event -> dataProvider
                .addFilter(oblea -> StringUtils.containsIgnoreCase(oblea.getFechaVencimiento(), fechaVencimientoFilter.getValue())));

        ViewUtils.setFilterInColumnGrid(filterRow, numeroDeObleaFilter, obleaGrid.getColumnByKey("numero"));
        ViewUtils.setFilterInColumnGrid(filterRow, fechaVencimientoFilter, obleaGrid.getColumnByKey("fechaVencimiento"));

    }

}
