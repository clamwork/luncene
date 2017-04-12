package cn.com.jandar.luncene.model;

import java.util.Date;

/**
 *
 * 文书索引
 * @author Chengw
 * @since 2017/4/12 08:59.
 */
public class DocBean {

    private String docCode;

    private String docName;

    private String keyword;

    private String content;

    private String path;

    private Long count;

    private Date createDate;

    public void DocIndexer(String docCode,String keyword) throws Exception{
        DocIndexer(docCode,"",keyword,0L);
    }

    public void DocIndexer(String docCode,String docName,String keyword) throws Exception{
        DocIndexer(docCode,docName,keyword,0L);
    }

    public void DocIndexer(String docCode,String docName,String keyword,Long count) throws Exception{
        this.docCode = docCode;
        this.docName = docName;
        this.keyword = keyword;
        this.count = count;
        this.createDate = new Date();
    }

    public void setPath(String path){
        this.path = path;
    }

    public String getPath(){
        return this.path;
    }

    public String getDocCode() {
        return docCode;
    }

    public void setDocCode(String docCode) {
        this.docCode = docCode;
    }

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
