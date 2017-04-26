package com.sohu.mp.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LogByteSizeMergePolicy;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.sohu.mp.model.News;
import com.sohu.mp.model.Profile;
import com.sohu.mp.newsDAO.DataManage;

@Service
public class IndexService {
	
	
	@Resource
	private FileService fileService;
	
	static Analyzer analyzer = new IKAnalyzer();
	
	//mpNews field
	public static final String ID = "id";
	public static final String TITLE = "title";
	public static final String MPMEDIAID = "mpMediaId";
	public static final String USERNAME = "userName";
	public static final String AUDITWORD = "auditWord";
	public static final String MEDIATYPE = "mediaType";
	public static final String POSTTIME = "postTime";
	public static final String STARTTIME = "startTime";
	public static final String ENDTIME = "endTime";
	public static final String CMSID="cmsId";
	
	//Profile field
	public static final String PROFILEID = "id";
	public static final String PROFILENAME = "userName";
	public static final String PROFILEPASSPORT = "passport";
	
	
	
	public IndexWriter getWriter(String indexPath){
	   
		IndexWriter writer = null;
		File file = new File(indexPath);
		IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_45, analyzer);
		conf.setRAMBufferSizeMB(8);
		conf.setMaxBufferedDocs(500);
		conf.setReaderPooling(true);
//		LogByteSizeMergePolicy mp = new LogByteSizeMergePolicy();
//		mp.setMergeFactor(100);
		try {
			writer = new IndexWriter(FSDirectory.open(file), conf);
			return writer;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public void closeWriter(IndexWriter writer) {
		
			try {
				if(writer != null){
					writer.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public void addProfileDocument(Profile profile, IndexWriter writer) throws IOException{
		Document doc = new Document();
		doc.add(new LongField(IndexService.PROFILEID, profile.getId(), Field.Store.YES));
		doc.add(new TextField(IndexService.PROFILENAME, profile.getUserName(), Field.Store.NO));
		doc.add(new StringField(IndexService.PROFILEPASSPORT, profile.getPassport(), Field.Store.NO));
		writer.addDocument(doc);
		DataManage.profileSum++;
		if(DataManage.sum>50000){
			writer.commit();
			fileService.writeFile(DataManage.profilePath, DataManage.profileId);
			DataManage.profileSum = 0;
		}
	}
	
	public void addDocument(News news, IndexWriter writer) throws IOException{
		
		Document doc = new Document();
		doc.add(new LongField(IndexService.ID, news.getId(), Field.Store.YES));
		doc.add(new TextField(IndexService.TITLE, news.getTitle(), Field.Store.NO));
		doc.add(new StringField(IndexService.MPMEDIAID, news.getMpMediaId(), Field.Store.YES));
		doc.add(new TextField(IndexService.USERNAME, news.getUserName(), Field.Store.NO));
		doc.add(new TextField(IndexService.AUDITWORD, news.getAuditWords(), Field.Store.NO));
        if (news.getMediaType() != null) {
            doc.add(new IntField(IndexService.MEDIATYPE, news.getMediaType(), Field.Store.NO));
        }
        if (news.getPostTime() != null) {
            doc.add(new LongField(IndexService.POSTTIME, news.getPostTime().getTime(), Field.Store.NO));
        }
		writer.addDocument(doc);
		DataManage.sum++;
		if(DataManage.sum>50000){
		    writer.commit();
		    fileService.writeFile(DataManage.newsPath, DataManage.ids);
		    DataManage.sum=0;
		}
	}
	
	   public void addTimeDocument(News news, IndexWriter writer) throws IOException{
	        
	        if(news.getCmsId()!=null){
	            Document doc = new Document();
	            doc.add(new StringField(IndexService.ID, news.getId()+"", Field.Store.YES));
	            doc.add(new StringField(IndexService.MPMEDIAID, news.getMpMediaId(), Field.Store.YES));
	            doc.add(new StringField(IndexService.CMSID, news.getCmsId()+"", Field.Store.NO));
	            if (news.getPostTime() != null) {
	                doc.add(new LongField(IndexService.POSTTIME, news.getPostTime().getTime(), Field.Store.NO));
	            }
	            writer.addDocument(doc);
	            DataManage.timeSum++;
	            if(DataManage.timeSum>50000){
	                writer.commit();
	                fileService.writeFile(DataManage.timePath, DataManage.times);
	                DataManage.timeSum=0;
	            }
	        }
	    }
	
	
	public void commit(IndexWriter writer) throws IOException{
		writer.commit();
	}

	public void deleteIndex(String indexPath) throws IOException{
		IndexWriter writer = getWriter(indexPath);
		if(writer != null){
			writer.deleteAll();
			writer.commit();
			writer.close();
		}
	}
}
