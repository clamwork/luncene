package cn.com.jandar.luncene.test;

import cn.com.jandar.luncene.index.FileBeanIndex;
import cn.com.jandar.luncene.model.FileBean;
import cn.com.jandar.luncene.util.FileUtil;
import cn.com.jandar.luncene.util.SearchUtil;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Chengw
 * @since 2017/4/12 14:58.
 */
public class TestIndex {

//    @Test
    public void index() throws Exception {
        try {
            List<FileBean> fileBeans = FileUtil.getFolderFiles("/Users/superman/work/test/doc");
            int totalCount = fileBeans.size();
            int perThreadCount = 3000;
            System.out.println("查询到的数据总数是" + fileBeans.size());
            int threadCount = totalCount / perThreadCount + (totalCount % perThreadCount == 0 ? 0 : 1);
            ExecutorService pool = Executors.newFixedThreadPool(threadCount);
            CountDownLatch countDownLatch1 = new CountDownLatch(1);
            CountDownLatch countDownLatch2 = new CountDownLatch(threadCount);
            System.out.println(fileBeans.size());

            for (int i = 0; i < threadCount; i++) {
                int start = i * perThreadCount;
                int end = (i + 1) * perThreadCount < totalCount ? (i + 1) * perThreadCount : totalCount;
                List<FileBean> subList = fileBeans.subList(start, end);
                Runnable runnable = new FileBeanIndex("index", i, countDownLatch1, countDownLatch2, subList);
                //子线程交给线程池管理
                pool.execute(runnable);
            }
            countDownLatch1.countDown();
            System.out.println("开始创建索引");
            //等待所有线程都完成
            countDownLatch2.await();
            //线程全部完成工作
            System.out.println("所有线程都创建索引完毕");
            //释放线程池资源
            pool.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void search() throws Exception {
        try {
            IndexSearcher searcher = SearchUtil.getMultiSearcher("index", Executors.newCachedThreadPool(), false);
            Query phoneQuery = SearchUtil.getRegexExpQuery("content", "1[0-9]{10}");
            Query mailQuery = SearchUtil.getRegexExpQuery("content", "([a-z0-9A-Z]+[-_|\\.]?)+[a-z0-9A-Z]*@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}");
            Query finaQuery = SearchUtil.getMultiQueryLikeSqlIn(new Query[]{phoneQuery,mailQuery});
            TopDocs topDocs = SearchUtil.getScoreDocsByPerPageAndSortField(searcher, finaQuery, 0, 20, null);
            System.out.println("符合条件的数据总数："+topDocs.totalHits);
            System.out.println("本次查询到的数目为："+topDocs.scoreDocs.length);
            ScoreDoc[] scoreDocs = topDocs.scoreDocs;
            for (ScoreDoc scoreDoc : scoreDocs) {
                Document doc = searcher.doc(scoreDoc.doc);
                System.out.println(doc.get("path")+"    "+doc.get("content"));
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
