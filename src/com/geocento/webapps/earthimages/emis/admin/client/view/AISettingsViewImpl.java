package com.geocento.webapps.earthimages.emis.admin.client.view;

import com.geocento.webapps.earthimages.emis.admin.client.ClientFactory;
import com.geocento.webapps.earthimages.emis.admin.client.place.AISettingsPlace;
import com.geocento.webapps.earthimages.emis.admin.client.services.AdministrationService;
import com.geocento.webapps.earthimages.emis.common.client.popup.LoadingPanel;
import com.geocento.webapps.earthimages.emis.common.client.style.StyleResources;
import com.geocento.webapps.earthimages.emis.common.share.entities.Settings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.metaaps.webapps.libraries.client.property.domain.*;
import com.metaaps.webapps.libraries.client.property.editor.PropertiesInEditor;
import com.metaaps.webapps.libraries.client.widget.CompletionHandler;
import com.metaaps.webapps.libraries.client.widget.ValidationException;
import com.metaaps.webapps.libraries.client.widget.util.WidgetUtil;

import java.util.List;

public class AISettingsViewImpl extends Composite implements AISettingsView {

	private static AISensorsViewImplUiBinder uiBinder = GWT
			.create(AISensorsViewImplUiBinder.class);

    interface AISensorsViewImplUiBinder extends UiBinder<Widget, AISettingsViewImpl> {
	}
	
	static private StyleResources styles = GWT.create(StyleResources.class);
	
	public interface Style extends CssResource {
	}

	@UiField
    Style style;
	
	@UiField
    AIApplicationTemplateView templateView;
    @UiField
    Grid settings;
    @UiField
    PropertiesInEditor emailServerSettings;
    @UiField
    PropertiesInEditor emailNotificationSettings;
    @UiField
    PropertiesInEditor apiSettings;
    @UiField
    PropertiesInEditor customerApplicationSettings;
    @UiField
    Button uploadChanges;
    @UiField
    PropertiesInEditor otherSettings;
    @UiField
    Button testEmail;
    @UiField
    PropertiesInEditor braintreeSettings;
    @UiField
    PropertiesInEditor slackSettings;
    @UiField
    PropertiesInEditor xeroSettings;
    @UiField
    PropertiesInEditor hubspotSettings;
    @UiField
    HTMLPanel planetAccountValues;

    private ClientFactory clientFactory;

	private Presenter presenter;
	
