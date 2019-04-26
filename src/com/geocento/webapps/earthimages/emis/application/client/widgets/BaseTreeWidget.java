package com.geocento.webapps.earthimages.emis.application.client.widgets;

import com.geocento.webapps.earthimages.emis.application.client.style.StyleResources;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.metaaps.webapps.libraries.client.widget.LoadingIcon;

public class BaseTreeWidget extends Composite {

	static protected StyleResources style = GWT.create(StyleResources.class);
	
	static public Tree.Resources treeResources = new Tree.Resources() {
		
		@Override
		public ImageResource treeOpen() {
			return style.arrowDownSmaller();
		}
		
		@Override
		public ImageResource treeLeaf() {
			return style.arrowLeftSmaller();
		}
		
		@Override
		public ImageResource treeClosed() {
			return style.arrowLeftSmaller();
		}
	};
	
	protected Tree tree;
	private TreeItem loadingTreeItem;
	
	public BaseTreeWidget() {
		tree = new Tree(treeResources) {
            @Override
            public void setFocus(boolean focus) {
                // Don't do anything
            }
        };
		tree.setScrollOnSelectEnabled(true);

        initWidget(tree);
	}
	
	static public class LoadingWidget extends Composite {
		
		public LoadingWidget() {
            LoadingIcon label = new LoadingIcon();
            initWidget(label);
			label.setText("Loading...");
            label.setStyleName(StyleResources.INSTANCE.style().treeItemLabel());
		}
		
	}
	
	public void setLoading(boolean loading) {
		if(loading) {
			tree.clear();
			if(loadingTreeItem == null) {
				loadingTreeItem = new TreeItem(new LoadingWidget());
			}
			tree.addItem(loadingTreeItem);
		} else {
			tree.removeItem(loadingTreeItem);
		}
	}

	public void setMessage(String message) {
		tree.clear();
		tree.addItem(new Label(message));
	}

}
