package com.gnc.task.application.utilities;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ViewUtils {
    public static HorizontalLayout logoLayout(String imgRoute) {
        HorizontalLayout logoLayout = new HorizontalLayout();
        logoLayout.setId("logo");
        logoLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        logoLayout.add(new Image(imgRoute, "Migration Task logo"));
        logoLayout.add(new H1("Migration Task"));
        return logoLayout;
    }

    public static Paragraph getTitleValue(String title, String value) {
        Span spanTitle = new Span(title);
        spanTitle.getStyle().set("font-weight", "bold");
        Span spanValue = new Span(": " + value);
        return new Paragraph(spanTitle, spanValue);
    }

    public static Notification notification(String title, String htmlMessage, NotificationVariant variant,
                                            Notification.Position position) {
        Notification notification = new Notification();
        notification.getElement().getStyle().set("height", "100%");
        notification.addThemeVariants(variant);
        Div notificationDiv = new Div();
        notificationDiv.setId("div-notification");
        notificationDiv.setSizeFull();
        H4 titleLabel = new H4(title);
        titleLabel.getStyle().set("color", "#FFFFFF");
        titleLabel.getStyle().set("margin-top", "1%");
        titleLabel.setWidthFull();
        notificationDiv.add(titleLabel);
        Span label = new Span();
        label.setWidthFull();
        label.getElement().setProperty("innerHTML", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + htmlMessage);
        Button okButton = new Button("OK", VaadinIcon.ENVELOPE_OPEN.create());
        okButton.setSizeFull();
        okButton.getElement().getStyle().set("margin-right", "2%");
        okButton.addClickListener(e -> {
            notification.close();
        });
        okButton.addClickShortcut(Key.ENTER);
        okButton.addClickShortcut(Key.ESCAPE);
        okButton.setAutofocus(true);
        notification.setPosition(position);
        notificationDiv.add(label);
        notificationDiv.add(okButton);
        notification.add(notificationDiv);
        return notification;
    }

    public static TextField newBasicConfigTextField(String name) {
        TextField value = new TextField(name);
        value.setWidthFull();
        return value;
    }

    public static PasswordField newBasicConfigPasswordField(String name) {
        PasswordField value = new PasswordField(name);
        value.setWidthFull();
        value.setPlaceholder("Enter password...");
        return value;
    }

    public static EmailField newBasicConfigEmailField(String name) {
        EmailField value = new EmailField(name);
        value.setWidthFull();
        value.setPlaceholder("Enter email...");
        value.setErrorMessage("Please enter a valid email address.");
        value.setInvalid(true);
        return value;
    }

    public static VerticalLayout newBasicVerticalLayout(TextField... columns) {
        VerticalLayout formLayout = new VerticalLayout();
        formLayout.add(columns);
        return formLayout;
    }

    public static Details newBasicDetailLayout(String detailName, Boolean opened, TextField... columns) {
        VerticalLayout formLayout = newBasicVerticalLayout(columns);
        Details component = new Details(detailName, formLayout);
        component.setOpened(opened);
        return component;
    }

    public static <T> List<String> gridReziseColumns(Grid<T> grid) {
        List<String> columns = new ArrayList<>();
        grid.getColumns().forEach(column -> {
            columns.add(column.getKey());
            column.setResizable(true);
            Element parent = column.getElement().getParent();
            while (parent != null && "vaadin-grid-column-group".equals(parent.getTag())) {
                parent.setProperty("resizable", "true");
                parent = parent.getParent();
            }
        });
        return columns;
    }

    public static void setButtonAutoMarginAndVariant(Button button, ButtonVariant variant) {
        button.getElement().getStyle().set("margin-left", "auto");
        button.getElement().getStyle().set("margin-right", "auto");
        button.addThemeVariants(variant);
    }

    public static void buttonConfig(VerticalLayout layout, Button addButton, Button deleteButton,
                                    Button refreshButton, Button editButton) {
        Div buttons = new Div();
        buttons.setId("div-buttons");
        buttons.setHeight("5%");
        buttons.add(refreshButton, addButton, editButton, deleteButton);
        layout.add(buttons);
    }

    public static TextField createNewFilterForColumnGrid() {
        TextField filter = new TextField();
        filter.setPlaceholder("Filter");
        filter.setClearButtonVisible(true);
        filter.setWidthFull();
        filter.setValueChangeMode(ValueChangeMode.EAGER);
        return filter;
    }

    public static <T> void setFilterInColumnGrid(HeaderRow filterRow, TextField filter, Grid.Column<T> column) {
        filterRow.getCell(column).setComponent(filter);
    }
}
