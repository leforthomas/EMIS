package com.geocento.webapps.earthimages.emis.common.client.style;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface StyleResources extends ClientBundle {

    public StyleResources INSTANCE =
            GWT.create(StyleResources.class);

    @Source({"Style.css", "Defs.css", "DataGrid.css"})
	Style style();

    // for widgets to move to libraries
    @Source("img/arrowLeftLeftFacing.png")
    ImageResource arrowLeftLeftFacing();

    @Source("img/arrowLeftRightFacing.png")
    ImageResource arrowLeftRightFacing();

    @Source("img/arrowRightLeftFacing.png")
    ImageResource arrowRightLeftFacing();

    @Source("img/arrowRightRightFacing.png")
    ImageResource arrowRightRightFacing();

    @Source("img/arrowDownSmall.png")
    ImageResource arrowDownSmall();

    @Source("img/arrowUpSmall.png")
    ImageResource arrowUpSmall();

    @Source("img/arrowLeftSmall.png")
    ImageResource arrowLeftSmall();

    // for application widgets
    @Source("img/icon-save-to-disk-white.png")
    ImageResource save();

    @Source("img/glyphicons_151_edit_small_white.png")
    ImageResource editMiniWhite();

    @Source("img/export.png")
    ImageResource exportSmall();

    @Source("img/glyphicons_027_search.png")
    ImageResource zoomLarge();

    @Source("img/icon-add.png")
    ImageResource addIcon();

    @Source("img/glyphicons_333_bell.png")
    ImageResource automaticAlert();

    @Source("img/grid 1.png")
    ImageResource worldGridWhite();

    @Source("img/74-location.png")
    ImageResource displayCoordinates();

    @Source("img/glyphicons_027_search.png")
    ImageResource setCenter();

    @Source("img/globe-green.png")
    ImageResource globeIcon();

    @Source("img/map-pin.png")
    ImageResource mapIcon();

    @Source("img/186-ruler.png")
    ImageResource ruler();

    @Source("img/orders.png")
    ImageResource order();

    @Source("img/satellite-small.png")
    ImageResource satellite();

    @Source("img/instrument.png")
    ImageResource sensor();

    @Source("img/mode.png")
    ImageResource mode();

    @Source("img/toggle-small.png")
    ImageResource toggle();

    @Source("img/toggle-small-expand.png")
    ImageResource toggleExpand();

    @Source("img/future.png")
    ImageResource constellation();

    @Source("img/glyphicons_063_power.png")
    ImageResource logOut();

    @Source("img/folder-horizontal-open.png")
    ImageResource folder();

    @Source("img/glyphicons_187_more.png")
    ImageResource expandMenu();

    @Source("img/download.png")
    ImageResource loadQuery();

    @Source("img/export.png")
    ImageResource export();

    @Source("img/import.png")
    ImageResource importFile();

    @Source("img/circle.png")
    ImageResource circle();

    @Source("img/rectangle.png")
    ImageResource rectangle();

    @Source("img/polygon.png")
    ImageResource polygon();

    @Source("img/mapExtent.png")
    ImageResource mapExtent();

    @Source("img/close_small.png")
    ImageResource closeSmall();

    @Source("img/status-offline.png")
    ImageResource statusOff();

    @Source("img/document.png")
    ImageResource option();

    @Source("img/query.png")
    ImageResource query();

    @Source("img/new.png")
    ImageResource newQuery();

    @Source("img/measuredCloud.png")
    ImageResource measured();

    @Source("img/forecastClouds.png")
    ImageResource forecast();

    @Source("img/statsCloud.png")
    ImageResource statistics();

    @Source("img/addCartIcon.png")
    ImageResource cartIcon();

    @Source("img/removeCartIcon.png")
    ImageResource uncartIcon();

    @Source("img/cartUnavailable.png")
    ImageResource cartGrayed();

    @Source("img/glyphicons_152_new_window.png")
    ImageResource detachSmall();

    @Source("img/mapIconGrayed.png")
    ImageResource mapIconNotDisplayed();

    @Source("img/mapIconGrayed.png")
    ImageResource drawMapExtent();

    @Source("img/zoomBack.png")
    ImageResource zoomBack();

    @Source("img/arrow-circle-135.png")
    ImageResource inProduction();

    @Source("img/exclamation-red.png")
    ImageResource failed();

    @Source("img/validate.png")
    ImageResource completed();

    @Source("img/hand-share.png")
    ImageResource submitProduct();

    @Source("img/category.png")
    ImageResource categoryIcon();

    @Source("img/glyphicons_195_circle_info.png")
    ImageResource help();

    @Source("img/glyphicons_195_circle_info.png")
    ImageResource drawAoI();

    @Source("img/slider.png")
    ImageResource slider();

    @Source("img/control-stop-180-small.png")
    ImageResource timeFrameBack();

    @Source("img/control-stop-000-small.png")
    ImageResource timeFrameForward();

    @Source("img/brightness-small.png")
    ImageResource dayLightTerminator();

    @Source("img/brightness-small-off.png")
    ImageResource dayLightTerminatorOff();

    @Source("img/minimize.png")
    ImageResource minimize();

    @Source("img/displayed.png")
    ImageResource settings();

    @Source("img/arrowBack.png")
    ImageResource arrowBack();

    @Source("img/arrowForward.png")
    ImageResource arrowForward();

    @Source("img/download.png")
    ImageResource download();

    @Source("img/tracker.png")
    ImageResource tracker();

    @Source("img/arrow-move.png")
    ImageResource dragAndDrop();

    @Source("img/layer.png")
    ImageResource layer();

    @Source("img/timeLayer.png")
    ImageResource timeLayer();

    @Source("img/layers-stack.png")
    ImageResource layers();

    @Source("img/041.png")
    ImageResource archive();

    @Source("img/glyphicons_015_print.png")
    ImageResource print();

    @Source("img/table.png")
    ImageResource table();

    @Source("img/sedasSmaller.png")
    ImageResource smallerLogo();

    @Source("img/UK Space Agency Logo.png")
    ImageResource uksaLogo();

    @Source("img/SA_SM_White.png")
    ImageResource catapultLogo();

    @Source("img/geocento_white.png")
    ImageResource geocentoLogo();

    @Source("img/logotipo-earthimages.jpg")
    ImageResource logoLarge();

    @Source("img/logotipo-earthimages.jpg")
    ImageResource logoApplication();

    @Source("img/informationIconTiny.png")
    ImageResource helpSmall();

    public interface Style extends CssResource {

        @ClassName("gwt-SplitLayoutPanel-VDragger")
        String gwtSplitLayoutPanelVDragger();

        String eventIcon();

        String sensorLabel();

        String timeLabel();

        @ClassName("ei-largeTextShadow")
        String eiLargeTextShadow();

        @ClassName("gwt-Frame")
        String gwtFrame();

        String breadcrumb();

        String treeDraggedOver();

        String nolists();

        String notVerified();

        String pastilleTabs();

        String shadow();

        String verified();

        @ClassName("ei-pageHeader")
        String eiPageHeader();

        String niceredBackgroundGradient();

        String iconAnchorToggled();

        String niceblueBackground();

        @ClassName("ei-textShadow")
        String eiTextShadow();

        @ClassName("ei-orangeAnchorButton")
        String eiOrangeAnchorButton();

        @ClassName("gwt-TimePickerPopup")
        String gwtTimePickerPopup();

        String unselectable();

        @ClassName("lib-popupTitleBar")
        String libPopupTitleBar();

        String verifiedLabel();

        @ClassName("ei-greyAnchorButton")
        String eiGreyAnchorButton();

        @ClassName("gwt-Tree")
        String gwtTree();

        String chartElement();

        @ClassName("gwt-RichTextArea")
        String gwtRichTextArea();

        String nicepinkBackgroundGradient();

        String backgroundGrayGradient();

        String active();

        @ClassName("gwt-TreeItem")
        String gwtTreeItem();

        @ClassName("gwt-SplitLayoutPanel-HDragger")
        String gwtSplitLayoutPanelHDragger();

        @ClassName("gwt-StackLayoutPanel")
        String gwtStackLayoutPanel();

        @ClassName("gwt-StackLayoutPanelHeader")
        String gwtStackLayoutPanelHeader();

        String paleblueBackground();

        String iconbutton();

        @ClassName("ei-blueAnchor")
        String eiBlueAnchor();

        @ClassName("ei-title")
        String eiTitle();

        @ClassName("ei-commentBox")
        String eiCommentBox();

        @ClassName("ei-explanation")
        String eiExplanation();

        @ClassName("gwt-CheckBox")
        String gwtCheckBox();

        @ClassName("ei-bottomBorderExceptLast")
        String eiBottomBorderExceptLast();

        String datePickerDayIsValue();

        String unselectableLabels();

        String verticalText();

        @ClassName("gwt-Label")
        String gwtLabel();

        String selected();

        String datePickerDayIsToday();

        String iconanchor();

        @ClassName("ei-paleblueBackground")
        String eiPaleblueBackground();

        @ClassName("ei-blueColor")
        String eiBlueColor();

        String nicegreenBackgroundGradient();

        @ClassName("ei-subField")
        String eiSubField();

        String durationPanel();

        String grayblueBackground();

        @ClassName("ei-blueAnchorButton")
        String eiBlueAnchorButton();

        String applyButton();

        @ClassName("ei-clipContent")
        String eiClipContent();

        @ClassName("gwt-Anchor")
        String gwtAnchor();

        String clickableTreeItemLabel();

        String portletTitle();

        String grayBackground();

        String nicepinkBackground();

        String niceblueBackgroundGradient();

        String dataGridCell();

        @ClassName("ei-latLngOverlay")
        String eiLatLngOverlay();

        @ClassName("twipsy-inner")
        String twipsyInner();

        String dataGridLastColumnFooter();

        @ClassName("nav-header")
        String navHeader();

        String right();

        String dataGridSortableHeader();

        String dataGridKeyboardSelectedRow();

        String dataGridOddRowCell();

        String dataGridFooter();

        String dataGridKeyboardSelectedCell();

        @ClassName("twipsy-arrow")
        String twipsyArrow();

        String dataGridEvenRow();

        String left();

        String fade();

        @ClassName("ei-imageColumn")
        String eiImageColumn();

        String dataGridSelectedRow();

        String dataGridOddRow();

        String tabbable();

        String dataGridFirstColumn();

        @ClassName("ei-archiveProduct")
        String eiArchiveProduct();

        String dataGridFirstColumnHeader();

        String dataGridHoveredRow();

        String dataGridSortedHeaderDescending();

        @ClassName("tabs-below")
        String tabsBelow();

        String menuItem();

        @ClassName("nav-tabs")
        String navTabs();

        String dataGridLastColumn();

        String dataGridHoveredRowCell();

        @ClassName("tabs-left")
        String tabsLeft();

        String dataGridWidget();

        String dataGridSelectedRowCell();

        String dataGridSortedHeaderAscending();

        String twipsy();

        String dataGridEvenRowCell();

        String dataGridFirstColumnFooter();

        @ClassName("ei-futureOpportunityProduct")
        String eiFutureOpportunityProduct();

        @ClassName("tab-content")
        String tabContent();

        String below();

        String above();

        @ClassName("ei-plannedAcqProduct")
        String eiPlannedAcqProduct();

        String dataGridHeader();

        String dataGridLastColumnHeader();

        String dataGridKeyboardSelectedRowCell();

        @ClassName("tabs-right")
        String tabsRight();

        String in();

        String nav();

        @ClassName("tab-pane")
        String tabPane();

        String scrollVertical();

        String titlePanel();

        @ClassName("ei-darkerPinkAnchorButton")
        String eiDarkerPinkAnchorButton();

        String actionAnchorButton();

        String actionAnchorButtonDisabled();

        String cancelActionAnchorButton();

        String slimScrollbar();
    }

    @Source("img/sedas.png")
    ImageResource smallLogo();

    @Source("img/glyphicons_027_search.png")
    ImageResource zoom();

    @Source("img/trashbin.png")
    ImageResource remove();

    @Source("img/icon23.png")
    ImageResource cart();

    @Source("img/validate.png")
    ImageResource validate();

    @Source("img/error.png")
    ImageResource error();

    @Source("img/cancel.png")
    ImageResource cancel();

    @Source("img/ajax-loader.gif")
    ImageResource loading();

    @Source("img/bell-small.png")
    ImageResource alert();

    @Source("img/edit.png")
    ImageResource edit();

    @Source("img/glyphicons_151_edit.png")
    ImageResource editSmall();

    @Source("img/menu-down-arrow.png")
    ImageResource menuDownArrow();

    @Source("img/info.png")
    ImageResource info();

    @Source("img/balloon-ellipsis.png")
    ImageResource comment();

}
