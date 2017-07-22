package com.wingsglory.foru_android.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wingsglory.foru_android.App;
import com.wingsglory.foru_android.R;
import com.wingsglory.foru_android.model.Result;
import com.wingsglory.foru_android.model.User;
import com.wingsglory.foru_android.util.HttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "LoginActivity";

    private TextView phoneView;
    private TextView passwordView;
    private Button signInView;
    private Button signUpView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        phoneView = (TextView) findViewById(R.id.login_et_username);
        passwordView = (TextView) findViewById(R.id.login_et_pwd);
        signInView = (Button) findViewById(R.id.login_btn);
        signInView.setOnClickListener(this);
        signUpView = (Button) findViewById(R.id.register_btn);
        signUpView.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_btn:
                signIn();
                break;
            case R.id.register_btn:
                RegisterActivity.actionStart(this);
                break;
            default:
                break;
        }
    }
    
    private void signIn() {
        String phone = phoneView.getText().toString();
        if ("".equals(phone)) {
            Toast.makeText(this, "手机号不能为空", Toast.LENGTH_SHORT).show();
        }
        String password = passwordView.getText().toString();
        if ("".equals(password)) {
            Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
        }
        new SignInAsyncTask(phone, password).execute();
    }

    class SignInAsyncTask extends AsyncTask<Void, Void, String> {
        private String phone;
        private String password;

        public SignInAsyncTask(String phone, String password) {
            this.phone = phone;
            this.password = password;
        }

        @Override
        protected void onPostExecute(String s) {
            if ("".equals(s)) {
                Toast.makeText(LoginActivity.this, "未知错误", Toast.LENGTH_SHORT).show();
            }
            try {
                JSONObject jsonObject = new JSONObject(s);
                String res = jsonObject.getString("result");
                ObjectMapper objectMapper = new ObjectMapper();
                Result result = objectMapper.readValue(res, Result.class);
                if (result.isSuccess()) {
                    String userStr = jsonObject.getString("user");
                    User user = objectMapper.readValue(userStr, User.class);
                    MainActivity.actionStart(LoginActivity.this);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, result.getErr(), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(LoginActivity.this, "未知错误", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                HttpUtil.Param param = new HttpUtil.Param();
                param.put("phone", phone);
                param.put("password", password);
                HttpUtil.Header header = new HttpUtil.Header();
                header.put("Content-Type", "application/x-www-form-urlencoded");
                String json = HttpUtil.post(new URL(App.BASE_URL + "/user/sign_in"), header, param);
                Log.d(TAG, "user sign in " + json);
                return json;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }
    }

}
