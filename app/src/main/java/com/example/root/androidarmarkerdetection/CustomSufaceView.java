package com.example.root.androidarmarkerdetection;
/**
 * Created by root on 20/7/17.
 */
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.AttributeSet;

import org.opencv.android.JavaCameraView;

import java.util.List;
/**
* <h1>Helper class for proper camera handling</h1>
* This class gets the camera properties and provide
* usefull setting so as to work without considering 
* much about the internal paramters
* <p>
* <b>Note:</b> Alternation of any kind in this code
* will affect the performance of app
*
* @author  zalpha
* @version 1.0
* @since   2017-07-17
*/
public class CustomSufaceView extends JavaCameraView {

    private static final String TAG = "OpenCustomSufaceView";

 /**
   * This method initializes the camera view
   * with necessary context and attributes
   * @param context main activity context
   * @param attrs attributes from resouces
   */
    public CustomSufaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

 /**
   * This method is used to 
   * get the supported color effects. 
   * @param Nothing
   * @return color effect as String list
   */
    public List<String> getEffectList() {
        return mCamera.getParameters().getSupportedColorEffects();
    }

 /**
   * This method ris used to 
   * check the supportability of 
   * color effects. 
   * @param Nothing
   * @return support or not
   */
    public boolean isEffectSupported() {
        return (mCamera.getParameters().getColorEffect() != null);
    }

 /**
   * This method is used to 
   * gets the current color effect setting.. 
   * @param Nothing
   * @return color effect as String 
   */
    public String getEffect() {
        return mCamera.getParameters().getColorEffect();
    }

 /**
   * This method is used to 
   * sets the current color effect setting.. 
   * @param effect effect to be set
   * @return Nothing
   */
    public void setEffect(String effect) {
        Camera.Parameters params = mCamera.getParameters();
        params.setColorEffect(effect);
        mCamera.setParameters(params);
    }
    
/**
   * This method is used to 
   * get the camera paramters 
   * @param Nothing
   * @return needed camera paramters
   */
    public Camera.Parameters getParameters(){
    	Camera.Parameters params = mCamera.getParameters();
    	return params;
    }
    
/**
   * This method is used to 
   * set the camera paramters 
   * @param params camera paramters
   * @return Nothing
   */
    public void setParameters(Camera.Parameters params){
    	mCamera.setParameters(params);
    }

/**
   * This method is used to 
   * get the resolution list 
   * @param Nothing
   * @return resolutiong as String list
   */
    public List<Size> getResolutionList() {
        return mCamera.getParameters().getSupportedPreviewSizes();
    }

/**
   * This method is used to 
   * set the given resolution 
   * @param resoltion resolution to be set
   * @return Nothing
   */
    public void setResolution(Size resolution) {
        disconnectCamera();
        mMaxHeight = resolution.height;
        mMaxWidth = resolution.width;
        connectCamera(getWidth(), getHeight());
    }

}
