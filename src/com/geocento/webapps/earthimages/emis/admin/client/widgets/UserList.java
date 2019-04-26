package com.geocento.webapps.earthimages.emis.admin.client.widgets;

import com.geocento.webapps.earthimages.emis.admin.client.event.AddCreditEvent;
import com.geocento.webapps.earthimages.emis.admin.client.event.LoadUsersEvent;
import com.geocento.webapps.earthimages.emis.admin.client.event.RemoveUserEvent;
import com.geocento.webapps.earthimages.emis.admin.client.event.UserChanged;
import com.geocento.webapps.earthimages.emis.admin.share.CreditDTO;
import com.geocento.webapps.earthimages.emis.admin.share.UserDTO;
import com.geocento.webapps.earthimages.emis.common.client.popup.PopupPropertyEditor;
import com.geocento.webapps.earthimages.emis.common.client.style.MyCellTableResources;
import com.geocento.webapps.earthimages.emis.common.share.entities.Price;
import com.geocento.webapps.earthimages.emis.common.share.entities.TRANSACTION_TYPE;
import com.geocento.webapps.earthimages.emis.common.share.entities.USER_ROLE;
import com.geocento.webapps.earthimages.emis.common.share.entities.USER_STATUS;
import com.geocento.webapps.earthimages.emis.common.share.utils.UserHelper;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SelectionCell;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.web.bindery.event.shared.EventBus;
import com.metaaps.webapps.libraries.client.property.domain.*;
import com.metaaps.webapps.libraries.client.widget.AsyncPagingCellTable;
import com.metaaps.webapps.libraries.client.widget.CompletionHandler;
import com.metaaps.webapps.libraries.client.widget.CountryEditor;
import com.metaaps.webapps.libraries.client.widget.ValidationException;
import com.metaaps.webapps.libraries.client.widget.util.DateUtil;
import com.metaaps.webapps.libraries.client.widget.util.ListUtil;
import com.metaaps.webapps.libraries.client.widget.util.Utils;

import java.util.List;

public class UserList extends AsyncPagingCellTable<UserDTO> {

	private TextColumn<UserDTO> usernameColumn;
	private SingleSelectionModel<UserDTO> selectionModel;
	private Column<UserDTO, String> thumbnailColumn;
	private TextColumn<UserDTO> emailColumn;
	private TextColumn<UserDTO> lastLoggedInColumn;
	private Column<UserDTO, String> editColumn;
	private TextColumn<UserDTO> typeColumn;
    private TextColumn<UserDTO> statusColumn;
    private TextColumn<UserDTO> creditColumn;
    private TextColumn<UserDTO> lastDownloadColumn;

    private final EventBus eventBus;

	public UserList(EventBus eventBus) {
        super(10, MyCellTableResources.INSTANCE);
        this.eventBus = eventBus;
		pager.setPageStart(0);
	}

