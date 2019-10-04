package de.hybris.cx.cos.shanghai.solr.analyzers;

import java.util.Map;  
import org.apache.lucene.analysis.TokenFilter;  
import org.apache.lucene.analysis.TokenStream;    
import org.apache.lucene.analysis.util.TokenFilterFactory;

import de.hybris.cx.cos.shanghai.solr.pinyin.utils.Constant;

public class CXPinyinTransformTokenFilterFactory extends TokenFilterFactory{

	 /**是否输出原中文*/  
    private boolean outChinese;  
    /**是否只转换简拼*/  
    private boolean shortPinyin;  
    /**是否转换全拼+简拼*/  
    private boolean pinyinAll;  
    /**中文词组长度过滤，默认超过minTermLength长度的中文才转换拼音*/  
    private int minTermLength;  
    /**是否转换前三个字中文首字母用于排序*/
    private boolean tranformtointforsort;
    
    public boolean isTranformtointforsort() {
		return tranformtointforsort;
	}

	public void setTranformtointforsort(boolean tranformtointforsort) {
		this.tranformtointforsort = tranformtointforsort;
	}

	public CXPinyinTransformTokenFilterFactory(Map<String, String> args) {  
        super(args);  
        this.outChinese = getBoolean(args, "outChinese", Constant.DEFAULT_OUT_CHINESE);  
        this.shortPinyin = getBoolean(args, "shortPinyin", Constant.DEFAULT_SHORT_PINYIN);  
        this.pinyinAll = getBoolean(args, "pinyinAll", Constant.DEFAULT_PINYIN_ALL);  
        this.minTermLength = getInt(args, "minTermLength", Constant.DEFAULT_MIN_TERM_LRNGTH);  
        this.tranformtointforsort = getBoolean(args, "tranformtointforsort", Constant.DEFAULT_TRANFORM_TO_INT_FOR_SORT);  
    }  
  
    public TokenFilter create(TokenStream input) {  
        return new CXPinyinTransformTokenFilter(input,this.tranformtointforsort, this.shortPinyin,this.outChinese,  
                this.minTermLength);  
    }  
  
    public boolean isOutChinese() {  
        return outChinese;  
    }  
  
    public void setOutChinese(boolean outChinese) {  
        this.outChinese = outChinese;  
    }  
  
    public boolean isShortPinyin() {  
        return shortPinyin;  
    }  
  
    public void setShortPinyin(boolean shortPinyin) {  
        this.shortPinyin = shortPinyin;  
    }  
  
    public boolean isPinyinAll() {  
        return pinyinAll;  
    }  
  
    public void setPinyinAll(boolean pinyinAll) {  
        this.pinyinAll = pinyinAll;  
    }  
  
    public int getMinTermLength() {  
          
          
        return minTermLength;  
    }  
  
    public void setMinTermLength(int minTermLength) {  
        this.minTermLength = minTermLength;  
    }  
}
