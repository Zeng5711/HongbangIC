package com.hongbang.ic.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.hongbang.ic.activity.MainActivity;

/**
 * 主页面Fragment基类
 * <p>
 * Created by xionghf on 16/3/27.
 */
public abstract class BaseMainFragment extends Fragment {

    protected MainActivity mActivity;

    public void onCustomTitleButtonClick() {

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    public void onReceiveWifiBroadcast(Intent intent) {

    }
}
