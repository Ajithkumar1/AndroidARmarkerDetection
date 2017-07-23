package com.example.root.androidarmarkerdetection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.android.Utils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.root.androidarmarkerdetection.imageProcessing.ColorBlobDetector;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.tesseract.android.ResultIterator;
import com.googlecode.tesseract.android.TessBaseAPI;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.SumPathEffect;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.core.Size;

import static android.R.attr.bitmap;

public class MainActivity extends Activity implements OnTouchListener, CvCameraViewListener2 {

    static {
        System.loadLibrary("opencv_java3");
    }
    private static final String    TAG                 = "   ";
    public static final int        JAVA_DETECTOR       = 0;
    public static final int        NATIVE_DETECTOR     = 1;

    private Mat                    mRgba;
    private Mat                    mGray;
    private Mat                    mRgbaT;
    private Mat                    mRgbaF;
    private Mat 				   mIntermediateMat;

    private int                    mDetectorType       = JAVA_DETECTOR;

    private CustomSufaceView   mOpenCvCameraView;
    private List<Size> mResolutionList;

    private SeekBar minTresholdSeekbar = null;
    private SeekBar maxTresholdSeekbar = null;
    private TextView minTresholdSeekbarText = null;
//    private TextView numberOfFingersText = null;

    double iThreshold = 0;

    private Scalar               	mBlobColorHsv;
    private Scalar               	mBlobColorRgba;
    private ColorBlobDetector    	mDetector;
    private Mat                  	mSpectrum;
    private boolean				mIsColorSelected = false;

    private Size                 	SPECTRUM_SIZE;
    private Scalar               	CONTOUR_COLOR;
    private Scalar               	CONTOUR_COLOR_WHITE;
    static String temp_string = null;

    int taskStatus = 0;

    final Handler mHandler = new Handler();
    int numberOfFingers = 0;
   // Bitmap frame;
   // TessOCR ocr;
    InfoContainer wordDetailes;

    private ProgressDialog mProgressDialog;


    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory().toString() + "/Tesseract/";

    public static final String lang = "eng";
    TextView resultText;



//    final Runnable mUpdateFingerCountResults = new Runnable() {
//        public void run() {
//            updateNumberOfFingers();
//        }
//    };

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(MainActivity.this);
                    // 640x480
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public MainActivity() {
        wordDetailes = new InfoContainer();
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");

        super.onCreate(savedInstanceState);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.main_surface_view);
        if (!OpenCVLoader.initDebug()) {
            Log.e("Test","man");
        }else{
        }

        mOpenCvCameraView = (CustomSufaceView) findViewById(R.id.main_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);

        minTresholdSeekbarText = (TextView) findViewById(R.id.textView3);


//        numberOfFingersText = (TextView) findViewById(R.id.numberOfFingers);

        minTresholdSeekbar = (SeekBar)findViewById(R.id.seekBar1);
        minTresholdSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            int progressChanged = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
                progressChanged = progress;
                minTresholdSeekbarText.setText(String.valueOf(progressChanged));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                minTresholdSeekbarText.setText(String.valueOf(progressChanged));
            }
        });
        minTresholdSeekbar.setProgress(8700);


        String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };

        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
                    return;
                } else {
                    Log.v(TAG, "Created directory " + path + " on sdcard");
                }
            }

        }

        if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
            try {

                AssetManager assetManager = getAssets();
                InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
                //GZIPInputStream gin = new GZIPInputStream(in);
                OutputStream out = new FileOutputStream(DATA_PATH
                        + "tessdata/" + lang + ".traineddata");

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                //while ((lenf = gin.read(buff)) > 0) {
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                //gin.close();
                out.close();

                Log.v(TAG, "Copied " + lang + " traineddata");
            } catch (IOException e) {
                Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
            }
        }

        resultText = (TextView) findViewById(R.id.result_text);
        resultText.setVisibility(View.INVISIBLE);
        wordDetailes.mcontext = getApplicationContext();
        wordDetailes.data_path = DATA_PATH;
        wordDetailes.lang = lang;
        //ocr = new TessOCR(DATA_PATH,lang);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null){
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
        mRgbaT = new Mat();
        mRgbaF = new Mat();
        mIntermediateMat = new Mat();

        /*
        mResolutionList = mOpenCvCameraView.getResolutionList();
        ListIterator<Size> resolutionItr = mResolutionList.listIterator();
        while(resolutionItr.hasNext()) {
            Size element = resolutionItr.next();
            Log.i(TAG, "Resolution Option ["+Integer.valueOf(element.width).toString() + "x" + Integer.valueOf(element.height).toString()+"]");
        }
        Size resolution = mResolutionList.get(7);
        mOpenCvCameraView.setResolution(resolution);
        resolution = mOpenCvCameraView.getResolution();
        String caption = "Resolution "+ Integer.valueOf(resolution.width).toString() + "x" + Integer.valueOf(resolution.height).toString();
        Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
        */
