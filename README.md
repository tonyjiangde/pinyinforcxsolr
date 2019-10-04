# PinyinForCXSolr

Since OOTB CX Commerce provided only very basic support  of double-byte languages like Chinese in Solr search. This small project shows how to customize the solr configurations for commerce suit to have richer facet search support for Chinese language e.g. support of full or short pinyin search of chinese content and sort the conent on first letters of pinyin.



**Environment Setup:**

customizations are built on 1811.9 and solr 7.7

1. Use installer b2c_acc by running : *./install.sh -r b2c_acc*
2. Generate accelerator module by using the yacceleratorstorefront:  *ant modulegen -Dinput.module=accelerator -Dinput.name=AzureHackathon -Dinput.package=de.hybris.azurehackathon -Dinput.template=develop*
3. reinstall all ootb addons on the created storefront: *ant reinstall_addons -Dtarget.storefront=AzureHackathonstorefront*
4. rebuild the sysem: *ant clean all*
5. Initializ the system: *ant initialize*


**Third party Libs:**

Chinese text needs specific tokenzier and analyzer. For tokenizer we have following choices:
1. apache one: org.apache.lucene.analysis.cn.smart.HMMChineseTokenizerFactory 
        
        <tokenizer class="org.apache.lucene.analysis.cn.smart.HMMChineseTokenizerFactory" />
        
2. [IKAnalyzer](https://github.com/blueshen/ik-analyzer)

        <tokenizer class="org.wltea.analyzer.lucene.IKTokenizerFactory" useSmart="false" conf="ik.conf"/>
        
3. [Ansi](https://github.com/NLPchina/ansj_seg)

        <tokenizer class="org.ansj.solr.AnsjTokenizerFactory"  isQuery="false"/>

There are articles compares also the performance of tokenizers e.g. https://www.cnblogs.com/lies-joker/p/4203788.html, you should make a choice.

In order to allow pinyin index and search of chinese on solr, you need also addional analyzer, you can also find third party libs online like pinyinanalyzer from com.shentong.search.analyzers. Most of them are using pinyin4j  (http://pinyin4j.sourceforge.net/) to transform chinese character into pinyin.
So, the basic process flow is that tokenizer will identify works in sentence => analyzer using pinyin4j transform the words into pinyin => pinyin will be indexed by solr for later query.
e.g. 我是中国人 => 我  是  中国人 =>Wo Shi Zhongguoren (full)   w s zgr (short)



**About this Repo:**
In order to extend current configuration of CX Commerce allowing for more standard search, auto sugguestion, spell check and use pinyin to seach chinese content on storefront and backoffice, I take references to online resources and implement classes for this purpose.
1. CXPinyinTransformTokenFilter for transform the chinese token into pinyin, you can config it with follwing properties. For example 中文 zhongwen (full pinyin) zw (short pinyin), we need short for sorting the chinese words on first letter of each character.
        
        /**是否输出原中文*/  
        private boolean isOutChinese;  
        /**是否只转换简拼*/  
        private boolean shortPinyin;  
        /**是否转换全拼+简拼*/  
        private boolean pinyinAll;  
        /**中文词组长度过滤，默认超过2位长度的中文才转换拼音*/  
        private int minTermLength; 

2. CXPinyinNGramTokenFilter, NGram is  useful for auto-complete, it will cut the word by a size specified by yourself.

        For example, the word “paris”, minGramSize takes 2, maxGramSize takes 3, we will get :
        paris => “pa”, “ar”, “ri”, “is”
        => “par”, “ari”, “ris”

3. CXPinyinEdgeNGramTokenFilter, EdgeNGram, it will create n-grams from the beginning edge of a input token.

        Also take the word “paris” as an example, and take minGramSize equals to 2, maxGramSize equals to 10, side from front
        paris => “pa”, “par”, “pari”, “paris”


4. CXPinyinIntField extends IntPointField which convert  string value into integer. Since in order to allow for the sort on first letter of pinyin of each character, the first letters need to be converted to an int value. E.g for “ewh”, calculate the difference of each letter to ascii a, you got 5，23，8 and the bin are: 00101，10101，01000, combine them 001011010101000 then sort the int value, you have what you want. Here I calculate only for the first letter of first three character, you should modify this base on your needs.




**How to use it in commerce:**

1.   Place pinyin4j-2.5.0.jar (http://pinyin4j.sourceforge.net/) and dist/CXCOSShanghaiPinyinSortAnalyzer.jar at solr-webapp/webapp/WEB-INF/lib/.
2.   Modify schema.xml of commerce solr config
      Add  filed types, one for index chinese content as pinyin:
      
                <fieldType name="text_pinyin" class="solr.TextField" positionIncrementGap="100">
                                        <analyzer type="index">
                                            <tokenizer class="org.apache.lucene.analysis.cn.smart.HMMChineseTokenizerFactory"/>
                                            <filter class="de.hybris.cx.cos.shanghai.solr.analyzers.CXPinyinTransformTokenFilterFactory" minTermLength="1" />
                                            <filter class="de.hybris.cx.cos.shanghai.solr.analyzers.CXPinyinNGramTokenFilterFactory" minGram="1" maxGram="20" />
                                        </analyzer>
                                        <analyzer type="query">
                                            <tokenizer class="org.apache.lucene.analysis.cn.smart.HMMChineseTokenizerFactory"/>
                                            <filter class="de.hybris.cx.cos.shanghai.solr.analyzers.CXPinyinTransformTokenFilterFactory" minTermLength="1" />
                                            <filter class="de.hybris.cx.cos.shanghai.solr.analyzers.CXPinyinNGramTokenFilterFactory" minGram="1" maxGram="20" />
                                        </analyzer>
                </fieldType>
    
        one for sorting
    
            <fieldType name="text_pinyin_sort" class="de.hybris.cx.cos.shanghai.solr.analyzers.CXPinyinIntField" positionIncrementGap="100">
            </fieldType>
    

3. Define fields for each field of Chinese language and copy the value to new fields:

        <dynamicField name="*_text_zh_pinyin" type="text_pinyin" indexed="true" stored="true" multiValued ="false"/>
        <dynamicField name="*_text_zh_mv_pinyin" type="text_pinyin" indexed="true" stored="true" multiValued="true" />


        <copyField source="*_text_zh" dest="*_text_zh_pinyin"/>
        <copyField source="*_text_zh_mvn" dest="*_text_zh_mv_pinyin"/>

        <dynamicField name="*_text_zh_pinyin_sort" type="text_pinyin_sort" indexed="true" stored="true" multiValued ="false" />

        <copyField source="*_text_zh" dest="*_text_zh_pinyin_sort"/>

4. Restart hybris and execute full index in backoffice and check in solr admin console.
   
  Use Analysis tool you can see "照相机" would be tranform to pinyin "zhaoxiangji" and "zxj" ![screen shot](https://github.com/tonyjiangde/pinyinforcxsolr/blob/master/images/analysis-pinyin.png)

 You can also search chinese content using pinyin ![screen shot](https://github.com/tonyjiangde/pinyinforcxsolr/blob/master/images/search.png)

 You can also sort chinese content using the "*_text_zh_pinyin_sort" field ![screen shot](https://github.com/tonyjiangde/pinyinforcxsolr/blob/master/images/sort.png)
