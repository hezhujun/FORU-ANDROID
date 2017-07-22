package com.wingsglory.foru_android.view.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wingsglory.foru_android.App;
import com.wingsglory.foru_android.R;
import com.wingsglory.foru_android.model.Result;
import com.wingsglory.foru_android.model.User;
import com.wingsglory.foru_android.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RegisterActivity";
    private static final String PHONE_REGEX = "(13\\d|14[57]|15[^4,\\D]|17[13678]|18\\d)\\d{8}|170[0589]\\d{7}";

    private TextView phoneView;
    private TextView passwordView;
    private TextView passwordConfirmView;
    private TextView usernameView;
    private TextView verificationCodeView;
    private Button getVerificationCodeView;
    private Button signUpView;
    private String verificationCode;
    private ProgressDialog progressDialog;

    private SendPhoneVerificationAsyncTask sendPhoneVerificationAsyncTask;
    private SignUpAsyncTask signUpAsyncTask;

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, RegisterActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameView = (TextView) findViewById(R.id.register_et_username);
        phoneView = (TextView) findViewById(R.id.phone_num);
        passwordView = (TextView) findViewById(R.id.register_et_pwd);
        passwordConfirmView = (TextView) findViewById(R.id.register_et_verify_pwd);
        verificationCodeView = (TextView) findViewById(R.id.verificationcodetext);
        getVerificationCodeView = (Button) findViewById(R.id.send_verification_code);
        getVerificationCodeView.setOnClickListener(this);
        signUpView = (Button) findViewById(R.id.register_btn);
        signUpView.setOnClickListener(this);

        View view = findViewById(R.id.back);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        view.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String phone = phoneView.getText().toString();
        if ("".equals(phone)) {
            Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!phone.matches(PHONE_REGEX)) {
            Toast.makeText(this, "手机号格式不正确", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (v.getId()) {
            case R.id.send_verification_code:
                sendPhoneVerificationAsyncTask = new SendPhoneVerificationAsyncTask(phone);
                sendPhoneVerificationAsyncTask.execute(phone);
                // 未请求完成不能继续发送验证码请求
                getVerificationCodeView.setOnClickListener(null);
                break;
            case R.id.register_btn:
                String username = usernameView.getText().toString();
                if ("".equals(username)) {
                    Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show();
                    return;
                }
                String password = passwordView.getText().toString();
                if ("".equals(password)) {
                    Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                String passwordConfirm = passwordConfirmView.getText().toString();
                if ("".equals(passwordConfirm)) {
                    Toast.makeText(this, "请输入确认密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!password.equals(passwordConfirm)) {
                    Toast.makeText(this, "密码前后不一致", Toast.LENGTH_SHORT).show();
                    return;
                }
                String verificationCode = verificationCodeView.getText().toString();
                if ("".equals(verificationCode)) {
                    Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (this.verificationCode != null && !verificationCode.equals(this.verificationCode)) {
                    Toast.makeText(this, "验证码不正确", Toast.LENGTH_SHORT).show();
                    return;
                }
                signUpAsyncTask = new SignUpAsyncTask(phone, username, password, verificationCode);
                signUpAsyncTask.execute();
                if (progressDialog == null) {
                    progressDialog = new ProgressDialog(this);
                }

                progressDialog.setMessage("请稍后。。。");
                progressDialog.setCancelable(false);
                progressDialog.show();
                break;
            default:
                break;
        }
    }

    class SendPhoneVerificationAsyncTask extends AsyncTask<String, Void, JSONObject> {

        private String phone;

        public SendPhoneVerificationAsyncTask(String phone) {
            this.phone = phone;
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            OkHttpClient client = new OkHttpClient();
            FormBody formBody = new FormBody.Builder()
                    .add("phone", phone)
                    .build();
            Request request = new Request.Builder()
                    .url(App.BASE_URL + "/verification_code/phone")
                    .post(formBody)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    LogUtil.d(TAG, "验证码返回数据：" + json);
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
                Toast.makeText(RegisterActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    String resultStr = jsonObject.getString("result");
                    ObjectMapper objectMapper = new ObjectMapper();
                    Result result = objectMapper.readValue(resultStr, Result.class);
                    if (result.isSuccess()) {
                        verificationCode = jsonObject.getString("verification_code");
                    } else {
                        Toast.makeText(RegisterActivity.this, "验证码发送异常", Toast.LENGTH_SHORT).show();
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
            getVerificationCodeView.setOnClickListener(RegisterActivity.this);
        }
    }

    class SignUpAsyncTask extends AsyncTask<Void, Void, JSONObject> {

        private String phone;
        private String username;
        private String password;
        private String verificationCode;

        public SignUpAsyncTask(String phone, String username, String password, String verificationCode) {
            this.phone = phone;
            this.username = username;
            this.password = password;
            this.verificationCode = verificationCode;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();
            FormBody formBody = new FormBody.Builder()
                    .add("phone", phone)
                    .add("password", password)
                    .add("username", username)
                    .add("verificationCode", verificationCode)
                    .build();
            Request request = new Request.Builder()
                    .post(formBody)
                    .url(App.BASE_URL + "/user/sign_up")
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    LogUtil.d(TAG, "注册返回：" + json);
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
                Toast.makeText(RegisterActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    String resultStr = jsonObject.getString("result");
                    ObjectMapper objectMapper = new ObjectMapper();
                    Result result = objectMapper.readValue(resultStr, Result.class);
                    if (result.isSuccess()) {
                        String userStr = jsonObject.getString("user");
                        User user = objectMapper.readValue(userStr, User.class);
                        // 保存用户信息到Application中
                        App app = (App) getApplication();
                        app.setUser(user);
                        MainActivity.actionStart(RegisterActivity.this);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, result.getErr(),
                                Toast.LENGTH_SHORT).show();
                    }
                } catch (JsonParseException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (JsonMappingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
            }
        }
    }
}
