package com.wingsglory.foru_android.view.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wingsglory.foru_android.App;
import com.wingsglory.foru_android.R;
import com.wingsglory.foru_android.model.Result;
import com.wingsglory.foru_android.model.User;
import com.wingsglory.foru_android.util.LogUtil;
import com.wingsglory.foru_android.util.PreferenceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "LoginActivity";

    private TextView phoneView;
    private TextView passwordView;
    private Button signInView;
    private Button signUpView;

    private ProgressDialog progressDialog;

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

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

        boolean isAutoLogin = PreferenceUtil.isAutoLogin(this);
        if (isAutoLogin) {
            String phone = PreferenceUtil.readUserPhone(this);
            phoneView.setText(phone);
            String password = PreferenceUtil.readUserPassword(this);
            passwordView.setText(password);
            signIn(phone, password);
        }
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
        signIn(phone, password);
    }

    private void signIn(String phone, String password) {
        new SignInAsyncTask(phone, password).execute();
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setMessage("请稍后...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    class SignInAsyncTask extends AsyncTask<Void, Void, JSONObject> {
        private String phone;
        private String password;

        public SignInAsyncTask(String phone, String password) {
            this.phone = phone;
            this.password = password;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject == null) {
                Toast.makeText(LoginActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    String resultStr = jsonObject.getString("result");
                    ObjectMapper objectMapper = new ObjectMapper();
                    Result result = objectMapper.readValue(resultStr, Result.class);
                    if (result.isSuccess()) {
                        String userStr = jsonObject.getString("user");
                        User user = objectMapper.readValue(userStr, User.class);
                        App app = (App) getApplication();
                        app.setUser(user);
                        PreferenceUtil.save(app, user.getId(), phone, password);
                        PreferenceUtil.setAutoLogin(app, true);
                        jMessageLogin(user.getUsername(), password);
                        MainActivity.actionStart(LoginActivity.this);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, result.getErr(),
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
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();
            FormBody formBody = new FormBody.Builder()
                    .add("phone", phone)
                    .add("password", password)
                    .build();
            Request request = new Request.Builder()
                    .post(formBody)
                    .url(App.BASE_URL + "/user/sign_in")
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    LogUtil.d(TAG, "登录返回：" + json);
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
    }

    private void jMessageLogin(final String username, final String password) {
        new Thread() {
            @Override
            public void run() {
                JMessageClient.login(username, password, new BasicCallback() {
                    private int count = 0;

                    @Override
                    public void gotResult(int responseCode, String responseMessage) {
                        if (responseCode == 0) {
                            LogUtil.d(TAG, "JMessage 登录成功");
                            new Thread() {
                                @Override
                                public void run() {
                                    updateUserAvatar();
                                }
                            }.start();
                        } else {
                            LogUtil.d(TAG, "JMessage 登录失败" + responseMessage);
                            count++;
                            // 3次重试机会
                            if (count == 3) {
                                return;
                            } else {
                                JMessageClient.login(username, password, this);
                            }
                        }
                    }
                });
            }
        }.start();
    }

    private void updateUserAvatar() {
        final User user = app.getUser();
        if (user == null || user.getProtraitUrl() == null) {
            return;
        }
        final UserInfo userInfo = JMessageClient.getMyInfo();
        String avatar = userInfo.getAvatar();
        if (TextUtils.isEmpty(avatar)) {
            String lastFormat = user.getProtraitUrl().substring(user.getProtraitUrl().lastIndexOf("."));
            if (TextUtils.isEmpty(lastFormat)) {
                lastFormat = ".jpg";
            }
            String filename = user.getUsername() + lastFormat;
            File file = new File(getExternalCacheDir() + File.separator + filename);
            File directory = file.getParentFile();
            if (!directory.exists()) {
                directory.mkdirs();
            }
            if (!file.exists()) {
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(user.getProtraitUrl())
                        .get()
                        .build();
                FileOutputStream fos = null;
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    ResponseBody responseBody = response.body();
                    if (response.isSuccessful()) {
                        fos = new FileOutputStream(file);
                        fos.write(responseBody.bytes());
                        fos.flush();
                        fos.close();
                    } else {
                        LogUtil.d(TAG, responseBody.string());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            if (file.exists()) {
                JMessageClient.updateUserAvatar(file, new BasicCallback() {
                    @Override
                    public void gotResult(int i, String s) {
                        if (i == 0) {
                            LogUtil.d(TAG, "JMessage 更新头像成功");
                        } else {
                            LogUtil.d(TAG, "更新头像失败：" + s);
                        }
                    }
                });
            }
        }
    }

}
