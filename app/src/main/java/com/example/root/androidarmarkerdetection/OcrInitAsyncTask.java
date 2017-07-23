//package com.example.root.androidarmarkerdetection;
//
///**
// * Created by root on 22/7/17.
// */
//
//
//import java.io.BufferedInputStream;
//import java.io.BufferedOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.io.RandomAccessFile;
//import java.net.HttpURLConnection;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.util.zip.GZIPInputStream;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipInputStream;
//
//import com.android.volley.Request;
//import com.android.volley.RequestQueue;
//import com.android.volley.Response;
//import com.android.volley.VolleyError;
//import com.android.volley.toolbox.JsonObjectRequest;
//import com.android.volley.toolbox.Volley;
//import com.googlecode.tesseract.android.ResultIterator;
//import com.googlecode.tesseract.android.TessBaseAPI;
//import com.googlecode.tesseract.android.PageIterator;
//import com.googlecode.tesseract.android.TessBaseAPI.PageIteratorLevel;
//
//import android.app.Activity;
//import android.app.ProgressDialog;
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.Rect;
//import android.os.AsyncTask;
//import android.os.Handler;
//import android.util.Log;
//import android.view.View;
//import android.view.animation.TranslateAnimation;
//import android.widget.TextView;
//import android.widget.Toast;
//import com.googlecode.leptonica.android.ReadFile;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.opencv.core.Point;
//
//import static android.R.attr.bitmap;
//import static android.R.id.message;
//import static android.content.ContentValues.TAG;
//
//final class OcrInitAsyncTask extends AsyncTask<InfoContainer, String, InfoContainer> {
//    private static final String TAG = OcrInitAsyncTask.class.getSimpleName();
//
//    private MainActivity activity;
//    private TessBaseAPI baseApi;
//    private Activity context;
//    String result_word = null;
//    String url1 = "https://api.pearson.com/v2/dictionaries/ldoce5/entries?headword=";
//    String url2 = "&apikey=vMw1ZcpxUnRJ7KarxYuANAytmXufIexa";
//
//    String url = null;
//    static String temp_string = null;
//    public static final String lang = "eng";
//    int status = 0;
//    JSONArray result=null;
//    JSONObject resultobj = null;
//    JSONArray sensearr = null;
//    JSONObject defobj=null;
//    JSONArray def=null;
//    MainActivity main = new MainActivity();
//
//    OcrInitAsyncTask() {
//        baseApi = new TessBaseAPI();
//    }
//
//    @Override
//    protected void onPreExecute() {
//        super.onPreExecute();
//     //   activity.setButtonVisibility(false);
//
//    }
//
//    @Override
//    protected InfoContainer doInBackground(InfoContainer... info){
//
//        ////
//
//        ////
////		String datapath = Environment.getExternalStorageDirectory() + "/tesseract/";
////		String language = "eng";
////		File dir = new File(datapath + "tessdata/");
////		if (!dir.exists())
////			dir.mkdirs();
//        baseApi.init(info[0].data_path, lang);
//        baseApi.setImage(ReadFile.readBitmap(info[0].frame));
//
//        String textResult = baseApi.getUTF8Text();
//        Log.d(TAG, textResult);
//       // String result = mTess.getUT    F8Text();
//        ResultIterator iterator = baseApi.getResultIterator();
//        String lastUTF8Text;
//        float lastConfidence;
//        int[] lastBoundingBox;
//        Rect lastBoundingRect;
//        int count = 0;
//        double subX = 0, subY = 0;
//        double distance = Integer.MAX_VALUE;
//        double tempDistance = Integer.MAX_VALUE;
//        info[0].nearBox = new Rect(0,0,0,0);
//        do {
//            lastUTF8Text = iterator.getUTF8Text(PageIteratorLevel.RIL_WORD);
//            lastConfidence = iterator.confidence(PageIteratorLevel.RIL_WORD);
//            lastBoundingBox = iterator.getBoundingBox(PageIteratorLevel.RIL_WORD);
//            lastBoundingRect = iterator.getBoundingRect(PageIteratorLevel.RIL_WORD);
//            count++;
//            subX = info[0].lowest.x  - lastBoundingRect.centerX();
//            subY = info[0].lowest.y  - lastBoundingRect.centerY();
//            subX = Math.abs(subX);
//            subY = Math.abs(subY);
//            tempDistance = Math.sqrt((subX*subX)+(subY*subY));
//            Log.v(TAG, "Distance" + distance +" "+tempDistance);
//
//            if(distance > tempDistance){
//                Log.v(TAG, "Word" + lastUTF8Text);
//                Log.v(TAG, "confidence" + lastConfidence);
//                Log.v(TAG, "nearbox" + lastBoundingRect);
//                //Log.v(TAG, "Distance" + distance +" "+tempDistance);
//
//                distance = tempDistance;
//                info[0].nearBox = lastBoundingRect;
//                info[0].word = lastUTF8Text;
//                info[0].confidence = lastConfidence;
//            }
//            //Log.v(TAG, "Boundingbox" + lastBoundingRect.top +" "+lastBoundingRect.bottom +" "+ lastBoundingRect.right +" "+ lastBoundingRect.left +  " = " + lastUTF8Text + " " + lastConfidence);
//        } while (iterator.next(PageIteratorLevel.RIL_WORD));
//        Log.v(TAG, "Boundingbox" + info[0].nearBox.top +" "+info[0].nearBox.bottom +" "+ info[0].nearBox.right +" "+ info[0].nearBox.left +  " = " + info[0].word + " " + info[0].confidence);
//        iterator.delete();
//
//        String url = url1+info[0].word+url2;
//        Log.d(TAG,"URL"+url);
//
//        JsonObjectRequest jsRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject response) {   // jsRequest đọc json và lưu vào response
//                int count_value = 0;
//
//                Log.d("respone - "," output ="+response.toString());
//                try {
//                    count_value = response.getInt("count");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                if(count_value == 0){
//                    status = -1;
//                    return;
//                }
//                try {
//                    result = response.getJSONArray("results");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                try {
//                    resultobj = result.getJSONObject(0);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//
//                try {
//                    sensearr = resultobj.getJSONArray("senses");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//                try {
//                    defobj = sensearr.getJSONObject(0);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//                try {
//                    def = defobj.getJSONArray("definition");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                try {
//                    temp_string = def.getString(0);
//                    Log.d("response",temp_string);
//                    Log.d("response ","definition = "+def.getString(0));
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.d(TAG,"Error: " + error.toString());
//            }
//        });
//        RequestQueue rQueue = Volley.newRequestQueue(info[0].mcontext);
//        rQueue.add(jsRequest);
//
////        result_word = info[0].word;
////        return result_word;
//        if(status == -1){
//            info[0].word += "No definitions found";
//            status = 0;
//            return info[0];
//        }
//        //temp_string +="= ";
//        info[0].word += " = "+temp_string;
//        Log.d(TAG,"final word:"+info[0].word);
//        return info[0];
//    }
//
//
//    @Override
//    protected void onProgressUpdate(String... message){
//        super.onProgressUpdate();
//
//    }
//
//    @Override
//    protected void onPostExecute(InfoContainer result) {
//        super.onPostExecute(result);
//        Log.v(TAG, "RESULT" + result.word);
//
//        main.resultText.setVisibility(View.VISIBLE);
//        main.resultText.setText(result.word);
//        main.resultText.postDelayed(new Runnable() {
//            public void run() {
//                main.resultText.setVisibility(View.INVISIBLE);
//            }
//        }, 3000);
//
//        Toast.makeText(result.mcontext, result.word, Toast.LENGTH_SHORT).show();
//
//    }
//    public void onDestroy() {
//        if (baseApi != null)
//            baseApi.end();
//    }
//}