//        Camera.Size resolution = mOpenCvCameraView.getResolution();
//        String caption = "Resolution "+ Integer.valueOf(resolution.width).toString() + "x" + Integer.valueOf(resolution.height).toString();
//        Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
//
//        Camera.Parameters cParams = mOpenCvCameraView.getParameters();
//        cParams.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
//        mOpenCvCameraView.setParameters(cParams);
//        Toast.makeText(this, "Focus mode : "+cParams.getFocusMode(), Toast.LENGTH_SHORT).show();

        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mRgbaT = new Mat(height, width, CvType.CV_8UC4);
        mRgbaF = new Mat(height, width, CvType.CV_8UC4);
        mDetector = new ColorBlobDetector();
        mSpectrum = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(255,0,0,255);
        CONTOUR_COLOR_WHITE = new Scalar(255,255,255,255);

    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    public boolean onTouch(View v, MotionEvent event) {
        int cols = mRgba.cols();
        int rows = mRgba.rows();

        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

        int x = (int)event.getX() - xOffset;
        int y = (int)event.getY() - yOffset;

        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        Rect touchedRect = new Rect();

        touchedRect.x = (x>5) ? x-5 : 0;
        touchedRect.y = (y>5) ? y-5 : 0;

        touchedRect.width = (x+5 < cols) ? x + 5 - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y+5 < rows) ? y + 5 - touchedRect.y : rows - touchedRect.y;

        Mat touchedRegionRgba = mRgba.submat(touchedRect);

        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        // Calculate average color of touched region
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width*touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;

        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);

        Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
                ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");

        mDetector.setHsvColor(mBlobColorHsv);

        Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

        mIsColorSelected = true;

        touchedRegionRgba.release();
        touchedRegionHsv.release();

        return false; // don't need subsequent touch events
    }

    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
