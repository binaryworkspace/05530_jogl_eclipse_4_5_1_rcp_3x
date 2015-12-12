package com.binaryworkspace.rcp.jogl.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.media.opengl.GL2ES2;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

/**
 * A utility class provided to assist with basic GL2ES2 operations such as
 * loading textures and OpenGL error handling.
 * 
 * @author Chris Ludka
 */
public final class GL2ES2_Util {

	/**
	 * @param gl2es2
	 *            GL2ES2 pipeline object for the calling GLContext. Note, the
	 *            provided gl2es2 instance must come for the current glcontext
	 *            (e.g. glcanvas.setCurrent(), then glcontext.makeCurrent()).
	 * @param bundle
	 *            In order to load a texture file, Eclipse RCP requires the
	 *            resource (e.g. "res/earth_night.png") be obtained through the
	 *            bundle. Recall the bundle in RCP can be obtained with the
	 *            call: Bundle bundle = Activator.getDefault().getBundle();
	 * @param filePath
	 *            is the string path. For instance for a rsc folder at the same
	 *            (sibling) level of icons folder in a typical RCP Application
	 *            the filePath would be: String filePath =
	 *            "rsc/textureExample.png";
	 * @return Texture an OpenGL texture object.
	 * 
	 * @throws RuntimeException
	 *             if the texture fails to load in any way.
	 */
	public static Texture createTexture(GL2ES2 gl2es2, Bundle bundle, String filePath) {
		// Load the texture
		Path path = new Path(filePath); //$NON-NLS-1$
		URL url = FileLocator.find(bundle, path, Collections.emptyMap());
		URL fileUrl = null;
		try {
			fileUrl = FileLocator.toFileURL(url);
		} catch (IOException e) {
			throw new RuntimeException("Could not load the texture file: " + filePath, e);
		}
		File file = new File(fileUrl.getPath());

		// Create texture data
		TextureData textureData = null;
		try {

			/**
			 * Best practice for loading images for jogl 2.0-rc11
			 * 
			 * <pre>
			 * @see http://forum.jogamp.org/PNG-interlace-td4027479.html
			 * </pre>
			 */
			BufferedImage bufferedImage = ImageIO.read(file);
			textureData = AWTTextureIO.newTextureData(gl2es2.getGLProfile(), bufferedImage, true);
		} catch (IOException e) {
			throw new RuntimeException("Could not create a texture data for: " + filePath, e);
		}

		// Create the texture
		Texture texture = TextureIO.newTexture(textureData);
		return texture;
	}

	/**
	 * Loads and compiles the C Source Code on the video card.
	 * 
	 * @param gl2es2
	 *            GL2ES2 pipeline object for the calling GLContext. Note, the
	 *            provided gl2es2 instance must come for the current glcontext
	 *            (e.g. glcanvas.setCurrent(), then glcontext.makeCurrent()).
	 * @param shaderType
	 *            (e.g. GL2ES2.GL_VERTEX_SHADER, GL2ES2.GL_FRAGMENT_SHADER)
	 * @param source
	 *            Shader C Source Code presented as a single string
	 * @return int which is the shader's id if successfully loaded and compiled.
	 * 
	 * @throws RuntimeException
	 *             if the shader fails to load or compile.
	 */
	public static int loadShader(GL2ES2 gl2es2, int shaderType, String source) {
		// Create a shader for the given type
		int shaderId = gl2es2.glCreateShader(shaderType);
		if (shaderId == 0) {
			throw new RuntimeException("Could not create the sharder with type: " + shaderType + " with source: " + source);
		}

		// Compile the shader source code
		String[] c_src = new String[] { source };
		int[] c_srcLengths = new int[] { c_src[0].length() };
		gl2es2.glShaderSource(shaderId, c_srcLengths.length, c_src, c_srcLengths, 0);
		gl2es2.glCompileShader(shaderId);

		// Check compile status
		int[] compiled = new int[1];
		gl2es2.glGetShaderiv(shaderId, GL2ES2.GL_COMPILE_STATUS, compiled, 0);

		// Return state for shaderId is good
		if (compiled[0] > 0) {
			return shaderId;
		}

		// Return state for shaderId is bad. Obtain a detailed shader log and
		// throw a runtime exception
		int[] logLength = new int[1];
		gl2es2.glGetShaderiv(shaderId, GL2ES2.GL_INFO_LOG_LENGTH, logLength, 0);
		byte[] log = new byte[logLength[0]];
		gl2es2.glGetShaderInfoLog(shaderId, logLength[0], (int[]) null, 0, log, 0);
		throw new RuntimeException("Error compiling the shader: " + new String(log));
	}

