package com.linkallcloud.core.lang.tmpl;

import com.linkallcloud.core.castor.Castors;
import com.linkallcloud.core.lang.Strings;

class TmplFloatEle extends TmplDynamicEle {

    public TmplFloatEle(String key, String fmt, String dft) {
        super("float", key, fmt, dft);
        this.fmt = Strings.sNull(fmt, "%#.2f");
    }

    @Override
    protected String _val(Object val) {
        Float n = Castors.me().castTo(val, Float.class);
        if (null != n) {
            return String.format(fmt, n);
        }
        return null;
    }

}
