package com.sohu.mp.newsDAO;

import java.util.List;
import java.util.Map;

import com.sohu.mp.model.News;

public interface NewsMapper {
	
	public List<News> selectByDate(Map map);
	public List<News> selectById(Map map);
	public News ById(Map map);
	public News ByPostTime(Map map);
}
