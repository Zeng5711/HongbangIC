package com.hongbang.ic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hongbang.ic.R;
import com.hongbang.ic.constant.AppConstants;
import com.hongbang.ic.model.OneNoticeInfo;
import com.hongbang.ic.util.StringUtils;

import java.util.List;

/**
 * Created by xionghf on 16/4/9.
 */
public class NoticeListAdapter extends BaseAdapter {

    private List<OneNoticeInfo> mDataList = null;

    public NoticeListAdapter(List<OneNoticeInfo> list) {
        this.mDataList = list;
    }

    public void setData(List<OneNoticeInfo> list) {
        this.mDataList.clear();
        this.mDataList.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mDataList == null) {
            return 0;
        } else {
            return mDataList.size();
        }
    }

    @Override
    public OneNoticeInfo getItem(int position) {
        if (mDataList == null || mDataList.size() - 1 < position || position < 0) {
            return null;
        } else {
            return mDataList.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View layout = null;
        ViewHolder holder = null;
        OneNoticeInfo data = getItem(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layout = inflater.inflate(R.layout.common_list_item, null);

            holder = new ViewHolder();
            holder.titleView = (TextView) layout.findViewById(R.id.text_title);
            holder.descView = (TextView) layout.findViewById(R.id.text_desc);
            holder.timeView = (TextView) layout.findViewById(R.id.text_time);
            layout.setTag(holder);
        } else {
            layout = convertView;
            holder = (ViewHolder) layout.getTag();
        }

        holder.titleView.setText(data.title);
        holder.descView.setText(data.content);
        if (data.date > 0) {
            holder.timeView.setText(StringUtils.formatDate(data.date, AppConstants.DATE_FORMAT_1));
        }

        return layout;
    }

    private class ViewHolder {
        TextView titleView;

        TextView timeView;

        TextView descView;
    }

}