	/**
	 * Performs a check as to if a given shader program is linked to the
	 * provided pipeline for the calling context.
	 * 
	 * @param gl2es2
	 *            GL2ES2 pipeline object for the calling GLContext. Note, the
	 *            provided gl2es2 instance must come for the current glcontext
	 *            (e.g. glcanvas.setCurrent(), then glcontext.makeCurrent()).
	 * @param programId
	 *            obtained from the pipeline when creating the shader program
	 *            (return value from glCreateProgram()).
	 * 
	 * @throws RunTimeException
	 *             if the program is not linked.
	 */
	public static void validateProgramLinkage(GL2ES2 gl2es2, int programId) {
		// Get program link status value
		int[] linkStatus = new int[1];
		gl2es2.glGetProgramiv(programId, GL2ES2.GL_LINK_STATUS, linkStatus, 0);

		// Return good state if the link status is true
		if (linkStatus[0] == GL2ES2.GL_TRUE) {
			return;
		}

		// *** Link status is not in a good state, display error logic *** //
		// Obtain program information log
		int[] infoLength = { 0 };
		gl2es2.glGetProgramiv(programId, GL2ES2.GL_INFO_LOG_LENGTH, infoLength, 0);
		byte[] log = new byte[infoLength[0]];
		gl2es2.glGetProgramInfoLog(programId, infoLength[0], infoLength, 0, log, 0);

		// Write warning logic
		StringBuilder sb = new StringBuilder();
		sb.append("WARNING: Shader program not linked: " + programId + " \n");
		for (int i = 0; i < infoLength[0]; i++) {
			sb.append(String.format("%c", log[i]));
		}
		gl2es2.glDeleteProgram(programId);

		throw new RuntimeException(sb.toString());

	}

	/**
	 * Pulls errors off the OpenGL pipeline stack in the order of occurrence and
	 * throws a runtime exception with error codes if any existed.
	 * 
	 * @param gl2es2
	 *            GL2ES2 pipeline object for the calling GLContext. Note, the
	 *            provided gl2es2 instance must come for the current glcontext
	 *            (e.g. glcanvas.setCurrent(), then glcontext.makeCurrent()).
	 * @param description
	 *            of the type of OpenGL calls that were made before this check
	 *            was performed (e.g. glUseProgram, glUniforms, glDrawArrays).
	 */
	public static void checkGlError(GL2ES2 gl2es2, String description) {
		List<Integer> errorCodes = null;

		// Collect all glError codes
		while (true) {
			int error = gl2es2.glGetError();
			if (error == GL2ES2.GL_NO_ERROR) {
				break;
			} else {
				if (errorCodes == null) {
					errorCodes = new ArrayList<Integer>();
				}
				errorCodes.add(error);
			}
		}

		// Throw an exception and list all found glError codes if errors found
		if (errorCodes != null) {
			Collections.reverse(errorCodes);
			StringBuilder sb = new StringBuilder();
			for (Integer errorCode : errorCodes) {
				if (sb.length() > 0) {
					sb.append("\n");
				}
				sb.append("glError: ");
				sb.append(errorCode);
			}
			throw new RuntimeException("The GLError check for " + description + " pulled errors from the OpenGL pipeline stack./n" + sb.toString());
		}
	}
}
