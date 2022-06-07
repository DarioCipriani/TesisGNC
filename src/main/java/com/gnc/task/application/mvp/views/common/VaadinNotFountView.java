package com.gnc.task.application.mvp.views.common;

import com.gnc.task.application.utilities.ViewUtils;
import com.gnc.task.application.mvp.views.MainLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;

@ParentLayout(MainLayout.class)
public class VaadinNotFountView extends VerticalLayout implements HasErrorParameter<NotFoundException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(VaadinNotFountView.class);

    private final Span explanation;

    public VaadinNotFountView() {
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.START);
        setAlignItems(Alignment.CENTER);
        getStyle().set("padding-top", "8rem");
        Div div = new Div();
        div.setId("center-div");

        VerticalLayout layout = new VerticalLayout();
        layout.addClassName("error-view");
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getThemeList().set("spacing-s", true);
        layout.setAlignItems(Alignment.CENTER);
        layout.setJustifyContentMode(JustifyContentMode.CENTER);

        HorizontalLayout logoLayout = ViewUtils.logoLayout("icons/icon.png");//"icons/icon-96x96.png"
        logoLayout.setSizeFull();
        logoLayout.setPadding(false);
        logoLayout.setSpacing(false);
        logoLayout.getThemeList().set("spacing-s", true);
        logoLayout.setAlignItems(Alignment.CENTER);
        logoLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        layout.add(logoLayout);
        Div divSecondary = new Div();
        divSecondary.setId("center-s-div");
        divSecondary.getStyle().set("text-align", "center");
        divSecondary.add(new H1("La vista no pudo ser encontrada."));
        explanation = new Span();
        divSecondary.add(explanation);
        layout.add(divSecondary);

        div.add(layout);
        add(div);
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
        LOGGER.info(parameter.hasCustomMessage() ? parameter.getCustomMessage() : "La ruta no fue encontrada",
                parameter.getCaughtException());
        explanation.setText("No es posible navegar a '" + event.getLocation().getPath() + "'.");
        return HttpServletResponse.SC_NOT_FOUND;
    }
}
