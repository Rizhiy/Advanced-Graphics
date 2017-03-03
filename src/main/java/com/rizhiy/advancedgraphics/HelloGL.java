package com.rizhiy.advancedgraphics;

import org.ejml.data.DenseMatrix64F;
import org.ejml.simple.SimpleMatrix;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.*;

import java.nio.FloatBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glEnable;


public class HelloGL {

    public static void main(String[] args) {


        ///////////////////////////////////////////////////////////////////////////
        // Set up GLFW window

//        GLFWErrorCallback errorCallback = GLFWErrorCallback.createPrint(System.err);
//        GLFW.glfwSetErrorCallback(errorCallback);

        final int   width       = 800;
        final int   height      = 600;
        final float fov         = 60f;
        final float aspectRatio = (float) width / (float) height;
        final float near_plane  = 0.01f;
        final float far_plane   = 1000.0f;

        final float y_scale         = (float) (1f / Math.tan(fov / 2 * Math.PI / 180));
        final float x_scale         = (float) (y_scale / aspectRatio);
        final float frustrum_length = far_plane - near_plane;


        GLFW.glfwInit();
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
        long window = GLFW.glfwCreateWindow(width/* width */, height /* height */, "HelloGL", 0, 0);
        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(1);
        GLFW.glfwShowWindow(window);

        ///////////////////////////////////////////////////////////////////////////
        // Set up OpenGL

        GL.createCapabilities();
        GLUtil.setupDebugMessageCallback();
        GL11.glClearColor(0.2f, 0.4f, 0.6f, 0.0f);
        GL11.glClearDepth(1.0f);
        glEnable(GL_DEPTH_TEST);

        ///////////////////////////////////////////////////////////////////////////
        // Set up minimal shader programs

        String vertex_shader   = Utilities.loadShaderSource("/home/rizhiy/Dropbox/CS STUFF/Advanced Graphics/src/main/GLSL/vertex_shader.glsl");
        String fragment_shader = Utilities.loadShaderSource("/home/rizhiy/Dropbox/CS STUFF/Advanced Graphics/src/main/GLSL/fragment_shader.glsl");

        // Compile vertex shader
        int vs = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vs, vertex_shader);
        GL20.glCompileShader(vs);

        // Compile fragment shader
        int fs = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fs, fragment_shader);
        GL20.glCompileShader(fs);

        // Link vertex and fragment shaders into an active program
        int program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vs);
        GL20.glAttachShader(program, fs);
        GL20.glLinkProgram(program);
        GL20.glUseProgram(program);

        ///////////////////////////////////////////////////////////////////////////
        // Set up data

        // Fill a Java FloatBuffer object with memory-friendly floats
        List<Float> coords = Utilities.offTofloatArray("/home/rizhiy/Dropbox/CS STUFF/Advanced Graphics/src/main/java/com/rizhiy/advancedgraphics/off/bunny.off");
        FloatBuffer fbo    = BufferUtils.createFloatBuffer(coords.size());
        for (Float f : coords) {
            fbo.put(f); // Copy the vertex coords into the floatbuffer
        }
        fbo.flip();                                     // Mark the floatbuffer ready for reads

        // Store the FloatBuffer's contents in a Vertex Buffer Object
        int vbo = GL15.glGenBuffers();                  // Get an OGL name for the VBO
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);   // Activate the VBO
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, fbo, GL15.GL_STATIC_DRAW);  // Send VBO data to GPU

        // Bind the VBO in a Vertex Array Object
        int vao = GL30.glGenVertexArrays();             // Get an OGL name for the VAO
        GL30.glBindVertexArray(vao);    // Activate the VAO
        int vLoc = GL20.glGetAttribLocation(program, "v");
        GL20.glEnableVertexAttribArray(vLoc);              // Enable the VAO's first attribute (0)
        GL20.glVertexAttribPointer(vLoc, 3, GL11.GL_FLOAT, false, 0, 0);  // Link VBO to VAO attrib 0


        double data[][] = new double[][]{{1.0f, 0, 0, 0},
                                         {0, 1.0f, 0, 0},
                                         {0, 0, 1.0f, 0},
                                         {0, 0, 0, 1.0f}};
        FloatBuffer Identity4 = floatArrayToBuffer(data);

        //
        data = new double[][]{{1.0f, 0, 0, 0},
                              {0, 1.0f, 0, 0},
                              {0, 0, 1.0f, 0},
                              {0, 0, 0, 1.0f}};
        SimpleMatrix matrix = new SimpleMatrix(data);
        matrix = translate(matrix, 0, 0, 0);
