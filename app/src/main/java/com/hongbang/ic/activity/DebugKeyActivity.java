package com.hongbang.ic.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hongbang.ic.R;
import com.hongbang.ic.common.UserDefaults;
import com.hongbang.ic.keycenter.KeyUtils;
import com.hongbang.ic.keycenter.WifiAdmin;
import com.hongbang.ic.keycenter.tasks.AbstractTask;
import com.hongbang.ic.keycenter.tasks.OpenDoorTask;
import com.hongbang.ic.keycenter.tasks.DownloadBlackListTask;
import com.hongbang.ic.keycenter.tasks.DownloadRecordTask;
import com.hongbang.ic.keycenter.tasks.DownloadWhiteListTask;
import com.hongbang.ic.keycenter.tasks.OnTaskTracker;
import com.hongbang.ic.keycenter.tasks.UploadBlackListTask;
import com.hongbang.ic.keycenter.tasks.UploadRecordTask;
import com.hongbang.ic.keycenter.tasks.UploadWhiteListTask;
import com.hongbang.ic.model.OneKeyInfo4DB;
import com.hongbang.ic.util.FileUtils;
import com.hongbang.ic.util.Logger;
import com.hongbang.ic.util.StringUtils;
import com.hongbang.ic.util.T;
import com.hongbang.ic.view.DividerItemDecoration;

import org.xutils.common.util.DensityUtil;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;

public class DebugKeyActivity extends BaseActivity implements OnTaskTracker {

    private static final String TAG = DebugKeyActivity.class.getName();

    public static final String EXTRA_TITLE = "title";

    public static final String EXTRA_ACTION = "action";

    private int mActionMode = 0;

    private AbstractTask mCurrentTask;

    private WifiAdmin mWifiAdmin;

    @ViewInject(R.id.list_view)
    private ListView mListView;

    @ViewInject(R.id.input_ip)
    private EditText mInputIp;

    private ArrayAdapter<String> mAdapter;

    private ArrayList<String> mDataList = new ArrayList<>();

