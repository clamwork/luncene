package cn.com.jandar.luncene.util;


import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;

import com.hankcs.lucene.HanLPAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RegexpQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortField.Type;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.store.MMapDirectory;


public class SearchUtil {
    public static final Analyzer analyzer = new HanLPAnalyzer();
    /**获取IndexSearcher对象（适合单索引目录查询使用）
     * @param indexPath 索引目录
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static IndexSearcher getIndexSearcher(String indexPath,ExecutorService service,boolean realtime) throws IOException, InterruptedException{
        DirectoryReader reader = DirectoryReader.open(IndexUtil.getIndexWriter(indexPath, true), realtime);
        IndexSearcher searcher = new IndexSearcher(reader,service);
        if(service != null){
            service.shutdown();
        }
        return searcher;
    }


    /**多目录多线程查询
     * @param parentPath 父级索引目录
     * @param service 多线程查询
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static IndexSearcher getMultiSearcher(final String parentPath, ExecutorService service, boolean realtime) throws IOException, InterruptedException{
        MultiReader multiReader;
        File file = new File(parentPath);
        File[] files = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if(pathname.getName().toLowerCase().endsWith("ds_store")){
                    return false;
                }
                return true;
            }
        });
        IndexReader[] readers = new IndexReader[files.length];
        if(!realtime){
            for (int i = 0 ; i < files.length ; i ++) {
                readers[i] = DirectoryReader.open(MMapDirectory.open(Paths.get(files[i].getPath(), new String[0])));
            }
        }else{
            for (int i = 0 ; i < files.length ; i ++) {
                readers[i] = DirectoryReader.open(IndexUtil.getIndexWriter(files[i].getPath(), true), true);
            }
        }

        multiReader = new MultiReader(readers);
        IndexSearcher searcher = new IndexSearcher(multiReader,service);
        if(service != null){
            service.shutdown();
        }
        return searcher;
    }

    /**从指定配置项中查询
     * @return
     * @param analyzer 分词器
     * @param field 字段
     * @param fieldType	字段类型
     * @param queryStr 查询条件
     * @param range 是否区间查询
     * @return
     */
    public static Query getQuery(String field,String fieldType,String queryStr,boolean range){
        Query q = null;
        if(queryStr != null && !"".equals(queryStr)){
            if(range){
                String[] strs = queryStr.split("\\|");
                if("int".equals(fieldType)){
                    int min = new Integer(strs[0]);
                    int max = new Integer(strs[1]);
                    q = NumericRangeQuery.newIntRange(field, min, max, true, true);
                }else if("double".equals(fieldType)){
                    Double min = new Double(strs[0]);
                    Double max = new Double(strs[1]);
                    q = NumericRangeQuery.newDoubleRange(field, min, max, true, true);
                }else if("float".equals(fieldType)){
                    Float min = new Float(strs[0]);
                    Float max = new Float(strs[1]);
                    q = NumericRangeQuery.newFloatRange(field, min, max, true, true);
                }else if("long".equals(fieldType)){
                    Long min = new Long(strs[0]);
                    Long max = new Long(strs[1]);
                    q = NumericRangeQuery.newLongRange(field, min, max, true, true);
                }
            }else{
                if("int".equals(fieldType)){
                    q = NumericRangeQuery.newIntRange(field, new Integer(queryStr), new Integer(queryStr), true, true);
                }else if("double".equals(fieldType)){
                    q = NumericRangeQuery.newDoubleRange(field, new Double(queryStr), new Double(queryStr), true, true);
                }else if("float".equals(fieldType)){
                    q = NumericRangeQuery.newFloatRange(field, new Float(queryStr), new Float(queryStr), true, true);
                }else{
                    Term term = new Term(field, queryStr);
                    q = new TermQuery(term);
                }
            }
        }else{
            q= new MatchAllDocsQuery();
        }

        System.out.println(q);
        return q;
    }
    /**多条件查询类似于sql in
     * @param querys
     * @return
     */
    public static Query getMultiQueryLikeSqlIn(Query ... querys){
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        for (Query subQuery : querys) {
            builder.add(subQuery,Occur.SHOULD);
        }
        return builder.build();
    }

    /**获取regexQuery对象
     * @param field
     * @param regex
     * @return
     */
    public static Query getRegexExpQuery(String field,String regex){
        Query query = null;
        Term term = new Term(field, regex);
        query = new RegexpQuery(term);
        return query;
    }
    /**多条件查询类似于sql and
     * @param querys
     * @return
     */
    public static Query getMultiQueryLikeSqlAnd(Query ... querys){
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        for (Query subQuery : querys) {
            builder.add(subQuery,Occur.MUST);
        }
        return builder.build();
    }
    /**对多个条件进行排序构建排序条件
     * @param fields
     * @param type
     * @param reverses
     * @return
     */
    public static Sort getSortInfo(String[] fields,Type[] types,boolean[] reverses){
        SortField[] sortFields = null;
        int fieldLength = fields.length;
        int typeLength = types.length;
        int reverLength = reverses.length;
        if(!(fieldLength == typeLength) || !(fieldLength == reverLength)){
            return null;
        }else{
            sortFields = new SortField[fields.length];
            for (int i = 0; i < fields.length; i++) {
                sortFields[i] = new SortField(fields[i], types[i], reverses[i]);
            }
        }
        return new Sort(sortFields);
    }
    /**根据查询器、查询条件、每页数、排序条件进行查询
     * @param query 查询条件
     * @param first 起始值
     * @param max 最大值
     * @param sort 排序条件
     * @return
     */
    public static TopDocs getScoreDocsByPerPageAndSortField(IndexSearcher searcher,Query query, int first,int max, Sort sort){
        try {
            if(query == null){
                System.out.println(" Query is null return null ");
                return null;
            }
            TopFieldCollector collector = null;
            if(sort != null){
                collector = TopFieldCollector.create(sort, first+max, false, false, false);
            }else{
                sort = new Sort(new SortField[]{new SortField("modified", SortField.Type.LONG)});
                collector = TopFieldCollector.create(sort, first+max, false, false, false);
            }
            searcher.search(query, collector);
            return collector.topDocs(first, max);
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
        return null;
    }
    /**获取上次索引的id,增量更新使用
     * @return
     */
    public static Integer getLastIndexBeanID(IndexReader multiReader){
        Query query = new MatchAllDocsQuery();
        IndexSearcher searcher = null;
        searcher = new IndexSearcher(multiReader);
        SortField sortField = new SortField("id", SortField.Type.INT,true);
        Sort sort = new Sort(new SortField[]{sortField});
        TopDocs docs = getScoreDocsByPerPageAndSortField(searcher,query, 0, 1, sort);
        ScoreDoc[] scoreDocs = docs.scoreDocs;
        int total = scoreDocs.length;
        if(total > 0){
            ScoreDoc scoreDoc = scoreDocs[0];
            Document doc = null;
            try {
                doc = searcher.doc(scoreDoc.doc);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return new Integer(doc.get("id"));
        }
        return 0;
    }
}

