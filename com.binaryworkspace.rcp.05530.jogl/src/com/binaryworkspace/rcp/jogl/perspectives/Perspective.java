package com.binaryworkspace.rcp.jogl.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.binaryworkspace.rcp.jogl.views.AnimatedOneTriangleViewPart;
import com.binaryworkspace.rcp.jogl.views.OneTriangleEmbeddedViewPart;
import com.binaryworkspace.rcp.jogl.views.OneTriangleViewPart;

public class Perspective implements IPerspectiveFactory {
	
	public static final String ID = Perspective.class.getName();
	
	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(false);
		IFolderLayout folderReport = layout.createFolder("Perspective.folder", IPageLayout.LEFT, 1.0f, layout.getEditorArea());
		folderReport.addView(OneTriangleViewPart.ID);
		folderReport.addView(OneTriangleEmbeddedViewPart.ID);
		folderReport.addView(AnimatedOneTriangleViewPart.ID);
	}
}
