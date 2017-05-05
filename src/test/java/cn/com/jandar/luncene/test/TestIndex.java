package cn.com.jandar.luncene.test;

import cn.com.jandar.luncene.index.FileBeanIndex;
import cn.com.jandar.luncene.model.FileBean;
import cn.com.jandar.luncene.util.FileUtil;
import cn.com.jandar.luncene.util.SearchUtil;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.*;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Chengw
 * @since 2017/4/12 14:58.
 */
public class TestIndex {

    @Test
    public void index() throws Exception {
        try {
            List<FileBean> fileBeans = FileUtil.getFolderFiles("/Users/superman/work/test/WebFiles/330782/2017/0301");
            int totalCount = fileBeans.size();
            int perThreadCount = 3000;
            System.out.println("查询到的数据总数是" + fileBeans.size());
            int threadCount = totalCount / perThreadCount + (totalCount % perThreadCount == 0 ? 0 : 1);
            ExecutorService pool = Executors.newFixedThreadPool(threadCount);
            CountDownLatch maincountDownLatch = new CountDownLatch(1);
            CountDownLatch subcountDownLatch = new CountDownLatch(threadCount);
            System.out.println(fileBeans.size());

            for (int i = 0; i < threadCount; i++) {
                int start = i * perThreadCount;
                int end = (i + 1) * perThreadCount < totalCount ? (i + 1) * perThreadCount : totalCount;
                List<FileBean> subList = fileBeans.subList(start, end);
                Runnable runnable = new FileBeanIndex("index",maincountDownLatch, subcountDownLatch, subList);
                //子线程交给线程池管理
                pool.execute(runnable);
            }
            maincountDownLatch.countDown();
            System.out.println("开始创建索引");
            //等待所有线程都完成
            subcountDownLatch.await();
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
            Query finaQuery = SearchUtil.getRegexExpQuery("code","[a-zA-Z]{1,4}");
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
