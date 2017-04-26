package com.sohu.mp.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONObject;
import com.sohu.mp.DAO.ProfileMapper;
import com.sohu.mp.newsDAO.DataManage;
import com.sohu.mp.service.IndexService;
import com.sohu.mp.service.ProfileSearchService;
import com.sohu.mp.service.ProfileService;
import com.sohu.mp.service.ScheduleService;

@Controller
@RequestMapping("profile")
public class ProfileSearchController {

	@Resource
	private ProfileService profileService;
	@Resource
	private ScheduleService scheduleService;
	@Resource
	private IndexService indexService;
	@Resource
	private ProfileSearchService proSearchService;
	@Resource
	private ProfileMapper profileMapper;
	
	private final Logger logger = LoggerFactory.getLogger(ProfileSearchController.class);
	
	
	@RequestMapping("start")
	public void startIndex(HttpServletResponse response){
		DataManage.profileIsUpdate = false;
		if(profileService.indexProfile()){
			DataManage.profileIsUpdate = true;
			sendJsonResponse(response, true, null, null);
		}
		else{
			DataManage.profileIsUpdate = false;
			sendJsonResponse(response, false, null, null);
		}
	}
	
	@RequestMapping("update")
	public void update(HttpServletResponse response){
		if(scheduleService.updateProfile())
			sendJsonResponse(response, true, null, null);
		else
			sendJsonResponse(response, false, null, null);
	}
	
	//删除索引
	@RequestMapping("delete")
	public void delete(HttpServletResponse response){
		try {
			indexService.deleteIndex(DataManage.profileIndexDir);
			sendJsonResponse(response, true, null, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			sendJsonResponse(response, false, null, null);
		}
	}
	
	@RequestMapping("search")
	public void search(HttpServletRequest request, HttpServletResponse response){
		Integer pno = 1;
		Integer pageSize = 20;
		
		String name = request.getParameter("name");
		String passport = request.getParameter("passport");
		if(request.getParameter("pno")!=null)
			pno = Integer.valueOf(request.getParameter("pno"));
		if(request.getParameter("pageSize")!=null)
			pageSize = Integer.valueOf(request.getParameter("pageSize"));
		Map<String, String> map = new HashMap<String, String>();
		map.put(IndexService.PROFILENAME, name);
		map.put(IndexService.PROFILEPASSPORT, passport);
		try{
			Map<String, Object> result = proSearchService.searchId(map, pno, pageSize);
			sendJsonResponse(response, true, result, pno);
		}
		catch(Exception e){
			logger.info(e.getMessage());
			logger.info("search profile failed");
			sendJsonResponse(response, false, null, null);
		}
	}
	
	private void sendJsonResponse(HttpServletResponse response, boolean success, Map result, Integer pno){
		JSONObject json = new JSONObject();
		json.put("success", success);
		if(success)
			json.put("code", 200);
		else
			json.put("code", 500);
		if(result!=null){
			List<Long> id = (ArrayList<Long>) result.get("id");
			Integer count = (Integer) result.get("count");
			json.put("content", id);
			json.put("count", count);
			json.put("pno", pno);
			json.put("size", id.size());
		}

		response.setContentType("application/json");
		try {
			response.getWriter().print(json.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
