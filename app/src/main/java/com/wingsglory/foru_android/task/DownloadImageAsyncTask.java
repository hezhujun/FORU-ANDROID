package com.wingsglory.foru_android.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.wingsglory.foru_android.Globle;
import com.wingsglory.foru_android.model.ImageData;
import com.wingsglory.foru_android.model.TaskDTO;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by hezhujun on 2017/7/2.
 */

public class DownloadImageAsyncTask extends AsyncTask<Void, Void, Void> {

    private TaskDTO taskDTO;

    public DownloadImageAsyncTask(TaskDTO taskDTO) {
        this.taskDTO = taskDTO;
    }

    @Override
    protected Void doInBackground(Void... params) {
        URL picUrl = null;
        ImageData imageData = new ImageData();
        imageData.setUrl(taskDTO.getImageUrl());
        // 判断是否已经下载
        for (ImageData img :
                Globle.imageBuffer) {
            if (img.equals(imageData)) {
                taskDTO.setImage(img);
                return null;
            }
        }
        // 没有下载，直接下载
        try {
            System.out.println("download image: " + imageData.getUrl());
            picUrl = new URL(imageData.getUrl());
            Bitmap pngBM = BitmapFactory.decodeStream(picUrl.openStream());
            imageData.setBitmap(pngBM);
            Globle.imageBuffer.add(imageData);
            taskDTO.setImage(imageData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
