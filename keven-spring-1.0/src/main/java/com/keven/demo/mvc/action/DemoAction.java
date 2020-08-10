package com.keven.demo.mvc.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.keven.demo.service.IDemoService;
import com.keven.mvcframework.annotation.KevenAutowired;
import com.keven.mvcframework.annotation.KevenController;
import com.keven.mvcframework.annotation.KevenRequestMapping;
import com.keven.mvcframework.annotation.KevenRequestParam;


//虽然，用法一样，但是没有功能
@KevenController
@KevenRequestMapping("/demo")
public class DemoAction {

  	@KevenAutowired
	private IDemoService demoService;

	@KevenRequestMapping("/query")
	public void query(HttpServletRequest req, HttpServletResponse resp,
					  @KevenRequestParam("name") String name){
		String result = demoService.get(name);
//		String result = "My name is " + name;
		try {
			resp.getWriter().write(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@KevenRequestMapping("/add")
	public void add(HttpServletRequest req, HttpServletResponse resp,
					@KevenRequestParam("a") Integer a, @KevenRequestParam("b") Integer b){
		try {
			resp.getWriter().write(a + "+" + b + "=" + (a + b));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@KevenRequestMapping("/sub")
	public void add(HttpServletRequest req, HttpServletResponse resp,
					@KevenRequestParam("a") Double a, @KevenRequestParam("b") Double b){
		try {
			resp.getWriter().write(a + "-" + b + "=" + (a - b));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@KevenRequestMapping("/remove")
	public String  remove(@KevenRequestParam("id") Integer id){
		return "" + id;
	}

}
