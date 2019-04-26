package com.geocento.webapps.earthimages.emis.application.client.style;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

/**
 * Created by thomas on 26/09/2014.
 */
public interface StyleResources extends ClientBundle {

    public StyleResources INSTANCE =
            GWT.create(StyleResources.class);

    @Source({"Style.css", "Defs.css"})
    Style style();

    @Source("img/glyphicons_096_vector_path_polygon.png")
    ImageResource whitePolygon();

    @Source("img/glyphicons_095_vector_path_circle.png")
    ImageResource whiteCircle();

    @Source("img/glyphicons_094_vector_path_square.png")
    ImageResource whiteRectangle();

    @Source("img/icon22.png")
    ImageResource whiteCalendar();

    @Source("img/share.png")
    ImageResource share();

    @Source("img/glyphicons_195_circle_info.png")
    ImageResource help();

    @Source("img/satellite-smaller.png")
    ImageResource satellite();

    @Source("img/numberOne.png")
    ImageResource numberOne();

    @Source("img/numberTwo.png")
    ImageResource numberTwo();

    @Source("img/imageType.png")
    ImageResource imageType();

    @Source("img/logoGeocento.png")
    ImageResource logoGeocento();

    @Source("img/logo-earthimages.png")
    ImageResource logoGeocentoEarthImages();

    @Source("img/rotate.png")
    ImageResource rotate();

    @Source("img/icon1.png")
    ImageResource selectPlace();

    @Source("img/icon10.png")
    ImageResource mapExtent();

    @Source("img/icon11.png")
    ImageResource zoomBack();

    @Source("img/icon13.png")
    ImageResource setCenter();

    @Source("img/icon32.png")
    ImageResource displayCoordinates();

    @Source("img/icon31.png")
    ImageResource worldGrid();

    @Source("img/icon27.png")
    ImageResource globeIcon();

    @Source("img/icon26.png")
    ImageResource mapIcon();

    @Source("img/backIcon.png")
    ImageResource timeFrameBack();

    @Source("img/forwardIcon.png")
    ImageResource timeFrameForward();

    @Source("img/icono-36.png")
    ImageResource dayLightTerminator();

    @Source("img/forwardIcon.png")
    ImageResource minimize();

    @Source("img/geocento_list_icons.png")
    ImageResource cart();

    @Source("img/icon4.png")
    ImageResource filters();

    @Source("img/icono-39.png")
    ImageResource checked();

    @Source("img/icono-38.png")
    ImageResource unchecked();

    @Source("img/icono-34-large.png")
    ImageResource cartIcon();

    @Source("img/icono-41-large.png")
    ImageResource uncartIcon();

    @Source("img/icon15-large.png")
    ImageResource export();

    @Source("img/icon14-large.png")
    ImageResource info();

    @Source("img/icon14-white.png")
    ImageResource infoWhite();

    @Source("img/infowhite.png")
    ImageResource infoWhiteSmall();

    @Source("img/icon19-large.png")
    ImageResource download();

    @Source("img/icono-35-large.png")
    ImageResource detach();

    @Source("img/icon18-large.png")
    ImageResource center();

    @Source("img/icono-42.png")
    ImageResource mapIconSmall();

    @Source("img/icon17-large.png")
    ImageResource mapIconNotDisplayed();

    @Source("img/icono-37.png")
    ImageResource settings();

    @Source("img/logotipo-earthimages.jpg")
    ImageResource logoEIExpressOrange();

    @Source("img/icono-38-white.png")
    ImageResource uncheckedWhite();

    @Source("img/icono-38-partial-white.png")
    ImageResource partialCheckedWhite();

    @Source("img/icono-39-white.png")
    ImageResource checkedWhite();

    @Source("img/mapLayers.png")
    ImageResource mapLayers();

    @Source("img/apolloCatalog.png")
    ImageResource apolloCatalogIcon();

    @Source("img/bell-icon.png")
    ImageResource imageAlertIcon();

    @Source("img/satellite-icon.png")
    ImageResource coverageRequestIcon();

    @Source("img/icono-drone2.png")
    ImageResource dronesIcon();

    @Source("img/informationIcon.png")
    ImageResource informationIcon();

    @Source("img/loading.gif")
    ImageResource loadingLarge();

    @Source("img/loading.gif")
    ImageResource loading();

    @Source("img/logoEIExpress.png")
    ImageResource logoEIExpress();

    @Source("img/polygon.png")
    ImageResource polygon();

    @Source("img/ellipse.png")
    ImageResource ellipse();

    @Source("img/circle.png")
    ImageResource circle();

