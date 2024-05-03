package com.dbn.language.common;

import com.dbn.vfs.DBVirtualFile;
import com.dbn.vfs.file.DBConsoleVirtualFile;
import com.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.codeInsight.daemon.impl.analysis.FileHighlightingSetting;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.codeInsight.daemon.impl.analysis.FileHighlightingSetting.FORCE_HIGHLIGHTING;
import static com.intellij.codeInsight.daemon.impl.analysis.FileHighlightingSetting.SKIP_HIGHLIGHTING;

public class DefaultHighlightingSettingProvider extends com.intellij.codeInsight.daemon.impl.analysis.DefaultHighlightingSettingProvider {
    @Override
    public @Nullable FileHighlightingSetting getDefaultSetting(@NotNull Project project, @NotNull VirtualFile file) {
        if (file instanceof DBSourceCodeVirtualFile) return FORCE_HIGHLIGHTING;
        if (file instanceof DBConsoleVirtualFile) return FORCE_HIGHLIGHTING;
        if (file instanceof DBVirtualFile) return SKIP_HIGHLIGHTING;

        return null;
    }
}
