package com.wingsglory.foru_android.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wingsglory.foru_android.Globle;
import com.wingsglory.foru_android.R;
import com.wingsglory.foru_android.model.TaskDTO;
import com.wingsglory.foru_android.model.User;
import com.wingsglory.foru_android.task.DownloadImageAsyncTask;

import java.sql.Timestamp;

public class TaskDetailActivity extends AppCompatActivity {
    private static final String TAG = "TaskDetailActivity";

    private TaskDTO task;
    private User user;

    private ImageView userImageView;
    private TextView usernameView;
    private TextView taskTitleView;
    private TextView taskRewardView;
    private TextView taskRemainTimeView;
    private TextView taskStateView;
    private TextView taskContentView;
    private TextView taskTargetPositionView;
    private TextView addresseeNameView;
    private TextView addresseePhoneView;
    private TextView addresseeAddressView;
    private View addresseeView;

    public static Intent startActivity(Context context, TaskDTO taskDTO, User user) {
        Intent intent = new Intent(context, TaskDetailActivity.class);
        Globle.currentTask = taskDTO;
        intent.putExtra("user", user);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);
        try {
            Intent intent = getIntent();
            task = Globle.currentTask;
            user = (User) intent.getSerializableExtra("user");

            userImageView = (ImageView) findViewById(R.id.user_image);
            new DownloadImageAsyncTask(task.getImageUrl(), userImageView).execute();
            usernameView = (TextView) findViewById(R.id.user_name);
            usernameView.setText(task.getPublisher());
            taskTitleView = (TextView) findViewById(R.id.task_title);
            taskTitleView.setText(task.getTaskContent().getTitle());
            taskRewardView = (TextView) findViewById(R.id.task_reward);
            taskRewardView.setText("赏" + task.getTaskContent().getReward() + "元");
            taskRemainTimeView = (TextView) findViewById(R.id.task_remain_time);
            Timestamp timeout = task.getTaskContent().getTimeout();
            long day1 = timeout.getTime() / 1000 / 60 / 60 / 24;
            long day2 = System.currentTimeMillis() / 1000 / 60 / 60 / 24;
            long diff = day1 - day2;
            if (diff >= 0) {
                taskRemainTimeView.setText("还剩" + diff + "天");
            } else {
                taskRemainTimeView.setText("已过期");
            }
            taskStateView = (TextView) findViewById(R.id.task_state);
            taskStateView.setText(task.getTask().getState());
            taskContentView = (TextView) findViewById(R.id.task_content);
            taskContentView.setText(task.getTaskContent().getContent());
            taskTargetPositionView = (TextView) findViewById(R.id.task_target_position);
            taskTargetPositionView.setText(task.getTaskContent().getTargetPosition());
            addresseeNameView = (TextView) findViewById(R.id.addressee);
            addresseeNameView.setText(task.getAddressee().getName());
            addresseePhoneView = (TextView) findViewById(R.id.phone);
            addresseePhoneView.setText(task.getAddressee().getPhone());
            addresseeAddressView = (TextView) findViewById(R.id.address);
            addresseeAddressView.setText(task.getAddressee().getAddress() + " " + task.getAddressee().getAddressDetail());
            addresseeView = findViewById(R.id.task_addressee_view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
