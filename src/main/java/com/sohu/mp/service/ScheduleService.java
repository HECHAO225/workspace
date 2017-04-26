package com.sohu.mp.service;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;

import org.apache.lucene.index.IndexWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sohu.mp.model.News;
import com.sohu.mp.model.Profile;
import com.sohu.mp.newsDAO.DataManage;

@Service
public class ScheduleService {

	@Resource
	private NewsService newsService;
	@Resource
	private IndexService indexService;
	@Resource
	private FileService fileService;
	@Resource
	private ProfileService profileService;
	@Resource
	private NewsTimeService newsTimeService;
	
	private final Logger logger = LoggerFactory.getLogger(ScheduleService.class);
	
	public boolean updateJob(){
		
		return newsService.updateNews();
//		if(DataManage.isUpdate){
//			IndexWriter writer = indexService.getWriter(DataManage.indexDir);
//			if(writer!=null){
//				try {
//					logger.info("update mpNews' index");
//					List<News> news = newsService.updateNews();
//					if(news!=null && news.size()>0){
//						for (News n : news) {
//							indexService.addDocument(n, writer);
//							logger.info(String.valueOf(n.getId()) + "," + n.getMpMediaId());
//						}
//						indexService.commit(writer);
//						indexService.closeWriter(writer);
//						fileService.writeFile(DataManage.newsPath, DataManage.ids); // 更新id
//						logger.info("update mpNews finish");
//						return true;
//					}
//					else{
//						logger.info("update mpNews finish, no new data");
//						indexService.closeWriter(writer);
//						return true;
//					}
//				} catch (Exception e) {
//					logger.info(e.getMessage());
//					logger.info("update mpNews failed");
//					indexService.closeWriter(writer);
//					return false;
//				}
//			}
//			else{
//				logger.info("get mpNews' indexWriter failed");
//				return false;
//			}
//		}
//		else{
//			logger.info("mpNews' index is writing, update later");
//			return false;
//		}
	}
	
	public boolean updateProfile(){
		if(DataManage.profileIsUpdate){
			IndexWriter writer = indexService.getWriter(DataManage.profileIndexDir);
			if(writer!=null){
				try{
					logger.info("update profile's index");
					List<Profile> profiles = profileService.updateProfiles();
					if(profiles!=null){
						for(Profile profile: profiles){
							indexService.addProfileDocument(profile, writer);
							logger.info(String.valueOf(profile.getId()));
						}
						indexService.commit(writer);
						indexService.closeWriter(writer);
						fileService.writeFile(DataManage.profilePath, DataManage.profileId);
						logger.info("update profile finish");
						return true;
					}
					else{
						logger.info("update profile finish, no new data");
						indexService.closeWriter(writer);
						return true;
					}
				}
				catch(Exception e){
					logger.info(e.getMessage());
					logger.info("update profile failed");
					indexService.closeWriter(writer);
					return false;
				}
			}
			else{
				logger.info("get profile's indexWriter failed");
				return false;
			}
		}
		else{
			logger.info("profile's index is writing, update later");
			return false;
		}
	}
	
    public boolean updateByTime() {
        return newsTimeService.updateNewsTime();
    }
}
