package com.wingsglory.foru_android.view.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.wingsglory.foru_android.Const;
import com.wingsglory.foru_android.Globle;
import com.wingsglory.foru_android.R;
import com.wingsglory.foru_android.model.Result;
import com.wingsglory.foru_android.model.User;
import com.wingsglory.foru_android.util.HttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class RegisterActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "RegisterActivity";
    private static final String PHONE_REGEX = "(13\\d|14[57]|15[^4,\\D]|17[13678]|18\\d)\\d{8}|170[0589]\\d{7}";

    private TextView phoneView;
    private TextView passwordView;
    private TextView passwordConfirmView;
    private TextView usernameView;
    private TextView verificationCodeView;
    private Button getVerificationCodeView;
    private Button signUpView;
    private View mLoginFormView;
    private View mProgressView;
    private String verificationCode;

    private SendPhoneVerificationAsyncTask sendPhoneVerificationAsyncTask;
    private SignUpAsyncTask signUpAsyncTask;

    public RegisterActivity() {
        Globle.registerActivity = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
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
        mLoginFormView = findViewById(R.id.register_form);
        mProgressView = findViewById(R.id.register_progress);

        View view = findViewById(R.id.back);
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
        }
        switch (v.getId()) {
            case R.id.send_verification_code:
                sendPhoneVerificationAsyncTask = new SendPhoneVerificationAsyncTask();
                sendPhoneVerificationAsyncTask.execute(phone);
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
                break;
            case R.id.back:
                finish();
            default:
                break;
        }
    }

    class SendPhoneVerificationAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            Log.d(TAG, "doInBackground");
            String phone = params[0];
            HttpUtil.Param param = new HttpUtil.Param();
            param.put("phone", phone);
            Log.d(TAG, "phone " + phone);
            try {
                HttpUtil.Header header = new HttpUtil.Header();
                header.put("Content-Type", "application/x-www-form-urlencoded");
                String json = HttpUtil.post(new URL(Const.BASE_URL + "/verification_code/phone"), header, param);
                Log.d(TAG, "发送验证码 " + json);
                return json;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d(TAG, "onPostExecute " + s);
            if ("".equals(s)) {
                return;
            }
            try {
                JSONObject jsonObject = jsonObject = new JSONObject(s);
                String result = jsonObject.getString("result");
                Gson gson = new Gson();
                Result res = gson.fromJson(result, Result.class);
                if (res.isSuccess()) {
                    verificationCode = jsonObject.getString("verification_code");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class SignUpAsyncTask extends AsyncTask<Void, Void, String> {

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
        protected String doInBackground(Void... params) {
            HttpUtil.Param param = new HttpUtil.Param();
            param.put("phone", phone);
            param.put("password", password);
            param.put("username", username);
            param.put("verificationCode", verificationCode);
            try {
                HttpUtil.Header header = new HttpUtil.Header();
                header.put("Content-Type", "application/x-www-form-urlencoded");
                String json = HttpUtil.post(new URL(Const.BASE_URL + "/user/sign_up"), header, param);
                Log.d(TAG, "注册 " + json);
                return json;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            if ("".equals(s)) {
                Toast.makeText(RegisterActivity.this, "未知异常", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(s);
                String res = jsonObject.getString("result");
                Gson gson = new Gson();
                Result result = gson.fromJson(res, Result.class);
                if (result.isSuccess()) {
                    String userJson = jsonObject.getString("user");
                    gson = new Gson();
                    User user = gson.fromJson(userJson, User.class);
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.putExtra("user", user);
                    startActivity(intent);
                } else {
                    Toast.makeText(RegisterActivity.this, result.getErr(), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
