package com.example.emailfeature.ui;

import com.example.base.ui.component.ViewToolbar;
import com.example.base.ui.MainLayout;
import com.example.emailfeature.Email;
import com.example.emailfeature.EmailService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Optional;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route(value = "emails", layout = MainLayout.class)
@PageTitle("Emails")
@Menu(order = 1, icon = "vaadin:envelope", title = "Emails")
public class EmailView extends Main {

    private final EmailService emailService;

    final TextField recipient;
    final TextField subject;
    final TextArea body;
    final Button sendBtn;
    final Grid<Email> emailGrid;

    public EmailView(EmailService emailService) {
        this.emailService = emailService;

        recipient = new TextField();
        recipient.setPlaceholder("Recipient");
        recipient.setAriaLabel("Recipient email");
        recipient.setMinWidth("20em");

        subject = new TextField();
        subject.setPlaceholder("Subject");
        subject.setAriaLabel("Email subject");
        subject.setMinWidth("20em");

        body = new TextArea();
        body.setPlaceholder("Body");
        body.setAriaLabel("Email body");
        body.setMinWidth("20em");

        sendBtn = new Button("Send", event -> sendEmail());
        sendBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(Locale.getDefault());

        emailGrid = new Grid<>();
        emailGrid.setItems(query -> emailService.list(toSpringPageRequest(query)).stream());
        emailGrid.addColumn(Email::getRecipient).setHeader("To");
        emailGrid.addColumn(Email::getSubject).setHeader("Subject");
        emailGrid.addColumn(Email::getBody).setHeader("Body");
        emailGrid.addColumn(email -> Optional.ofNullable(email.getSentAt())
                .map(dateTimeFormatter::format).orElse("")).setHeader("Sent Date");
        emailGrid.setSizeFull();

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);

        add(new ViewToolbar("Emails", ViewToolbar.group(recipient, subject, body, sendBtn)));
        add(emailGrid);
    }

    private void sendEmail() {
        try {
            emailService.sendEmail(recipient.getValue(), subject.getValue(), body.getValue());
            emailGrid.getDataProvider().refreshAll();
            recipient.clear();
            subject.clear();
            body.clear();
            Notification.show("Email sent", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification.show("Failed to send email: " + e.getMessage(), 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}