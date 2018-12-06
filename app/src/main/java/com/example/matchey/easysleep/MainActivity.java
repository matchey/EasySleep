package com.example.matchey.easysleep;

// import android.annotation.TargetApi;
import android.app.admin.DevicePolicyManager;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
// import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
{
    public static final String TAG = "MCNK";
    private static final int REQUEST_SETTINGS = 1;

    private static final int ADMIN_INTENT = 1;
    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mComponentName;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDevicePolicyManager = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
        mComponentName = new ComponentName(this, Admin.class);

        startService();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ADMIN_INTENT){
            if(resultCode == RESULT_OK){
                Toast.makeText(getApplicationContext(), "Registered As Admin", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext(), "Failed to register as Admin", Toast.LENGTH_SHORT).show();
            }
            startService();
        }else if(requestCode == REQUEST_SETTINGS){
            startService();
        }
    }

    // @TargetApi(Build.VERSION_CODES.M)
    private void startService()
    {
        Intent intent = null;
        if(!canGetUsageStats()){
            intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        }else if(!Settings.canDrawOverlays(getApplicationContext())){
            Uri uri = Uri.parse("package:" + getPackageName());
            intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri);
        }else if(!mDevicePolicyManager.isAdminActive(mComponentName)){
            intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mComponentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Administrator description");
        }

        if(intent != null){
            startActivityForResult(intent, REQUEST_SETTINGS);
            Toast.makeText(getApplicationContext(), "Please turn ON", Toast.LENGTH_SHORT).show();
        }else{
            intent = new Intent(MainActivity.this, MainService.class);
            startService(intent);
        }
    }

    public boolean canGetUsageStats()
    {
        AppOpsManager aom = (AppOpsManager)getSystemService(APP_OPS_SERVICE);
        int uid = android.os.Process.myUid();
        int mode = aom.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, uid, getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
//        startService();
        Log.d("LockScreen", "onResume");
    }
}

