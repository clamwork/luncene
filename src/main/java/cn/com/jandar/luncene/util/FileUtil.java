package cn.com.jandar.luncene.util;

import cn.com.jandar.luncene.filter.DocFilter;
import cn.com.jandar.luncene.model.FileBean;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.xmlbeans.XmlException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Chengw
 * @since 2017/4/12 14:30.
 */
public class FileUtil {

    /**读取文件信息和下属文件夹
     * @param folder
     * @return
     * @throws IOException
     */
    public static List<FileBean> getFolderFiles(String folder) throws Exception {
        List<FileBean> fileBeans = new LinkedList<FileBean>();
        File file = new File(folder);
        if(file.isDirectory()){
            File[] files = file.listFiles(new DocFilter());
            if(files != null){
                for (File file2 : files) {
                    fileBeans.addAll(getFolderFiles(file2.getAbsolutePath()));
                }
            }
        }else{
            FileBean bean = new FileBean();
            String filePath = file.getAbsolutePath();
            bean.setPath(file.getAbsolutePath());
            bean.setModified(file.lastModified());
            String content = "";
            if(filePath.endsWith(".doc") || filePath.endsWith(".docx")){
                content = readDoc(file);
            }else{
                content = new String(Files.readAllBytes(Paths.get(folder)));
            }
            bean.setContent(content);
            fileBeans.add(bean);
        }
        return fileBeans;
    }

    public static String readDoc(File file) throws IOException, XmlException, OpenXML4JException {
        String filePath = file.getAbsolutePath();
        if(filePath.endsWith(".doc")){
            InputStream is = new FileInputStream(file);
            WordExtractor ex = new WordExtractor(is);
            String text2003 = ex.getText();
            ex.close();
            is.close();
            return text2003;
        }else{
            OPCPackage opcPackage = POIXMLDocument.openPackage(filePath);
            POIXMLTextExtractor extractor = new XWPFWordExtractor(opcPackage);
            String text2007 = extractor.getText();
            extractor.close();
            return text2007;
        }
    }

}
