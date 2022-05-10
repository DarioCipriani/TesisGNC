package com.gnc.task.application.views.fallasconfig;

import com.gnc.task.application.data.Role;
import com.gnc.task.application.data.entity.Falla;
import com.gnc.task.application.data.entity.User;
import com.gnc.task.application.data.service.FailService;
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

@PageTitle("Fallas")
@Route(value = "fallas", layout = MainLayout.class)
@RolesAllowed("admin")
public class FailConfigView extends HorizontalLayout {

	public static final String NEW_FAIL = "Nueva Falla";
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
	// creacion de iconos para la ventana de creacion de Fallas
	private final Icon check = new Icon(VaadinIcon.CHECK);
	private final Icon close = new Icon(VaadinIcon.CLOSE);
	// creacion de botones para la ventana de creacion de producto
	private final Button saveButton = new Button(GUARDAR, check);
	private final Button cancelButton = new Button("Cancelar", close);
	//campos que pertenecen a la fila (grid) de fallas en la ventana de fallas
	private Grid<Falla> fallaGrid;
	private TextField nombre;
	private TextField marcaVehiculo;
	private TextField modeloVehiculo;
	private TextField añoVehiculo;
	private TextField descripcionFalla;
	private TextField descripcionSolucion;
	// Binder es el encargado de enlazar los campos de front con los campos de la base de datos
	private Binder<Falla> fallaBinder;
	private ListDataProvider<Falla> dataProvider;
	private Dialog popUp;
	private FormLayout editorDiv;
	private HorizontalLayout buttonDiv;
	private AuthenticatedUser authenticatedUser;
	private FailService failService;

