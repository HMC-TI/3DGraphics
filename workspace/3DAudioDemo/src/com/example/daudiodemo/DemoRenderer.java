package com.example.daudiodemo;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.FloatMath;
import android.util.Log;

/**
 * This class implements our custom renderer. Note that the GL10 parameter
 * passed in is unused for OpenGL ES 2.0 renderers -- the static class GLES20 is
 * used instead.
 */
public class DemoRenderer implements GLSurfaceView.Renderer {
	// Used for debug logs.
	private static final String TAG = "DemoRenderer";

	public final Context mActivityContext;
	
	// Used to keep score
	public static int count;
	
	// Used to keep track of what level a player is in the game
	// Different levels have different background textures
	public boolean levelTwo = false;
	public boolean levelThree = false;

	// Declare as volatile because we are updating it from another thread
	private float theta;
	private float phi = 3.14159265359f / 2;
	public volatile boolean leftRotate = false;
	public volatile boolean rightRotate = false;
	public volatile boolean upRotate = false;
	public volatile boolean downRotate = false;
	// For pyramid location randomizing functionality
	public volatile float pyrX = 3.0f;
	public volatile float pyrY = 0.0f;
	public volatile float pyrZ = -6.0f;
	// For pyramid finding functionality
	private boolean objectFound;
	private int windowWidth;
	private int windowHeight;

	// Store the model matrix. This matrix is used to move models from object
	// space (where each model can be thought
	// of being located at the center of the universe) to world space.
	private float[] mModelMatrix = new float[16];

	// Store the view matrix. This can be thought of as our camera. This matrix
	// transforms world space to eye space;
	// it positions things relative to our eye.
	private float[] mViewMatrix = new float[16];

	// Store the projection matrix. This is used to project the scene onto a 2D
	// viewport.
	private float[] mProjectionMatrix = new float[16];

	// Allocate storage for the final combined matrix. This will be passed into
	// the shader program.
	private float[] mMVPMatrix = new float[16];

	// Stores a copy of the model matrix specifically for the light position.
	private float[] mLightModelMatrix = new float[16];

	// Store our model data in a float buffer.
	private final FloatBuffer mCubePositions;
	private final FloatBuffer mCubeColors;
	private final FloatBuffer mCubeNormals;
	private final FloatBuffer mCubeTextureCoordinates;

	private final FloatBuffer mPyramidPositions;
	private final FloatBuffer mPyramidColors;
	private final FloatBuffer mPyramidNormals;
	private final FloatBuffer mFoundPyramidColors;

	// This will be used to pass in the transformation matrix.
	private int mMVPMatrixHandle;

	// This will be used to pass in the modelview matrix.
	private int mMVMatrixHandle;

	// This will be used to pass in the light position.
	private int mLightPosHandle;

	// This will be used to pass in the texture.
	private int mTextureUniformHandle;

	// This will be used to pass in model position information.
	private int mPositionHandle;

	// This will be used to pass in model color information.
	private int mColorHandle;

	// This will be used to pass in model normal information.
	private int mNormalHandle;

	// This will be used to pass in model texture coordinate information.
	private int mTextureCoordinateHandle;

	// How many bytes per float.
	private final int mBytesPerFloat = 4;

	// Size of the position data in elements.
	private final int mPositionDataSize = 3;

	// Size of the color data in elements.
	private final int mColorDataSize = 4;

	// Size of the normal data in elements.
	private final int mNormalDataSize = 3;

	// Size of the texture coordinate data in elements.
	private final int mTextureCoordinateDataSize = 2;

	// Used to hold a light centered on the origin in model space. We need a 4th
	// coordinate so we can get translations
	// to work when we multiply this by our transformation matrices.
	private final float[] mLightPosInModelSpace = new float[] { 0.0f, 0.0f,
			0.0f, 1.0f };

	// Used to hold the current position of the light in world space (after
	// transformation via model matrix).
	private final float[] mLightPosInWorldSpace = new float[4];

	// Used to hold the transformed position of the light in eye space (after
	// transformation via modelview matrix)
	private final float[] mLightPosInEyeSpace = new float[4];

	// This is a handle to our per-vertex cube shading program.
	private int mPerVertexProgramHandle;

	// This is a handle to our cube shading program.
	public int mCubeProgramHandle;

	// This is a handle to our pyramid shading program.
	public int mPyramidProgramHandle;

	// This is a handle to our light point program.
	private int mPointProgramHandle;

	// This is a handle to our texture data.
	public int mTextureDataHandle;

	// These are the things from OnDrawFrame
	private long time;
	private float angleInDegrees;

	// Position the eye in front of the origin.
	final float eyeX = 0.0f;
	final float eyeY = 0.0f;
	final float eyeZ = -6.0f;

	// We are looking toward the distance
	private float lookX = 0.0f;
	private float lookY = 0.0f;
	private float lookZ = 12.0f;

	// Set our up vector. This is where our head would be pointing were we
	// holding the camera.
	final float upX = 0.0f;
	final float upY = 1.0f;
	final float upZ = 0.0f;

