package de.hybris.cx.cos.shanghai.solr.analyzers;

import java.io.IOException;

import org.apache.lucene.analysis.CharacterUtils;
import org.apache.lucene.analysis.TokenFilter;  
import org.apache.lucene.analysis.TokenStream;  
import org.apache.lucene.analysis.miscellaneous.CodepointCountFilter;  
 
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;  
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;  
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;  
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute;  
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;  
//import org.apache.lucene.analysis.util.CharacterUtils;  

import de.hybris.cx.cos.shanghai.solr.pinyin.utils.Constant;
import de.hybris.cx.cos.shanghai.solr.pinyin.utils.StringUtils;

public class CXPinyinNGramTokenFilter extends TokenFilter {  
    private char[] curTermBuffer;  
    private int curTermLength;  
    private int curCodePointCount;  
    private int curGramSize;  
    private int curPos;  
    private int curPosInc, curPosLen;  
    private int tokStart;  
    private int tokEnd;  
    private boolean hasIllegalOffsets;  
  
    private int minGram;  
    private int maxGram;  
    /** 是否需要对中文进行NGram[默认为false] */  
    private final boolean nGramChinese;  
    /** 是否需要对纯数字进行NGram[默认为false] */  
    private final boolean nGramNumber;  
  
    private CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);  
    private PositionIncrementAttribute posIncAtt;  
    private PositionLengthAttribute posLenAtt;  
    private OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);  
    private TypeAttribute typeAtt;  
  
    public CXPinyinNGramTokenFilter(TokenStream input, int minGram, int maxGram,  
            boolean nGramChinese,boolean nGramNumber) {  
        super(new CodepointCountFilter(input, minGram, Integer.MAX_VALUE));  
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
          
        this.termAtt = addAttribute(CharTermAttribute.class);  
        this.offsetAtt = addAttribute(OffsetAttribute.class);  
        this.typeAtt = addAttribute(TypeAttribute.class);  
        this.posIncAtt = addAttribute(PositionIncrementAttribute.class);  
        this.posLenAtt = addAttribute(PositionLengthAttribute.class);  
    }  
  
    public CXPinyinNGramTokenFilter(TokenStream input, int minGram, int maxGram,  
            boolean nGramChinese) {  
        this(input, minGram, maxGram, nGramChinese, Constant.DEFAULT_NGRAM_NUMBER);  
    }  
      
    public CXPinyinNGramTokenFilter(TokenStream input, int minGram, int maxGram) {  
        this(input, minGram, maxGram, Constant.DEFAULT_NGRAM_CHINESE);  
    }  
      
    public CXPinyinNGramTokenFilter(TokenStream input, int minGram) {  
        this(input, minGram, Constant.DEFAULT_MAX_GRAM);  
    }  
      
    public CXPinyinNGramTokenFilter(TokenStream input) {  
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
                curPos = 0;  
                curPosInc = posIncAtt.getPositionIncrement();  
                curPosLen = posLenAtt.getPositionLength();  
                tokStart = offsetAtt.startOffset();  
                tokEnd = offsetAtt.endOffset();  
  
                hasIllegalOffsets = (tokStart + curTermLength) != tokEnd;  
            }  
  
            if (curGramSize > maxGram  
                    || (curPos + curGramSize) > curCodePointCount) {  
                ++curPos;  
                curGramSize = minGram;  
            }  
            if ((curPos + curGramSize) <= curCodePointCount) {  
                clearAttributes();  
                final int start = offsetByCodePoints(curTermBuffer,  
                        0, curTermLength, 0, curPos);  
                final int end = offsetByCodePoints(curTermBuffer, 0,  
                        curTermLength, start, curGramSize);  
                termAtt.copyBuffer(curTermBuffer, start, end - start);  
                posIncAtt.setPositionIncrement(curPosInc);  
                curPosInc = 0;  
                posLenAtt.setPositionLength(curPosLen);  
                offsetAtt.setOffset(tokStart, tokEnd);  
                curGramSize++;  
                return true;  
            }  
            curTermBuffer = null;  
        }  
    }  
  
    @Override  
    public void reset() throws IOException {  
        super.reset();  
        curTermBuffer = null;  
    }  
}  
