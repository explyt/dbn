package com.dbn.editor.copyright;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.maddyhome.idea.copyright.CopyrightProfile;
import com.maddyhome.idea.copyright.psi.UpdateAnyFileCopyright.Provider;
import com.maddyhome.idea.copyright.psi.UpdateCopyright;

public class DatabaseFileCopyrightProvider extends Provider {

    public DatabaseFileCopyrightProvider() {
        super();
    }

    @Override
    public UpdateCopyright createInstance(Project project, Module module, VirtualFile file, FileType base, CopyrightProfile options) {
        return new DatabaseFileCopyright(project, module, file, options);
    }


}
