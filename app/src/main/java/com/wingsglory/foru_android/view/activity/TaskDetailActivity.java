package com.wingsglory.foru_android.view.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wingsglory.foru_android.App;
import com.wingsglory.foru_android.R;
import com.wingsglory.foru_android.model.Addressee;
import com.wingsglory.foru_android.model.Result;
import com.wingsglory.foru_android.model.Task;
import com.wingsglory.foru_android.model.TaskContent;
import com.wingsglory.foru_android.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TaskDetailActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "TaskDetailActivity";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private App app;
    private User user;
    private Task task;

    private ImageView userImageView;
    private TextView usernameView;
    private TextView taskTitleView;
    private TextView taskRewardView;
    private TextView taskTimeoutView;
    private TextView taskStateView;
    private TextView taskContentView;
    private TextView taskTargetPositionView;
    private TextView addresseeNameView;
    private TextView addresseePhoneView;
    private TextView addresseeAddressView;
    private View addresseeView;
    private TextView deleteTaskButton;
    private TextView acceptTaskButton;
    private TextView completeTaskButton;
    private TextView confirmTaskButton;
    private ProgressDialog progressDialog;

    public static void actionStart(Context context, Task task) {
        Intent intent = new Intent(context, TaskDetailActivity.class);
        intent.putExtra("task", task);
        context.startActivity(intent);
    }

    private void initView() {
        userImageView = (ImageView) findViewById(R.id.user_image);
        usernameView = (TextView) findViewById(R.id.user_name);
        taskTitleView = (TextView) findViewById(R.id.task_title);
        taskRewardView = (TextView) findViewById(R.id.task_reward);
        taskTimeoutView = (TextView) findViewById(R.id.task_timeout);
        taskStateView = (TextView) findViewById(R.id.task_state);
        taskContentView = (TextView) findViewById(R.id.task_content);
        taskTargetPositionView = (TextView) findViewById(R.id.task_target_position);
        addresseeNameView = (TextView) findViewById(R.id.addressee);
        addresseePhoneView = (TextView) findViewById(R.id.phone);
        addresseeAddressView = (TextView) findViewById(R.id.address);
        addresseeView = findViewById(R.id.task_addressee_view);
        deleteTaskButton = (TextView) findViewById(R.id.delete_task);
        acceptTaskButton = (TextView) findViewById(R.id.accept_task);
        completeTaskButton = (TextView) findViewById(R.id.complete_task);
        confirmTaskButton = (TextView) findViewById(R.id.confirm_task);
        deleteTaskButton.setOnClickListener(this);
        acceptTaskButton.setOnClickListener(this);
        completeTaskButton.setOnClickListener(this);
        confirmTaskButton.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);
    }

    private void initData() {
        addresseeView.setVisibility(View.GONE);
        deleteTaskButton.setVisibility(View.GONE);
        acceptTaskButton.setVisibility(View.GONE);
        completeTaskButton.setVisibility(View.GONE);
        confirmTaskButton.setVisibility(View.GONE);

        TaskContent content = task.getContent();
        User publisher = task.getPublisher();
        Glide.with(this).load(publisher.getProtraitUrl()).into(userImageView);
        usernameView.setText(publisher.getUsername());
        taskTitleView.setText(content.getTitle());
        taskContentView.setText(content.getContent());
        taskTargetPositionView.setText(content.getTargetPosition());
        taskRewardView.setText("赏" + content.getReward() + "元");
        Date timeout = content.getTimeout();
        taskTimeoutView.setText(sdf.format(timeout) + "到期");
        Addressee addressee = content.getAddressee();
        addresseeNameView.setText(addressee.getName());
        addresseePhoneView.setText(addressee.getPhone());
        addresseeAddressView.setText(addressee.getAddress() + " " +
                addressee.getAddressDetail());
        // 判断任务是否过期
        long diff = timeout.getTime() - System.currentTimeMillis();

        if (Task.TASK_STATE_NEW.equals(task.getState())) {
            if (diff < 0) {
                task.setState(Task.TASK_STATE_OVERDUE);
                return; // 不在此判断处理
            }
            if (user.getId().equals(task.getPublisherId())) {
                // 是发布者在浏览信息
                addresseeView.setVisibility(View.VISIBLE);
                deleteTaskButton.setVisibility(View.VISIBLE);
            } else {
                // 其他人在浏览
                acceptTaskButton.setVisibility(View.VISIBLE);
            }
        }
        if (Task.TASK_STATE_WAIT_FOR_COMPLETE.equals(task.getState())) {
            if (diff < 0) {
                task.setState(Task.TASK_STATE_FAIL);
                // 不在此判断处理
            }
            addresseeView.setVisibility(View.VISIBLE);
            if (user.getId().equals(task.getRecipientId())) {
                completeTaskButton.setVisibility(View.VISIBLE);
            }
        }
        if (Task.TASK_STATE_WAIT_FOR_CONFIRM.equals(task.getState())) {
            addresseeView.setVisibility(View.VISIBLE);
            if (user.getId().equals(task.getPublisherId())) {
                confirmTaskButton.setVisibility(View.VISIBLE);
            }
        }
        if (Task.TASK_STATE_COMPLETE.equals(task.getState())) {
            addresseeView.setVisibility(View.VISIBLE);
        }
        if (Task.TASK_STATE_OVERDUE.equals(task.getState())) {
            if (user.getId().equals(task.getPublisherId())) {
                addresseeView.setVisibility(View.VISIBLE);
            }
        }
        if (Task.TASK_STATE_FAIL.equals(task.getState())) {
            addresseeView.setVisibility(View.VISIBLE);
        }
        taskStateView.setText(task.getState());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        app = (App) getApplication();
        user = app.getUser();

        Intent intent = getIntent();
        task = (Task) intent.getSerializableExtra("task");

        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    @Override
    public void onClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("注意");
        switch (v.getId()) {
            case R.id.delete_task:
                builder.setMessage("是否删除此任务？");
                builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new OperationAsyncTask("/task/remove", "删除成功",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                }).execute();
                        progressDialog.setMessage("请稍后...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                    }
                });
                break;
            case R.id.accept_task:
                builder.setMessage("是否接受此任务？");
                builder.setPositiveButton("接受", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new OperationAsyncTask("/task/accept", "接受任务成功",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        task.setRecipientId(user.getId());
                                        task.setState(Task.TASK_STATE_WAIT_FOR_COMPLETE);
                                        initData();
                                    }
                                }).execute();
                        progressDialog.setMessage("请稍后...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                    }
                });
                break;
            case R.id.complete_task:
                builder.setMessage("是否完成任务？");
                builder.setPositiveButton("完成", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new OperationAsyncTask("/task/complete", "完成",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        task.setState(Task.TASK_STATE_WAIT_FOR_CONFIRM);
                                        initData();
                                    }
                                }).execute();
                        progressDialog.setMessage("请稍后...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                    }
                });
                break;
            case R.id.confirm_task:
                builder.setMessage("是否确认完成任务？");
                builder.setPositiveButton("确认完成", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new OperationAsyncTask("/task/confirm", "完成",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        task.setState(Task.TASK_STATE_COMPLETE);
                                        initData();
                                    }
                                }).execute();
                        progressDialog.setMessage("请稍后...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                    }
                });
                break;
            default:
                break;
        }
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    class OperationAsyncTask extends AsyncTask<Void, Void, JSONObject> {

        private String url;
        private String successMsg;
        private DialogInterface.OnClickListener listener;

        public OperationAsyncTask(String url, String successMsg,
                                  DialogInterface.OnClickListener listener) {
            this.url = url;
            this.successMsg = successMsg;
            this.listener = listener;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();
            FormBody formBody = new FormBody.Builder()
                    .add("userId", String.valueOf(user.getId()))
                    .add("taskId", String.valueOf(task.getId()))
                    .build();
            Request request = new Request.Builder()
                    .post(formBody)
                    .url(App.BASE_URL + url)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    JSONObject jsonObject = new JSONObject(json);
                    return jsonObject;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject == null) {
                Toast.makeText(TaskDetailActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    String resultStr = jsonObject.getString("result");
                    Result result = objectMapper.readValue(resultStr, Result.class);
                    if (result.isSuccess()) {
                        new AlertDialog.Builder(TaskDetailActivity.this)
                                .setTitle("结果")
                                .setMessage(successMsg)
                                .setPositiveButton("确定", listener)
                                .show();
                    } else {
                        Toast.makeText(TaskDetailActivity.this, result.getErr(),
                                Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (JsonParseException e) {
                    e.printStackTrace();
                } catch (JsonMappingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            progressDialog.dismiss();
        }
    }
}
