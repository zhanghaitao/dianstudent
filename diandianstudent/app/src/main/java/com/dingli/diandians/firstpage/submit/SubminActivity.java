package com.dingli.diandians.firstpage.submit;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.dingli.diandians.R;
import com.dingli.diandians.bean.SysInfoUtil;
import com.dingli.diandians.common.BaseActivity;
import com.dingli.diandians.common.CheatDialog;
import com.dingli.diandians.common.Constant;
import com.dingli.diandians.common.CozyDialog;
import com.dingli.diandians.common.DianApplication;
import com.dingli.diandians.common.DianTool;
import com.dingli.diandians.common.GoSetDialog;
import com.dingli.diandians.common.HostAdress;
import com.dingli.diandians.common.Initoken;
import com.dingli.diandians.common.Result;
import com.dingli.diandians.firstpage.FirstPageFragment;
import com.dingli.diandians.numbertest.NumberTestActivity;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.HttpHeaders;
import com.lzy.okgo.model.HttpParams;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by dingliyuangong on 2016/11/23.
 */
public class SubminActivity extends BaseActivity implements View.OnClickListener,LocationSource,AMapLocationListener {
    private LinearLayout titlocation;
    private TextView tvlocation,qiandaokechen;
    private Button btkaishi;
    private MapView map;
    private AMap aMap;
    int zizeng;
    int zizengs;
    int firstone;
    private LocationSource.OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    int keid;
    double longitu;
    double laitu;
    String addressName;
    String gpstype;
    String rollcalltype;
    String intoken;
    Marker marker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        map = (MapView) findViewById(R.id.map);
        map.onCreate(savedInstanceState);
        initView();
    }
    private void initView() {
        String coursename=getIntent().getStringExtra(Constant.COURSENAME);
        intoken=getIntent().getStringExtra("intoken");
        keid=getIntent().getIntExtra(Constant.KE_ID,0);
        rollcalltype=getIntent().getStringExtra(Constant.ROLLCALLTYPE);
        ImageView locationback = (ImageView) findViewById(R.id.locationback);
        titlocation = (LinearLayout) findViewById(R.id.titlocation);
        tvlocation = (TextView) findViewById(R.id.tvlocation);
        qiandaokechen=(TextView)findViewById(R.id.qiandaokechen);
        qiandaokechen.setText("签到课程:" + coursename);
        btkaishi = (Button) findViewById(R.id.btkaishi);
        btkaishi.setOnClickListener(this);
        locationback.setOnClickListener(this);
        if (aMap==null){
            aMap=map.getMap();
            setUpMap();
        }
        String type=DianApplication.sharedPreferences.getStringValue(Constant.NETWORKTYPE);
        if (!TextUtils.isEmpty(type)){
                if (type.equals("4G")) {
                    if (TextUtils.isEmpty(DianApplication.sharedPreferences.getStringValue("wifi"))) {
                    CozyDialog dialog = new CozyDialog(this, new CozyDialog.SelectDialogButtonListener() {
                        @Override
                        public void checkButton(int id) {
                        }
                    });
                    dialog.show();
                   DianApplication.sharedPreferences.saveString("wifi","wifi");
                }
            }
        }
    }
    @TargetApi(Build.VERSION_CODES.M)
    void setUpMap(){
        zizengs++;
        if (zizengs==1) {
            if (DianTool.getsdkbanbe()>22){
                if (DianTool.getsdkbanbe()>22){
                    int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
                    if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                100);
                        return;
                    }
                }
            }
        }
        aMap.setLocationSource(this);// 设置定位监听
