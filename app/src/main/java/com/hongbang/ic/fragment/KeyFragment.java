package com.hongbang.ic.fragment;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hongbang.ic.IWifiKeyService;
import com.hongbang.ic.R;
import com.hongbang.ic.activity.ShareKeyActivity;
import com.hongbang.ic.adapter.KeyListAdapter;
import com.hongbang.ic.api.SelectCallbcak;
import com.hongbang.ic.api.response.BaseResponse;
import com.hongbang.ic.api.response.DefaultResponse;
import com.hongbang.ic.common.UserDefaults;
import com.hongbang.ic.common.app;
import com.hongbang.ic.keycenter.KeyUtils;
import com.hongbang.ic.keycenter.WifiAdmin;
import com.hongbang.ic.keycenter.WifiKeyService;
import com.hongbang.ic.keycenter.tasks.AbstractTask;
import com.hongbang.ic.keycenter.tasks.OpenDoorTask;
import com.hongbang.ic.keycenter.tasks.OnTaskCallback;
import com.hongbang.ic.model.MainData;
import com.hongbang.ic.model.OneKeyInfo;
import com.hongbang.ic.model.OneKeyInfo4DB;
import com.hongbang.ic.util.Logger;
import com.hongbang.ic.util.T;

import org.xutils.common.Callback;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * 钥匙
 * <p>
 * Created by xionghf on 16/3/27.
 */
@RuntimePermissions
public class KeyFragment extends BaseMainFragment implements KeyListAdapter.OnShareKeyListener, Observer {

    @ViewInject(R.id.key_list_view)
    private RecyclerView mRecyclerView;

    @ViewInject(R.id.tip_not_authorized)
    private View mNullDataView;

    @ViewInject(R.id.btn_brush_card)
    private View mBrushCardBtn;

    private KeyListAdapter mListAdapter;

    private List<OneKeyInfo4DB> mDataList = new ArrayList<>();

    private IWifiKeyService mRemoteService = null;

    private WifiAdmin mWifiAdmin;

    private AbstractTask mCurrentTask;

    private boolean isConnected = false;

    private ArrayBlockingQueue<String> mWifiStateQueue = new ArrayBlockingQueue<>(50);

    public KeyFragment() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (KeyUtils.clearTempKeys() > 0) {
                    loadKeyData();
                }
            }
        }, 60000, 60000);
    }

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_key, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        x.view().inject(this, this.getView());

        mWifiAdmin = new WifiAdmin(getActivity());

        mListAdapter = new KeyListAdapter(mDataList);
        mListAdapter.setOnShareKeyListener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mListAdapter);
        mRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        Intent intent = new Intent(getActivity(), WifiKeyService.class);
        getActivity().bindService(intent, connection, Service.BIND_ABOVE_CLIENT);

        UserDefaults.defaults().addObserver(this);

        loadKeyData();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

