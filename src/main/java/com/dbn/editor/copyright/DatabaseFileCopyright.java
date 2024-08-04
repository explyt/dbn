package com.dbn.editor.copyright;

import com.dbn.language.common.DBLanguagePsiFile;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.maddyhome.idea.copyright.CopyrightProfile;
import com.maddyhome.idea.copyright.psi.UpdateAnyFileCopyright;

public class DatabaseFileCopyright extends UpdateAnyFileCopyright {
    public DatabaseFileCopyright(Project project, Module module, VirtualFile root, CopyrightProfile options) {
        super(project, module, root, options);
    }

    @Override
    protected boolean accept() {
        return getFile() instanceof DBLanguagePsiFile;
    }

    @Override
    protected void scanFile() {
        super.scanFile();
    }
}
