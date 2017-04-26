package com.sohu.mp.newsDAO;

public class DataManage {
	public static Long[] ids = new Long[256];
	public static Long[] profileId = new Long[1];
	public static Long[] times = new Long[256];
	public static boolean isUpdate = true;
	public static boolean profileIsUpdate = true;
	public static boolean timeIsUpdate = true;
	public static int profileSum = 0;
	public static int sum = 0;
	public static int timeSum = 0;
	public static String newsPath = "/opt/data/mp-searchV2/8099/logs/ids.txt";  //保存mpNews的最大id
	public static String profilePath = "/opt/data/mp-searchV2/8099/logs/profile.txt";  //保存profile的最大id
	public static String timePath = "/opt/data/mp-searchV2/8099/logs/time.txt"; 
	public static String indexDir = "/opt/data/mp-searchV2/8099/index/";  //保存mpNews索引
	public static String profileIndexDir = "/opt/data/mp-searchV2/8099/profileIndex/"; //保存profile索引
	public static String newsTimeIndexDir = "/opt/data/mp-searchV2/8099/newsTimeIndex/";
//	public static String newsPath="ids.txt";
//	public static String profilePath="profile.txt";
//	public static String timePath="time.txt";
//	public static String indexDir="index/";
//	public static String profileIndexDir="profileIndex/";
//    public static String newsTimeIndexDir="newsTimeIndex/";
}
