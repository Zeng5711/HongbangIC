package com.hongbang.ic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hongbang.ic.R;
import com.hongbang.ic.constant.AppConstants;
import com.hongbang.ic.model.OneRepairInfo;
import com.hongbang.ic.util.StringUtils;

import java.util.List;

/**
 * Created by xionghf on 16/4/9.
 */
public class RepairListAdapter extends BaseAdapter {

    private List<OneRepairInfo> mDataList = null;


    public RepairListAdapter(List<OneRepairInfo> list) {
        this.mDataList = list;
    }

    public void setData(List<OneRepairInfo> list) {
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
    public OneRepairInfo getItem(int position) {
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
        OneRepairInfo data = getItem(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layout = inflater.inflate(R.layout.common_list_item, null);

            holder = new ViewHolder();
            holder.processView = (ImageView) layout.findViewById(R.id.corner_process_state);
            holder.processView.setVisibility(View.VISIBLE);
            holder.titleView = (TextView) layout.findViewById(R.id.text_title);
            holder.descView = (TextView) layout.findViewById(R.id.text_desc);
            holder.timeView = (TextView) layout.findViewById(R.id.text_time);
            layout.setTag(holder);
        } else {
            layout = convertView;
            holder = (ViewHolder) layout.getTag();
        }

        Integer corner = AppConstants.PROCESS_STATE_ICON_MAP.get(data.processState);
        if (corner == null) {
            corner = R.drawable.icon_corner_unprocessed;
        }
        holder.processView.setImageResource(corner);
        holder.titleView.setText(data.title);
        holder.descView.setText(data.content);
        if (data.submitTime > 0) {
            holder.timeView.setText(StringUtils.formatDate(data.submitTime, AppConstants.DATE_FORMAT_1));
        }

        return layout;
    }

    private class ViewHolder {
        ImageView processView;

        TextView titleView;

        TextView timeView;

        TextView descView;
    }

}
