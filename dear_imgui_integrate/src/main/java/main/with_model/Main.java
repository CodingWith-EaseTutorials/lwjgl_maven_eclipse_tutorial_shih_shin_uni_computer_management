package main.with_model;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImInt;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.Platform;
import utils.Camera;
import utils.Model;
import utils.Shader;
import utils.Time;

import static org.lwjgl.opengl.GL11.*;
import static utils.Camera.CameraMovement;

import java.util.logging.Logger;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;

public class Main {
    private static Logger logger = Logger.getAnonymousLogger();

    // Window size
    private static int windowWidth = 800;
    private static int windowHeight = 600;

    private static boolean updateProjection = true;

    // Camera
    private static Camera camera = new Camera();
    static {
        camera.position.z = 3.0f;
    }

    private static final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private static final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    private static final String glslVersion = "#version 330";;

    private static Time time;

    private static float lastX = (float)windowWidth / 2.0f;
    private static float lastY = (float)windowHeight / 2.0f;
    private static boolean firstMouseInput = true;

    static boolean pressedTurnOnOffMouse = false;
    static boolean mouseOn = false;

    //ImGUI values holders:
    private static float[] sliderValue = {0};
    private static float[] colorsValue = {1.f, 1.f, 1.f};
    private static ImInt radioValue = new ImInt(0);

    public static void main(String[] args) {
        GLFW.glfwInit();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        if (Platform.get() == Platform.MACOSX) {
            GLFW.glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, 1);
        }

        long window = GLFW.glfwCreateWindow(800, 600, "LearnOpenGL", 0L, 0L);
        if (window == 0L) {
            logger.severe("Failed to create GLFW Window");
            GLFW.glfwTerminate();
            return;
        }

        glfwMakeContextCurrent(window);
        glfwSetFramebufferSizeCallback(window, FRAMEBUFFER_SIZE_CALLBACK);
        glfwSetCursorPosCallback(window, MOUSE_MOVE_CALLBACK);
        glfwSetScrollCallback(window, SCROLL_CALLBACK);

        // Tell GLFW to capture our mouse
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        // Load OpenGL functions
        final GLCapabilities gl = GL.createCapabilities();
        if(gl == null) {
            logger.severe("Failed to initialize OpenGL");
            glfwTerminate();
            return;
        }

        Shader ourShader = new Shader("src/main/resources/shaders/model_lighted_vs.glsl", "src/main/resources/shaders/model_lighted_fs.glsl");
        ourShader.setInt("material.diffuse", 0);
        ourShader.setInt("material.specular", 1);
        ourShader.setFloat("alphaValue", 1);

        Model ourModel = new Model("src/main/resources/objects/nanosuit/nanosuit.obj");

        // Draw in wireframe
        // glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        // Configure global OpenGL state
        glEnable(GL_DEPTH_TEST);

        // Pass projection matrix to shader (as projection matrix rarely changes there's no need to do this per frame)
        // ** This is true as long as you don't change the window size!
        // That's why I check every frame if the projection matrix has to be changed
        Matrix4f projection = new Matrix4f();

        // Create the model matrix before enter the loop to avoid calling new every frame
        Matrix4f model = new Matrix4f();


        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        imGuiGlfw.init(window, true);
        imGuiGl3.init(glslVersion);

        time = new Time();

