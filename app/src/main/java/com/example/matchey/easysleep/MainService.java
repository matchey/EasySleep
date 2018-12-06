package com.example.matchey.easysleep;

import android.app.admin.DevicePolicyManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
//import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
//import android.view.GestureDetector.OnDoubleTapListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.example.matchey.easysleep.MainActivity.TAG;

public class MainService extends Service implements View.OnTouchListener//, OnDoubleTapListener
{
    private View mView;

    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mComponentName;

    private GestureDetector gestureDetector;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);

        int LAYOUT_FLAG;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else{
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        } // WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                0,
                0,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.OPAQUE);

        mView = new View(this);
        mView.setOnTouchListener(this);

        wm.addView(mView, params);

        mDevicePolicyManager = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
        mComponentName = new ComponentName(this, Admin.class);

//        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener());
        gestureDetector = new GestureDetector(this, onGestureListener);
        gestureDetector.setOnDoubleTapListener(onGestureListener);

        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        wm.removeView(mView);
        mView = null;
    }

    private final GestureDetector.SimpleOnGestureListener onGestureListener = new GestureDetector.SimpleOnGestureListener() {
    @Override
    public boolean onDoubleTap(MotionEvent e)
    {
        Log.d(TAG, "event: " + e);

        if(e.getAction() != MotionEvent.ACTION_OUTSIDE){
            return false;
        }

        String packageName = getTopActivityPackageName();
        boolean matched = packageName.equals("com.google.android.apps.nexuslauncher")
            || packageName.equals("android");

        Log.d(TAG, "matched: " + matched);

        if(!matched){
            return false;
        }

        if(mDevicePolicyManager.isAdminActive(mComponentName)){
            mDevicePolicyManager.lockNow();
        }else{
            Log.d("LockScreen", "admin not active");
            Toast.makeText(getApplicationContext(), "Launch from " + packageName, Toast.LENGTH_SHORT).show();
        }

        return false;
    }

//    @Override
//    public boolean onDoubleTapEvent(MotionEvent e) { Log.d(TAG, "onDoubleTapEvent"); return false; }
//
//    @Override
//    public boolean onSingleTapConfirmed(MotionEvent e) { Log.d(TAG, "onSingleTapConfirmed"); return false; }
    };

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        gestureDetector.onTouchEvent(event);

        Log.d(TAG, "onTouch event: " + event);

        return false;
    }

    // @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private String getTopActivityPackageName()
    {
        String packageName = "";
        UsageStatsManager usm = (UsageStatsManager)getSystemService(Context.USAGE_STATS_SERVICE);
        long endTime = System.currentTimeMillis();
        long beginTime = endTime - 7 * 24 * 60 * 60 * 1000;
        List<UsageStats> list = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, beginTime, endTime);
        if(list != null && list.size() > 0){
            SortedMap<Long, UsageStats> map = new TreeMap<>();
            for(UsageStats usageStats : list){
                map.put(usageStats.getLastTimeUsed(), usageStats);
                Log.d(TAG, "package: " + usageStats.getPackageName());
            }
            Log.d(TAG, "size: " + map.size());
            if(!map.isEmpty()){
                packageName = map.get(map.lastKey()).getPackageName();
            }
        }
        Log.d(TAG, "Current packageName: " + packageName);

        return packageName;
    }
}

