package com.gnc.task.application.views.fallas;

import com.gnc.task.application.data.Role;
import com.gnc.task.application.data.entity.Fallas;
import com.gnc.task.application.data.entity.User;
import com.gnc.task.application.data.service.FallaService;
import com.gnc.task.application.security.AuthenticatedUser;
import com.gnc.task.application.security.FallaDetailsServiceImpl;
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
import java.util.Objects;
import java.util.Optional;

@PageTitle("Manual de Fallas")
@Route(value = "fallas", layout = MainLayout.class)
@RolesAllowed("admin")
public class FallasConfigView extends HorizontalLayout {

    public static final String NEW_BUG = "Nueva Falla";
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private Grid<Fallas> fallaGrid;

    private TextField marca;
    private TextField modelo;
    private TextField description;
    private TextField solution;

    private Binder<Fallas> fallaBinder;
    private ListDataProvider<Fallas> dataProvider;

    private final Icon refreshIcon = new Icon(VaadinIcon.REFRESH);
    private final Icon addIcon = new Icon(VaadinIcon.PLUS);
    private final Icon editIcon = new Icon(VaadinIcon.PENCIL);
    private final Icon deleteIcon = new Icon(VaadinIcon.TRASH);

    private final Button refreshButton = new Button(refreshIcon);
    private final Button addButton = new Button(addIcon);
    private final Button editButton = new Button(editIcon);
    private final Button deleteButton = new Button(deleteIcon);

    private Dialog popUp;
    private FormLayout editorDiv;
    private HorizontalLayout buttonDiv;

    private final Icon check = new Icon(VaadinIcon.CHECK);
    private final Icon close = new Icon(VaadinIcon.CLOSE);

    private final Button saveButton = new Button("Save", check);
    private final Button cancelButton = new Button("Cancel", close);

    private AuthenticatedUser authenticatedUser;
    private FallaService fallaService;
    private FallaDetailsServiceImpl fallaDetailsServiceImpl;