	// These are the relative az and elev that will allow us to calculate the
	// needed az and elev for the audio
	// Spherical coordinates describing where we are looking
	private float rLook;
	private float elevLook;
	private float azLook;

	// These are the relative az and elev that will allow us to calculate the
	// needed az and elev for the audio
	// Spherical coordinates describing the location of the object emitting
	// noise
	private float rPyr;
	private float elevPyr;
	private float azPyr;

	// float rollRadians = (float) SensorHubService.roll / 180* (float)
	// Math.PI;
	private float pitchRadians;
	private float yawRadians;

	// Convert object 3D coordinates to 2D window coordinates
	private int[] view = new int[2];
	private float[] spacePos = new float[4];
	private float[] clipSpacePosIntermediate = new float[4];
	private float[] clipSpacePos = new float[4];

	// Normalize using w coordinate
	private float[] ndcSpacePos = new float[3];
	// Convert to 2D
	float[] outputCoords = new float[2];

	// Initialize the model data.
	public DemoRenderer(final Context activityContext) {
		mActivityContext = activityContext;

		// Define points for a cube.
		// X, Y, Z
		final float[] cubePositionData = {
				// In OpenGL counter-clockwise winding is default. This means
				// that when we look at a triangle,
				// if the points are counter-clockwise we are looking at the
				// "front". If not we are looking at
				// the back. OpenGL has an optimization where all back-facing
				// triangles are culled, since they
				// usually represent the backside of an object and aren't
				// visible anyways.

				// Front face
				-1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f,
				-1.0f, 1.0f, 1.0f, -1.0f,
				1.0f,
				1.0f,
				1.0f,
				1.0f,

				// Right face
				1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f,
				-1.0f, 1.0f, 1.0f, -1.0f, -1.0f,
				1.0f,
				1.0f,
				-1.0f,

				// Back face
				1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f,
				1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,
				1.0f,
				-1.0f,

				// Left face
				-1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f,
				-1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f,
				1.0f,

				// Top face
				-1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f,
				-1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f,

				// Bottom face
				1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f,
				1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, };

		// R, G, B, A
		final float[] cubeColorData = {
				// Front face (red)
				1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f,
				0.0f, 1.0f, 1.0f, 0.0f,
				0.0f,
				1.0f,
				1.0f,
				0.0f,
				0.0f,
				1.0f,
				1.0f,
				0.0f,
				0.0f,
				1.0f,

				// Right face (green)
				0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f,
				0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f,
				0.0f,
				1.0f,
				0.0f,
				1.0f,
				0.0f,
				1.0f,
				0.0f,
				1.0f,

				// Back face (blue)
				0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f,
				1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f,
				1.0f,
				1.0f,
				0.0f,
				0.0f,
				1.0f,
				1.0f,

				// Left face (yellow)
				1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f,
				0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f,
				1.0f,
				1.0f,
				0.0f,
				1.0f,

				// Top face (cyan)
				0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f,
				1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f,
				0.0f, 1.0f, 1.0f,
				1.0f,

				// Bottom face (magenta)
				1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f,
				1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
				1.0f, 0.0f, 1.0f, 1.0f };

		// X, Y, Z
		// The normal is used in light calculations and is a vector which points
		// orthogonal to the plane of the surface. For a cube model, the normals
		// should be orthogonal to the points of each face.
		final float[] cubeNormalData = {
				// Front face
				0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
				0.0f, 1.0f, 0.0f, 0.0f,
				1.0f,
				0.0f,
				0.0f,
				1.0f,

				// Right face
				1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
				0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
				1.0f,
				0.0f,
				0.0f,

				// Back face
				0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f,
				0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f,
				0.0f,
				-1.0f,

				// Left face
				-1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f,
				0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f,
				0.0f,

				// Top face
				0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
				1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,

				// Bottom face
				0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,
				-1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f };

		// S, T (or X, Y)
		// Texture coordinate data.
		// Because images have a Y axis pointing downward (values increase as
		// you move down the image) while
		// OpenGL has a Y axis pointing upward, we adjust for that here by
		// flipping the Y axis.
		// Since we are texturing the inside of a cubical room with a cube map
		// texture, all of the
		// faces are actually inverted. These coordinates work for a horizontal
		// cross cube map.
		final float[] cubeTextureCoordinateData = {
				// Front face
				0.0f, 2.0f / 3, 0.0f, 1.0f / 3, 0.25f, 2.0f / 3, 0.0f,
				1.0f / 3,
				0.25f,
				1.0f / 3,
				0.25f,
				2.0f / 3,

				// Right face
				0.25f, 2.0f / 3, 0.25f, 1.0f / 3, 0.5f, 2.0f / 3, 0.25f,
				1.0f / 3, 0.5f,
				1.0f / 3,
				0.5f,
				2.0f / 3,

				// Back face
				0.5f, 2.0f / 3, 0.5f, 1.0f / 3, 0.75f, 2.0f / 3, 0.5f,
				1.0f / 3, 0.75f, 1.0f / 3,
				0.75f,
				2.0f / 3,

				// Left face
				0.75f, 2.0f / 3, 0.75f, 1.0f / 3, 1.0f, 2.0f / 3, 0.75f,
				1.0f / 3, 1.0f, 1.0f / 3, 1.0f,
				2.0f / 3,

				// Top face
				0.5f, 1.0f, 0.25f, 1.0f, 0.5f, 2.0f / 3, 0.25f, 1.0f, 0.25f,
				2.0f / 3, 0.5f, 2.0f / 3,

				// Bottom face
				0.5f, 1.0f / 3, 0.25f, 1.0f / 3, 0.5f, 0.0f, 0.25f, 1.0f / 3,
				0.25f, 0.0f, 0.5f, 0.0f };

		/** The initial position definition */
		final float pyramidPositionData[] = { 0.0f, 1.0f, 0.0f, // Top Of
																// Triangle
																// (Front)
				-1.0f, -1.0f, 1.0f, // Left Of Triangle (Front)
				1.0f, -1.0f, 1.0f, // Right Of Triangle (Front)

				0.0f, 1.0f, 0.0f, // Top Of Triangle (Right)
				1.0f, -1.0f, 1.0f, // Left Of Triangle (Right)
				1.0f, -1.0f, -1.0f, // Right Of Triangle (Right)

				0.0f, 1.0f, 0.0f, // Top Of Triangle (Back)
				1.0f, -1.0f, -1.0f, // Left Of Triangle (Back)
				-1.0f, -1.0f, -1.0f, // Right Of Triangle (Back)

				0.0f, 1.0f, 0.0f, // Top Of Triangle (Left)
				-1.0f, -1.0f, -1.0f, // Left Of Triangle (Left)
				-1.0f, -1.0f, 1.0f // Right Of Triangle (Left)
		};
		/** The initial color definition */
		final float pyramidColorData[] = { 1.0f, 0.0f, 0.0f, 1.0f, // Red
				0.0f, 1.0f, 0.0f, 1.0f, // Green
				0.0f, 0.0f, 1.0f, 1.0f, // Blue

				1.0f, 0.0f, 0.0f, 1.0f, // Red
				0.0f, 0.0f, 1.0f, 1.0f, // Blue
				0.0f, 1.0f, 0.0f, 1.0f, // Green

				1.0f, 0.0f, 0.0f, 1.0f, // Red
				0.0f, 1.0f, 0.0f, 1.0f, // Green
				0.0f, 0.0f, 1.0f, 1.0f, // Blue

				1.0f, 0.0f, 0.0f, 1.0f, // Red
				0.0f, 0.0f, 1.0f, 1.0f, // Blue
				0.0f, 1.0f, 0.0f, 1.0f // Green
		};

		final float foundPyramidColorData[] = { 1.0f, 0.5f, 0.5f, 1.0f, // Red
				0.5f, 1.0f, 0.5f, 1.0f, // Green
				0.5f, 0.5f, 1.0f, 1.0f, // Blue

				1.0f, 0.5f, 0.5f, 1.0f, // Red
				0.5f, 0.5f, 1.0f, 1.0f, // Blue
				0.5f, 1.0f, 0.5f, 1.0f, // Green

				1.0f, 0.5f, 0.5f, 1.0f, // Red
				0.5f, 1.0f, 0.5f, 1.0f, // Green
				0.5f, 0.5f, 1.0f, 1.0f, // Blue

				1.0f, 0.5f, 0.5f, 1.0f, // Red
				0.5f, 0.5f, 1.0f, 1.0f, // Blue
				0.5f, 1.0f, 0.5f, 1.0f // Green
		};

		/** The initial normal definition */
		final float pyramidNormalData[] = { 0.0f, 1.0f, 1.0f, // Top Of Triangle
																// (Front)
				0.0f, 1.0f, 1.0f, // Left Of Triangle (Front)
				0.0f, 1.0f, 1.0f, // Right Of Triangle (Front)

				1.0f, 1.0f, 0.0f, // Top Of Triangle (Right)
				1.0f, 1.0f, 0.0f, // Left Of Triangle (Right)
				1.0f, 1.0f, 0.0f, // Right Of Triangle (Right)

				0.0f, 1.0f, -1.0f, // Top Of Triangle (Back)
				0.0f, 1.0f, -1.0f, // Left Of Triangle (Back)
				0.0f, 1.0f, -1.0f, // Right Of Triangle (Back)

				-1.0f, 1.0f, 0.0f, // Top Of Triangle (Left)
				-1.0f, 1.0f, 0.0f, // Left Of Triangle (Left)
				-1.0f, 1.0f, 0.0f // Right Of Triangle (Left)
		};

		// Initialize the buffers.
		mCubePositions = ByteBuffer
				.allocateDirect(cubePositionData.length * mBytesPerFloat)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mCubePositions.put(cubePositionData).position(0);

		mCubeColors = ByteBuffer
				.allocateDirect(cubeColorData.length * mBytesPerFloat)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mCubeColors.put(cubeColorData).position(0);

		mCubeNormals = ByteBuffer
				.allocateDirect(cubeNormalData.length * mBytesPerFloat)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mCubeNormals.put(cubeNormalData).position(0);

		mCubeTextureCoordinates = ByteBuffer
				.allocateDirect(
						cubeTextureCoordinateData.length * mBytesPerFloat)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mCubeTextureCoordinates.put(cubeTextureCoordinateData).position(0);

		mPyramidPositions = ByteBuffer
				.allocateDirect(pyramidPositionData.length * mBytesPerFloat)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mPyramidPositions.put(pyramidPositionData).position(0);

		mPyramidColors = ByteBuffer
				.allocateDirect(pyramidColorData.length * mBytesPerFloat)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mPyramidColors.put(pyramidColorData).position(0);

		mFoundPyramidColors = ByteBuffer
				.allocateDirect(foundPyramidColorData.length * mBytesPerFloat)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mFoundPyramidColors.put(foundPyramidColorData).position(0);

		mPyramidNormals = ByteBuffer
				.allocateDirect(pyramidNormalData.length * mBytesPerFloat)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mPyramidNormals.put(pyramidNormalData).position(0);
	}

