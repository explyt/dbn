package com.dbn.data.type;

import com.dbn.common.constant.Constant;
import com.dbn.common.ui.Presentable;
import com.dbn.nls.NlsResources;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.dbn.nls.NlsResources.txt;

@Getter
@AllArgsConstructor
public enum GenericDataType implements Presentable, Constant<GenericDataType> {
    LITERAL(txt("app.data.const.GenericDataType_LITERAL")),
    NUMERIC(txt("app.data.const.GenericDataType_NUMERIC")),
    DATE_TIME(txt("app.data.const.GenericDataType_DATE_TIME")),
    CLOB(txt("app.data.const.GenericDataType_CLOB")),
    NCLOB(txt("app.data.const.GenericDataType_NCLOB")),
    BLOB(txt("app.data.const.GenericDataType_BLOB")),
    ROWID(txt("app.data.const.GenericDataType_ROWID")),
    REF(txt("app.data.const.GenericDataType_REF")),
    FILE(txt("app.data.const.GenericDataType_FILE")),
    BOOLEAN(txt("app.data.const.GenericDataType_BOOLEAN")),
    OBJECT(txt("app.data.const.GenericDataType_OBJECT")),
    CURSOR(txt("app.data.const.GenericDataType_CURSOR")),
    TABLE(txt("app.data.const.GenericDataType_TABLE")),
    ARRAY(txt("app.data.const.GenericDataType_ARRAY")),
    COLLECTION(txt("app.data.const.GenericDataType_COLLECTION")),
    XMLTYPE(txt("app.data.const.GenericDataType_XMLTYPE")),
    PROPRIETARY(txt("app.data.const.GenericDataType_PROPRIETARY")),
    COMPLEX(txt("app.data.const.GenericDataType_COMPLEX")),
    ;

    private final String name;

    public boolean is(GenericDataType... genericDataTypes) {
        for (GenericDataType genericDataType : genericDataTypes) {
            if (this == genericDataType) return true;
        }
        return false;
    }

    public boolean isLOB() {
        return is(BLOB, CLOB, NCLOB, XMLTYPE);
    }


}
