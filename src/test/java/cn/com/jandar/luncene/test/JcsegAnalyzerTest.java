package cn.com.jandar.luncene.test;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;
import org.lionsoul.jcseg.analyzer.v5x.JcsegAnalyzer5X;
import org.lionsoul.jcseg.tokenizer.core.JcsegTaskConfig;


public class JcsegAnalyzerTest {

    @Test
    public void tokenTest() {
        Analyzer analyzer = new JcsegAnalyzer5X(JcsegTaskConfig.SEARCH_MODE);
        //非必须(用于修改默认配置): 获取分词任务配置实例  
        JcsegAnalyzer5X jcseg = (JcsegAnalyzer5X) analyzer;
        JcsegTaskConfig config = jcseg.getTaskConfig();
        //追加同义词到分词结果中, 需要在jcseg.properties中配置jcseg.loadsyn=1  
        config.setAppendCJKSyn(true);
        //追加拼音到分词结果中, 需要在jcseg.properties中配置jcseg.loadpinyin=1  
        config.setAppendCJKPinyin(true);
        config.setICnName(true);
        //更多配置, 请查看com.webssky.jcseg.core.JcsegTaskConfig类  
        String words = "中华人民共和国姓名男女儿金华";
        TokenStream stream = null;

        try {
            stream = analyzer.tokenStream("myfield", words);
            stream.reset();
            CharTermAttribute offsetAtt = stream.addAttribute(CharTermAttribute.class);
            while (stream.incrementToken()) {
                System.out.println(offsetAtt.toString());
            }
            stream.end();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (stream != null)
                    stream.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}  