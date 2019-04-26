package com.geocento.webapps.earthimages.emis.admin.client.view;

import com.geocento.webapps.earthimages.emis.admin.client.Admin;
import com.geocento.webapps.earthimages.emis.admin.client.ClientFactory;
import com.geocento.webapps.earthimages.emis.admin.client.event.ProductTaskChanged;
import com.geocento.webapps.earthimages.emis.admin.client.place.AIPublishPlace;
import com.geocento.webapps.earthimages.emis.admin.client.widgets.ProductFetchTaskList;
import com.geocento.webapps.earthimages.emis.admin.share.ProductFetchTaskDTO;
import com.geocento.webapps.earthimages.emis.common.client.popup.LoadingPanel;
import com.geocento.webapps.earthimages.emis.common.client.popup.PopupPropertyEditor;
import com.geocento.webapps.earthimages.emis.common.client.style.StyleResources;
import com.geocento.webapps.earthimages.emis.common.share.entities.STATUS;
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
import com.metaaps.webapps.libraries.client.property.domain.ChoiceProperty;
import com.metaaps.webapps.libraries.client.property.domain.Property;
import com.metaaps.webapps.libraries.client.widget.AsyncPagingCellTable;
import com.metaaps.webapps.libraries.client.widget.CompletionHandler;
import com.metaaps.webapps.libraries.client.widget.ValidationException;
import com.metaaps.webapps.libraries.client.widget.util.ListUtil;
import com.metaaps.webapps.libraries.client.widget.util.Utils;

import java.util.List;

public class AIPublishViewImpl extends Composite implements AIPublishView {

	private static AIPublishViewImplUiBinder uiBinder = GWT
			.create(AIPublishViewImplUiBinder.class);

    interface AIPublishViewImplUiBinder extends UiBinder<Widget, AIPublishViewImpl> {
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
    ProductFetchTaskList productFetchTaskList;

    private ClientFactory clientFactory;

	private Presenter presenter;
	
	public AIPublishViewImpl(ClientFactory clientFactory) {
		
		this.clientFactory = clientFactory;

        initWidget(uiBinder.createAndBindUi(this));

        templateView.setPlace(new AIPublishPlace());

        productFetchTaskList.setPresenter(new AsyncPagingCellTable.Presenter() {
            @Override
            public void rangeChanged(int start, int length, Column<?, ?> column, boolean isAscending) {
                presenter.rangeChanged(start, length, productFetchTaskList.getSortBy(), isAscending);
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
    public void editProductFetchTask(ProductFetchTaskDTO editedProductTask) {
        PopupPropertyEditor.getInstance().edit("Edit fetch task", null,
                ListUtil.toList(new Property[]{
                        new ChoiceProperty("Status of the task", "The status for this task", editedProductTask.getStatus().toString(), true, true, Utils.enumNameToStringArray(STATUS.values()))
                }), new CompletionHandler<List<Property>>() {
                    @Override
                    public void onCompleted(List<Property> result) throws ValidationException {
                        int index = 0;
                        editedProductTask.setStatus(STATUS.valueOf((String) result.get(index++).getValue()));
                        Admin.clientFactory.getEventBus().fireEvent(new ProductTaskChanged(editedProductTask));
                    }

                    @Override
                    public void onCancel() {

                    }
                });
    }

    @Override
    public void setProductFetchTasks(int start, int limit, String sortBy, boolean isAscending, List<ProductFetchTaskDTO> productFetchTasks) {
	    productFetchTaskList.setSortBy(sortBy, isAscending);
        productFetchTaskList.setRowData(start, productFetchTasks);
    }

}
