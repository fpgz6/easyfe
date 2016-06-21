package com.scut.easyfe.ui.activity;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.scut.easyfe.R;
import com.scut.easyfe.app.App;
import com.scut.easyfe.app.Constants;
import com.scut.easyfe.app.Variables;
import com.scut.easyfe.entity.PollingData;
import com.scut.easyfe.entity.user.User;
import com.scut.easyfe.event.DataChangeEvent;
import com.scut.easyfe.event.PDHandler;
import com.scut.easyfe.network.RequestBase;
import com.scut.easyfe.network.RequestListener;
import com.scut.easyfe.network.RequestManager;
import com.scut.easyfe.network.request.RGetOnlineParams;
import com.scut.easyfe.network.request.authentication.RUpdateUser;
import com.scut.easyfe.ui.activity.auth.LoginActivity;
import com.scut.easyfe.ui.activity.auth.ParentRegisterActivity;
import com.scut.easyfe.ui.activity.auth.TeacherRegisterOneActivity;
import com.scut.easyfe.ui.activity.auth.TeacherRegisterTwoActivity;
import com.scut.easyfe.ui.activity.order.MyOrderActivity;
import com.scut.easyfe.ui.activity.order.PublishSpreadActivity;
import com.scut.easyfe.ui.activity.reward.InviteRewardActivity;
import com.scut.easyfe.ui.activity.reward.TaskRewardActivity;
import com.scut.easyfe.ui.base.BaseActivity;
import com.scut.easyfe.ui.base.BaseFragment;
import com.scut.easyfe.ui.customView.CircleImageView;
import com.scut.easyfe.ui.fragment.HomeFragment;
import com.scut.easyfe.utils.DialogUtils;
import com.scut.easyfe.utils.ImageUtils;
import com.scut.easyfe.utils.OtherUtils;
import com.scut.easyfe.utils.polling.PollingUtil;

import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 程序主界面
 *
 * @author jay
 */
public class MainActivity extends BaseActivity {
    private static final int FRAGMENT_HOME = 0;
    private DrawerLayout mDrawerLayout;
    private LinearLayout mLeftDrawer;
    private CircleImageView mAvatarImageView;
    private RelativeLayout mAdvertiseRelativeLayout;
    private ImageView mAdvertiseImage;
    private ImageView mPresentNewImageView;
    private TextView mNameTextView;
    private Map<Integer, BaseFragment> mFragments = new HashMap<>();
    private HomeFragment mHomeFragment;

