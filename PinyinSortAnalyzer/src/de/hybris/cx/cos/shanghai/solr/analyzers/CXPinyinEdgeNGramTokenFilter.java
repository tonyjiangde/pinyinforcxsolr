package de.hybris.cx.cos.shanghai.solr.analyzers;

import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.Attribute;

import de.hybris.cx.cos.shanghai.solr.pinyin.utils.Constant;
import de.hybris.cx.cos.shanghai.solr.pinyin.utils.StringUtils;

public class CXPinyinEdgeNGramTokenFilter extends TokenFilter {  
    private final int minGram;  
    private final int maxGram;  
    /** 是否需要对中文进行NGram[默认为false] */  
    private final boolean nGramChinese;  
    /** 是否需要对纯数字进行NGram[默认为false] */  
    private final boolean nGramNumber;   
    private char[] curTermBuffer;  
    private int curTermLength;  
    private int curCodePointCount;  
    private int curGramSize;  
    private int tokStart;  
    private int tokEnd;  
    private int savePosIncr;  
    private int savePosLen;  
  
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);  
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);  
    private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);  
    private final PositionLengthAttribute posLenAtt = addAttribute(PositionLengthAttribute.class);  
    private TypeAttribute typeAtt = addAttribute(TypeAttribute.class);  
      
    public CXPinyinEdgeNGramTokenFilter(TokenStream input, int minGram,  
            int maxGram, boolean nGramChinese, boolean nGramNumber) {  
        super(input);  
        if (minGram < 1) {  
            throw new IllegalArgumentException(  
                    "minGram must be greater than zero");  
        }  
  
        if (minGram > maxGram) {  
            throw new IllegalArgumentException(  
                    "minGram must not be greater than maxGram");  
        }  
  
        this.minGram = minGram;  
        this.maxGram = maxGram;  
        this.nGramChinese = nGramChinese;  
        this.nGramNumber = nGramNumber;  
    }  
      
    public CXPinyinEdgeNGramTokenFilter(TokenStream input, int minGram,  
            int maxGram, boolean nGramChinese) {  
        this(input, minGram, maxGram, nGramChinese, Constant.DEFAULT_NGRAM_NUMBER);  
    }  
      
    public CXPinyinEdgeNGramTokenFilter(TokenStream input, int minGram,  
            int maxGram) {  
        this(input, minGram, maxGram, Constant.DEFAULT_NGRAM_CHINESE);  
    }  
      
    public CXPinyinEdgeNGramTokenFilter(TokenStream input, int minGram) {  
        this(input, minGram, Constant.DEFAULT_MAX_GRAM);  
    }  
      
    public CXPinyinEdgeNGramTokenFilter(TokenStream input) {  
        this(input, Constant.DEFAULT_MIN_GRAM);  
    }  
  
    public int codePointCount(CharSequence seq) {
        return Character.codePointCount(seq, 0, seq.length());
      }
    
    public int offsetByCodePoints(char[] buf, int start, int count, int index, int offset) {
        return Character.offsetByCodePoints(buf, start, count, index, offset);
      }
    @Override  
    public final boolean incrementToken() throws IOException {  
        while (true) {  
            if (curTermBuffer == null) {  
                if (!input.incrementToken()) {  
                    return false;  
                }  
                String type = this.typeAtt.type();  
                if(null != type && "normal_word".equals(type)) {  
                    return true;  
                }  
                if(null != type && "numeric_original".equals(type)) {  
                    return true;  
                }  
                if(null != type && "chinese_original".equals(type)) {  
                    return true;  
                }  
                if ((!this.nGramNumber)  
                        && (StringUtils.isNumeric(this.termAtt.toString()))) {  
                    return true;  
                }  
                if ((!this.nGramChinese)  
                        && (StringUtils.containsChinese(this.termAtt.toString()))) {  
                    return true;  
                }  
                curTermBuffer = termAtt.buffer().clone();  
                curTermLength = termAtt.length();  
                curCodePointCount = codePointCount(termAtt);  
                curGramSize = minGram;  
                tokStart = offsetAtt.startOffset();  
                tokEnd = offsetAtt.endOffset();  
                savePosIncr += posIncrAtt.getPositionIncrement();  
                savePosLen = posLenAtt.getPositionLength();  
            }  
            if (curGramSize <= maxGram) {   
                if (curGramSize <= curCodePointCount) {   
                    clearAttributes();  
                    offsetAtt.setOffset(tokStart, tokEnd);  
                    if (curGramSize == minGram) {  
                        posIncrAtt.setPositionIncrement(savePosIncr);  
                        savePosIncr = 0;  
                    } else {  
                        posIncrAtt.setPositionIncrement(0);  
                    }  
                    posLenAtt.setPositionLength(savePosLen);  
                    final int charLength = offsetByCodePoints(  
                            curTermBuffer, 0, curTermLength, 0, curGramSize);  
                    termAtt.copyBuffer(curTermBuffer, 0, charLength);  
                    curGramSize++;  
                    return true;  
                }  
            }  
            curTermBuffer = null;  
        }  
    }  
  
    @Override  
    public void reset() throws IOException {  
        super.reset();  
        curTermBuffer = null;  
        savePosIncr = 0;  
    }  
}  
