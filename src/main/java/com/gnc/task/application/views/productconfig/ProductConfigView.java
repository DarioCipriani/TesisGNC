package com.gnc.task.application.views.productconfig;

import com.gnc.task.application.data.Role;
import com.gnc.task.application.data.entity.Producto;
import com.gnc.task.application.data.entity.User;
import com.gnc.task.application.data.service.ProductService;
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

@PageTitle("Producto")
@Route(value = "productos", layout = MainLayout.class)
@RolesAllowed("admin")
public class ProductConfigView extends HorizontalLayout {

    public static final String NEW_PRODUCTO = "Nuevo Producto";
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
    // creacion de iconos para la ventana de creacion de Producto
    private final Icon check = new Icon(VaadinIcon.CHECK);
    private final Icon close = new Icon(VaadinIcon.CLOSE);
    // creacion de botones para la ventana de creacion de producto
    private final Button saveButton = new Button(GUARDAR, check);
    private final Button cancelButton = new Button("Cancelar", close);
    //campos que pertenecen a la fila (grid) de productos en la ventana de productos
    private Grid<Producto> productoGrid;
    private TextField codigo;
    private TextField nombre;
    private TextField descripcion;
    private IntegerField cantidad;
    private BigDecimalField precio;
    // Binder es el encargado de enlazar los campos de front con los campos de la base de datos
    private Binder<Producto> productoBinder;
    private ListDataProvider<Producto> dataProvider;
    private Dialog popUp;
    private FormLayout editorDiv;
    private HorizontalLayout buttonDiv;
    private AuthenticatedUser authenticatedUser;
    private ProductService productService;

