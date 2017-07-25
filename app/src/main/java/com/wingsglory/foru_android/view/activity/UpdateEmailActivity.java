package com.wingsglory.foru_android.view.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
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

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UpdateEmailActivity extends AppCompatActivity {
    private static final String TAG = "UpdateEmailActivity";
    private static final String EMAIL_REGEX =
            "\\w[-\\w.+]*@([A-Za-z0-9][-A-Za-z0-9]+\\.)+[A-Za-z]{2,14}";

    private App app;
    private User user;

    private EditText userEmailView;
    private ProgressDialog mProgressDialog;

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, UpdateEmailActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("设置邮箱");
        actionBar.setDisplayHomeAsUpEnabled(true);

        app = (App) getApplication();
        user = app.getUser();

        userEmailView = (EditText) findViewById(R.id.user_email);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ok, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.ok:
                updateEmail();
                break;
            default:
                break;
        }
        return true;
    }

    private void updateEmail() {
        String email = userEmailView.getText().toString().trim();
        if ("".equals(email)) {
            Toast.makeText(this, "请输入邮箱", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!email.matches(EMAIL_REGEX)) {
            Toast.makeText(this, "邮箱格式不正确", Toast.LENGTH_SHORT).show();
            return;
        }
        new UpdateEmailAsyncTask(email).execute();
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
        }
        mProgressDialog.setMessage("请稍后...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    class UpdateEmailAsyncTask extends AsyncTask<Void, Void, JSONObject> {

        private String email;
        private ObjectMapper objectMapper = new ObjectMapper();

        public UpdateEmailAsyncTask(String email) {
            this.email = email;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {


            User updateUser = new User();
            updateUser.setId(user.getId());
            updateUser.setEmail(email);
            try {
                String userJson = objectMapper.writeValueAsString(updateUser);

                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = RequestBody.create(
                        MediaType.parse("application/json"), userJson);
                Request request = new Request.Builder()
                        .post(requestBody)
                        .url(App.BASE_URL + "/user/update")
                        .build();
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    LogUtil.d(TAG, "修改返回：" + json);
                    JSONObject jsonObject = new JSONObject(json);
                    return jsonObject;
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
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
                Toast.makeText(UpdateEmailActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    String resultStr = jsonObject.getString("result");
                    Result result = objectMapper.readValue(resultStr, Result.class);
                    if (result.isSuccess()) {
                        String userStr = jsonObject.getString("user");
                        User updateUser = objectMapper.readValue(userStr, User.class);
                        user.setEmail(updateUser.getEmail());
                        new AlertDialog.Builder(UpdateEmailActivity.this).setTitle("结果")
                                .setMessage("设置邮箱成功")
                                .setPositiveButton("返回", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                                .show();
                    } else {
                        Toast.makeText(UpdateEmailActivity.this, result.getErr(),
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
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
        }
    }
}