    private String mAdvertiseLink = "";
    private List<String> mGuideMapImages = new ArrayList<>();
    private List<String> mGuideMapLinks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        PollingUtil.start(getApplicationContext());
        App.get().getEventBus().register(mContext);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PollingUtil.stop();
        App.get().getEventBus().unregister(mContext);
    }

    @Override
    protected void onResume() {
        super.onResume();

        initAvatarAndName();

        boolean isTeacher = App.getUser(false).isTeacher();
//        mLeftDrawer.findViewById(R.id.left_drawer_tv_wallet).setVisibility(isTeacher ? View.VISIBLE : View.GONE);
//        mLeftDrawer.findViewById(R.id.left_drawer_tv_divider_wallet).setVisibility(isTeacher ? View.VISIBLE : View.GONE);
        mLeftDrawer.findViewById(R.id.left_drawer_tv_special_order).setVisibility(isTeacher ? View.VISIBLE : View.GONE);
        mLeftDrawer.findViewById(R.id.left_drawer_tv_divider_special_order).setVisibility(isTeacher ? View.VISIBLE : View.GONE);
        mLeftDrawer.findViewById(R.id.left_drawer_tv_teacher_info).setVisibility(isTeacher ? View.VISIBLE : View.GONE);
        mLeftDrawer.findViewById(R.id.left_drawer_tv_divider_teacher_info).setVisibility(isTeacher ? View.VISIBLE : View.GONE);

        mAdvertiseRelativeLayout = OtherUtils.findViewById(this, R.id.main_rl_advertisement);
        mAdvertiseImage = OtherUtils.findViewById(this, R.id.main_iv_advertisement_image);
    }

    private void initAvatarAndName() {
        if (App.getUser(false).hasLogin()) {
            if (App.getUser().isParent()) {
                mNameTextView.setText(App.getUser().getName());
            } else {
                String nameText = App.getUser().getName();
                switch (App.getUser().getTeacherMessage().getCheckType()) {
                    case Constants.Identifier.TEACHER_UNCHECK:
                        nameText += "(审核中)";
                        break;

                    case Constants.Identifier.TEACHER_REJECT:
                        nameText += "(审核不通过,点击重新审核)";
                        break;
                }

                mNameTextView.setText(nameText);
            }
            ImageUtils.displayImage(App.getUser().getAvatar(), mAvatarImageView);
        } else {
            mNameTextView.setText("登录/注册");
            mAvatarImageView.setImageResource(R.mipmap.default_avatar);
        }
    }

    @Override
    protected void setLayoutView() {
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void initView() {
        ((TextView) OtherUtils.findViewById(this, R.id.main_tv_title)).setText(R.string.app_name);
        mDrawerLayout = OtherUtils.findViewById(this, R.id.drawer_layout);
        mLeftDrawer = OtherUtils.findViewById(this, R.id.drawer);
        mAvatarImageView = OtherUtils.findViewById(this, R.id.left_drawer_civ_avatar);
        mNameTextView = OtherUtils.findViewById(this, R.id.left_drawer_tv_name);
        mPresentNewImageView = OtherUtils.findViewById(this, R.id.main_iv_present_new);

        mHomeFragment = new HomeFragment();
        mFragments.put(FRAGMENT_HOME, mHomeFragment);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.main_container, mFragments.get(FRAGMENT_HOME))
                .commit();

        //onCreate的时候显示最新的状态
        if(!PDHandler.get().getLatestData().equals(new PollingData())) {
            onEvent(new DataChangeEvent(PDHandler.get().getLatestData()));
        }
    }

    @Override
    protected void fetchData() {
        startLoading("加载数据中");
        RequestManager.get().execute(new RGetOnlineParams(), new RequestListener<JSONObject>() {
            @Override
            public void onSuccess(RequestBase request, JSONObject result) {
                mHomeFragment.setText(result.optJSONObject("specialText").optString("line1"),
                        result.optJSONObject("specialText").optString("line2"),
                        result.optJSONObject("specialText").optString("line3"),
                        result.optJSONObject("specialText").optString("bottomText"));

                ((TextView) OtherUtils.findViewById(mLeftDrawer, R.id.left_drawer_tv_bottom_hint)).setText(result.optString("sidebarBottomText"));

                App.setQNToken(result.optString("qnToken"));
                App.setServicePhone(result.optString("phone"));
                Variables.TUTOR_PRICE = result.optInt("professionalPrice");

                JSONObject advertise = result.optJSONObject("advertise");
                if (advertise != null) {
                    String image = advertise.optString("image");
                    mAdvertiseLink = advertise.optString("link");
                    if (image != null && image.length() != 0) {
                        ImageLoader.getInstance().displayImage(image, mAdvertiseImage);

                    } else {
                        onCloseAdvertiseClick(null);
                    }

                } else {
                    onCloseAdvertiseClick(null);
                }

                JSONArray guideMap = result.optJSONArray("guideMap");
                if (guideMap != null) {
                    for (int i = 0; i < guideMap.length(); i++) {
                        try {
                            mGuideMapImages.add(guideMap.getJSONObject(i).optString("image"));
                            mGuideMapLinks.add(guideMap.getJSONObject(i).optString("link"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (mHomeFragment != null) {
                    if (0 == mGuideMapImages.size()) {

                    } else {
                        mHomeFragment.setAdvertiseImages(mGuideMapImages, mGuideMapLinks);
                        mHomeFragment.notifyAdvertiseChange();
                    }
                }

                stopLoading();
            }

            @Override
            public void onFailed(RequestBase request, int errorCode, String errorMsg) {
                stopLoading();
            }
        });

        if (App.getUser(false).getToken().length() != 0) {
            RequestManager.get().execute(new RUpdateUser(App.getUser(false).getToken()), new RequestListener<User>() {
                @Override
                public void onSuccess(RequestBase request, User result) {
                    App.setUser(result);
                    Toast.makeText(App.get().getApplicationContext(), "更新用户信息成功", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailed(RequestBase request, int errorCode, String errorMsg) {
                    Toast.makeText(App.get().getApplicationContext(), errorMsg, Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    public void onNameClick(View view){
        if(App.getUser().getTeacherMessage().getCheckType() == Constants.Identifier.TEACHER_REJECT){
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.Key.TO_TEACHER_REGISTER_ONE_ACTIVITY_TYPE, Constants.Identifier.TYPE_REGISTER);
            redirectToActivity(mContext, TeacherRegisterOneActivity.class, bundle);

        }else {
            onAvatarClick(view);
        }
    }

    /**
     * 左上角个人按钮点击侧拉
     *
     * @param view 被点击View
     */
    public void onPersonImageClick(View view) {
//        Variables.localData = new PollingData();
//        toast("清除了本地轮询数据缓存");
        if (null != mDrawerLayout) {
            mDrawerLayout.openDrawer(mLeftDrawer);
        }
    }


    /**
     * 点击我的等级
     *
     * @param view 被点击View
     */
    public void onLevelClick(View view) {
        if (App.getUser().hasLogin()) {

            if(App.getUser().isTeacher() && (!App.getUser().getTeacherMessage().isChecked())){
                toast(Constants.Config.TEACHER_UNCHECKED_INFO);
                return;
            }

            ((TextView) OtherUtils.findViewById(this, R.id.left_drawer_tv_level)).
                    setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
            Variables.localData.getMine().setLevel(
                    PDHandler.get().getLatestData().getMine().getLevel());
            Variables.localData.equals(PDHandler.get().getLatestData(), true);
            Variables.localData.save2Cache(App.getUser().getPhone());

            redirectToActivity(this, LevelActivity.class);
        }
    }

    /**
     * 点击我的订单
     */
    public void onMyOrderClick(View view) {
        if (App.getUser().hasLogin()) {

            if(App.getUser().isTeacher() && (!App.getUser().getTeacherMessage().isChecked())){
                toast(Constants.Config.TEACHER_UNCHECKED_INFO);
                return;
            }

            redirectToActivity(mContext, MyOrderActivity.class);
        }
    }

    /**
     * 点击头像
     *
     * @param view 被点击视图
     */
    public void onAvatarClick(View view) {
        if (App.getUser().hasLogin()) {
            if (App.getUser().getType() == Constants.Identifier.USER_PARENT) {
                Bundle bundle = new Bundle();
                bundle.putInt(Constants.Key.TO_PARENT_REGISTER_ACTIVITY_TYPE, Constants.Identifier.TYPE_MODIFY);
                redirectToActivity(mContext, ParentRegisterActivity.class, bundle);

            } else if (App.getUser().getType() == Constants.Identifier.USER_TEACHER ||
                    App.getUser().getType() == Constants.Identifier.USER_TP) {

                if((!App.getUser().getTeacherMessage().isChecked())){
                    toast(Constants.Config.TEACHER_UNCHECKED_INFO);
                    return;
                }

                Bundle bundle = new Bundle();
                bundle.putInt(Constants.Key.TO_TEACHER_REGISTER_ONE_ACTIVITY_TYPE, Constants.Identifier.TYPE_MODIFY);
                redirectToActivity(mContext, TeacherRegisterOneActivity.class, bundle);

            }

        } else {
            redirectToActivity(mContext, LoginActivity.class);
        }
    }

    /**
     * 点击我的钱包
     */
    public void onPocketClick(View view) {
        if (App.getUser().hasLogin()) {

            if(App.getUser().isTeacher() && (!App.getUser().getTeacherMessage().isChecked())){
                toast(Constants.Config.TEACHER_UNCHECKED_INFO);
                return;
            }

            ((TextView) OtherUtils.findViewById(this, R.id.left_drawer_tv_wallet)).
                    setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
            Variables.localData.getMine().setWallet(
                    PDHandler.get().getLatestData().getMine().getWallet());
            Variables.localData.equals(PDHandler.get().getLatestData(), true);
            Variables.localData.save2Cache(App.getUser().getPhone());

            redirectToActivity(mContext, WalletActivity.class);
        }
    }

    /**
     * 点击我的会员活动
     */
    public void onMyVipActivityClick(View view) {
        if (!App.getUser().hasLogin()) {
            return;
        }

        if(App.getUser().isTeacher() && (!App.getUser().getTeacherMessage().isChecked())){
            toast(Constants.Config.TEACHER_UNCHECKED_INFO);
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putBoolean(Constants.Key.IS_MY_VIP_ACTIVITY, true);
        redirectToActivity(this, VipActivity.class, bundle);
    }

    /**
     * 点击反馈报告
     */
    public void onReportClick(View view) {
        if (!App.getUser().hasLogin()) {
            return;
        }

        if(App.getUser().isTeacher() && (!App.getUser().getTeacherMessage().isChecked())){
            toast(Constants.Config.TEACHER_UNCHECKED_INFO);
            return;
        }

        redirectToActivity(this, FeedbackReportActivity.class);
    }

    public void onTeacherMsgManageClick(View view) {

        if(App.getUser().isTeacher() && (!App.getUser().getTeacherMessage().isChecked())){
            toast(Constants.Config.TEACHER_UNCHECKED_INFO);
            return;
        }

        Bundle extras = new Bundle();
        extras.putBoolean(Constants.Key.IS_REGISTER, false);
        redirectToActivity(this, TeacherRegisterTwoActivity.class, extras);
    }

    /**
     * 点击推广(特价订单)
     */
    public void onSpreadClick(View view) {
        if (!App.getUser().isTeacher()) {
            return;
        }

        if(App.getUser().isTeacher() && (!App.getUser().getTeacherMessage().isChecked())){
            toast(Constants.Config.TEACHER_UNCHECKED_INFO);
            return;
        }

        if (Constants.Identifier.TEACHER_REJECT == App.getUser().getTeacherMessage().getCheckType()) {
            toast("您的信息审核未通过\n请重新提交审核后才能发布订单喔");
            return;
        }

        redirectToActivity(mContext, PublishSpreadActivity.class);
    }

    /**
     * 点击消息中心
     */
    public void onMessageCenterClick(View view) {
        if (App.getUser().hasLogin()) {
            ((TextView) OtherUtils.findViewById(this, R.id.left_drawer_tv_message)).
                    setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
            Variables.localData.getCommon().setMessage(
                    PDHandler.get().getLatestData().getCommon().getMessage());
            Variables.localData.equals(PDHandler.get().getLatestData(), true);
            Variables.localData.save2Cache(App.getUser().getPhone());

            redirectToActivity(mContext, MessageCenterActivity.class);
        }
    }

    /**
     * 点击任务奖励
     */
    public void onTaskRewardClick(View view) {
        if (App.getUser(false).hasLogin()) {
            if(App.getUser().isTeacher() && (!App.getUser().getTeacherMessage().isChecked())){
                toast(Constants.Config.TEACHER_UNCHECKED_INFO);
                return;
            }

            redirectToActivity(mContext, TaskRewardActivity.class);

        } else {
            DialogUtils.makeConfirmDialog(mContext, null, "亲，您需要先注册/登陆哦\n\n ^-^");
        }
    }

    /**
     * 点击任务奖励
     */
    public void onPresentClick(View view) {
        onTaskRewardClick(view);
    }

    /**
     * 点击邀请有奖
     */
    public void onInviteRewardClick(View view) {
        if (App.getUser().hasLogin()) {
            if(App.getUser().isTeacher() && (!App.getUser().getTeacherMessage().isChecked())){
                toast(Constants.Config.TEACHER_UNCHECKED_INFO);
                return;
            }

            redirectToActivity(mContext, InviteRewardActivity.class);
        }
    }

    /**
     * 点击联系我们
     */
    public void onContactUsClick(View view) {
        redirectToActivity(mContext, ContactUsActivity.class);
    }

    /**
     * 点击更多
     */
    public void onMoreClick(View view) {
        redirectToActivity(mContext, MoreActivity.class);
    }

    public void onCloseAdvertiseClick(View view) {
        mAdvertiseRelativeLayout.setVisibility(View.GONE);
    }

    public void onAdvertiseClick(View view) {
        if (null != mAdvertiseLink && mAdvertiseLink.length() != 0) {
            Bundle bundle = new Bundle();
            bundle.putString(Constants.Key.WEB_TITLE, "推广");
            bundle.putString(Constants.Key.WEB_URL, mAdvertiseLink);
            redirectToActivity(this, WebActivity.class, bundle);
        }
        onCloseAdvertiseClick(view);
    }

    @Subscribe
    public void onEvent(DataChangeEvent event) {
        if (null == mHomeFragment) {
            return;
        }

        mHomeFragment.setVipNewState(Variables.localData.getCommon().isVipNew(event.getData().getCommon()));

        ((TextView) OtherUtils.findViewById(this, R.id.left_drawer_tv_message)).
                setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0,
                        Variables.localData.getCommon().getMessage().
                                isMessageNew(event.getData().getCommon().getMessage()) ?
                                R.mipmap.icon_red_point_padding : 0, 0);

        ((TextView) OtherUtils.findViewById(this, R.id.left_drawer_tv_wallet)).
                setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0,
                        Variables.localData.getMine().isWalletNew(event.getData().getMine()) ?
                                R.mipmap.icon_red_point_padding : 0, 0);

        if (Variables.localData.getMine().isCheckTypeNew(event.getData().getMine())) {
            if (App.getUser().isTeacher() && null != mNameTextView) {
                Variables.localData.getMine().setCheckType(event.getData().getMine().getCheckType());
                App.getUser().getTeacherMessage().setCheckType((int) event.getData().getMine().getCheckType());
                initAvatarAndName();
                mNameTextView.setTextColor(getResources().getColor(event.getData().getMine().getCheckType() == Constants.Identifier.TEACHER_REJECT ? R.color.info_color : R.color.white));
            }
        }

        ((TextView) OtherUtils.findViewById(this, R.id.left_drawer_tv_level)).
                setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0,
                        Variables.localData.getMine().isLevelNew(event.getData().getMine()) ?
                                R.mipmap.icon_red_point_padding : 0, 0);

        ((TextView) OtherUtils.findViewById(this, R.id.left_drawer_tv_reward)).
                setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0,
                        Variables.localData.getMine().getReward().isRewardNew(event.getData().getMine().getReward()) ?
                                R.mipmap.icon_red_point_padding : 0, 0);

        ((TextView) OtherUtils.findViewById(this, R.id.left_drawer_tv_order)).
                setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0,
                        Variables.localData.getMine().isOrderNew(event.getData().getMine()) ?
                                R.mipmap.icon_red_point_padding : 0, 0);


        mPresentNewImageView.setVisibility(
                Variables.localData.getMine().getReward().isRewardNew(event.getData().getMine().getReward()) ?
                        View.VISIBLE : View.INVISIBLE);
    }
}
