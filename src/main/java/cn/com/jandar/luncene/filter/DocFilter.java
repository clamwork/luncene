package cn.com.jandar.luncene.filter;

import java.io.File;
import java.io.FileFilter;

/**
 * @author Chengw
 * @since 2017/4/12 09:12.
 */
public class DocFilter implements FileFilter{

    @Override
    public boolean accept(File pathname) {
        //如果是目录则直接返回
        if(pathname.isDirectory())
            return true;
        if(pathname.getName().toLowerCase().endsWith("doc"))
            return true;
        if(pathname.getName().toLowerCase().endsWith("docx"))
            return true;
        return false;
    }
}