/////
        //mRgba = inputFrame.rgba();
        // Rotate mRgba 90 degrees
        //Core.transpose(mRgba, mRgbaT);
        //Imgproc.resize(mRgba, mRgbaF, mRgbaF.size(), 0,0, 0);
        //Core.flip(mRgbaF, mRgba, 1 );

        wordDetailes.frame = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mRgba,wordDetailes.frame);
       // ocr.processOCR(frame);
        ////
        iThreshold = minTresholdSeekbar.getProgress();

        //Imgproc.blur(mRgba, mRgba, new Size(5,5));
        Imgproc.GaussianBlur(mRgba, mRgba, new org.opencv.core.Size(3, 3), 1, 1);
        //Imgproc.medianBlur(mRgba, mRgba, 3);

        if (!mIsColorSelected) return mRgba;

        List<MatOfPoint> contours = mDetector.getContours();
        mDetector.process(mRgba);

        Log.d(TAG, "Contours count: " + contours.size());

        if (contours.size() <= 0) {
            return mRgba;
        }

        RotatedRect rect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(0)	.toArray()));

        double boundWidth = rect.size.width;
        double boundHeight = rect.size.height;
        int boundPos = 0;

        for (int i = 1; i < contours.size(); i++) {
            rect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(i).toArray()));
            if (rect.size.width * rect.size.height > boundWidth * boundHeight) {
                boundWidth = rect.size.width;
                boundHeight = rect.size.height;
                boundPos = i;
            }
        }

        Rect boundRect = Imgproc.boundingRect(new MatOfPoint(contours.get(boundPos).toArray()));

        Imgproc.rectangle( mRgba, boundRect.tl(), boundRect.br(), CONTOUR_COLOR_WHITE, 2, 8, 0 );


        Log.d(TAG,
                " Row start ["+
                        (int) boundRect.tl().y + "] row end ["+
                        (int) boundRect.br().y+"] Col start ["+
                        (int) boundRect.tl().x+"] Col end ["+
                        (int) boundRect.br().x+"]");

        int rectHeightThresh = 0;
        double a = boundRect.br().y - boundRect.tl().y;
        a = a * 0.7;
        a = boundRect.tl().y + a;

        Log.d(TAG,
                " A ["+a+"] br y - tl y = ["+(boundRect.br().y - boundRect.tl().y)+"]");

        //Core.rectangle( mRgba, boundRect.tl(), boundRect.br(), CONTOUR_COLOR, 2, 8, 0 );
        Imgproc.rectangle( mRgba, boundRect.tl(), new Point(boundRect.br().x, a), CONTOUR_COLOR, 2, 8, 0 );

        MatOfPoint2f pointMat = new MatOfPoint2f();
        Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(boundPos).toArray()), pointMat, 3, true);
        contours.set(boundPos, new MatOfPoint(pointMat.toArray()));

        MatOfInt hull = new MatOfInt();
        MatOfInt4 convexDefect = new MatOfInt4();
        Imgproc.convexHull(new MatOfPoint(contours.get(boundPos).toArray()), hull);

        if(hull.toArray().length < 3) return mRgba;

        Imgproc.convexityDefects(new MatOfPoint(contours.get(boundPos)	.toArray()), hull, convexDefect);

        List<MatOfPoint> hullPoints = new LinkedList<MatOfPoint>();
        List<Point> listPo = new LinkedList<Point>();
        for (int j = 0; j < hull.toList().size(); j++) {
            listPo.add(contours.get(boundPos).toList().get(hull.toList().get(j)));
        }

        MatOfPoint e = new MatOfPoint();
        e.fromList(listPo);
        hullPoints.add(e);

        List<MatOfPoint> defectPoints = new LinkedList<MatOfPoint>();
        List<Point> listPoDefect = new LinkedList<Point>();
        for (int j = 0; j < convexDefect.toList().size(); j = j+4) {
            Point farPoint = contours.get(boundPos).toList().get(convexDefect.toList().get(j+2));
            Integer depth = convexDefect.toList().get(j+3);
            if(depth > iThreshold && farPoint.y < a){
                listPoDefect.add(contours.get(boundPos).toList().get(convexDefect.toList().get(j+2)));
            }
            Log.d(TAG, "defects ["+j+"] " + convexDefect.toList().get(j+3));
        }

        MatOfPoint e2 = new MatOfPoint();
        e2.fromList(listPo);
        defectPoints.add(e2);

        Log.d(TAG, "hull: " + hull.toList());
        Log.d(TAG, "defects: " + convexDefect.toList());

        Imgproc.drawContours(mRgba, hullPoints, -1, CONTOUR_COLOR, 3);
////////
        wordDetailes.lowest = new Point(0,0);

        org.opencv.core.Point[] points = hullPoints.get(0).toArray();
        wordDetailes.lowest.x = points[0].x;
        wordDetailes.lowest.y = points[0].y;
        for (int i = 0; i < points.length; i++)
        {
            if(wordDetailes.lowest.x > points[i].x)
            {
                wordDetailes.lowest.x = points[i].x;
                wordDetailes.lowest.y = points[i].y;
            }
        }

        if(taskStatus == 0) {
            new OcrInitAsyncTask().execute(wordDetailes);
        }
