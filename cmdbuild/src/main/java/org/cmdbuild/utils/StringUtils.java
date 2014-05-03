package org.cmdbuild.utils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;

import java.util.Arrays;

public class StringUtils {
	
	// because "toString" is not always viable...
	public interface Stringyfier<T> {
		String stringify(T obj);
	}
	public static final Stringyfier<Object> DefaultStringyfier = new Stringyfier<Object>() {
		public String stringify(Object obj) { return obj.toString(); };
	};
	
	public static <T> String join(Collection<T> coll, String delimiter, Stringyfier<T> stringyfier) {
		StringBuffer buffer = new StringBuffer();
		boolean first = true;
		for( T item : coll ) {
			if(first){first=false;}else{buffer.append(delimiter);}
			buffer.append( stringyfier.stringify(item) );
		}
		return buffer.toString();
	}
	public static <T> String join(T[] coll, String delimiter, Stringyfier<T> stringyfier) {
		return join(Arrays.asList(coll),delimiter, stringyfier);
	}
	
	
	public static String ucFirst(String str) {
		return str.substring(0,1).toUpperCase() + str.substring(1).toLowerCase();
	}
	
	public static String join(Collection<?> s, String delimiter) {
        return join(s.iterator(), delimiter);
    }
	public static String join(Object[] objs, String delimiter) {
		return join(Arrays.asList(objs),delimiter);
	}

	public static String join(Iterator<?> iter, String delimiter) {
        StringBuffer buffer = new StringBuffer();
        if (iter.hasNext()) {
            buffer.append(iter.next().toString());
            while (iter.hasNext()) {
                buffer.append(delimiter).append(iter.next().toString());
            }
        }
        return buffer.toString();
    }

	public static String arrayToCsv(Object array) {
		StringBuffer output = new StringBuffer();
		if (array!=null) {
			for (int i=0;i<Array.getLength(array);i++) {
				if (i!=0) {
					output.append(",");
				}
				output.append(Array.get(array, i));
			}
		}
		return output.toString();
	}
}
