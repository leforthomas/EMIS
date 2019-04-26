package com.geocento.webapps.earthimages.emis.admin.client.view;

import com.geocento.webapps.earthimages.emis.admin.client.Admin;
import com.geocento.webapps.earthimages.emis.admin.client.ClientFactory;
import com.geocento.webapps.earthimages.emis.admin.client.event.SampleChanged;
import com.geocento.webapps.earthimages.emis.admin.client.place.AISamplesPlace;
import com.geocento.webapps.earthimages.emis.admin.client.widgets.SamplesList;
import com.geocento.webapps.earthimages.emis.admin.share.SampleDTO;
import com.geocento.webapps.earthimages.emis.common.client.popup.LoadingPanel;
import com.geocento.webapps.earthimages.emis.common.client.popup.PopupPropertyEditor;
import com.geocento.webapps.earthimages.emis.common.client.style.StyleResources;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.metaaps.webapps.libraries.client.property.domain.Property;
import com.metaaps.webapps.libraries.client.property.domain.TextProperty;
import com.metaaps.webapps.libraries.client.widget.AsyncPagingCellTable;
import com.metaaps.webapps.libraries.client.widget.CompletionHandler;
import com.metaaps.webapps.libraries.client.widget.ValidationException;
import com.metaaps.webapps.libraries.client.widget.util.ListUtil;

import java.util.List;

public class AISamplesViewImpl extends Composite implements AISamplesView {

	private static AIPublishViewImplUiBinder uiBinder = GWT
			.create(AIPublishViewImplUiBinder.class);

    interface AIPublishViewImplUiBinder extends UiBinder<Widget, AISamplesViewImpl> {
	}

	static private StyleResources styles = GWT.create(StyleResources.class);

	public interface Style extends CssResource {
	}

	@UiField
    Style style;

	@UiField
    AIApplicationTemplateView templateView;
    @UiField
    Button refreshLogfile;
    @UiField
    TextBox filter;
    @UiField
    SamplesList samplesList;

    private ClientFactory clientFactory;

	private Presenter presenter;
	
	public AISamplesViewImpl(ClientFactory clientFactory) {
		
		this.clientFactory = clientFactory;

        initWidget(uiBinder.createAndBindUi(this));

        templateView.setPlace(new AISamplesPlace());

        samplesList.setPresenter(new AsyncPagingCellTable.Presenter() {
            @Override
            public void rangeChanged(int start, int length, Column<?, ?> column, boolean isAscending) {
                presenter.rangeChanged(start, length, samplesList.getSortBy(), isAscending);
            }
        });

        filter.addKeyDownHandler(new KeyDownHandler() {

            @Override
            public void onKeyDown(KeyDownEvent event) {
                presenter.filterHasChanged();
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
    public HasClickHandlers getRefreshButton() {
        return refreshLogfile;
    }

    @Override
    public void displaySuccess(String message) {
        Window.alert(message);
    }

    @Override
    public String getFilter() {
        return filter.getValue();
    }

    @Override
    public void editSample(SampleDTO sampleDTO) {
        List<Property> properties = ListUtil.toList(new Property[] {
                new TextProperty("Product Order ID", null, sampleDTO.getId(), false),
                new TextProperty("Title", null, sampleDTO.getName(), true, 0, 100),
                new TextProperty("Description", null, sampleDTO.getDescription(), true, 0, 100),
                new TextProperty("Keywords", null, sampleDTO.getKeywords(), true, 0, 10000)
        });
        PopupPropertyEditor.getInstance().edit("Publish product as sample value",
                properties,
                new CompletionHandler<List<Property>>() {
                    @Override
                    public void onCompleted(List<Property> result) throws ValidationException {
                        int index = 0;
                        String productOrderId = (String) result.get(index++).getValue();
                        String title = (String) result.get(index++).getValue();
                        String description = (String) result.get(index++).getValue();
                        String keyWords = (String) result.get(index++).getValue();
                        sampleDTO.setName(title);
                        sampleDTO.setDescription(description);
                        sampleDTO.setKeywords(keyWords);
                        Admin.clientFactory.getEventBus().fireEvent(new SampleChanged(sampleDTO));
                        PopupPropertyEditor.getInstance().hide();
                    }

                    @Override
                    public void onCancel() {

                    }
                });
    }

    @Override
    public void setSamples(int start, int limit, String sortBy, boolean isAscending, List<SampleDTO> samples) {
	    samplesList.setSortBy(sortBy, isAscending);
        samplesList.setRowData(start, samples);
    }

}
