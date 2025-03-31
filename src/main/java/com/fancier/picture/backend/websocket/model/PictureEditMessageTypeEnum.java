package com.fancier.picture.backend.websocket.model;

import lombok.Getter;

/**
 * 图片编辑消息类型枚举
 */
@Getter
public enum PictureEditMessageTypeEnum {

    INFO("发送通知", "INFO"),// 如 新用户建立连接
    ERROR("发送错误", "ERROR"), // 如 参数错误需要返回错误
    ENTER_EDIT("进入编辑状态", "ENTER_EDIT"), //
    EXIT_EDIT("退出编辑状态", "EXIT_EDIT"),
    EDIT_ACTION("执行编辑操作", "EDIT_ACTION");

    private final String text;
    private final String value;

    PictureEditMessageTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     */
    public static PictureEditMessageTypeEnum of(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        for (PictureEditMessageTypeEnum typeEnum : PictureEditMessageTypeEnum.values()) {
            if (typeEnum.value.equals(value)) {
                return typeEnum;
            }
        }
        return null;
    }
}