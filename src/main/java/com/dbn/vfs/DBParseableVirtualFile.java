package com.dbn.vfs;

import com.dbn.language.common.DBLanguage;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiFile;

public interface DBParseableVirtualFile extends DBVirtualFile{
    Key<String> PARSE_ROOT_ID_KEY = new Key<>("DBN_PARSE_ROOT_ID");

    PsiFile initializePsiFile(DatabaseFileViewProvider fileViewProvider, DBLanguage<?> language);
}
