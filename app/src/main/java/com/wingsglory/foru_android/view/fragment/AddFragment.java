package com.wingsglory.foru_android.view.fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wingsglory.foru_android.App;
import com.wingsglory.foru_android.R;
import com.wingsglory.foru_android.model.Addressee;
import com.wingsglory.foru_android.model.Position;
import com.wingsglory.foru_android.model.Result;
import com.wingsglory.foru_android.model.Task;
import com.wingsglory.foru_android.model.TaskContent;
import com.wingsglory.foru_android.model.User;
import com.wingsglory.foru_android.util.HttpUtil;
import com.wingsglory.foru_android.util.LogUtil;
import com.wingsglory.foru_android.view.activity.AddresseeListActivity;
import com.wingsglory.foru_android.view.activity.MainActivity;
import com.wingsglory.foru_android.view.activity.PositionActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;


public class AddFragment extends Fragment
        implements View.OnClickListener, DatePickerDialog.OnDateSetListener, TextWatcher {
    private static final String TAG = "AddFragment";
    private static final int GET_TARGET_POSITION_REQUEST_CODE = 1;
    private static final int GET_ADDRESSEE_REQUEST_CODE = 2;

    private Date timeout;
    private Addressee addressee;
    private Position addressPosition;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat simpleDateFormat2 =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
    private ProgressDialog progressDialog;

    private View view;

    private User user;
    private App app;

    public static AddFragment newInstance() {
        AddFragment fragment = new AddFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (App) getActivity().getApplication();
        user = app.getUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_add, container, false);
        initView();
        return view;
    }

    private void initView() {
        taskTitleView = (EditText) view.findViewById(R.id.task_title);
        taskContentView = (EditText) view.findViewById(R.id.task_content);
        taskRewardView = (EditText) view.findViewById(R.id.task_reward);
        taskRewardView.addTextChangedListener(this);
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
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.task_address_view:
                intent = PositionActivity.startActivity(getActivity(), false, 0, 0);
                startActivityForResult(intent, GET_TARGET_POSITION_REQUEST_CODE);
                break;
            case R.id.task_addressee_view:
                intent = AddresseeListActivity.actionStart(getActivity(),
                        GET_ADDRESSEE_REQUEST_CODE);
                startActivityForResult(intent, GET_ADDRESSEE_REQUEST_CODE);
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
        switch (requestCode) {
            case GET_TARGET_POSITION_REQUEST_CODE:
                switch (resultCode) {
                    case PositionActivity.RETURN_POSITION_SUCCESS:
                        addressPosition = (Position) data.getSerializableExtra("position");
                        addressView.setText(addressPosition.getPosition());
                        addressView.setGravity(Gravity.CENTER | Gravity.LEFT);
                        break;
                    case PositionActivity.RETURN_POSITION_ERROR:
                        Toast.makeText(getActivity(), "获取地址失败", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
                break;
            case GET_ADDRESSEE_REQUEST_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        addressee = (Addressee) data.getSerializableExtra("addressee");
                        StringBuilder showStr = new StringBuilder(addressee.getName() + " " +
                                addressee.getPhone() + "\n");
                        showStr.append(addressee.getAddress() + " " +
                                addressee.getAddressDetail());
                        addresseeView.setText(showStr.toString());
                        addresseeView.setGravity(Gravity.CENTER | Gravity.LEFT);
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Log.d(TAG, "set timeout on: " + year + "-" + month + "-" + dayOfMonth);
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        Date date = calendar.getTime();
        String dateStr = simpleDateFormat.format(date);
        timeoutView.setText(dateStr);
        try {
            timeout = simpleDateFormat2.parse(dateStr + " 23:59:59");
        } catch (ParseException e) {
            e.printStackTrace();
        }
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
        } catch (Exception e) {
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
        if (timeout == null) {
            Toast.makeText(getActivity(), "请设置有效期", Toast.LENGTH_SHORT).show();
            return;
        }
        TaskContent taskContent = new TaskContent();
        taskContent.setTitle(title);
        taskContent.setContent(content);
        taskContent.setReward(reward);
        taskContent.setTimeout(timeout);
        taskContent.setTargetPosition(addressPosition.getPosition());
        taskContent.setLongitude(
                new BigDecimal(String.format("%3.6f", addressPosition.getLng())));
        taskContent.setLatitude(
                new BigDecimal(String.format("%3.6f", addressPosition.getLat())));
        addressee.setUserId(null);
        addressee.setId(null);
        taskContent.setAddressee(addressee);
        Task task = new Task();
        task.setPublisherId(user.getId());
        task.setGmtCreate(new Date());
        task.setState(Task.TASK_STATE_NEW);
        task.setContent(taskContent);

        new PublishTaskAsyncTask(task).execute();

        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
        }
        progressDialog.setMessage("请稍后...");
        progressDialog.setCancelable(false);
        progressDialog.show();
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

        addressee = null;
        addressPosition = null;
        timeout = null;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String rewardStr = s.toString();
        if ("".equals(rewardStr)) {
            return;
        }
        // 确定只有两位小数
        if (rewardStr.contains(".")) {
            String[] numbers = rewardStr.split("\\.");
            // 注意 "0." 的情况
            if (numbers.length == 2) {
                if (numbers[1].length() > 2) {
                    numbers[1] = numbers[1].substring(0, 2);
                    taskRewardView.setText(numbers[0] + "." + numbers[1]);
                }
            }
        }
    }

    class PublishTaskAsyncTask extends AsyncTask<Void, Void, JSONObject> {

        private Task task;

        public PublishTaskAsyncTask(Task task) {
            this.task = task;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject == null) {
                Toast.makeText(getActivity(), "网络异常", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    String resultStr = jsonObject.getString("result");
                    Result result = objectMapper.readValue(resultStr, Result.class);
                    if (result.isSuccess()) {
                        String taskStr = jsonObject.getString("task");
                        Task task = objectMapper.readValue(taskStr, Task.class);
                        app.addTask(task);
                        new AlertDialog.Builder(getActivity())
                                .setTitle("结果")
                                .setMessage("成功发布任务！")
                                .setPositiveButton("确定", null)
                                .show();
                        clearView();
                    } else {
                        Toast.makeText(getActivity(), result.getErr(), Toast.LENGTH_SHORT).show();
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
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            try {
                HttpUtil.Holder holder = new HttpUtil.Holder() {
                    @Override
                    public void dealWithOutputStream(OutputStream outputStream) throws IOException {
                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.writeValue(outputStream, task);
                    }
                };
                HttpUtil.Header header = new HttpUtil.Header();
                header.put("Content-Type", "application/json");
                InputStream outputStream =
                        HttpUtil.execute(new URL(App.BASE_URL + "/task/publish"),
                                header, "POST", holder);
                String json = HttpUtil.getContent(outputStream);
                LogUtil.d(TAG, "发布任务返回：" + json);
                JSONObject jsonObject = new JSONObject(json);
                return jsonObject;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
