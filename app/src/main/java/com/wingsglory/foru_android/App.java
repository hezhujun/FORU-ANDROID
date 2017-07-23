package com.wingsglory.foru_android;

import android.app.Application;

import com.wingsglory.foru_android.model.Task;
import com.wingsglory.foru_android.model.User;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hezhujun on 2017/6/28.
 */

public class App extends Application {
//    public static final String BASE_URL = "http://118.89.51.45:8080/foru";
    public static final String BASE_URL = "http://192.168.244.86:8080";
    public static final String DEFAULT_IMAGE_URL = "https://ps.ssl.qhimg.com/t0123f47c7eae031cbb.jpg";

    private User user;
    private Map<Integer, Task> taskBuffer = new HashMap<>();

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Map<Integer, Task> getTaskBuffer() {
        return taskBuffer;
    }

    public void addTask(Task task){
        taskBuffer.put(task.getId(), task);
    }

    public void remove(Integer taskId) {
        taskBuffer.remove(taskId);
    }
}
