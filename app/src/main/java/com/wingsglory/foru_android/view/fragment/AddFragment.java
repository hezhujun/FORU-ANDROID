package com.wingsglory.foru_android.view.fragment;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wingsglory.foru_android.App;
import com.wingsglory.foru_android.R;
import com.wingsglory.foru_android.model.Addressee;
import com.wingsglory.foru_android.model.Position;
import com.wingsglory.foru_android.model.Result;
import com.wingsglory.foru_android.model.Task;
import com.wingsglory.foru_android.model.TaskContent;
import com.wingsglory.foru_android.model.TaskDTO;
import com.wingsglory.foru_android.model.User;
import com.wingsglory.foru_android.util.HttpUtil;
import com.wingsglory.foru_android.view.activity.AddresseeListActivity;
import com.wingsglory.foru_android.view.activity.MainActivity;
import com.wingsglory.foru_android.view.activity.PositionActivity;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;


public class AddFragment extends Fragment implements View.OnClickListener, DatePickerDialog.OnDateSetListener {
    private static final String TAG = "AddFragment";

    private TaskDTO taskDTO;
    private Task task = new Task();
    private TaskContent taskContent = new TaskContent();
    private Addressee addressee;
    private Position addressPosition;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private EditText taskTitleView;
    private EditText taskContentView;
    private EditText taskRewardView;
    private TextView addressView;
    private TextView addresseeView;
    private TextView timeoutView;
    private View taskAddressView;
    private View taskAddresseeView;
    private View taskTimeoutView;
    private View publishButton;

    private User user;
    private Map<Integer, TaskDTO> taskPublished;
    private MainActivity father;

    public AddFragment() {
    }

