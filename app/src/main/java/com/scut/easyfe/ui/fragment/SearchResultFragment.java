package com.scut.easyfe.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.scut.easyfe.R;
import com.scut.easyfe.app.App;
import com.scut.easyfe.app.Constants;
import com.scut.easyfe.entity.order.Order;
import com.scut.easyfe.ui.activity.ParentRegisterActivity;
import com.scut.easyfe.ui.activity.TeacherInfoActivity;
import com.scut.easyfe.ui.adapter.SearchResultAdapter;
import com.scut.easyfe.ui.base.BaseRefreshFragment;
import com.scut.easyfe.utils.DensityUtil;
import com.scut.easyfe.utils.DialogUtils;
import com.scut.easyfe.utils.LogUtils;
import com.scut.easyfe.utils.MapUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 单次预约多次预约筛选结果页
 * Created by jay on 16/3/31.
 */
public class SearchResultFragment extends BaseRefreshFragment {
    private int mReserveType = Constants.Identifier.RESERVE_MULTI;
    private List<Order> mOrders = new ArrayList<>();

    @Override
    protected void initData() {}

    @Override
    protected void initView(View view) {
        super.initView(view);

        TextView headView = new TextView(getActivity());
        headView.setMinimumHeight(DensityUtil.dip2px(mActivity, 5));
        if (mOrders.size() > 5) {
            headView.setBackground(null);
        } else {
            headView.setBackgroundResource(R.color.search_result_tip_bg);
            headView.setGravity(Gravity.CENTER);
            headView.setTextColor(getResources().getColor(R.color.text_hint_color));
            headView.setText("人数太少？放宽点筛选项吧亲~O(∩_∩)O~");
            headView.setPadding(0, 32, 0, 32);
        }

        mDataListView.addHeaderView(headView);
        mDataListView.setDividerHeight(DensityUtil.dip2px(mActivity, 5));
        mAdapter = new SearchResultAdapter(getActivity(), mOrders);
        setBaseAdapter(mAdapter);
    }

    @Override
    protected void onLoadingData() {
        setIsLoading(false);
    }

    @Override
    protected void onRefreshData() {
        setIsLoading(false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null == mActivity) {
            return;
        }

        //点击HeadView
        if (position == 0) {
            return;
        }

        final int index = position - 1;   //减1是为了减去HeaderView


        if(!App.getUser(false).isParent()){
            if(null == mActivity){
                return;
            }

            DialogUtils.makeChooseDialog(mActivity, "提示", "只有家长才可以预约呦\n去注册?", new DialogUtils.OnChooseListener() {
                @Override
                public void onChoose(boolean sure) {
                    if(sure && null != mActivity){
                        Bundle bundle = new Bundle();
                        bundle.putInt(Constants.Key.TO_PARENT_REGISTER_ACTIVITY_TYPE, Constants.Identifier.TYPE_REGISTER);
                        mActivity.redirectToActivity(mActivity, ParentRegisterActivity.class, bundle);
                    }
                }
            });
            return;
        }

        if(mOrders.get(index).getTeacher().get_id().equals(App.getUser().get_id())){
            toast("您不能预约自己呦");
            return;
        }

        MapUtils.getDurationFromPosition(mOrders.get(index).getTeacher().getPosition().getLatitude(), mOrders.get(index).getTeacher().getPosition().getLongitude(),
                App.getUser().getPosition().getLatitude(), App.getUser().getPosition().getLongitude(),
                mOrders.get(index).getTeacher().getPosition().getCity(), new MapUtils.GetDurationCallback() {
                    @Override
                    public void onSuccess(int durationSeconds) {
                        if (null == mActivity) {
                            return;
                        }

                        if (durationSeconds / 60 > mOrders.get(index).getTeacher().getTeacherMessage().getMaxTrafficTime()) {
                            DialogUtils.makeConfirmDialog(mActivity, "温馨提示", "您与家教老师的距离已超过他（她）设定的最远距离，试试别的老师吧。");
                        } else {
                            if (durationSeconds / 60 < mOrders.get(index).getTeacher().getTeacherMessage().getFreeTrafficTime()) {
                                mOrders.get(index).setSubsidy(0);
                            }

                            Bundle bundle = new Bundle();
                            bundle.putInt(Constants.Key.RESERVE_WAY, getReserveType());
                            bundle.putSerializable(Constants.Key.ORDER, mOrders.get(index));
                            bundle.putInt(Constants.Key.TO_TEACHER_INFO_ACTIVITY_TYPE, Constants.Identifier.TYPE_RESERVE);
                            mActivity.redirectToActivity(mActivity, TeacherInfoActivity.class, bundle);
                        }
                    }

                    @Override
                    public void onFailed(String errorMsg) {
                        LogUtils.i(Constants.Tag.MAP_TAG, errorMsg);

                        if (null == mActivity) {
                            return;
                        }
                        DialogUtils.makeConfirmDialog(mActivity, "温馨提示", "您与家教老师的距离已超过他（她）设定的最远距离，试试别的老师吧。");
                    }
                });
    }

    public int getReserveType() {
        return mReserveType;
    }

    public void setReserveType(int mReserveType) {
        this.mReserveType = mReserveType;
    }

    public void setOrders(List<Order> orders) {
        this.mOrders = orders;
    }
}
