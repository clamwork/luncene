package cn.com.jandar.luncene.base;

import cn.com.jandar.luncene.util.IndexUtil;
import org.apache.lucene.index.IndexWriter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 公共的索引处理类
 * @author Chengw
 * @since 2017/4/12 14:22.
 */
public abstract class BaseIndex<T> implements Runnable {

    /**
     * 父级索引路径
     */
    private String parentIndexPath;
    /**
     * 索引编写器
     */
    private IndexWriter writer;
    private int subIndex;
    /**
     * 主线程
     */
    private final CountDownLatch maincountDownLatch;
    /**
     *工作线程
     */
    private final CountDownLatch subcountDownLatch;

    /**
     * 对象列表
     */
    private List<T> list;
    public BaseIndex(IndexWriter writer,CountDownLatch maincountDownLatch, CountDownLatch subcountDownLatch,
                     List<T> list){
        super();
        this.writer = writer;
        this.maincountDownLatch = maincountDownLatch;
        this.subcountDownLatch = subcountDownLatch;
        this.list = list;
    }
    public BaseIndex(String parentIndexPath, int subIndex,
                     CountDownLatch maincountDownLatch, CountDownLatch subcountDownLatch,
                     List<T> list) {
        super();
        this.parentIndexPath = parentIndexPath;
        this.subIndex = subIndex;
        try {
            //多目录索引创建
            File file = new File(parentIndexPath+"/index"+subIndex);
            if(!file.exists()){
                file.mkdir();
            }
            this.writer = IndexUtil.getIndexWriter(parentIndexPath+"/index"+subIndex, true);
        } catch (IOException e) {
            e.printStackTrace();
        };
        this.subIndex = subIndex;
        this.maincountDownLatch = maincountDownLatch;
        this.subcountDownLatch = subcountDownLatch;
        this.list = list;
    }
    public BaseIndex(String path,CountDownLatch maincountDownLatch, CountDownLatch subcountDownLatch,
                     List<T> list) {
        super();
        try {
            //单目录索引创建
            File file = new File(path);
            if(!file.exists()){
                file.mkdir();
            }
            this.writer = IndexUtil.getIndexWriter(path,true);
        } catch (IOException e) {
            e.printStackTrace();
        };
        this.maincountDownLatch = maincountDownLatch;
        this.subcountDownLatch = subcountDownLatch;
        this.list = list;
    }

    /**创建索引
     * @param writer
     * @param carSource
     * @param create
     * @throws IOException
     * @throws ParseException
     */
    public abstract void indexDoc(IndexWriter writer,T t) throws Exception;
    /**批量索引创建
     * @param writer
     * @param t
     * @throws Exception
     */
    public void indexDocs(IndexWriter writer,List<T> t) throws Exception{
        for (T t2 : t) {
            indexDoc(writer,t2);
        }
    }

    @Override
    public void run() {
        try {
            maincountDownLatch.await();
            System.out.println(writer);
            indexDocs(writer,list);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            subcountDownLatch.countDown();
            try {
                writer.commit();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

}
