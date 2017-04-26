package com.sohu.mp.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.stereotype.Service;

import com.sohu.mp.newsDAO.DataManage;

@Service
public class FileService {
	
	
	
	
	public boolean writeFile(String path, Long[] ids){
		File file = new File(path);
		try{
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			for(Long id: ids){
				if(id!=null)
					bw.write(id+"\r\n");
			}
			bw.flush();
			bw.close();
			return true;
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	
	public void readFile(String path, Long[] ids) throws Exception{
		File file = new File(path);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		String line = null;
		int i = 0;
		while((line = reader.readLine())!=null){
			ids[i] = Long.valueOf(line);
			i++;
		}
		reader.close();
	}
}
