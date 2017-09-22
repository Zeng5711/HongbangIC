package com.hongbang.ic.activity;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hongbang.ic.R;
import com.hongbang.ic.common.UserDefaults;
import com.hongbang.ic.fragment.AroundFragment;
import com.hongbang.ic.fragment.BaseMainFragment;
import com.hongbang.ic.fragment.BusinessFragment;
import com.hongbang.ic.fragment.CommunityFragment;
import com.hongbang.ic.fragment.KeyFragment;
import com.hongbang.ic.fragment.PersonalFragment;
import com.hongbang.ic.fragment.PropertyPersonalFragment;
import com.hongbang.ic.keycenter.WifiKeyService;
import com.hongbang.ic.model.MainData;

import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends BaseActivity implements Observer, SensorEventListener {

    private static final int[] TAB_ID_LIST = {
            R.id.tab_community,
            R.id.tab_around,
            R.id.tab_key,
            R.id.tab_business,
            R.id.tab_personal,
    };

    private BaseMainFragment mCommunityFragment;
    private BaseMainFragment mAroundFragment;
    private BaseMainFragment mKeyFragment;
    private BaseMainFragment mBusinessFragment;
    private BaseMainFragment mPersonalFragment;

    private BaseMainFragment mFrontFragment;

    @ViewInject(R.id.register_count_area)
    private View mRegisterCountArea;

    @ViewInject(R.id.register_count_view)
    private TextView mRegisterCountView;


    private final int START_SHAKE = 1;
    private final int START_OK = 2;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private boolean isShake = false;
    private Vibrator mVibrator;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case START_SHAKE:
                    if (mVibrator != null) {
                        mVibrator.vibrate(300);
                    }
                    break;
                case START_OK:
                    isShake = false;
                    break;
                default:
                    break;


            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main, false);

        x.view().inject(this);

        initDefaultView();

        registerWifiReceiver();

        UserDefaults.defaults().addObserver(this);

        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        startServices();
        MainActivityPermissionsDispatcher.getPermissionsWithCheck(MainActivity.this);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        starttSensor();//启动传感
    }

    private void starttSensor() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (mSensor != null) {
                mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
            }
        }
    }

    @Override
    protected void onPause() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mWifiConnectReceiver);
    }

    protected void initDefaultView() {
        setTitle(R.string.module_community);
        setTabSelected(R.id.tab_community);

        FragmentManager fm = getFragmentManager();
        // 开启Fragment事务
        FragmentTransaction transaction = fm.beginTransaction();
        mCommunityFragment = new CommunityFragment();
        mFrontFragment = mCommunityFragment;
        transaction.replace(R.id.main_content, mFrontFragment);
        transaction.commit();
//        if (UserDefaults.defaults().isPropertyUser()) {
        if (UserDefaults.defaults().getMainData() != null) {
            mRegisterCountArea.setVisibility(View.VISIBLE);
            mRegisterCountView.setText(String.valueOf(UserDefaults.defaults().getMainData().registerCount));
        }
//        }
    }

    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    protected void getPermissions() {
    }

    @OnShowRationale({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    protected void onShowRationale(PermissionRequest request) {
        showRationaleDialog("请启用定位权限，如果拒绝，将无法使用摇一摇开门和亮屏开门功能。", request);
    }


    private void showRationaleDialog(String message, final PermissionRequest request) {
        new AlertDialog.Builder(MainActivity.this)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .setMessage(message)
                .setTitle("提示")
                .show();
    }

    @Event(value = {
            R.id.tab_community,
            R.id.tab_around,
            R.id.tab_key,
            R.id.tab_business,
            R.id.tab_personal,
    }, type = View.OnClickListener.class)
    private void onTabClick(View view) {
        int resId = view.getId();
        setTabSelected(resId);
        BaseMainFragment fragment = null;
        switch (resId) {
            case R.id.tab_community:
                if (mCommunityFragment == null) {
                    mCommunityFragment = new CommunityFragment();
                }
                this.setTitle(R.string.module_community);
                fragment = mCommunityFragment;
                break;

            case R.id.tab_around:
                if (mAroundFragment == null) {
                    mAroundFragment = new AroundFragment();
                }
                this.setTitle(R.string.module_around);
                fragment = mAroundFragment;
                break;

            case R.id.tab_key:
                if (mKeyFragment == null) {
                    mKeyFragment = new KeyFragment();
                }
                this.setTitle(R.string.module_key);
                fragment = mKeyFragment;
                break;

            case R.id.tab_business:
                if (mBusinessFragment == null) {
                    mBusinessFragment = new BusinessFragment();
                }
                this.setTitle(R.string.module_business);
                fragment = mBusinessFragment;
                break;

            case R.id.tab_personal:
                if (mPersonalFragment == null) {
                    if (UserDefaults.defaults().isPropertyUser()) {
                        mPersonalFragment = new PropertyPersonalFragment();
                    } else {
                        mPersonalFragment = new PersonalFragment();
                    }
                }
                this.setTitle(R.string.module_personal);
                fragment = mPersonalFragment;
                break;
        }
        if (fragment == mFrontFragment) {
            return;
        }

        if (resId == R.id.tab_key) {
            setCustomImageButton(R.drawable.sel_btn_update_keys, mOnCustomButtonClick);
        } else {
            hideCustomTitleButton();
        }

        if (resId == R.id.tab_community) {
            mRegisterCountArea.setVisibility(View.VISIBLE);
        } else {
            mRegisterCountArea.setVisibility(View.GONE);
        }

        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.hide(mFrontFragment);
        if (fragment != null && fragment.getFragmentManager() == null) {
            transaction.add(R.id.main_content, fragment);
        } else {
            transaction.show(fragment);
        }
        transaction.commit();
        mFrontFragment = fragment;
    }

    private void setTabSelected(int selectedId) {
        for (int id : TAB_ID_LIST) {
            if (id == selectedId) {
                findViewById(id).setSelected(true);
            } else {
                findViewById(id).setSelected(false);
            }
        }
    }

    private View.OnClickListener mOnCustomButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mFrontFragment.onCustomTitleButtonClick();
        }
    };

    public void startServices() {
        Intent intent = new Intent(this, WifiKeyService.class);
        startService(intent);
    }

    /**
     * 菜单、返回键响应
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitBy2Click();
        }
        return false;
    }

    /**
     * 双击退出函数
     */
    private static Boolean isExit = false;

    private void exitBy2Click() {
        Timer tExit;
        if (!isExit) {
            isExit = true;
            Toast.makeText(this, "再次点击退出应用", Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false;
                }
            }, 2000);

        } else {
            finish();
        }
    }

    private void registerWifiReceiver() {
        IntentFilter mWifiFilter = new IntentFilter();
        mWifiFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mWifiFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(mWifiConnectReceiver, mWifiFilter);
    }

    private BroadcastReceiver mWifiConnectReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mKeyFragment != null) {
                mKeyFragment.onReceiveWifiBroadcast(intent);
            }
            if (mPersonalFragment != null) {
                mPersonalFragment.onReceiveWifiBroadcast(intent);
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(MainActivity.this, requestCode, grantResults);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg == null || !(arg instanceof MainData)) {
            return;
        }
//        if (UserDefaults.defaults().isPropertyUser()) {
        mRegisterCountView.setText(String.valueOf(UserDefaults.defaults().getMainData().registerCount));
//        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
            return;
        }

        int type = event.sensor.getType();
        if (type == Sensor.TYPE_ACCELEROMETER) {
            //获取三个方向值
            float[] values = event.values;
            float x = values[0];
            float y = values[1];
            float z = values[2];

            if ((Math.abs(x) > 17 || Math.abs(y) > 17 || Math
                    .abs(z) > 17) && !isShake) {
                isShake = true;
                // TODO: 实现摇动逻辑
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            mHandler.sendEmptyMessage(START_SHAKE);
                            Thread.sleep(500);

                            mHandler.sendEmptyMessage(START_SHAKE);
                            Thread.sleep(500);

                            mHandler.sendEmptyMessage(START_OK);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                thread.start();
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
