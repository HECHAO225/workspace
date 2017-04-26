package com.sohu.mp.service;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.lucene.index.IndexWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sohu.mp.DAO.Db;
import com.sohu.mp.model.News;
import com.sohu.mp.newsDAO.DataManage;
import com.sohu.mp.newsDAO.NewsMapper;

@Service
public class NewsTimeService {

    @Resource
    private NewsMapper newsMapper;
    @Resource
    private IndexService indexService;
    @Resource
    private NewsService newsService;
    @Resource
    private Db db;
    @Resource
    private FileService fileService;
    
    private final Logger logger = LoggerFactory.getLogger(NewsTimeService.class);
    private static SimpleDateFormat simpleDateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public boolean getNews(){
        logger.info("mpNews cmsID start index");
        IndexWriter writer = indexService.getWriter(DataManage.newsTimeIndexDir);
        if(writer!=null){
            ResultSet rs = null;
            boolean record = false;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.MONTH, -3);
            Date date = calendar.getTime();
            String dateStr = simpleDateFormat.format(date);
            List<String> dbs = newsService.getDbName();
            try{
                for(String name : dbs){
                    rs = db.selectNewsByTime(name, dateStr);
                    record = false;
                    while(rs.next()){
                        News news = new News();
                        news.setId(rs.getLong("id"));
                        news.setMpMediaId(rs.getString("mp_media_id"));
                        news.setPostTime(rs.getTimestamp("post_time"));  
                        news.setCmsId(rs.getLong("cms_id"));
                        if(!record){
                            Integer index = Integer.valueOf(name.substring(8));
                            DataManage.times[index] = news.getPostTime().getTime();
                            record = true;
                        }
                        indexService.addTimeDocument(news, writer);
                        logger.info(String.valueOf(news.getId())+","+news.getMpMediaId());
                    }
                }
                indexService.commit(writer);
                indexService.closeWriter(writer);
                db.dbClose(true);
                fileService.writeFile(DataManage.timePath, DataManage.times);
                logger.info("mpNews cmsID index finish");
                return true;
            }catch(Exception e){
                indexService.closeWriter(writer);
                db.dbClose(true);
                logger.info(e.getMessage());
                logger.info("get mpNews cmsID index failed");
                return false;
            }finally{
                indexService.closeWriter(writer);
            }
        }
        else{
            logger.info("get mpNews' cmsID indexWriter failed");
            return true;
        }
    }
    
    public boolean updateNewsTime(){
        if(DataManage.timeIsUpdate){
            IndexWriter writer = indexService.getWriter(DataManage.newsTimeIndexDir);
            if(writer!=null){
                try{
                    List<String> dbNames = newsService.getDbName();
                    Map<String, Object> map = new HashMap<String, Object>();
                    fileService.readFile(DataManage.timePath, DataManage.times);
                    logger.info("update mpNews cmsID index");
                    for(String name : dbNames){
                        Integer index = Integer.valueOf(name.substring(8));
                        Long time = DataManage.times[index];
                        Date date = new Date(time);
                        String postTime = simpleDateFormat.format(date);
                        map.put("db", name);
                        map.put("date", "\""+postTime+"\"");
                        List<News> news = newsMapper.selectByDate(map);
                        if(news.size()>0){
                            DataManage.times[index] = news.get(0).getPostTime().getTime();
                            for(News n : news){
                                indexService.addTimeDocument(n, writer);
                                logger.info(String.valueOf(n.getId())+","+n.getMpMediaId());
                            }
                        }
                    }
                    indexService.commit(writer);
                    indexService.closeWriter(writer);
                    fileService.writeFile(DataManage.timePath, DataManage.times);
                    logger.info("update mpNews cmsID finish");
                    return true;
                }catch(Exception e){
                    logger.info(e.getMessage());
                    logger.info("update mpNews cmsID failed");
                    indexService.closeWriter(writer);
                    return false;
                }
            }
            else{
                logger.info("get mpNews' cmsID indexWriter failed");
                return false;
            }
        }else{
            logger.info("mpNews' cmsID index is writing, update later");
            return false;
        }
    }
}
