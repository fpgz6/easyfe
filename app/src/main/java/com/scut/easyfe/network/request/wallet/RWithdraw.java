package com.scut.easyfe.network.request.wallet;

import android.support.annotation.NonNull;

import com.scut.easyfe.app.Constants;
import com.scut.easyfe.network.RequestBase;
import com.scut.easyfe.network.kjFrame.http.HttpParams;
import com.scut.easyfe.network.kjFrame.http.Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * 提现接口
 * Created by jay on 16/4/10.
 */
public class RWithdraw extends RequestBase<JSONObject>{
    private String mToken = "";
    private float mMoney = 0f;
    private JSONObject mWay = new JSONObject();

    public RWithdraw(@NonNull String mToken, @NonNull float mMoney, @NonNull JSONObject way) {
        this.mToken = mToken;
        this.mMoney = mMoney;
        this.mWay = way;
    }

    @Override
    public String getUrl() {
        return Constants.URL.URL_WALLET_WITHDRAW;
    }

    @Override
    public JSONObject getJsonParams() throws JSONException {
        JSONObject params = new JSONObject();
        params.put("token", mToken);
        params.put("money", mMoney);
        params.put("way", mWay);
        return params;
    }

    @Override
    public HttpParams getQueryParams() {
        return null;
    }

    @Override
    public JSONObject parseResultAsObject(JSONObject jsonObject) throws IOException, JSONException {
        return jsonObject;
    }

    @Override
    public int getMethod() {
        return Request.HttpMethod.POST;
    }
}
