package cn.com.jandar.luncene.index;

import cn.com.jandar.luncene.base.BaseIndex;
import cn.com.jandar.luncene.model.DocBean;
import cn.com.jandar.luncene.model.FileBean;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author Chengw
 * @since 2017/4/12 14:46.
 */
public class DocBeanIndex extends BaseIndex<DocBean>{

    public DocBeanIndex(IndexWriter writer, CountDownLatch countDownLatch1,
                         CountDownLatch countDownLatch2, List<DocBean> list) {
        super(writer, countDownLatch1, countDownLatch2, list);
    }
    public DocBeanIndex(String parentIndexPath, int subIndex, CountDownLatch countDownLatch1,
                         CountDownLatch countDownLatch2, List<DocBean> list) {
        super(parentIndexPath, subIndex, countDownLatch1, countDownLatch2, list);
    }

    @Override
    public void indexDoc(IndexWriter writer, DocBean t) throws Exception {
        Document doc = new Document();
        doc.add(new StringField("docCode-keyword", t.getDocCode()+"-"+t.getKeyword(), Field.Store.YES));
        doc.add(new StringField("path", t.getPath(), Field.Store.YES));
        doc.add(new StringField("docName", t.getDocName(), Field.Store.YES));
        doc.add(new StringField("keyword", t.getKeyword(), Field.Store.YES));
        doc.add(new StringField("count", t.getCount().toString(), Field.Store.YES));
        doc.add(new TextField("content", t.getContent(), Field.Store.YES));
        if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE){
            writer.addDocument(doc);
        }else{
            writer.updateDocument(new Term("docCode-keyword", t.getDocCode()+"-"+t.getKeyword()), doc);
        }
    }
}
