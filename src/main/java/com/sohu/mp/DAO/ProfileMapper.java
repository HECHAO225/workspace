package com.sohu.mp.DAO;

import java.util.List;

import com.sohu.mp.model.Profile;

public interface ProfileMapper {
	
	public String selectUser(String passport);
	public List<Profile> selectAll();
	public List<Profile> selectById(Long id);
	public Profile ById(Long id);
}
