package com.hongbang.ic.activity;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;

import com.hongbang.ic.IWifiKeyService;
import com.hongbang.ic.R;
import com.hongbang.ic.adapter.CommunityChooseAdapter;
import com.hongbang.ic.api.request.GetCityListParams;
import com.hongbang.ic.api.request.GetCommunityListParams;
import com.hongbang.ic.api.response.BaseResponse;
import com.hongbang.ic.common.UserDefaults;
import com.hongbang.ic.common.app;
import com.hongbang.ic.keycenter.WifiKeyService;
import com.hongbang.ic.model.BaseCommunityChooseItem;
import com.hongbang.ic.model.CityInfoList;
import com.hongbang.ic.model.CommunityInfoList;
import com.hongbang.ic.model.OneCommunityInfo;
import com.hongbang.ic.model.UserInfo;
import com.hongbang.ic.util.T;
import com.hongbang.ic.view.BladeView;
import com.hongbang.ic.view.PinnedHeaderListView;

import org.xutils.common.Callback;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CommunityChooseActivity extends BaseActivity {

    public static final String EXTRA_CITY_ID = "city_id";
    public static final String EXTRA_CHOOSE_MODE = "choose_mode";
    public static final String EXTRA_CHANGE_COMMUNITY = "is_change_community";

    public static final int MODE_CHOOSE_PROVINCE = 0;
    public static final int MODE_CHOOSE_CITY = 1;
    public static final int MODE_CHOOSE_DISTRICT = 2;
    public static final int MODE_CHOOSE_COMMUNITY = 3;

    @ViewInject(R.id.edit_community_filter)
    private EditText mFilterEdit;

    @ViewInject(R.id.community_choose_index)
    private BladeView mIndexView;

    @ViewInject(R.id.community_choose_list_view)
    private PinnedHeaderListView mChooseListView;

    private CommunityChooseAdapter mAdapter;

    private List<CommunityChooseAdapter.SectionedItemData> mAdapterData;

    private int mChooseMode = MODE_CHOOSE_PROVINCE;

    private boolean isChangeCommunity = false;

    private String mCityId = null;

    private static ArrayList<CommunityChooseActivity> mActivityStack = new ArrayList<>();

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
        mActivityStack.add(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mChooseMode = bundle.getInt(EXTRA_CHOOSE_MODE, MODE_CHOOSE_PROVINCE);
            mCityId = bundle.getString(EXTRA_CITY_ID, "0");
            isChangeCommunity = bundle.getBoolean(EXTRA_CHANGE_COMMUNITY, false);
        }

        setContentView(R.layout.activity_cummunity_choose, mChooseMode != MODE_CHOOSE_PROVINCE || isChangeCommunity);

        x.view().inject(this);

        switch (mChooseMode) {
            case MODE_CHOOSE_PROVINCE:
                setTitle(R.string.title_choose_province);
                break;
            case MODE_CHOOSE_CITY:
                setTitle(R.string.title_choose_city);
                break;
            case MODE_CHOOSE_DISTRICT:
                setTitle(R.string.title_choose_distinct);
                break;
            case MODE_CHOOSE_COMMUNITY:
                Intent intent = new Intent(this, WifiKeyService.class);
                if (isChangeCommunity) {
                    bindService(intent, connection, Service.BIND_ABOVE_CLIENT);
                }
                setTitle(R.string.title_choose_community);
                break;
            default:
                mChooseMode = MODE_CHOOSE_PROVINCE;
                setTitle(R.string.title_choose_province);
                break;
        }

        mFilterEdit.addTextChangedListener(mFilterChanged);

        mIndexView.setOnItemClickListener(new BladeView.OnItemClickListener() {
            @Override
            public void onItemClick(String s) {
                int selection = mAdapter.gotoSection(s.toUpperCase());
                if (selection >= 0) {
                    mChooseListView.setSelection(selection);
                }
            }
        });

        mAdapter = new CommunityChooseAdapter();
        mChooseListView.setAdapter(mAdapter);

        mChooseListView.setOnItemClickListener(mItemClickListener);

        requestData();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mActivityStack.contains(this)) {
            mActivityStack.remove(this);
        }

        if (mChooseMode == MODE_CHOOSE_COMMUNITY && isChangeCommunity) {
            unbindService(connection);
        }
    }

    @Override
    public void onBackPressed() {
        if (mChooseMode != MODE_CHOOSE_PROVINCE || isChangeCommunity) {
            super.onBackPressed();
        }
    }

    private void requestData() {
        showLoadingDialog();
        if (mChooseMode != MODE_CHOOSE_COMMUNITY) {
            GetCityListParams params = new GetCityListParams();
            params.id = mCityId;

            app.data().invokeRequest(params, mGetCityCallback);
        } else {
            GetCommunityListParams params = new GetCommunityListParams();
            params.cityId = mCityId;

            app.data().invokeRequest(params, mGetCommunityCallback);
        }
    }

    private TextWatcher mFilterChanged = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            filterAdapterData(s.toString());
        }
    };

    private Callback.CommonCallback<BaseResponse<CityInfoList>> mGetCityCallback
            = new Callback.CommonCallback<BaseResponse<CityInfoList>>() {
        @Override
        public void onSuccess(BaseResponse<CityInfoList> result) {
            if (result.code == 0 && result.getData() != null) {
                loadView(result.getData().cities != null ? result.getData().cities : result.getData().citys);
            } else {
                T.showShort(CommunityChooseActivity.this, R.string.load_failed);
            }
        }

        @Override
        public void onError(Throwable ex, boolean isOnCallback) {
            T.showShort(CommunityChooseActivity.this, R.string.load_failed);
        }

        @Override
        public void onCancelled(Callback.CancelledException cex) {

        }

        @Override
        public void onFinished() {
            dismissLoadingDialog();
        }
    };

    private Callback.CommonCallback<BaseResponse<CommunityInfoList>> mGetCommunityCallback
            = new Callback.CommonCallback<BaseResponse<CommunityInfoList>>() {
        @Override
        public void onSuccess(BaseResponse<CommunityInfoList> result) {
            if (result.code == 0 && result.getData() != null) {
                loadView(result.getData().parks);
            } else {
                T.showShort(CommunityChooseActivity.this, R.string.load_failed);
            }
        }

        @Override
        public void onError(Throwable ex, boolean isOnCallback) {
            T.showShort(CommunityChooseActivity.this, R.string.load_failed);
        }

        @Override
        public void onCancelled(Callback.CancelledException cex) {

        }

        @Override
        public void onFinished() {
            dismissLoadingDialog();
        }
    };

    private char getFirstLetter(BaseCommunityChooseItem obj) {
        if (obj == null) {
            return '#';
        }
        try {
            return obj.pin.toUpperCase().charAt(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return '#';
    }

    private Comparator<BaseCommunityChooseItem> mComparator = new Comparator<BaseCommunityChooseItem>() {

        @Override
        public int compare(BaseCommunityChooseItem lhs, BaseCommunityChooseItem rhs) {
            if (lhs == rhs) {
                return 0;
            } else if (getFirstLetter(lhs) != getFirstLetter(rhs)) {
                return getFirstLetter(lhs) - getFirstLetter(rhs);
            } else {
                return -1;
            }
        }
    };

    private void loadView(List<? extends BaseCommunityChooseItem> list) {
        mAdapterData = getAdapterData(list);
        mAdapter.setData(mAdapterData);
    }

    private List<CommunityChooseAdapter.SectionedItemData> getAdapterData(List<? extends BaseCommunityChooseItem> list) {
        if (list == null || list.size() == 0) {
            return null;
        }
        Collections.sort(list, mComparator);

        List<CommunityChooseAdapter.SectionedItemData> retval = new ArrayList<>();
        CommunityChooseAdapter.SectionedItemData data = new CommunityChooseAdapter.SectionedItemData();
        retval.add(data);
        for (BaseCommunityChooseItem obj : list) {
            String firstLetter = getFirstLetter(obj) + "";
            if (data.firstLetter == null) {
                data.firstLetter = firstLetter;
            } else if (!data.firstLetter.equals(firstLetter)) {
                data = new CommunityChooseAdapter.SectionedItemData();
                data.firstLetter = firstLetter;
                retval.add(data);
            }
            data.itemsInSection.add(obj);
        }

        return retval;
    }

    private void filterAdapterData(String filter) {
        List<CommunityChooseAdapter.SectionedItemData> filtered;
        if (filter == null || filter.trim().length() == 0 || mAdapterData == null) {
            filtered = mAdapterData;
        } else {
            filtered = new ArrayList<>();
            for (CommunityChooseAdapter.SectionedItemData data : mAdapterData) {
                List<BaseCommunityChooseItem> items = new ArrayList<>();
                for (BaseCommunityChooseItem item : data.itemsInSection) {
                    if (item.name == null) {
                        continue;
                    }
                    if (item.name.toUpperCase().contains(filter.toUpperCase())) {
                        items.add(item);
                    }
                }
                if (items.size() > 0) {
                    CommunityChooseAdapter.SectionedItemData section = new CommunityChooseAdapter.SectionedItemData();
                    section.firstLetter = data.firstLetter;
                    section.itemsInSection = items;
                    filtered.add(section);
                }
            }
        }
        mAdapter.setData(filtered);
    }

    PinnedHeaderListView.OnItemClickListener mItemClickListener = new PinnedHeaderListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int section, int position, long id) {
            BaseCommunityChooseItem item = mAdapter.getItem(section, position);
            if (mChooseMode != MODE_CHOOSE_COMMUNITY) {
                String cityId = item.id;
                int mode = mChooseMode + 1;
                Intent i = new Intent(CommunityChooseActivity.this, CommunityChooseActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(EXTRA_CHOOSE_MODE, mode);
                bundle.putString(EXTRA_CITY_ID, cityId);
                bundle.putBoolean(EXTRA_CHANGE_COMMUNITY, isChangeCommunity);
                i.putExtras(bundle);
                startActivity(i);
            } else {
                final OneCommunityInfo community = (OneCommunityInfo) item;
                if (community.id.equals(UserDefaults.defaults().getUserInfo().communityId)) {
                    startActivity(new Intent(CommunityChooseActivity.this, MainActivity.class));
                    if (!isChangeCommunity) {
                        BaseActivity.finishAll();
                    } else {
                        for (CommunityChooseActivity activity : mActivityStack) {
                            activity.finish();
                        }
                    }
                }

                showLoadingDialog("正在绑定小区...");
                app.data().bindCommunity(community.id, new Callback.CommonCallback<BaseResponse<Object>>() {
                    @Override
                    public void onSuccess(BaseResponse<Object> result) {
                        if (result.code == 0) {
                            UserInfo userInfo = UserDefaults.defaults().getUserInfo();
                            userInfo.communityId = community.id;
                            userInfo.communityName = community.name;
                            UserDefaults.defaults().setUserInfo(userInfo);
                            UserDefaults.defaults().setMainData(null);
                            try {
                                if (mRemoteService != null) {
                                    mRemoteService.onCommunityChanged();
                                }
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                            startActivity(new Intent(CommunityChooseActivity.this, MainActivity.class));
                            if (!isChangeCommunity) {
                                BaseActivity.finishAll();
                            } else {
                                for (CommunityChooseActivity activity : mActivityStack) {
                                    activity.finish();
                                }
                            }
                        } else {
                            T.showShort(getBaseContext(), "小区绑定失败: " + result.msg);
                        }
                    }

                    @Override
                    public void onError(Throwable ex, boolean isOnCallback) {
                        T.showShort(getBaseContext(), "小区绑定失败: " + ex.getMessage());
                    }

                    @Override
                    public void onCancelled(CancelledException cex) {

                    }

                    @Override
                    public void onFinished() {
                        dismissLoadingDialog();
                    }
                });
            }
        }

        @Override
        public void onSectionClick(AdapterView<?> adapterView, View view, int section, long id) {

        }
    };

}
