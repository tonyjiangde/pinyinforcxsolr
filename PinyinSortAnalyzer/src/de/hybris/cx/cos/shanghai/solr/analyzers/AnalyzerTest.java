package de.hybris.cx.cos.shanghai.solr.analyzers;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

public class AnalyzerTest {

	public static void main(String[] args) throws IOException {  
        String s = "数码照相机";  
        
       /* String x = "ewz";
        int result=0;
        int l = Math.min(3, x.length());
        for(int i =0;i<l;i++) {
        	char c = x.charAt(i);
        	int a = 'a';
        	int ioc = c;
        	ioc = ioc -a;
        	System.out.println(ioc);
        	System.out.println(Integer.toBinaryString(ioc));
        	result = result | (ioc << (2-i)*5);
        	System.out.println(Integer.toBinaryString(result));
        }
        System.out.println(result);*/
       /* int a = 'a';
        int z = 'z';
        z =z-a;
        int zz = z << 26;
        System.out.println(a+"---"+z);
        System.out.println(Integer.toBinaryString(z));
        System.out.println(Integer.toBinaryString(zz));*/
        
        Analyzer analyzer = new CXPinyinAnalyzer(true);  
        TokenStream tokenStream = analyzer.tokenStream("text", s);  
        displayTokens(tokenStream);  
  
    }  
      
    public static void displayTokens(TokenStream tokenStream) throws IOException {  
        OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);  
        PositionIncrementAttribute positionIncrementAttribute = tokenStream.addAttribute(PositionIncrementAttribute.class);  
        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);  
        TypeAttribute typeAttribute = tokenStream.addAttribute(TypeAttribute.class);  
          
        tokenStream.reset();  
        int position = 0;  
        while (tokenStream.incrementToken()) {  
            int increment = positionIncrementAttribute.getPositionIncrement();  
            if(increment > 0) {  
                position = position + increment;  
                System.out.print(position + ":");  
            }  
            int startOffset = offsetAttribute.startOffset();  
            int endOffset = offsetAttribute.endOffset();  
            String term = charTermAttribute.toString();  
            System.out.println("[" + term + "]" + ":(" + startOffset + "-->" + endOffset + "):" + typeAttribute.type());  
        }  
        tokenStream.end();  
        tokenStream.close();  
    }  
}