    public FallasConfigView(AuthenticatedUser authenticatedUser, FallaService fallasService, FallaDetailsServiceImpl fallaDetailsServiceImpl) {
        this.authenticatedUser = authenticatedUser;
        this.fallaService = fallasService;
        this.fallaDetailsServiceImpl = fallaDetailsServiceImpl;
        Optional<User> userLogged = authenticatedUser.get();
        if (userLogged.isPresent() && userLogged.get().getRoles().contains(Role.ADMIN)) {
            setSizeFull();
            extraLayoutConfig();
            fallaGrid = new Grid<>();
            fallaGrid.setSizeFull();
            fallaGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
            fallaGrid.addThemeName("grid-selection-theme");
            fallaGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.MATERIAL_COLUMN_DIVIDERS);
            fallaGrid.addColumn(Fallas::getId).setHeader("Id").setResizable(true).setKey("id")
                    .setSortable(true);
            fallaGrid.addColumn(Fallas::getMarca).setHeader("Marca").setResizable(true)
                    .setKey("marca").setSortable(true);
            fallaGrid.addColumn(Fallas::getModelo).setHeader("Modelo").setResizable(true).setKey("modelo").setSortable(true);

            fallaGrid.addColumn(Fallas::getDescription).setHeader("Descripción").setResizable(true)
                    .setKey("description").setSortable(true);
            fallaGrid.addColumn(Fallas::getSolution).setHeader("Solución").setResizable(true)
                    .setKey("solution").setSortable(true);

            fallaBinder = new Binder<>(Fallas.class);
            fallaBinder.bind(marca, Fallas::getMarca, Fallas::setMarca);
            fallaBinder.bind(modelo, Fallas::getModelo, Fallas::setModelo);
            fallaBinder.bind(description, Fallas::getDescription, Fallas::setDescription);
            fallaBinder.bind(solution, Fallas::getSolution, Fallas::setSolution);


            fallaBinder.bindInstanceFields(this);

            dataProvider = new ListDataProvider<>(fallaService.getAllFallas());
            fallaGrid.setDataProvider(dataProvider);

            addFiltersToGrid();

            fallaGrid.asSingleSelect().addValueChangeListener(event -> {
                if (event.getValue() != null) {
                    saveButton.setText("Save");
                } else {
                    fallaGrid.getDataProvider().refreshAll();
                    saveButton.setText(NEW_BUG);
                }
            });

            addButtonListeners();

            VerticalLayout layout = new VerticalLayout();
            layout.setSizeFull();

            createButtonLayout(layout);
            createGridLayout(layout);

            add(layout);

        }

    }

    private void createGridLayout(VerticalLayout layout) {
        Div wrapper = new Div();
        wrapper.setId("wrapper");
        wrapper.setSizeFull();
        layout.add(wrapper);
        wrapper.add(fallaGrid);
    }

    private void addButtonListeners() {
        cancelButton.addClickListener(e -> {
            refreshGrid(fallaDetailsServiceImpl);
            popUp.close();
        });

        deleteButton.addClickListener(e -> {
            Fallas falla = fallaGrid.asSingleSelect().getValue();
            ConfirmDialog dialog = new ConfirmDialog("Confirm delete",
                    "Are you sure you want to delete the Bug? Falla: " + falla.getDescription(), "Delete",
                    confirmEvent -> {
                        if (Objects.nonNull(falla) && fallaService.deleteFallaByID(falla.getId())) {
                            ViewUtils.notification("Success delete",
                                            "<b>Nombre:</b> " + falla.getDescription() + "</br>",
                                            NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE)
                                    .open();
                            refreshGrid(fallaDetailsServiceImpl);
                            saveButton.setText(NEW_BUG);
                        } else { // is unable to delete this user
                            ViewUtils
                                    .notification("Unable delete",
                                            "<b>Nombre:</b> " + (Objects.nonNull(falla) ? falla.getDescription()
                                                    : "<i>not selected</i>") + "</br>",
                                            NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE)
                                    .open();
                        }
                    }, "Cancel", cancelEvent -> {
            });
            dialog.setConfirmButtonTheme(NotificationVariant.LUMO_ERROR.getVariantName());
            dialog.open();
        });

        saveButton.addClickListener(e -> {
            Fallas falla = fallaGrid.asSingleSelect().getValue();
            if (Objects.nonNull(falla)) {
                if ("Save".equals(saveButton.getText())) {
                    falla = fallaService.getClientByMarca(falla.getMarca());
                    if (Objects.nonNull(falla)) {
                        if (fallaBinder.hasChanges()) {
                            falla.setMarca(marca.getValue());
                            falla.setModelo(modelo.getValue());
                            falla.setSolution(solution.getValue());
                            falla.setDescription(description.getValue());
                            fallaService.updateFalla(falla);
                            popUp.close();
                            ViewUtils.notification("Updated falla",
                                            "<b>Nombre:</b> " + falla.getDescription() + "</br>",
                                            NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE)
                                    .open();
                            refreshGrid(fallaDetailsServiceImpl);
                        } else {
                            popUp.close();
                            ViewUtils.notification("There are no changes",
                                            "<b>Nombre:</b> " + falla.getDescription() + "</br>",
                                            NotificationVariant.LUMO_PRIMARY, Notification.Position.MIDDLE)
                                    .open();
                        }
                    } else {
                        LOGGER.error("the user no longer exists or there was a problem trying to get it");
                        ViewUtils.notification("Something went wrong",
                                "The client could not be obtained, try to refresh and do the process again, if something goes wrong consult with the administrator. </br>",
                                NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE).open();
                    }
                }
            } else {
                if (NEW_BUG.equals(saveButton.getText())) {
                    falla = new Fallas();
                    falla.setMarca(marca.getValue());
                    falla.setModelo(modelo.getValue());
                    falla.setSolution(solution.getValue());
                    falla.setDescription(description.getValue());
                    fallaService.updateFalla(falla);
                    popUp.close();
                    refreshGrid(fallaDetailsServiceImpl);
                    ViewUtils
                            .notification("Creado cliente", "<b>Falla:</b> " + falla.getDescription() + "</br>",
                                    NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE)
                            .open();
                }
            }

        });

        refreshButton.addClickListener(event -> refreshGrid(fallaDetailsServiceImpl));

        addButton.addClickListener(event -> {
            fallaGrid.asSingleSelect().clear();
            clearForm();
            saveButton.setText(NEW_BUG);
            popUpOpened();
        });

        editButton.addClickListener(event -> {
            Fallas falla = fallaGrid.asSingleSelect().getValue();
            if (Objects.nonNull(falla)) {
                populateForm(falla);
                saveButton.setText("Save");
                popUpOpened();
            } else {
                ViewUtils.notification("Select falla",
                        "You must select a falla and then click the edit button if you want to edit the user."
                                + "</br>",
                        NotificationVariant.LUMO_PRIMARY, Notification.Position.MIDDLE).open();
            }
        });

    }

    private void extraLayoutConfig() {
        popUp = new Dialog();
        popUp.setResizable(true);
        popUp.setCloseOnOutsideClick(false);
        popUp.setCloseOnEsc(true);

        editorDiv = new FormLayout();
        editorDiv.setId("editor");
        editorDiv.setSizeFull();
        editorDiv.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("600px", 1, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));

        marca = ViewUtils.newBasicConfigTextField("Marca");
        modelo = ViewUtils.newBasicConfigTextField("Modelo");
        description = ViewUtils.newBasicConfigTextField("Descripcion");
        solution = ViewUtils.newBasicConfigTextField("Solucion");


        editorDiv.add(marca, modelo, solution, description);

        buttonDiv = new HorizontalLayout();
        buttonDiv.setId("button-layout");
        buttonDiv.setSizeFull();
        buttonDiv.add(cancelButton, saveButton);
        buttonDiv.setAlignItems(Alignment.CENTER);
        buttonDiv.setVerticalComponentAlignment(Alignment.CENTER);
        editorDiv.add(buttonDiv);
        popUp.add(editorDiv);


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

    private void populateForm(Fallas falla) {
        fallaBinder.readBean(falla);
        marca.setValue(falla.getMarca());
        modelo.setValue(falla.getModelo());
        solution.setValue(falla.getSolution());
        description.setValue(falla.getDescription());
    }

    private void popUpOpened() {
        popUp.setCloseOnEsc(true);
        popUp.setCloseOnOutsideClick(false);
        popUp.open();
    }

    private void clearForm() {
        marca.clear();
        modelo.clear();
        solution.clear();
        description.clear();
    }

    private void refreshGrid(FallaDetailsServiceImpl fallaDetailsServiceImpl) {
        dataProvider = new ListDataProvider<>(fallaDetailsServiceImpl.findAllFalla());
        fallaGrid.setItems(dataProvider);
        fallaGrid.getDataProvider().refreshAll();
        fallaGrid.select(null);
        dataProvider.refreshAll();
    }

    private void addFiltersToGrid() {
        HeaderRow filterRow = fallaGrid.appendHeaderRow();
        TextField solutionFilter = ViewUtils.createNewFilterForColumnGrid();
        solutionFilter.addValueChangeListener(event -> dataProvider
                .addFilter(fallas -> StringUtils.containsIgnoreCase(fallas.getSolution(), solutionFilter.getValue())));
        TextField marcaFilter = ViewUtils.createNewFilterForColumnGrid();
        marcaFilter.addValueChangeListener(event -> dataProvider
                .addFilter(fallas -> StringUtils.containsIgnoreCase(fallas.getMarca(), marcaFilter.getValue())));
        TextField modeloFilter = ViewUtils.createNewFilterForColumnGrid();
        marcaFilter.addValueChangeListener(event -> dataProvider
                .addFilter(fallas -> StringUtils.containsIgnoreCase(fallas.getModelo(), modeloFilter.getValue())));
        TextField descriptionFilter = ViewUtils.createNewFilterForColumnGrid();
        marcaFilter.addValueChangeListener(event -> dataProvider
                .addFilter(fallas -> StringUtils.containsIgnoreCase(fallas.getDescription(), descriptionFilter.getValue())));
        ViewUtils.setFilterInColumnGrid(filterRow, marcaFilter, fallaGrid.getColumnByKey("marca"));
        ViewUtils.setFilterInColumnGrid(filterRow, modeloFilter, fallaGrid.getColumnByKey("modelo"));
        ViewUtils.setFilterInColumnGrid(filterRow, descriptionFilter, fallaGrid.getColumnByKey("description"));
        ViewUtils.setFilterInColumnGrid(filterRow, solutionFilter, fallaGrid.getColumnByKey("solution"));

    }
}
