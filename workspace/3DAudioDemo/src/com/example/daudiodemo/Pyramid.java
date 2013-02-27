package com.example.daudiodemo;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

/**
 * 
 * @author Savas Ziplies (nea/INsanityDesign)
 */
public class Pyramid {
	
	/** The buffer holding the vertices */
	private FloatBuffer vertexBuffer;
	/** The buffer holding the color values */
	private FloatBuffer colorBuffer;
	/** How many bytes per float. */
	private final int mBytesPerFloat = 4;	
	/** Size of the position data in elements. */
	private final int mPositionDataSize = 3;	
	/** Size of the color data in elements. */
	private final int mColorDataSize = 4;	
	/** Size of the normal data in elements. */
	private final int mNormalDataSize = 3;
	/** This will be used to pass in model position information. */
	private int mPositionHandle;
	/** This will be used to pass in model color information. */
	private int mColorHandle;
	/** This will be used to pass in model normal information. */
	private int mNormalHandle;
	/** This is a handle to our per-vertex cube shading program. */
	private int mPerVertexProgramHandle;
	/** This will be used to pass in the transformation matrix. */
	private int mMVPMatrixHandle;
	/** This will be used to pass in the modelview matrix. */
	private int mMVMatrixHandle;
	/**
	 * Store the model matrix. This matrix is used to move models from object space (where each model can be thought
	 * of being located at the center of the universe) to world space.
	 */
	private float[] mModelMatrix = new float[16];
	/**
	 * Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space;
	 * it positions things relative to our eye.
	 */
	private float[] mViewMatrix = new float[16];
	/** Store the projection matrix. This is used to project the scene onto a 2D viewport. */
	private float[] mProjectionMatrix = new float[16];
	/** Allocate storage for the final combined matrix. This will be passed into the shader program. */
	private float[] mMVPMatrix = new float[16];
	/** Used for debug logs. */
	private static final String TAG = "DemoRenderer";
		
	/** The initial vertex definition */
	private float vertices[] = { 
					 	0.0f,  1.0f,  0.0f,		//Top Of Triangle (Front)
						-1.0f, -1.0f, 1.0f,		//Left Of Triangle (Front)
						 1.0f, -1.0f, 1.0f,		//Right Of Triangle (Front)
						 0.0f,  1.0f, 0.0f,		//Top Of Triangle (Right)
						 1.0f, -1.0f, 1.0f,		//Left Of Triangle (Right)
						 1.0f, -1.0f, -1.0f,	//Right Of Triangle (Right)
						 0.0f,  1.0f, 0.0f,		//Top Of Triangle (Back)
						 1.0f, -1.0f, -1.0f,	//Left Of Triangle (Back)
						-1.0f, -1.0f, -1.0f,	//Right Of Triangle (Back)
						 0.0f,  1.0f, 0.0f,		//Top Of Triangle (Left)
						-1.0f, -1.0f, -1.0f,	//Left Of Triangle (Left)
						-1.0f, -1.0f, 1.0f		//Right Of Triangle (Left)
											};
	/** The initial color definition */	
	private float colors[] = {
			    		1.0f, 0.0f, 0.0f, 1.0f, //Red
			    		0.0f, 1.0f, 0.0f, 1.0f, //Green
			    		0.0f, 0.0f, 1.0f, 1.0f, //Blue
			    		1.0f, 0.0f, 0.0f, 1.0f, //Red
			    		0.0f, 0.0f, 1.0f, 1.0f, //Blue
			    		0.0f, 1.0f, 0.0f, 1.0f, //Green
			    		1.0f, 0.0f, 0.0f, 1.0f, //Red
			    		0.0f, 1.0f, 0.0f, 1.0f, //Green
			    		0.0f, 0.0f, 1.0f, 1.0f, //Blue
			    		1.0f, 0.0f, 0.0f, 1.0f, //Red
			    		0.0f, 0.0f, 1.0f, 1.0f, //Blue
			    		0.0f, 1.0f, 0.0f, 1.0f 	//Green
						    					};

	
	/**
	 * The Pyramid constructor.
	 * 
	 * Initiate the buffers.
	 */
	public Pyramid() {
		//
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		vertexBuffer = byteBuf.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);
		
		//
		byteBuf = ByteBuffer.allocateDirect(colors.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		colorBuffer = byteBuf.asFloatBuffer();
		colorBuffer.put(colors);
		colorBuffer.position(0);
	}
	
	protected String getVertexShader()
	{
		// TODO: Explain why we normalize the vectors, explain some of the vector math behind it all. Explain what is eye space.
		final String vertexShader =
				"uniform mat4 u_MVPMatrix;      \n"		// A constant representing the combined model/view/projection matrix.
				
				  + "attribute vec4 a_Position;     \n"		// Per-vertex position information we will pass in.
				  + "attribute vec4 a_Color;        \n"		// Per-vertex color information we will pass in.			  
				  
				  + "varying vec4 v_Color;          \n"		// This will be passed into the fragment shader.
				  
				  + "void main()                    \n"		// The entry point for our vertex shader.
				  + "{                              \n"
				  + "   v_Color = a_Color;          \n"		// Pass the color through to the fragment shader. 
				  											// It will be interpolated across the triangle.
				  + "   gl_Position = u_MVPMatrix   \n" 	// gl_Position is a special variable used to store the final position.
				  + "               * a_Position;   \n"     // Multiply the vertex by the matrix to get the final point in 			                                            			 
				  + "}                              \n";    // normalized screen coordinates.
		return vertexShader;
	}
	
