package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.geocento.webapps.earthimages.emis.common.share.EIException;
import com.geocento.webapps.earthimages.emis.common.share.utils.CreditCardHelper;
import com.geocento.webapps.earthimages.emis.application.share.CreditCardRequest;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.metaaps.webapps.libraries.client.widget.MessageLabel;
import com.metaaps.webapps.libraries.client.widget.SelectionWidget;
import com.metaaps.webapps.libraries.client.widget.ValidationException;
import com.metaaps.webapps.libraries.client.widget.util.StringUtils;
import com.metaaps.webapps.libraries.client.widget.util.Util;

import java.util.ArrayList;
import java.util.List;

public class CreditCardWidget extends Composite {

    interface CreditCardWidgetUiBinder extends UiBinder<HTMLPanel, CreditCardWidget> {
    }

    private static CreditCardWidgetUiBinder ourUiBinder = GWT.create(CreditCardWidgetUiBinder.class);

    static public interface Style extends CssResource {

        String error();
    }

    @UiField Style style;

    @UiField
    HTMLPanel footer;
    @UiField
    TextBox cardNumber;
    @UiField
    TextBox month;
    @UiField
    TextBox year;
    @UiField
    Anchor CVVHelp;
    @UiField
    TextBox cvv;
    @UiField
    MessageLabel validationMessage;
    @UiField
    SelectionWidget saveCard;

    public CreditCardWidget() {
        initWidget(ourUiBinder.createAndBindUi(this));

        HTMLPanel helpPanel = new HTMLPanel("");
        new HoverWidgetPopup<HTMLPanel, String>(helpPanel) {
            @Override
            protected void onDisplayWidget(String helpContent) {
                widget.clear();
                widget.add(new HTML(helpContent));
            }
        }.registerPopup(CVVHelp, "<p style='width: 250px; padding: 20px;'>For MasterCard, Visa or Discover, it's the last 3 digits in the signature area on the back of your card.</p>", Util.TYPE.left, 500);

    }

    public CreditCardRequest validateCreditCardRequest() throws ValidationException {
        String number = cardNumber.getText();
        String month = this.month.getText();
        String year = this.year.getText();
        String cvv = this.cvv.getText();
        boolean saveCard = this.saveCard.isSelected();
        // validate fields
        List<String> errors = new ArrayList<String>();
        try {
            CreditCardHelper.checkNumber(number);
        } catch (EIException e) {
            errors.add(e.getMessage());
            setError(cardNumber, true);
        }
        try {
            CreditCardHelper.checkMonth(month);
        } catch (EIException e) {
            errors.add(e.getMessage());
            setError(this.month, true);
        }
        try {
            CreditCardHelper.checkYear(year);
        } catch (EIException e) {
            errors.add(e.getMessage());
            setError(this.year, true);
        }
        try {
            CreditCardHelper.checkCVV(cvv);
        } catch (EIException e) {
            errors.add(e.getMessage());
            setError(this.cvv, true);
        }
        boolean valid = errors.size() == 0;
        if(!valid) {
            throw new ValidationException("Problems with credit card details: " + StringUtils.join(errors, ", "));
        }
        CreditCardRequest creditCardRequest = new CreditCardRequest();
        creditCardRequest.setNumber(number);
        creditCardRequest.setMonth(month);
        creditCardRequest.setYear(year);
        creditCardRequest.setStoreCard(false);
        creditCardRequest.setCvv(cvv);
        creditCardRequest.setStoreCard(saveCard);
        return creditCardRequest;
    }

    private void setError(TextBox textBox, boolean error) {
        textBox.setStyleName(style.error(), error);
    }

    public void displayValidationError(String message) {
        validationMessage.displayError(message);
    }

    public void hideValidationError() {
        validationMessage.setVisible(false);
    }

    public void clearFields() {
        cardNumber.setText("");
        month.setText("");
        year.setText("");
        cvv.setText("");
        saveCard.setSelected(false);
        validationMessage.setVisible(false);
    }

    @UiChild(tagname = "footer")
    public void addFooterWidget(Widget widget) {
        footer.add(widget);
    }

}