package com.wingsglory.foru_android.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import com.wingsglory.foru_android.util.HttpUtil;
import com.wingsglory.foru_android.util.LogUtil;
import com.wingsglory.foru_android.view.adapter.AddresseeAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AddressActivity extends BaseActivity
        implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "AddressActivity";

    private View addAddressView;
    private ListView addressListView;
    private AddresseeAdapter addresseeAdapter;
    private List<Addressee> addresseeList = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private User user;

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, AddressActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        app = (App) getApplication();
        user = app.getUser();

        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.addressee_manager, menu);
        return true;
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle("我的地址");
        }

        addAddressView = findViewById(R.id.add_address);
        addAddressView.setOnClickListener(this);
        addressListView = (ListView) findViewById(R.id.address_list);
        addresseeAdapter = new AddresseeAdapter(this,
                R.layout.address_list_item,
                addresseeList,
                new AddresseeListItemOnClickListener());
        addressListView.setAdapter(addresseeAdapter);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefreshLayout.setOnRefreshListener(this);
    }

    private void initData() {
        new GetAddresseeListAsyncTask(user.getId()).execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.manager_addressee:
                if ("管理".equals(item.getTitle())) {
                    item.setTitle("完成");
                    addresseeAdapter.showEditAndDeleteButton(true);
                } else {
                    item.setTitle("管理");
                    addresseeAdapter.showEditAndDeleteButton(false);
                }
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_address:
                if (addresseeList.size() >= 10) {
                    Toast.makeText(this, "最多创建10个收货地址", Toast.LENGTH_SHORT).show();
                    return;
                }
                AddAddressActivity.actionStart(this, AddAddressActivity.ADDRESS_ADD, null);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Addressee addressee;
        switch (resultCode) {
            case AddAddressActivity.ADD_ADDRESSEE_SUCCESS:
                addressee = (Addressee) data.getSerializableExtra("addressee");
                addresseeList.add(addressee);
                addresseeAdapter.notifyDataSetChanged();
//                new GetAddresseeListAsyncTask(Globle.user.getId()).execute();
                break;
            case AddAddressActivity.UPDATE_ADDRESSEE_SUCCESS:
                Addressee oldAddressee = (Addressee) data.getSerializableExtra("oldAddressee");
                addressee = (Addressee) data.getSerializableExtra("addressee");
                Iterator<Addressee> iterator = addresseeList.iterator();
                int position = -1;
                while (iterator.hasNext()) {
                    position++;
                    Addressee a = iterator.next();
                    if (a.equals(oldAddressee)) {
                        break;
                    }
                }
                if (position >= 0 && position < addresseeList.size()) {
                    addresseeList.set(position, addressee);
                }
                addresseeAdapter.notifyDataSetChanged();
                new GetAddresseeListAsyncTask(user.getId()).execute();
                break;
            case AddAddressActivity.OPERATION_ERROR:
                break;
        }
    }

    @Override
    public void onRefresh() {
        initData();
    }

    class AddresseeListItemOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            int position;
            switch (v.getId()) {
                case R.id.address_item_edit:
                    position = (int) v.getTag();
                    editItem(position);
                    break;
                case R.id.address_item_delete:
                    position = (int) v.getTag();
                    deleteItem(position);
                    break;
            }
        }
    }

    private void editItem(int position) {
        AddAddressActivity.actionStart(this,
                AddAddressActivity.ADDRESS_UPDATE,
                addresseeList.get(position));
    }

    private void deleteItem(int position) {
        Addressee addressee = addresseeList.remove(position);
        addresseeAdapter.notifyDataSetChanged();
        new DeleteAddresseeListAsyncTask(user.getId(), addressee).execute();
    }

    class GetAddresseeListAsyncTask extends AsyncTask<Void, Void, JSONObject> {

        private Integer userId;

        public GetAddresseeListAsyncTask(Integer userId) {
            this.userId = userId;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject == null) {
                Toast.makeText(AddressActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
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
                        }
                    } else {
                        Toast.makeText(AddressActivity.this, result.getErr(),
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
            mSwipeRefreshLayout.setRefreshing(false);
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

    class DeleteAddresseeListAsyncTask extends AsyncTask<Void, Void, JSONObject> {

        private Integer userId;
        private Addressee addressee;

        public DeleteAddresseeListAsyncTask(Integer userId, Addressee addressee) {
            this.userId = userId;
            this.addressee = addressee;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject == null) {
                Toast.makeText(AddressActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    String resultStr = jsonObject.getString("result");
                    Result result = objectMapper.readValue(resultStr, Result.class);
                    if (result.isSuccess()) {
                        Iterator<Addressee> iterator = addresseeList.iterator();
                        while (iterator.hasNext()) {
                            Addressee addressee = iterator.next();
                            if (addressee.getId().equals(this.addressee.getId())) {
                                iterator.remove();
                            }
                        }
                        addresseeAdapter.notifyDataSetChanged();
                    } else {
                        if ("".equals(result.getErr())) {
                            Toast.makeText(AddressActivity.this, "删除失败",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AddressActivity.this, result.getErr(),
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
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();
            FormBody formBody = new FormBody.Builder()
                    .add("userId", String.valueOf(userId))
                    .add("addresseeId", String.valueOf(addressee.getId()))
                    .build();
            Request request = new Request.Builder()
                    .post(formBody)
                    .url(App.BASE_URL + "/addressee/remove")
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    LogUtil.d(TAG, "删除收货信息返回的消息：" + json);
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
