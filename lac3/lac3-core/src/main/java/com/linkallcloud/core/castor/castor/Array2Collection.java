package com.linkallcloud.core.castor.castor;

import java.lang.reflect.Array;
import java.util.Collection;

import com.linkallcloud.core.castor.Castor;
import com.linkallcloud.core.castor.FailToCastObjectException;

@SuppressWarnings({"unchecked", "rawtypes"})
public class Array2Collection extends Castor<Object, Collection> {

    public Array2Collection() {
        this.fromClass = Array.class;
        this.toClass = Collection.class;
    }

    @Override
    public Collection<?> cast(Object src, Class<?> toType, String... args)
            throws FailToCastObjectException {
        Collection coll = createCollection(src, toType);
        for (int i = 0; i < Array.getLength(src); i++)
            coll.add(Array.get(src, i));
        return coll;

    }
}
