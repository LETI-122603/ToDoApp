package com.example.implementacao_pdf.ui;

import com.example.base.ui.component.ViewToolbar;
import com.example.implementacao_pdf.Pdf;
import com.example.implementacao_pdf.PdfService;
import com.example.implementacao_pdf.PdfService.DueFilter;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Optional;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route("pdf-prints")
@PageTitle("PDF Prints")
@Menu(order = 1, icon = "vaadin:print", title = "PDF Prints")
class PdfListView extends Main {

    private final PdfService pdfService;

    // Create
    final TextField nameField;
    final DatePicker dueDateField;
    final ComboBox<Pdf.Status> statusField;
    final Button createBtn;

    // Filtros
    final TextField searchField;
    final ComboBox<DueFilter> dueFilterBox;
    final ComboBox<Pdf.Status> statusFilterBox;
    final Span totalLabel;

    // Grid
    final Grid<Pdf> pdfGrid;

    PdfListView(PdfService pdfService) {
        this.pdfService = pdfService;

        // ---- Create controls
        nameField = new TextField();
        nameField.setPlaceholder("PDF name");
        nameField.setAriaLabel("PDF name");
        nameField.setMaxLength(100);
        nameField.setMinWidth("20em");

        dueDateField = new DatePicker();
        dueDateField.setPlaceholder("Due date");
        dueDateField.setAriaLabel("Due date");

        statusField = new ComboBox<>();
        statusField.setPlaceholder("Status");
        statusField.setItems(Pdf.Status.values());
        statusField.setItemLabelGenerator(this::statusLabel);
        statusField.setWidth("12em");
        statusField.setValue(Pdf.Status.PENDING);

        createBtn = new Button("Create", event -> createPdf());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // ---- Filters
        searchField = new TextField();
        searchField.setPlaceholder("Search by name…");
        searchField.setClearButtonVisible(true);
        searchField.setWidth("16em");

        dueFilterBox = new ComboBox<>("Due filter");
        dueFilterBox.setItems(DueFilter.ALL, DueFilter.NO_DUE_DATE, DueFilter.OVERDUE, DueFilter.DUE_TODAY, DueFilter.UPCOMING);
        dueFilterBox.setItemLabelGenerator(v -> switch (v) {
            case ALL -> "All";
            case NO_DUE_DATE -> "No due date";
            case OVERDUE -> "Overdue";
            case DUE_TODAY -> "Due today";
            case UPCOMING -> "Upcoming";
        });
        dueFilterBox.setValue(DueFilter.ALL);
        dueFilterBox.setWidth("12em");
        dueFilterBox.setClearButtonVisible(true);

        statusFilterBox = new ComboBox<>("Status filter");
        statusFilterBox.setItems(Pdf.Status.values());
        statusFilterBox.setItemLabelGenerator(this::statusLabel);
        statusFilterBox.setPlaceholder("Any");
        statusFilterBox.setClearButtonVisible(true);
        statusFilterBox.setWidth("12em");

        totalLabel = new Span("Total: 0");
        totalLabel.addClassNames(LumoUtility.FontWeight.SEMIBOLD);

        // ---- Grid
        var dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(getLocale())
                .withZone(ZoneId.systemDefault());
        var dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(getLocale());

        pdfGrid = new Grid<>();
        pdfGrid.setSizeFull();

        var nameCol = pdfGrid.addColumn(Pdf::getName).setHeader("Name").setSortable(true);

        var dueCol = pdfGrid.addColumn(pdf ->
                Optional.ofNullable(pdf.getDueDate()).map(dateFormatter::format).orElse("Not set")
        ).setHeader("Due Date").setSortable(true);

        var createdCol = pdfGrid.addColumn(pdf -> dateTimeFormatter.format(pdf.getCreatedAt()))
                .setHeader("Created At").setSortable(true);

        // Badge por vencimento
        pdfGrid.addComponentColumn(this::renderDueBadge).setHeader("Due").setAutoWidth(true);

        // Editor de status inline
        pdfGrid.addComponentColumn(this::renderStatusEditor).setHeader("Status").setAutoWidth(true);

        // Data provider com filtros
        var dp = new CallbackDataProvider<Pdf, Void>(
                query -> {
                    var pageable = toSpringPageRequest(query);
                    String q = searchField.getValue();
                    var dueFilter = Optional.ofNullable(dueFilterBox.getValue()).orElse(DueFilter.ALL);
                    var statusFilter = statusFilterBox.getValue();

                    return pdfService
                            .list(q, dueFilter, statusFilter, pageable)
                            .stream();
                },
                query -> {
                    String q = searchField.getValue();
                    var dueFilter = Optional.ofNullable(dueFilterBox.getValue()).orElse(DueFilter.ALL);
                    var statusFilter = statusFilterBox.getValue();
                    long count = pdfService.count(q, dueFilter, statusFilter);
                    totalLabel.setText("Total: " + count);
                    return (int) Math.min(count, Integer.MAX_VALUE);
                }
        );
        pdfGrid.setDataProvider(dp);

        pdfGrid.sort(GridSortOrder.desc(createdCol).build());

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);

