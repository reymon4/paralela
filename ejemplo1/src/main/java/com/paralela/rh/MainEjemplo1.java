package com.paralela.rh;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

class MainEjemplo1 {
    private static long window;
    static int textureID;

    static FPSCounter fpsCounter = new FPSCounter();
    static void run() {
        System.out.println("LWJGL " + Version.getVersion());

        init();

        loop();
    }

    static void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        // Create the window
        window = glfwCreateWindow(Params.WIDTH, Params.HEIGHT, "Test", NULL, NULL);

        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);
        });

        glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
            glViewport(0, 0, width, height);
        });

        {
            // centrar ventana
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - Params.WIDTH) / 2,
                    (vidmode.height() - Params.HEIGHT) / 2
            );
        }

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);

        GL.createCapabilities();

        String version = glGetString(GL_VERSION);
        String vendor = glGetString(GL_VENDOR);
        String renderer = glGetString(GL_RENDERER);

        System.out.println("OpenGL version: " + version);
        System.out.println("OpenGL vendor: " + vendor);
        System.out.println("OpenGL renderer: " + renderer);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(-1, 1, -1, 1, -1, 1);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        glEnable(GL_TEXTURE_2D);

        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        initTexteures();
    }

    static void initTexteures() {
        textureID = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, textureID);

        glTexImage2D(GL_TEXTURE_2D,
                0,
                GL_RGBA8,
                Params.WIDTH, Params.HEIGHT, 0,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                NULL
        );

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        glBindTexture(GL_TEXTURE_2D, 0);
    }

    static void loop() {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            paint();

            glfwSwapBuffers(window);

            glfwPollEvents();
        }
    }


    //--------------------------------------------------------------------------------
    static int pixel_buffer[] = new int[Params.WIDTH * Params.HEIGHT]; // size= WIDTHxHEIGHT

    static void mandelbrotCpu() {
        int maxIterations = 100;
        double escapeRadius = 2.0;
        double escapeRadiusSquared = escapeRadius * escapeRadius;

        double scaleX = (Params.xMax - Params.xMin) / Params.WIDTH;
        double scaleY = (Params.yMax - Params.yMin) / Params.HEIGHT;

        for (int px = 0; px < Params.WIDTH; px++) {
            for (int py = 0; py < Params.HEIGHT; py++) {
                // Mapear píxel (px, py) al plano complejo
                double cReal = Params.xMin + px * scaleX;
                double cImag = Params.yMin + py * scaleY;

                // Inicializar z = 0 en el plano complejo
                double zReal = 0.0, zImag = 0.0;
                int iteration = 0;

                // Aplicar la fórmula iterativa: z_{n+1} = z_n^2 + c
                while (iteration < maxIterations) {
                    double zRealSquared = zReal * zReal;
                    double zImagSquared = zImag * zImag;

                    if (zRealSquared + zImagSquared > escapeRadiusSquared) {
                        break;
                    }
                    double zRealTemp = zRealSquared - zImagSquared + cReal;
                    zImag = 2.0 * zReal * zImag + cImag;
                    zReal = zRealTemp;

                    iteration++;
                }

                // Determinar el color: negro si no escapa, blanco si escapa
               // int color = (iteration == maxIterations) ? 0x000000 : 0xFFFFFF;
                //int color = getColor(iteration,maxIterations);
                int color = (iteration == maxIterations)? 0x000000 : Params.color_ramp[iteration%Params.PALETTE_SIZE];


                // Almacenar el color en el buffer de píxeles
                pixel_buffer[py * Params.WIDTH + px] = color;
            }
        }

        //dibujar
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexImage2D(GL_TEXTURE_2D,
                0,
                GL_RGBA,
                Params.WIDTH, Params.HEIGHT, 0,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                pixel_buffer);

        glBegin(GL_QUADS);
        {
            glTexCoord2f(0, 1);
            glVertex3f(-1, -1, 0);

            glTexCoord2f(0, 0);
            glVertex3f(-1, 1, 0);

            glTexCoord2f(1, 0);
            glVertex3f(1, 1, 0);

            glTexCoord2f(1, 1);
            glVertex3f(1, -1, 0);
        }
        glEnd();
    }

    //Método para obtener los colores
    static int getColor(int iteration, int maxIterations) {
        float t = (float) iteration / maxIterations;
        int r = (int) (9 * (1 - t) * t * t * t * 255);
        int g = (int) (15 * (1 - t) * (1 - t) * t * t * 255);
        int b = (int) (8.5 * (1 - t) * (1 - t) * (1 - t) * t * 255);

        return (r << 16) | (g << 8) | b; // Color in RGB
    }
    //--------------------------------------------------------------------------------

    static void paint() {
        fpsCounter.update();
        mandelbrotCpu();

//        glBegin(GL_TRIANGLES);
//        {
//            glVertex2d(-1, -1);
//            glVertex2d(0, 0);
//            glVertex2d(0, -1);
//        }
//        glEnd();
    }

    public static void main(String[] args) {
        run();
    }
}
