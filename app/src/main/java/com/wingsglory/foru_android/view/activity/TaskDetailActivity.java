package com.wingsglory.foru_android.view.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wingsglory.foru_android.App;
import com.wingsglory.foru_android.R;
import com.wingsglory.foru_android.model.Addressee;
import com.wingsglory.foru_android.model.Result;
import com.wingsglory.foru_android.model.Task;
import com.wingsglory.foru_android.model.TaskContent;
import com.wingsglory.foru_android.model.User;
import com.wingsglory.foru_android.util.PreferenceUtil;
import com.wingsglory.foru_android.util.WalkRouteOverlay;
import com.wingsglory.foru_android.view.service.ForuService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TaskDetailActivity extends BaseActivity implements View.OnClickListener, AMap.OnMyLocationChangeListener, RouteSearch.OnRouteSearchListener {
    private static final String TAG = "TaskDetailActivity";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private User user;
    private Task task;

    private ImageView userImageView;
    private TextView usernameView;
    private TextView taskTitleView;
    private TextView taskRewardView;
    private TextView taskTimeoutView;
    private TextView taskStateView;
    private TextView taskContentView;
    private TextView taskTargetPositionView;
    private TextView addresseeNameView;
    private TextView addresseePhoneView;
    private TextView addresseeAddressView;
    private View addresseeView;
    private TextView deleteTaskButton;
    private TextView acceptTaskButton;
    private TextView completeTaskButton;
    private TextView confirmTaskButton;
    private ProgressDialog progressDialog;
    private View scrollView;

    private MapView mMapView;
    private AMap aMap;
    private MyLocationStyle myLocationStyle;
    private LatLng target;
    private Marker targetMarker;
    private LatLng myLocation;
    private LatLng recipientLocation;
    private Marker recipientMarker;
    private RouteSearch mRouteSearch;
    private WalkRouteResult mWalkRouteResult;
    private boolean isShowWalkRoute = false;

    private void initMap() {
        aMap = mMapView.getMap();
        myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER);
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setMyLocationEnabled(true);
        aMap.setOnMyLocationChangeListener(this);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);

        mRouteSearch = new RouteSearch(this);
        mRouteSearch.setRouteSearchListener(this);
    }

    private ForuService.ServiceController serviceController;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serviceController = (ForuService.ServiceController) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public static Intent actionStart(Context context, Task task) {
        Intent intent = new Intent(context, TaskDetailActivity.class);
        intent.putExtra("task", task);
        return intent;
    }

    private void initView() {
        userImageView = (ImageView) findViewById(R.id.user_image);
        usernameView = (TextView) findViewById(R.id.user_name);
        taskTitleView = (TextView) findViewById(R.id.task_title);
        taskRewardView = (TextView) findViewById(R.id.task_reward);
        taskTimeoutView = (TextView) findViewById(R.id.task_timeout);
        taskStateView = (TextView) findViewById(R.id.task_state);
        taskContentView = (TextView) findViewById(R.id.task_content);
        taskTargetPositionView = (TextView) findViewById(R.id.task_target_position);
        addresseeNameView = (TextView) findViewById(R.id.addressee);
        addresseePhoneView = (TextView) findViewById(R.id.phone);
        addresseeAddressView = (TextView) findViewById(R.id.address);
        addresseeView = findViewById(R.id.task_addressee_view);
        deleteTaskButton = (TextView) findViewById(R.id.delete_task);
        acceptTaskButton = (TextView) findViewById(R.id.accept_task);
        completeTaskButton = (TextView) findViewById(R.id.complete_task);
        confirmTaskButton = (TextView) findViewById(R.id.confirm_task);
        deleteTaskButton.setOnClickListener(this);
        acceptTaskButton.setOnClickListener(this);
        completeTaskButton.setOnClickListener(this);
        confirmTaskButton.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);
        scrollView = findViewById(R.id.scrollView);
    }

    private void initData() {
        addresseeView.setVisibility(View.GONE);
        deleteTaskButton.setVisibility(View.GONE);
        acceptTaskButton.setVisibility(View.GONE);
        completeTaskButton.setVisibility(View.GONE);
        confirmTaskButton.setVisibility(View.GONE);

        TaskContent content = task.getContent();
        // 显示目的地位置
        target = new LatLng(content.getLatitude().doubleValue(), content.getLongitude().doubleValue());
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(target, 15.0f));
        if (targetMarker == null) {
            targetMarker = aMap.addMarker(new MarkerOptions().position(target).title("任务地址")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.amap_end)));
        } else {
            targetMarker.setPosition(target);
        }
        User publisher = task.getPublisher();
        if (publisher.getProtraitUrl() != null && !"".equals(publisher.getProtraitUrl())) {
            Glide.with(this).load(publisher.getProtraitUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(userImageView);
        }
        usernameView.setText(publisher.getUsername());
        taskTitleView.setText(content.getTitle());
        taskContentView.setText(content.getContent());
        taskTargetPositionView.setText(content.getTargetPosition());
        taskRewardView.setText("赏" + content.getReward() + "元");
        Date timeout = content.getTimeout();
        taskTimeoutView.setText(sdf.format(timeout) + "到期");
        Addressee addressee = content.getAddressee();
        addresseeNameView.setText(addressee.getName());
        addresseePhoneView.setText(addressee.getPhone());
        addresseeAddressView.setText(addressee.getAddress() + " " +
                addressee.getAddressDetail());
        // 判断任务是否过期
        long diff = timeout.getTime() - System.currentTimeMillis();

        if (Task.TASK_STATE_NEW.equals(task.getState())) {
            if (diff < 0) {
                task.setState(Task.TASK_STATE_OVERDUE);
                return; // 不在此判断处理
            }
            if (user.getId().equals(task.getPublisherId())) {
                // 是发布者在浏览信息
                addresseeView.setVisibility(View.VISIBLE);
                deleteTaskButton.setVisibility(View.VISIBLE);
            } else {
                // 其他人在浏览
                acceptTaskButton.setVisibility(View.VISIBLE);
            }
        }
        if (Task.TASK_STATE_WAIT_FOR_COMPLETE.equals(task.getState())) {
            if (diff < 0) {
                task.setState(Task.TASK_STATE_FAIL);
                // 不在此判断处理
            }
            addresseeView.setVisibility(View.VISIBLE);
            if (user.getId().equals(task.getRecipientId())) {
                completeTaskButton.setVisibility(View.VISIBLE);
                // 显示导航路径
                showWalkRoute();
            } else if (user.getId().equals(task.getPublisherId())) {
                // 显示任务接受者位置
                showRecipientPosition();
            }
        }
        if (Task.TASK_STATE_WAIT_FOR_CONFIRM.equals(task.getState())) {
            addresseeView.setVisibility(View.VISIBLE);
            if (user.getId().equals(task.getPublisherId())) {
                confirmTaskButton.setVisibility(View.VISIBLE);
            }
        }
        if (Task.TASK_STATE_COMPLETE.equals(task.getState())) {
            addresseeView.setVisibility(View.VISIBLE);
        }
        if (Task.TASK_STATE_OVERDUE.equals(task.getState())) {
            if (user.getId().equals(task.getPublisherId())) {
                addresseeView.setVisibility(View.VISIBLE);
            }
        }
        if (Task.TASK_STATE_FAIL.equals(task.getState())) {
            addresseeView.setVisibility(View.VISIBLE);
        }
        taskStateView.setText(task.getState());
    }

    // 显示任务接受者的位置
    private void showRecipientPosition() {
        // 每个两秒刷新位置
        ScheduledExecutorService schedule = Executors.newScheduledThreadPool(1);
        Runnable updateRecipientPosition = new Runnable() {
            long lastUpdateTime = System.currentTimeMillis();

            @Override
            public void run() {
                // 没有收到任务接受人的位置信息
                if (PreferenceUtil.isUserPositionExists(app, task.getRecipientId())) {
                    return;
                }
                long updateTime = PreferenceUtil.getUserPositionUpdateTime(app, task.getRecipientId());
                if (updateTime >= lastUpdateTime) {
                    lastUpdateTime = updateTime;
                    recipientLocation = PreferenceUtil.getUserPosition(app, task.getRecipientId());
                }
                if (recipientLocation != null) {
                    if (recipientMarker == null) {
                        recipientMarker = aMap.addMarker(new MarkerOptions()
                                .title("任务接受者")
                                .position(recipientLocation)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.run)));
                    } else {
                        recipientMarker.setPosition(recipientLocation);
                    }
                }
                // 任务接受者和目的地址都显示在地图上
                LatLngBounds bounds = getLatLngBounds(target, recipientLocation);
                aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
            }
        };
        schedule.scheduleAtFixedRate(updateRecipientPosition, 0, 2, TimeUnit.SECONDS);
    }

    // 显示导航路线
    private void showWalkRoute() {
        isShowWalkRoute = true;
        // 确定显示导航路线
        myLocation = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        app = (App) getApplication();
        user = app.getUser();

        if (user == null) {
            // app已经完全退出了
            finish();
            LoginActivity.actionStart(this);
        }

        Intent intent = getIntent();
        task = (Task) intent.getSerializableExtra("task");
        // 通过推送收到的任务没有保存再App中，现在保存
        app.addTask(task);

        initView();
        Intent serviceIntent = new Intent(this, ForuService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);

        taskStateChangedReceiver = new TaskStateChangedReceiver();
        IntentFilter taskStateChangedIntentFilter = new IntentFilter("com.wingsglory.foru_android.TASK_STATE_CHANGED");
        registerReceiver(taskStateChangedReceiver, taskStateChangedIntentFilter);
    }

    private TaskStateChangedReceiver taskStateChangedReceiver;

    class TaskStateChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            String title = bundle.getString("title");
            Task task = (Task) bundle.getSerializable("task");
            if (App.TASK_ACCEPT.equals(title)) {
                // ...
            } else if (App.TASK_ABANDON.equals(title)) {
                // ...
            } else if (App.TASK_COMPLETE.equals(title)) {
                // ...
            } else if (App.TASK_FINISH.equals(title)) {
                // ...
            }
            if (task.getId().equals(TaskDetailActivity.this.task.getId())) {
                TaskDetailActivity.this.task = task;
                initData();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initMap();
        initData();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }

    @Override
    public void onClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("注意");
        switch (v.getId()) {
            case R.id.delete_task:
                builder.setMessage("是否删除此任务？");
                builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new OperationAsyncTask("/task/remove", "删除成功",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        task.setState(Task.TASK_STATE_DELETE);
                                        finish();
                                    }
                                }).execute();
                        progressDialog.setMessage("请稍后...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                    }
                });
                break;
            case R.id.accept_task:
                builder.setMessage("是否接受此任务？");
                builder.setPositiveButton("接受", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new OperationAsyncTask("/task/accept", "接受任务成功",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        task.setRecipientId(user.getId());
                                        task.setState(Task.TASK_STATE_WAIT_FOR_COMPLETE);
                                        initData();
                                    }
                                }).execute();
                        progressDialog.setMessage("请稍后...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                    }
                });
                break;
            case R.id.complete_task:
                builder.setMessage("是否完成任务？");
                builder.setPositiveButton("完成", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new OperationAsyncTask("/task/complete", "完成",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        task.setState(Task.TASK_STATE_WAIT_FOR_CONFIRM);
                                        initData();
                                    }
                                }).execute();
                        progressDialog.setMessage("请稍后...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                    }
                });
                break;
            case R.id.confirm_task:
                builder.setMessage("是否确认完成任务？");
                builder.setPositiveButton("确认完成", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new OperationAsyncTask("/task/confirm", "完成",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        task.setState(Task.TASK_STATE_COMPLETE);
                                        initData();
                                    }
                                }).execute();
                        progressDialog.setMessage("请稍后...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                    }
                });
                break;
            default:
                break;
        }
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    @Override
    public void onMyLocationChange(Location location) {
        if (location == null) {
            return;
        }
        Bundle bundle = location.getExtras();
        if (bundle == null) {
            return;
        }
        int errorCode = bundle.getInt(MyLocationStyle.ERROR_CODE);
        if (errorCode == 0) {
            LatLng newLocation = new LatLng(location.getLatitude(), location.getLongitude());
            // 第一次获取到用户位置
            if (myLocation == null) {
                // 显示目的地址和任务接收人在地图上
                LatLngBounds bounds = getLatLngBounds(target, newLocation);
                aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
                if (isShowWalkRoute) {
                    LatLonPoint startPoint = new LatLonPoint(newLocation.latitude, newLocation.longitude);
                    LatLonPoint endPoint = new LatLonPoint(target.latitude, target.longitude);
                    final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(startPoint, endPoint);
                    RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fromAndTo, RouteSearch.WalkDefault);
                    mRouteSearch.calculateWalkRouteAsyn(query);// 异步路径规划步行模式查询
                }
            }
            if (!newLocation.equals(myLocation)) {
                myLocation = newLocation;
                if (serviceController != null) {
                    serviceController.updateUserPosition(
                            String.valueOf(location.getLatitude()),
                            String.valueOf(location.getLongitude()));
                }
            }
        }
    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {

    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult result, int errorCode) {
        aMap.clear();
        if (errorCode != AMapException.CODE_AMAP_SUCCESS) {
            return;
        }
        if (result == null || result.getPaths() == null || result.getPaths().size() == 0) {
            return;
        }
        mWalkRouteResult = result;
        WalkPath walkPath = mWalkRouteResult.getPaths().get(0);
        WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(
                this, aMap, walkPath,
                mWalkRouteResult.getStartPos(),
                mWalkRouteResult.getTargetPos());
        walkRouteOverlay.removeFromMap();
        walkRouteOverlay.addToMap();
        walkRouteOverlay.zoomToSpan();
    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

    }

    class OperationAsyncTask extends AsyncTask<Void, Void, JSONObject> {

        private String url;
        private String successMsg;
        private DialogInterface.OnClickListener listener;

        public OperationAsyncTask(String url, String successMsg,
                                  DialogInterface.OnClickListener listener) {
            this.url = url;
            this.successMsg = successMsg;
            this.listener = listener;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();
            FormBody formBody = new FormBody.Builder()
                    .add("userId", String.valueOf(user.getId()))
                    .add("taskId", String.valueOf(task.getId()))
                    .build();
            Request request = new Request.Builder()
                    .post(formBody)
                    .url(App.BASE_URL + url)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String json = response.body().string();
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

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject == null) {
                Toast.makeText(TaskDetailActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    String resultStr = jsonObject.getString("result");
                    Result result = objectMapper.readValue(resultStr, Result.class);
                    if (result.isSuccess()) {
                        new AlertDialog.Builder(TaskDetailActivity.this)
                                .setTitle("结果")
                                .setMessage(successMsg)
                                .setPositiveButton("确定", listener)
                                .show();
                    } else {
                        Toast.makeText(TaskDetailActivity.this, result.getErr(),
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
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
        if (serviceConnection != null) {
            unbindService(serviceConnection);
        }
        unregisterReceiver(taskStateChangedReceiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    private LatLngBounds getLatLngBounds(LatLng... latLngs) {
        LatLngBounds.Builder b = LatLngBounds.builder();
        for (int i = 0; i < latLngs.length; i++) {
            b.include(latLngs[i]);
        }
        return b.build();
    }
}
