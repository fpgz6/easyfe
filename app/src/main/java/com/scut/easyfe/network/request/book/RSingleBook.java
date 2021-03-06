package com.scut.easyfe.network.request.book;

import com.fasterxml.jackson.databind.JavaType;
import com.scut.easyfe.app.Constants;
import com.scut.easyfe.entity.book.MultiBookCondition;
import com.scut.easyfe.entity.book.SingleBookCondition;
import com.scut.easyfe.entity.order.Order;
import com.scut.easyfe.network.RequestBase;
import com.scut.easyfe.network.kjFrame.http.HttpParams;
import com.scut.easyfe.network.kjFrame.http.Request;
import com.scut.easyfe.utils.LogUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 单次预约
 * Created by jay on 16/4/12.
 */
public class RSingleBook extends RequestBase<List<Order>>{
    private SingleBookCondition mCondition = new SingleBookCondition();

    public RSingleBook(SingleBookCondition mCondition) {
        this.mCondition = mCondition;
    }

    @Override
    public String getUrl() {
        return Constants.URL.URL_SINGLE_RESERVE;
    }

    @Override
    public JSONObject getJsonParams() throws JSONException {
        JSONObject params = new JSONObject();
        if(mCondition.getToken().length() != 0) {
            params.put("token", mCondition.getToken());
        }
        params.put("course", mCondition.getCourse());
        params.put("grade", mCondition.getGrade());
        params.put("time", mCondition.getTime());
        params.put("childAge", mCondition.getChildAge());
        params.put("childGender", mCondition.getChildGender());
        params.put("singleBookTime", mCondition.getSingleBookTime().getSingleTimeConditionJson());
        params.put("score", mCondition.getScore());
        params.put("teacherName", mCondition.getTeacherName());

        JSONArray priceArray = new JSONArray();
        for (JSONArray price :
                mCondition.getPrice()) {
            priceArray.put(price);
        }
        params.put("price", priceArray);

        JSONArray schoolArray = new JSONArray();
        for (String schoolName :
                mCondition.getSchool()) {
            schoolArray.put(schoolName);
        }
        params.put("school", schoolArray);

        return params;
    }

    @Override
    public HttpParams getQueryParams() {
        return null;
    }

    @Override
    public List<Order> parseResultAsObject(JSONObject jsonObject) throws IOException, JSONException {
        List<Order> result = new ArrayList<>();
        JavaType javaType = mObjectMapper.getTypeFactory().constructParametricType(List.class, Order.class);
        try {
            JSONArray messages = jsonObject.optJSONArray("teachers");
            if(null != messages){
                /** 将返回的地址JsonArray转化为List<Order> */
                result = mObjectMapper.readValue(messages.toString(), javaType);
            }
        } catch (IOException e) {
            LogUtils.i("Json转换为List<Order>失败!");
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public int getMethod() {
        return Request.HttpMethod.POST;
    }
}
