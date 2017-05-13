package cn.com.jandar.luncene.test;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.grouping.GroupDocs;
import org.apache.lucene.search.grouping.SearchGroup;
import org.apache.lucene.search.grouping.TopGroups;
import org.apache.lucene.search.grouping.term.TermAllGroupsCollector;
import org.apache.lucene.search.grouping.term.TermFirstPassGroupingCollector;
import org.apache.lucene.search.grouping.term.TermSecondPassGroupingCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;

import java.util.Collection;

/**
 * Lucene分类统计示例
 * @author Chengw
 * @since 2017/5/13 13:54.
 */
public class FacetingTest {

    public static void main(String[] args) throws Exception{

        // Lucene Document的主要域名
        String mainFieldName = "text";

        // 实例化Analyzer分词器
        Analyzer analyzer = new StandardAnalyzer();

        Directory directory;
        IndexWriter writer;
        IndexReader reader;
        IndexSearcher searcher;

        //索引过程**********************************
        //建立内存索引对象
        directory = new RAMDirectory();
        IndexWriterConfig iwConfig = new IndexWriterConfig(analyzer);
        iwConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        writer = new IndexWriter(directory, iwConfig);
        for (int i = 0; i < 100; ++i)
        {
            Document doc = new Document();
            doc.add(new TextField(mainFieldName, "Banana is sweet " + i, Field.Store.YES));
            doc.add(new TextField("catalog", "fruit", Field.Store.YES));
            writer.addDocument(doc);
        }
        for (int i = 0; i < 50; ++i)
        {
            Document doc = new Document();
            doc.add(new TextField(mainFieldName, "Juice is sweet " + i, Field.Store.YES));
            doc.add(new TextField("catalog", "drink", Field.Store.YES));
            writer.addDocument(doc);
        }
        for (int i = 0; i < 25; ++i)
        {
            Document doc = new Document();
            doc.add(new TextField(mainFieldName, "Hankcs is here " + i, Field.Store.YES));
            doc.add(new TextField("catalog", "person", Field.Store.YES));
            writer.addDocument(doc);
        }
        writer.close();

        //搜索过程**********************************
        //实例化搜索器

        reader = DirectoryReader.open(directory);
        searcher = new IndexSearcher(reader);

        final String keyword = "sweet";
        //使用QueryParser查询分析器构造Query对象
        QueryParser qp = new QueryParser(mainFieldName, analyzer);
        Query query = qp.parse(keyword);
        System.out.println("Query = " + query);


        //搜索相似度最高的5条记录并且分组
        int topNGroups = 10; // 每页需要多少个组
        int groupOffset = 0; // 起始的组
        boolean fillFields = true;
        Sort docSort = Sort.RELEVANCE; // groupSort用于对组进行排序，docSort用于对组内记录进行排序，多数情况下两者是相同的，但也可不同
        Sort groupSort = docSort;
        int docOffset = 0;   // 用于组内分页，起始的记录
        int docsPerGroup = 2;// 每组返回多少条结果
        boolean requiredTotalGroupCount = true; // 是否需要计算总的组的数量

        // 如果需要对Lucene的score进行修正，则需要重载TermFirstPassGroupingCollector
        TermFirstPassGroupingCollector c1 = new TermFirstPassGroupingCollector("catalog", groupSort, topNGroups);
        boolean cacheScores = true;
        double maxCacheRAMMB = 16.0;
        CachingCollector cachedCollector = CachingCollector.create(c1, cacheScores, maxCacheRAMMB);
        searcher.search(query, cachedCollector);

        Collection<SearchGroup<BytesRef>> topGroups = c1.getTopGroups(groupOffset, fillFields);

        if (topGroups == null)
        {
            // No groups matched
            return;
        }

        Collector secondPassCollector = null;

        boolean getScores = true;
        boolean getMaxScores = true;
        // 如果需要对Lucene的score进行修正，则需要重载TermSecondPassGroupingCollector
        TermSecondPassGroupingCollector c2 = new TermSecondPassGroupingCollector("catalog", topGroups, groupSort, docSort, docsPerGroup, getScores, getMaxScores, fillFields);

        // 是否需要计算一共有多少个分类，这一步是可选的
        TermAllGroupsCollector allGroupsCollector = null;
        if (requiredTotalGroupCount)
        {
            allGroupsCollector = new TermAllGroupsCollector("catalog");
            secondPassCollector = MultiCollector.wrap(c2, allGroupsCollector);
        }
        else
        {
            secondPassCollector = c2;
        }

        if (cachedCollector.isCached())
        {
            // 被缓存的话，就用缓存
            cachedCollector.replay(secondPassCollector);
        }
        else
        {
            // 超出缓存大小，重新执行一次查询
            searcher.search(query, secondPassCollector);
        }

        int totalGroupCount = -1; // 所有组的数量
        int totalHitCount = -1; // 所有满足条件的记录数
        int totalGroupedHitCount = -1; // 所有组内的满足条件的记录数(通常该值与totalHitCount是一致的)
        if (requiredTotalGroupCount)
        {
            totalGroupCount = allGroupsCollector.getGroupCount();
        }
        System.out.println("一共匹配到多少个分类: " + totalGroupCount);

        TopGroups<BytesRef> groupsResult = c2.getTopGroups(docOffset);
        totalHitCount = groupsResult.totalHitCount;
        totalGroupedHitCount = groupsResult.totalGroupedHitCount;
        System.out.println("groupsResult.totalHitCount:" + totalHitCount);
        System.out.println("groupsResult.totalGroupedHitCount:" + totalGroupedHitCount);

        int groupIdx = 0;
        // 迭代组
        for (GroupDocs<BytesRef> groupDocs : groupsResult.groups)
        {
            groupIdx++;
            System.out.println("group[" + groupIdx + "]:" + groupDocs.groupValue); // 组的标识
            System.out.println("group[" + groupIdx + "]:" + groupDocs.totalHits);  // 组内的记录数
            int docIdx = 0;
            // 迭代组内的记录
            for (ScoreDoc scoreDoc : groupDocs.scoreDocs)
            {
                docIdx++;
                System.out.println("group[" + groupIdx + "][" + docIdx + "]:" + scoreDoc.doc + "/" + scoreDoc.score);
                Document doc = searcher.doc(scoreDoc.doc);
                System.out.println("group[" + groupIdx + "][" + docIdx + "]:" + doc);
            }
        }

    }


}
