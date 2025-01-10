package com.paralela.rh;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {
    static int WIDTH = 1600;
    static int HEIGHT = 900;
    public static void main(String[] args) throws IOException {
        int[] pixels = new int[WIDTH * HEIGHT];
        int num_iteraciones = 1000;

        FractalDll.INSTANCE.mandelbrotCpu(pixels, num_iteraciones);

        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);

        image.setRGB(0, 0, WIDTH, HEIGHT, pixels, 0, WIDTH);
        File archivo = new File("mandelbrot-cpp.png");
        ImageIO.write(image, "png", archivo);

    }
}
