package com.binaryworkspace.rcp.jogl.views;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * Renders a basic color triangle using JOGL in a SWT to AWT Bridge between and
 * SWT Composite and AWT Frame.
 * <p>
 * <b>Notes:</b>
 * <ul>
 * <li>This is a modification of two code examples provided by Wade Walker.
 * </ul>
 * 
 * @author Chris Ludka
 *         <p>
 * @see <a href=
 *      "https://wadeawalker.wordpress.com/2010/10/09/tutorial-a-cross-platform-workbench-program-using-java-opengl-and-eclipse/">
 *      https://wadeawalker.wordpress.com/2010/10/09/tutorial-a-cross-platform-
 *      workbench-program-using-java-opengl-and-eclipse/</a>
 * @see <a href=
 *      "https://jogamp.org/wiki/index.php/Using_JOGL_in_AWT_SWT_and_Swing">
 *      https://jogamp.org/wiki/index.php/Using_JOGL_in_AWT_SWT_and_Swing</a>
 */

public class OneTriangleEmbeddedViewPart extends ViewPart {

	public static final String ID = OneTriangleEmbeddedViewPart.class.getName();

	@Override
	public void createPartControl(Composite parent) {
		// GLProfile and GLCanvas
		GLProfile glProfile = GLProfile.getDefault();
		GLCapabilities glCapabilities = new GLCapabilities(glProfile);
		final GLCanvas glCanvas = new GLCanvas(glCapabilities);

		// Swing to SWT bridge
		final Composite swtEmbeddedComposite = new Composite(parent, SWT.EMBEDDED);
		swtEmbeddedComposite.setLayout(new FormLayout());
		FormData formData = new FormData();
		formData.top = new FormAttachment(0, 0);
		formData.bottom = new FormAttachment(100, 0);
		formData.left = new FormAttachment(0, 0);
		formData.right = new FormAttachment(100, 0);
		swtEmbeddedComposite.setLayoutData(formData);

		// AWT Frame
		java.awt.Frame awtFrame = SWT_AWT.new_Frame(swtEmbeddedComposite);
		awtFrame.add(glCanvas);

		// Add listener to resize the drawing
		glCanvas.addGLEventListener(new GLEventListener() {

			@Override
			public void reshape(GLAutoDrawable glautodrawable, int x, int y, int width, int height) {
				// Mode
				GL2 gl2 = glautodrawable.getGL().getGL2();
				gl2.glMatrixMode(GL2.GL_PROJECTION);
				gl2.glLoadIdentity();

				/*
				 * Size the draw space the coordinate system origin residing at
				 * the lower left.
				 */
				GLU glu = new GLU();
				glu.gluOrtho2D(0.0f, width, 0.0f, height);

				// Mode
				gl2.glMatrixMode(GL2.GL_MODELVIEW);
				gl2.glLoadIdentity();

				// Set GLViewPort
				gl2.glViewport(0, 0, width, height);
			}

			@Override
			public void init(GLAutoDrawable glautodrawable) {
			}

			@Override
			public void dispose(GLAutoDrawable glautodrawable) {
			}

			@Override
			public void display(GLAutoDrawable glautodrawable) {
				// GL2
				GL2 gl2 = glautodrawable.getGL().getGL2();
				int width = glautodrawable.getWidth();
				int height = glautodrawable.getHeight();

				// Clear
				gl2.glClear(GL.GL_COLOR_BUFFER_BIT);

				// Draw a triangle filling the window
				gl2.glLoadIdentity();
				gl2.glBegin(GL.GL_TRIANGLES);
				gl2.glColor3f(1, 0, 0);
				gl2.glVertex2f(0, 0);
				gl2.glColor3f(0, 1, 0);
				gl2.glVertex2f(width, 0);
				gl2.glColor3f(0, 0, 1);
				gl2.glVertex2f(width / 2, height);
				gl2.glEnd();
			}
		});
	}

	@Override
	public void setFocus() {
		// Do Nothing.
	}

	@Override
	public void dispose() {
		super.dispose();
	}
}