	protected String getFragmentShader()
	{
		final String fragmentShader =
			"precision mediump float;       \n"		// Set the default precision to medium. We don't need as high of a 
													// precision in the fragment shader.				
		  + "varying vec4 v_Color;          \n"		// This is the color from the vertex shader interpolated across the 
		  											// triangle per fragment.			  
		  + "void main()                    \n"		// The entry point for our fragment shader.
		  + "{                              \n"
		  + "   gl_FragColor = v_Color;     \n"		// Pass the color directly through the pipeline.		  
		  + "}                              \n";
		
		return fragmentShader;
	}
	
	public void draw()
	{
		final String vertexShader = getVertexShader();   		
 		final String fragmentShader = getFragmentShader();			
		
		final int vertexShaderHandle = compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);		
		final int fragmentShaderHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);
		
		mPerVertexProgramHandle = createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle, 
				new String[] {"a_Position",  "a_Color", "a_Normal"});	
		
		mPositionHandle = GLES20.glGetAttribLocation(mPerVertexProgramHandle, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(mPerVertexProgramHandle, "a_Color");
        mNormalHandle = GLES20.glGetAttribLocation(mPerVertexProgramHandle, "a_Normal");
		
		// Pass in the position information
		vertexBuffer.position(0);		
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
        		0, vertexBuffer);        
                
        GLES20.glEnableVertexAttribArray(mPositionHandle);        
        
        // Pass in the color information
        colorBuffer.position(0);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
        		0, colorBuffer);        
        
        GLES20.glEnableVertexAttribArray(mColorHandle);
        
		// This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);   
        
        // Pass in the modelview matrix.
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);                
        
        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // Draw the cube.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);                               
	}

	/**
	 * The object own drawing function.
	 * Called from the renderer to redraw this instance
	 * with possible changes in values.
	 * 
	 * @param gl - The GL Context
	 */
	public void draw(GL10 gl) {	
		//Set the face rotation
		gl.glFrontFace(GL10.GL_CW);
		
		//Point to our buffers
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer);
		
		//Enable the vertex and color state
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
		
		//Draw the vertices as triangles
		gl.glDrawArrays(GL10.GL_TRIANGLES, 0, vertices.length / 3);
		
		//Disable the client state before leaving
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
	}
	
	/**
	 * Helper function to compile and link a program.
	 * 
	 * @param vertexShaderHandle An OpenGL handle to an already-compiled vertex shader.
	 * @param fragmentShaderHandle An OpenGL handle to an already-compiled fragment shader.
	 * @param attributes Attributes that need to be bound to the program.
	 * @return An OpenGL handle to the program.
	 */
	private int createAndLinkProgram(final int vertexShaderHandle, final int fragmentShaderHandle, final String[] attributes) 
	{
		int programHandle = GLES20.glCreateProgram();
		
		if (programHandle != 0) 
		{
			// Bind the vertex shader to the program.
			GLES20.glAttachShader(programHandle, vertexShaderHandle);			

			// Bind the fragment shader to the program.
			GLES20.glAttachShader(programHandle, fragmentShaderHandle);
			
			// Bind attributes
			if (attributes != null)
			{
				final int size = attributes.length;
				for (int i = 0; i < size; i++)
				{
					GLES20.glBindAttribLocation(programHandle, i, attributes[i]);
				}						
			}
			
			// Link the two shaders together into a program.
			GLES20.glLinkProgram(programHandle);

			// Get the link status.
			final int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

			// If the link failed, delete the program.
			if (linkStatus[0] == 0) 
			{				
				Log.e(TAG, "Error compiling program: " + GLES20.glGetProgramInfoLog(programHandle));
				GLES20.glDeleteProgram(programHandle);
				programHandle = 0;
			}
		}
		
		if (programHandle == 0)
		{
			throw new RuntimeException("Error creating program.");
		}
		
		return programHandle;
	}
	
	private int compileShader(final int shaderType, final String shaderSource) 
	{
		int shaderHandle = GLES20.glCreateShader(shaderType);

		if (shaderHandle != 0) 
		{
			// Pass in the shader source.
			GLES20.glShaderSource(shaderHandle, shaderSource);

			// Compile the shader.
			GLES20.glCompileShader(shaderHandle);

			// Get the compilation status.
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

			// If the compilation failed, delete the shader.
			if (compileStatus[0] == 0) 
			{
				Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shaderHandle));
				GLES20.glDeleteShader(shaderHandle);
				shaderHandle = 0;
			}
		}

		if (shaderHandle == 0)
		{			
			throw new RuntimeException("Error creating shader.");
		}
		
		return shaderHandle;
	}
}
