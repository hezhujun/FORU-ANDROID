package com.wingsglory.foru_android.view.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.district.DistrictItem;
import com.amap.api.services.district.DistrictResult;
import com.amap.api.services.district.DistrictSearch;
import com.amap.api.services.district.DistrictSearchQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.wingsglory.foru_android.R;
import com.wingsglory.foru_android.model.Position;
import com.wingsglory.foru_android.util.AMapUtil;
import com.wingsglory.foru_android.util.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PositionActivity extends BaseActivity implements AdapterView.OnItemSelectedListener, DistrictSearch.OnDistrictSearchListener, AMap.OnCameraChangeListener, AMap.OnMyLocationChangeListener, GeocodeSearch.OnGeocodeSearchListener, AMap.OnMapTouchListener {

    /**
     * @param context
     * @param hasPosition 是否显示特定位置 false：lat lng不处理
     * @param lat
     * @param lng
     * @return
     */
    public static Intent startActivity(Context context, boolean hasPosition, double lat, double lng) {
        Intent intent = new Intent(context, PositionActivity.class);
        intent.putExtra("hasPosition", hasPosition);
        if (hasPosition) {
            intent.putExtra("lat", lat);
            intent.putExtra("lng", lng);
        }
        return intent;
    }

    public static final int RETURN_POSITION_SUCCESS = 0;
    public static final int RETURN_POSITION_ERROR = -1;


    public static final String COUNTRY = "country"; // 行政区划，国家级

    public static final String PROVINCE = "province"; // 行政区划，省级

    public static final String CITY = "city"; // 行政区划，市级

    public static final String DISTRICT = "district"; // 行政区划，区级

    public static final String BUSINESS = "biz_area"; // 行政区划，商圈级

    //当前选中的级别
    private String selectedLevel = COUNTRY;

    // 当前行政区划
    private com.amap.api.services.district.DistrictItem currentDistrictItem = null;

    // 下级行政区划集合
    private Map<String, List<DistrictItem>> subDistrictMap = new HashMap<String, List<DistrictItem>>();

    // 省级列表
    private List<DistrictItem> provinceList = new ArrayList<DistrictItem>();

    // 市级列表
    private List<DistrictItem> cityList = new ArrayList<DistrictItem>();

    // 区县级列表
    private List<DistrictItem> districtList = new ArrayList<DistrictItem>();

    // 是否已经初始化
    private boolean isInit = false;

    private Spinner spinnerProvince;
    private Spinner spinnerCity;
    private Spinner spinnerDistrict;

    private boolean isSpinnerProvinceInit = false;
    private boolean isSpinnerCityInitInit = false;
    private boolean isSpinnerDistrictInit = false;

    private boolean isShowPosition;
    private LatLng showPosition;
    private int myLocationUpdateCount = 0; // 我的位置更新次数
    private LatLng myLocation;
    private boolean isAllInit = false;

    private MapView mMapView;
    private AMap aMap;
    private MyLocationStyle myLocationStyle;
    private boolean showMyLocation = false;
    private Marker marker;
    private GeocodeSearch geocoderSearch;

    private TextView positionView;
    private Position position = new Position();
    private int geocoderBefore = 0; // 记录地位位置逆编码的查询次数
    private int geocoderAfter = 0; // 记录逆编码查询结果数

    private View sure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_position);

        // 请求获取地理位置权限
        PackageManager pm = getPackageManager();
        if (pm.checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, "com.wingsglory.foru_android")
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    Toast.makeText(this, "需要开启定位权限", Toast.LENGTH_SHORT).show();
                } else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
                }
            }
        }

        Intent intent = getIntent();
        isShowPosition = intent.getBooleanExtra("hasPosition", false);
        if (isShowPosition) {
            double lat = intent.getDoubleExtra("lat", 0);
            double lng = intent.getDoubleExtra("lng", 0);
            showPosition = new LatLng(lat, lng);
        }

        spinnerProvince = (Spinner) findViewById(R.id.province_spinner);
        spinnerCity = (Spinner) findViewById(R.id.city_spinner);
        spinnerDistrict = (Spinner) findViewById(R.id.district_spinner);

        spinnerProvince.setOnItemSelectedListener(this);
        spinnerCity.setOnItemSelectedListener(this);
        spinnerDistrict.setOnItemSelectedListener(this);

        View button = findViewById(R.id.sure);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PositionActivity.this.finish();
            }
        });

        positionView = (TextView) findViewById(R.id.position);
        sure = findViewById(R.id.sure);
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goback();
                finish();
            }
        });

        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(this);
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        aMap = mMapView.getMap();
        aMap.setOnCameraChangeListener(this);
        aMap.setOnMapTouchListener(this);
        aMap.moveCamera(CameraUpdateFactory.zoomTo(16.0f));
        // 中间显示marker
        marker = aMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.my_local_image));
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW);
        myLocationStyle.interval(2000);
        myLocationStyle.showMyLocation(true); // 显示我的位置
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        aMap.setMyLocationEnabled(true);
        aMap.setOnMyLocationChangeListener(this);
        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
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

    private void init() {
        // 设置行政区划查询监听
        DistrictSearch districtSearch = new DistrictSearch(this);
        districtSearch.setOnDistrictSearchListener(this);
        // 查询中国的区划
        DistrictSearchQuery query = new DistrictSearchQuery();
        query.setKeywords("中国");
        districtSearch.setQuery(query);
        // 异步查询行政区
        districtSearch.searchDistrictAsyn();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        DistrictItem districtItem = null;
        switch (parent.getId()) {
            case R.id.province_spinner:
//                // 避免初始化导致地图中心位置转移
//                if (isSpinnerProvinceInit) {
                    districtItem = provinceList.get(position);
                    selectedLevel = PROVINCE;
//                } else {
//                    isSpinnerProvinceInit = true;
//                }
                break;
            case R.id.city_spinner:
//                if (isSpinnerCityInitInit) {
                    selectedLevel = CITY;
                    districtItem = cityList.get(position);
//                } else {
//                    isSpinnerCityInitInit = true;
//                }
                break;
            case R.id.district_spinner:
//                if (isSpinnerDistrictInit) {
                    selectedLevel = DISTRICT;
                    districtItem = districtList.get(position);
//                } else {
//                    isSpinnerDistrictInit = true;
//                }
                break;
            default:
                break;
        }

        if (districtItem != null) {
            currentDistrictItem = districtItem;
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    AMapUtil.convertToLatLng(districtItem.getCenter()), 15));
            // 先查缓存如果缓存存在则直接从缓存中查找，无需再执行查询请求
            List<DistrictItem> cache = subDistrictMap.get(districtItem
                    .getAdcode());
            if (null != cache) {
                setSpinnerView(cache);
            } else {
                querySubDistrict(districtItem);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onDistrictSearched(DistrictResult result) {
        List<DistrictItem> subDistrictList = null;
        if (result != null) {
            if (result.getAMapException().getErrorCode() == AMapException.CODE_AMAP_SUCCESS) {

                List<DistrictItem> district = result.getDistrict();

                if (!isInit) {
                    isInit = true;
                    currentDistrictItem = district.get(0);
                }

                // 将查询得到的区划的下级区划写入缓存
                for (int i = 0; i < district.size(); i++) {
                    DistrictItem districtItem = district.get(i);
                    subDistrictMap.put(districtItem.getAdcode(),
                            districtItem.getSubDistrict());
                }
                // 获取当前区划的下级区划列表
                subDistrictList = subDistrictMap
                        .get(currentDistrictItem.getAdcode());
            } else {
                ToastUtil.showerror(this, result.getAMapException().getErrorCode());
            }
        }
        setSpinnerView(subDistrictList);
    }

    // 设置spinner视图
    private void setSpinnerView(List<DistrictItem> subDistrictList) {
        List<String> nameList = new ArrayList<String>();
        if (subDistrictList != null && subDistrictList.size() > 0) {
            for (int i = 0; i < subDistrictList.size(); i++) {
                nameList.add(subDistrictList.get(i).getName());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, nameList);

            if (selectedLevel.equalsIgnoreCase(COUNTRY)) {
                provinceList = subDistrictList;
                spinnerProvince.setAdapter(adapter);
            }

            if (selectedLevel
                    .equalsIgnoreCase(PROVINCE)) {
                cityList = subDistrictList;
                spinnerCity.setAdapter(adapter);
            }

            if (selectedLevel.equalsIgnoreCase(CITY)) {
                districtList = subDistrictList;
                //如果没有区县，将区县说明置空
                if (null == nameList || nameList.size() <= 0) {

                }
                spinnerDistrict.setAdapter(adapter);
            }
        } else {
            List<String> emptyNameList = new ArrayList<String>();
            ArrayAdapter<String> emptyAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, emptyNameList);
            if (selectedLevel.equalsIgnoreCase(COUNTRY)) {

                spinnerProvince.setAdapter(emptyAdapter);
                spinnerCity.setAdapter(emptyAdapter);
                spinnerDistrict.setAdapter(emptyAdapter);
            }

            if (selectedLevel
                    .equalsIgnoreCase(PROVINCE)) {

                spinnerCity.setAdapter(emptyAdapter);
                spinnerDistrict.setAdapter(emptyAdapter);
            }

            if (selectedLevel
                    .equalsIgnoreCase(CITY)) {
                spinnerDistrict.setAdapter(emptyAdapter);
            }
        }
    }

    /**
     * 查询下级区划
     *
     * @param districtItem 要查询的区划对象
     */
    private void querySubDistrict(DistrictItem districtItem) {
        DistrictSearch districtSearch = new DistrictSearch(PositionActivity.this);
        districtSearch.setOnDistrictSearchListener(PositionActivity.this);
        // 异步查询行政区
        DistrictSearchQuery query = new DistrictSearchQuery();
        query.setKeywords(districtItem.getName());
        districtSearch.setQuery(query);
        districtSearch.searchDistrictAsyn();
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        LatLng latLng = cameraPosition.target;
        getAddress(new LatLonPoint(latLng.latitude, latLng.longitude));
        // 固定marker
        Point point = toScreenLocation(latLng);
        marker.setPositionByPixels(point.x, point.y);
    }

    /**
     * 响应逆地理编码
     */
    public void getAddress(final LatLonPoint latLonPoint) {
        position.setLng(latLonPoint.getLongitude());
        position.setLat(latLonPoint.getLatitude());
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,
                GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        geocoderBefore++;
        geocoderSearch.getFromLocationAsyn(query);// 设置异步逆地理编码请求
    }

    private Point toScreenLocation(LatLng latLng) {
        LatLng latlng = new LatLng(latLng.latitude, latLng.longitude);
        Point point = aMap.getProjection().toScreenLocation(latlng);
        return point;
    }

    @Override
    public void onBackPressed() {
        goback();
        super.onBackPressed();
    }

    private void goback() {
        // 确认地址和经纬度是一致的
        if (geocoderBefore != geocoderAfter) {
            setResult(RETURN_POSITION_ERROR);
        } else {
            Intent intent = new Intent();
            intent.putExtra("position", position);
            setResult(RETURN_POSITION_SUCCESS, intent);
        }
    }

    @Override
    public void onMyLocationChange(Location location) {
        if (location != null) {
            Log.e("amap", "onMyLocationChange 定位成功， lat: " + location.getLatitude() + " lon: " + location.getLongitude());
            Bundle bundle = location.getExtras();
            if (bundle != null) {
                int errorCode = bundle.getInt(MyLocationStyle.ERROR_CODE);
                if (errorCode == 0) {
                    myLocation = new LatLng(location.getLatitude(), location.getLongitude());

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
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
        geocoderAfter++;
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getRegeocodeAddress() != null
                    && result.getRegeocodeAddress().getFormatAddress() != null) {
                position.setPosition(result.getRegeocodeAddress().getFormatAddress());
                LatLonPoint point = result.getRegeocodeQuery().getPoint();
                String address = result.getRegeocodeAddress().getFormatAddress();
                positionView.setText(address);
            }
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    @Override
    public void onTouch(MotionEvent motionEvent) {
        if (!isAllInit) {
            if (isShowPosition) {
                aMap.moveCamera(CameraUpdateFactory.changeLatLng(showPosition));
            }
            // 取消不断定位到我的位置
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW);
            aMap.setMyLocationStyle(myLocationStyle);
            isAllInit = true;
        }

    }
}
