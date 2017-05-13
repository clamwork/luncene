package cn.com.jandar.luncene.test;

import cn.com.jandar.luncene.model.FileBean;
import cn.com.jandar.luncene.util.FileUtil;
import cn.com.jandar.luncene.util.IndexUtil;
import cn.com.jandar.luncene.util.SearchUtil;
import com.hankcs.lucene.HanLPAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.List;

/**
 * @author Chengw
 * @since 2017/4/13 09:48.
 */
public class LuceneTest {

    @Test
    public void test_1() throws Exception{
        IndexWriter writer = IndexUtil.getIndexWriter("test",true);

        List<FileBean> fileBeans = FileUtil.getFolderFiles("/Users/superman/work/test/WebFiles/330782/2017/0301");

        for(FileBean t:fileBeans){
            Document doc = new Document();
            doc.add(new StringField("path", t.getPath(), Field.Store.YES));
            doc.add(new StringField("code", "1", Field.Store.YES));
            doc.add(new LongField("modified", t.getModified(), Field.Store.YES));
            doc.add(new TextField("content", t.getContent(), Field.Store.YES));
            if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
                writer.addDocument(doc);
            } else {
                writer.updateDocument(new Term("path", t.getPath()), doc);
            }
        }
        writer.close();
        Analyzer analyzer = new HanLPAnalyzer();
        Directory directory = MMapDirectory.open(Paths.get("test"));
        //读取索引并查询
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher isearcher = new IndexSearcher(reader);
        //解析一个简单的查询
        QueryParser parser = new QueryParser("code", analyzer);
        Query query = parser.parse("1");
        ScoreDoc[] hits = isearcher.search(query, 1000).scoreDocs;
        //迭代输出结果
        for (int i = 0; i < hits.length; i++) {
            Document hitDoc = isearcher.doc(hits[i].doc);
            System.out.println(hitDoc.get("path"));
            System.out.println(hitDoc.get("content"));
        }
        reader.close();
        directory.close();

    }

    @Test
    public void test_2() throws  Exception{
        Analyzer analyzer = new HanLPAnalyzer();
        Directory directory = MMapDirectory.open(Paths.get("/Users/superman/work/test/index/doc"));
        //读取索引并查询
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher isearcher = new IndexSearcher(reader);
        //解析一个简单的查询
        QueryParser parser = new QueryParser("caseName", analyzer);
        Query query = parser.parse("受贿");
        //FuzzyQuery fuzzyQuery = new FuzzyQuery(new Term("content","人民"));
        int count =  isearcher.count(query);
        System.out.println("count:"+ count);
        ScoreDoc[] hits = SearchUtil.getScoreDocsByPerPageAndSortField(isearcher,query,0,100, Sort.INDEXORDER).scoreDocs;
        //迭代输出结果

        for (int i = 0; i < hits.length; i++) {
            Document hitDoc = isearcher.doc(hits[i].doc);
            System.out.println(hitDoc.get("caseName"));
            System.out.println(hitDoc.get("path"));
            //System.out.println(hitDoc.get("content"));
        }
        System.out.println("length:"+ hits.length);
        reader.close();
        directory.close();
    }

}