	@Override
	public void initTableColumns(CellTable<UserDTO> dataGrid) {

        editColumn = new Column<UserDTO, String>(new ButtonCell()) {
            @Override
            public String getValue(UserDTO object) {
                return "Edit";
            }
        };
        addColumn(editColumn, "Action", "100px");
        editColumn.setFieldUpdater(new FieldUpdater<UserDTO, String>() {

            @Override
            public void update(int index, final UserDTO userDTO, String value) {
                boolean hasCurrency = userDTO.getCredit() != null && userDTO.getCredit().getCurrency() != null;
                PopupPropertyEditor.getInstance().edit("Edit user " + userDTO.getUsername(), ListUtil.toList(new Property[]{
                        new TextProperty("Email", "So we can contact you for order updates and to recover your password", userDTO.getEmail(), true, 5, 200, UserHelper.emailregExp),
                        new TextProperty("Password", "Do not fill in if you want to leave the password unchanged", null, true, 5, 100),
                        new ChoiceProperty("User Status", "The registration status for this user", userDTO.getUserStatus().toString(), true, true, new String[]{USER_STATUS.APPROVED.toString(), USER_STATUS.SUSPENDED.toString()}),
                        new ChoiceProperty("User Role", "The role for this user", userDTO.getUserRole().toString(), true, true, Utils.enumNameToStringArray(USER_ROLE.values())),
                        new BooleanProperty("Can order", "Whether the user can order or not", userDTO.isCanOrder(), true),
                        new BooleanProperty("Charge VAT", "Whether the user needs to be charged UK VAT", userDTO.isChargeVAT(), true),
                        new BooleanProperty("Needs VAT", "Whether the user needs to specify their community VAT number", userDTO.isNeedsVATNumber(), true),
                        new TextProperty("Community VAT number", "Provide the community VAT number", userDTO.getCommunityVATNumber(), true, 0, 100),
                        new ChoiceProperty("User currency", "The currency for the user", hasCurrency ? userDTO.getCredit().getCurrency() : null, true, !hasCurrency, Price.supportedCurrencies)
                }), new CompletionHandler<List<Property>>() {
                    @Override
                    public void onCompleted(List<Property> result) throws ValidationException {
                        int index = 0;
                        userDTO.setEmail((String) result.get(index++).getValue());
                        userDTO.setPassword((String) result.get(index++).getValue());
                        userDTO.setUserStatus(USER_STATUS.valueOf((String) result.get(index++).getValue()));
                        userDTO.setUserRole(USER_ROLE.valueOf((String) result.get(index++).getValue()));
                        userDTO.setCanOrder((Boolean) result.get(index++).getValue());
                        userDTO.setChargeVAT((Boolean) result.get(index++).getValue());
                        userDTO.setNeedsVATNumber((Boolean) result.get(index++).getValue());
                        userDTO.setCommunityVATNumber((String) result.get(index++).getValue());
                        if(!hasCurrency) {
                            CreditDTO creditDTO = new CreditDTO();
                            creditDTO.setAmount(0);
                            creditDTO.setCurrency((String) result.get(index++).getValue());
                            userDTO.setCredit(creditDTO);
                        }
                        eventBus.fireEvent(new UserChanged(userDTO));
                    }

                    @Override
                    public void onCancel() {

                    }
                });
            }
        });

        usernameColumn = new TextColumn<UserDTO>() {
		      @Override
		      public String getValue(UserDTO object) {
		        return object.getUsername();
		      }
		    };
		addResizableColumn(usernameColumn, "User Name", "100px");
	    usernameColumn.setSortable(true);
	    
	    emailColumn = new TextColumn<UserDTO>() {
		      @Override
		      public String getValue(UserDTO object) {
		        return object.getEmail();
		      }
		    };
		addResizableColumn(emailColumn, "Email", "100px");
		emailColumn.setSortable(true);
	    
	    typeColumn = new TextColumn<UserDTO>() {
		      @Override
		      public String getValue(UserDTO object) {
		        return object.getUserRole().toString();
		      }
		    };
		addColumn(typeColumn, "Role", "100px");

        statusColumn = new TextColumn<UserDTO>() {
            @Override
            public String getValue(UserDTO object) {
                return object.getUserStatus().toString();
            }
        };
        addColumn(statusColumn, "Status", "100px");

        lastLoggedInColumn = new TextColumn<UserDTO>() {
		      @Override
		      public String getValue(UserDTO object) {
		        return DateUtil.displaySimpleDate(object.getLastLoggedIn());
		      }
		    };
		addResizableColumn(lastLoggedInColumn, "Last logged in", "100px");
		lastLoggedInColumn.setSortable(true);

        TextColumn<UserDTO> firstNameColumn = new TextColumn<UserDTO>() {
            @Override
            public String getValue(UserDTO object) {
                return object.getFirstName() == null ? "Not set" : object.getFirstName();
            }
        };
        addResizableColumn(firstNameColumn, "First Name", "100px");

        TextColumn<UserDTO> lastNameColumn = new TextColumn<UserDTO>() {
            @Override
            public String getValue(UserDTO object) {
                return object.getLastName() == null ? "Not set" : object.getLastName();
            }
        };
        addResizableColumn(lastNameColumn, "Last Name", "100px");

        TextColumn<UserDTO> organisationColumn = new TextColumn<UserDTO>() {
            @Override
            public String getValue(UserDTO object) {
                return object.getCompany() == null ? "Not set" : object.getCompany();
            }
        };
        addResizableColumn(organisationColumn, "Organisation", "100px");

        TextColumn<UserDTO> countryColumn = new TextColumn<UserDTO>() {
            @Override
            public String getValue(UserDTO object) {
                return object.getCountryCode() == null ? "Not set" : CountryEditor.getDisplayName(object.getCountryCode());
            }
        };
        addResizableColumn(countryColumn, "Country", "100px");

        TextColumn<UserDTO> domainColumn = new TextColumn<UserDTO>() {
            @Override
            public String getValue(UserDTO object) {
                return object.getDomain() == null ? "Not set" : object.getDomain().toString();
            }
        };
        addResizableColumn(domainColumn, "Domain", "100px");

        creditColumn = new TextColumn<UserDTO>() {
            @Override
            public String getValue(UserDTO object) {
                return object.getCredit() == null ? "No credits" : com.geocento.webapps.earthimages.emis.common.client.utils.Utils.displayPrice(new Price(object.getCredit().getAmount(), object.getCredit().getCurrency()));
            }
        };
        addResizableColumn(creditColumn, "Credit", "100px");

        Column<UserDTO, String> addCreditColumn = new Column<UserDTO, String>(new ButtonCell()) {
            @Override
            public String getValue(UserDTO object) {
                return "Add Credit";
            }
        };
        addColumn(addCreditColumn, "Credit", "100px");
        addCreditColumn.setFieldUpdater(new FieldUpdater<UserDTO, String>() {

            @Override
            public void update(int index, final UserDTO userDTO, String value) {
                if(eventBus != null) {
                    PopupPropertyEditor.getInstance().edit("Add credit", "Add credit to user account " + userDTO.getUsername(),
                            ListUtil.toList(new Property[]{
                                    new ChoiceProperty("Credit type", null, null, true, true, new String[] {"BACS transfer", "Voucher", "Purchase"}, Utils.enumNameToStringArray(new TRANSACTION_TYPE[] {TRANSACTION_TYPE.bacsTransfer, TRANSACTION_TYPE.voucher, TRANSACTION_TYPE.purchase})),
                                    new DoubleProperty("Credit amount", null, 0.0, true, -100000.0, 100000.0),
                                    new ChoiceProperty("Currency", null, "GBP", true, true, Price.supportedCurrencies),
                                    new TextProperty("Comment", null, null, true, 0, 1000)
                            }), new CompletionHandler<List<Property>>() {
                                @Override
                                public void onCompleted(List<Property> result) throws ValidationException {
                                    int index = 0;
                                    TRANSACTION_TYPE transactionType = TRANSACTION_TYPE.valueOf((String) result.get(index++).getValue());
                                    double amount = (double) result.get(index++).getValue();
                                    String currency = (String) result.get(index++).getValue();
                                    String comment = (String) result.get(index++).getValue();
                                    eventBus.fireEvent(new AddCreditEvent(userDTO.getUsername(), transactionType, amount, currency, comment));
                                }

                                @Override
                                public void onCancel() {

                                }
                            });
                }
            }
        });

        Column<UserDTO, String> removeColumn = new Column<UserDTO, String>(new ButtonCell()) {
            @Override
            public String getValue(UserDTO object) {
                return "Remove";
            }
        };
        addColumn(removeColumn, "Remove", "100px");
        removeColumn.setFieldUpdater(new FieldUpdater<UserDTO, String>() {

            @Override
            public void update(int index, final UserDTO userDTO, String value) {
                if(eventBus != null) {
                    eventBus.fireEvent(new RemoveUserEvent(userDTO.getUsername()));
                }
            }
        });

        String[] actions = new String[]{"Choose", "Transactions", "Orders"};
        SelectionCell categoryCell = new SelectionCell(ListUtil.toList(actions));
        Column<UserDTO, String> actionColumn = new Column<UserDTO, String>(categoryCell) {
            @Override
            public String getValue(UserDTO object) {
                return actions[0];
            }
        };
        addColumn(actionColumn, "Download", "100px");
        actionColumn.setFieldUpdater((FieldUpdater<UserDTO, String>) (index, userDTO, value) -> {
            switch (value) {
                case "Transactions": {
                    Window.open("./api/orders/transactions/" + userDTO.getUsername() + "/download/csv", "_blank", null);
                } break;
                case "Orders": {
                    Window.open("./api/orders/products/" + userDTO.getUsername() + "/download/csv", "_blank", null);
                } break;
            }
        });

        dataGrid.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
	    // Add a selection model to handle user selection.
	    selectionModel = new SingleSelectionModel<UserDTO>();
	    dataGrid.setSelectionModel(selectionModel);
	    selectionModel.addSelectionChangeHandler(event -> {
              UserDTO selected = selectionModel.getSelectedObject();
              if(selected != null) {
              }
        });
        lastLoggedInColumn.setDefaultSortAscending(false);
        dataGrid.getColumnSortList().clear();
        dataGrid.getColumnSortList().push(lastLoggedInColumn);
	    super.setPresenter((start, length, column, isAscending) -> {
            if(eventBus != null) {
                eventBus.fireEvent(new LoadUsersEvent(start, length, getSortBy(), isAscending));
            }
        });
	    
	}

    public String getSortBy() {
        Column sortedColumn = dataGrid.getColumnSortList().get(0).getColumn();
        return sortedColumn == usernameColumn ? "username" :
                sortedColumn == emailColumn ? "emailAddress" :
                        sortedColumn == lastDownloadColumn ? "lastDownload" :
                                "lastLoggedIn";
    }

    public boolean isAscending() {
        return dataGrid.getColumnSortList().get(0).isAscending();
    }

    public int getStart() {
        return pager.getPageStart();
    }

    public void setPageSize(int pageSize) {
        pager.setPageSize(pageSize);
    }

    public int getPageSize() {
        return pager.getPageSize();
    }

}