	protected String getVertexShader() {
		// TODO: Explain why we normalize the vectors, explain some of the
		// vector math behind it all. Explain what is eye space.
		final String vertexShader = "uniform mat4 u_MVPMatrix;      \n" // A
																		// constant
																		// representing
																		// the
																		// combined
																		// model/view/projection
																		// matrix.
				+ "uniform mat4 u_MVMatrix;       \n" // A constant representing
														// the combined
														// model/view matrix.

				+ "attribute vec4 a_Position;     \n" // Per-vertex position
														// information we will
														// pass in.
				+ "attribute vec4 a_Color;        \n" // Per-vertex color
														// information we will
														// pass in.
				+ "attribute vec3 a_Normal;       \n" // Per-vertex normal
														// information we will
														// pass in.
				+ "attribute vec2 a_TexCoordinate; \n" // Per-vertex texture
														// coordinate
														// information we will
														// pass in.

				+ "varying vec3 v_Position; 		\n" // This will be passed into
				// the fragment shader.
				+ "varying vec4 v_Color;         	\n" // This will be passed
														// into the fragment
														// shader.
				+ "varying vec3 v_Normal;         \n" // This will be passed
														// into the fragment
														// shader.
				+ "varying vec2 v_TexCoordinate;  \n" // This will be passed
														// into the fragment
														// shader.

				+ "void main()                    \n" // The entry point for our
														// vertex shader.
				+ "{                              \n"
				// Transform the vertex into eye space.
				+ "	v_Position = vec3(u_MVMatrix * a_Position);			\n"
				// Pass through the color.
				+ "   v_Color = a_Color;                           	    \n"
				// Pass through the texture coordinate.
				+ "	v_TexCoordinate = a_TexCoordinate;					\n"
				// Transform the normal's orientation into eye space.
				+ "	v_Normal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));	\n"
				// gl_Position is a special variable used to store the final
				// position.
				// Multiply the vertex by the matrix to get the final point in
				// normalized screen coordinates.
				+ "   gl_Position = u_MVPMatrix * a_Position;             \n"
				+ "}                                                      \n";

		return vertexShader;
	}

