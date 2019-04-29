package com.geocento.webapps.earthimages.emis.application.client.views;

import com.geocento.webapps.earthimages.emis.application.client.Application;
import com.geocento.webapps.earthimages.emis.application.client.ClientFactory;
import com.geocento.webapps.earthimages.emis.application.client.widgets.PasswordPopup;
import com.geocento.webapps.earthimages.emis.common.client.widgets.LoadingAnchor;
import com.geocento.webapps.earthimages.emis.common.share.EIException;
import com.geocento.webapps.earthimages.emis.common.share.entities.Price;
import com.geocento.webapps.earthimages.emis.common.share.entities.TRANSACTION_TYPE;
import com.geocento.webapps.earthimages.emis.common.share.utils.UserHelper;
import com.geocento.webapps.earthimages.emis.application.client.place.PlaceHistoryHelper;
import com.geocento.webapps.earthimages.emis.application.client.place.SettingsPlace;
import com.geocento.webapps.earthimages.emis.application.client.services.CustomerService;
import com.geocento.webapps.earthimages.emis.application.client.style.StyleResources;
import com.geocento.webapps.earthimages.emis.application.client.widgets.CreditCardWidget;
import com.geocento.webapps.earthimages.emis.application.client.widgets.LoadingPanel;
import com.geocento.webapps.earthimages.emis.application.share.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.*;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.*;
import com.metaaps.webapps.libraries.client.property.domain.CountryProperty;
import com.metaaps.webapps.libraries.client.property.domain.Property;
import com.metaaps.webapps.libraries.client.property.domain.TextProperty;
import com.metaaps.webapps.libraries.client.property.editor.PropertiesEditor;
import com.metaaps.webapps.libraries.client.property.editor.PropertyEditor;
import com.metaaps.webapps.libraries.client.widget.*;
import com.metaaps.webapps.libraries.client.widget.util.DateUtil;
import com.metaaps.webapps.libraries.client.widget.util.Utils;
import com.metaaps.webapps.libraries.client.widget.util.WidgetUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thomas on 26/09/2014.
 */
public class SettingsViewImpl extends Composite implements SettingsView, ResizeHandler, HasShowRangeHandlers<Integer> {

    interface SettingsViewUiBinder extends UiBinder<Widget, SettingsViewImpl> {
    }

    private static SettingsViewUiBinder ourUiBinder = GWT.create(SettingsViewUiBinder.class);

    static public interface Style extends CssResource {

        String transactionsHistory();
    }

    @UiField Style style;

    @UiField
    EILiteTemplateView templateView;
    @UiField
    SwitchWidget switchWidget;
    @UiField Label information;
    @UiField Label password;
    @UiField Label documentation;
    @UiField
    HTMLPanel informationPanel;
    @UiField HTMLPanel passwordPanel;
    @UiField HTMLPanel documentationPanel;

    @UiField(provided=true)
    PropertiesEditor propertyWidget;
    @UiField
    ValidationEditor validation;
    @UiField
    PasswordTextBox currentPassword;
    @UiField PasswordTextBox newPassword;
    @UiField PasswordTextBox newPasswordConfirm;

    @UiField FlowPanel documentationList;
    @UiField
    HTMLPanel meansPaymentsPanel;
    @UiField
    HTMLPanel paymentMeansPanel;
    @UiField
    HTMLPanel navigationTransactionsToolbarPanel;
    @UiField
    HTMLPanel paymentsHistoryPanel;
    @UiField
    Label payments;
    @UiField
    HTMLPanel paymentTransactionsPanel;
    @UiField
    MessageLabel messageLabel;
    @UiField
    SwitchWidget switchMainPanel;
    @UiField
    HTMLPanel contentPanel;
    @UiField
    Anchor savePersonalChanges;
    @UiField
    Anchor savePasswordChanges;
    @UiField
    Label transactions;
    @UiField
    Label currentFunds;
    @UiField
    Anchor addFunds;
    @UiField
    SwitchWidget switchPaymentPanel;
    @UiField
    CreditCardWidget creditCard;
    @UiField
    CheckBox acceptTerms;
    @UiField
    LoadingAnchor addFundsPayment;
    @UiField
    DoubleBox amount;
    @UiField
    Label currency;
    @UiField
    HTMLPanel creditCardPanel;
    @UiField
    HTMLPanel paymentMessagePanel;
    @UiField
    Label minAmountLabel;
    @UiField
    AnchorElement termsOfSales;
    @UiField
    Label vatValue;
    @UiField
    Label vatMessage;