//        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btkaishi:
                if (DianTool.isConnectionNetWork(this)) {
                    if (laitu == 0.0) {
                        DianTool.showTextToast(SubminActivity.this, "请开启应用定位权限及GPS定位");
                    } else if (TextUtils.isEmpty(addressName)) {
                        DianTool.showTextToast(SubminActivity.this, "请开启应用定位权限及GPS定位");
                    } else {
                        if (!TextUtils.isEmpty(addressName)) {
                                btkaishi.setClickable(false);
                                submin();
                        } else {
                            btkaishi.setClickable(true);
                            DianTool.showTextToast(this, "定位成功后,才能签到");
                        }
                    }
                }else{
                    DianTool.showTextToast(this,"请检查网络");
                }
                break;
            case R.id.locationback:
                finish();
                overridePendingTransition(R.anim.activity_pop_enter,R.anim.activity_pop_exit);
                break;
        }
    }
    void drawMarkers(double longitu,double laitu,String addressName){
             LatLng  latLng=new LatLng(laitu,longitu);
           if (marker!=null){
               marker.remove();
           }
             marker=aMap.addMarker(new MarkerOptions()
             .position(latLng));
             aMap.moveCamera(CameraUpdateFactory.zoomTo(16));
             tvlocation.setText(addressName);
             marker.showInfoWindow();
    }
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null && aMapLocation != null) {
            if (aMapLocation != null
                    && aMapLocation.getErrorCode() == 0) {
                if (!TextUtils.isEmpty(DianApplication.user.strNetworkType)) {
                    DianApplication.user.gpstype = DianApplication.user.strNetworkType;
                }else {
                    if (aMapLocation.getLocationType() == 5) {
                        gpstype = "wifi";
                        DianApplication.user.gpstype = "wifi";
                    } else if (aMapLocation.getLocationType() == 1) {
                        gpstype = "gps";
                        DianApplication.user.gpstype = "gps";
                    } else if (aMapLocation.getLocationType() == 6) {
                        gpstype = "4G";
                        DianApplication.user.gpstype = "4G";
                    }
                }
                mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
               longitu=aMapLocation.getLongitude();
                laitu=aMapLocation.getLatitude();
                addressName= aMapLocation.getAddress();
                drawMarkers(longitu,laitu,addressName);// 添加10个带有系统默认icon的marker
            } else {
                if (SysInfoUtil.mayOnEmulator(this)==false) {
                    if (DianTool.isConnectionNetWork(SubminActivity.this)) {
                        firstone++;
                        if (firstone == 1) {
                            GoSetDialog dialog = new GoSetDialog(this, "请开启应用定位权限及GPS", new GoSetDialog.SelectDialogButtonListener() {
                                @Override
                                public void checkButton(int id) {
                                    switch (id) {
                                        case R.id.btnSelectDialogDetermineset:
                                            Intent intent = new Intent(Settings.ACTION_SETTINGS);
                                            startActivity(intent);
                                            break;
                                    }
                                }
                            });
                            dialog.show();
                        }
                    }
                }else{
                    DianTool.showTextToast(this,"请关闭模拟器,在真机上签到");
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 100) {
            if (permissions[0].equals(Manifest.permission.ACCESS_COARSE_LOCATION)
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setUpMap();
                map.onResume();
            }else{
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setMessage("该应用需要赋予访问定位的权限，不开启将无法正常工作！")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                 dialog.dismiss();
                                    setUpMap();
                                  map.onResume();
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create();
                    dialog.show();
                    return;
                }
            }
        }
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mLocationOption.setInterval(2000);

            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
            //在activity中启动自定义本地服务LocationService
        }
    }
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }
    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
        deactivate();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        map.onSaveInstanceState(outState);
    }
    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        map.onDestroy();
        OkGo.getInstance().cancelTag(this);
    }
    void submin(){
        DianApplication.user.token=DianApplication.sharedPreferences.getStringValue(Constant.SPLITONE)+"-"+
                DianApplication.sharedPreferences.getStringValue(Constant.SPLITTWO)+"-"+DianApplication.sharedPreferences.getStringValue(Constant.SPLITTHREE)
                +"-"+DianApplication.sharedPreferences.getStringValue(Constant.SPLITFOUR)+"-"+DianApplication.sharedPreferences.getStringValue(Constant.SPLITFIVE);
        if (DianTool.isConnectionNetWork(this)){
            MobclickAgent.onEvent(this,"signLocation");
            DianTool.showMyDialog(this,"");
            JSONObject jsonObject=new JSONObject();
            HttpHeaders headers=new HttpHeaders();
            headers.put("Authorization", DianApplication.user.token_type + "" + DianApplication.user.token);
            try{
                jsonObject.put("authCode", "");
                jsonObject.put("gps",laitu+"-"+longitu);
                jsonObject.put("gpsDetail",addressName);
                jsonObject.put("gpsType",DianApplication.user.gpstype);
                jsonObject.put("rollCallType", rollcalltype);
                jsonObject.put("scheduleId", keid);
                jsonObject.put("deviceToken",DianApplication.sharedPreferences.getStringValue(Constant.DEVICEID));
            }catch (Exception e){
                e.printStackTrace();
            }
           OkGo.post(HostAdress.getRequest("/api/phone/v1/student/signIn")).tag(this)
                   .headers(headers).upJson(jsonObject).execute(new StringCallback() {
               @Override
               public void onSuccess(String s, Call call, Response response) {
                   DianTool.dissMyDialog();
                   btkaishi.setClickable(true);
                   if (!TextUtils.isEmpty(s)) {
                       if(!s.equals("{}")) {
                           Result result = JSON.parseObject(s, Result.class);
                           if (result.success == false) {
                               if (result.code.equals("40025004")) {
                                   DianTool.showTextToast(SubminActivity.this, "老师已修改您的考勤,不能再次签到。请联系老师 !");
                               } else if (result.code.equals("40025005")) {
                                   DianTool.showTextToast(SubminActivity.this, "老师将您的考勤修改为请假,所以不能签到");
                               } else if (result.code.equals("40025007")) {
                                   DianTool.showTextToast(SubminActivity.this, "老师没有开启点名");
                               } else if (result.code.equals("40025006")) {
                                   CheatDialog dialog = new CheatDialog(SubminActivity.this, result.message, new CheatDialog.SelectDialogButtonListener() {
                                       @Override
                                       public void checkButton(int id) {

                                       }
                                   });
                                   dialog.show();
                               } else if (result.code.equals("40025008")) {
                                   DianTool.showTextToast(SubminActivity.this, "老师更改了点名方式");
                                   Initoken.signId(SubminActivity.this);
                               } else if (result.code.equals("40025009")) {
                                   DianTool.showTextToast(SubminActivity.this, "老师已关闭该点名");
                               }
                           } else {
                               DianTool.showTextToast(SubminActivity.this, "提交成功");
                               if (TextUtils.isEmpty(intoken)) {
                                   SubminActivity.this.finish();
                               } else {
                                   Intent intent = new Intent(SubminActivity.this, FirstPageFragment.class);
                                   startActivity(intent);
                                   SubminActivity.this.finish();
                               }
                               overridePendingTransition(R.anim.activity_pop_enter, R.anim.activity_pop_exit);
                           }
                       }
                   }
               }
               @Override
               public void onError(Call call, Response response, Exception e) {
                   DianTool.dissMyDialog();
                   btkaishi.setClickable(true);
                   DianTool.response(response,SubminActivity.this);
               }
           });
        }else{
            btkaishi.setClickable(true);
            DianTool.showTextToast(this, "请检查网络");
        }
    }
}
