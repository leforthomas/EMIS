package com.geocento.webapps.earthimages.emis.admin.client.view;

import com.geocento.webapps.earthimages.emis.admin.client.ClientFactory;
import com.geocento.webapps.earthimages.emis.admin.client.event.UserCreated;
import com.geocento.webapps.earthimages.emis.admin.client.place.AIUsersPlace;
import com.geocento.webapps.earthimages.emis.admin.client.widgets.UserList;
import com.geocento.webapps.earthimages.emis.admin.share.UserDTO;
import com.geocento.webapps.earthimages.emis.common.client.popup.LoadingPanel;
import com.geocento.webapps.earthimages.emis.common.client.popup.PopupPropertyEditor;
import com.geocento.webapps.earthimages.emis.common.client.style.StyleResources;
import com.geocento.webapps.earthimages.emis.common.share.entities.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.metaaps.webapps.libraries.client.property.domain.ChoiceProperty;
import com.metaaps.webapps.libraries.client.property.domain.CountryProperty;
import com.metaaps.webapps.libraries.client.property.domain.Property;
import com.metaaps.webapps.libraries.client.property.domain.TextProperty;
import com.metaaps.webapps.libraries.client.widget.CompletionHandler;
import com.metaaps.webapps.libraries.client.widget.IconAnchor;
import com.metaaps.webapps.libraries.client.widget.ValidationException;
import com.metaaps.webapps.libraries.client.widget.util.ListUtil;
import com.metaaps.webapps.libraries.client.widget.util.Utils;

import java.util.List;

public class AIUsersViewImpl extends Composite implements AIUsersView {

	private static AISensorsViewImplUiBinder uiBinder = GWT
			.create(AISensorsViewImplUiBinder.class);

    interface AISensorsViewImplUiBinder extends UiBinder<Widget, AIUsersViewImpl> {
	}
	
	static private StyleResources styles = GWT.create(StyleResources.class);
	
	public interface Style extends CssResource {
	}

	@UiField
    Style style;
	
	@UiField
    AIApplicationTemplateView templateView;
    @UiField
    TextBox searchBox;
    @UiField(provided = true)
    UserList userList;
    @UiField
    Button createUser;
    @UiField
    Button refreshButton;
    @UiField
    FormPanel formPanel;
    @UiField
    IconAnchor downloadUsers;
    @UiField
    IconAnchor downloadTransactions;

    private ClientFactory clientFactory;

	private Presenter presenter;
	
	public AIUsersViewImpl(ClientFactory clientFactory) {
		
		this.clientFactory = clientFactory;

        userList = new UserList(clientFactory.getEventBus());

        initWidget(uiBinder.createAndBindUi(this));

        templateView.setPlace(new AIUsersPlace());

        formPanel.setAction(GWT.getModuleBaseURL() + "UsersDownload");
        formPanel.setEncoding(FormPanel.ENCODING_URLENCODED);
        formPanel.setMethod(FormPanel.METHOD_POST);

        downloadTransactions.addClickHandler(event -> Window.open("./api/orders/transactions/all/download/csv", "_blank", null));
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public AIApplicationTemplateView getTemplateView() {
        return templateView;
    }

    @Override
    public void displayError(String message) {
        Window.alert(message);
    }

    @Override
    public void displayLoading(String message) {
        LoadingPanel.getInstance().show(message);
    }

    @Override
    public void hideLoading() {
        LoadingPanel.getInstance().hide();
    }

    @Override
    public void displayUsers(int start, int limit, String sortBy, boolean isAscending, String keyWords, List<UserDTO> users) {
        userList.setPageSize(limit);
        userList.setRowData(start, users);
    }

    @Override
    public void refreshUsers() {
        userList.refreshDisplay();
    }

    @Override
    public HasClickHandlers getCreateUser() {
        return createUser;
    }

    @Override
    public HasClickHandlers getRefresh() {
        return refreshButton;
    }

    @Override
    public void displayCreateUser() {
        PopupPropertyEditor.getInstance().edit("Create new user",
                ListUtil.toList(new Property[]{
                        new TextProperty("User name", null, "", true, 5, 100),
                        new TextProperty("Email", null, "", true, 5, 100),
                        new TextProperty("Password", null, "", true, 5, 100),
                        new TextProperty("First Name", null, "", true, 1, 100),
                        new TextProperty("Last Name", null, "", true, 1, 100),
                        new TextProperty("Organisation", null, "", true, 1, 100),
                        new CountryProperty("Country", null, "GB", true),
                        new ChoiceProperty("Application Domain", "The application domain for this user", DOMAIN.commercial.toString(), true, true, Utils.enumNameToStringArray(DOMAIN.values())),
                        new ChoiceProperty("Usage", "The usage for this user", USAGE.atmosphere.toString(), true, true, Utils.enumNameToStringArray(USAGE.values())),
                        new ChoiceProperty("User Role", "The role for this user", USER_ROLE.CONSUMER.toString(), true, true, Utils.enumNameToStringArray(USER_ROLE.values())),
                        new ChoiceProperty("User Status", "The registration status for this user", USER_STATUS.APPROVED.toString(), true, true, new String[]{USER_STATUS.APPROVED.toString()}),
                        new ChoiceProperty("Currency", null, "EUR", true, true, Price.supportedCurrencies),
                }), new CompletionHandler<List<Property>>() {

                    @Override
                    public void onCompleted(List<Property> result) throws ValidationException {
                        int index = 0;

                        String username = (String) result.get(index++).getValue();
                        String email  = (String) result.get(index++).getValue();
                        String password = (String) result.get(index++).getValue();
                        String firstName = (String) result.get(index++).getValue();
                        String lastName = (String) result.get(index++).getValue();
                        String organization = (String) result.get(index++).getValue();
                        String country = (String) result.get(index++).getValue();

                        DOMAIN appDomain = DOMAIN.other;
                        try {
                            appDomain = DOMAIN.valueOf((String) result.get(index++).getValue());
                        }catch(Exception e)
                        {

                        }

                        USAGE usage = USAGE.other;
                        try {
                            usage = USAGE.valueOf((String) result.get(index++).getValue());
                        }catch(Exception e)
                        {

                        }

                        USER_ROLE rol = USER_ROLE.valueOf((String) result.get(index++).getValue());
                        USER_STATUS status = USER_STATUS.valueOf((String) result.get(index++).getValue());

                        String currency = (String) result.get(index++).getValue();

                        clientFactory.getEventBus().fireEvent(new UserCreated(
                                username,
                                email,
                                password,
                                firstName,
                                lastName,
                                organization,
                                country,
                                appDomain,
                                usage,
                                rol,
                                status,
                                currency
                        ));
                    }

                    @Override
                    public void onCancel() {

                    }
                });
    }

    @Override
    public String getKeywords() {
        return searchBox.getText();
    }

    @Override
    public int getStart() {
        return userList.getStart();
    }

    @Override
    public int getLimit() {
        return userList.getPageSize();
    }

    @Override
    public String getSortBy() {
        return userList.getSortBy();
    }

    @Override
    public boolean isAscending() {
        return userList.isAscending();
    }

    @UiHandler("downloadUsers")
    void downloadUsers(ClickEvent clickEvent) {
        formPanel.submit();
    }

}
