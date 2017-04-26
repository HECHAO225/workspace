package com.sohu.mp.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.sohu.mp.DAO.ProfileMapper;
import com.sohu.mp.model.Profile;
import com.sohu.mp.newsDAO.DataManage;

@Service
public class ProfileSearchService {
    
    @Resource
    private IndexService indexService;
    @Resource
    private ProfileMapper profileMapper;
    
	
	public Map searchId(Map<String, String> map, int pno, int pageSize) throws Exception{
		
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(DataManage.profileIndexDir)));
		IndexSearcher indexSearcher = new IndexSearcher(reader);
		Query query = getSearchQuery(map);
		
		ScoreDoc lastDoc = this.getLastDoc(pno, pageSize, indexSearcher, query);
		TopDocs topDocs = indexSearcher.searchAfter(lastDoc, query, pageSize);
		ScoreDoc[] docs = topDocs.scoreDocs;
		int count = topDocs.totalHits;
		int num = pageSize > docs.length ? docs.length: pageSize;
		List<Long> l = new ArrayList<Long>();
		for(int i=0; i<num; i++){
			Document doc = indexSearcher.doc(docs[i].doc);
			Long re = Long.valueOf(doc.get(IndexService.PROFILEID));
            if (l.contains(re)) {
                Profile profile = profileMapper.ById(re);
                if(profile!=null){
                  IndexWriter writer = indexService.getWriter(DataManage.profileIndexDir);
                  if(writer!=null){
                      writer.deleteDocuments(NumericRangeQuery.newLongRange(IndexService.PROFILEID, re, re, true, true));
                      indexService.addProfileDocument(profile, writer);
                      indexService.closeWriter(writer);
                  }
                }
            } else {
                l.add(re);
            }
		}
		Map<String, Object> result = new HashMap<String, Object>();
		reader.close();
		result.put("count", count);
		result.put("id", l);
		result.put("size", num);
		return result;
	}
	
	private ScoreDoc getLastDoc(int pno, int pageSize, IndexSearcher searcher, Query query) throws IOException{
		if(pno==1)
			return null;
		int num = (pno - 1) * pageSize;
		TopDocs topDocs = searcher.search(query, num);
		return topDocs.scoreDocs[num-1];
	}
	
	public Query getSearchQuery(Map<String, String> map) throws ParseException{
		BooleanQuery boolQuery = new BooleanQuery();
		Analyzer analyzer = new IKAnalyzer();
		String user = map.get(IndexService.PROFILENAME);
		String passport = map.get(IndexService.PROFILEPASSPORT);
		if(user!=null){
			QueryParser queryParser = new QueryParser(Version.LUCENE_45, IndexService.PROFILENAME, analyzer);
			queryParser.setDefaultOperator(QueryParser.Operator.AND);
			Query query = queryParser.parse(user);
			boolQuery.add(query, BooleanClause.Occur.MUST);
		}
		if(passport!=null){
			Query queryPass = new TermQuery(new Term(IndexService.PROFILEPASSPORT, passport));
			boolQuery.add(queryPass, BooleanClause.Occur.MUST);
		}
		return boolQuery;
		
	}
}
