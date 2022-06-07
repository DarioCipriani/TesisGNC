package com.gnc.task.application.mvp.views.budgetconfig;

import com.gnc.task.application.mvp.model.entity.Role;
import com.gnc.task.application.mvp.model.entity.Presupuesto;
import com.gnc.task.application.mvp.model.entity.User;
import com.gnc.task.application.mvp.presenter.BudgetService;
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
import com.vaadin.flow.component.textfield.BigDecimalField;
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

@PageTitle("Presupuesto")
@Route(value = "presupuestos", layout = MainLayout.class)
@RolesAllowed("admin")
public class BudgetConfigView extends HorizontalLayout {

    public static final String NEW_PRESUPESUTO = "Nuevo Presupuesto";
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
    // creacion de iconos para la ventana de creacion de Presupuesto
    private final Icon check = new Icon(VaadinIcon.CHECK);
    private final Icon close = new Icon(VaadinIcon.CLOSE);
    // creacion de botones para la ventana de creacion de Presupuesto
    private final Button saveButton = new Button(GUARDAR, check);
    private final Button cancelButton = new Button("Cancelar", close);
    //campos que pertenecen a la fila (grid) de presupuestos en la ventana de presupuesto
    private Grid<Presupuesto> presupuestoGrid;
    private IntegerField nroPresupuesto;
    private BigDecimalField importeTotal;
    // Binder es el encargado de enlazar los campos de front con los campos de la base de datos
    private Binder<Presupuesto> presupuestoBinder;
    private ListDataProvider<Presupuesto> dataProvider;
    private Dialog popUp;
    private FormLayout editorDiv;
    private HorizontalLayout buttonDiv;
    private AuthenticatedUser authenticatedUser;
    private BudgetService budgetService;

