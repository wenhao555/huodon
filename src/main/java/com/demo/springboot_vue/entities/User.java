package com.demo.springboot_vue.entities;

import lombok.Data;

import java.util.List;

@Data
public class User {
    private int id;
    private String account;
    private String password;
    private String name;
    private String sex;
    private String birth;
    private Boolean admin;
    private String image;
    private String stuNo;
    // 已报名的活动
    private List<Integer> signActivityList;
    // 审核通过的活动
    private List<Integer> accessActivityList;
    private Boolean activityAdmin;
    private Boolean access;
    private Boolean sign;
    // 已签到的活动
    private List<Integer> signOnActivityList;

}
