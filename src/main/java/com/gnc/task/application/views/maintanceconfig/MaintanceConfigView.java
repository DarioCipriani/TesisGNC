package com.gnc.task.application.views.maintanceconfig;

import com.gnc.task.application.data.Role;
import com.gnc.task.application.data.TMantenimiento;
import com.gnc.task.application.data.entity.Mantenimiento;
import com.gnc.task.application.data.entity.User;
import com.gnc.task.application.data.entity.Vehiculo;
import com.gnc.task.application.data.repository.MaintenancesRepository;
import com.gnc.task.application.data.repository.VehiclesRepository;
import com.gnc.task.application.data.service.MaintanceService;
import com.gnc.task.application.data.service.VehicleService;
import com.gnc.task.application.security.AuthenticatedUser;
import com.gnc.task.application.utilities.ViewUtils;
import com.gnc.task.application.views.MainLayout;
import com.vaadin.componentfactory.MultipleSelect;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
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
import com.vaadin.flow.data.selection.SingleSelect;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.security.RolesAllowed;
import java.util.*;
import java.util.stream.Stream;

@PageTitle("Mantenimiento")
@Route(value = "mantenimientos", layout = MainLayout.class)
@RolesAllowed("admin")
public class MaintanceConfigView extends HorizontalLayout {

	public static final String NEW_MANTENIMIENTO = "Nuevo Mantenimiento";
	public static final String GUARDAR = "Guardar";
	private VehicleService service;
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
	// creacion de iconos para la ventana de creacion de mantenimiento
	private final Icon check = new Icon(VaadinIcon.CHECK);
	private final Icon close = new Icon(VaadinIcon.CLOSE);
	// creacion de botones para la ventana de creacion de mantenimiento
	private final Button saveButton = new Button(GUARDAR, check);
	private final Button cancelButton = new Button("Cancelar", close);
	//campos que pertenecen a la fila (grid) de Mantenimiento en la ventana de mantenimientos
	private Grid<Mantenimiento> mantenimientoGrid;
	private TextField descripcion;
	private ComboBox<TMantenimiento> tipoMantenimiento;
	private ComboBox<Vehiculo> vehiculoSingleSelect ;

	// Binder es el encargado de enlazar los campos de front con los campos de la base de datos
	private Binder<Mantenimiento> mantenimientoBinder;
	private ListDataProvider<Mantenimiento> dataProvider;
	private Dialog popUp;
	private FormLayout editorDiv;
	private HorizontalLayout buttonDiv;
	private AuthenticatedUser authenticatedUser;
	private MaintanceService maintanceService;

