/************************************************************************
 *
 * Package:     gov.nih.nlm.umls.jekyll.util
 * Object:      JavaToolkit
 *
 * Author:      Vladimir Olenichev
 *
 * Remarks:     
 *
 * Change History: 
 *  10/07/2002: First version
 *
 ***********************************************************************/

package gov.nih.nlm.umls.jekyll.util;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A collection of frequently used, useful methods.
 * 
 * <p>
 * {@link <a href="/vlad-doc/jekyll/src_files/Util/JavaToolkit.java.html">Browse Source</a>}
 */
public class JavaToolkit {

    /**
     * This method is somewhat simplified way to cast an array. Here is a simple
     * example of how to use it:
     * 
     * <pre>
     * 
     *  import gov.nih.nlm.umls.jekyll.util.JavaToolkit;
     * 
     *  // somewhere in your code...
     *  Object[] someData = ...;
     *  String[] someDataAsString = (String[]) castArray( someData, String.class );
     *  
     * </pre>
     * 
     * @param src
     *                  an array to be casted
     * @param targetType
     *                  the type of the target entity
     * @return the casted array
     */
    public static final Object[] castArray(Object[] src, Class targetType) {
        Object[] array = (Object[]) java.lang.reflect.Array.newInstance(
                targetType, src.length);
        System.arraycopy(src, 0, array, 0, src.length);
        return array;
    }

    /**
     * Checks a specified string whether it only contains integers.
     * 
     * @param str
     *                  a string to be checked
     * @return <code>true</code> if the string consists of integers,
     *             <code>false</code> otherwise.
     */
    public static boolean isInteger(String str) {
        Pattern p = Pattern.compile("^[0-9]+$");
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     * Breaks down a space delimited string into its constituent words.
     * 
     * @param str
     *                  a string to be parsed
     * @return an array of tokens
     */
    public static String[] tokenizeString(String str) {
        int i = 0;

        StringTokenizer st = new StringTokenizer(str);
        String[] terms = new String[st.countTokens()];

        while (st.hasMoreTokens()) {
            terms[i] = st.nextToken();
            i++;
        }

        return terms;
    }
}