	protected String getFragmentShader(String type) {
		final String fragmentShader = "precision mediump float;       \n" // Set
																			// the
																			// default
																			// precision
																			// to
																			// medium.
																			// We
																			// don't
																			// need
																			// as
																			// high
																			// of
																			// a
																			// precision
																			// in
																			// the
																			// fragment
																			// shader.
				+ "uniform sampler2D u_Texture;	\n" // The input texture.
				+ "varying vec3 v_Position; 		\n" // Interpolated position for
				// this fragment.
				+ "varying vec4 v_Color;          \n" // This is the color from
														// the vertex shader
														// interpolated across
														// the
														// triangle per
														// fragment.
				+ "varying vec3 v_Normal;      	\n" // Interpolated normal for
													// this fragment.
				+ "varying vec2 v_TexCoordinate;	\n" // Interpolated texture
														// coordinate per
														// fragment.
				+ "void main()                    \n" // The entry point for our
														// fragment shader.
				+ "{                              \n"
				// Multiply the color by the texture value to get final output
				// color.
				+ "gl_FragColor = (v_Color * texture2D(u_Texture, v_TexCoordinate));	  \n"
				+ "}                              \n";

		final String cubeFragmentShader = "precision mediump float;       \n" // Set
																				// the
																				// default
																				// precision
																				// to
																				// medium.
																				// We
																				// don't
																				// need
																				// as
																				// high
																				// of
																				// a
																				// precision
																				// in
																				// the
																				// fragment
																				// shader.
				+ "uniform sampler2D u_Texture;	\n" // The input texture.
				+ "varying vec2 v_TexCoordinate;	\n" // Interpolated texture
														// coordinate per
														// fragment.
				+ "void main()                    \n" // The entry point for our
														// fragment shader.
				+ "{                              \n"
				// No color to affect texture value.
				+ "gl_FragColor = texture2D(u_Texture, v_TexCoordinate);	  \n"
				+ "}                              \n";

		final String pyramidFragmentShader = "precision mediump float;       \n" // Set
																					// the
																					// default
																					// precision
																					// to
																					// medium.
																					// We
																					// don't
																					// need
																					// as
																					// high
																					// of
																					// a
																					// precision
																					// in
																					// the
																					// fragment
																					// shader.
				+ "varying vec4 v_Color;          \n" // This is the color from
														// the vertex shader
														// interpolated across
														// the
														// triangle per
														// fragment.
				+ "void main()                    \n" // The entry point for our
														// fragment shader.
				+ "{                              \n"
				// Only show final output color.
				+ "gl_FragColor = v_Color;	  \n"
				+ "}                              \n";

		if (type == "cube") {
			return cubeFragmentShader;
		} else if (type == "pyramid") {
			return pyramidFragmentShader;
		} else {
			return fragmentShader;
		}
	}

