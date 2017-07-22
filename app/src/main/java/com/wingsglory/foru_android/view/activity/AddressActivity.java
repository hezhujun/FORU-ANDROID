package com.wingsglory.foru_android.view.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wingsglory.foru_android.App;
import com.wingsglory.foru_android.R;
import com.wingsglory.foru_android.model.Addressee;
import com.wingsglory.foru_android.model.Result;
import com.wingsglory.foru_android.model.User;
import com.wingsglory.foru_android.util.HttpUtil;
import com.wingsglory.foru_android.view.adapter.AddresseeAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AddressActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = "AddressActivity";
    private View addAddressView;
    private View returnView;
    private TextView toolBarTitle;
    private TextView managerButton;
    private ListView addressListView;
    private AddresseeAdapter addresseeAdapter;
    private List<Addressee> addresseeList = new ArrayList<>();

    private User user;
    private App app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        app = (App) getApplication();
        user = app.getUser();

        addAddressView = findViewById(R.id.add_address);
        addAddressView.setOnClickListener(this);
        returnView = findViewById(R.id.return_button);
        returnView.setOnClickListener(this);
        toolBarTitle = (TextView) findViewById(R.id.tool_bar_title);
        toolBarTitle.setText("我的地址");
        managerButton = (TextView) findViewById(R.id.address_manager);
        managerButton.setOnClickListener(this);
        addressListView = (ListView) findViewById(R.id.address_list);
        addresseeAdapter = new AddresseeAdapter(this, R.layout.address_list_item, addresseeList, new AddresseeListItemOnClickListener());
        addressListView.setAdapter(addresseeAdapter);
        addressListView.setOnItemClickListener(this);
        new GetAddresseeListAsyncTask(user.getId()).execute();
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    class AddresseeListItemOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            int position;
            switch (v.getId()) {
                case R.id.address_item_edit:
                    position = (int) v.getTag();
//                    Toast.makeText(AddressActivity.this, "editItem " + position, Toast.LENGTH_SHORT).show();
                    editItem(position);
                    break;
                case R.id.address_item_delete:
                    position = (int) v.getTag();
//                    Toast.makeText(AddressActivity.this, "delete " + position, Toast.LENGTH_SHORT).show();
                    deleteItem(position);
                    break;
            }
        }
    }

    private void editItem(int position) {
        Intent intent = AddAddressActivity.startActivity(AddressActivity.this, "更新地址", AddAddressActivity.ADDRESS_UPDATE, addresseeList.get(position));
        startActivityForResult(intent, AddAddressActivity.ADDRESS_UPDATE);
    }

    private void deleteItem(int position) {
        Addressee addressee = addresseeList.remove(position);
        addresseeAdapter.notifyDataSetChanged();
        new DeleteAddresseeListAsyncTask(user.getId(), position, addressee).execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.return_button:
                finish();
                break;
            case R.id.add_address:
                Intent intent = AddAddressActivity.startActivity(this, "添加地址", AddAddressActivity.ADDRESS_ADD, null);
                startActivityForResult(intent, AddAddressActivity.ADD_ADDRESSEE_SUCCESS);
                break;
            case R.id.address_manager:
                String text = managerButton.getText().toString();
                if ("管理".equals(text)) {
                    managerButton.setText("完成");
                    addresseeAdapter.showEditAndDeleteButton(true);
                } else {
                    managerButton.setText("管理");
                    addresseeAdapter.showEditAndDeleteButton(false);
                }
                break;
        }
    }

    class GetAddresseeListAsyncTask extends AsyncTask<Void, Void, List<Addressee>> {

        private Integer userId;

        public GetAddresseeListAsyncTask(Integer userId) {
            this.userId = userId;
        }

        @Override
        protected void onPostExecute(List<Addressee> addressees) {
            addresseeList.clear();
            addresseeList.addAll(addressees);
            addresseeAdapter.notifyDataSetChanged();
        }

        @Override
        protected List<Addressee> doInBackground(Void... params) {
            try {
                HttpUtil.Param param = new HttpUtil.Param();
                param.put("userId", String.valueOf(userId));
                HttpUtil.Header header = new HttpUtil.Header();
                header.put("Content-Type", "application/x-www-form-urlencoded");
                String json = HttpUtil.post(new URL(App.BASE_URL + "/addressee/list"), header, param);
                Log.d(TAG, "addressee list " + json);
                JSONObject jsonObject = new JSONObject(json);
                String res = jsonObject.getString("result");
                ObjectMapper objectMapper = new ObjectMapper();
                Result result = objectMapper.readValue(res, Result.class);
                if (result.isSuccess()) {
                    String addresseesStr = jsonObject.getString("addressees");
                    List<Addressee> addressees = objectMapper.readValue(addresseesStr, new TypeReference<List<Addressee>>() {
                    });
                    return addressees;
                }
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

    class DeleteAddresseeListAsyncTask extends AsyncTask<Void, Void, Boolean> {

        private Integer userId;
        private int position;
        private Addressee addressee;

        public DeleteAddresseeListAsyncTask(Integer userId, int position, Addressee addressee) {
            this.userId = userId;
            this.position = position;
            this.addressee = addressee;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
            } else {
                addresseeList.add(position, addressee);
                addresseeAdapter.notifyDataSetChanged();
                Toast.makeText(AddressActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                HttpUtil.Param param = new HttpUtil.Param();
                param.put("userId", String.valueOf(userId));
                param.put("addresseeId", String.valueOf(addressee.getId()));
                HttpUtil.Header header = new HttpUtil.Header();
                header.put("Content-Type", "application/x-www-form-urlencoded");
                String json = HttpUtil.post(new URL(App.BASE_URL + "/addressee/remove"), header, param);
                Log.d(TAG, "addressee delete " + json);
                JSONObject jsonObject = new JSONObject(json);
                String res = jsonObject.getString("result");
                ObjectMapper objectMapper = new ObjectMapper();
                Result result = objectMapper.readValue(res, Result.class);
                if (result.isSuccess()) {
                    return true;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

}