    private ArrayBlockingQueue<String> mWifiStateQueue = new ArrayBlockingQueue<>(50);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_key);

        setTitle(getIntent().getStringExtra(EXTRA_TITLE));

        mActionMode = getIntent().getIntExtra(EXTRA_ACTION, 0);

        x.view().inject(this);

        mWifiAdmin = new WifiAdmin(this);

        mDataList.add(String.format(Locale.getDefault(), "小区信息: %s(%05d)",
                UserDefaults.defaults().getUserInfo().communityName,
                Integer.valueOf(UserDefaults.defaults().getUserInfo().communityId)));

        mListView.setAdapter(mAdapter = new ArrayAdapter<>(this, R.layout.debug_list_item, mDataList));

        mListView.setClickable(false);

        registerWifiReceiver();

        setCustomTitleButton("开始", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftInputFromWindow();
                hideCustomTitleButton();
                mCurrentTask = null;
                mDataList.clear();
                mDataList.add(String.format(Locale.getDefault(), "小区信息: %s(%05d)",
                        UserDefaults.defaults().getUserInfo().communityName,
                        Integer.valueOf(UserDefaults.defaults().getUserInfo().communityId)));
                mAdapter.notifyDataSetChanged();
                startTask();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        this.unregisterReceiver(mWifiConnectReceiver);

        if (mCurrentTask != null && mCurrentTask.isWorking()) {
            mCurrentTask.destroy();
        }
    }

    private void startTask() {
        switch (mActionMode) {
            case 1:
                mCurrentTask = new UploadWhiteListTask(this);
                break;
            case 2:
                mCurrentTask = new DownloadWhiteListTask(this);
                break;
            case 3:
                mCurrentTask = new UploadBlackListTask(this);
                break;
            case 4:
                mCurrentTask = new DownloadBlackListTask(this);
                break;
            case 5:
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
                    T.showShort(this, "本地没有未上传的刷卡记录");
                } else {
                    MaterialDialog dialog = new MaterialDialog.Builder(this)
                            .items(recordFileNames)
                            .title("选择要上传的刷卡记录")
                            .titleGravity(GravityEnum.CENTER)
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                    String path = recordFiles.get(which);
                                    mCurrentTask = new UploadRecordTask(DebugKeyActivity.this, path);
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
            case 6:
                mCurrentTask = new DownloadRecordTask(this);
                break;
            default:
                List<OneKeyInfo4DB> keyList = KeyUtils.getAll();

                if (keyList != null && keyList.size() > 0) {
                    mCurrentTask = new OpenDoorTask(this);
                } else {
                    addLog("本地没有钥匙包，请先更新钥匙包到本地。");
                    return;
                }
        }

        executeCurrentTask();
    }

    private void executeCurrentTask() {
        if (!mWifiAdmin.isWifiEnabled() && !mWifiAdmin.isWifiEnabling() && !mWifiAdmin.openWifi()) {
            addLog("网络无法使用，请检查网络配置");
            return;
        }
        mCurrentTask.setTracker(this);

        mCurrentTask.setWifiStateQueue(mWifiStateQueue);

        String input = mInputIp.getText().toString().trim();
        mInputIp.setText(input);
        if (StringUtils.isInet4Address(input)) {
            mCurrentTask.setTargetIp(input);
        }

        if (mCurrentTask != null && mWifiAdmin.isWifiEnabled()) {
            mCurrentTask.start();
        }
    }

    private void registerWifiReceiver() {
        IntentFilter mWifiFilter = new IntentFilter();
        mWifiFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mWifiFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        this.registerReceiver(mWifiConnectReceiver, mWifiFilter);
    }

    private BroadcastReceiver mWifiConnectReceiver = new BroadcastReceiver() {

        private int mWifiState;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                mWifiState = wifiState;
                Logger.info("WifiManager", "WIFI_STATE_CHANGED_ACTION: " + mWifiState);
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
                    boolean isConnected = state == NetworkInfo.State.CONNECTED;
                    Logger.info("WifiManager", "NETWORK_STATE_CHANGED_ACTION: " + isConnected);
                    if (isConnected && mWifiState == WifiManager.WIFI_STATE_ENABLED) {
                        mWifiStateQueue.clear();
                        mWifiStateQueue.add("connected");
                    }
                }
            }
        }
    };

    private String[] states = {"任务开始", "连接中...", "连接成功",
            "任务开始", "任务结束", "任务出错", "任务中止"};

    @Override
    public void onStateChanged(int state) {
        if (state == AbstractTask.TASK_STATE_ERROR) {
            setCustomTitleButton("重试", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideSoftInputFromWindow();
                    hideCustomTitleButton();
                    mCurrentTask = null;
                    mDataList.clear();
                    mDataList.add(String.format(Locale.getDefault(), "小区: %s(%05d)",
                            UserDefaults.defaults().getUserInfo().communityName,
                            Integer.valueOf(UserDefaults.defaults().getUserInfo().communityId)));
                    mAdapter.notifyDataSetChanged();
                    startTask();
                }
            });
        }
        addLog(">>" + states[state]);
    }

    @Override
    public void onConnected(String ssid) {
        addLog("连接到Wifi: " + ssid);
    }

    @Override
    public void onReceiveData(String hex) {
        addLog(">>收到数据:\n" + hex);
    }

    @Override
    public void onSendData(String hex) {
        addLog(">>发送数据:\n" + hex);
    }

    @Override
    public void onError(Throwable ex) {
        if (ex != null) {
            addLog(">>异常信息:\n" + ex.getMessage());
        }
    }

    @Override
    public void onLog(String log) {
        addLog(log);
    }

    private void addLog(String log) {
        mDataList.add(log);
        mAdapter.notifyDataSetChanged();
        mListView.setSelection(mAdapter.getCount() - 1);
    }
}
