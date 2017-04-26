package com.sohu.mp.service;

import java.util.List;

import javax.annotation.Resource;

import org.apache.lucene.index.IndexWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sohu.mp.DAO.ProfileMapper;
import com.sohu.mp.model.Profile;
import com.sohu.mp.newsDAO.DataManage;

@Service
public class ProfileService {

	@Resource
	private ProfileMapper profileMapper;
	@Resource
	private IndexService indexService;
	@Resource
	private FileService fileService;
	
	private final Logger logger = LoggerFactory.getLogger(ProfileService.class);
	
	public boolean indexProfile() {
		logger.info("profile start index");
		IndexWriter writer = indexService.getWriter(DataManage.profileIndexDir);
		if(writer != null){
			try{
				List<Profile> profiles = profileMapper.selectAll();
				DataManage.profileId[0] = profiles.get(0).getId();
				for(Profile profile: profiles){
					indexService.addProfileDocument(profile, writer);
					logger.info(String.valueOf(profile.getId()));
				}
				indexService.commit(writer);
				indexService.closeWriter(writer);
				fileService.writeFile(DataManage.profilePath, DataManage.profileId);
				logger.info("profile index finish");
				return true;
			}catch(Exception e){
				indexService.closeWriter(writer);
				logger.info(e.getMessage());
				logger.info("profile index failed");
				return false;
			}finally{
				indexService.closeWriter(writer);
			}
		}
		else{
			logger.info("profile index failed");
			return false;
		}
	}
	
	public List<Profile> updateProfiles() throws Exception{
		fileService.readFile(DataManage.profilePath, DataManage.profileId);
		List<Profile> profiles = profileMapper.selectById(DataManage.profileId[0]);
		if(profiles!=null && profiles.size()>0){
			DataManage.profileId[0] = profiles.get(0).getId();
			return profiles;
		}
		else{
			return null;
		}
		
	}
}
