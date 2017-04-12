package cn.com.jandar.luncene.index;

import cn.com.jandar.luncene.base.BaseIndex;
import cn.com.jandar.luncene.model.FileBean;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;

import java.util.List;
import java.util.concurrent.CountDownLatch;


/**
 * @author Chengw
 * @since 2017/4/12 14:40.
 */
public class FileBeanIndex extends BaseIndex<FileBean>{

    public FileBeanIndex(IndexWriter writer, CountDownLatch countDownLatch1,
                         CountDownLatch countDownLatch2, List<FileBean> list) {
        super(writer, countDownLatch1, countDownLatch2, list);
    }
    public FileBeanIndex(String parentIndexPath, int subIndex, CountDownLatch countDownLatch1,
                         CountDownLatch countDownLatch2, List<FileBean> list) {
        super(parentIndexPath, subIndex, countDownLatch1, countDownLatch2, list);
    }

    @Override
    public void indexDoc(IndexWriter writer, FileBean t) throws Exception {
        Document doc = new Document();
        System.out.println(t.getPath());
        doc.add(new StringField("path", t.getPath(), Field.Store.YES));
        doc.add(new LongField("modified", t.getModified(), Field.Store.YES));
        doc.add(new TextField("content", t.getContent(), Field.Store.YES));
        if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
            writer.addDocument(doc);
        } else {
            writer.updateDocument(new Term("path", t.getPath()), doc);
        }
    }

}