//        matrix = rotateX(matrix, -70);
//        matrix = rotateZ(matrix, 50);
//        matrix = rotateY(matrix, -30);
//        matrix = uniformScale(matrix,1);
        FloatBuffer model = floatArrayToBuffer(sMatrixToArray(matrix.getMatrix()));

        int modelToWorldLoc = GL20.glGetUniformLocation(program, "modelToWorld");
        if (modelToWorldLoc != -1) {
            GL20.glUniformMatrix4fv(modelToWorldLoc, false, model);
        }
        int worldToCameraLoc = GL20.glGetUniformLocation(program, "worldToCamera");
        if (worldToCameraLoc != -1) {
            GL20.glUniformMatrix4fv(worldToCameraLoc, false, Identity4);
        }


//        data = new double[][]{{x_scale, 0, 0, 0},
//                              {0, y_scale, 0, 0},
//                              {0, 0, -(far_plane + near_plane) / frustrum_length, -(2 * near_plane * far_plane) / frustrum_length},
//                              {0, 0, 1, 0}};
        System.out.print(new SimpleMatrix(data).minus(matrix).toString());
        FloatBuffer cameraToScreen = floatArrayToBuffer(data);
        FloatBuffer projection = cameraToScreen;

        int cameraToScreenLoc = GL20.glGetUniformLocation(program, "cameraToScreen");
        if (cameraToScreenLoc != -1) {
            GL20.glUniformMatrix4fv(cameraToScreenLoc, false, projection);
        }

        ///////////////////////////////////////////////////////////////////////////
        // Loop until window is closed

        while (!GLFW.glfwWindowShouldClose(window)) {
            GLFW.glfwPollEvents();

            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            GL30.glBindVertexArray(vao);
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);

            GLFW.glfwSwapBuffers(window);
        }

        ///////////////////////////////////////////////////////////////////////////
        // Clean up

        GL15.glDeleteBuffers(vbo);
        GL30.glDeleteVertexArrays(vao);
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
        GLFW.glfwSetErrorCallback(null).free();
    }

    public static FloatBuffer floatArrayToBuffer(final double[][] data) {
        FloatBuffer result = BufferUtils.createFloatBuffer(16);
        for (int col = 0; col < 4; col++) {
            for (int row = 0; row < 4; row++) {
                result.put((float) (data[row][col]));
            }
        }
        result.flip();
        return result;
    }

    public static SimpleMatrix rotateX(SimpleMatrix matrix, double angle) {
        angle = angle * Math.PI / 180;
        double data[][] = new double[][]{{1, 0, 0, 0},
                                         {0, Math.cos(angle), -Math.sin(angle), 0},
                                         {0, Math.sin(angle), Math.cos(angle), 0},
                                         {0, 0, 0, 1.0}};
        SimpleMatrix rotationMatrix = new SimpleMatrix(data);
        matrix = matrix.mult(rotationMatrix);
        return matrix;
    }

    public static SimpleMatrix rotateY(SimpleMatrix matrix, double angle) {
        angle = angle * Math.PI / 180;
        double data[][] = new double[][]{{Math.cos(angle), 0, Math.sin(angle), 0},
                                         {0, 1.0, 0, 0},
                                         {-Math.sin(angle), 0, Math.cos(angle), 0},
                                         {0, 0, 0, 1.0}};
        SimpleMatrix rotationMatrix = new SimpleMatrix(data);
        matrix = matrix.mult(rotationMatrix);
        return matrix;
    }

    public static SimpleMatrix rotateZ(SimpleMatrix matrix, double angle) {
        angle = angle * Math.PI / 180;
        double data[][] = new double[][]{{Math.cos(angle), -Math.sin(angle), 0, 0},
                                         {Math.sin(angle), Math.cos(angle), 0, 0},
                                         {0, 0, 1, 0},
                                         {0, 0, 0, 1.0}};
        SimpleMatrix rotationMatrix = new SimpleMatrix(data);
        matrix = matrix.mult(rotationMatrix);
        return matrix;
    }

    public static SimpleMatrix translate(SimpleMatrix matrix, double x, double y, double z) {
        double data[][] = new double[][]{{1, 0, 0, x},
                                         {0, 1, 0, y},
                                         {0, 0, 1, z},
                                         {0, 0, 0, 1}};
        SimpleMatrix translationMatrix = new SimpleMatrix(data);
        matrix = matrix.mult(translationMatrix);
        return matrix;
    }

    public static SimpleMatrix uniformScale(SimpleMatrix matrix, double s){
        double data[][] = new double[][]{{1, 0, 0, 0},
                                         {0, 1, 0, 0},
                                         {0, 0, 1, 0},
                                         {0, 0, 0, 1/s}};
        SimpleMatrix scaleMatrix = new SimpleMatrix(data);
        matrix = matrix.mult(scaleMatrix);
        return matrix;
    }

    private static double[][] sMatrixToArray(DenseMatrix64F matrix) {
        double array[][] = new double[matrix.getNumRows()][matrix.getNumCols()];
        for (int r = 0; r < matrix.getNumRows(); r++) {
            for (int c = 0; c < matrix.getNumCols(); c++) {
                array[r][c] = matrix.get(r, c);
            }
        }
        return array;
    }
}