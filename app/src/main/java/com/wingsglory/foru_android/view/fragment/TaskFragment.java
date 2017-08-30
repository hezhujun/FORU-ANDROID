package com.wingsglory.foru_android.view.fragment;

import android.Manifest;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wingsglory.foru_android.App;
import com.wingsglory.foru_android.R;
import com.wingsglory.foru_android.model.PageBean;
import com.wingsglory.foru_android.model.Result;
import com.wingsglory.foru_android.model.Task;
import com.wingsglory.foru_android.model.TaskContent;
import com.wingsglory.foru_android.model.User;
import com.wingsglory.foru_android.util.AMapUtil;
import com.wingsglory.foru_android.util.LogUtil;
import com.wingsglory.foru_android.view.activity.TaskDetailActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by hezhujun on 2017/6/30.
 */
public class TaskFragment extends Fragment
        implements AMap.OnMyLocationChangeListener,
        AMap.OnCameraChangeListener,
        AMap.OnMapTouchListener,
        AMap.OnMarkerClickListener,
        View.OnClickListener {
    private static final String TAG = "TaskFragment";
    private static final int REQUEST_PERMISSION = 1;

    private View view;
    private View taskMapInfoView;
    private TextView taskTitleView;
    private TextView taskContentView;
    private TextView taskRewardView;
    private ImageView userImageView;
    private TextView usernameView;
    private TextView positionDistanceView;
    private MapView mMapView;
    private AMap aMap;
    private MyLocationStyle myLocationStyle;
    private LatLng myLocation = new LatLng(39.989584, 116.480724); // 默认是北京
    private boolean isGetMyLocation;
    private boolean isGetTaskList = false;

    private User user;
    private App app;
    private Map<Integer, Task> taskBuffer;
    private Task currentSelectedTask;
    private List<Marker> markerList = new ArrayList<>();

    public static TaskFragment newInstance() {
        TaskFragment taskFragment = new TaskFragment();
        return taskFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        app = (App) getActivity().getApplication();
        user = app.getUser();
        taskBuffer = app.getTaskBuffer();

        // 请求获取地理位置权限
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION:
                if (grantResults != null && grantResults.length > 0) {
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getActivity(),
                                "需要开启定位权限，不然无法实现定位功能",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
                break;
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_task, container, false);
        mMapView = (MapView) view.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        aMap = mMapView.getMap();
        myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW);
        myLocationStyle.showMyLocation(true);
        myLocationStyle.myLocationIcon(
                BitmapDescriptorFactory.fromResource(R.drawable.my_location));
        myLocationStyle.anchor(0.5f, 1f);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setMyLocationEnabled(true);
        aMap.setOnMyLocationChangeListener(this);
        aMap.setOnCameraChangeListener(this);
        aMap.setOnMapTouchListener(this);
        aMap.setOnMarkerClickListener(this);
        aMap.moveCamera(CameraUpdateFactory.zoomTo(14.0f));
        initView();
        return view;
    }

    private void initView() {
        taskMapInfoView = view.findViewById(R.id.task_map_info_view);
        taskMapInfoView.setOnClickListener(this);
        taskTitleView = (TextView) view.findViewById(R.id.task_title);
        taskRewardView = (TextView) view.findViewById(R.id.task_reward);
        taskContentView = (TextView) view.findViewById(R.id.task_content);
        userImageView = (ImageView) view.findViewById(R.id.user_image);
        usernameView = (TextView) view.findViewById(R.id.user_name);
        positionDistanceView = (TextView) view.findViewById(R.id.position_distance);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        // 获取任务列表
        LatLng center = aMap.getCameraPosition().target;
        new TaskListAsyncTask(user.getId(),
                center.latitude, center.longitude).execute();
        listTask();
    }

    /**
     * 显示任务在地图中
     */
    private void listTask() {
        List<Marker> newMarkerList = new ArrayList<>();
        for (Map.Entry<Integer, Task> entry :
                app.getTaskBuffer().entrySet()) {
            Task task = entry.getValue();
            // 任务状态是新建的
            if (Task.TASK_STATE_NEW.equals(task.getState())) {
                Date timeout = task.getContent().getTimeout();
                long diff = timeout.getTime() - System.currentTimeMillis();
                if (diff < 0) {
                    // 任务过期了
                    task.setState(Task.TASK_STATE_OVERDUE);
                } else {
                    // 任务没有过期
                    TaskContent content = task.getContent();
                    LatLng position = new LatLng(content.getLatitude().doubleValue(),
                            content.getLongitude().doubleValue());
                    // 如果已经创建了对应的marker，则不重新创建
                    // 删了再创建marker的效果不好，而且选中的marker的颜色会变回蓝色。
                    boolean isShowed = false;
                    for (Marker m :
                            markerList) {
                        if (task.getId().equals(Integer.parseInt(m.getTitle()))) {
                            m.setPosition(position);
                            isShowed = true;
                            newMarkerList.add(m);
                            break;
                        }
                    }
                    if (!isShowed) {
                        Marker marker = aMap.addMarker(
                                new MarkerOptions()
                                        .position(position)
                                        .title(String.valueOf(task.getId()))
                                        .icon(BitmapDescriptorFactory
                                                .fromResource(R.drawable.marker_blue)));
                        marker.setAnchor(0.5f, 1f);
                        marker.setInfoWindowEnable(false);
                        newMarkerList.add(marker);
                        markerList.add(marker);
                    }
                }
            }
        }
        // 删除不用的marker
        Iterator<Marker> iterator = markerList.iterator();
        while (iterator.hasNext()) {
            Marker m = iterator.next();
            if (!newMarkerList.contains(m)) {
                // 新的marker中没有包含，则删除
                iterator.remove();
            }
        }
    }

    // fragment的生命周期和activity绑在一起
    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        LogUtil.d(TAG, "----------------------------------------onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onMyLocationChange(Location location) {
        boolean before = isGetMyLocation;
        if (location != null) {
            LogUtil.e("amap", "onMyLocationChange 定位成功， lat: " +
                    location.getLatitude() + " lon: " +
                    location.getLongitude());
            Bundle bundle = location.getExtras();
            if (bundle != null) {
                int errorCode = bundle.getInt(MyLocationStyle.ERROR_CODE);
                if (errorCode == 0) {
                    myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    isGetMyLocation = true;
                }
                String errorInfo = bundle.getString(MyLocationStyle.ERROR_INFO);
                // 定位类型，可能为GPS WIFI等，具体可以参考官网的定位SDK介绍
                int locationType = bundle.getInt(MyLocationStyle.LOCATION_TYPE);
                Log.e("amap", "定位信息， code: " + errorCode +
                        " errorInfo: " + errorInfo + " locationType: " + locationType);
            } else {
                LogUtil.e("amap", "定位信息， bundle is null ");
            }
        } else {
            LogUtil.e("amap", "定位失败");
        }
        if (!before && isGetMyLocation) {
            // 第一次获取我的位置成功后重新获取task列表
            new TaskListAsyncTask(user.getId(),
                    myLocation.latitude, myLocation.longitude).execute();
        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    /**
     * 地图中心改变，刷新任务
     *
     * @param cameraPosition
     */
    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        LatLng center = cameraPosition.target;
        new TaskListAsyncTask(user.getId(), center.latitude, center.longitude).execute();
    }

    /**
     * 取消追踪到我的位置
     *
     * @param motionEvent
     */
    @Override
    public void onTouch(MotionEvent motionEvent) {
        if (myLocationStyle.getMyLocationType() == MyLocationStyle.LOCATION_TYPE_FOLLOW) {
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW);
            aMap.setMyLocationStyle(myLocationStyle);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (!markerList.contains(marker)) {
            // 我的位置的marker
            // 不需要处理
            return true;
        }
        // 改变marker的颜色
        for (Marker m :
                markerList) {
            m.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_blue));
        }
        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_red));
        // 显示任务信息
        taskMapInfoView.setVisibility(View.VISIBLE);
        System.out.println(marker.getTitle());
        Integer taskId = Integer.parseInt(marker.getTitle());
        Task task = taskBuffer.get(taskId);
        if (task != null) {
            TaskContent content = task.getContent();
            if (content != null) {
                taskTitleView.setText(content.getTitle());
                taskContentView.setText(content.getContent());
                taskRewardView.setText("赏" + content.getReward().toString() + "元");
            }
            User publisher = task.getPublisher();
            if (publisher != null) {
                if (publisher.getProtraitUrl() != null
                        && !"".equals(publisher.getProtraitUrl())) {
                    Glide.with(this).load(publisher.getProtraitUrl())
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(userImageView);
                }
                usernameView.setText(publisher.getUsername());
                float distance = AMapUtils.calculateLineDistance(myLocation,
                        new LatLng(content.getLatitude().doubleValue(),
                                content.getLongitude().doubleValue()));
                if (distance > 1000) {
                    positionDistanceView.setText(
                            String.format("%.2f", distance / 1000) + "km");
                } else {
                    positionDistanceView.setText(String.format("%.2f", distance) + "m");
                }
                currentSelectedTask = task;
            }
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.task_map_info_view:
                TaskDetailActivity.actionStart(getActivity(), currentSelectedTask);
                break;
        }
    }

    class TaskListAsyncTask extends AsyncTask<Void, Void, JSONObject> {

        private Integer userId;
        private double latitude;
        private double longitude;

        public TaskListAsyncTask(Integer userId, double latitude, double longitude) {
            this.userId = userId;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject == null) {
                Toast.makeText(getActivity(), "网络异常，获取任务失败",
                        Toast.LENGTH_SHORT).show();
            } else {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    String resultStr = jsonObject.getString("result");
                    Result result = objectMapper.readValue(resultStr, Result.class);
                    if (result.isSuccess()) {
                        String tasksStr = jsonObject.getString("tasks");
                        PageBean<Task> taskPageBean = objectMapper.readValue(tasksStr,
                                new TypeReference<PageBean<Task>>() {
                                });
                        // 把获取到的任务存放到任务缓存中
                        if (taskPageBean.size() > 0) {
                            List<Task> taskList = taskPageBean.getBeans();
                            for (Task task :
                                    taskList) {
                                app.addTask(task);
                            }
                            // 在地图上显示
                            listTask();
                        }
                    } else {
                        Toast.makeText(getActivity(), "获取任务失败\n" + result.getErr(),
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
                    .add("latitude", AMapUtil.parseLatLngToString(latitude))
                    .add("longitude", AMapUtil.parseLatLngToString(longitude))
                    .add("radius", String.valueOf(calculateScreenRadius()))
                    .add("page", String.valueOf(1))
                    .add("rows", String.valueOf(Integer.MAX_VALUE))
                    .build();
            Request request = new Request.Builder()
                    .post(formBody)
                    .url(App.BASE_URL + "/task/published")
                    .build();
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    LogUtil.d(TAG, "返回任务列表：" + json);
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

    /**
     * 计算屏幕范围半径长度对应的实际长度
     *
     * @return
     */
    private int calculateScreenRadius() {
        // 获取中心坐标
        LatLng center = aMap.getCameraPosition().target;
        // 转换成屏幕坐标
        Point screenCenter = aMap.getProjection().toScreenLocation(center);
        if (screenCenter != null) {
            // 计算边缘点，屏幕半径大概为500
            Point screenEdge = new Point(screenCenter.x, screenCenter.y + 500);
            // 边缘点转换成经纬度
            LatLng edge = aMap.getProjection().fromScreenLocation(screenEdge);
            if (edge != null) {
                int distance = (int) AMapUtils.calculateLineDistance(center, edge);
                return distance;
            }
        }
        return 10000;
    }

    public void hide() {
        if (myLocationStyle.getMyLocationType() == MyLocationStyle.LOCATION_TYPE_FOLLOW) {
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW);
            aMap.setMyLocationStyle(myLocationStyle);
        }
        mMapView.onPause();
    }

    public void show() {
        mMapView.onResume();
        if (!isGetMyLocation) {
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW);
            aMap.setMyLocationStyle(myLocationStyle);
        }
        // 获取任务列表
        LatLng center = aMap.getCameraPosition().target;
        new TaskListAsyncTask(user.getId(),
                center.latitude, center.longitude).execute();
        listTask();
    }
}