    public ProductConfigView(AuthenticatedUser authenticatedUser, ProductService productService) {
        this.authenticatedUser = authenticatedUser;
        this.productService = productService;
        Optional<User> userLogged = authenticatedUser.get();
        if (userLogged.isPresent() && userLogged.get().getRoles().contains(Role.ADMIN)) {
            setSizeFull();
            crearPopUp();
            editor("");
            seccionBotones();
            editorDiv.add(buttonDiv);
            popUp.add(editorDiv);
            // creacion de la fila (Grid) con el encabezado con cada uno de los campos de producto
            productoGrid = new Grid<>();
            productoGrid.setSizeFull();
            productoGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
            productoGrid.addThemeName("grid-selection-theme");
            productoGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.MATERIAL_COLUMN_DIVIDERS);
            productoGrid.addColumn(Producto::getCodigo).setHeader("Código").setResizable(true).setKey("codigo")
                    .setSortable(true);
            productoGrid.addColumn(Producto::getNombre).setHeader("Nombre").setResizable(true).setKey("nombre")
                    .setSortable(true);
            productoGrid.addColumn(Producto::getDescripcion).setHeader("Descripción").setResizable(true).setKey("descripcion").setSortable(true);
            productoGrid.addColumn(Producto::getCantidad).setHeader("Cantidad").setResizable(true).setKey("cantidad")
                    .setSortable(true);
            productoGrid.addColumn(Producto::getPrecio).setHeader("Precio").setResizable(true).setKey("precio")
                    .setSortable(true);

            productoBinder = new Binder<>(Producto.class);
            productoBinder.bind(codigo, Producto::getCodigo, Producto::setCodigo);
            productoBinder.bind(nombre, Producto::getNombre, Producto::setNombre);
            productoBinder.bind(descripcion, Producto::getDescripcion, Producto::setDescripcion);
            productoBinder.bind(cantidad, Producto::getCantidad, Producto::setCantidad);
            productoBinder.bind(precio, Producto::getPrecio, Producto::setPrecio);

            productoBinder.bindInstanceFields(this);

            dataProvider = new ListDataProvider<>(productService.getAllProducts());
            productoGrid.setDataProvider(dataProvider);

            addFiltersToGrid();
            //Cada vez que se selecciona un elemento de la grilla si el evento no tiene valor (por ejemplo el Crear
            // Producto) entonces seteamos el boton de guardar con el texto "Nuevo Producto", mientras que si el evento
            // tiene valor (por ejemplo el Modificar Producto) entonces seteamos el boton con el texto "Guardar"
            productoGrid.asSingleSelect().addValueChangeListener(event -> {
                if (event.getValue() != null) {
                    saveButton.setText(GUARDAR);
                } else {
                    productoGrid.getDataProvider().refreshAll();
                    saveButton.setText(NEW_PRODUCTO);
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
        wrapper.add(productoGrid);
    }

    /*Metodo encargado de decir a cada boton que es lo que tiene que hacer*/
    private void addButtonListeners() {
        cancelButton.addClickListener(e -> {
            refreshGrid();
            popUp.close();
        });

        deleteButton.addClickListener(e -> {
            Producto producto = productoGrid.asSingleSelect().getValue();
            if (Objects.isNull(producto)) {
                ViewUtils.notification("Seleccionar producto",
                        "Usted debe seleccionar el producto y hacer click en el botón de eliminar si desea eliminar el producto."
                                + "</br>",
                        NotificationVariant.LUMO_PRIMARY, Notification.Position.MIDDLE).open();
            } else {
                String detalle = Objects.nonNull(producto)
                        ? producto.getCodigo().concat(" - ").concat(producto.getNombre())
                        : "";
                ConfirmDialog dialog = new ConfirmDialog("Confirmar Borrado",
                        "Está seguro de eliminar el registro seleccionado? Producto: " + detalle, "Borrar",
                        confirmEvent -> {
                            if (Objects.nonNull(producto) && productService.deleteProductByProductID(producto.getId())) {
                                ViewUtils.notification("Borrado exitoso", "<b>Producto:</b> " + detalle + "</br>",
                                        NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE).open();
                                refreshGrid();
                                saveButton.setText(NEW_PRODUCTO);
                            } else { // no es posible borrar el producto
                                ViewUtils
                                        .notification("Producto no habilitado para la eliminación", "<b>Producto:</b> "
                                                + (Objects.nonNull(producto) ? detalle : "<i>No está seleccionado</i>")
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

            Producto producto = productoGrid.asSingleSelect().getValue();
            if (GUARDAR.equals(saveButton.getText())) {
                producto = productService.getProductByCodigo(producto.getCodigo());
                if (Objects.nonNull(producto)) {
                    producto.setCodigo(codigo.getValue());
                    producto.setNombre(nombre.getValue());
                    producto.setDescripcion(descripcion.getValue());
                    producto.setCantidad(cantidad.getValue());
                    producto.setPrecio(precio.getValue());
                    producto.setUpdatedAt(new Date());
                    try {
                        productService.updateProduct(producto);
                        popUp.close();
                        ViewUtils
                                .notification("Producto Actualizado",
                                        "<b>Producto:</b> " + producto.getCodigo().concat(" - ").concat(producto.getNombre())
                                                + "</br>",
                                        NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE)
                                .open();
                        refreshGrid();

                    } catch (Exception exception) {
                        ViewUtils
                                .notification("Producto No Actualizado",
                                        "<b>Causa:</b> " + exception.getCause() + "</br>",
                                        NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE)
                                .open();
                    }

                } else {
                    LOGGER.error("El Producto no existe o hay un problema para obtener sus datos");
                    ViewUtils.notification("Algo esta mal",
                            "El producto no puede ser cargado, intente refrescando la página y volviendo a realizar los cambios, si el error continúa, consulte con el administrador. </br>",
                            NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE).open();
                }

            } else {
                if (NEW_PRODUCTO.equals(saveButton.getText())) {
                    producto = new Producto();
                    producto.setCodigo(codigo.getValue());
                    producto.setNombre(nombre.getValue());
                    producto.setDescripcion(descripcion.getValue());
                    producto.setCantidad(cantidad.getValue());
                    producto.setPrecio(precio.getValue());
                    producto.setCreatedAt(new Date());
                    producto.setUpdatedAt(new Date());
                    try {
                        productService.updateProduct(producto);
                        popUp.close();
                        refreshGrid();
                        ViewUtils
                                .notification("Producto creado",
                                        "<b>Producto:</b> " + producto.getCodigo().concat(" - ").concat(producto.getNombre())
                                                + "</br>",
                                        NotificationVariant.LUMO_SUCCESS, Notification.Position.MIDDLE)
                                .open();
                    } catch (Exception exception) {
                        ViewUtils
                                .notification("Producto No creado",
                                        "<b>Causa:</b> " + exception.getCause() + "</br>",
                                        NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE)
                                .open();
                    }
                } else
                    ViewUtils.notification("Algo esta mal",
                            "El producto no puede ser cargado, intente refrescando la pagina y volviendo a realizar los cambios, si el error continúa, consulte con el administrador. </br>",
                            NotificationVariant.LUMO_ERROR, Notification.Position.MIDDLE).open();
            }

        });
        // El boton de refrescar es el encargado de decirle al grid que recargue nuevamente los datos que trae de la
        // base de datos
        refreshButton.addClickListener(event -> refreshGrid());
        //Es lo que hace el boton +
        addButton.addClickListener(event -> {
            crearPopUp();
            editor("Crear Producto");
            seccionBotones();
            editorDiv.add(buttonDiv);
            popUp.add(editorDiv);
            productoGrid.asSingleSelect().clear();
            clearForm();
            saveButton.setText(NEW_PRODUCTO);
            popUpOpened();
        });

        // Boton de editar
        editButton.addClickListener(event -> {
            crearPopUp();
            editor("Modificar Producto");
            seccionBotones();
            editorDiv.add(buttonDiv);
            popUp.add(editorDiv);
            Producto producto = productoGrid.asSingleSelect().getValue();
            if (Objects.nonNull(producto)) {
                populateForm(producto);
                saveButton.setText(GUARDAR);
                popUpOpened();
            } else {
                ViewUtils.notification("Seleccionar Producto",
                        "Usted debe seleccionar el producto y hacer click en el botón de editar si desea editar el producto."
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
        codigo = ViewUtils.newBasicConfigTextField("Código");
        nombre = ViewUtils.newBasicConfigTextField("Nombre");
        descripcion = ViewUtils.newBasicConfigTextField("Descripción");
        cantidad = ViewUtils.newBasicConfigIntegerField("Cantidad");
        precio = ViewUtils.newBasicConfigBigDecimalField("Precio");

        //Creacion del titulo del PopUp
        H2 headline = new H2(titulo);
        headline.getStyle().set("margin-top", "0");
        editorDiv.add(headline, codigo, nombre, descripcion, cantidad, precio);
    }

    /* Seteo del titulo a lo que se refiere cada boton al acercar el mouse al boton*/
    private void createButtonLayout(VerticalLayout layout) {
        close.setColor("#BE2123");
        ViewUtils.setButtonAutoMarginAndVariant(saveButton, ButtonVariant.LUMO_TERTIARY);
        ViewUtils.setButtonAutoMarginAndVariant(cancelButton, ButtonVariant.LUMO_ERROR);
        refreshButton.getElement().setProperty("title", "Actualizar Productos");
        addButton.getElement().setProperty("title", "Agregar Nuevo Producto");
        editButton.getElement().setProperty("title", "Editar Producto");
        deleteButton.getElement().setProperty("title", "Borrar Producto");
        ViewUtils.buttonConfig(layout, addButton, deleteButton, refreshButton, editButton);
    }

    /* Este metodo carga en el PopUp todos los campos del objeto pasado como parametro*/
    private void populateForm(Producto producto) {
        productoBinder.readBean(producto);
        codigo.setValue(producto.getCodigo());
        nombre.setValue(producto.getNombre());
        descripcion.setValue(producto.getDescripcion());
        cantidad.setValue(producto.getCantidad());
        precio.setValue(producto.getPrecio());
    }

    /* Abre el PopUp y le setea algunas propiedades */
    private void popUpOpened() {
        popUp.setCloseOnEsc(true);
        popUp.setCloseOnOutsideClick(false);
        popUp.open();
    }

    /* Este metodo se encarga de limpiar los campos del PopUp, por ejemplo el PopUp Crear Producto*/
    private void clearForm() {
        codigo.clear();
        nombre.clear();
        descripcion.clear();
        cantidad.clear();
        precio.clear();
    }

    /* Este metodo es el encargado de refrescar los datos de la grilla cada vez que se modifica, se agrega o se borra
    un dato*/
    private void refreshGrid() {
        dataProvider = new ListDataProvider<>(productService.getAllProducts());
        productoGrid.setItems(dataProvider);
        productoGrid.getDataProvider().refreshAll();
        productoGrid.select(null);
        dataProvider.refreshAll();
    }

    /* Este metodo es el encargado de crear los filtros en la grilla*/
    private void addFiltersToGrid() {
        HeaderRow filterRow = productoGrid.appendHeaderRow();

        TextField codigoFilter = ViewUtils.createNewFilterForColumnGrid();
        codigoFilter.addValueChangeListener(event -> dataProvider
                .addFilter(producto -> StringUtils.containsIgnoreCase(producto.getCodigo(), codigoFilter.getValue())));

        TextField nombreFilter = ViewUtils.createNewFilterForColumnGrid();
        nombreFilter.addValueChangeListener(event -> dataProvider
                .addFilter(producto -> StringUtils.containsIgnoreCase(producto.getNombre(), nombreFilter.getValue())));

        ViewUtils.setFilterInColumnGrid(filterRow, codigoFilter, productoGrid.getColumnByKey("codigo"));
        ViewUtils.setFilterInColumnGrid(filterRow, nombreFilter, productoGrid.getColumnByKey("nombre"));

    }

}
