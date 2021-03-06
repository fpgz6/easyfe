package com.scut.easyfe.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.scut.easyfe.app.App;
import com.scut.easyfe.app.Constants;
import com.scut.easyfe.entity.order.BriefOrder;
import com.scut.easyfe.entity.order.Order;
import com.scut.easyfe.network.RequestBase;
import com.scut.easyfe.network.RequestListener;
import com.scut.easyfe.network.RequestManager;
import com.scut.easyfe.network.request.order.RGetOrderDetail;
import com.scut.easyfe.network.request.order.RGetOrders;
import com.scut.easyfe.ui.activity.EvaluationActivity;
import com.scut.easyfe.ui.activity.ReservedOrCompletedOrderActivity;
import com.scut.easyfe.ui.activity.ToDoOrderActivity;
import com.scut.easyfe.ui.adapter.MyOrderAdapter;
import com.scut.easyfe.ui.base.BaseRefreshFragment;
import com.scut.easyfe.utils.DensityUtil;
import com.scut.easyfe.utils.OtherUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 我的订单页面使用的Fragment
 *
 * @author jay
 */
public class MyOrderFragment extends BaseRefreshFragment {
    private int mOrderType;
    private int mState = Constants.Identifier.STATE_NORMAL;
    private ArrayList<BriefOrder> mOrders = new ArrayList<>();

    @Override
    protected void initView(View view) {
        super.initView(view);

        if (null == getActivity()) {
            return;
        }

        View headView = new View(getActivity());
        headView.setMinimumHeight(DensityUtil.dip2px(mActivity, 5));
        headView.setBackground(null);

        mDataListView.addHeaderView(headView);
        mDataListView.setDividerHeight(DensityUtil.dip2px(mActivity, 5));
        mAdapter = new MyOrderAdapter(getActivity(), mOrders);
        ((MyOrderAdapter) mAdapter).setState(mState);
        setBaseAdapter(mAdapter);
    }

    @Override
    protected void onLoadingData() {
        RequestManager.get().execute(
                new RGetOrders(App.getUser().getToken(), mOrderType, Constants.DefaultValue.DEFAULT_LOAD_COUNT, mOrders.size()),
                new RequestListener<List<BriefOrder>>() {
                    @Override
                    public void onSuccess(RequestBase request, List<BriefOrder> result) {
                        mOrders.addAll(result);
                        setIsLoading(false);
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailed(RequestBase request, int errorCode, String errorMsg) {
                        toast(errorMsg);
                        setIsLoading(false);
                    }
                });
    }

    @Override
    protected void onRefreshData() {
        RequestManager.get().execute(
                new RGetOrders(App.getUser().getToken(), mOrderType, Constants.DefaultValue.DEFAULT_LOAD_COUNT, 0),
                new RequestListener<List<BriefOrder>>() {
                    @Override
                    public void onSuccess(RequestBase request, List<BriefOrder> result) {
                        mOrders.clear();
                        mOrders.addAll(result);
                        setIsLoading(false);
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailed(RequestBase request, int errorCode, String errorMsg) {
                        toast(errorMsg);
                        setIsLoading(false);
                    }
                });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(OtherUtils.isFastDoubleClick())
        if(position <= 0){
            return;
        }

        if(mState == Constants.Identifier.STATE_EDIT){
            return;
        }

        if(mOrderType == Constants.Identifier.ORDER_INVALID){
            return;
        }

        RequestManager.get().execute(new RGetOrderDetail(App.getUser().getToken(),
                mOrders.get(position - 1).get_id()), new RequestListener<Order>() {
            @Override
            public void onSuccess(RequestBase request, Order result) {
                goDetail(result);
            }

            @Override
            public void onFailed(RequestBase request, int errorCode, String errorMsg) {
                toast(errorMsg);
            }
        });
    }

    private void goDetail(Order order) {
        if (null == mActivity) {
            return;
        }

        Bundle bundle = new Bundle();
        switch (order.getState()) {
            case Constants.Identifier.ORDER_RESERVATION:
            case Constants.Identifier.ORDER_MODIFIED_WAILT_CONFIRM:
                bundle.putInt(Constants.Key.ORDER_TYPE, Constants.Identifier.ORDER_RESERVATION);
                bundle.putSerializable(Constants.Key.ORDER, order);
                mActivity.redirectToActivity(mActivity, ReservedOrCompletedOrderActivity.class, bundle);
                break;

            case Constants.Identifier.ORDER_COMPLETED:
                bundle.putInt(Constants.Key.ORDER_TYPE, Constants.Identifier.ORDER_COMPLETED);
                bundle.putSerializable(Constants.Key.ORDER, order);
                mActivity.redirectToActivity(mActivity,
                        !order.isHadComment() && App.getUser().isParent() ?
                                EvaluationActivity.class : ReservedOrCompletedOrderActivity.class, bundle);
                break;

            case Constants.Identifier.ORDER_TO_DO:
                bundle.putSerializable(Constants.Key.ORDER, order);
                mActivity.redirectToActivity(mActivity, ToDoOrderActivity.class, bundle);
                break;

            default:
                break;
        }
    }

    /**
     * 重新从服务器拉取数据
     */
    public void updateData(){
        if(null != mAdapter) {
            onRefreshData();
        }
    }

    public void setType(int type) {
        mOrderType = type;
    }

    public List<String> getSelectedOrderIds(){
        List<String> selectedOrders = new ArrayList<>();
        for (BriefOrder briefOrder :
                mOrders) {
            if (briefOrder.isSelected()) {
                selectedOrders.add(briefOrder.get_id());
            }
        }

        return selectedOrders;
    }

    public List<BriefOrder> getSelectedOrders(){
        List<BriefOrder> selectedOrders = new ArrayList<>();
        for (BriefOrder briefOrder :
                mOrders) {
            if (briefOrder.isSelected()) {
                selectedOrders.add(briefOrder);
            }
        }

        return selectedOrders;
    }

    public void setState(int state) {
        mState = state;
        if (null != mAdapter) {
            ((MyOrderAdapter) mAdapter).setState(state);
            mAdapter.notifyDataSetChanged();
        }
    }

    public int getState() {
        return mState;
    }
}
