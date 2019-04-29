package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.geocento.webapps.earthimages.emis.application.client.style.StyleResources;
import com.geocento.webapps.earthimages.emis.application.share.FileDTO;
import com.geocento.webapps.earthimages.emis.common.client.utils.Utils;
import com.google.gwt.user.client.ui.TreeItem;
import com.metaaps.webapps.libraries.client.widget.IconAnchor;
import com.metaaps.webapps.libraries.client.widget.IconLabel;

import java.util.List;

public class FileTreeWidget extends BaseTreeWidget {

    static public interface Presenter {
        void onFileSelected(FileDTO fileDTO);
    }

    private FileTreeWidget.Presenter presenter;

    public FileTreeWidget() {
        tree.setScrollOnSelectEnabled(false);
        tree.getElement().getStyle().clearProperty("position");
    }

    public void setPresenter(FileTreeWidget.Presenter presenter) {
        this.presenter = presenter;
    }

    public void setFiles(List<FileDTO> files) {
        tree.removeItems();
        addFiles(null, files);
    }

    private void addFiles(TreeItem directoryTreeItem, List<FileDTO> files) {
        for (FileDTO fileDTO : files) {
            boolean isDirectory = fileDTO.getFiles() != null;
            // add the files
            if (isDirectory) {
                IconLabel directoryWidget = new IconLabel(StyleResources.INSTANCE.folder(), fileDTO.getName());
                TreeItem treeItem = new TreeItem(directoryWidget);
                if (directoryTreeItem == null) {
                    tree.addItem(treeItem);
                } else {
                    directoryTreeItem.addItem(treeItem);
                }
                // now add the files from the directory
                addFiles(treeItem, fileDTO.getFiles());
            } else {
                IconAnchor fileWidget = new IconAnchor();
                fileWidget.setSimple(true);
                fileWidget.setResource(StyleResources.INSTANCE.file());
                fileWidget.setText(fileDTO.getName());
                fileWidget.setTooltip("File size is " + Utils.displayFileSize(fileDTO.getSizeInBytes()) + ", click to download");
/*
                HTMLPanel fileWidget = new HTMLPanel("<span>" + fileDTO.getName() + "</span>");
                Image infoIcon = new Image(StyleResources.INSTANCE.info());
                Tooltip.getTooltip().registerTooltip(infoIcon, "File size is " + Utils.displayFileSize(fileDTO.getSizeInBytes()));
                fileWidget.add(infoIcon);
                Image downloadIcon = new Image(StyleResources.INSTANCE.download());
                Tooltip.getTooltip().registerTooltip(downloadIcon, "Download file");
                downloadIcon.addClickHandler(event -> {
                    if (presenter != null) {
                        presenter.onFileSelected(fileDTO);
                    }
                });
                fileWidget.add(downloadIcon);
*/
                fileWidget.addClickHandler(event -> {
                    if (presenter != null) {
                        presenter.onFileSelected(fileDTO);
                    }
                });
                TreeItem treeItem = new TreeItem(fileWidget);
                if (directoryTreeItem == null) {
                    tree.addItem(treeItem);
                } else {
                    directoryTreeItem.addItem(treeItem);
                }
            }
        }
    }

}
