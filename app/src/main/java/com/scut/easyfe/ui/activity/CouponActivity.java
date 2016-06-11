package com.scut.easyfe.ui.activity;

import android.content.DialogInterface;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.scut.easyfe.R;
import com.scut.easyfe.entity.Coupon;
import com.scut.easyfe.network.RequestBase;
import com.scut.easyfe.network.RequestListener;
import com.scut.easyfe.network.RequestManager;
import com.scut.easyfe.network.request.wallet.RGetMyTicket;
import com.scut.easyfe.ui.adapter.CouponAdapter;
import com.scut.easyfe.ui.base.BaseActivity;
import com.scut.easyfe.utils.OtherUtils;

import java.util.ArrayList;
import java.util.List;

public class CouponActivity extends BaseActivity {
    private ListView mTicketListView;
    private CouponAdapter mAdapter;
    private List<Coupon> mCoupons = new ArrayList<>();
    private boolean mIsLoadingClosedByUser = true;

    @Override
    protected void setLayoutView() {
        setContentView(R.layout.activity_coupon);
    }

    @Override
    protected void initView() {
        ((TextView) OtherUtils.findViewById(this, R.id.titlebar_tv_title)).setText("我的现金券");

        mTicketListView = OtherUtils.findViewById(this, R.id.ticket_lv_coupon);

        mAdapter = new CouponAdapter(this, mCoupons);
        mTicketListView.setAdapter(mAdapter);
    }

    @Override
    protected void fetchData() {
        startLoading("加载数据中", new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(mIsLoadingClosedByUser){
                    finish();
                }
            }
        });

        RequestManager.get().execute(new RGetMyTicket(), new RequestListener<List<Coupon>>() {
            @Override
            public void onSuccess(RequestBase request, List<Coupon> result) {
                mCoupons.clear();
                mCoupons.addAll(result);
                mAdapter.notifyDataSetChanged();

                mIsLoadingClosedByUser = false;
                stopLoading();
            }

            @Override
            public void onFailed(RequestBase request, int errorCode, String errorMsg) {
                toast(errorMsg);
                mIsLoadingClosedByUser = false;
                stopLoading();
            }
        });
    }

    public void onBackClick(View view){
        finish();
    }
}