    public static AddFragment newInstance(User user, Map<Integer, TaskDTO> taskPublished, MainActivity father) {
        AddFragment fragment = new AddFragment();
        fragment.user = user;
        fragment.taskPublished = taskPublished;
        fragment.father = father;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);
        taskTitleView = (EditText) view.findViewById(R.id.task_title);
        taskContentView = (EditText) view.findViewById(R.id.task_content);
        taskRewardView = (EditText) view.findViewById(R.id.task_reward);
        addressView = (TextView) view.findViewById(R.id.task_address);
        addresseeView = (TextView) view.findViewById(R.id.task_addressee);
        timeoutView = (TextView) view.findViewById(R.id.task_timeout);
        taskAddressView = view.findViewById(R.id.task_address_view);
        taskAddressView.setOnClickListener(this);
        taskAddresseeView = view.findViewById(R.id.task_addressee_view);
        taskAddresseeView.setOnClickListener(this);
        taskTimeoutView = view.findViewById(R.id.task_timeout_view);
        taskTimeoutView.setOnClickListener(this);
        publishButton = view.findViewById(R.id.publish_button);
        publishButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.task_address_view:
                intent = PositionActivity.startActivity(getActivity(), false, 0, 0);
                startActivityForResult(intent, PositionActivity.RETURN_POSITION_SUCCESS);
                break;
            case R.id.task_addressee_view:
                intent = AddresseeListActivity.startActivity(getActivity());
                startActivityForResult(intent, AddresseeListActivity.SELECT_ADDRESSEE);
                break;
            case R.id.task_timeout_view:
                showTimeoutDialog();
                break;
            case R.id.publish_button:
                publish();
                break;
        }
    }

    private void showTimeoutDialog() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(getActivity(), this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case PositionActivity.RETURN_POSITION_SUCCESS:
                addressPosition = (Position) data.getSerializableExtra("position");
                addressView.setText(addressPosition.getPosition());
                addressView.setGravity(Gravity.CENTER | Gravity.LEFT);
                break;
            case PositionActivity.RETURN_POSITION_ERROR:
                Toast.makeText(getActivity(), "获取地址失败", Toast.LENGTH_SHORT).show();
                break;
            case AddresseeListActivity.SELECT_ADDRESSEE:
                addressee = (Addressee) data.getSerializableExtra("addressee");
                StringBuilder showStr = new StringBuilder(addressee.getName() + " " + addressee.getPhone() + "\n");
                showStr.append(addressee.getAddress() + " " + addressee.getAddressDetail());
                addresseeView.setText(showStr.toString());
                addresseeView.setGravity(Gravity.CENTER | Gravity.LEFT);
                break;
            case AddresseeListActivity.NO_SELECT_ADDRESSEE:
                break;
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Log.d(TAG, "set timeout on: " + year + "-" + month + "-" + dayOfMonth);
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        Date date = calendar.getTime();
        timeoutView.setText(simpleDateFormat.format(date));
        taskContent.setTimeout(new Timestamp(date.getTime()));
    }

    public void publish() {
        String title = taskTitleView.getText().toString();
        if (title == null || title.trim().equals("")) {
            Toast.makeText(getActivity(), "请输入标题", Toast.LENGTH_SHORT).show();
            return;
        }
        String content = taskContentView.getText().toString();
        if (content == null || content.trim().equals("")) {
            Toast.makeText(getActivity(), "请输入描述信息", Toast.LENGTH_SHORT).show();
            return;
        }
        BigDecimal reward;
        try {
            reward = new BigDecimal(taskRewardView.getText().toString());
        }catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "报酬格式不正确，请输入数字", Toast.LENGTH_SHORT).show();
            return;
        }
        if (addressPosition == null) {
            Toast.makeText(getActivity(), "请选择地址", Toast.LENGTH_SHORT).show();
            return;
        }
        if (addressee == null) {
            Toast.makeText(getActivity(), "请选择收货地址", Toast.LENGTH_SHORT).show();
            return;
        }
        if (taskContent.getTimeout() == null) {
            Toast.makeText(getActivity(), "请设置有效期", Toast.LENGTH_SHORT).show();
            return;
        }
        taskContent.setTitle(title);
        taskContent.setContent(content);
        taskContent.setReward(reward);
        taskContent.setTargetPosition(addressPosition.getPosition());
        taskContent.setLongitude(String.format("%3.6f", addressPosition.getLng()));
        taskContent.setLatitude(String.format("%3.6f", addressPosition.getLat()));

        task.setPublisher(user.getId());
        task.setGmtCreate(new Timestamp(System.currentTimeMillis()));
        task.setGmtModified(new Timestamp(System.currentTimeMillis()));
        task.setState(Task.PUBLISHED);

        addressee.setId(null);
        addressee.setUserId(null);
        taskDTO = new TaskDTO(task, taskContent, addressee);
        new PublishTaskAsyncTask(taskDTO).execute();
    }

    private void clearView() {
        taskTitleView.setText("");
        taskContentView.setText("");
        taskRewardView.setText("0.00");

        addressView.setText("选择地址");
        addressView.setGravity(Gravity.CENTER | Gravity.RIGHT);
        addressPosition = null;

        addressee = null;
        addresseeView.setText("选择收件地址");
        addresseeView.setGravity(Gravity.CENTER | Gravity.RIGHT);

        timeoutView.setText("为任务设置一个截止时间");

        task = null;
        taskContent = null;
        task = new Task();
        taskContent = new TaskContent();
    }

    class PublishTaskAsyncTask extends AsyncTask<Void, Void, Result> {

        private TaskDTO taskDTO;

        public PublishTaskAsyncTask(TaskDTO taskDTO) {
            this.taskDTO = taskDTO;
        }

        @Override
        protected void onPostExecute(Result result) {
            if (result != null) {
                if (result.isSuccess()) {
                    Toast.makeText(getActivity(), "发布成功", Toast.LENGTH_SHORT).show();
                    clearView();
                    // 更新任务页面
                    father.updateTaskPublished();
                    father.updateTaskMyPublished();
                } else {
                    Toast.makeText(getActivity(), "发布失败\n" + result.getErr(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        protected Result doInBackground(Void... params) {
            try {
                HttpUtil.Holder holder = new HttpUtil.Holder() {
                    @Override
                    public void dealWithOutputStream(OutputStream outputStream) throws IOException {
                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.writeValue(outputStream, taskDTO);
                    }
                };
                HttpUtil.Header header = new HttpUtil.Header();
                header.put("Content-Type", "application/json");
                InputStream outputStream = HttpUtil.execute(new URL(App.BASE_URL + "/task/publish"), header, "POST", holder);
                String json = HttpUtil.getContent(outputStream);
                Log.d(TAG, "task publish result: " + json);
                ObjectMapper objectMapper = new ObjectMapper();
                JSONObject jsonObject = new JSONObject(json);
                String res = jsonObject.getString("result");
                Log.d(TAG, "result json: " + res);
                Result result = objectMapper.readValue(res, Result.class);
                if (result.isSuccess()) {
                    String taskTDOStr = jsonObject.getString("task");
                    Log.d(TAG, "taskTDO json: " + taskTDOStr);
                    TaskDTO taskDTO = objectMapper.readValue(taskTDOStr, TaskDTO.class);
                    taskPublished.put(taskDTO.getTask().getId(), taskDTO);
                    Log.d(TAG, "task publish success: " + taskDTO);
                }
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
