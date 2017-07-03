package com.wingsglory.foru_android.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wingsglory.foru_android.Const;
import com.wingsglory.foru_android.Globle;
import com.wingsglory.foru_android.R;
import com.wingsglory.foru_android.model.Addressee;
import com.wingsglory.foru_android.model.Position;
import com.wingsglory.foru_android.model.Result;
import com.wingsglory.foru_android.util.HttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;

public class AddAddressActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int ADDRESS_ADD = 1;
    public static final int ADDRESS_UPDATE = 2;
    public static final int ADD_ADDRESSEE_SUCCESS = 0;
    public static final int UPDATE_ADDRESSEE_SUCCESS = 1;
    public static final int OPERATION_ERROR = -1;

    private static final String TAG = "AddAddressActivity";
    private String title;
    private int operation;
    private Addressee addressee;
    private View submit;
    private EditText addresseeView;
    private EditText phoneView;
    private TextView addressView;
    private EditText addressDetailView;
    private View getAddressButton;
    private View returnView;
    private TextView toolBarTitle;

    public static Intent startActivity(Context context, String title, int operation, Addressee addressee) {
        Intent intent = new Intent(context, AddAddressActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("operation", operation);
        if (operation == ADDRESS_UPDATE) {
            intent.putExtra("addressee", addressee);
        }
        return intent;
    }

    private Position position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        operation = intent.getIntExtra("operation", ADDRESS_ADD);
        if (operation == ADDRESS_UPDATE) {
            addressee = (Addressee) intent.getSerializableExtra("addressee");
        }

        submit = findViewById(R.id.submit_button);
        addresseeView = (EditText) findViewById(R.id.addressee);
        phoneView = (EditText) findViewById(R.id.phone);
        addressView = (TextView) findViewById(R.id.address);
        addressDetailView = (EditText) findViewById(R.id.address_detail);
        getAddressButton = findViewById(R.id.get_address_button);
        returnView = findViewById(R.id.return_button);
        toolBarTitle = (TextView) findViewById(R.id.tool_bar_title);
        toolBarTitle.setText(title);

        if (operation == ADDRESS_UPDATE) {
            addresseeView.setText(addressee.getName());
            phoneView.setText(addressee.getPhone());
            addressView.setText(addressee.getAddress());
            addressDetailView.setText(addressee.getAddressDetail());
            if (addressee.getLongitude() != null && addressee.getLatitude() != null) {
                try {
                    position = new Position(addressee.getAddress(), Double.parseDouble(addressee.getLatitude()), Double.parseDouble(addressee.getLongitude()));
                } catch (Exception e) {
                    position = null;
                }
            }
        }

        submit.setOnClickListener(this);
        getAddressButton.setOnClickListener(this);
        returnView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.get_address_button:
                Intent intent;
                if (operation == ADDRESS_UPDATE && addressee != null) {
                    double lat = Double.parseDouble(addressee.getLatitude());
                    double lng = Double.parseDouble(addressee.getLongitude());
                    intent = PositionActivity.startActivity(this, true, lat, lng);
                } else {
                    intent = PositionActivity.startActivity(this, false, 0, 0);
                }
                startActivityForResult(intent, PositionActivity.RETURN_POSITION_SUCCESS);
                break;
            case R.id.submit_button:
                submit();
                break;
            case R.id.return_button:
                setResult(OPERATION_ERROR);
                finish();
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
        Addressee addresseeObj;
        if (operation == ADDRESS_ADD) {
            addresseeObj = new Addressee();
            addresseeObj.setName(addressee);
            addresseeObj.setPhone(phone);
            addresseeObj.setAddress(position.getPosition());
            addresseeObj.setAddressDetail(addressDetail);
            addresseeObj.setLatitude(String.format("%3.6f", position.getLat()));
            addresseeObj.setLongitude(String.format("%3.6f", position.getLng()));
            addresseeObj.setUserId(Globle.user.getId());
            addresseeObj.setGmtCreate(new Timestamp(System.currentTimeMillis()));
            addresseeObj.setGmtModified(new Timestamp(System.currentTimeMillis()));
            new SaveAddresseeAsyncTask(addresseeObj).execute();
        } else if (operation == ADDRESS_UPDATE) {
            try {
                addresseeObj = (Addressee) this.addressee.clone();
                addresseeObj.setName(addressee);
                addresseeObj.setPhone(phone);
                addresseeObj.setAddress(position.getPosition());
                addresseeObj.setAddressDetail(addressDetail);
                addresseeObj.setLatitude(String.format("%3.6f", position.getLat()));
                addresseeObj.setLongitude(String.format("%3.6f", position.getLng()));
                addresseeObj.setGmtModified(new Timestamp(System.currentTimeMillis()));
                new UpdateAddresseeAsyncTask(addresseeObj).execute();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }

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

    class SaveAddresseeAsyncTask extends AsyncTask<Void, Void, String> {
        private Addressee addressee;

        public SaveAddresseeAsyncTask(Addressee addressee) {
            this.addressee = addressee;
        }

        @Override
        protected void onPostExecute(String s) {
            if ("".equals(s)) {
                Toast.makeText(AddAddressActivity.this, "添加地址失败", Toast.LENGTH_SHORT).show();
            }
            try {
                JSONObject jsonObject = new JSONObject(s);
                String res = jsonObject.getString("result");
                ObjectMapper objectMapper = new ObjectMapper();
                Result result = objectMapper.readValue(res, Result.class);
                if (result.isSuccess()) {
                    String userStr = jsonObject.getString("addressee");
                    Addressee addressee = objectMapper.readValue(userStr, Addressee.class);
                    Intent intent = new Intent();
                    intent.putExtra("addressee", addressee);
                    setResult(ADD_ADDRESSEE_SUCCESS, intent);
                    finish();
                } else {
                    Toast.makeText(AddAddressActivity.this, result.getErr(), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(AddAddressActivity.this, "添加地址失败", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                HttpUtil.Header header = new HttpUtil.Header();
                header.put("Content-Type", "application/json");
                HttpUtil.Holder holder = new HttpUtil.Holder() {
                    @Override
                    public void dealWithOutputStream(OutputStream outputStream) throws IOException {
                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.writeValue(outputStream, addressee);
                    }
                };
                InputStream inputStream = HttpUtil.execute(new URL(Const.BASE_URL + "/addressee/save"), header, "POST", holder);
                String json = HttpUtil.getContent(inputStream);
                Log.d(TAG, "addressee add " + json);
                return json;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }
    }

    class UpdateAddresseeAsyncTask extends AsyncTask<Void, Void, String> {
        private Addressee addressee;

        public UpdateAddresseeAsyncTask(Addressee addressee) {
            this.addressee = addressee;
        }

        @Override
        protected void onPostExecute(String s) {
            if ("".equals(s)) {
                Toast.makeText(AddAddressActivity.this, "更改地址失败", Toast.LENGTH_SHORT).show();
            }
            try {
                JSONObject jsonObject = new JSONObject(s);
                String res = jsonObject.getString("result");
                ObjectMapper objectMapper = new ObjectMapper();
                Result result = objectMapper.readValue(res, Result.class);
                if (result.isSuccess()) {
                    String userStr = jsonObject.getString("addressee");
                    Addressee addressee = objectMapper.readValue(userStr, Addressee.class);
                    Intent intent = new Intent();
                    intent.putExtra("addressee", addressee);
                    intent.putExtra("oldAddressee", this.addressee);
                    setResult(UPDATE_ADDRESSEE_SUCCESS, intent);
                    finish();
                } else {
                    Toast.makeText(AddAddressActivity.this, result.getErr(), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(AddAddressActivity.this, "更改地址失败", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                HttpUtil.Header header = new HttpUtil.Header();
                header.put("Content-Type", "application/json");
                HttpUtil.Holder holder = new HttpUtil.Holder() {
                    @Override
                    public void dealWithOutputStream(OutputStream outputStream) throws IOException {
                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.writeValue(outputStream, addressee);
                    }
                };
                InputStream inputStream = HttpUtil.execute(new URL(Const.BASE_URL + "/addressee/update"), header, "POST", holder);
                String json = HttpUtil.getContent(inputStream);
                Log.d(TAG, "addressee update " + json);
                return json;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }
    }

    @Override
    public void onBackPressed() {
        setResult(OPERATION_ERROR);
        super.onBackPressed();
    }
}