	public AISettingsViewImpl(ClientFactory clientFactory) {
		
		this.clientFactory = clientFactory;

        initWidget(uiBinder.createAndBindUi(this));

        templateView.setPlace(new AISettingsPlace());

        testEmail.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                displayLoading("Sending test email");
                AdministrationService.App.getInstance().testEmail(new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        hideLoading();
                        Window.alert("Error sending email, error is " + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(Void result) {
                        hideLoading();
                        Window.alert("Email sent, check your mailbox");
                    }
                });
            }
        });
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
    public void displaySettings(final Settings settings) {
        emailServerSettings.setProperties(new CompletionHandler<List<Property>>() {
            @Override
            public void onCompleted(List<Property> result) throws ValidationException {
                int index = 0;
                settings.setApplicationName((String) result.get(index++).getValue());
                settings.setServerType((String) result.get(index++).getValue());
                settings.setEmailFrom((String) result.get(index++).getValue());
                settings.setEmailServer((String) result.get(index++).getValue());
                settings.setEmailPort((Integer) result.get(index++).getValue());
                settings.setSMTPS((Boolean) result.get(index++).getValue());
                settings.setEmailAccount((String) result.get(index++).getValue());
                settings.setEmailPassword((String) result.get(index++).getValue());
                if(presenter != null) {
                    presenter.settingsHaveChanged();
                }
            }

            @Override
            public void onCancel() {

            }
        },
                new TextProperty("Application Name", "", settings.getApplicationName(), true, 3, 255),
                new TextProperty("Server Type", "", settings.getServerType(), true, 3, 255),
                new TextProperty("From", "", settings.getEmailFrom(), true, 3, 255),
                new TextProperty("Server", "", settings.getEmailServer(), true, 3, 255),
                new IntegerProperty("Port", "", settings.getEmailPort() == null ? 465 : settings.getEmailPort(), true, 0, 8000),
                new BooleanProperty("SMTPS", "", settings.isSMTPS(), true),
                new TextProperty("Account", "", settings.getEmailAccount(), true, 3, 255),
                new PasswordProperty("Password", "", settings.getEmailPassword(), true, 3, 255)
                );

        emailNotificationSettings.setProperties(new CompletionHandler<List<Property>>() {
                    @Override
                    public void onCompleted(List<Property> result) throws ValidationException {
                        int index = 0;
                        settings.setSupportEmail((String) result.get(index++).getValue());
                        settings.setOrderEmail((String) result.get(index++).getValue());
                        if(presenter != null) {
                            presenter.settingsHaveChanged();
                        }
                    }

                    @Override
                    public void onCancel() {

                    }
                },
                new TextProperty("Support email", "Email for general support", settings.getSupportEmail(), true),
                new TextProperty("Order email", "Email for forwarding product requests to, eg for coverage requests", settings.getOrderEmail(), true)
        );

        apiSettings.setProperties(new CompletionHandler<List<Property>>() {
                                      @Override
                                      public void onCompleted(List<Property> result) throws ValidationException {
                                          int index = 0;
                                          settings.setEartimagesAPIURL((String) result.get(index++).getValue());
                                          settings.setEarthimagesAPIToken((String) result.get(index++).getValue());
                                          settings.setEIProductCallbackURL((String) result.get(index++).getValue());
                                          settings.setMaxPolygonPoints((Integer) result.get(index++).getValue());
                                          settings.setMaxDaysFuture((Integer) result.get(index++).getValue());
                                          settings.setMonitoringRefresh((Integer) result.get(index++).getValue());
                                          settings.setMaxImageAlerts((Integer) result.get(index++).getValue());
                                          if (presenter != null) {
                                              presenter.settingsHaveChanged();
                                          }
                                      }

                                      @Override
                                      public void onCancel() {

                                      }
                                  },
                new TextProperty("EI API URL", "", settings.getEartimagesAPIURL(), true, 0, 1000),
                new PasswordProperty("EI API Token", "", settings.getEarthimagesAPIToken(), true, 0, 100),
                new TextProperty("EI Product callback URL", "The URL for the product order change notification callback", settings.getEIProductCallbackURL(), true, 0, 1000),
                new IntegerProperty("Max Polygon Points", "", settings.getMaxPolygonPoints(), true, 0, 500),
                new IntegerProperty("Max Days For Future Searches", "", settings.getMaxDaysFuture(), true, 0, 30),
                new IntegerProperty("Monitoring delay", "Time between two searches for new imagery for an alert", settings.getMonitoringRefresh(), true, 0, 500),
                new IntegerProperty("Max Image Alerts per user", "", settings.getMaxImageAlerts(), true, 0, 500)
        );

        braintreeSettings.setProperties(new CompletionHandler<List<Property>>() {
                                            @Override
                                            public void onCompleted(List<Property> result) throws ValidationException {
                                                int index = 0;
                                                settings.setBraintreeSandbox((Boolean) result.get(index++).getValue());
                                                settings.setBraintreeMerchantId((String) result.get(index++).getValue());
                                                settings.setBraintreeCSE((String) result.get(index++).getValue());
                                                settings.setBraintreePublicKey((String) result.get(index++).getValue());
                                                settings.setBraintreePrivateKey((String) result.get(index++).getValue());
                                                settings.setMinAmountEuros((double) result.get(index++).getValue());
                                                if (presenter != null) {
                                                    presenter.settingsHaveChanged();
                                                }
                                            }

                                            @Override
                                            public void onCancel() {

                                            }
                                        },
                new BooleanProperty("Sandbox mode", "", settings.isBraintreeSandbox(), true),
                new TextProperty("Merchant ID", "", settings.getBraintreeMerchantId(), true, 0, 1000),
                new TextProperty("CSE value", "", settings.getBraintreeCSE(), true, 0, 1000),
                new TextProperty("Public key", "", settings.getBraintreePublicKey(), true, 0, 1000),
                new TextProperty("Private key", "", settings.getBraintreePrivateKey(), true, 0, 1000),
                new DoubleProperty("Min amount allowed", "Set the minimum allowed for credit card payments, in euros", settings.getMinAmountEuros(), true, 0, 1000000)
        );

        slackSettings.setProperties(new CompletionHandler<List<Property>>() {
                                        @Override
                                        public void onCompleted(List<Property> result) throws ValidationException {
                                            int index = 0;
                                            settings.setSlackClientID((String) result.get(index++).getValue());
                                            settings.setSlackClientSecret((String) result.get(index++).getValue());
                                            settings.setSlackVerificationToken((String) result.get(index++).getValue());
                                            settings.setPrivateSlackHook((String) result.get(index++).getValue());
                                            if (presenter != null) {
                                                presenter.settingsHaveChanged();
                                            }
                                        }

                                        @Override
                                        public void onCancel() {

                                        }
                                    },
                new TextProperty("Client ID", "", settings.getSlackClientID(), true, 0, 1000),
                new PasswordProperty("Client Secret", "", settings.getSlackClientSecret(), true, 0, 1000),
                new TextProperty("Verification Token", "", settings.getSlackVerificationToken(), true, 0, 1000),
                new TextProperty("Private App Webhook", "", settings.getPrivateSlackHook(), true, 0, 1000)
        );

        xeroSettings.setProperties(new CompletionHandler<List<Property>>() {
                                       @Override
                                       public void onCompleted(List<Property> result) throws ValidationException {
                                           int index = 0;
                                           settings.setXeroPathToPrivateKey((String) result.get(index++).getValue());
                                           settings.setXeroPrivateKeyPassword((String) result.get(index++).getValue());
                                           settings.setXeroConsumerKey((String) result.get(index++).getValue());
                                           settings.setXeroConsumerSecret((String) result.get(index++).getValue());
                                           settings.setXeroBrandingThemeID((String) result.get(index++).getValue());
                                           settings.setXeroContactLink((String) result.get(index++).getValue());
                                           settings.setXeroInvoiceLink((String) result.get(index++).getValue());
                                           if (presenter != null) {
                                               presenter.settingsHaveChanged();
                                           }
                                       }

                                       @Override
                                       public void onCancel() {

                                       }
                                   },
                new TextProperty("Path to private key", "", settings.getXeroPathToPrivateKey(), true, 0, 1000),
                new PasswordProperty("Private key password", "", settings.getXeroPrivateKeyPassword(), true, 0, 100),
                new TextProperty("Consumer key", "", settings.getXeroConsumerKey(), true, 0, 100),
                new PasswordProperty("Consumer secret", "", settings.getXeroConsumerSecret(), true, 0, 100),
                new TextProperty("Branding theme ID", "", settings.getXeroBrandingThemeID(), true, 0, 100),
                new TextProperty("Contact link", "", settings.getXeroContactLink(), true, 0, 1000),
                new TextProperty("Invoice link", "", settings.getXeroInvoiceLink(), true, 0, 1000)
        );

        hubspotSettings.setProperties(new CompletionHandler<List<Property>>() {
                                          @Override
                                          public void onCompleted(List<Property> result) throws ValidationException {
                                              int index = 0;
                                              settings.setHubSpotAPIKey((String) result.get(index++).getValue());
                                              settings.setHubSpotAPIId((String) result.get(index++).getValue());
                                              settings.setHubSpotClientId((String) result.get(index++).getValue());
                                              settings.setHubSpotClientSecret((String) result.get(index++).getValue());
                                              settings.setHubSpotRefreshToken((String) result.get(index++).getValue());
                                              settings.setHubSpotDealLink((String) result.get(index++).getValue());
                                              if (presenter != null) {
                                                  presenter.settingsHaveChanged();
                                              }
                                          }

                                          @Override
                                          public void onCancel() {

                                          }
                                      },
                new PasswordProperty("API key", "", settings.getHubSpotAPIKey(), true, 0, 100),
                new TextProperty("App ID", "", settings.getHubSpotAPIId(), true, 0, 100),
                new TextProperty("Client ID", "", settings.getHubSpotClientId(), true, 0, 100),
                new PasswordProperty("Client secret", "", settings.getHubSpotClientSecret(), true, 0, 100),
                new TextProperty("Refresh token", "", settings.getHubSpotRefreshToken(), true, 0, 100),
                new TextProperty("HubSpot deal link", "", settings.getHubSpotDealLink(), true, 0, 1000)
        );

        customerApplicationSettings.setProperties(new CompletionHandler<List<Property>>() {
                      @Override
                      public void onCompleted(List<Property> result) throws ValidationException {
                          int index = 0;
                          settings.setAboutUsURL((String) result.get(index++).getValue());
                          settings.setContactUsURL((String) result.get(index++).getValue());
                          settings.setContactInfoSales((String) result.get(index++).getValue());
                          settings.setHelpLink((String) result.get(index++).getValue());
                          settings.setTermsAndConditionsURL((String) result.get(index++).getValue());
                          settings.setWMSLayersUrl((String) result.get(index++).getValue());
                          settings.setProductServiceWMSURL((String) result.get(index++).getValue());
                          settings.setMaxArea(((Double) result.get(index++).getValue()) * 1000.0 * 1000.0);
                          settings.setQueryLimit((Integer) result.get(index++).getValue());
                          if (presenter != null) {
                              presenter.settingsHaveChanged();
                          }
                      }

                      @Override
                      public void onCancel() {

                      }
                  },
/*
                new ImageUrlProperty("Logo", "The home icon for the application", settings.getLogoUrl(), true, 140, 40),
                new TextProperty("Home URL", "The URL to open when the user clicks on the home icon", settings.getHomeURL(), true, 0, 1000, "", 5, false),
*/
                new TextProperty("About Us URL", "URL to your About Us web page", settings.getAboutUsURL(), true, 0, 1000),
                new TextProperty("Contact us", "URL to your Contact Us web page form", settings.getContactUsURL(), true),
                new TextProperty("Contact Sales Info", "The contact details of a sales representative, appears in the banner of the request web page", settings.getAboutUsURL(), true, 0, 100),
                new TextProperty("Help Link", "", settings.getHelpLink(), true, 0, 1000),
                new TextProperty("Terms and Conditions URL", "", settings.getTermsAndConditionsURL(), true, 0, 1000),
                new TextProperty("WMS layers URL", "", settings.getWMSLayersUrl(), true, 0, 1000),
                new TextProperty("Product WMS layers service URL", "", settings.getProductServiceWMSURL(), true, 0, 1000),
                new DoubleProperty("Max Area", "The max area authorised per AoI in sqkms", settings.getMaxArea() / 1000.0 / 1000.0, true, 0, Double.MAX_VALUE),
                new IntegerProperty("Max number of results returned", "", settings.getQueryLimit(), true, 0, 1000)
        );

        otherSettings.setProperties(new CompletionHandler<List<Property>>() {
                      @Override
                      public void onCompleted(List<Property> result) throws ValidationException {
                          int index = 0;
                          settings.setPlanetAPIToken((String) result.get(index++).getValue());
                          settings.setPlanetAPISubscriptionId((Integer) result.get(index++).getValue());
                          settings.setWebsiteUrl((String) result.get(index++).getValue());
                          if (presenter != null) {
                              presenter.settingsHaveChanged();
                          }
                      }

                      @Override
                      public void onCancel() {

                      }
                  },
                new TextProperty("Planet API token", "The Planet API token, used for ordering Planet products", settings.getPlanetAPIToken(), true, 0, 1000),
                new IntegerProperty("Planet API subscription id", "The Planet API subscription id, used for ordering Planet products", settings.getPlanetAPISubscriptionId() == null ? 0 : settings.getPlanetAPISubscriptionId(), true, 0, Integer.MAX_VALUE),
                new TextProperty("Website URL", "The public URL for the website, used for links in emails etc...", settings.getWebsiteUrl(), true, 0, 1000)
        );

    }

    @Override
    public void displaySave(boolean display) {
        uploadChanges.setVisible(display);
    }

    @Override
    public HasClickHandlers getSaveButton() {
        return uploadChanges;
    }

    @Override
    public void displaySuccess(String message) {
        Window.alert(message);
    }

    @Override
    public void displayAccountValues(String result) {
        planetAccountValues.clear();
        String values = "<p>Planet account values</p>";
        JSONObject jsonObject = JSONParser.parseLenient(result).isObject();
        for(String keyName : jsonObject.keySet()) {
            values += "<div style='margin-left: 20px'><b>" + keyName + "</b> " + jsonObject.get(keyName).toString() + "</div>";
        }
        planetAccountValues.add(new HTML(values));
    }

    private void addProperty(String name, Object value) {
        WidgetUtil.addGridProperty(settings, name, value == null ? "Not available" : value.toString());
    }

}
