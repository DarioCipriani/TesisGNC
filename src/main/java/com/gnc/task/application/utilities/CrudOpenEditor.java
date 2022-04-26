package com.gnc.task.application.utilities;

import com.gnc.task.application.data.entity.Cliente;
import com.gnc.task.application.data.entity.Pertenece;
import com.gnc.task.application.data.entity.Vehiculo;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.crud.BinderCrudEditor;
import com.vaadin.flow.component.crud.Crud;
import com.vaadin.flow.component.crud.CrudEditor;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import sun.jvm.hotspot.oops.BooleanField;

import java.util.Arrays;
import java.util.List;

@Route("crud-open-editor")
public class CrudOpenEditor extends Div {

  private Crud<Pertenece> crud;
  //Vehiculo
  private String DOMINIO = "dominio";
  private String MARCA = "marca";
  private String MODELO = "modelo";
  //Cliente
  private String NOMBRE = "nombre";
  private String APELLIDO = "apellido";
  private String DNI = "dni";
  //Pertenece
  private String PERTENECEN = "pertenecen";



  public CrudOpenEditor() {
    crud = new Crud<>(
      Pertenece.class,
      createEditor()
    );

    setupGrid();
    setupDataProvider();

    add(crud);
  }

  private CrudEditor<Pertenece> createEditor() {
    ComboBox<Vehiculo> vehiculoComboBox = ViewUtils.newBasicConfigComboBoxField("Vehículo");;
    ComboBox<Cliente> clienteComboBox = ViewUtils.newBasicConfigComboBoxField("Cliente");;
    TextField pertenecen = new TextField("Pertenecen");
    FormLayout form = new FormLayout(clienteComboBox, vehiculoComboBox, pertenecen);

    Binder<Pertenece> binder = new Binder<>(Pertenece.class);
    binder.forField(vehiculoComboBox).asRequired().bind(Pertenece::getVehiculo, Pertenece::setVehiculo);
    binder.forField(clienteComboBox).asRequired().bind(Pertenece::getCliente, Pertenece::setCliente);
   // binder.forField(pertenecen).asRequired().bind(Pertenece::getPertenecen(),Pertenece::setPertenecen);

    return new BinderCrudEditor<>(binder, form);
  }

  private void setupGrid() {
    // tag::snippet[]
    Grid<Pertenece> grid = crud.getGrid();

    // Remove edit column
    Crud.removeEditColumn(grid);
    // grid.removeColumnByKey(EDIT_COLUMN);
    // grid.removeColumn(grid.getColumnByKey(EDIT_COLUMN));

    // Open editor on double click
    grid.addItemDoubleClickListener(event ->
      crud.edit(event.getItem(), Crud.EditMode.EXISTING_ITEM)
    );
    // end::snippet[]

    // Only show these columns (all columns shown by default):
    List<String> visibleColumns = Arrays.asList(
            NOMBRE,
            APELLIDO,
            DNI,
            DOMINIO,
            MARCA,
            MODELO,
            PERTENECEN
    );
    grid.getColumns().forEach(column -> {
      String key = column.getKey();
      if (!visibleColumns.contains(key)) {
        grid.removeColumn(column);
      }
    });

    // Reorder the columns (alphabetical by default)
  /*  grid.setColumnOrder(
      grid.getColumnByKey(DNI),
      grid.getColumnByKey(DOMINIO),
      grid.getColumnByKey(PERTENECEN)
    );*/
  }

  private void setupDataProvider() {
   /* PersonDataProvider dataProvider = new PersonDataProvider();
    crud.setDataProvider(dataProvider);
    crud.addDeleteListener(deleteEvent ->
      dataProvider.delete(deleteEvent.getItem())
    );
    crud.addSaveListener(saveEvent ->
      dataProvider.persist(saveEvent.getItem())
    );*/
  }
}
