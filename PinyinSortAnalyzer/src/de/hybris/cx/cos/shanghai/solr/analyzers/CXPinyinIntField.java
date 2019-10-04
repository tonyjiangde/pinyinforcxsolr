package de.hybris.cx.cos.shanghai.solr.analyzers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.SortField;
import org.apache.lucene.util.NumericUtils;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IntPointField;
import org.apache.solr.schema.NumberType;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.uninverting.UninvertingReader.Type;

import de.hybris.cx.cos.shanghai.solr.pinyin.utils.Pinyin4jUtil;

public class CXPinyinIntField extends IntPointField {
	
	private int pytoint(String pinyin) {
        int result=0;
        int l = Math.min(3, pinyin.length());
        for(int i =0;i<l;i++) {
        	char c = pinyin.charAt(i);
        	int a = 'a';
        	int ioc = c;
        	ioc = ioc -a;
        	result = result | (ioc << (2-i)*8);
        	
        }
        return result;
    }
	
	/*@Override
	  public void write(TextResponseWriter writer, String name, IndexableField f) throws IOException {
		String s = f.stringValue();
		
		if (s.length()==0) {
		      writer.writeNull(name);
		      return;
		    }
		    try {
	
		    	int i = Math.min(3, s.length());
		     String pinyinshort = Pinyin4jUtil.getPinyinShort(s.substring(0,i)); 
		      writer.writeInt(name,pytoint(pinyinshort));
		    } catch (NumberFormatException e){
		      // can't parse - write out the contents as a string so nothing is lost and
		      // clients don't get a parse error.
		      writer.writeStr(name, s, true);
		    }
	  }*/
	@Override
	  public IndexableField createField(SchemaField field, Object value) {
		int intValue;
		System.out.println(value.getClass().getName());
		if(value instanceof Number) {
			intValue = ((Number) value).intValue();
			System.out.println("we got a int:"+intValue);
		}else {
			String s = value.toString();
			System.out.println("we got a String:"+s);
			int i = Math.min(3, s.length());
			 String pinyinshort = Pinyin4jUtil.getPinyinShort(s.substring(0,i)); 
			 System.out.println("we got a pinyinshort:"+pinyinshort);
			 intValue = Integer.valueOf(pytoint(pinyinshort));
			 System.out.println("we got a pinyinshort to int:"+intValue);
		}
	    return new IntPoint(field.getName(), intValue);
	  }
	
	@Override
	  public List<IndexableField> createFields(SchemaField sf, Object value) {
		if(value instanceof Number) {
			
			System.out.println("we got a int:"+((Number) value).intValue());
		}else {
			String s = value.toString();
			System.out.println("we got a String:"+s);
			int i = Math.min(3, s.length());
			 String pinyinshort = Pinyin4jUtil.getPinyinShort(s.substring(0,i)); 
			 System.out.println("we got a pinyinshort:"+pinyinshort);
			 value = Integer.valueOf(pytoint(pinyinshort));
			 System.out.println("we got a pinyinshort to int:"+value);
		}
	    List<IndexableField> fields = new ArrayList<>(3);
	    IndexableField field = null;
	    if (sf.indexed()) {
	      field = createField(sf, value);
	      fields.add(field);
	    }
	    
	    if (sf.hasDocValues()) {
	      final Number numericValue;
	      if (field == null) {
	        final Object nativeTypeObject = toNativeType(value);
	        if (getNumberType() == NumberType.DATE) {
	          numericValue = ((Date)nativeTypeObject).getTime();
	        } else {
	          numericValue = (Number) nativeTypeObject;
	        }
	      } else {
	        numericValue = field.numericValue();
	      }
	      final long bits;
	      if (!sf.multiValued()) {
	        if (numericValue instanceof Integer || numericValue instanceof Long) {
	          bits = numericValue.longValue();
	        } else if (numericValue instanceof Float) {
	          bits = Float.floatToIntBits(numericValue.floatValue());
	        } else {
	          assert numericValue instanceof Double;
	          bits = Double.doubleToLongBits(numericValue.doubleValue());
	        }
	        fields.add(new NumericDocValuesField(sf.getName(), bits));
	      } else {
	        // MultiValued
	        if (numericValue instanceof Integer || numericValue instanceof Long) {
	          bits = numericValue.longValue();
	        } else if (numericValue instanceof Float) {
	          bits = NumericUtils.floatToSortableInt(numericValue.floatValue());
	        } else {
	          assert numericValue instanceof Double;
	          bits = NumericUtils.doubleToSortableLong(numericValue.doubleValue());
	        }
	        fields.add(new SortedNumericDocValuesField(sf.getName(), bits));
	      }
	    } 
	    if (sf.stored()) {
	      fields.add(getStoredField(sf, value));
	    }
	    return fields;
	  }
	/*@Override
	public SortField getSortField(SchemaField field, boolean top) {
		field.checkSortability();
	    return new SortField(field.getName(),SortField.Type.INT, top);
	}*/

}
