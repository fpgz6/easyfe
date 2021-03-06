package com.scut.easyfe.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.scut.easyfe.R;
import com.scut.easyfe.app.App;
import com.scut.easyfe.app.Constants;
import com.scut.easyfe.ui.activity.CallbackActivity;
import com.scut.easyfe.ui.activity.BookActivity;
import com.scut.easyfe.ui.activity.SpecialOrderActivity;
import com.scut.easyfe.ui.activity.TeacherRegisterOneActivity;
import com.scut.easyfe.ui.activity.TeacherRegisterTwoActivity;
import com.scut.easyfe.ui.adapter.ImagePagerAdapter;
import com.scut.easyfe.ui.base.BaseFragment;
import com.scut.easyfe.ui.customView.CircleIndicator;
import com.scut.easyfe.ui.customView.ScrollableViewPager;
import com.scut.easyfe.utils.OtherUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 主页Fragment
 * Created by jay on 16/3/15.
 */
public class HomeFragment extends BaseFragment implements View.OnClickListener{
    private TextView mLineOneTextView;
    private TextView mLineTwoTextView;
    private TextView mLineThreeTextView;
    private TextView mHintTextView;
    private RelativeLayout mAdvertiseRelativeLayout;
    private ScrollableViewPager mAdvertiseViewPager;
    private CircleIndicator mAdvertiseIndicator;
    private ImagePagerAdapter mAdvertisePagerAdapter;

    @Override
    protected void setLayoutRes() {
        layoutRes = R.layout.fragment_home;
    }

    @Override
    protected void initView(View v) {
        mLineOneTextView = OtherUtils.findViewById(v, R.id.home_main_text);
        mLineTwoTextView = OtherUtils.findViewById(v, R.id.home_second_text);
        mLineThreeTextView = OtherUtils.findViewById(v, R.id.home_third_text);
        mHintTextView = OtherUtils.findViewById(v, R.id.home_need_report);

        mAdvertiseRelativeLayout = OtherUtils.findViewById(v, R.id.home_rl_advertise_container);
        mAdvertiseViewPager = OtherUtils.findViewById(v, R.id.home_vp_advertise);
        mAdvertiseIndicator = OtherUtils.findViewById(v, R.id.home_indicator_advertise);

        /** 展示页所用图片的 ID 们 */
        List<Integer> imageResourceIds = new ArrayList<>();

        imageResourceIds.add(R.mipmap.image_splash_1);
        imageResourceIds.add(R.mipmap.image_splash_2);
        imageResourceIds.add(R.mipmap.image_splash_3);

        mAdvertisePagerAdapter = new ImagePagerAdapter(imageResourceIds);

        mAdvertiseViewPager.setAdapter(mAdvertisePagerAdapter);
//        mAdvertiseViewPager.setPageTransformer(true,new DepthPageTransformer());
        mAdvertiseIndicator.setViewPager(mAdvertiseViewPager);

        initAdvertiseHeight();
    }

    private void initAdvertiseHeight(){
        int height = 4;
        int width = 7;
        int widthPx = OtherUtils.getScreenWidth(mActivity);
        int heightPx = widthPx * height / width;

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mAdvertiseRelativeLayout.getLayoutParams();
        params.width = widthPx;
        params.height = heightPx;
        mAdvertiseRelativeLayout.setLayoutParams(params);
    }

    @Override
    protected void initListener(View v) {
        super.initListener(v);
        v.findViewById(R.id.home_main_text).setOnClickListener(this);
        v.findViewById(R.id.home_second_text).setOnClickListener(this);
        v.findViewById(R.id.home_third_text).setOnClickListener(this);
        v.findViewById(R.id.home_vertical_divider).setOnClickListener(this);
        v.findViewById(R.id.home_need_report).setOnClickListener(this);
        v.findViewById(R.id.home_book_multi).setOnClickListener(this);
        v.findViewById(R.id.home_book_multi_text).setOnClickListener(this);
        v.findViewById(R.id.home_book_once).setOnClickListener(this);
        v.findViewById(R.id.home_book_once_text).setOnClickListener(this);
        v.findViewById(R.id.home_teacher).setOnClickListener(this);
        v.findViewById(R.id.home_teacher_text).setOnClickListener(this);

        mAdvertisePagerAdapter.setOnItemClickListener(new ImagePagerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                toast(position + "");
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.home_main_text:
            case R.id.home_second_text:
            case R.id.home_third_text:
            case R.id.home_vertical_divider:
                onSpecialOrderClick(v);
                break;

            case R.id.home_need_report:
                onNeedReportClick(v);
                break;

            case R.id.home_book_multi:
            case R.id.home_book_multi_text:
                onMultiReserveClick(v);
                break;

            case R.id.home_book_once:
            case R.id.home_book_once_text:
                onOnceReserveClick(v);
                break;

            case R.id.home_teacher:
            case R.id.home_teacher_text:
                onTeacherClick(v);
                break;

            default:
                break;
        }
    }

    /**
     * 点击特价订单
     * @param view 被点击视图
     */
    private void onSpecialOrderClick(View view){
        if(null != mActivity){
            mActivity.redirectToActivity(mActivity, SpecialOrderActivity.class);
        }
    }

    /**
     * 点击反馈需求
     * @param view 被点击视图
     */
    private void onNeedReportClick(View view){
        if(null != mActivity) {
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.Key.CALLBACK_TYPE, Constants.Identifier.CALLBACK_NEED);
            bundle.putString(Constants.Key.CALLBACK_HINT_TEXT, getResources().getString(R.string.home_report_callback_hint));
            mActivity.redirectToActivity(mActivity, CallbackActivity.class, bundle);
        }
    }

    /**
     * 点击多次预约
     * @param view 被点击视图
     */
    private void onMultiReserveClick(View view){
        if(null != mActivity){
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.Key.RESERVE_WAY, Constants.Identifier.RESERVE_MULTI);
            mActivity.redirectToActivity(mActivity, BookActivity.class, bundle);
        }
    }

    /**
     * 点击单次预约
     * @param view 被点击视图
     */
    private void onOnceReserveClick(View view){
        if(null != mActivity){
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.Key.RESERVE_WAY, Constants.Identifier.RESERVE_SINGLE);
            mActivity.redirectToActivity(mActivity, BookActivity.class, bundle);
        }
    }

    /**
     * 点击我是家教
     * @param view 被点击视图
     */
    private void onTeacherClick(View view){
        if(null != mActivity) {
            if(!App.getUser(false).isTeacher()) {
                Bundle bundle = new Bundle();
                bundle.putInt(Constants.Key.TO_TEACHER_REGISTER_ONE_ACTIVITY_TYPE, Constants.Identifier.TYPE_REGISTER);
                mActivity.redirectToActivity(mActivity, TeacherRegisterOneActivity.class);
            }else{

                Bundle extras = new Bundle();
                extras.putBoolean(Constants.Key.IS_REGISTER, false);
                mActivity.redirectToActivity(mActivity, TeacherRegisterTwoActivity.class, extras);
            }
        }
    }

    /**
     * 设置首页的文字
     * @param line1     第一行文字(类似特价推广)
     * @param line2     第二行文字(类似低至1元/小时)
     * @param line3     第三行文字
     * @param hint      提示需求反馈文字
     */
    public void setText(String line1, String line2, String line3, String hint){
        mLineOneTextView.setText(line1);
        mLineTwoTextView.setText(line2);
        mLineThreeTextView.setText(line3);
        mHintTextView.setText(hint);
    }
}
