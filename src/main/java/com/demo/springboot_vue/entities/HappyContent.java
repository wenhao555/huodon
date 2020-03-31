package com.demo.springboot_vue.entities;

import lombok.Data;

@Data
public class HappyContent {
	// 唯一的id
	private int id;
	// 活动内容
	private String content;
	private String title;
	private String image;
}
