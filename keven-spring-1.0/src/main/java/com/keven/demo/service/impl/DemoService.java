package com.keven.demo.service.impl;

import com.keven.demo.service.IDemoService;
import com.keven.mvcframework.annotation.KevenService;

/**
 * 核心业务逻辑
 */
@KevenService
public class DemoService implements IDemoService{

	public String get(String name) {
		return "My name is " + name + ",from service.";
	}

}
