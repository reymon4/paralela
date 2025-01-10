package com.paralela.rh;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface FractalDll extends Library {

    String LIBRARY_NAME = "libfractal_dll";
    //Creamos el proxy para conectarnos con el dll definido
    FractalDll INSTANCE = Native.load(LIBRARY_NAME, FractalDll.class);

    void mandelbrotCpu(int[] pixels, int num_iteraciones);


}