//        new Thread(new Runnable() {
//            public void run() {
//
//                ocr.processOCR(wordDetailes.frame);
//                //ocr.getBoundingBoxes(bitmap);
//                wordDetailes = ocr.getNearBoundingBoxes(wordDetailes.frame,wordDetailes.lowest);
//
//
//
//                runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        // TODO Auto-generated method stub
//                        if (wordDetailes != null && !wordDetailes.word.equals("")) {
//                            // Imgproc.rectangle(mRgba, wordDetailes.nearBox, new Scalar(255,0,0,255),1,8,0);
//                            Imgproc.putText(mRgba, "Word", new Point(wordDetailes.nearBox.centerX(),wordDetailes.nearBox.centerY()), 3, 2, new Scalar(255, 0, 255, 255), 3);
//                            //////
//                            // Imgproc.putText(mRgba, "Ajith Kumar", lowest, 3, 2, new Scalar(255, 0, 0, 255), 3);
//                            Imgproc.putText(mRgba, wordDetailes.word, wordDetailes.lowest, 3, 2, new Scalar(255, 0, 0, 255), 3);
//                        }
//
//                        mProgressDialog.dismiss();
//                    }
//
//                });
//
//            };
//        }).start();
        /////


//        InfoContainer wordDetailes = ocr.getNearBoundingBoxes(frame,lowest);
//
//       // Imgproc.rectangle(mRgba, wordDetailes.nearBox, new Scalar(255,0,0,255),1,8,0);
//        Imgproc.putText(mRgba, "Word", new Point(wordDetailes.nearBox.centerX(),wordDetailes.nearBox.centerY()), 3, 2, new Scalar(255, 0, 255, 255), 3);
//                //////
//       // Imgproc.putText(mRgba, "Ajith Kumar", lowest, 3, 2, new Scalar(255, 0, 0, 255), 3);
//        Imgproc.putText(mRgba, wordDetailes.word, lowest, 3, 2, new Scalar(255, 0, 0, 255), 3);
        //////
        int defectsTotal = (int) convexDefect.total();
        Log.d(TAG, "Defect total " + defectsTotal);

//        this.numberOfFingers = listPoDefect.size();
//        if(this.numberOfFingers > 5) this.numberOfFingers = 5;
//
//        mHandler.post(mUpdateFingerCountResults);

        for(Point p : listPoDefect){
            Imgproc.circle(mRgba, p, 6, new Scalar(255,0,255));
        }

        return mRgba;
    }

//    public void updateNumberOfFingers(){
//        numberOfFingersText.setText(String.valueOf(this.numberOfFingers));
//    }
final class OcrInitAsyncTask extends AsyncTask<InfoContainer, String, InfoContainer> {
    //private static final String TAG = com.example.root.androidarmarkerdetection.OcrInitAsyncTask.class.getSimpleName();

    private MainActivity activity;
    private TessBaseAPI baseApi;
    private Activity context;
    String result_word = null;
    String url1 = "https://api.pearson.com/v2/dictionaries/ldoce5/entries?headword=";
    String url2 = "&apikey=vMw1ZcpxUnRJ7KarxYuANAytmXufIexa";

    String url = null;

    public static final String lang = "eng";
    int status = 0;
    JSONArray result=null;
    JSONObject resultobj = null;
    JSONArray sensearr = null;
    JSONObject defobj=null;
    JSONArray def=null;
//    MainActivity main = new MainActivity();

    OcrInitAsyncTask() {
        baseApi = new TessBaseAPI();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //   activity.setButtonVisibility(false);
        taskStatus = 1;
    }

