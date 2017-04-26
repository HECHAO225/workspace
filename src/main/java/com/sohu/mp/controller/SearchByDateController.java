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
import com.sohu.mp.newsDAO.DataManage;
import com.sohu.mp.service.CMSIdSearchService;
import com.sohu.mp.service.IndexService;
import com.sohu.mp.service.NewsService;
import com.sohu.mp.service.NewsTimeService;
import com.sohu.mp.service.ScheduleService;

@Controller
@RequestMapping("mpNews")
public class SearchByDateController {
    
    @Resource
    private NewsTimeService newsTimeService;
    @Resource
    private ScheduleService scheduleService; 
    @Resource
    private IndexService indexService;
    @Resource
    private CMSIdSearchService cmsSearch;
    
    private final Logger logger = LoggerFactory.getLogger(ProfileSearchController.class);

    @RequestMapping("test")
    public void test(){
        newsTimeService.getNews();
    }
    
    @RequestMapping("start")
    public void index(HttpServletResponse response){
        DataManage.timeIsUpdate = false;
        if(newsTimeService.getNews()){
            DataManage.timeIsUpdate = true;
            sendJsonResponse(response, true, null, null);
        }
        else{
            DataManage.timeIsUpdate = false;
            sendJsonResponse(response, false, null, null);
        }
    }
    
    @RequestMapping("update")
    public void update(HttpServletResponse response){
        if(scheduleService.updateByTime())
            sendJsonResponse(response, true, null, null);
        else
            sendJsonResponse(response, false, null, null);
    }
    
    @RequestMapping("delete")
    public void delete(HttpServletResponse response){
        try{
            indexService.deleteIndex(DataManage.newsTimeIndexDir);
            sendJsonResponse(response, true, null, null);
        }catch(Exception e){
            e.printStackTrace();
            sendJsonResponse(response, false, null, null);
        }
    }
    
    @RequestMapping("search")
    public void search(HttpServletRequest request, HttpServletResponse response){
        
        String passport = request.getParameter("passport");
        String cmsId = request.getParameter("cmsid");
        Map<String, String> map = new HashMap<String, String>();
        map.put(IndexService.MPMEDIAID, passport);
        map.put(IndexService.CMSID, cmsId);
        try{
            Map<String, String> result = cmsSearch.searchCMSId(map);
            if(result==null)
                sendJsonResponse(response, false, null, null);
            else
                sendJsonResponse(response, true, result, null);
        }
        catch(Exception e){
            logger.info(e.getMessage());
            logger.info("search cmsID failed");
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
            json.put("id", result.get("id"));
            json.put("passport", result.get("passport"));
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
