package com.dci.intellij.dbn.common.file.util;

import com.dci.intellij.dbn.vfs.DBVirtualFileImpl;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.StandardFileSystems;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.io.ReadOnlyAttributeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.io.File;
import java.io.IOException;

public class VirtualFileUtil {

    public static Icon getIcon(VirtualFile virtualFile) {
        if (virtualFile instanceof DBVirtualFileImpl) {
            DBVirtualFileImpl file = (DBVirtualFileImpl) virtualFile;
            return file.getIcon();
        }
        return virtualFile.getFileType().getIcon();
    }

    public static boolean isDatabaseFileSystem(@NotNull VirtualFile file) {
        return file.getFileSystem() instanceof DatabaseFileSystem;
    }

    public static boolean isLocalFileSystem(@NotNull VirtualFile file) {
        return file.isInLocalFileSystem();
    }

    public static boolean isVirtualFileSystem(@NotNull VirtualFile file) {
        return !isDatabaseFileSystem(file) && !isLocalFileSystem(file);
    }

    public static VirtualFile ioFileToVirtualFile(File file) {
        return LocalFileSystem.getInstance().findFileByIoFile(file);
    }

    public static void setReadOnlyAttribute(VirtualFile file, boolean readonly) {
        try {
            ReadOnlyAttributeUtil.setReadOnlyAttribute(file, readonly);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setReadOnlyAttribute(String path, boolean readonly) {
        try {
            ReadOnlyAttributeUtil.setReadOnlyAttribute(path, readonly);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static VirtualFile[] lookupFilesForName(Project project, String name) {
        VirtualFile[] contentRoots = getContentRoots(project);
        return lookupFilesForName(contentRoots, name);
    }

    public static VirtualFile[] lookupFilesForName(VirtualFile[] roots, String name) {
        FileCollector collector = new FileCollector(FileCollectorType.NAME, name);
        return collectFiles(roots, collector);
    }

    public static VirtualFile[] lookupFilesForExtensions(Project project, String ... extensions) {
        FileCollector collector = new FileCollector(FileCollectorType.EXTENSION, extensions);
        return collectFiles(getContentRoots(project), collector);
    }

    private static VirtualFile[] collectFiles(VirtualFile[] roots, FileCollector collector) {
        for (VirtualFile root : roots) {
            VfsUtilCore.visitChildrenRecursively(root, collector);
        }
        return collector.files();
    }

    @NotNull
    private static VirtualFile[] getContentRoots(Project project) {
        ProjectRootManager rootManager = ProjectRootManager.getInstance(project);
        return rootManager.getContentRoots();
    }

    public static String ensureFilePath(String fileUrlOrPath) {
        if (fileUrlOrPath != null && fileUrlOrPath.startsWith(StandardFileSystems.FILE_PROTOCOL_PREFIX)) {
            return fileUrlOrPath.substring(StandardFileSystems.FILE_PROTOCOL_PREFIX.length());
        }
        return fileUrlOrPath;
    }

    public static String ensureFileUrl(String fileUrlOrPath) {
        if (fileUrlOrPath != null && !fileUrlOrPath.startsWith(StandardFileSystems.FILE_PROTOCOL_PREFIX)) {
            return StandardFileSystems.FILE_PROTOCOL_PREFIX + fileUrlOrPath;
        }
        return fileUrlOrPath;
    }

    @Nullable
    public static VirtualFile getOriginalFile(VirtualFile virtualFile) {
        if (virtualFile instanceof LightVirtualFile) {
            LightVirtualFile lightVirtualFile = (LightVirtualFile) virtualFile;
            return lightVirtualFile.getOriginalFile();
        }
        return null;
    }


}
