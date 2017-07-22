package com.wingsglory.foru_android.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

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
import java.util.List;

public class AddresseeListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "AddresseeListActivity";

    public static final int SELECT_ADDRESSEE = 11;
    public static final int NO_SELECT_ADDRESSEE = 10;

    private ListView addresseeListView;
    private List<Addressee> addresseeList;
    private AddresseeAdapter addresseeAdapter;

    private User user;
    private App app;

    public static Intent startActivity(Context context) {
        Intent intent = new Intent(context, AddresseeListActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addressee_list);

        app = (App) getApplication();
        user = app.getUser();

        addresseeListView = (ListView) findViewById(R.id.addressee_list);
        addresseeList = new ArrayList<>();
        addresseeAdapter = new AddresseeAdapter(this, R.layout.address_list_item, addresseeList, null);
        addresseeListView.setAdapter(addresseeAdapter);
        addresseeListView.setOnItemClickListener(this);
        new GetAddresseeListAsyncTask(user.getId()).execute();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Addressee addressee = addresseeList.get(position);
        Intent intent = new Intent();
        intent.putExtra("addressee", addressee);
        setResult(SELECT_ADDRESSEE, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        setResult(NO_SELECT_ADDRESSEE);
        super.onBackPressed();
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
}
