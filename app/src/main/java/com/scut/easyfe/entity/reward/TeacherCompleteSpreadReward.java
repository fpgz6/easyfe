package com.scut.easyfe.entity.reward;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import com.scut.easyfe.R;
import com.scut.easyfe.app.App;

import java.util.ArrayList;
import java.util.Locale;

/**
 * 特价订单奖励
 * Created by jay on 16/5/3.
 */
public class TeacherCompleteSpreadReward extends BaseReward{
    private String detail = "";
    private float money = 0f;

    @Override
    public SpannableStringBuilder getAsString() {
        String content = "";
        content += detail;
        content += "\n";
        content += "现金奖励: ";

        int start = content.length();
        content += String.format(Locale.CHINA, "%.0f 元", money / 100);
        SpannableStringBuilder builder = new SpannableStringBuilder(content);
        builder.setSpan(new ForegroundColorSpan(App.get().getResources().getColor(R.color.theme_color)), start, content.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }

    @Override
    public boolean isReceivable() {
        return getCount() > 0;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public float getMoney() {
        return money;
    }

    public void setMoney(float money) {
        this.money = money;
    }

}
