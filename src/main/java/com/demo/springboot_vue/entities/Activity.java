package com.demo.springboot_vue.entities;

import lombok.Data;

import java.util.List;

@Data
public class Activity {
    // 唯一的id
    private int id;
    // 报名的user
    private List<User> activityUserList;
    private String image;
    private String date;
    private String title;
    // 活动内容
    private String content;
    // 活动管理员
    private User activityAdminUser;
    //建议内容
    private List<String> suggestList;
    // 签退的user
    private User signOffUser;
    // 签到的user
    private List<User> signUserList;
    // 审核通过的user
    private List<User> accessUserList;
}