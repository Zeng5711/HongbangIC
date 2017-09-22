package com.hongbang.ic.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.hongbang.ic.R;
import com.hongbang.ic.common.UserDefaults;
import com.hongbang.ic.constant.AppConstants;
import com.hongbang.ic.model.OneKeyInfo4DB;
import com.hongbang.ic.util.StringUtils;

import java.util.List;

/**
 * 钥匙列表Adapter
 * <p/>
 * Created by xionghf on 16/4/9.
 */
public class KeyListAdapter extends RecyclerView.Adapter<KeyListAdapter.ViewHolder> {

    private List<OneKeyInfo4DB> mDataList = null;

    private OnShareKeyListener mOnShareKeyListener;

    public KeyListAdapter(List<OneKeyInfo4DB> list) {
        this.mDataList = list;
    }

    public void setData(List<OneKeyInfo4DB> list) {
        this.mDataList.clear();
        this.mDataList.addAll(list);
        notifyDataSetChanged();
    }

    public void setOnShareKeyListener(OnShareKeyListener listener) {
        mOnShareKeyListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return new ViewHolder(inflater.inflate(R.layout.key_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mDataList != null && mDataList.size() > position) {
            holder.bindData(mDataList.get(position));
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getTag() != null && v.getTag() instanceof OneKeyInfo4DB) {
                if (mOnShareKeyListener != null) {
                    mOnShareKeyListener.onShareKey((OneKeyInfo4DB) v.getTag());
                }
            }
        }
    };

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView communityNameView;

        TextView validityPeriodView;

        TextView keyTypeView;

        Button shareButton;

        public ViewHolder(View itemView) {
            super(itemView);

            this.communityNameView = (TextView) itemView.findViewById(R.id.text_community);
            this.validityPeriodView = (TextView) itemView.findViewById(R.id.text_validity_period);
            this.shareButton = (Button) itemView.findViewById(R.id.btn_share_key);
            this.keyTypeView = (TextView) itemView.findViewById(R.id.text_key_type);
            this.shareButton.setOnClickListener(mOnClickListener);
        }

        public void bindData(OneKeyInfo4DB data) {
            this.communityNameView.setText(UserDefaults.defaults().getUserInfo().communityName);
            if (data.validityPeriod > 0) {
                this.validityPeriodView.setText(StringUtils.
                        formatDate(data.validityPeriod, AppConstants.DATE_FORMAT_1));
            } else {
                this.validityPeriodView.setText("--");
            }
            switch (data.type) {
                case AppConstants.KEY_TYPE_ALL:
                    this.keyTypeView.setText("全通");
                    break;
                case AppConstants.KEY_TYPE_TIMES:
                    this.keyTypeView.setText("计次");
                    break;
                case AppConstants.KEY_TYPE_STORED:
                    this.keyTypeView.setText("储值");
                    break;
                case AppConstants.KEY_TYPE_SHARED:
                    this.keyTypeView.setText("临时");
                    break;
                default:
                    this.keyTypeView.setText("普通");
            }
            if (data.type != AppConstants.KEY_TYPE_SHARED) {
                this.shareButton.setVisibility(View.VISIBLE);
                this.shareButton.setTag(data);
            } else {
                this.shareButton.setVisibility(View.GONE);
                this.shareButton.setTag(null);
            }
        }
    }

    public interface OnShareKeyListener {
        void onShareKey(OneKeyInfo4DB keyInfo);
    }

}