    @Override
    protected InfoContainer doInBackground(InfoContainer... info){

        ////

        ////
//		String datapath = Environment.getExternalStorageDirectory() + "/tesseract/";
//		String language = "eng";
//		File dir = new File(datapath + "tessdata/");
//		if (!dir.exists())
//			dir.mkdirs();
        baseApi.init(info[0].data_path, lang);
        baseApi.setImage(ReadFile.readBitmap(info[0].frame));

        String textResult = baseApi.getUTF8Text();
        Log.d(TAG, textResult);
        // String result = mTess.getUT    F8Text();
        ResultIterator iterator = baseApi.getResultIterator();
        String lastUTF8Text;
        float lastConfidence;
        int[] lastBoundingBox;
        android.graphics.Rect lastBoundingRect;
        int count = 0;
        double subX = 0, subY = 0;
        double distance = Integer.MAX_VALUE;
        double tempDistance = Integer.MAX_VALUE;
        info[0].nearBox = new android.graphics.Rect(0,0,0,0);
        do {
            lastUTF8Text = iterator.getUTF8Text(TessBaseAPI.PageIteratorLevel.RIL_WORD);
            lastConfidence = iterator.confidence(TessBaseAPI.PageIteratorLevel.RIL_WORD);
            lastBoundingBox = iterator.getBoundingBox(TessBaseAPI.PageIteratorLevel.RIL_WORD);
            lastBoundingRect = iterator.getBoundingRect(TessBaseAPI.PageIteratorLevel.RIL_WORD);
            count++;
            subX = info[0].lowest.x  - lastBoundingRect.centerX();
            subY = info[0].lowest.y  - lastBoundingRect.centerY();
            subX = Math.abs(subX);
            subY = Math.abs(subY);
            tempDistance = Math.sqrt((subX*subX)+(subY*subY));
            Log.v(TAG, "Distance" + distance +" "+tempDistance);

            if(distance > tempDistance){
                Log.v(TAG, "Word" + lastUTF8Text);
                Log.v(TAG, "confidence" + lastConfidence);
                Log.v(TAG, "nearbox" + lastBoundingRect);
                //Log.v(TAG, "Distance" + distance +" "+tempDistance);

                distance = tempDistance;
                info[0].nearBox = lastBoundingRect;
                info[0].word = lastUTF8Text;
                info[0].confidence = lastConfidence;
            }
            //Log.v(TAG, "Boundingbox" + lastBoundingRect.top +" "+lastBoundingRect.bottom +" "+ lastBoundingRect.right +" "+ lastBoundingRect.left +  " = " + lastUTF8Text + " " + lastConfidence);
        } while (iterator.next(TessBaseAPI.PageIteratorLevel.RIL_WORD));
        Log.v(TAG, "Boundingbox" + info[0].nearBox.top +" "+info[0].nearBox.bottom +" "+ info[0].nearBox.right +" "+ info[0].nearBox.left +  " = " + info[0].word + " " + info[0].confidence);
        iterator.delete();

        String url = url1+info[0].word+url2;
        Log.d(TAG,"URL"+url);

        JsonObjectRequest jsRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {   // jsRequest đọc json và lưu vào response
                int count_value = 0;

                Log.d("respone - "," output ="+response.toString());
                try {
                    count_value = response.getInt("count");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(count_value == 0){
                    status = -1;
                    return;
                }
                try {
                    result = response.getJSONArray("results");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    resultobj = result.getJSONObject(0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                try {
                    sensearr = resultobj.getJSONArray("senses");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    defobj = sensearr.getJSONObject(0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    def = defobj.getJSONArray("definition");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    temp_string = def.getString(0);
                    Log.d("response",temp_string);
                    Log.d("response ","definition = "+def.getString(0));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG,"Error: " + error.toString());
            }
        });
        RequestQueue rQueue = Volley.newRequestQueue(info[0].mcontext);
        rQueue.add(jsRequest);

//        result_word = info[0].word;
//        return result_word;
        if(status == -1){
            info[0].word += "No definitions found";
            status = 0;
            return info[0];
        }
        //temp_string +="= ";
        info[0].word += " = "+temp_string;
        Log.d(TAG,"final word:"+info[0].word);
        return info[0];
    }


    @Override
    protected void onProgressUpdate(String... message){
        super.onProgressUpdate();

    }

    @Override
    protected void onPostExecute(InfoContainer result) {
        super.onPostExecute(result);
        Log.v(TAG, "RESULT" + result.word);

        resultText.setVisibility(View.VISIBLE);
        resultText.setText(result.word);
        resultText.postDelayed(new Runnable() {
            public void run() {
                resultText.setVisibility(View.VISIBLE);
            }
        }, 3000);
        resultText.setVisibility(View.INVISIBLE);

       // Toast.makeText(MainActivity.this, result.word, Toast.LENGTH_SHORT).show();
        taskStatus = 0;
    }
    public void onDestroy() {
        if (baseApi != null)
            baseApi.end();
    }
}
}