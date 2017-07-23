package com.example.root.androidarmarkerdetection;

import java.io.File;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Log;

import java.lang.Object;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.googlecode.tesseract.android.TessBaseAPI.PageIteratorLevel;
import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.Pixa;
import com.googlecode.tesseract.android.ResultIterator;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.googlecode.tesseract.android.TessBaseAPI.PageIteratorLevel;
import com.googlecode.tesseract.android.TessBaseAPI.ProgressNotifier;
import com.googlecode.tesseract.android.TessBaseAPI.ProgressValues;

import org.opencv.core.Point;
import static android.R.attr.bitmap;
import static android.content.ContentValues.TAG;

public class TessOCR {
	private TessBaseAPI mTess;



	public TessOCR(String datapath, String language) {
		// TODO Auto-generated constructor stub
		mTess = new TessBaseAPI();
		////

		////
//		String datapath = Environment.getExternalStorageDirectory() + "/tesseract/";
//		String language = "eng";
//		File dir = new File(datapath + "tessdata/");
//		if (!dir.exists())
//			dir.mkdirs();
		mTess.init(datapath, language);
	}
	
	public void processOCR(Bitmap bitmap) {
		
		mTess.setImage(bitmap);
		String result = mTess.getUTF8Text();

		//return result;
    }

	public InfoContainer getNearBoundingBoxes(Bitmap bitmap, Point lowest) {
		InfoContainer info = new InfoContainer();
		ResultIterator iterator = mTess.getResultIterator();
		String lastUTF8Text;
		float lastConfidence;
		int[] lastBoundingBox;
		Rect lastBoundingRect;
		int count = 0;
		double subX = 0, subY = 0;
		double distance = Integer.MAX_VALUE;
		double tempDistance = Integer.MAX_VALUE;
		info.nearBox = new Rect(0,0,0,0);
		do {
			lastUTF8Text = iterator.getUTF8Text(PageIteratorLevel.RIL_WORD);
			lastConfidence = iterator.confidence(PageIteratorLevel.RIL_WORD);
			lastBoundingBox = iterator.getBoundingBox(PageIteratorLevel.RIL_WORD);
			lastBoundingRect = iterator.getBoundingRect(PageIteratorLevel.RIL_WORD);
			count++;
			subX = lowest.x  - lastBoundingRect.centerX();
			subY = lowest.y  - lastBoundingRect.centerY();
			subX = Math.abs(subX);
			subY = Math.abs(subY);
			tempDistance = Math.sqrt((subX*subX)+(subY*subY));
			if(distance < tempDistance){
				distance = tempDistance;
				info.nearBox = lastBoundingRect;
				info.word = lastUTF8Text;
				info.confidence = lastConfidence;
			}
			//Log.v(TAG, "Boundingbox" + lastBoundingRect.top +" "+lastBoundingRect.bottom +" "+ lastBoundingRect.right +" "+ lastBoundingRect.left +  " = " + lastUTF8Text + " " + lastConfidence);
		} while (iterator.next(PageIteratorLevel.RIL_WORD));
		Log.v(TAG, "Boundingbox" + info.nearBox.top +" "+info.nearBox.bottom +" "+ info.nearBox.right +" "+ info.nearBox.left +  " = " + lastUTF8Text + " " + lastConfidence);
		iterator.delete();
		return info;
	}
	
	public void onDestroy() {
		if (mTess != null)
			mTess.end();
	}
	
}
