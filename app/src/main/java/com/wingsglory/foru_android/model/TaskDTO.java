package com.wingsglory.foru_android.model;

import android.graphics.Bitmap;

/**
 * Created by hezhujun on 2017/6/24.
 */
public class TaskDTO {
    private Task task;
    private TaskContent taskContent;
    private Addressee addressee;
    private String publisher;
    private String imageUrl;
    private ImageData image;

    public TaskDTO() {
    }

    public TaskDTO(Task task, TaskContent taskContent, Addressee addressee) {
        this.task = task;
        this.taskContent = taskContent;
        this.addressee = addressee;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TaskDTO taskDTO = (TaskDTO) o;

        return task != null ? task.equals(taskDTO.task) : taskDTO.task == null;

    }

    @Override
    public int hashCode() {
        return task != null ? task.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "TaskDTO{" +
                "task=" + task +
                ", taskContent=" + taskContent +
                ", addressee=" + addressee +
                '}';
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public TaskContent getTaskContent() {
        return taskContent;
    }

    public void setTaskContent(TaskContent taskContent) {
        this.taskContent = taskContent;
    }

    public Addressee getAddressee() {
        return addressee;
    }

    public void setAddressee(Addressee addressee) {
        this.addressee = addressee;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public ImageData getImage() {
        return image;
    }

    public void setImage(ImageData image) {
        this.image = image;
    }
}
