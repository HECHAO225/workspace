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

import com.sohu.mp.newsDAO.DataManage;
import com.sohu.mp.service.IndexService;
import com.sohu.mp.service.NewsService;
import com.sohu.mp.service.ScheduleService;
import com.sohu.mp.service.SearchService;

import com.alibaba.fastjson.JSONObject;

@Controller
@RequestMapping("index")
public class SearchController {
	
	@Resource
	private IndexService indexService;
	@Resource
	private SearchService searchService;
	@Resource
	private NewsService newsService;
	@Resource
	private ScheduleService scheduleService;

	private final Logger logger = LoggerFactory.getLogger(SearchController.class);
	
	//构建索引
	@RequestMapping("start")
	public void index(HttpServletResponse response){
		DataManage.isUpdate = false;
		if(newsService.indexNews()){
			DataManage.isUpdate = true;
			sendJsonResponse(response, true, null, null);
		}
		else{
			DataManage.isUpdate = false;
			sendJsonResponse(response, false, null, null);
		}
	}
	
	//更新索引
	@RequestMapping("update")
	public void update(HttpServletResponse response){
		if(scheduleService.updateJob())
			sendJsonResponse(response, true, null, null);
		else
			sendJsonResponse(response, false, null, null);
	}
	
	//删除索引
	@RequestMapping("delete")
	public void delete(HttpServletResponse response){
		try {
			indexService.deleteIndex(DataManage.indexDir);
			sendJsonResponse(response, true, null, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			sendJsonResponse(response, false, null, null);
		}
	}
	
	
	//搜索
	@RequestMapping("search")
	public void search(HttpServletRequest request, HttpServletResponse response){
		
		Integer pno = 1;  //默认页码
		Integer pageSize = 20;  //默认页大小
		
		String id = request.getParameter("id");
		String title = request.getParameter("title");  //标题查询
		String key = request.getParameter("key");     //关键字查询
		String passport = request.getParameter("passport"); //根据passportid查询
		String userName = request.getParameter("user"); //根据用户名进行查询
		String type = request.getParameter("type");//根据媒体类型查询
		String startTime = request.getParameter("start"); //起始时间
		String endTime = request.getParameter("end");  //结束时间
		if(request.getParameter("pno")!=null)
			pno = Integer.valueOf(request.getParameter("pno"));
		if(request.getParameter("pageSize")!=null)
			pageSize = Integer.valueOf(request.getParameter("pageSize"));
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(IndexService.ID, id);
		map.put(IndexService.TITLE, title);
		map.put(IndexService.AUDITWORD, key);
		map.put(IndexService.MPMEDIAID, passport);
		map.put(IndexService.USERNAME, userName);
		map.put(IndexService.MEDIATYPE, type);
		map.put(IndexService.STARTTIME, startTime);
		map.put(IndexService.ENDTIME, endTime);
		
		try {
			Map<String, Object> result = searchService.searchText(map, pno, pageSize);
			sendJsonResponse(response, true, result, pno);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.info(e.getMessage());
			logger.info("search mpNews failed");
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
			List<Map<String, String>> id = (ArrayList<Map<String, String>>) result.get("id");
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
