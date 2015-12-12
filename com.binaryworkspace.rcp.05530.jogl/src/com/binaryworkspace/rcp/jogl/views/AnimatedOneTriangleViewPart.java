package com.binaryworkspace.rcp.jogl.views;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLProfile;
import javax.media.opengl.glu.GLU;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * Renders a basic color triangle being animated using JOGL in a SWT Composite.
 * <p>
 * <b>Notes:</b>
 * <ul>
 * <li>This is a modification of the Animated One Triangle example presented as
 * part of the JOGL tutorials and two code examples provided by Wade Walker.
 * </ul>
 * 
 * @author Chris Ludka
 *         <p>
 * @see <a href="https://sites.google.com/site/justinscsstuff/jogl-tutorial-3">
 *      https://sites.google.com/site/justinscsstuff/jogl-tutorial-3</a>
 * @see <a href=
 *      "https://wadeawalker.wordpress.com/2010/10/09/tutorial-a-cross-platform-workbench-program-using-java-opengl-and-eclipse/">
 *      https://wadeawalker.wordpress.com/2010/10/09/tutorial-a-cross-platform-
 *      workbench-program-using-java-opengl-and-eclipse/</a>
 * @see <a href=
 *      "https://jogamp.org/wiki/index.php/Using_JOGL_in_AWT_SWT_and_Swing">
 *      https://jogamp.org/wiki/index.php/Using_JOGL_in_AWT_SWT_and_Swing</a>
 */

public class AnimatedOneTriangleViewPart extends ViewPart {

	public static final String ID = AnimatedOneTriangleViewPart.class.getName();

	private Composite baseComposite;

	private GLCanvas glcanvas;

	private GLContext glContext;

	// Triangle Model
	private double theta = 0;
	private double s = 0;
	private double c = 0;

	@Override
	public void createPartControl(Composite parent) {
		// Base Composite
		baseComposite = new Composite(parent, SWT.NONE);
		baseComposite.setLayout(new FillLayout());

		// GLCanvas
		GLData gldata = new GLData();
		gldata.doubleBuffer = true;
		glcanvas = new GLCanvas(baseComposite, SWT.NO_BACKGROUND, gldata);
		glcanvas.setCurrent();

		// GLProfile
		GLProfile glProfile = GLProfile.get(GLProfile.GL2);
		glContext = GLDrawableFactory.getFactory(glProfile).createExternalGLContext();

		// Add listener to resize the drawing
		glcanvas.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				// Get canvas size
				Rectangle rectangle = glcanvas.getClientArea();
				int width = rectangle.width;
				int height = rectangle.height;

				// Make current
				glcanvas.setCurrent();
				glContext.makeCurrent();

				// Update the projection
				GL2 gl2 = glContext.getGL().getGL2();
				gl2.glMatrixMode(GL2.GL_PROJECTION);
				gl2.glLoadIdentity();

				/*
				 * Size the draw space the coordinate system origin residing at
				 * the lower left.
				 */
				GLU glu = new GLU();
				glu.gluOrtho2D(0.0f, width, 0.0f, height);

				// Identity
				gl2.glMatrixMode(GL2.GL_MODELVIEW);
				gl2.glLoadIdentity();

				// Set GLViewPort
				gl2.glViewport(0, 0, width, height);

				// Release Context
				glContext.release();
			}
		});

		// Create render thread
		(new Thread() {
			public void run() {
				while ((glcanvas != null) && !glcanvas.isDisposed()) {
					render();
					try {
						/*
						 * Don't make loop too tight, or not enough time to
						 * process window messages properly.
						 */
						sleep(1);
					} catch (InterruptedException interruptedexception) {
						/*
						 * Application just quits on interrupt, so nothing
						 * required here.
						 */
					}
				}
			}
		}).start();
	}

	// Render
	protected void render() {
		// UI Render Thread
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				// Validate for disposal
				if ((glcanvas == null) || glcanvas.isDisposed()) {
					return;
				}

				// Get canvas size
				Rectangle rectangle = glcanvas.getClientArea();
				int width = rectangle.width;
				int height = rectangle.height;

				// Model Update
				theta += 0.01;
				s = Math.sin(theta);
				c = Math.cos(theta);
				double sx = (1 + s) * width / 2;
				double sy = (1 + s) * height / 2;
				double cx = (1 + c) * width / 2;
				double cy = (1 + c) * height / 2;

				// Make current
				glcanvas.setCurrent();
				glContext.makeCurrent();

				// Update the projection
				GL2 gl2 = glContext.getGL().getGL2();
				gl2.glClear(GL.GL_COLOR_BUFFER_BIT);

				// Draw a triangle filling the window
				gl2.glLoadIdentity();
				gl2.glBegin(GL.GL_TRIANGLES);
				gl2.glColor3f(1, 0, 0);
				gl2.glVertex2d(cx, cy);
				gl2.glColor3f(0, 1, 0);
				gl2.glVertex2d(0, cy);
				gl2.glColor3f(0, 0, 1);
				gl2.glVertex2d(sx, sy);
				gl2.glEnd();

				// Release Context
				glcanvas.swapBuffers();
				glContext.release();
			}
		});
	}

	@Override
	public void setFocus() {
		// Do Nothing.
	}

	@Override
	public void dispose() {
		glcanvas.dispose();
		super.dispose();
	}
}
