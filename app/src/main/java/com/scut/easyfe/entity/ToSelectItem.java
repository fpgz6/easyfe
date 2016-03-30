package com.scut.easyfe.entity;

/**
 * 供选择的选项实体类
 * Created by jay on 16/3/30.
 */
public class ToSelectItem extends BaseEntity{
    private String text;              //被选择内容
    private boolean selected = false; //是否被选中

    public ToSelectItem(String text, boolean selected) {
        this.text = text;
        this.selected = selected;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