        add(new ViewToolbar("PDF Prints",
                ViewToolbar.group(nameField, dueDateField, statusField, createBtn),
                ViewToolbar.group(searchField, dueFilterBox, statusFilterBox, totalLabel)
        ));

        add(pdfGrid);

        // Refresh nos filtros/pesquisa
        searchField.addValueChangeListener(e -> pdfGrid.getDataProvider().refreshAll());
        dueFilterBox.addValueChangeListener(e -> pdfGrid.getDataProvider().refreshAll());
        statusFilterBox.addValueChangeListener(e -> pdfGrid.getDataProvider().refreshAll());
    }

    private Span renderDueBadge(Pdf p) {
        LocalDate today = LocalDate.now();
        Span badge;
        if (p.getDueDate() == null) {
            badge = new Span("—");
            badge.getElement().getThemeList().add("badge");
            return badge;
        }
        if (p.getDueDate().isBefore(today)) {
            badge = new Span("Overdue");
            badge.getElement().getThemeList().add("badge error");
        } else if (p.getDueDate().isEqual(today)) {
            badge = new Span("Due today");
            badge.getElement().getThemeList().add("badge contrast");
        } else {
            badge = new Span("Upcoming");
            badge.getElement().getThemeList().add("badge success");
        }
        return badge;
    }

    private ComboBox<Pdf.Status> renderStatusEditor(Pdf p) {
        ComboBox<Pdf.Status> cb = new ComboBox<>();
        cb.setItems(Pdf.Status.values());
        cb.setItemLabelGenerator(this::statusLabel);
        cb.setValue(p.getStatus());
        cb.setWidth("12em");

        cb.addValueChangeListener(e -> {
            var newStatus = e.getValue();
            if (newStatus == null || p.getId() == null) return;
            try {
                pdfService.updateStatus(p.getId(), newStatus);
                p.setStatus(newStatus);
                pdfGrid.getDataProvider().refreshItem(p);
                Notification.show("Status updated", 2500, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                cb.setValue(p.getStatus());
                Notification.show("Failed to update status: " + ex.getMessage(), 4000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        return cb;
    }

    private String statusLabel(Pdf.Status s) {
        return switch (s) {
            case PENDING -> "Pending";
            case PRINTED -> "Printed";
            case SENT -> "Sent";
            case CANCELED -> "Canceled";
        };
    }

    private void createPdf() {
        if (dueDateField.isEmpty()) {
            Notification.show("Please select a due date.", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        var status = Optional.ofNullable(statusField.getValue()).orElse(Pdf.Status.PENDING);
        pdfService.createPdf(nameField.getValue(), dueDateField.getValue(), status);

        nameField.clear();
        dueDateField.clear();
        statusField.setValue(Pdf.Status.PENDING);
        pdfGrid.getDataProvider().refreshAll();

        Notification.show("PDF added", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}
