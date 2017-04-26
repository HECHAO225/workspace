package com.sohu.mp.service;


import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;

import javax.annotation.Resource;

import org.apache.lucene.index.IndexWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sohu.mp.DAO.Db;
import com.sohu.mp.DAO.ProfileMapper;
import com.sohu.mp.model.News;
import com.sohu.mp.newsDAO.NewsMapper;
import com.sohu.mp.newsDAO.DataManage;

@Service
public class NewsService {

	@Resource
	private ProfileMapper profileMapper;
	@Resource
	private NewsMapper newsMapper;
	@Resource
	private FileService fileService;
	@Resource
	private Db db;
	@Resource
	private IndexService indexService;
	
	private final Logger logger = LoggerFactory.getLogger(NewsService.class);
	

	//使用jdbc查询并建立索引
	public boolean indexNews() {
		logger.info("mpNews start index");
		IndexWriter writer = indexService.getWriter(DataManage.indexDir);
		if(writer != null){
			// 查询结果
			ResultSet rs = null;
			boolean record = false; // 最大Id是否已被记录
			// 获取日期
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			calendar.add(Calendar.YEAR, -1);
			Date date = calendar.getTime();
			String dateStr = simpleDateFormat.format(date);
			// 读取数据
			List<String> dbs = this.getDbName();
			try{
				for (String name : dbs) {
					rs = db.selectNews(name, dateStr);
					record = false;
					while (rs.next()) {
						News news = new News();
						news.setAuditWords(rs.getString("audit_words"));
						news.setId(rs.getLong("id"));
						news.setMediaType(rs.getInt("media_type"));
						news.setMpMediaId(rs.getString("mp_media_id"));
						news.setPostTime(rs.getTimestamp("post_time"));
						news.setTitle(rs.getString("title"));
						this.addUserName(news);
						if (!record) {
							Integer index = Integer.valueOf(name.substring(8));
							DataManage.ids[index] = news.getId();
							record = true;
						}
						indexService.addDocument(news, writer);
						logger.info(String.valueOf(news.getId()) + "," + news.getMpMediaId());
					}
				}
				indexService.commit(writer);
				indexService.closeWriter(writer);
				db.dbClose(true);
				fileService.writeFile(DataManage.newsPath, DataManage.ids);
				logger.info("mpNews index finish");
				return true;
			}
			catch(Exception e){
				indexService.closeWriter(writer);
				db.dbClose(true);
				logger.info(e.getMessage());
				logger.info("get mp index failed");
				return false;
			}finally{
				indexService.closeWriter(writer);
			}
		}
		else{
			logger.info("get mpNews' indexWriter failed");
			return true;
		}
	}

	public News getNewsById(String id, String passport){
	    String dbName = "mp_news_"+getRouteTableIndex(passport);
	    Map<String, Object> param = new HashMap<String, Object>();
	    param.put("db", dbName);
	    param.put("id", Long.valueOf(id));
	    News news = newsMapper.ById(param);
	    if(news==null)
	        return null;
	    this.addUserName(news);
	    return news;
	}
	
	//每5分钟调用一次
//	public List<News> updateNews() throws Exception{
//		List<String> dbNames = this.getDbName();
//		List<News> newsAll = new ArrayList<News>();
//		Map<String, Object> map = new HashMap<String, Object>();
//		fileService.readFile(DataManage.newsPath, DataManage.ids);
//		for(String name: dbNames){
//			Integer index = Integer.valueOf(name.substring(8));
//			Long id = DataManage.ids[index];
//			map.put("db", name);
//			map.put("id", id);
//			List<News> news = newsMapper.selectById(map);
//			if(news.size()>0){
//				DataManage.ids[index] = news.get(0).getId(); //更新索引
//				newsAll.addAll(news);
//			}
//		}
//		this.addUserName(newsAll);
//		return newsAll;
//	}
	
	//每5分钟调用一次
    public boolean updateNews() {

        if (DataManage.isUpdate) {
            IndexWriter writer = indexService.getWriter(DataManage.indexDir);
            if (writer != null) {
                try {
                    List<String> dbNames = this.getDbName();
                    Map<String, Object> map = new HashMap<String, Object>();
                    fileService.readFile(DataManage.newsPath, DataManage.ids);
                    logger.info("update mpNews' index");
                    for (String name : dbNames) {
                        Integer index = Integer.valueOf(name.substring(8));
                        Long id = DataManage.ids[index];
                        map.put("db", name);
                        map.put("id", id);
                        List<News> news = newsMapper.selectById(map);
                        if (news.size() > 0) {
                            DataManage.ids[index] = news.get(0).getId(); // 更新索引
                            this.addUserName(news);
                            for(News n:news){
                                indexService.addDocument(n, writer);
                                logger.info(String.valueOf(n.getId())+","+n.getMpMediaId());
                            }
                        }
                    }
                    indexService.commit(writer);
                    indexService.closeWriter(writer);
                    fileService.writeFile(DataManage.newsPath, DataManage.ids);
                    logger.info("update mpNews finish");
                    return true;

                } catch (Exception e) {
                    logger.info(e.getMessage());
                    logger.info("update mpNews failed");
                    indexService.closeWriter(writer);
                    return false;
                }
            } else {
                logger.info("get mpNews' indexWriter failed");
                return false;
            }
        } else {
            logger.info("mpNews' index is writing, update later");
            return false;
        }
    }
	
	//获得表名
	public List<String> getDbName(){
		
		String baseName = "mp_news_";
		List<String> names = new ArrayList<String>();
		for(int i=0; i<256; i++){
			String name = baseName+i;
			names.add(name);
		}
		return names;
	}
	
	//处理null值，添加username
	private void addUserName(List<News> newsAll){
		for(News ns: newsAll){
		    this.addUserName(ns);
		}
	}
	

	private void addUserName(News news){
		news.setUserName(profileMapper.selectUser(news.getMpMediaId()));
		if(news.getAuditWords()==null)
			news.setAuditWords("");
		if(news.getTitle()==null)
			news.setTitle("");
		if(news.getUserName()==null)
			news.setUserName("");
	}
	
    private static int getRouteTableIndex(String key) {
        try {
            CRC32 checksum = new CRC32();
            checksum.update(key.getBytes("utf-8"));
            int crc = (int) checksum.getValue();
            int r = Math.abs((crc >> 16) & 0x7fff) % 256;
            return r;
        } catch (Exception e) {
            throw new RuntimeException("can not get route table index which " + key);
        }
    }           
        
}
