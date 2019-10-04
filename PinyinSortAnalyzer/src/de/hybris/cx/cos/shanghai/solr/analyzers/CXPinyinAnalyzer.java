package de.hybris.cx.cos.shanghai.solr.analyzers;

import java.io.BufferedReader;  
import java.io.Reader;  
import java.io.StringReader;  
  
import org.apache.lucene.analysis.Analyzer;  
import org.apache.lucene.analysis.TokenStream;  
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.cn.smart.HMMChineseTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import de.hybris.cx.cos.shanghai.solr.pinyin.utils.Constant;

//import org.wltea.analyzer.lucene.IKAnalyzer;
//import org.wltea.analyzer.lucene.IKTokenizer; 

public class CXPinyinAnalyzer extends Analyzer {  
    private int minGram;  
    private int maxGram;  
    private boolean useSmart;  
    /** 是否需要对中文进行NGram[默认为false] */  
    private boolean nGramChinese;  
    /** 是否需要对纯数字进行NGram[默认为false] */  
    private boolean nGramNumber;  
    /**是否开启edgesNGram模式*/  
    private boolean edgesNGram;  
      
    private boolean tranformtointforsort;
    
    public CXPinyinAnalyzer() {  
        this(Constant.DEFAULT_TRANFORM_TO_INT_FOR_SORT);  
    }  
    
    public CXPinyinAnalyzer(boolean tranformtointforsort) {  
        this(tranformtointforsort,Constant.DEFAULT_IK_USE_SMART);  
    }  
    
    public CXPinyinAnalyzer(boolean tranformtointforsort,boolean useSmart) {  
        this(tranformtointforsort,Constant.DEFAULT_MIN_GRAM, Constant.DEFAULT_MAX_GRAM, Constant.DEFAULT_EDGES_GRAM, useSmart,Constant.DEFAULT_NGRAM_CHINESE);  
    }  
      
    public CXPinyinAnalyzer(int minGram) {  
        this(Constant.DEFAULT_TRANFORM_TO_INT_FOR_SORT,minGram, Constant.DEFAULT_MAX_GRAM, Constant.DEFAULT_EDGES_GRAM, Constant.DEFAULT_IK_USE_SMART, Constant.DEFAULT_NGRAM_CHINESE,Constant.DEFAULT_NGRAM_NUMBER);  
    }  
  
    public CXPinyinAnalyzer(int minGram,boolean useSmart) {  
        this(Constant.DEFAULT_TRANFORM_TO_INT_FOR_SORT,minGram, Constant.DEFAULT_MAX_GRAM, Constant.DEFAULT_EDGES_GRAM, useSmart,Constant.DEFAULT_NGRAM_CHINESE);  
    }  
      
    public CXPinyinAnalyzer(int minGram, int maxGram) {  
        this(minGram, maxGram, Constant.DEFAULT_EDGES_GRAM);  
    }  
      
    public CXPinyinAnalyzer(int minGram, int maxGram,boolean edgesNGram) {  
        this(minGram, maxGram, edgesNGram, Constant.DEFAULT_IK_USE_SMART);  
    }  
      
    public CXPinyinAnalyzer(int minGram, int maxGram,boolean edgesNGram,boolean useSmart) {  
        this(Constant.DEFAULT_TRANFORM_TO_INT_FOR_SORT,minGram, maxGram, edgesNGram, useSmart,Constant.DEFAULT_NGRAM_CHINESE);  
    }  
  
    public CXPinyinAnalyzer(boolean tranformtointforsort,int minGram, int maxGram,boolean edgesNGram,boolean useSmart,  
            boolean nGramChinese) {  
        this(tranformtointforsort,minGram, maxGram, edgesNGram, useSmart,nGramChinese,Constant.DEFAULT_NGRAM_NUMBER);  
    }  
      
    public CXPinyinAnalyzer(boolean tranformtointforsort,int minGram, int maxGram,boolean edgesNGram,boolean useSmart,  
            boolean nGramChinese,boolean nGramNumber) {  
        super();  
        this.minGram = minGram;  
        this.maxGram = maxGram;  
        this.edgesNGram = edgesNGram;  
        this.useSmart = useSmart;  
        this.nGramChinese = nGramChinese;  
        this.nGramNumber = nGramNumber;  
        this.tranformtointforsort =tranformtointforsort;
    }  
  
    @Override  
    protected TokenStreamComponents createComponents(String fieldName) {  
        Reader reader = new BufferedReader(new StringReader(fieldName));  
        Tokenizer tokenizer = new HMMChineseTokenizer();
        tokenizer.setReader(reader);
        //转拼音  
        TokenStream tokenStream = new CXPinyinTransformTokenFilter(tokenizer, tranformtointforsort,false, 
            Constant.DEFAULT_SHORT_PINYIN,Constant.DEFAULT_PINYIN_ALL, 1);
        //对拼音进行NGram处理  
        if(edgesNGram) {  
            tokenStream = new CXPinyinEdgeNGramTokenFilter(tokenStream,this.minGram,  
                this.maxGram,this.nGramChinese,this.nGramNumber);  
        } else {  
            tokenStream = new CXPinyinNGramTokenFilter(tokenStream,this.minGram,  
                    this.maxGram,this.nGramChinese,this.nGramNumber);  
        }  
        return new Analyzer.TokenStreamComponents(tokenizer, tokenStream);  
    }  
} 
