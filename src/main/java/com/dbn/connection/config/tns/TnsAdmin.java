package com.dbn.connection.config.tns;

import com.dbn.common.util.Environment;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TnsAdmin {
    public static String location() {
        return Environment.getVariable("TNS_ADMIN");
    }
}
