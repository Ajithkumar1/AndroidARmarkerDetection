package com.example.root.androidarmarkerdetection;

/**
 * Created by root on 20/7/17.
 */
import android.graphics.Bitmap;
import android.graphics.Rect;
import org.opencv.core.Point;
import android.content.Context;

/**
* <h1>Generic container of usefull resources</h1>
* This class is temporarily created to assist handling 
* different data resources across different 
* scopes. This class contains some important 
* paramters needed for the application
*
* @author  zalpha
* @version 1.0
* @since   2017-07-17
*/
public class InfoContainer {

    Rect nearBox;           //Nearest Bounding Box
    String word;            //Word from OCR
    Bitmap frame;           //current bitmap frame in execution
    float confidence;       // confidence(0-100) of the detected word
    String data_path;       //datapath for the Tessdata storage
    String lang;            //langauge to be used
    Point lowest;           //optimum point from contours
    Context mcontext;       //main application context


    public InfoContainer(){

    }
}