	public FailConfigView(AuthenticatedUser authenticatedUser, FailService failService) {
		this.authenticatedUser = authenticatedUser;
		this.failService = failService;
		Optional<User> userLogged = authenticatedUser.get();
		if (userLogged.isPresent() && userLogged.get().getRoles().contains(Role.ADMIN)) {
			setSizeFull();
			crearPopUp();
			editor("");
			seccionBotones();
			editorDiv.add(buttonDiv);
			popUp.add(editorDiv);
			// creacion de la fila (Grid) con el encabezado con cada uno de los campos de las fallas
			fallaGrid = new Grid<>();
			fallaGrid.setSizeFull();
			fallaGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
			fallaGrid.addThemeName("grid-selection-theme");
			fallaGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.MATERIAL_COLUMN_DIVIDERS);
			fallaGrid.addColumn(Falla::getNombre).setHeader("Nombre").setResizable(true).setKey("nombre")
					.setSortable(true);
			fallaGrid.addColumn(Falla::getMarcaVehiculo).setHeader("Marca Vehículo").setResizable(true).setKey("marcaVehiculo")
					.setSortable(true);
			fallaGrid.addColumn(Falla::getModeloVehiculo).setHeader("Modelo Vehículo").setResizable(true).setKey("modeloVehiculo").setSortable(true);
			fallaGrid.addColumn(Falla::getAñoVehiculo).setHeader("Año Vehículo").setResizable(true).setKey("añoVehiculo")
					.setSortable(true);
			fallaGrid.addColumn(Falla::getDescripcionFalla).setHeader("Descripción Falla").setResizable(true).setKey("descripcionFalla")
					.setSortable(true);
			fallaGrid.addColumn(Falla::getDescripcionSolucion).setHeader("Descripción Solución").setResizable(true).setKey("descripcionSolucion")
					.setSortable(true);

			fallaBinder = new Binder<>(Falla.class);
			fallaBinder.bind(nombre, Falla::getNombre, Falla::setNombre);
			fallaBinder.bind(marcaVehiculo, Falla::getMarcaVehiculo, Falla::setMarcaVehiculo);
			fallaBinder.bind(modeloVehiculo, Falla::getModeloVehiculo, Falla::setModeloVehiculo);
			fallaBinder.bind(añoVehiculo, Falla::getAñoVehiculo, Falla::setAñoVehiculo);
			fallaBinder.bind(descripcionFalla, Falla::getDescripcionFalla, Falla::setDescripcionFalla);
			fallaBinder.bind(descripcionSolucion, Falla::getDescripcionSolucion, Falla::setDescripcionSolucion);

			fallaBinder.bindInstanceFields(this);

			dataProvider = new ListDataProvider<>(failService.getAllFails());
			fallaGrid.setDataProvider(dataProvider);

			addFiltersToGrid();
			//Cada vez que se selecciona un elemento de la grilla si el evento no tiene valor (por ejemplo el Crear
			// Falla) entonces seteamos el boton de guardar con el texto "Nueva Falla", mientras que si el evento
			// tiene valor (por ejemplo el Modificar Falla) entonces seteamos el boton con el texto "Guardar"
			fallaGrid.asSingleSelect().addValueChangeListener(event -> {
				if (event.getValue() != null) {
					saveButton.setText(GUARDAR);
				} else {
					fallaGrid.getDataProvider().refreshAll();
					saveButton.setText(NEW_FAIL);
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
		wrapper.add(fallaGrid);
	}
	/*Metodo encargado de decir a cada boton que es lo que tiene que hacer*/
	private void addButtonListeners() {
		cancelButton.addClickListener(e -> {
			refreshGrid();
			popUp.close();
		});

		deleteButton.addClickListener(e -> {
			Falla falla = fallaGrid.asSingleSelect().getValue();
			if (Objects.isNull(falla)) {
				ViewUtils.notification("Seleccionar falla",
						"Usted debe seleccionar la falla y hacer click en el botón de eliminar si desea eliminar la falla."
								+ "</br>",
						NotificationVariant.LUMO_PRIMARY, Notification.Position.MIDDLE).open();
			} else {
				String detalle = Objects.nonNull(falla)
						? falla.getNombre().concat(" - ").concat(falla.getDescripcionFalla())
						: "";
				ConfirmDialog dialog = new ConfirmDialog("Confirmar Borrado",
						"Está seguro de eliminar el registro seleccionado? Falla: " + detalle, "Borrar",
						confirmEvent -> {
							if (Objects.nonNull(falla) && failService.deleteFailByFailID(falla.getId())) {
								ViewUtils.notification("Borrado exitoso", "<b>Falla:</b> " + detalle + "</br>",
										NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE).open();
								refreshGrid();
								saveButton.setText(NEW_FAIL);
							} else { // no es posible borrar la falla
								ViewUtils
										.notification("Falla no habilitada para la eliminación", "<b>Falla:</b> "
												+ (Objects.nonNull(falla) ? detalle : "<i>No está seleccionada</i>")
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

			Falla falla = fallaGrid.asSingleSelect().getValue();
			if (GUARDAR.equals(saveButton.getText())) {
				falla = failService.getFallaByNombre(falla.getNombre());
				if (Objects.nonNull(falla)) {
					falla.setNombre(nombre.getValue());
					falla.setMarcaVehiculo(marcaVehiculo.getValue());
					falla.setModeloVehiculo(modeloVehiculo.getValue());
					falla.setAñoVehiculo(añoVehiculo.getValue());
					falla.setDescripcionFalla(descripcionFalla.getValue());
					falla.setDescripcionSolucion(descripcionSolucion.getValue());
					falla.setUpdatedAt(new Date());

					try{
						failService.updateFalla(falla);
						popUp.close();
						ViewUtils
								.notification("Falla Actualizada",
										"<b>Falla:</b> " + falla.getNombre().concat(" - ").concat(falla.getDescripcionFalla())
												+ "</br>",
										NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE)
								.open();
						refreshGrid();
					}catch (Exception exception){
						ViewUtils
								.notification("Falla No Actualizada",
										"<b>Causa:</b> " + exception.getCause() + "</br>",
										NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE)
								.open();
					}

				} else {
					LOGGER.error("La Falla no existe o hay un problema para obtener sus datos");
					ViewUtils.notification("Algo esta mal",
							"La Falla no puede ser cargada, intente refrescando la página y volviendo a realizar los cambios, si el error continúa, consulte con el administrador. </br>",
							NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE).open();
				}

			} else {
				if (NEW_FAIL.equals(saveButton.getText())) {
					falla = new Falla();
					falla.setNombre(nombre.getValue());
					falla.setMarcaVehiculo(marcaVehiculo.getValue());
					falla.setModeloVehiculo(modeloVehiculo.getValue());
					falla.setAñoVehiculo(añoVehiculo.getValue());
					falla.setDescripcionFalla(descripcionFalla.getValue());
					falla.setDescripcionSolucion(descripcionSolucion.getValue());
					falla.setCreatedAt(new Date());
					falla.setUpdatedAt(new Date());

					try{
						failService.updateFalla(falla);
						popUp.close();
						refreshGrid();
						ViewUtils
								.notification("Falla Creada",
										"<b>Falla:</b> " + falla.getNombre().concat(" - ").concat(falla.getDescripcionFalla())
												+ "</br>",
										NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE)
								.open();
					}catch (Exception exception){
						ViewUtils
								.notification("Falla No creada",
										"<b>Causa:</b> " + exception.getCause() + "</br>",
										NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE)
								.open();
					}

				} else
					ViewUtils.notification("Algo esta mal",
							"La Falla no puede ser cargada, intente refrescando la pagina y volviendo a realizar los cambios, si el error continúa, consulte con el administrador. </br>",
							NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE).open();
			}

		});
		// El boton de refrescar es el encargado de decirle al grid que recargue nuevamente los datos que trae de la
		// base de datos
		refreshButton.addClickListener(event -> refreshGrid());
		//Es lo que hace el boton +
		addButton.addClickListener(event -> {
			crearPopUp();
			editor("Crear Falla");
			seccionBotones();
			editorDiv.add(buttonDiv);
			popUp.add(editorDiv);
			fallaGrid.asSingleSelect().clear();
			clearForm();
			saveButton.setText(NEW_FAIL);
			popUpOpened();
		});

		// Boton de editar
		editButton.addClickListener(event -> {
			crearPopUp();
			editor("Modificar Falla");
			seccionBotones();
			editorDiv.add(buttonDiv);
			popUp.add(editorDiv);
			Falla falla = fallaGrid.asSingleSelect().getValue();
			if (Objects.nonNull(falla)) {
				populateForm(falla);
				saveButton.setText(GUARDAR);
				popUpOpened();
			} else {
				ViewUtils.notification("Seleccionar la Falla",
						"Usted debe seleccionar la falla y hacer click en el botón de editar si desea editar la falla."
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
		marcaVehiculo = ViewUtils.newBasicConfigTextField("Marca Vehículo");
		modeloVehiculo = ViewUtils.newBasicConfigTextField("Modelo Vehículo");
		añoVehiculo = ViewUtils.newBasicConfigTextField("Año Vehículo");
		descripcionFalla = ViewUtils.newBasicConfigTextField("Descripción Falla");
		descripcionSolucion = ViewUtils.newBasicConfigTextField("Descripción Solución");

		//Creacion del titulo del PopUp
		H2 headline = new H2(titulo);
		headline.getStyle().set("margin-top", "0");
		editorDiv.add(headline, nombre, marcaVehiculo, modeloVehiculo, añoVehiculo, descripcionFalla, descripcionSolucion);
	}
	/* Seteo del titulo a lo que se refiere cada boton al acercar el mouse al boton*/
	private void createButtonLayout(VerticalLayout layout) {
		close.setColor("#BE2123");
		ViewUtils.setButtonAutoMarginAndVariant(saveButton, ButtonVariant.LUMO_TERTIARY);
		ViewUtils.setButtonAutoMarginAndVariant(cancelButton, ButtonVariant.LUMO_ERROR);
		refreshButton.getElement().setProperty("title", "Actualizar Falla");
		addButton.getElement().setProperty("title", "Agregar Nueva Falla");
		editButton.getElement().setProperty("title", "Editar Falla");
		deleteButton.getElement().setProperty("title", "Borrar Falla");
		ViewUtils.buttonConfig(layout, addButton, deleteButton, refreshButton, editButton);
	}
	/* Este metodo carga en el PopUp todos los campos del objeto pasado como parametro*/
	private void populateForm(Falla falla) {
		fallaBinder.readBean(falla);
		nombre.setValue(falla.getNombre());
		marcaVehiculo.setValue(falla.getMarcaVehiculo());
		modeloVehiculo.setValue(falla.getModeloVehiculo());
		añoVehiculo.setValue(falla.getAñoVehiculo());
		descripcionFalla.setValue(falla.getDescripcionFalla());
		descripcionSolucion.setValue(falla.getDescripcionSolucion());
	}
	/* Abre el PopUp y le setea algunas propiedades */
	private void popUpOpened() {
		popUp.setCloseOnEsc(true);
		popUp.setCloseOnOutsideClick(false);
		popUp.open();
	}
	/* Este metodo se encarga de limpiar los campos del PopUp, por ejemplo el PopUp Crear Falla*/
	private void clearForm() {
		nombre.clear();
		marcaVehiculo.clear();
		modeloVehiculo.clear();
		añoVehiculo.clear();
		descripcionFalla.clear();
		descripcionSolucion.clear();
	}
	/* Este metodo es el encargado de refrescar los datos de la grilla cada vez que se modifica, se agrega o se borra
	un dato*/
	private void refreshGrid() {
		dataProvider = new ListDataProvider<>(failService.getAllFails());
		fallaGrid.setItems(dataProvider);
		fallaGrid.getDataProvider().refreshAll();
		fallaGrid.select(null);
		dataProvider.refreshAll();
	}
	/* Este metodo es el encargado de crear los filtros en la grilla*/
	private void addFiltersToGrid() {
		HeaderRow filterRow = fallaGrid.appendHeaderRow();

		TextField nombreFilter = ViewUtils.createNewFilterForColumnGrid();
		nombreFilter.addValueChangeListener(event -> dataProvider
				.addFilter(falla -> StringUtils.containsIgnoreCase(falla.getNombre(), nombreFilter.getValue())));

		TextField marcaFilter = ViewUtils.createNewFilterForColumnGrid();
		marcaFilter.addValueChangeListener(event -> dataProvider
				.addFilter(falla -> StringUtils.containsIgnoreCase(falla.getMarcaVehiculo(), marcaFilter.getValue())));

		TextField modeloFilter = ViewUtils.createNewFilterForColumnGrid();
		modeloFilter.addValueChangeListener(event -> dataProvider
				.addFilter(falla -> StringUtils.containsIgnoreCase(falla.getModeloVehiculo(), modeloFilter.getValue())));

		ViewUtils.setFilterInColumnGrid(filterRow, nombreFilter, fallaGrid.getColumnByKey("nombre"));
		ViewUtils.setFilterInColumnGrid(filterRow, marcaFilter, fallaGrid.getColumnByKey("marcaVehiculo"));
		ViewUtils.setFilterInColumnGrid(filterRow, modeloFilter, fallaGrid.getColumnByKey("modeloVehiculo"));

	}

}