//        if (!hidden) {
//            downloadKeyList();
//        }
    }

    @Override
    public void onCustomTitleButtonClick() {
        downloadKeyList();
    }

    private void downloadKeyList() {
        mActivity.showLoadingDialog("下载中...");
        app.data().getKeyList(new Callback.CommonCallback<BaseResponse<List<OneKeyInfo>>>() {
            @Override
            public void onSuccess(BaseResponse<List<OneKeyInfo>> result) {
                if (result.code == 0) {
                    List<OneKeyInfo> list = result.getData();
                    List<OneKeyInfo4DB> keyList = new ArrayList<>();
                    if (list != null) {
                        for (OneKeyInfo keyInfo : list) {
                            OneKeyInfo4DB keyInfo4DB = keyInfo.toOneKeyInfo4DB();
                            if (keyInfo4DB != null) {
                                keyList.add(keyInfo4DB);
                            }
                        }
                    }
                    KeyUtils.saveAll(keyList);
                    KeyUtils.clearTempKeys();

                    if (mRemoteService != null) {
                        try {
                            mRemoteService.reloadKeyData();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                    app.data().clearKeyList(new CommonCallback<DefaultResponse>() {
                        @Override
                        public void onSuccess(DefaultResponse defaultResponse) {

                        }

                        @Override
                        public void onError(Throwable throwable, boolean b) {

                        }

                        @Override
                        public void onCancelled(CancelledException e) {

                        }

                        @Override
                        public void onFinished() {

                        }
                    });
                    if (keyList.size() > 0) {
                        T.showShort(getActivity(), "更新钥匙完成");
                    } else {
                        T.showShort(getActivity(), "您已下载所有钥匙");
                    }

                } else {
                    T.showShort(getActivity(), "更新钥匙失败: " + result.msg);
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ex.printStackTrace();
                T.showShort(getActivity(), "更新钥匙失败");
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
                loadKeyData();
                mActivity.dismissLoadingDialog();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
//        if (isVisible()) {
//            downloadKeyList();
//        }
    }

    @Override
    public void onDestroy() {
        getActivity().unbindService(connection);
        super.onDestroy();
    }

    private void loadKeyData() {
        x.task().post(new Runnable() {
            @Override
            public void run() {
                KeyUtils.clearTempKeys();

                List<OneKeyInfo4DB> keyList = KeyUtils.getAll();

                if (keyList != null) {
                    mDataList.clear();
                    mDataList.addAll(keyList);
                    mListAdapter.notifyDataSetChanged();

                    if (keyList.size() > 0) {
                        mRecyclerView.scrollBy(0, -mRecyclerView.computeVerticalScrollOffset());
                        mNullDataView.setVisibility(View.GONE);
                        mBrushCardBtn.setVisibility(View.VISIBLE);
                    } else {
                        mNullDataView.setVisibility(View.VISIBLE);
                        mBrushCardBtn.setVisibility(View.GONE);
                    }
                } else {
                    mNullDataView.setVisibility(View.VISIBLE);
                    mBrushCardBtn.setVisibility(View.GONE);
                }
//                mBrushCardBtn.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onShareKey(OneKeyInfo4DB keyInfo) {
        Intent intent = new Intent(x.app(), ShareKeyActivity.class);
        intent.putExtra(ShareKeyActivity.EXTRA_SHARE_KEY, keyInfo);
        startActivity(intent);
    }

    @Event(R.id.btn_open_mailbox)
    private void openMailbox(View view) {
        T.showShort(getActivity(), "信报箱功能还在努力开发中，敬请期待！");
    }

    @Event(R.id.btn_open_door)
    private void openDoor(View view) {
        T.showShort(getActivity(), "房门开锁功能还在努力开发中，敬请期待！");
    }

    @Override
    public void update(Observable observable, Object data) {
        if (data != null && data instanceof MainData) {
            return;
        }

        loadKeyData();
    }

    private boolean isSelectColse = false;

    private OnTaskCallback mTaskCallback = new OnTaskCallback() {
        @Override
        public void onSuccess() {
            if (mCurrentTask != null && !isSelectColse) {
                T.showShort(getActivity(), "开门成功 ");
            }
            mCurrentTask = null;
            mActivity.dismissLoadingDialog();
            mBrushCardBtn.setSelected(false);
            isConnected = false;
        }

        @Override
        public void onError(final Throwable ex) {
            if (mCurrentTask != null) {
                T.showShort(getActivity(), ex.getMessage());
            }
            mCurrentTask = null;
            mActivity.dismissLoadingDialog();
            mBrushCardBtn.setSelected(false);
            isConnected = false;
        }

        @Override
        public void onShowSelect(String str[]) {
            mActivity.dismissLoadingDialog();
            mActivity.showSelectDialog("请选择", str, new SelectCallbcak() {
                @Override
                public void onSelection(int position, CharSequence text) {

                    if (mWifiAdmin.isWifiEnabled()) {
                        mCurrentTask.connectWIFI(position);
                    }
                    mActivity.dismissLoadingDialog();

                }

                @Override
                public void onPositive() {
                    isSelectColse = true;
                    mCurrentTask.setSelectColse(isSelectColse);
                    mCurrentTask.onSuccess();
                    mCurrentTask = null;
                    mActivity.dismissLoadingDialog();
                    mBrushCardBtn.setSelected(false);
                }
            });
        }

        @Override
        public void onConnected() {
            mActivity.showLoadingDialog("正在开门...");
        }
    };

    @Event(R.id.btn_brush_card)
    private void onBrushCardClick(View view) {
        KeyFragmentPermissionsDispatcher.startBrushCardWithCheck(this);
    }

    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    protected void startBrushCard() {
        if (mCurrentTask != null && mCurrentTask.isWorking() || mDataList.isEmpty()) {
            if (mDataList.isEmpty()) {
                T.showShort(getActivity(), "钥匙丢失，请重新更新");
            } else {
                T.showShort(getActivity(), "正在开门，请稍后...");
            }
            return;
        }

        mCurrentTask = new OpenDoorTask(mActivity);

        if (!mWifiAdmin.isWifiEnabled() && !mWifiAdmin.isWifiEnabling() && !mWifiAdmin.openWifi()) {
            return;
        }
        mCurrentTask.setOnTaskCallback(mTaskCallback);
        isConnected = true;
        mCurrentTask.setWifiStateQueue(mWifiStateQueue);
        isSelectColse = false;
        mCurrentTask.setSelectColse(isSelectColse);
        mActivity.showLoadingDialog("正在搜索设备...");
        mBrushCardBtn.setSelected(true);

        if (mWifiAdmin.isWifiEnabled()) {
            mCurrentTask.start();
        }
    }

    @OnShowRationale({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    protected void onShowRationale(PermissionRequest request) {
        request.proceed();
    }

    @OnPermissionDenied({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void onPermissionDenied() {
        T.showShort(getActivity(), "已拒绝应用获取网络信息");
    }

    @OnNeverAskAgain({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void onNeverAskAgain() {
        T.showShort(getActivity(), "已拒绝应用获取网络信息22");
    }

    private void showRationaleDialog(String messageResId, final PermissionRequest request) {
        new AlertDialog.Builder(getActivity())
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.proceed();//请求权限
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .setMessage(messageResId)
                .show();
    }

    private int mWifiState = -1;

    @Override
    public void onReceiveWifiBroadcast(Intent intent) {
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
            mWifiState = wifiState;
            Logger.info("WifiManager", "WIFI_STATE_CHANGED_ACTION: " + mWifiState);
            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLED:
                    mBrushCardBtn.setSelected(false);
                    mActivity.dismissLoadingDialog();
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    mBrushCardBtn.setSelected(false);
                    mActivity.dismissLoadingDialog();
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    if (mCurrentTask != null && !mCurrentTask.hasStarted()) {
                        mCurrentTask.start();
                    }
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    break;
            }
        }

        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (null != parcelableExtra) {
                NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                NetworkInfo.State state = networkInfo.getState();
                boolean isConnected_ = state == NetworkInfo.State.CONNECTED;
                Logger.info("Zeng", "KeyFragment ==== > NETWORK_STATE_CHANGED_ACTION: " + isConnected_);
                if (isConnected && isConnected_ && mWifiState == WifiManager.WIFI_STATE_ENABLED ||
                        isConnected && isConnected_ && mWifiAdmin != null ? mWifiAdmin.isWifiEnabled() : false) {
                    mWifiStateQueue.clear();
                    mWifiStateQueue.add("connected");
                }
            }
        }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        KeyFragmentPermissionsDispatcher.onRequestPermissionsResult(KeyFragment.this, requestCode, grantResults);
    }
}
