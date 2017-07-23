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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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
import com.wingsglory.foru_android.model.User;
import com.wingsglory.foru_android.util.HttpUtil;
import com.wingsglory.foru_android.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class AddAddressActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int ADDRESS_ADD = 1;
    public static final int ADDRESS_UPDATE = 2;

    public static final int ADD_ADDRESSEE_SUCCESS = 0;
    public static final int UPDATE_ADDRESSEE_SUCCESS = 1;
    public static final int OPERATION_ERROR = -1;

    private static final String TAG = "AddAddressActivity";

    private int operation;
    private Addressee addressee;
    private TextView submit;
    private EditText addresseeView;
    private EditText phoneView;
    private TextView addressView;
    private EditText addressDetailView;
    private View getAddressButton;
    private ProgressDialog progressDialog;

    private User user;
    private App app;
    private Position position;

    public static void actionStart(Context context, int operation, Addressee addressee) {
        Intent intent = new Intent(context, AddAddressActivity.class);
        if (operation == ADDRESS_UPDATE) {
            intent.putExtra("addressee", addressee);
        }
        intent.putExtra("operation", operation);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        app = (App) getApplication();
        user = app.getUser();

        Intent intent = getIntent();
        operation = intent.getIntExtra("operation", ADDRESS_ADD);
        if (operation == ADDRESS_UPDATE) {
            addressee = (Addressee) intent.getSerializableExtra("addressee");
        }

        initView();
        initData();
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        if (operation == ADDRESS_ADD) {
            actionBar.setTitle("添加收货信息");
        } else {
            actionBar.setTitle("修改收货信息");
        }

        submit = (TextView) findViewById(R.id.submit);
        submit.setOnClickListener(this);
        addresseeView = (EditText) findViewById(R.id.addressee);
        phoneView = (EditText) findViewById(R.id.phone);
        addressView = (TextView) findViewById(R.id.address);
        addressDetailView = (EditText) findViewById(R.id.address_detail);
        getAddressButton = findViewById(R.id.get_address_button);
        getAddressButton.setOnClickListener(this);
    }

    private void initData() {
        if (operation == ADDRESS_UPDATE) {
            addresseeView.setText(addressee.getName());
            phoneView.setText(addressee.getPhone());
            addressView.setText(addressee.getAddress());
            addressDetailView.setText(addressee.getAddressDetail());
            if (addressee.getLongitude() != null && addressee.getLatitude() != null) {
                try {
                    position = new Position(addressee.getAddress(),
                            addressee.getLatitude().doubleValue(),
                            addressee.getLongitude().doubleValue());
                } catch (Exception e) {
                    position = null;
                }
            }
            submit.setText("更新");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.get_address_button:
                Intent intent;
                if (operation == ADDRESS_UPDATE && addressee != null) {
                    double lat = addressee.getLatitude().doubleValue();
                    double lng = addressee.getLongitude().doubleValue();
                    intent = PositionActivity.startActivity(this, true, lat, lng);
                } else {
                    intent = PositionActivity.startActivity(this, false, 0, 0);
                }
                startActivityForResult(intent, PositionActivity.RETURN_POSITION_SUCCESS);
                break;
            case R.id.submit:
                submit();
                break;
            default:
                break;
        }
    }

    private void submit() {
        String addressee = addresseeView.getText().toString();
        if ("".equals(addressee)) {
            Toast.makeText(this, "请输入收货人姓名", Toast.LENGTH_SHORT).show();
            return;
        }
        String phone = phoneView.getText().toString();
        if ("".equals(phone)) {
            Toast.makeText(this, "请输入收货手机号码", Toast.LENGTH_SHORT).show();
            return;
        }
        if (position == null || position.getPosition() == null) {
            Toast.makeText(this, "请选择收货地址", Toast.LENGTH_SHORT).show();
            return;
        }
        String addressDetail = addressDetailView.getText().toString();
        Addressee addresseeObj = null;
        if (operation == ADDRESS_ADD) {
            addresseeObj = new Addressee();
            addresseeObj.setUserId(user.getId());
        } else {
            addresseeObj = this.addressee;
        }
        addresseeObj.setName(addressee);
        addresseeObj.setPhone(phone);
        addresseeObj.setAddress(position.getPosition());
        addresseeObj.setAddressDetail(addressDetail);
        addresseeObj.setLatitude(new BigDecimal(String.format("%3.6f", position.getLat())));
        addresseeObj.setLongitude(new BigDecimal(String.format("%3.6f", position.getLng())));
        new SubmitAsyncTask(addresseeObj).execute();
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setMessage("请稍后...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case PositionActivity.RETURN_POSITION_SUCCESS:
                position = (Position) data.getSerializableExtra("position");
                if (position != null) {
                    addressView.setText(position.getPosition());
                }
                break;
            case PositionActivity.RETURN_POSITION_ERROR:
                Toast.makeText(this, "请重新选择地址", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    class SubmitAsyncTask extends AsyncTask<Void, Void, JSONObject> {
        private Addressee addressee;

        public SubmitAsyncTask(Addressee addressee) {
            this.addressee = addressee;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject == null) {
                Toast.makeText(AddAddressActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    String resultStr = jsonObject.getString("result");
                    Result result = objectMapper.readValue(resultStr, Result.class);
                    if (result.isSuccess()) {
                        DialogInterface.OnClickListener listener =
                                new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        };
                        AlertDialog.Builder builder =
                                new AlertDialog.Builder(AddAddressActivity.this)
                                .setTitle("结果")
                                .setPositiveButton("确定", listener);
                        if (operation == ADDRESS_ADD) {
                            builder.setMessage("添加收货信息成功");
                        } else {
                            builder.setMessage("修改收货信息成功");
                        }
                        builder.show();
                    } else {
                        if (operation == ADDRESS_ADD) {
                            Toast.makeText(AddAddressActivity.this,
                                    "添加失败\n" + result.getErr(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AddAddressActivity.this,
                                    "修改失败\n" + result.getErr(),
                                    Toast.LENGTH_SHORT).show();
                        }
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
            HttpUtil.Holder holder = new HttpUtil.Holder() {
                @Override
                public void dealWithOutputStream(OutputStream outputStream) throws IOException {
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.writeValue(outputStream, addressee);
                }
            };
            HttpUtil.Header header = new HttpUtil.Header();
            header.put("Content-Type", "application/json");
            InputStream is = null;
            try {
                if (operation == ADDRESS_ADD) {
                    is = HttpUtil.execute(new URL(App.BASE_URL + "/addressee/save"),
                            header, "POST", holder);
                } else {
                    is = HttpUtil.execute(new URL(App.BASE_URL + "/addressee/update"),
                            header, "POST", holder);
                }
                String json = HttpUtil.getContent(is);
                LogUtil.d(TAG, "返回结果：" + json);
                JSONObject jsonObject = new JSONObject(json);
                return jsonObject;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
