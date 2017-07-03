package com.wingsglory.foru_android.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.constraint.solver.Goal;
import android.widget.ImageView;

import com.wingsglory.foru_android.Globle;
import com.wingsglory.foru_android.R;
import com.wingsglory.foru_android.model.ImageData;
import com.wingsglory.foru_android.model.TaskDTO;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by hezhujun on 2017/7/2.
 */

public class DownloadImageAsyncTask extends AsyncTask<Void, Void, Bitmap> {

    private String url;
    private ImageView imageView;

    public DownloadImageAsyncTask(String url, ImageView imageView) {
        this.url = url;
        this.imageView = imageView;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(R.drawable.person01);
        }
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        if (url != null) {
            Bitmap bitmap = Globle.imageBuffer.get(url);
            if (bitmap != null) {
                return bitmap;
            } else {
                try {
                    System.out.println("download image: " + url);
                    URL picUrl = new URL(url);
                    Bitmap pngBM = BitmapFactory.decodeStream(picUrl.openStream());
                    Globle.imageBuffer.put(url, pngBM);
                    return pngBM;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
