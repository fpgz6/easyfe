package com.scut.easyfe.network.request.pay;

import com.scut.easyfe.app.App;
import com.scut.easyfe.app.Constants;
import com.scut.easyfe.network.RequestBase;
import com.scut.easyfe.network.kjFrame.http.HttpParams;
import com.scut.easyfe.network.kjFrame.http.Request;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * 微信支付
 * Created by jay on 16/6/18.
 */
public class RWechatPay extends RequestBase<JSONObject>{

    private int buy = -1;                   //0支付订单, 1支付会员活动, 2充值
    private String orderId = "";            //支付订单时传
    private String vipEventId = "";         //支付会员活动时传
    private int money = 0;                  //单位: 分

    public RWechatPay(int buy, String orderId, String vipEventId, int money) {
        this.buy = buy;
        this.orderId = orderId;
        this.vipEventId = vipEventId;
        this.money = money;
    }

    @Override
    public String getUrl() {
        return Constants.URL.URL_WECHAT_PAY;
    }

    @Override
    public JSONObject getJsonParams() throws JSONException {
        JSONObject params = new JSONObject();
        params.put("token", App.getUser().getToken());
        params.put("buy", buy);
        params.put("money", money);
        if (null != orderId && orderId.length() != 0) {
            params.put("orderId", orderId);
        }

        if (null != vipEventId && vipEventId.length() != 0) {
            params.put("vipEventId", vipEventId);
        }


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