    private NavigationToolbar navigationTransactionsToolbar;

    private int pageSize = 10;

    private UserInformationDTO userProfile;

    private Presenter presenter;

    private final ClientFactory clientFactory;

    private final ConfigurableTabWidget tabWidgetHelper;

    private StyleResources styleResources = StyleResources.INSTANCE;

    public SettingsViewImpl(final ClientFactory clientFactory) {

        this.clientFactory = clientFactory;

        propertyWidget = new PropertiesEditor(PropertyEditor.MODE.EDIT, PropertyEditor.LAYOUT.BELOW, null);

        initWidget(ourUiBinder.createAndBindUi(this));

        tabWidgetHelper = new ConfigurableTabWidget();
        tabWidgetHelper.registerTab(information, informationPanel, SettingsPlace.TOKENS.information.toString());
        tabWidgetHelper.registerTab(password, passwordPanel, SettingsPlace.TOKENS.password.toString());
        tabWidgetHelper.registerTab(payments, meansPaymentsPanel, SettingsPlace.TOKENS.means.toString());
        tabWidgetHelper.registerTab(transactions, paymentTransactionsPanel, SettingsPlace.TOKENS.transactions.toString());
        tabWidgetHelper.registerTab(documentation, documentationPanel, SettingsPlace.TOKENS.documentation.toString());
        tabWidgetHelper.displayTab(0);
        tabWidgetHelper.setTabSelectedHandler(tabPanel -> {
            String token = null;
            if(tabPanel == informationPanel) {
                token = Utils.generateTokens(SettingsPlace.TOKENS.tab.toString(), SettingsPlace.TOKENS.information.toString());
            } else if(tabPanel == passwordPanel) {
                token = Utils.generateTokens(SettingsPlace.TOKENS.tab.toString(), SettingsPlace.TOKENS.password.toString());
            } else if(tabPanel == meansPaymentsPanel) {
                token = Utils.generateTokens(SettingsPlace.TOKENS.tab.toString(), SettingsPlace.TOKENS.means.toString());
            } else if(tabPanel == paymentTransactionsPanel) {
                token = Utils.generateTokens(SettingsPlace.TOKENS.tab.toString(), SettingsPlace.TOKENS.transactions.toString());
            } else if(tabPanel == documentationPanel) {
                token = Utils.generateTokens(SettingsPlace.TOKENS.tab.toString(), SettingsPlace.TOKENS.documentation.toString());
            }
            clientFactory.getPlaceController().goTo(new SettingsPlace(token));
        });

        newPassword.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                try {
                    UserHelper.checkPasswordValidity(newPassword.getValue());
                    setValidated(newPassword);
                } catch(EIException e) {
                    setError(newPassword, e.getMessage());
                }
                checkSavePasswordChanges();
            }
        });
        WidgetUtil.synchroniseTextBoxes(newPassword, newPasswordConfirm, new WidgetUtil.CompareValue<String>() {
            public boolean compareValue(String firstValue, String secondValue) {
                // do not show anything if second field is empty
                if(secondValue.length() == 0) {
                    return true;
                }
                boolean identical = firstValue.contentEquals(secondValue);
                if(identical) {
                    setValidated(newPasswordConfirm);
                } else {
                    setError(newPasswordConfirm, "Passwords do not match");
                }
                checkSavePasswordChanges();
                return identical;
            }

        });

        savePersonalChanges.addClickHandler(event -> {
            try {
                List<Property> result = propertyWidget.getProperties();
                UserInformationDTO userProfile = new UserInformationDTO();
                int index = 0;
                userProfile.setFirstName((String) result.get(index++).getValue());
                userProfile.setLastName((String) result.get(index++).getValue());
                userProfile.setEmail((String) result.get(index++).getValue());
                userProfile.setCompany((String) result.get(index++).getValue());
                userProfile.setAddress((String) result.get(index++).getValue());
                userProfile.setCountryCode((String) result.get(index++).getValue());
                userProfile.setPhone((String) result.get(index++).getValue());
                if(SettingsViewImpl.this.userProfile.isNeedsVATNumber()) {
                    userProfile.setNeedsVATNumber(SettingsViewImpl.this.userProfile.isNeedsVATNumber());
                    userProfile.setCommunityVATNumber((String) result.get(index++).getValue());
                }
                saveProfile(userProfile);
            } catch (ValidationException e) {
                e.printStackTrace();
            }
        });

        termsOfSales.setHref(Application.getApplicationSettings().getTermsOfSalesUrl());

        navigationTransactionsToolbar = new NavigationToolbar(navigationTransactionsToolbarPanel, pageSize, 0, this);
        addShowRangeHandler(new ShowRangeHandler<Integer>() {
            @Override
            public void onShowRange(ShowRangeEvent<Integer> event) {
                presenter.changeRange(event.getStart());
            }
        });

        // set the minimum amount allowed
        Price minAmount = com.geocento.webapps.earthimages.emis.application.client.utils.Utils.getLoginInfo().getMinAmount();
        boolean hasMinAmount = minAmount != null && minAmount.getValue() > 0;
        minAmountLabel.setVisible(hasMinAmount);
        if(hasMinAmount) {
            minAmountLabel.setText(" (min " + com.geocento.webapps.earthimages.emis.common.client.utils.Utils.displayPrice(minAmount) + ")");
        }
        // add funds panel widgets
        switchPaymentPanel.showWidget(addFunds);
        String fundCurrency = com.geocento.webapps.earthimages.emis.application.client.utils.Utils.getLoginInfo().getPrepaidCurrency();
        addFunds.addClickHandler(event -> {
            switchPaymentPanel.showWidget(creditCardPanel);
            // clear fields
            amount.setValue(minAmount.getValue());
            currency.setText(fundCurrency);
            creditCard.clearFields();
            paymentMessagePanel.clear();
            updateVATMessage();
        });
        addFundsPayment.addClickHandler(event -> {
            creditCard.hideValidationError();
            try {
                CreditCardRequest creditCardRequest = creditCard.validateCreditCardRequest();
                if(!acceptTerms.getValue()) {
                    throw new ValidationException("You need to accept the terms and conditions first");
                }
                double fundAmount = amount.getValue();
                if(fundAmount < minAmount.getValue()) {
                    throw new ValidationException("A minimum of " + com.geocento.webapps.earthimages.emis.common.client.utils.Utils.displayRoundedPrice(minAmount.getValue(), fundCurrency) + " is required");
                }

                addFundsPayment.setLoading(true);
                Price fundPrice = new Price(fundAmount, fundCurrency);
                CustomerService.App.getInstance().addFundsWithCreditCard(fundPrice, creditCardRequest, new AsyncCallback<PaymentTransactionDTO>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        addFundsPayment.setLoading(false);
                        creditCard.displayValidationError(caught.getMessage());
                    }

                    @Override
                    public void onSuccess(PaymentTransactionDTO result) {
                        addFundsPayment.setLoading(false);
                        switchPaymentPanel.showWidget(addFunds);
                        MessageLabel messageLabel = new MessageLabel();
                        messageLabel.displayValidated(com.geocento.webapps.earthimages.emis.common.client.utils.Utils.displayPrice(fundPrice) + " has been added to your prepaid account. Thank you for trusting EarthImages.");
                        paymentMessagePanel.add(messageLabel);
                        presenter.loadTransactionsHistory();
                    }
                });
            } catch(ValidationException validationException) {
                creditCard.displayValidationError(validationException.getMessage());
            }
        });
        amount.addValueChangeHandler(event -> updateVATMessage());

        LoadingPanel.getInstance().getElement().getStyle().setZIndex(1000);

        Window.addResizeHandler(this);

        // refresh max width
        Scheduler.get().scheduleDeferred(new Command() {
            @Override
            public void execute() {
                onResize(null);
            }
        });
    }

    private void updateVATMessage() {
        boolean chargeVAT = com.geocento.webapps.earthimages.emis.application.client.utils.Utils.getLoginInfo().isChargeVAT();
        vatValue.setVisible(chargeVAT);
        vatMessage.setVisible(chargeVAT);
        if(chargeVAT) {
            Double value = amount.getValue();
            if(value != null && value != 0) {
                vatMessage.setText(com.geocento.webapps.earthimages.emis.common.client.utils.Utils.displayPrice(new Price(value * 1.2, com.geocento.webapps.earthimages.emis.application.client.utils.Utils.getLoginInfo().getPrepaidCurrency())) + " will be charged on your credit card");
            } else {
                vatMessage.setText("UK VAT 20% will be added to the amount of funds you select");
            }
        }
    }

    protected void checkSavePasswordChanges() {
        savePasswordChanges.setEnabled(false);
        try {
            UserHelper.checkPasswordValidity(currentPassword.getValue());
            UserHelper.checkPasswordValidity(newPassword.getValue());
            if(newPassword.getValue().contentEquals(newPasswordConfirm.getValue())) {
                savePasswordChanges.setEnabled(true);
            }
        } catch(EIException e) {
        }
    }

    private void setValidated(Widget widget) {
        clearWidget(widget);
        widget.addStyleName("ei-validated");
    }

    private void setError(Widget widget, String message) {
        clearWidget(widget);
        widget.addStyleName("ei-error");
        validation.setErrorMessage(message);
    }

    private void clearWidget(Widget widget) {
        widget.removeStyleName("ei-validated");
        widget.removeStyleName("ei-error");
        validation.cleanUp();
    }

    private void setLoading(Panel panel, String message) {
        panel.clear();
        panel.add(new LoadingIcon(message));
    }

    @Override
    public HasClickHandlers getSavePersonalChanges() {
        return savePersonalChanges;
    }

    @Override
    public HasClickHandlers getSavePasswordChanges() {
        return savePasswordChanges;
    }

    @Override
    public void displayCreditAmount(double amount, String currency) {
        // update pre paid account value
        currentFunds.setText(amount == 0 ? "No funds left on your pre-paid account" :
                "You have " + com.geocento.webapps.earthimages.emis.common.client.utils.Utils.displayPrice(new Price(amount, currency)) + " left on your pre-paid account");
    }

    // TODO - move to activity
    protected void saveProfile(final UserInformationDTO userProfile) {
        final PasswordPopup popup = PasswordPopup.getInstance();
        // ask for password first
        popup.requestPassword(new PasswordPopup.Presenter() {

            @Override
            public void onSubmitted(String password) {
/*
                try {
                    UserHelper.checkPasswordValidity(password);
                } catch(EIException e) {
                    popup.setErrorMessage(e.getMessage());
                    return;
                }
*/
                popup.hide();
                presenter.updateProfile(userProfile, password);
            }

            @Override
            public void onCancelled() {
                popup.hide();
            }
        });
    }

    @Override
    public int getTransactionsHistoryStart() {
        return navigationTransactionsToolbar.getIndex();
    }

    @Override
    public void hideTransactionsHistoryLoading() {

    }

    @Override
    public int getTransactionsHistoryPageSize() {
        return navigationTransactionsToolbar.getPageSize();
    }

    @Override
    public void setCreditCardsLoading(String message) {
        paymentMeansPanel.clear();
        LoadingIcon loadingMessage = new LoadingIcon(message);
        loadingMessage.getElement().getStyle().setMargin(20, com.google.gwt.dom.client.Style.Unit.PX);
        paymentMeansPanel.add(loadingMessage);
    }

    @Override
    public void setCreditCardsError(String message) {
        displayError(paymentMeansPanel, message);
    }

    @Override
    public void setCreditCards(List<CreditCardToken> creditCardTokens) {
        paymentMeansPanel.clear();
        if(creditCardTokens == null || creditCardTokens.size() == 0) {
            paymentMeansPanel.add(new Label("You do not have any registered means of payments."));
            return;
        }
        Grid grid = new Grid();
        grid.setCellPadding(5);
        grid.resize(creditCardTokens.size(), 5);
        int row = 0;
        for(CreditCardToken creditCardToken : creditCardTokens) {
            grid.setText(row, 0, "Credit Card (" + (row + 1) + ")");
            Image image = new Image(creditCardToken.getImageUrl());
            image.setWidth("30px");
            grid.setWidget(row, 1, image);
            grid.setText(row, 2, creditCardToken.getMaskedNumber());
            row++;
        }
        paymentMeansPanel.add(grid);
    }

    @Override
    public void displayTransactionsHistoryLoading(String message) {
        displayLoading(paymentsHistoryPanel, message);
    }

    @Override
    public void displayDocumentationLoading(String message) {
        displayLoading(documentationList, message);
    }

    private void displayLoading(Panel panel, String message) {
        panel.clear();
        LoadingIcon loadingMessage = new LoadingIcon(message);
        loadingMessage.getElement().getStyle().setMargin(20, com.google.gwt.dom.client.Style.Unit.PX);
        panel.add(loadingMessage);
    }

    private void displayError(Panel panel, String message) {
        panel.clear();
        ErrorIcon errorIcon = new ErrorIcon();
        errorIcon.getElement().getStyle().setMargin(20, com.google.gwt.dom.client.Style.Unit.PX);
        panel.add(errorIcon);
    }

    @Override
    public void hideDocumentationLoading() {
        documentationList.clear();
    }

    @Override
    public void setDocumentationError(String message) {
        displayError(documentationList, message);
    }

    @Override
    public void setDocumentation(List<DocumentDTO> documentDTOs) {
        documentationList.clear();
        if(documentDTOs == null || documentDTOs.size() == 0) {
            documentationList.add(new HTML("<p>No documentation provided so far...</p>"));
        } else {
            documentationList.add(new HTML("<p>Here are the documents you have provided so far</p>"));
            Grid grid = new Grid();
            grid.setCellPadding(5);
            grid.addStyleName(style.transactionsHistory());
            grid.resize(documentDTOs.size() + 1, 3);
            int row = 0;
            grid.setText(row, 0, "Type");
            grid.setText(row, 1, "Created on");
            grid.setText(row, 2, "");
            row++;
            for(DocumentDTO documentDTO : documentDTOs) {
                grid.setText(row, 0, documentDTO.getName());
                grid.setText(row, 1, DateUtil.displaySimpleUTCDate(documentDTO.getCreatedOn()));
                Anchor downloadAnchor = new Anchor("Download");
                downloadAnchor.setHref(documentDTO.getDownloadUrl());
                downloadAnchor.setTarget("_blank");
                downloadAnchor.addStyleName(StyleResources.INSTANCE.style().eiBlueAnchor());
                downloadAnchor.getElement().getStyle().setTextDecoration(com.google.gwt.dom.client.Style.TextDecoration.UNDERLINE);
                grid.setWidget(row, 2, downloadAnchor);
                row++;
            }
            documentationList.add(grid);
        }
    }

    @Override
    public void displayUserInformationLoading(String message) {
        displayLoading(message);
    }

    @Override
    public void hideUserInformationLoading() {
        hideLoading();
    }

    @Override
    public void displayTab(SettingsPlace.TOKENS tabToken) {
        tabWidgetHelper.displayTab(tabToken.toString(), false);
    }

    @Override
    public void setTransactionsHistoryError(String message) {
        paymentsHistoryPanel.clear();
        ErrorIcon errorMessage = new ErrorIcon(message);
        errorMessage.getElement().getStyle().setMargin(20, com.google.gwt.dom.client.Style.Unit.PX);
        paymentsHistoryPanel.add(errorMessage);
    }

    @Override
    public void setTransactionsHistory(final List<PaymentTransactionDTO> paymentTransactionDTOs) {
        paymentsHistoryPanel.clear();
        if(paymentTransactionDTOs == null || paymentTransactionDTOs.size() == 0) {
            paymentsHistoryPanel.add(new Label("You have not made any payments to date."));
            return;
        }
        Grid grid = new Grid();
        grid.setCellPadding(5);
        grid.addStyleName(style.transactionsHistory());
        grid.resize(paymentTransactionDTOs.size() + 1, 6);
        int row = 0;
        grid.setText(row, 0, "Id");
        grid.setText(row, 1, "In date of");
        grid.setText(row, 2, "Amount of");
        grid.setText(row, 3, "Payment means");
        grid.setText(row, 4, "Order");
        grid.setText(row, 5, "Description");
        row++;
        for(final PaymentTransactionDTO paymentTransactionDTO : paymentTransactionDTOs) {
            grid.setText(row, 0, "#" + paymentTransactionDTO.getId());
            grid.setText(row, 1, DateUtil.displaySimpleDate(paymentTransactionDTO.getCreatedOn()));
            grid.setText(row, 2, com.geocento.webapps.earthimages.emis.common.client.utils.Utils.displayPrice(paymentTransactionDTO.getAmount()));
            String textLabel = null;
            TRANSACTION_TYPE transactionType = paymentTransactionDTO.getType();
            if(transactionType == null) {
                textLabel = "Unknown";
            } else {
                switch (transactionType) {
                    case bacsTransfer:
                        textLabel = "BACS transfer";
                        break;
                    case creditCard:
                        textLabel = "Credit card payment";
                        break;
                    case purchase:
                        textLabel = "Purchase";
                        break;
                    case voucher:
                        textLabel = "Voucher";
                        break;
                }
            }
            Label paymentMeans = new Label(textLabel);
            grid.setWidget(row, 3, paymentMeans);
            if(paymentTransactionDTO.getOrderId() != null) {
/*
                Anchor orderLink = new Anchor("View Order");
                orderLink.setHref("#" + PlaceHistoryHelper.convertPlace(
                        new OrderingPlace(Utils.generateTokens(OrderingPlace.TOKENS.orderid.toString(),
                                paymentTransactionDTO.getOrderId()))));
                orderLink.getElement().getStyle().setColor("blue");
                grid.setWidget(row, 4, orderLink);
*/
            } else if(paymentTransactionDTO.getInvoiceUrl() != null) {
                Anchor invoiceLink = new Anchor("Download Invoice");
                invoiceLink.setHref(paymentTransactionDTO.getInvoiceUrl());
                invoiceLink.setTarget("_blank");
                invoiceLink.getElement().getStyle().setColor("blue");
                grid.setWidget(row, 4, invoiceLink);
            }
            // add comment
            grid.setWidget(row, 5, new Label(paymentTransactionDTO.getDescription()));
            row++;
        }
        paymentsHistoryPanel.add(grid);
    }

    @Override
    public HandlerRegistration addShowRangeHandler(ShowRangeHandler<Integer> handler) {
        return addHandler(handler, ShowRangeEvent.getType());
    }

    @Override
    public EILiteTemplateView getTemplateView() {
        return templateView;
    }

    @Override
    public void displayLoading(String message) {
        messageLabel.displayLoading(message);
        switchMainPanel.showWidget(messageLabel);
    }

    @Override
    public void hideLoading() {
        switchMainPanel.showWidget(contentPanel);
    }

    @Override
    public void displayError(String message) {
        messageLabel.displayError(message);
        switchMainPanel.showWidget(messageLabel);
    }

    @Override
    public void displayUserInformation(UserInformationDTO result) {
        this.userProfile = result;
        if(userProfile != null) {
            // update user fields
            final List<Property> properties = new ArrayList<Property>();
            properties.add(new TextProperty("First Name", null, userProfile.getFirstName(), true, 1, 100));
            properties.add(new TextProperty("Surname", null, userProfile.getLastName(), true, 1, 100));
            properties.add(new TextProperty("Email", "So we can contact you for order updates and to recover your password", userProfile.getEmail(), true, 5, 200, UserHelper.emailregExp));
            properties.add(new TextProperty("Organisation Name", "The name of the organisation you work for, if for private purpose please put 'private'", userProfile.getCompany(), true, 1, 200));
            properties.add(new TextProperty("Organisation Address", "Includes street, street number and city as well as postcode", userProfile.getAddress(), true, 5, 1000, "", 2, true));
            properties.add(new CountryProperty("Organisation Country", null, userProfile.getCountryCode(), true));
            properties.add(new TextProperty("Phone", "Your phone number with international extension", userProfile.getPhone(), true));
            if(userProfile.isNeedsVATNumber()) {
                properties.add(new TextProperty("EU intracommunity VAT number", "Please provide your EU intracommunity VAT number", userProfile.getPhone(), true));
            }
            propertyWidget.setProperties(properties);
        }
    }

    @Override
    public void hidePaymentMeansLoading() {
    }

    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onResize(ResizeEvent event) {

    }

}
