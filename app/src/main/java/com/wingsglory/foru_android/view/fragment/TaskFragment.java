package com.wingsglory.foru_android.view.fragment;

import android.Manifest;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wingsglory.foru_android.Const;
import com.wingsglory.foru_android.Globle;
import com.wingsglory.foru_android.R;
import com.wingsglory.foru_android.model.Result;
import com.wingsglory.foru_android.model.TaskDTO;
import com.wingsglory.foru_android.model.User;
import com.wingsglory.foru_android.task.DownloadImageAsyncTask;
import com.wingsglory.foru_android.util.AMapUtil;
import com.wingsglory.foru_android.util.HttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by hezhujun on 2017/6/30.
 */
public class TaskFragment extends Fragment implements AMap.OnMyLocationChangeListener, AMap.OnCameraChangeListener, AMap.OnMapTouchListener, AMap.OnMarkerClickListener {
    private static final String TAG = "TaskFragment";

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
    private LatLng myLocation = new LatLng(116.480724,39.989584); // 默认是北京
    private boolean isGetMyLocation;
    private boolean isGetTaskList = false;

    private Set<TaskDTO> taskSet = new HashSet<>();
    private User user;

    public static TaskFragment newInstance(User user, Set<TaskDTO> taskPublished) {
        TaskFragment taskFragment = new TaskFragment();
        taskFragment.user = user;
        taskFragment.taskSet = taskPublished;
        return taskFragment;
    }

    public TaskFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 请求获取地理位置权限
        PackageManager pm = getActivity().getPackageManager();
        // Here, thisActivity is the current activity
        if (pm.checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, "com.wingsglory.foru_android")
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    Toast.makeText(getActivity(), "需要开启定位权限", Toast.LENGTH_SHORT).show();
                } else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
                }
            }
        }

        new TaskListAsyncTask(Globle.user.getId(), myLocation.latitude, myLocation.longitude).execute();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task, container, false);
        taskMapInfoView = view.findViewById(R.id.task_map_info_view);
        taskTitleView = (TextView) view.findViewById(R.id.task_title);
        taskRewardView = (TextView) view.findViewById(R.id.task_reward);
        taskContentView = (TextView) view.findViewById(R.id.task_content);
        userImageView = (ImageView) view.findViewById(R.id.user_image);
        usernameView = (TextView) view.findViewById(R.id.user_name);
        positionDistanceView = (TextView) view.findViewById(R.id.position_distance);

        mMapView = (MapView) view.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        aMap = mMapView.getMap();
        myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW);
        myLocationStyle.showMyLocation(true);
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.my_local_image));
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setMyLocationEnabled(true);
        aMap.setOnMyLocationChangeListener(this);
        aMap.setOnCameraChangeListener(this);
        aMap.setOnMapTouchListener(this);
        aMap.setOnMarkerClickListener(this);
        aMap.moveCamera(CameraUpdateFactory.zoomTo(14.0f));
        return view;
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
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
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
            Log.e("amap", "onMyLocationChange 定位成功， lat: " + location.getLatitude() + " lon: " + location.getLongitude());
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
                Log.e("amap", "定位信息， code: " + errorCode + " errorInfo: " + errorInfo + " locationType: " + locationType);
            } else {
                Log.e("amap", "定位信息， bundle is null ");
            }
        } else {
            Log.e("amap", "定位失败");
        }
        if (!before && isGetMyLocation) {
            // 第一次获取我的位置成功后重新获取task列表
            new TaskListAsyncTask(Globle.user.getId(), myLocation.latitude, myLocation.longitude).execute();
        }
    }

    public void updateMapTask() {
        for (TaskDTO task :
                taskSet) {
            LatLng position = AMapUtil.parseLatLng(task.getTaskContent().getLatitude(), task.getTaskContent().getLongitude());
            Marker marker = aMap.addMarker(new MarkerOptions().position(position).title(String.valueOf(task.getTask().getId())).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker1)));
            marker.setInfoWindowEnable(false);
        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {

    }

    @Override
    public void onTouch(MotionEvent motionEvent) {
        if (myLocationStyle.getMyLocationType() == MyLocationStyle.LOCATION_TYPE_FOLLOW) {
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW);
            aMap.setMyLocationStyle(myLocationStyle);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        taskMapInfoView.setVisibility(View.VISIBLE);
        int taskId = Integer.parseInt(marker.getTitle());
        TaskDTO task = null;
        for (TaskDTO t :
                taskSet) {
            if (t.getTask().getId().equals(taskId)) {
                task = t;
                break;
            }
        }
        if (task != null) {
            taskTitleView.setText(task.getTaskContent().getTitle());
            taskContentView.setText(task.getTaskContent().getContent());
            taskRewardView.setText("赏" + task.getTaskContent().getReward().toString() + "元");
            userImageView.setImageBitmap(task.getImage().getBitmap());
            usernameView.setText(task.getPublisher());
            float distance = AMapUtils.calculateLineDistance(myLocation,
                    AMapUtil.parseLatLng(task.getTaskContent().getLatitude(), task.getTaskContent().getLongitude()));
            positionDistanceView.setText(String.format("%.2f", distance) + "m");
        }
        return true;
    }

    class TaskListAsyncTask extends AsyncTask<Void, Void, List<TaskDTO>> {

        private Integer userId;
        private double latitude;
        private double longitude;

        public TaskListAsyncTask(Integer userId, double latitude, double longitude) {
            this.userId = userId;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        protected void onPostExecute(List<TaskDTO> taskDTOs) {
            if (taskDTOs != null && taskDTOs.size() > 0) {
                for (TaskDTO task :
                        taskDTOs) {
                    taskSet.add(task);
                    new DownloadImageAsyncTask(task).execute();
                }
                updateMapTask();
            }
        }

        @Override
        protected List<TaskDTO> doInBackground(Void... params) {
            try {
                HttpUtil.Param param = new HttpUtil.Param();
                param.put("userId", String.valueOf(userId));
                param.put("latitude", AMapUtil.parseLatLngToString(latitude));
                param.put("longitude", AMapUtil.parseLatLngToString(longitude));
                HttpUtil.Header header = new HttpUtil.Header();
                header.put("Content-Type", "application/x-www-form-urlencoded");
                String json = HttpUtil.post(new URL(Const.BASE_URL + "/task/published"), header, param);
                Log.d(TAG, "task list return:" + json);
                JSONObject jsonObject = new JSONObject(json);
                String res = jsonObject.getString("result");
                ObjectMapper mapper = new ObjectMapper();
                Result result = mapper.readValue(res, Result.class);
                if (result.isSuccess()) {
                    String tasksStr = jsonObject.getString("tasks");
                    List<TaskDTO> taskList = mapper.readValue(tasksStr, new TypeReference<List<TaskDTO>>() {
                    });
                    return taskList;
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
    }
}