    public BudgetConfigView(AuthenticatedUser authenticatedUser, BudgetService budgetService) {
        this.authenticatedUser = authenticatedUser;
        this.budgetService = budgetService;
        Optional<User> userLogged = authenticatedUser.get();
        if (userLogged.isPresent() && userLogged.get().getRoles().contains(Role.ADMIN)) {
            setSizeFull();
            crearPopUp();
            editor("");
            seccionBotones();
            editorDiv.add(buttonDiv);
            popUp.add(editorDiv);
            // creacion de la fila (Grid) con el encabezado con cada uno de los campos del presupuesto
            presupuestoGrid = new Grid<>();
            presupuestoGrid.setSizeFull();
            presupuestoGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
            presupuestoGrid.addThemeName("grid-selection-theme");
            presupuestoGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.MATERIAL_COLUMN_DIVIDERS);
            presupuestoGrid.addColumn(Presupuesto::getNroPresupuesto).setHeader("Número de Presupuesto").setResizable(true).setKey("numero")
                    .setSortable(true);
            presupuestoGrid.addColumn(Presupuesto::getImporteTotal).setHeader("Importe Total").setResizable(true).setKey("importe")
                    .setSortable(true);

            presupuestoBinder = new Binder<>(Presupuesto.class);
            presupuestoBinder.bind(nroPresupuesto, Presupuesto::getNroPresupuesto, Presupuesto::setNroPresupuesto);
            presupuestoBinder.bind(importeTotal, Presupuesto::getImporteTotal, Presupuesto::setImporteTotal);

            presupuestoBinder.bindInstanceFields(this);

            dataProvider = new ListDataProvider<>(budgetService.getAllBudgets());
            presupuestoGrid.setDataProvider(dataProvider);

            addFiltersToGrid();
            //Cada vez que se selecciona un elemento de la grilla si el evento no tiene valor (por ejemplo el Crear
            // Presupuesto) entonces seteamos el boton de guardar con el texto "Nuevo Presupuesto", mientras que si el evento
            // tiene valor (por ejemplo el Modificar Presupuesto) entonces seteamos el boton con el texto "Guardar"
            presupuestoGrid.asSingleSelect().addValueChangeListener(event -> {
                if (event.getValue() != null) {
                    saveButton.setText(GUARDAR);
                } else {
                    presupuestoGrid.getDataProvider().refreshAll();
                    saveButton.setText(NEW_PRESUPESUTO);
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
        wrapper.add(presupuestoGrid);
    }

    /*Metodo encargado de decir a cada boton que es lo que tiene que hacer*/
    private void addButtonListeners() {
        cancelButton.addClickListener(e -> {
            refreshGrid();
            popUp.close();
        });

        deleteButton.addClickListener(e -> {
            Presupuesto presupuesto = presupuestoGrid.asSingleSelect().getValue();
            if (Objects.isNull(presupuesto)) {
                ViewUtils.notification("Seleccionar Presupuesto",
                        "Usted debe seleccionar el presupuesto y hacer click en el botón de eliminar si desea eliminar el presupuesto."
                                + "</br>",
                        NotificationVariant.LUMO_PRIMARY, Notification.Position.MIDDLE,15).open();
            } else {
                String detalle = Objects.nonNull(presupuesto)
                        ? String.valueOf(presupuesto.getNroPresupuesto()) : "";

                ConfirmDialog dialog = new ConfirmDialog("Confirmar Borrado",
                        "Está seguro de eliminar el registro seleccionado? Presupuesto: " + detalle, "Borrar",
                        confirmEvent -> {
                            if (Objects.nonNull(presupuesto) && budgetService.deleteBudgetByBudgetID(presupuesto.getId())) {
                                ViewUtils.notification("Borrado exitoso", "<b>Presupuesto:</b> " + detalle + "</br>",
                                        NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE,5).open();
                                refreshGrid();
                                saveButton.setText(NEW_PRESUPESUTO);
                            } else { // no es posible borrar el presupuesto
                                ViewUtils
                                        .notification("Presupuesto no habilitado para la eliminación", "<b>Producto:</b> "
                                                + (Objects.nonNull(presupuesto) ? detalle : "<i>No está seleccionado</i>")
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

            Presupuesto presupuesto = presupuestoGrid.asSingleSelect().getValue();
            if (GUARDAR.equals(saveButton.getText())) {
                presupuesto = budgetService.getBudgetByNroPresupuesto(presupuesto.getNroPresupuesto());
                if (Objects.nonNull(presupuesto)) {
                    presupuesto.setNroPresupuesto(nroPresupuesto.getValue());
                    presupuesto.setImporteTotal(importeTotal.getValue());
                    presupuesto.setUpdatedAt(new Date());
                    try {
                        budgetService.updateProduct(presupuesto);
                        popUp.close();
                        ViewUtils
                                .notification("Presupuesto Actualizado",
                                        "<b>Presupuesto:</b> " + presupuesto.getNroPresupuesto() + "</br>",
                                        NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE,5)
                                .open();
                        refreshGrid();

                    } catch (Exception exception) {
                        ViewUtils
                                .notification("Presupuesto No Actualizado",
                                        "<b>Causa:</b> " + exception.getCause() + "</br>",
                                        NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE,15)
                                .open();
                    }

                } else {
                    LOGGER.error("El Presupuesto no existe o hay un problema para obtener sus datos");
                    ViewUtils.notification("Algo esta mal",
                            "El presupuesto no puede ser cargado, intente refrescando la página y volviendo a realizar los cambios, si el error continúa, consulte con el administrador. </br>",
                            NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE,15).open();
                }

            } else {
                if (NEW_PRESUPESUTO.equals(saveButton.getText())) {
                    presupuesto = new Presupuesto();
                    presupuesto.setNroPresupuesto(nroPresupuesto.getValue());
                    presupuesto.setImporteTotal(importeTotal.getValue());
                    presupuesto.setCreatedAt(new Date());
                    presupuesto.setUpdatedAt(new Date());
                    try {
                        budgetService.updateProduct(presupuesto);
                        popUp.close();
                        refreshGrid();
                        ViewUtils
                                .notification("Presupuesto creado",
                                        "<b>Presupuesto:</b> " + presupuesto.getImporteTotal() + "</br>",
                                        NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE,5)
                                .open();
                    } catch (Exception exception) {
                        ViewUtils
                                .notification("Presupuesto No creado",
                                        "<b>Causa:</b> " + exception.getCause() + "</br>",
                                        NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE,15)
                                .open();
                    }
                } else
                    ViewUtils.notification("Algo esta mal",
                            "El presupuesto no puede ser cargado, intente refrescando la pagina y volviendo a realizar los cambios, si el error continúa, consulte con el administrador. </br>",
                            NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE,15).open();
            }

        });
        // El boton de refrescar es el encargado de decirle al grid que recargue nuevamente los datos que trae de la
        // base de datos
        refreshButton.addClickListener(event -> refreshGrid());
        //Es lo que hace el boton +
        addButton.addClickListener(event -> {
            crearPopUp();
            editor("Crear Presupuesto");
            seccionBotones();
            editorDiv.add(buttonDiv);
            popUp.add(editorDiv);
            presupuestoGrid.asSingleSelect().clear();
            clearForm();
            saveButton.setText(NEW_PRESUPESUTO);
            popUpOpened();
        });

        // Boton de editar
        editButton.addClickListener(event -> {
            crearPopUp();
            editor("Modificar Presupuesto");
            seccionBotones();
            editorDiv.add(buttonDiv);
            popUp.add(editorDiv);
            Presupuesto presupuesto = presupuestoGrid.asSingleSelect().getValue();
            if (Objects.nonNull(presupuesto)) {
                populateForm(presupuesto);
                saveButton.setText(GUARDAR);
                popUpOpened();
            } else {
                ViewUtils.notification("Seleccionar Presupuesto",
                        "Usted debe seleccionar el presupuesto y hacer click en el botón de editar si desea editar el presupuesto."
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
        nroPresupuesto = ViewUtils.newBasicConfigIntegerField("Número de Presupuesto");
        importeTotal = ViewUtils.newBasicConfigBigDecimalField("Importe Total");

        //Creacion del titulo del PopUp
        H2 headline = new H2(titulo);
        headline.getStyle().set("margin-top", "0");
        editorDiv.add(headline, nroPresupuesto, importeTotal);
    }

    /* Seteo del titulo a lo que se refiere cada boton al acercar el mouse al boton*/
    private void createButtonLayout(VerticalLayout layout) {
        close.setColor("#BE2123");
        ViewUtils.setButtonAutoMarginAndVariant(saveButton, ButtonVariant.LUMO_TERTIARY);
        ViewUtils.setButtonAutoMarginAndVariant(cancelButton, ButtonVariant.LUMO_ERROR);
        refreshButton.getElement().setProperty("title", "Actualizar Presupuesto");
        addButton.getElement().setProperty("title", "Agregar Nuevo Presupuesto");
        editButton.getElement().setProperty("title", "Editar Presupuesto");
        deleteButton.getElement().setProperty("title", "Borrar Presupuesto");
        ViewUtils.buttonConfig(layout, addButton, deleteButton, refreshButton, editButton);
    }

    /* Este metodo carga en el PopUp todos los campos del objeto pasado como parametro*/
    private void populateForm(Presupuesto presupuesto) {
        presupuestoBinder.readBean(presupuesto);
        nroPresupuesto.setValue(presupuesto.getNroPresupuesto());
        importeTotal.setValue(presupuesto.getImporteTotal());
    }

    /* Abre el PopUp y le setea algunas propiedades */
    private void popUpOpened() {
        popUp.setCloseOnEsc(true);
        popUp.setCloseOnOutsideClick(false);
        popUp.open();
    }

    /* Este metodo se encarga de limpiar los campos del PopUp, por ejemplo el PopUp Crear Presupuesto*/
    private void clearForm() {
        nroPresupuesto.clear();
        importeTotal.clear();
    }

    /* Este metodo es el encargado de refrescar los datos de la grilla cada vez que se modifica, se agrega o se borra
    un dato*/
    private void refreshGrid() {
        dataProvider = new ListDataProvider<>(budgetService.getAllBudgets());
        presupuestoGrid.setItems(dataProvider);
        presupuestoGrid.getDataProvider().refreshAll();
        presupuestoGrid.select(null);
        dataProvider.refreshAll();
    }

    /* Este metodo es el encargado de crear los filtros en la grilla*/
    private void addFiltersToGrid() {
        HeaderRow filterRow = presupuestoGrid.appendHeaderRow();

        TextField nroFilter = ViewUtils.createNewFilterForColumnGrid();
        nroFilter.addValueChangeListener(event -> dataProvider
                .addFilter(presupuesto -> StringUtils.containsIgnoreCase(presupuesto.getNroPresupuesto().toString(), nroFilter.getValue())));

        ViewUtils.setFilterInColumnGrid(filterRow, nroFilter, presupuestoGrid.getColumnByKey("numero"));

    }

}
