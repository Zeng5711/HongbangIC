package com.hongbang.ic.activity;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.SwitchCompat;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import com.hongbang.ic.IWifiKeyService;
import com.hongbang.ic.R;
import com.hongbang.ic.keycenter.WifiKeyService;
import com.hongbang.ic.util.Logger;
import com.hongbang.ic.util.SPUtils;

import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

public class SettingsActivity extends BaseActivity {

    @ViewInject(R.id.sw_screen_on)
    private SwitchCompat mScreenOnSwitch;

    @ViewInject(R.id.sw_shake)
    private SwitchCompat mShakeSwitch;

    @ViewInject(R.id.aut_conn)
    private SwitchCompat mAutConnSwitch;

    @ViewInject(R.id.sensitivity_seek)
    private SeekBar mSensitivitySeek;

    @ViewInject(R.id.sw_auto_disconnect)
    private SwitchCompat mAutoDisconnect;

    private IWifiKeyService mRemoteService = null;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mRemoteService = IWifiKeyService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setTitle("设置");

        x.view().inject(this);
        mScreenOnSwitch.setChecked((boolean) SPUtils.get(this, SPUtils.KEY_SCREEN_ON_ENABLED, true));
        mShakeSwitch.setChecked((boolean) SPUtils.get(this, SPUtils.KEY_SHAKE_ENABLED, true));
        mAutConnSwitch.setChecked((boolean) SPUtils.get(this,SPUtils.KEY_AUT_CONN,true));
        mAutoDisconnect.setChecked((boolean) SPUtils.get(this, SPUtils.KEY_AUTO_DISCONNECT, false));
        mSensitivitySeek.setProgress((int)SPUtils.get(this, SPUtils.KEY_WIFI_SENSITIVITY, 100));
        mSensitivitySeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SPUtils.put(SettingsActivity.this, SPUtils.KEY_WIFI_SENSITIVITY, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Intent intent = new Intent(this, WifiKeyService.class);
        bindService(intent, connection, Service.BIND_ABOVE_CLIENT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    @Event(value = R.id.sw_screen_on,
            type = CompoundButton.OnCheckedChangeListener.class)
    private void setScreenOnEnabled(CompoundButton compoundButton, boolean b) {
        if (!compoundButton.isPressed()) {
            return;
        }
        SPUtils.put(this, SPUtils.KEY_SCREEN_ON_ENABLED, b);
        try {
            if (mRemoteService != null) {
                mRemoteService.setScreenOnEnabled(b);

            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Event(value = R.id.sw_shake,
            type = CompoundButton.OnCheckedChangeListener.class)
    private void setShakeEnabled(CompoundButton compoundButton, boolean b) {
        if (!compoundButton.isPressed()) {
            return;
        }
        SPUtils.put(this, SPUtils.KEY_SHAKE_ENABLED, b);
        try {
            if (mRemoteService != null) {
                mRemoteService.setShakeEnabled(b);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    @Event(value = R.id.aut_conn,
            type = CompoundButton.OnCheckedChangeListener.class)
    private void setmAutConnSwitch(CompoundButton compoundButton, boolean b){
        if (!compoundButton.isPressed()) {
            return;
        }
        SPUtils.put(SettingsActivity.this, SPUtils.KEY_AUT_CONN, b);
        try {
            if (mRemoteService != null) {
                mRemoteService.setAutoConn(b);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    @Event(value = R.id.sw_auto_disconnect,
            type = CompoundButton.OnCheckedChangeListener.class)
    private void setAutoDisconnect(CompoundButton compoundButton, boolean b) {
        if (!compoundButton.isPressed()) {
            return;
        }
        SPUtils.put(this, SPUtils.KEY_AUTO_DISCONNECT, b);
        try {
            if (mRemoteService != null) {
                mRemoteService.setShakeEnabled(b);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