	public MaintanceConfigView(AuthenticatedUser authenticatedUser, MaintanceService maintanceService,VehicleService service) {
		this.authenticatedUser = authenticatedUser;
		this.maintanceService = maintanceService;
		this.service =service;
		Optional<User> userLogged = authenticatedUser.get();
		if (userLogged.isPresent() && userLogged.get().getRoles().contains(Role.ADMIN)) {
			setSizeFull();
			crearPopUp();
			editor("");
			seccionBotones();
			editorDiv.add(buttonDiv);
			popUp.add(editorDiv);
			// creacion de la fila (Grid) con el encabezado con cada uno de los campos de mantenimiento
			mantenimientoGrid = new Grid<>();
			mantenimientoGrid.setSizeFull();
			mantenimientoGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
			mantenimientoGrid.addThemeName("grid-selection-theme");
			mantenimientoGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.MATERIAL_COLUMN_DIVIDERS);
			mantenimientoGrid.addColumn(Mantenimiento::getVehiculo).setHeader("Vehículo").setResizable(true).setKey("vehiculo")
					.setSortable(true);
			mantenimientoGrid.addColumn(Mantenimiento::getTipoMantenimiento).setHeader("Tipo de Mantenimiento").setResizable(true).setKey("tipo")
					.setSortable(true);
			mantenimientoGrid.addColumn(Mantenimiento::getDescripcion).setHeader("Descripción").setResizable(true).setKey("descripcion").setSortable(true);

			mantenimientoBinder = new Binder<>(Mantenimiento.class);
			mantenimientoBinder.bind(vehiculoSingleSelect, Mantenimiento::getVehiculo, Mantenimiento::setVehiculo);
			mantenimientoBinder.bind(tipoMantenimiento, Mantenimiento::getTipoMantenimiento, Mantenimiento::setTipoMantenimiento);
			mantenimientoBinder.bind(descripcion, Mantenimiento::getDescripcion, Mantenimiento::setDescripcion);
			mantenimientoBinder.bindInstanceFields(this);

			dataProvider = new ListDataProvider<>(maintanceService.getAllMantenimientos());
			mantenimientoGrid.setDataProvider(dataProvider);

			addFiltersToGrid();
			//Cada vez que se selecciona un elemento de la grilla si el evento no tiene valor (por ejemplo el Crear
			// Mantenimiento) entonces seteamos el boton de guardar con el texto "Nuevo MAntenimiento", mientras que si el evento
			// tiene valor (por ejemplo el Modificar Mantenimiento) entonces seteamos el boton con el texto "Guardar"
			mantenimientoGrid.asSingleSelect().addValueChangeListener(event -> {
				if (event.getValue() != null) {
					saveButton.setText(GUARDAR);
				} else {
					mantenimientoGrid.getDataProvider().refreshAll();
					saveButton.setText(NEW_MANTENIMIENTO);
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
		wrapper.add(mantenimientoGrid);
	}
	/*Metodo encargado de decir a cada boton que es lo que tiene que hacer*/
	private void addButtonListeners() {
		cancelButton.addClickListener(e -> {
			refreshGrid();
			popUp.close();
		});

		deleteButton.addClickListener(e -> {
			Mantenimiento mantenimiento = mantenimientoGrid.asSingleSelect().getValue();
			if (Objects.isNull(mantenimiento)) {
				ViewUtils.notification("Seleccionar mantenimiento",
						"Usted debe seleccionar el mantenimiento y hacer click en el botón de eliminar si desea eliminar el mantenimiento."
								+ "</br>",
						NotificationVariant.LUMO_PRIMARY, Notification.Position.MIDDLE).open();
			} else {
				String detalle = Objects.nonNull(mantenimiento)
						? mantenimiento.getVehiculo().getDominio().concat(" - ").concat(mantenimiento.getTipoMantenimiento().getMantenimientoName())
						: "";
				ConfirmDialog dialog = new ConfirmDialog("Confirmar Borrado",
						"Está seguro de eliminar el registro seleccionado? Mantenimiento: " + detalle, "Borrar",
						confirmEvent -> {
							if (Objects.nonNull(mantenimiento) && maintanceService.deleteMantenimientoByMantenimientoID(mantenimiento.getId())) {
								ViewUtils.notification("Borrado exitoso", "<b>Mantenimiento:</b> " + detalle + "</br>",
										NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE).open();
								refreshGrid();
								saveButton.setText(NEW_MANTENIMIENTO);
							} else { // no es posible borrar el mantenimiento
								ViewUtils
										.notification("Mantenimiento no habilitado para la eliminación", "<b>Mantenimiento:</b> "
												+ (Objects.nonNull(mantenimiento) ? detalle : "<i>No está seleccionado</i>")
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

			Mantenimiento mantenimiento = mantenimientoGrid.asSingleSelect().getValue();
			if (GUARDAR.equals(saveButton.getText())) {
				mantenimiento = maintanceService.getMantenimientoById(mantenimiento.getId());
				if (Objects.nonNull(mantenimiento)) {
					mantenimiento.setVehiculo(vehiculoSingleSelect.getValue());
					mantenimiento.setTipoMantenimiento(tipoMantenimiento.getValue());
					mantenimiento.setDescripcion(descripcion.getValue());
					mantenimiento.setUpdatedAt(new Date());
					maintanceService.updateMantenimiento(mantenimiento);
					popUp.close();
					ViewUtils
							.notification("Mantenimiento Actualizado",
									"<b>Mantenimiento:</b> " + mantenimiento.getVehiculo().getDominio().concat(" - ").concat(mantenimiento.getTipoMantenimiento().getMantenimientoName())
											+ "</br>",
									NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE)
							.open();
					refreshGrid();

				} else {
					LOGGER.error("El Mantenimiento no existe o hay un problema para obtener sus datos");
					ViewUtils.notification("Algo esta mal",
							"El mantenimiento no puede ser cargado, intente refrescando la pagina y volviendo a realizar los cambios, si el error continúa, consulte con el administrador. </br>",
							NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE).open();
				}

			} else {
				if (NEW_MANTENIMIENTO.equals(saveButton.getText())) {
					mantenimiento = new Mantenimiento();
					mantenimiento.setVehiculo(vehiculoSingleSelect.getValue());
					mantenimiento.setTipoMantenimiento(tipoMantenimiento.getValue());
					mantenimiento.setDescripcion(descripcion.getValue());
					mantenimiento.setCreatedAt(new Date());
					mantenimiento.setUpdatedAt(new Date());

					maintanceService.updateMantenimiento(mantenimiento);
					popUp.close();
					refreshGrid();
					ViewUtils
							.notification("Mantenimiento creado",
									"<b>Mantenimiento:</b> " + mantenimiento.getVehiculo().getDominio().concat(" - ").concat(mantenimiento.getTipoMantenimiento().getMantenimientoName())
											+ "</br>",
									NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE)
							.open();
				} else
					ViewUtils.notification("Algo esta mal",
							"El mantenimiento no puede ser cargado, intente refrescando la pagina y volviendo a realizar los cambios, si el error continúa, consulte con el administrador. </br>",
							NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE).open();
			}

		});
		// El boton de refrescar es el encargado de decirle al grid que recargue nuevamente los datos que trae de la
		// base de datos
		refreshButton.addClickListener(event -> refreshGrid());
		//Es lo que hace el boton +
		addButton.addClickListener(event -> {
			crearPopUp();
			editor("Crear Mantenimiento");
			seccionBotones();
			editorDiv.add(buttonDiv);
			popUp.add(editorDiv);
			mantenimientoGrid.asSingleSelect().clear();
			clearForm();
			saveButton.setText(NEW_MANTENIMIENTO);
			popUpOpened();
		});

		// Boton de editar
		editButton.addClickListener(event -> {
			crearPopUp();
			editor("Modificar Mantenimiento");
			seccionBotones();
			editorDiv.add(buttonDiv);
			popUp.add(editorDiv);
			Mantenimiento mantenimiento = mantenimientoGrid.asSingleSelect().getValue();
			if (Objects.nonNull(mantenimiento)) {
				populateForm(mantenimiento);
				saveButton.setText(GUARDAR);
				popUpOpened();
			} else {
				ViewUtils.notification("Seleccionar Mantenimiento",
						"Usted debe seleccionar el mantenimiento y hacer click en el botón de editar si desea editar el mantenimiento."
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

		// vehiculo
		vehiculoSingleSelect = ViewUtils.newBasicConfigComboBoxField("Vehiculo");

		vehiculoSingleSelect.setItems(service.getAllVehiculos());
		vehiculoSingleSelect.setPlaceholder("Ingrese un vehículo");
		vehiculoSingleSelect.setRequired(true);
		// tipo de mantenimiento
		tipoMantenimiento= ViewUtils.newBasicConfigComboBoxField("Tipo de Mantenimiento");
		tipoMantenimiento.setItems(TMantenimiento.values());
		tipoMantenimiento.setPlaceholder("Ingrese tipo de mantenimiento");
		tipoMantenimiento.setItemLabelGenerator(Enum::toString);
		tipoMantenimiento.setRequired(true);
		tipoMantenimiento.setWidthFull();
		tipoMantenimiento.getElement().getClassList().add("full-width");
		descripcion = ViewUtils.newBasicConfigTextField("Descripción");

		//Creacion del titulo del PopUp
		H2 headline = new H2(titulo);
		headline.getStyle().set("margin-top", "0");
		editorDiv.add(headline, vehiculoSingleSelect, tipoMantenimiento, descripcion);
	}
	/* Seteo del titulo a lo que se refiere cada boton al acercar el mouse al boton*/
	private void createButtonLayout(VerticalLayout layout) {
		close.setColor("#BE2123");
		ViewUtils.setButtonAutoMarginAndVariant(saveButton, ButtonVariant.LUMO_TERTIARY);
		ViewUtils.setButtonAutoMarginAndVariant(cancelButton, ButtonVariant.LUMO_ERROR);
		refreshButton.getElement().setProperty("title", "Actualizar Mantenimiento");
		addButton.getElement().setProperty("title", "Agregar Nuevo Mantenimiento");
		editButton.getElement().setProperty("title", "Editar Mantenimiento");
		deleteButton.getElement().setProperty("title", "Borrar Mantenimiento");
		ViewUtils.buttonConfig(layout, addButton, deleteButton, refreshButton, editButton);
	}
	/* Este metodo carga en el PopUp todos los campos del objeto pasado como parametro*/
	private void populateForm(Mantenimiento mantenimiento) {
		mantenimientoBinder.readBean(mantenimiento);
		vehiculoSingleSelect.setValue(mantenimiento.getVehiculo());
		tipoMantenimiento.setValue(mantenimiento.getTipoMantenimiento());
		descripcion.setValue(mantenimiento.getDescripcion());
	}
	/* Abre el PopUp y le setea algunas propiedades */
	private void popUpOpened() {
		popUp.setCloseOnEsc(true);
		popUp.setCloseOnOutsideClick(false);
		popUp.open();
	}
	/* Este metodo se encarga de limpiar los campos del PopUp, por ejemplo el PopUp Crear Mantenimiento*/
	private void clearForm() {
		vehiculoSingleSelect.clear();
		tipoMantenimiento.clear();
		descripcion.clear();
	}
	/* Este metodo es el encargado de refrescar los datos de la grilla cada vez que se modifica, se agrega o se borra
	un dato*/
	private void refreshGrid() {
		dataProvider = new ListDataProvider<>(maintanceService.getAllMantenimientos());
		mantenimientoGrid.setItems(dataProvider);
		mantenimientoGrid.getDataProvider().refreshAll();
		mantenimientoGrid.select(null);
		dataProvider.refreshAll();
	}
	/* Este metodo es el encargado de crear los filtros en la grilla*/
	private void addFiltersToGrid() {
		HeaderRow filterRow = mantenimientoGrid.appendHeaderRow();

		TextField dominioFilter = ViewUtils.createNewFilterForColumnGrid();
		dominioFilter.addValueChangeListener(event -> dataProvider
				.addFilter(mantenimiento -> StringUtils.containsIgnoreCase(mantenimiento.getVehiculo().getDominio(), dominioFilter.getValue())));

		TextField tipoFilter = ViewUtils.createNewFilterForColumnGrid();
		tipoFilter.addValueChangeListener(event -> dataProvider
				.addFilter(mantenimiento -> StringUtils.containsIgnoreCase(mantenimiento.getTipoMantenimiento().getMantenimientoName(), tipoFilter.getValue())));

		ViewUtils.setFilterInColumnGrid(filterRow, dominioFilter, mantenimientoGrid.getColumnByKey("vehiculo"));
		ViewUtils.setFilterInColumnGrid(filterRow, tipoFilter, mantenimientoGrid.getColumnByKey("tipo"));

	}

}
