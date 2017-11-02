package com.chenjunquan.mobilesafer.service;

import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;

/**
 * Created by Administrator on 2017/10/22.
 */

public class LocationService extends Service{
    @Override
    public void onCreate() {
        super.onCreate();
        //获取位置管理者对象
        LocationManager locationManager= (LocationManager) getSystemService(LOCATION_SERVICE);
        //以最优的方式获取经纬度坐标
        Criteria criteria=new Criteria();
        //允许花费流量
        criteria.setCostAllowed(true);
        //设置精确度
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String bestProvider = locationManager.getBestProvider(criteria, true);

        //在一定时间距离后获取经纬度坐标
        MylocationListener mylocationListener = new MylocationListener();
        locationManager.requestLocationUpdates(bestProvider,0,0,mylocationListener);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    class MylocationListener implements LocationListener{

        @Override
        public void onLocationChanged(Location location) {
            //纬度
            double latitude = location.getLatitude();
            //经度
            double longitude = location.getLongitude();
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage("5556",null,longitude+"-"+latitude,null,null);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }
}