	@Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
		// Set the background clear color to black.
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		// Use culling to remove back faces.
		GLES20.glEnable(GLES20.GL_CULL_FACE);

		// Enable depth testing
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		final String vertexShader = getVertexShader();
		final String fragmentShader = getFragmentShader("");
		final String cubeFragmentShader = getFragmentShader("cube");
		final String pyramidFragmentShader = getFragmentShader("pyramid");

		final int vertexShaderHandle = ShaderHelper.compileShader(
				GLES20.GL_VERTEX_SHADER, vertexShader);
		final int fragmentShaderHandle = ShaderHelper.compileShader(
				GLES20.GL_FRAGMENT_SHADER, fragmentShader);
		final int cubeFragmentShaderHandle = ShaderHelper.compileShader(
				GLES20.GL_FRAGMENT_SHADER, cubeFragmentShader);
		final int pyramidFragmentShaderHandle = ShaderHelper.compileShader(
				GLES20.GL_FRAGMENT_SHADER, pyramidFragmentShader);

		mPerVertexProgramHandle = ShaderHelper.createAndLinkProgram(
				vertexShaderHandle, fragmentShaderHandle,
				new String[] { "a_Position", "a_Color", "a_Normal",
						"a_TexCoordinate" });
		mCubeProgramHandle = ShaderHelper.createAndLinkProgram(
				vertexShaderHandle, cubeFragmentShaderHandle,
				new String[] { "a_Position", "a_Color", "a_Normal",
						"a_TexCoordinate" });
		mPyramidProgramHandle = ShaderHelper.createAndLinkProgram(
				vertexShaderHandle, pyramidFragmentShaderHandle,
				new String[] { "a_Position", "a_Color", "a_Normal",
						"a_TexCoordinate" });

		// Define a simple shader program for our point.
		final String pointVertexShader = "uniform mat4 u_MVPMatrix;      \n"
				+ "attribute vec4 a_Position;     \n"
				+ "void main()                    \n"
				+ "{                              \n"
				+ "   gl_Position = u_MVPMatrix   \n"
				+ "               * a_Position;   \n"
				+ "   gl_PointSize = 5.0;         \n"
				+ "}                              \n";

		final String pointFragmentShader = "precision mediump float;       \n"
				+ "void main()                    \n"
				+ "{                              \n"
				+ "   gl_FragColor = vec4(1.0,    \n"
				+ "   1.0, 1.0, 1.0);             \n"
				+ "}                              \n";

		final int pointVertexShaderHandle = compileShader(
				GLES20.GL_VERTEX_SHADER, pointVertexShader);
		final int pointFragmentShaderHandle = compileShader(
				GLES20.GL_FRAGMENT_SHADER, pointFragmentShader);
		mPointProgramHandle = createAndLinkProgram(pointVertexShaderHandle,
				pointFragmentShaderHandle, new String[] { "a_Position" });