    @Source("img/rectangle.png")
    ImageResource rectangle();

    @Source("img/import.png")
    ImageResource importSmall();

    @Source("img/glyphicons_027_search.png")
    ImageResource zoomLarge();

    @Source("img/glyphicons_151_edit.png")
    ImageResource editSmall();

    @Source("img/price-tag.png")
    ImageResource priceIcon();

    @Source("img/trashbin.png")
    ImageResource trashbin();

    @Source("img/trashbin-blue.png")
    ImageResource trashbinblue();

    @Source("img/displayed.png")
    ImageResource displayed();

    @Source("img/notdisplayed.png")
    ImageResource notdisplayed();

    @Source("img/arrowDownSmall.png")
    ImageResource arrowDownSmall();

    @Source("img/arrowUpSmall.png")
    ImageResource arrowUpSmall();

    @Source("img/arrowLeftSmall.png")
    ImageResource arrowLeftSmall();

    @Source("img/arrowRightSmall.png")
    ImageResource arrowRightSmall();

    @Source("img/arrowDownSmaller.png")
    ImageResource arrowDownSmaller();

    @Source("img/arrowLeftSmaller.png")
    ImageResource arrowLeftSmaller();

    @Source("img/slider.png")
    ImageResource slider();

    @Source("img/validate.png")
    ImageResource validate();

    @Source("img/error.png")
    ImageResource error();

    @Source("img/cancel.png")
    ImageResource cancel();

    @Source("img/edit.png")
    ImageResource edit();

    @Source("img/bell-small.png")
    ImageResource alert();

    @Source("img/starSmall.png")
    ImageResource starSmall();

    @Source("img/menu-down-arrow.png")
    ImageResource menuDownArrow();

    @Source("img/menu-right-arrow.png")
    ImageResource menuRightArrow();

    @Source("img/whiteArrowUp.png")
    ImageResource whiteArrowUp();

    @Source("img/geocento_logo_white_icon.png")
    ImageResource geocentoIcon();

    @Source("img/arrowBack.png")
    ImageResource arrowBack();

    @Source("img/arrowBackDisabled.png")
    ImageResource arrowBackDisabled();

    @Source("img/arrowForward.png")
    ImageResource arrowForward();

    @Source("img/arrowForwardDisabled.png")
    ImageResource arrowForwardDisabled();

    @Source("img/filter.png")
    ImageResource productFilters();

    @Source("img/orderBy.png")
    ImageResource orderBy();

    @Source("img/icono-34-large-bw.png")
    ImageResource cartGrayed();

    @Source("img/no-images-found.png")
    ImageResource noImagesFound();

    @Source("img/processing.png")
    ImageResource process();

    @Source("img/coverage.png")
    ImageResource coverage();

    @Source("img/icon-save-to-disk.png")
    ImageResource save();

    @Source("img/share.png")
    ImageResource publish();

    @Source("img/wcsDownload.png")
    ImageResource wcsDownload();

    @Source("img/starSmall.png")
    ImageResource licenseIcon();

    @Source("img/constellation-smaller.png")
    ImageResource constellation();

    @Source("img/manageAccounts.png")
    ImageResource manageAccounts();

    @Source("img/folder.png")
    ImageResource folder();

    @Source("img/file.png")
    ImageResource file();

    @Source("img/orderedIcon.png")
    ImageResource orderIcon();

    @Source("img/workspace.png")
    ImageResource workspace();

    public interface Style extends CssResource {

        String actionAnchorButton();

        String actionAnchorButtonBlue();

        String actionAnchorButtonDisabled();

        @ClassName("ei-latLngOverlay")
        String eiLatLngOverlay();

        String cancelActionAnchorButton();

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

        @ClassName("uneditable-input")
        String uneditableInput();

        @ClassName("gwt-RichTextArea")
        String gwtRichTextArea();

        String niceblueBackgroundGradient();

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

        String treeItemLabel();

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

        @ClassName("gwt-Label")
        String gwtLabel();

        String verticalText();

        String selected();

        String datePickerDayIsToday();

        String iconanchor();

        @ClassName("ei-paleblueBackground")
        String eiPaleblueBackground();

        String scrollVertical();

        @ClassName("ei-blueColor")
        String eiBlueColor();

        @ClassName("gwt-TreeNode")
        String gwtTreeNode();

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

        String niceorangeBackgroundGradient();

        @ClassName("gwt-Anchor")
        String gwtAnchor();

        String clickableTreeItemLabel();

        String portletTitle();

        String grayBackground();
    }

}
