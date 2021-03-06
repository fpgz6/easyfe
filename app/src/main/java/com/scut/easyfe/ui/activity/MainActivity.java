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
import com.scut.easyfe.entity.user.User;
import com.scut.easyfe.network.RequestBase;
import com.scut.easyfe.network.RequestListener;
import com.scut.easyfe.network.RequestManager;
import com.scut.easyfe.network.request.RGetOnlineParams;
import com.scut.easyfe.network.request.authentication.RUpdateUser;
import com.scut.easyfe.ui.activity.reward.TaskRewardActivity;
import com.scut.easyfe.ui.base.BaseActivity;
import com.scut.easyfe.ui.base.BaseFragment;
import com.scut.easyfe.ui.customView.CircleImageView;
import com.scut.easyfe.ui.fragment.HomeFragment;
import com.scut.easyfe.utils.DialogUtils;
import com.scut.easyfe.utils.ImageUtils;
import com.scut.easyfe.utils.LogUtils;
import com.scut.easyfe.utils.OtherUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
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
    private TextView mNameTextView;
    private Map<Integer, BaseFragment> mFragments = new HashMap<>();
    private HomeFragment mHomeFragment;

    @Override
    protected void onResume() {
        super.onResume();

        if (App.getUser(false).hasLogin()) {
            mNameTextView.setText(App.getUser().getName());
            ImageUtils.displayImage(App.getUser().getAvatar(), mAvatarImageView);
        } else {
            mNameTextView.setText("登录/注册");
            mAvatarImageView.setImageResource(R.mipmap.default_avatar);
        }

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
        mHomeFragment = new HomeFragment();
        mFragments.put(FRAGMENT_HOME, mHomeFragment);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.main_container, mFragments.get(FRAGMENT_HOME))
                .commit();
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

                try {
                    JSONArray advertises = result.optJSONArray("guideMap");
                    if (advertises != null && advertises.length() > 0) {
                        String image = advertises.getJSONObject(0).optString("image");
                        String link = advertises.getJSONObject(0).optString("url");
                        ImageLoader.getInstance().displayImage(image, mAdvertiseImage);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
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

    @Override
    protected void initListener() {
    }


    /**
     * 左上角个人按钮点击侧拉
     *
     * @param view 被点击View
     */
    public void onPersonImageClick(View view) {
        if (null != mDrawerLayout) {
            mDrawerLayout.openDrawer(mLeftDrawer);
        }
    }

    /**
     * 点击我的订单
     */
    public void onMyOrderClick(View view) {
        if (App.getUser().hasLogin()) {
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
//        MapUtils.getDurationFromPosition(23.097653270780476, 113.34697650222962, 23.03, 113.345, "广州市", new MapUtils.GetDurationCallback() {
//            @Override
//            public void onSuccess(int durationSeconds) {
//                toast(durationSeconds + "");
//            }
//
//            @Override
//            public void onFailed(String errorMsg) {
//
//            }
//        });
        redirectToActivity(mContext, WalletActivity.class);
    }

    /**
     * 点击家教信息维护
     */
    public void onTeacherMsgManageClick(View view) {
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

        LogUtils.i("liujie", App.getUser().getTeacherMessage().isIsCheck() + "");
        if (!App.getUser().getTeacherMessage().isIsCheck()) {
            toast("您的信息还在审核中\n审核之后才可以发布订单喔");
            return;
        }

        redirectToActivity(mContext, PublishSpreadActivity.class);
    }

    /**
     * 点击消息中心
     */
    public void onMessageCenterClick(View view) {
        if (App.getUser().hasLogin()) {
            redirectToActivity(mContext, MessageCenterActivity.class);
        }
    }

    /**
     * 点击任务奖励
     */
    public void onTaskRewardClick(View view) {
        if (App.getUser(false).hasLogin()) {
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
        onCloseAdvertiseClick(view);
    }
}
