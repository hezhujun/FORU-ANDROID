package com.wingsglory.foru_android.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wingsglory.foru_android.App;
import com.wingsglory.foru_android.R;
import com.wingsglory.foru_android.model.Addressee;
import com.wingsglory.foru_android.model.PageBean;
import com.wingsglory.foru_android.model.Result;
import com.wingsglory.foru_android.model.User;
import com.wingsglory.foru_android.util.LogUtil;
import com.wingsglory.foru_android.view.adapter.AddresseeAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AddresseeListActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener {
    private static final String TAG = "AddresseeListActivity";

    private ListView addresseeListView;
    private List<Addressee> addresseeList;
    private AddresseeAdapter addresseeAdapter;
    private View noAddresseeMsgView;

    private User user;
    private App app;

    public static Intent actionStart(Context context, int requestCode) {
        Intent intent = new Intent(context, AddresseeListActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addressee_list);

        app = (App) getApplication();
        user = app.getUser();

        noAddresseeMsgView = findViewById(R.id.no_addressee_msg_view);
        addresseeListView = (ListView) findViewById(R.id.addressee_list);
        addresseeList = new ArrayList<>();
        addresseeAdapter = new AddresseeAdapter(this,
                R.layout.address_list_item, addresseeList, null);
        addresseeListView.setAdapter(addresseeAdapter);
        addresseeListView.setOnItemClickListener(this);
        new GetAddresseeListAsyncTask(user.getId()).execute();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Addressee addressee = addresseeList.get(position);
        Intent intent = new Intent();
        intent.putExtra("addressee", addressee);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    class GetAddresseeListAsyncTask extends AsyncTask<Void, Void, JSONObject> {

        private Integer userId;

        public GetAddresseeListAsyncTask(Integer userId) {
            this.userId = userId;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject == null) {
                Toast.makeText(AddresseeListActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    String resultStr = jsonObject.getString("result");
                    Result result = objectMapper.readValue(resultStr, Result.class);
                    if (result.isSuccess()) {
                        String addresseesStr = jsonObject.getString("addressees");
                        PageBean<Addressee> addresseePageBean =
                                objectMapper.readValue(addresseesStr,
                                        new TypeReference<PageBean<Addressee>>() {
                                        });
                        if (addresseePageBean.size() > 0) {
                            addresseeList.clear();
                            addresseeList.addAll(addresseePageBean.getBeans());
                            addresseeAdapter.notifyDataSetChanged();
                            noAddresseeMsgView.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(AddresseeListActivity.this, result.getErr(),
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
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();
            FormBody formBody = new FormBody.Builder()
                    .add("userId", String.valueOf(userId))
                    .add("page", String.valueOf(1))
                    .add("rows", String.valueOf(10))
                    .build();
            Request request = new Request.Builder()
                    .post(formBody)
                    .url(App.BASE_URL + "/addressee/list")
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    LogUtil.d(TAG, "返回地址信息列表：" + json);
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
}
