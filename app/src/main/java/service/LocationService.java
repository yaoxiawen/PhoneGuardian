package service;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

public class LocationService extends Service {
    LocationManager lm;
    MyLocationListener listener;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //获取位置管理器
        lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        //制定标准，获取当前最佳位置提供者
        Criteria criteria = new Criteria();
        //允许花费，如流量
        criteria.setCostAllowed(true);
        //精准度
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        //获取当前最佳位置提供者，第2个参数为从当前允许使用的中获取，保证获取到的位置提供者是可用的
        String provider = lm.getBestProvider(criteria, true);
        //设置监听者
        listener = new MyLocationListener();
        //权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //获取位置更新，
            //第1个参数为位置提供者，GPS，网络等
            //第2个参数为最短更新事件
            //第3个参数为最短更新距离
            //第4个参数是监听者
            lm.requestLocationUpdates(provider, 0, 0, listener);
        }
    }

    class MyLocationListener implements LocationListener {
        /**
         * 当监听到位置改变时调用
         *
         * @param location 位置
         */
        @Override
        public void onLocationChanged(Location location) {
            SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
            sp.edit().putString("location", "longitude:" + location.getLongitude()
                    + ",latitude:" + location.getLatitude()).apply();
            //停止服务，获取到一次位置就停止服务
            stopSelf();
        }

        /**
         * 当位置提供者的状态发生改变时调用
         *
         * @param s
         * @param i
         * @param bundle
         */
        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        /**
         * 当位置提供者开启时调用
         *
         * @param s
         */
        @Override
        public void onProviderEnabled(String s) {

        }

        /**
         * 当位置提供者关闭时调用
         *
         * @param s
         */
        @Override
        public void onProviderDisabled(String s) {

        }
    }

    @Override
    public void onDestroy() {
        //服务销毁时移除监听者
        lm.removeUpdates(listener);
        super.onDestroy();
    }
}
