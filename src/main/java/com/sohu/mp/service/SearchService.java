package com.sohu.mp.service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.springframework.stereotype.Service;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.sohu.mp.model.News;
import com.sohu.mp.newsDAO.DataManage;

@Service
public class SearchService {
	
    @Resource
    private IndexService indexService;
    @Resource
    private NewsService newsService;
	
	public Map searchText(Map<String, Object> map, int pno, int pageSize) throws Exception{
		
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(DataManage.indexDir)));
		IndexSearcher indexSearcher = new IndexSearcher(reader);
		Query query = this.getSearchQuery(map);
		
		//获得上一页最后一个元素
		ScoreDoc lastDoc = this.getLastDoc(pno, pageSize, indexSearcher, query);
		TopDocs topDocs = indexSearcher.searchAfter(lastDoc, query, pageSize);
		ScoreDoc[] docs = topDocs.scoreDocs;
		int count = topDocs.totalHits;  //命中总数
		int num = pageSize > docs.length ? docs.length: pageSize; //当前页数目
		List<Map<String, String>> l = new ArrayList<Map<String, String>>();
		for(int i=0; i<num; i++){
			Map<String, String> hashMap = new HashMap<String, String>();
			Document doc = indexSearcher.doc(docs[i].doc);
			hashMap.put("id", doc.get(IndexService.ID));
			hashMap.put("passport", doc.get(IndexService.MPMEDIAID));
            if (l.contains(hashMap)) {
                News news = newsService.getNewsById(hashMap.get("id"), hashMap.get("passport"));
                if(news!=null) {
                    IndexWriter writer = indexService.getWriter(DataManage.indexDir);
                    if (writer != null){
                        Long temp = Long.valueOf(hashMap.get("id"));
                        writer.deleteDocuments(NumericRangeQuery.newLongRange(IndexService.ID, temp, temp, true, true));
                        indexService.addDocument(news, writer);
                        indexService.closeWriter(writer);
                    }
                }
            }
            else {
                l.add(hashMap);
            }
		}
		Map<String, Object> result = new HashMap<String, Object>();
		reader.close();
		result.put("count", count);
		result.put("id", l);
		return result;
	}
	
	
	private Query getSearchQuery(Map<String, Object> map) throws Exception{
		
		BooleanQuery boolQuery = new BooleanQuery();
		Analyzer analyzer = new IKAnalyzer();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		
		String id = (String) map.get(IndexService.ID);
		String title = (String) map.get(IndexService.TITLE);
		String mediaId = (String) map.get(IndexService.MPMEDIAID);
		String userName = (String) map.get(IndexService.USERNAME);
		String auditWord = (String) map.get(IndexService.AUDITWORD);
		String mediaType = (String) map.get(IndexService.MEDIATYPE);
		String startTimeStr = (String)map.get(IndexService.STARTTIME);
		String endTimeStr = (String)map.get(IndexService.ENDTIME);		
		
		if(id!=null){
			Long lid = Long.valueOf(id);
			Query queryId = NumericRangeQuery.newLongRange(IndexService.ID, lid, lid, true, true);
			boolQuery.add(queryId, BooleanClause.Occur.MUST);
		}
		if(title!=null){
			QueryParser queryParserT = new QueryParser(Version.LUCENE_45, IndexService.TITLE, analyzer);
			queryParserT.setDefaultOperator(QueryParser.Operator.AND);
			Query queryTitle = queryParserT.parse(title);
			boolQuery.add(queryTitle, BooleanClause.Occur.MUST);
		}
		if(mediaId!=null){
			Query queryMediaId = new TermQuery(new Term(IndexService.MPMEDIAID, mediaId));
			boolQuery.add(queryMediaId, BooleanClause.Occur.MUST);
		}
		if(userName!=null){
			QueryParser queryParserU = new QueryParser(Version.LUCENE_45, IndexService.USERNAME, analyzer);
			queryParserU.setDefaultOperator(QueryParser.Operator.AND);
			Query queryUserName = queryParserU.parse(userName);
			boolQuery.add(queryUserName, BooleanClause.Occur.MUST);
		}
		if(auditWord!=null){
			QueryParser queryParserA = new QueryParser(Version.LUCENE_45, IndexService.AUDITWORD, analyzer);
			Query queryAuditWord = queryParserA.parse(auditWord);
			boolQuery.add(queryAuditWord, BooleanClause.Occur.MUST);
		}
		if(mediaType!=null){
			Query queryMediaType = NumericRangeQuery.newIntRange(IndexService.MEDIATYPE, Integer.valueOf(mediaType), Integer.valueOf(mediaType), true, true);
			boolQuery.add(queryMediaType, BooleanClause.Occur.MUST);
		}
		if(startTimeStr!=null && endTimeStr!=null){
			Date startDate = sdf.parse(startTimeStr);
			Date endDate = sdf.parse(endTimeStr);
			Query queryPostTime = NumericRangeQuery.newLongRange(IndexService.POSTTIME, startDate.getTime(), endDate.getTime(), true, true);
			boolQuery.add(queryPostTime, BooleanClause.Occur.MUST);
		}
		return boolQuery;
	}
	
	
	private ScoreDoc getLastDoc(int pno, int pageSize, IndexSearcher searcher, Query query) throws IOException{
		if(pno==1)
			return null;
		int num = (pno - 1) * pageSize;
		TopDocs topDocs = searcher.search(query, num);
		return topDocs.scoreDocs[num-1];
	}
	
	
	
    public void test(Long id) throws IOException{
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(DataManage.indexDir)));
        IndexSearcher indexSearcher = new IndexSearcher(reader);
        Term term = new Term(IndexService.ID, id+"");
//        Term term2 = new Term(IndexService.ID, id+"");
        Query query = new TermQuery(term);
//        Query queryId = NumericRangeQuery.newLongRange(IndexService.ID, id, id, true, true);
        TopDocs topDocs = indexSearcher.search(query, 10);
        ScoreDoc[] docs = topDocs.scoreDocs;
        System.out.println(docs.length);
    }
//	private IndexSearcher getIndexSeacher(){
//		IndexReader reader = null;
//		
//		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(IndexService.indexDir)));
//		IndexSearcher indexSearcher = new IndexSearcher(reader);
//	}
}