        while(!GLFW.glfwWindowShouldClose(window)) {
            time.syncTime();

            // Clear the screen
            glClearColor(0.05f, 0.05f, 0.05f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            imGuiGlfw.newFrame();
            ImGui.newFrame();

            // Input
            processInput(window);

            // Do not forget to enable shader before setting uniforms
            ourShader.use();

            ourShader.setVec3("viewPos", camera.getPosition());
            ourShader.setFloat("material.shininess", 32.0f);
//            ourShader.setVec3("material.color", 1.f, 1.f, 1.f);

            ourShader.setVec3("dirLight.direction", -0.2f, -1.0f, -0.3f);
            ourShader.setVec3("dirLight.ambient", 0.05f, 0.05f, 0.05f);
            ourShader.setVec3("dirLight.diffuse", 0.4f, 0.4f, 0.4f);
            ourShader.setVec3("dirLight.specular", 0.5f, 0.5f, 0.5f);

            // point light 1
            ourShader.setVec3("pointLights[0].position", pointLightPositions[0]);
            ourShader.setVec3("pointLights[0].ambient", 0.05f, 0.05f, 0.05f);
            ourShader.setVec3("pointLights[0].diffuse", 3.f, 3.f, 3.f);
            ourShader.setVec3("pointLights[0].specular", 1.0f, 1.0f, 1.0f);
            ourShader.setFloat("pointLights[0].constant", 1.0f);
            ourShader.setFloat("pointLights[0].linear", 0.09f);
            ourShader.setFloat("pointLights[0].quadratic", 0.032f);
            // point light 2
            ourShader.setVec3("pointLights[1].position", pointLightPositions[1]);
            ourShader.setVec3("pointLights[1].ambient", 0.05f, 0.05f, 0.05f);
            ourShader.setVec3("pointLights[1].diffuse", 2.f, 2.f, 2.f);
            ourShader.setVec3("pointLights[1].specular", 1.0f, 1.0f, 1.0f);
            ourShader.setFloat("pointLights[1].constant", 1.0f);
            ourShader.setFloat("pointLights[1].linear", 0.09f);
            ourShader.setFloat("pointLights[1].quadratic", 0.032f);

            // spotLight
            ourShader.setVec3("spotLights[0].position", spotLightPositions[0]);
            ourShader.setVec3("spotLights[0].direction", new Vector3f(0.f, -1.f, 0.f));
            ourShader.setVec3("spotLights[0].ambient", 0.0f, 0.0f, 0.0f);
            ourShader.setVec3("spotLights[0].diffuse", 2.0f, 2.0f, 10.f);
            ourShader.setVec3("spotLights[0].specular", 1.0f, 1.0f, 1.0f);
            ourShader.setFloat("spotLights[0].constant", 1.0f);
            ourShader.setFloat("spotLights[0].linear", 0.09f);
            ourShader.setFloat("spotLights[0].quadratic", 0.032f);
            ourShader.setFloat("spotLights[0].cutOff", Math.cos(Math.toRadians(12.5f)));
            ourShader.setFloat("spotLights[0].outerCutOff", Math.cos(Math.toRadians(30.f)));

            // Spotlight from camera.
            ourShader.setVec3("spotLights[1].position", camera.position);
            ourShader.setVec3("spotLights[1].direction", camera.front);
            ourShader.setVec3("spotLights[1].ambient", 0.0f, 0.0f, 0.0f);
            ourShader.setVec3("spotLights[1].diffuse", 10.0f, 3.0f, 3.f);
            ourShader.setVec3("spotLights[1].specular", 1.0f, 1.0f, 1.0f);
            ourShader.setFloat("spotLights[1].constant", 1.0f);
            ourShader.setFloat("spotLights[1].linear", 0.09f);
            ourShader.setFloat("spotLights[1].quadratic", 0.032f);
            ourShader.setFloat("spotLights[1].cutOff", Math.cos(Math.toRadians(12.5f)));
            ourShader.setFloat("spotLights[1].outerCutOff", Math.cos(Math.toRadians(15.f)));


            // Update projection matrix if necessary
            if(updateProjection) {
                projection.setPerspective((float)Math.toRadians(camera.zoom), (float)windowWidth / (float)windowHeight,
                        0.1f, 100.0f);
                ourShader.setMat4("projection", projection);
                updateProjection = false;
            }

            // Camera/view transformations
            final Matrix4f view = camera.getViewMatrix();
            ourShader.setMat4("view", view);

            // Render the loaded model
            model.translation(0.0f, -1.75f, 0.0f); // Translate it down so it's at the center of the scene
            model.scale(0.2f); // It's a bit too big for our scene, so scale it down
            ourShader.setMat4("model", model);
            ourModel.draw(ourShader);


            ImGui.begin("Cool Window"/*, ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoResize*/);
//            ImGui.setWindowPos(0, 0);
            ImGui.setWindowSize(windowWidth / 3.6f, windowHeight / 3.6f);
            if (ImGui.button("Cool Button", 100, 20)) {
                System.out.println("Cool!");
            }

            ImGui.sliderFloat("BallSize", sliderValue, 1.f, 5.f);
            ImGui.colorEdit3("Spinning wheel color", colorsValue);

            ImGui.newLine();

            ImGui.radioButton("Select Me!", radioValue, 0);
            ImGui.radioButton("Or Me!", radioValue, 1);

            ImGui.end();


            ImGui.render();
            imGuiGl3.renderDrawData(ImGui.getDrawData());

            // Swap buffers and poll IO events (key/mouse events)
            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();
        Callbacks.glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    private static void processInput(long window) {
        if (glfwGetKey(window, GLFW_KEY_Z) == GLFW_PRESS && !pressedTurnOnOffMouse) {
            glfwSetInputMode(window, GLFW_CURSOR, mouseOn? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);
            mouseOn = !mouseOn;
            //This can prevent game glitch when pressing Z key.
            //Treat returning the mouse back as first mouse input; so that it won't cause mouse position glitch.
            if (!mouseOn) firstMouseInput = true;
            pressedTurnOnOffMouse = true;
            System.out.println("Pressed Z");
        } else if (glfwGetKey(window, GLFW_KEY_Z) == GLFW_RELEASE && pressedTurnOnOffMouse) {
            pressedTurnOnOffMouse = false;
            System.out.println("Released Z");
        }


        //Only give player input controls when mouse is not on.
        if (!mouseOn) {

            final float speed = camera.movementSpeed;

            // Bonus: if left shift key is pressed, you double the speed!
            if(glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) {
                camera.movementSpeed *= 2.0f;
            }

            if(glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
                camera.processKeyboard(CameraMovement.FORWARD, time.getDeltaTime());
            }

            if(glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
                camera.processKeyboard(CameraMovement.BACKWARD, time.getDeltaTime());
            }

            if(glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
                camera.processKeyboard(CameraMovement.LEFT, time.getDeltaTime());
            }

            if(glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
                camera.processKeyboard(CameraMovement.RIGHT, time.getDeltaTime());
            }

            if (glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS) {
                camera.processKeyboard(CameraMovement.UP, time.getDeltaTime());
            }

            if (glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS) {
                camera.processKeyboard(CameraMovement.DOWN, time.getDeltaTime());
            }

            camera.movementSpeed = speed;
        }

        // Close window when ESC key is pressed
        if(glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
            glfwSetWindowShouldClose(window, true);

        }
    }

    static Vector3f[] pointLightPositions = new Vector3f[]{
            new Vector3f(4.f, 6.f, 4.f),
            new Vector3f(4.f, 6.f, -4.f),
            new Vector3f(-4.f, 6.f, 4.f),
            new Vector3f(-4.f, 6.f, -4.f),
            new Vector3f(0.f, .6f, 0.f),
    };
    static Vector3f[] spotLightPositions = new Vector3f[]{
            new Vector3f(0.f, 4.f, 0.f),
            new Vector3f(3.5f, 6.f, 0.f),
            new Vector3f(-3.5f, 6.f, 0.f),
            new Vector3f(0.f, 6.f, -7.f),
    };

    // ============== Callbacks ==============

    private static final GLFWFramebufferSizeCallbackI FRAMEBUFFER_SIZE_CALLBACK = (window, width, height) -> {
        // make sure the viewport matches the new window dimensions; note that width and
        // height will be significantly larger than specified on retina displays.
        glViewport(0, 0, width, height);
        // Also update the window width and height variables to correctly set the projection matrix
        windowWidth = width;
        windowHeight = height;
        updateProjection = true;
    };

    private static final GLFWCursorPosCallbackI MOUSE_MOVE_CALLBACK = (window, xpos, ypos) -> {
        if (!mouseOn) {
            if(firstMouseInput) {
                lastX = (float)xpos;
                lastY = (float)ypos;
                firstMouseInput = false;
            }

            float xoffset = (float)xpos - lastX;
            float yoffset = lastY - (float)ypos; // Reversed since y-coordinates go from bottom to top
            lastX = (float)xpos;
            lastY = (float)ypos;

            camera.processMouseMovement(xoffset, yoffset, true);
        }
    };

    private static final GLFWScrollCallbackI SCROLL_CALLBACK = (window, xoffset, yoffset) -> {
        camera.processMouseScroll((float)yoffset);
        updateProjection = true;
    };
}
