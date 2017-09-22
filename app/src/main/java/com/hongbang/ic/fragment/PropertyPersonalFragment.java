package com.hongbang.ic.fragment;

import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hongbang.ic.R;
import com.hongbang.ic.activity.AboutActivity;
import com.hongbang.ic.activity.BaseActivity;
import com.hongbang.ic.activity.ChangePasswordActivity;
import com.hongbang.ic.activity.DebugKeyActivity;
import com.hongbang.ic.activity.LoginActivity;
import com.hongbang.ic.activity.SettingsActivity;
import com.hongbang.ic.api.SelectCallbcak;
import com.hongbang.ic.common.UserDefaults;
import com.hongbang.ic.keycenter.WifiAdmin;
import com.hongbang.ic.keycenter.WifiKeyService;
import com.hongbang.ic.keycenter.tasks.AbstractTask;
import com.hongbang.ic.keycenter.tasks.DownloadBlackListTask;
import com.hongbang.ic.keycenter.tasks.DownloadRecordTask;
import com.hongbang.ic.keycenter.tasks.DownloadWhiteListTask;
import com.hongbang.ic.keycenter.tasks.OnTaskCallback;
import com.hongbang.ic.keycenter.tasks.UploadBlackListTask;
import com.hongbang.ic.keycenter.tasks.UploadRecordTask;
import com.hongbang.ic.keycenter.tasks.UploadWhiteListTask;
import com.hongbang.ic.util.FileUtils;
import com.hongbang.ic.util.T;
import com.hongbang.ic.view.DividerItemDecoration;

import org.xutils.common.util.DensityUtil;
import org.xutils.view.annotation.Event;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 物业端个人界面
 * <p/>
 * Created by xionghf on 16/4/17.
 */
public class PropertyPersonalFragment extends BaseMainFragment {

    private static final String TAG = PropertyPersonalFragment.class.getName();

    private WifiAdmin mWifiAdmin;

    private AbstractTask mCurrentTask;

    private ArrayBlockingQueue<String> mWifiStateQueue = new ArrayBlockingQueue<>(50);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_property_personal, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        x.view().inject(this, this.getView());