		// Load the texture
		mTextureDataHandle = TextureHelper.loadTexture(mActivityContext,
				R.drawable.waterfall_hires);
	}

	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height) {
		// Set the OpenGL viewport to the same size as the surface.
		GLES20.glViewport(0, 0, width, height);
		windowWidth = width;
		windowHeight = height;

		// Create a new perspective projection matrix. The height will stay the
		// same
		// while the width will vary as per aspect ratio.
		final float ratio = (float) width / height;
		final float left = -ratio;
		final float right = ratio;
		final float bottom = -1.0f;
		final float top = 1.0f;
		final float near = 1.0f;
		final float far = 10.0f;

		Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near,
				far);
	}

	@Override
	public void onDrawFrame(GL10 glUnused) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		time = SystemClock.uptimeMillis() % 10000L;

		// Do a complete rotation every 10 seconds.
		angleInDegrees = (360.0f / 10000.0f) * ((int) time);

		// ADD LEVELS HERE.
		// Additional levels are created by changing the
		// texture of the cube to different images of various
		// environemnetsChange the texture to simulate different levels!

		/* Level 2 */
		if (levelTwo == true) {
			// Load the texture for the new level
			mTextureDataHandle = TextureHelper.loadTexture(mActivityContext,
					R.drawable.field);
			levelTwo = false;
		}
		/* Level 3 */
		else if (levelThree == true) {
			// Load the texture for the new level
			mTextureDataHandle = TextureHelper.loadTexture(mActivityContext,
					R.drawable.mountain);
			levelThree = false;
		}

		// END LEVELS.

		// Calculate position of the light. Rotate and then push into the
		// distance.
		Matrix.setIdentityM(mLightModelMatrix, 0);
		Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, -5.0f);
		Matrix.rotateM(mLightModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
		Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, 2.0f);

		Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0,
				mLightPosInModelSpace, 0);
		Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0,
				mLightPosInWorldSpace, 0);

		// Draw some pyramids.
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, pyrX, pyrY + 0.4f, pyrZ);
		Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, -1.0f, 0.0f);
		Matrix.scaleM(mModelMatrix, 0, 0.2f, 0.2f, 0.2f);
		drawPyramid(GLES20.GL_CCW, objectFound);

		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, pyrX, pyrY - 0.4f, pyrZ);
		Matrix.scaleM(mModelMatrix, 0, 1.0f, -1.0f, 1.0f);
		Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, -1.0f, 0.0f);
		Matrix.scaleM(mModelMatrix, 0, 0.2f, 0.2f, 0.2f);
		Matrix.translateM(mModelMatrix, 0, 0.0f, -2.0f, 0.0f);
		drawPyramid(GLES20.GL_CW, objectFound);

		// Draw cube for room.
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, -6.0f);
		Matrix.scaleM(mModelMatrix, 0, -4.0f, -4.0f, -4.0f);
		drawCube(GLES20.GL_CCW);

		// Draw a point to indicate the light.
		GLES20.glUseProgram(mPointProgramHandle);
		// drawLight();

		// We are looking toward the distance

		// float rollRadians = (float) SensorHubService.roll / 180* (float)
		// Math.PI;
		pitchRadians = (float) SensorHubService.pitch / 180 * (float) Math.PI;
		yawRadians = (float) SensorHubService.yaw / 180 * (float) Math.PI;

		/*
		 * If your head is the x/y plane, then yaw is the rotation around the
		 * y-axis and pitch is the rotation around the x-axis and roll is the
		 * rotation around the z-axis
		 */

		// lookX = 5.0f * FloatMath.cos(-pitchRadians)
		// * FloatMath.sin(-yawRadians);
		// lookY = 5.0f * FloatMath.sin(-pitchRadians);
		// lookZ = 5.0f * FloatMath.cos(-pitchRadians)
		// * FloatMath.cos(-yawRadians) - 6.0f;

		// we calibrated in opposite direction so the angles are opposite
		lookX = -5.0f * FloatMath.cos(pitchRadians) * FloatMath.sin(yawRadians);
		lookY = -5.0f * FloatMath.sin(pitchRadians);
		lookZ = 5.0f * FloatMath.cos(pitchRadians) * FloatMath.cos(yawRadians)
				- 6.0f;

		// These are the relative az and elev that will allow us to calculate
		// the needed az and elev for the audio
		// Spherical coordinates describing where we are looking
		rLook = FloatMath.sqrt(lookX * lookX + lookY * lookY + (lookZ + 6.0f)
				* (lookZ + 6.0f));
		elevLook = (float) ((float) 180 / Math.PI * Math
				.acos((double) (lookZ + 6.0f) / rLook));
		azLook = (float) ((float) 180 / Math.PI * Math.atan((double) lookY
				/ lookX));
		if (lookX < 0) {
			azLook = azLook + (float) Math.signum((double) lookY) * 180;
		}

		// These are the relative az and elev that will allow us to calculate
		// the needed az and elev for the audio
		// Spherical coordinates describing the location of the object emitting
		// noise
		rPyr = FloatMath.sqrt(pyrX * pyrX + pyrY * pyrY + (pyrZ + 6.0f)
				* (pyrZ + 6.0f));
		elevPyr = (float) ((float) 180 / Math.PI * Math
				.acos((double) (pyrZ + 6.0f) / rPyr));
		azPyr = (float) ((float) 180 / Math.PI * Math
				.atan((double) pyrY / pyrX));
		if (pyrX < 0) {
			azPyr = azPyr + (float) Math.signum((double) pyrY) * 180;
		}

		// These values are the az and elev we need to use in our audio code:
		// Our thinking was that if it's to the left we want the angle to be
		// negative
		SensorHubService.az = azLook - azPyr;
		SensorHubService.elev = elevLook - elevPyr;

		Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY,
				lookZ, upX, upY, upZ);

		// Convert object 3D coordinates to 2D window coordinates
		view[0] = windowWidth;
		view[1] = windowHeight;
		spacePos[0] = pyrX;
		spacePos[1] = pyrY;
		spacePos[2] = pyrZ;
		spacePos[3] = 1.0f;

		// Convert object coordinates to clip space coordinates
		Matrix.multiplyMV(clipSpacePosIntermediate, 0, mViewMatrix, 0,
				spacePos, 0);
		Matrix.multiplyMV(clipSpacePos, 0, mProjectionMatrix, 0,
				clipSpacePosIntermediate, 0);

		// Normalize using w coordinate
		ndcSpacePos[0] = clipSpacePos[0] / clipSpacePos[3];
		ndcSpacePos[1] = clipSpacePos[1] / clipSpacePos[3];
		ndcSpacePos[2] = clipSpacePos[2] / clipSpacePos[3];

		// Convert to 2D
		outputCoords[0] = ((ndcSpacePos[0] + 1.0f) / 2.0f) * view[0];
		outputCoords[1] = ((ndcSpacePos[1] + 1.0f) / 2.0f) * view[1];

		// Check if octahedron is displayed in center of viewscreen
		if (outputCoords[0] > view[0] / 2 - 50
				&& outputCoords[0] < view[0] / 2 + 50) {
			if (outputCoords[1] > view[1] / 2 - 50
					&& outputCoords[1] < view[1] / 2 + 50) {
				objectFound = true;
			} else {
				objectFound = false;
			}
		} else {
			objectFound = false;
		}
	}

	public boolean hasBeenFound() {
		return objectFound;
	}

	// Draws a cube.
	private void drawCube(int mode) {
		// Set face rotation
		GLES20.glFrontFace(mode);

		// Set our per-vertex lighting program.
		// Change this to switch between colored walls and textured walls,
		// mPyramidProgramHandle and mCubeProgramHandle respectively.
		GLES20.glUseProgram(mCubeProgramHandle);

		// Set program handles for cube drawing.
		mMVPMatrixHandle = GLES20.glGetUniformLocation(mCubeProgramHandle,
				"u_MVPMatrix");
		mMVMatrixHandle = GLES20.glGetUniformLocation(mCubeProgramHandle,
				"u_MVMatrix");
		mLightPosHandle = GLES20.glGetUniformLocation(mCubeProgramHandle,
				"u_LightPos");
		mTextureUniformHandle = GLES20.glGetUniformLocation(mCubeProgramHandle,
				"u_Texture");
		mPositionHandle = GLES20.glGetAttribLocation(mCubeProgramHandle,
				"a_Position");
		mColorHandle = GLES20
				.glGetAttribLocation(mCubeProgramHandle, "a_Color");
		mNormalHandle = GLES20.glGetAttribLocation(mCubeProgramHandle,
				"a_Normal");
		mTextureCoordinateHandle = GLES20.glGetAttribLocation(
				mCubeProgramHandle, "a_TexCoordinate");

		// Set the active texture unit to texture unit 0.
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

		// Bind the texture to this unit.
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);

		// Tell the texture uniform sampler to use this texture in the shader by
		// binding to texture unit 0.
		GLES20.glUniform1i(mTextureUniformHandle, 0);

		// Pass in the position information
		mCubePositions.position(0);
		GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize,
				GLES20.GL_FLOAT, false, 0, mCubePositions);

		GLES20.glEnableVertexAttribArray(mPositionHandle);

		// Pass in the color information
		mCubeColors.position(0);
		GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize,
				GLES20.GL_FLOAT, false, 0, mCubeColors);

		GLES20.glEnableVertexAttribArray(mColorHandle);

		// Pass in the normal information
		mCubeNormals.position(0);
		GLES20.glVertexAttribPointer(mNormalHandle, mNormalDataSize,
				GLES20.GL_FLOAT, false, 0, mCubeNormals);

		GLES20.glEnableVertexAttribArray(mNormalHandle);

		// Pass in the texture coordinate information
		mCubeTextureCoordinates.position(0);
		GLES20.glVertexAttribPointer(mTextureCoordinateHandle,
				mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0,
				mCubeTextureCoordinates);

		GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

		// This multiplies the view matrix by the model matrix, and stores the
		// result in the MVP matrix
		// (which currently contains model * view).
		Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

		// Pass in the modelview matrix.
		GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);

		// This multiplies the modelview matrix by the projection matrix, and
		// stores the result in the MVP matrix
		// (which now contains model * view * projection).
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

		// Pass in the combined matrix.
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

		// Pass in the light position in eye space.
		GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0],
				mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

		// Draw the cube.
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
	}

	private void drawPyramid(int mode, boolean found) {
		// Set face rotation
		GLES20.glFrontFace(mode);

		// Set our per-vertex lighting program.
		GLES20.glUseProgram(mPyramidProgramHandle);

		// Set program handles for cube drawing.
		mMVPMatrixHandle = GLES20.glGetUniformLocation(mPyramidProgramHandle,
				"u_MVPMatrix");
		mMVMatrixHandle = GLES20.glGetUniformLocation(mPyramidProgramHandle,
				"u_MVMatrix");
		mLightPosHandle = GLES20.glGetUniformLocation(mPyramidProgramHandle,
				"u_LightPos");
		mTextureUniformHandle = GLES20.glGetUniformLocation(
				mPyramidProgramHandle, "u_Texture");
		mColorHandle = GLES20.glGetAttribLocation(mPyramidProgramHandle,
				"a_Color");

		// Pass in the position information
		mPyramidPositions.position(0);
		GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize,
				GLES20.GL_FLOAT, false, 0, mPyramidPositions);

		GLES20.glEnableVertexAttribArray(mPositionHandle);

		// Pass in the color information depending on whether or not it has been
		// found
		if (found) {
			mFoundPyramidColors.position(0);
			GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize,
					GLES20.GL_FLOAT, false, 0, mFoundPyramidColors);
		} else {
			mPyramidColors.position(0);
			GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize,
					GLES20.GL_FLOAT, false, 0, mPyramidColors);
		}

		GLES20.glEnableVertexAttribArray(mColorHandle);

		// Pass in the normal information
		mPyramidNormals.position(0);
		GLES20.glVertexAttribPointer(mNormalHandle, mNormalDataSize,
				GLES20.GL_FLOAT, false, 0, mPyramidNormals);

		GLES20.glEnableVertexAttribArray(mNormalHandle);

		// This multiplies the view matrix by the model matrix, and stores the
		// result in the MVP matrix
		// (which currently contains model * view).
		Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

		// Pass in the modelview matrix.
		GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);

		// This multiplies the modelview matrix by the projection matrix, and
		// stores the result in the MVP matrix
		// (which now contains model * view * projection).
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

		// Pass in the combined matrix.
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

		// Pass in the light position in eye space.
		GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0],
				mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

		// Draw the pyramid.
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 12);
	}

	/**
	 * Draws a point representing the position of the light.
	 */
	private void drawLight() {
		final int pointMVPMatrixHandle = GLES20.glGetUniformLocation(
				mPointProgramHandle, "u_MVPMatrix");
		final int pointPositionHandle = GLES20.glGetAttribLocation(
				mPointProgramHandle, "a_Position");

		// Pass in the position.
		GLES20.glVertexAttrib3f(pointPositionHandle, mLightPosInModelSpace[0],
				mLightPosInModelSpace[1], mLightPosInModelSpace[2]);

		// Since we are not using a buffer object, disable vertex arrays for
		// this attribute.
		GLES20.glDisableVertexAttribArray(pointPositionHandle);

		// Pass in the transformation matrix.
		Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mLightModelMatrix, 0);
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
		GLES20.glUniformMatrix4fv(pointMVPMatrixHandle, 1, false, mMVPMatrix, 0);

		// Draw the point.
		GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);
	}

	/**
	 * Helper function to compile a shader.
	 * 
	 * @param shaderType
	 *            The shader type.
	 * @param shaderSource
	 *            The shader source code.
	 * @return An OpenGL handle to the shader.
	 */
	private int compileShader(final int shaderType, final String shaderSource) {
		int shaderHandle = GLES20.glCreateShader(shaderType);

		if (shaderHandle != 0) {
			// Pass in the shader source.
			GLES20.glShaderSource(shaderHandle, shaderSource);

			// Compile the shader.
			GLES20.glCompileShader(shaderHandle);

			// Get the compilation status.
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS,
					compileStatus, 0);

			// If the compilation failed, delete the shader.
			if (compileStatus[0] == 0) {
				Log.e(TAG,
						"Error compiling shader: "
								+ GLES20.glGetShaderInfoLog(shaderHandle));
				GLES20.glDeleteShader(shaderHandle);
				shaderHandle = 0;
			}
		}

		if (shaderHandle == 0) {
			throw new RuntimeException("Error creating shader.");
		}

		return shaderHandle;
	}

	/**
	 * Helper function to compile and link a program.
	 * 
	 * @param vertexShaderHandle
	 *            An OpenGL handle to an already-compiled vertex shader.
	 * @param fragmentShaderHandle
	 *            An OpenGL handle to an already-compiled fragment shader.
	 * @param attributes
	 *            Attributes that need to be bound to the program.
	 * @return An OpenGL handle to the program.
	 */
	private int createAndLinkProgram(final int vertexShaderHandle,
			final int fragmentShaderHandle, final String[] attributes) {
		int programHandle = GLES20.glCreateProgram();

		if (programHandle != 0) {
			// Bind the vertex shader to the program.
			GLES20.glAttachShader(programHandle, vertexShaderHandle);

			// Bind the fragment shader to the program.
			GLES20.glAttachShader(programHandle, fragmentShaderHandle);

			// Bind attributes
			if (attributes != null) {
				final int size = attributes.length;
				for (int i = 0; i < size; i++) {
					GLES20.glBindAttribLocation(programHandle, i, attributes[i]);
				}
			}

			// Link the two shaders together into a program.
			GLES20.glLinkProgram(programHandle);

			// Get the link status.
			final int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS,
					linkStatus, 0);

			// If the link failed, delete the program.
			if (linkStatus[0] == 0) {
				Log.e(TAG,
						"Error compiling program: "
								+ GLES20.glGetProgramInfoLog(programHandle));
				GLES20.glDeleteProgram(programHandle);
				programHandle = 0;
			}
		}

		if (programHandle == 0) {
			throw new RuntimeException("Error creating program.");
		}

		return programHandle;
	}
}