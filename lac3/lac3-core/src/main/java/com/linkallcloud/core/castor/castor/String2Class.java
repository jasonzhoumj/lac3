package com.linkallcloud.core.castor.castor;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;

import com.linkallcloud.core.castor.Castor;
import com.linkallcloud.core.castor.FailToCastObjectException;
import com.linkallcloud.core.lang.Lang;

@SuppressWarnings({"rawtypes"})
public class String2Class extends Castor<String, Class> {

    public String2Class() {
        fromClass = String.class;
        toClass = Class.class;
    }

    public static final Map<String, Class<?>> map = new HashMap<String, Class<?>>();
    static {
        map.put("long", long.class);
        map.put("int", int.class);
        map.put("short", short.class);
        map.put("byte", byte.class);
        map.put("float", float.class);
        map.put("double", double.class);
        map.put("char", char.class);
        map.put("boolean", boolean.class);
    }

    @Override
    public Class<?> cast(String src, Class toType, String... args) {
        if (null == src)
            return null;
        Class<?> c = map.get(src);
        if (null != c)
            return c;
        try {
            return Lang.loadClass(src);
        }
        catch (ClassNotFoundException e) {
            throw new FailToCastObjectException(format("String '%s' can not cast to Class<?>!", src));
        }
    }

}
