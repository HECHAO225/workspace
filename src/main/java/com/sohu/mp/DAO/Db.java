package com.sohu.mp.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class Db {
	
	@Value("${driver}")
	private String driveName;
	@Value("${url_news}")
	private String url;
	@Value("${username_news}")
	private String user;
	@Value("${password_news}")
	private String password;
	
	private Connection conn;
	private ResultSet rs;
	private Statement statement;
	private boolean isConnect=false;
	
	
	public boolean dbConnect(){
		if(isConnect)
			return true;
		try{
			Class.forName(driveName);
			conn = DriverManager.getConnection(url, user, password);
			isConnect = true;
		}catch(Exception e){
			e.printStackTrace();
		}
		if(conn == null){
			isConnect = false;
		}
		else{
			isConnect = true;
		}
		return isConnect;
	}
	
	
	public void dbClose(boolean closeCon){
		if(rs != null){
			try {
				rs.close();
				rs = null;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(statement != null){
				try{
					statement.close();
					statement = null;
				}catch(SQLException e){
					e.printStackTrace();
				}
			}
			if(conn != null && closeCon){
				try{
					conn.close();
					conn = null;
					isConnect = false;
				}catch(Exception e){
					e.printStackTrace();
					isConnect = false;
					
				}
			}
		}
	}
	
	public ResultSet selectNews(String dbName, String date){
		String sql = "select * from "+dbName+" where post_time>'"+date+"'"+" order by id desc";
		try{
			dbConnect();
			statement = conn.createStatement();
			rs = statement.executeQuery(sql);	
		}catch(Exception e){
			e.printStackTrace();
		}
		return rs;
	}
	
    public ResultSet selectNewsByTime(String dbName, String date) {
        String sql = "select * from " + dbName + " where post_time>'" + date + "'" + " order by post_time desc";
        try {
            dbConnect();
            statement = conn.createStatement();
            rs = statement.executeQuery(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rs;
    }
}
