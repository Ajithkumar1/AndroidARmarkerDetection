package com.example.root.androidarmarkerdetection;

/**
 * Created by root on 20/7/17.
 */
import android.graphics.Bitmap;
import android.graphics.Rect;
import org.opencv.core.Point;
import android.content.Context;


public class InfoContainer {
    Rect nearBox;
    String word;
    Bitmap frame;
    float confidence;
    String data_path;
    String lang;
    Point lowest;
    Context mcontext;


    public InfoContainer(){

    }
}
