package com.sohu.mp.service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import com.sohu.mp.newsDAO.DataManage;

@Service
public class CMSIdSearchService {

    
    public Map searchCMSId(Map<String, String> map) throws IOException {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(DataManage.newsTimeIndexDir)));
        IndexSearcher indexSearcher = new IndexSearcher(reader);
        Query query = this.getSearchQuery(map);
        TopDocs topDocs = indexSearcher.search(query, 1);
        int total = topDocs.totalHits;
        if (total > 0) {
            ScoreDoc[] docs = topDocs.scoreDocs;
            Map<String, String> hashMap = new HashMap<String, String>();
            if (docs.length != 0 || docs[0] != null) {
                Document doc = indexSearcher.doc(docs[0].doc);
                hashMap.put("id", doc.get(IndexService.ID));
                hashMap.put("passport", doc.get(IndexService.MPMEDIAID));
            }
            reader.close();
            return hashMap;
        }
        else
            return null;
    }
    
    private Query getSearchQuery(Map<String, String> map){
        BooleanQuery boolQuery = new BooleanQuery();
        String mediaId = (String) map.get(IndexService.MPMEDIAID);
        String cmsId = (String) map.get(IndexService.CMSID);
        if(mediaId!=null){
            Query queryMediaId = new TermQuery(new Term(IndexService.MPMEDIAID, mediaId));
            boolQuery.add(queryMediaId, BooleanClause.Occur.MUST);
        }
        if(cmsId!=null){
            Query queryCmsId = new TermQuery(new Term(IndexService.CMSID, cmsId));
            boolQuery.add(queryCmsId, BooleanClause.Occur.MUST);
        }
        return boolQuery;
    }
}