        mWifiAdmin = new WifiAdmin(getActivity());

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Event(value = {
            R.id.btn_record_upload,
            R.id.btn_white_list_upload,
            R.id.btn_black_list_upload,
            R.id.btn_record_download,
            R.id.btn_white_list_download,
            R.id.btn_black_list_download,
    })
    private void onClick(View v) {
        if (mCurrentTask != null && mCurrentTask.isWorking()) {
            return;
        }

        mActivity.showLoadingDialog("正在搜索设备...");
        switch (v.getId()) {
            case R.id.btn_record_upload:
                String saveRootDir = Environment.getExternalStorageDirectory().getPath() + "/HongbangIC" +
                        File.separator + UserDefaults.defaults().getUserInfo().communityId + File.separator + "brush_record";
                final ArrayList<String> recordFiles = new ArrayList<>();
                ArrayList<String> recordFileNames = new ArrayList<>();
                if (FileUtils.isDirectory(saveRootDir)) {
                    File dir = new File(saveRootDir);
                    String[] files = dir.list();
                    for (int i = 0; files != null && files.length > 0 && i < files.length; i++) {
                        File file = new File(saveRootDir, files[i]);

                        if (file.isFile() && file.length() > 0
                                && file.getName().matches("^" + UserDefaults.defaults().getUserInfo().communityId + "-\\d{1,3}\\.bin$")) {
                            recordFileNames.add(files[i].substring(0, files[i].length() - 4));
                            recordFiles.add(file.getPath());
                        }
                    }
                }
                if (recordFiles.size() == 0) {
                    T.showShort(getActivity(), "本地没有未上传的刷卡记录");
                    mActivity.dismissLoadingDialog();
                } else {
                    mActivity.dismissLoadingDialog();
                    MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                            .items(recordFileNames)
                            .title("选择要上传的刷卡记录")
                            .titleGravity(GravityEnum.CENTER)
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                    String path = recordFiles.get(which);
                                    mCurrentTask = new UploadRecordTask(getActivity(), path);
                                    executeCurrentTask();
                                }
                            }).build();
                    DividerItemDecoration itemDecoration = new DividerItemDecoration(DividerItemDecoration.HORIZONTAL,
                            DensityUtil.dip2px(1), getResources().getColor(R.color.list_divider_color));
                    dialog.getRecyclerView().addItemDecoration(itemDecoration);
                    dialog.getRecyclerView().setPadding(0, 0, 0, 0);
                    dialog.getRecyclerView().setOverScrollMode(View.OVER_SCROLL_NEVER);
                    dialog.show();
                }
                return;

            case R.id.btn_white_list_upload:
                mCurrentTask = new UploadWhiteListTask(getActivity());
                break;

            case R.id.btn_black_list_upload:
                mCurrentTask = new UploadBlackListTask(getActivity());
                break;

            case R.id.btn_record_download:
                mCurrentTask = new DownloadRecordTask(getActivity());
                break;

            case R.id.btn_white_list_download:
                mCurrentTask = new DownloadWhiteListTask(getActivity());
                break;

            case R.id.btn_black_list_download:
                mCurrentTask = new DownloadBlackListTask(getActivity());
                break;
        }

        executeCurrentTask();
    }

    private boolean isSelectColse = false;

    private void executeCurrentTask() {
        if (!mWifiAdmin.isWifiEnabled() && !mWifiAdmin.isWifiEnabling() && !mWifiAdmin.openWifi()) {
            return;
        }
        isSelectColse = false;
        isConnected = true;
        mCurrentTask.setSelectColse(isSelectColse);
        mCurrentTask.setOnTaskCallback(mTaskCallback);
        mCurrentTask.setWifiStateQueue(mWifiStateQueue);

        if (mWifiAdmin.isWifiEnabled()) {
            mCurrentTask.start();
        }
    }

    private OnTaskCallback mTaskCallback = new OnTaskCallback() {
        @Override
        public void onSuccess() {
            if (mCurrentTask != null && !isSelectColse) {
                T.showShort(getActivity(), mCurrentTask.getSuccessMessage());
            }
            mCurrentTask = null;
            mActivity.dismissLoadingDialog();
            isConnected = false;
        }

        @Override
        public void onError(final Throwable ex) {
            if (mCurrentTask != null) {
                T.showShort(getActivity(), mCurrentTask.getErrorMessage() + ": " + ex.getMessage());
            }
            mCurrentTask = null;
            mActivity.dismissLoadingDialog();
            isConnected = false;
        }

        @Override
        public void onShowSelect(String[] str) {
            mActivity.dismissLoadingDialog();
            mActivity.showSelectDialog("请选择", str, new SelectCallbcak() {
                @Override
                public void onSelection(int position, CharSequence text) {

                    if (mWifiAdmin.isWifiEnabled()) {
                        mCurrentTask.connectWIFI(position);
                    }
                    mActivity.dismissLoadingDialog();
                    mActivity.showLoadingDialog("处理中...");

                }

                @Override
                public void onPositive() {
                    isSelectColse = true;
                    mCurrentTask.setSelectColse(isSelectColse);
                    mCurrentTask.onSuccess();
                    mCurrentTask = null;
                    mActivity.dismissLoadingDialog();
                }
            });
        }

        @Override
        public void onConnected() {
            mActivity.showLoadingDialog("处理中...");
        }
    };

    private int mWifiState;

    @Override
    public void onReceiveWifiBroadcast(Intent intent) {
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
            mWifiState = wifiState;
            Log.d("WifiManager", "WIFI_STATE_CHANGED_ACTION: " + mWifiState);
            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLED:
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
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
                if (isConnected && isConnected_ && mWifiState == WifiManager.WIFI_STATE_ENABLED ||
                        isConnected && isConnected_ && mWifiAdmin != null ? mWifiAdmin.isWifiEnabled() : false) {
                    mWifiStateQueue.clear();
                    mWifiStateQueue.add("connected");
                }
            }
        }
    }

    private boolean isConnected = false;

    @Event(R.id.btn_change_password)
    private void changePassword(View view) {
        startActivity(new Intent(getActivity(), ChangePasswordActivity.class));
    }

    @Event(R.id.btn_abort_app)
    private void abort(View view) {
        startActivity(new Intent(getActivity(), AboutActivity.class));
    }

    @Event(R.id.btn_logout)
    private void logout(View view) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                .content("注销登录将删除所有的本地数据，是否确定注销当前账号？")
                .positiveText(R.string.text_logout2)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        UserDefaults.defaults().clear();
                        Intent intent = new Intent();
                        intent.setClass(getActivity(), LoginActivity.class);
                        startActivity(intent);
                        BaseActivity.finishAll();
                        getActivity().sendBroadcast(new Intent(WifiKeyService.USER_INFO_CHANGED_ACTION));
                    }
                })
                .negativeText(R.string.cancel);

        MaterialDialog dialog = builder.build();
        dialog.show();
    }

    @Event(R.id.btn_settings)
    private void startSettingsView(View view) {
        startActivity(new Intent(getActivity(), SettingsActivity.class));
    }
}
