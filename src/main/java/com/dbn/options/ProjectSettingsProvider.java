package com.dbn.options;

import com.dbn.common.compatibility.Workaround;
import com.dbn.common.project.ProjectRef;
import com.dbn.common.util.Unsafe;
import com.intellij.openapi.extensions.ExtensionPoint;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurableEP;
import com.intellij.openapi.options.ConfigurableProvider;
import com.intellij.openapi.options.ex.ConfigurableWrapper;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dbn.common.Reflection.invokeMethod;

public class ProjectSettingsProvider extends ConfigurableProvider{
    public static final String PROJECT_CONFIGURABLE = "com.intellij.projectConfigurable";
    private final ProjectRef project;

    public ProjectSettingsProvider(Project project) {
        this.project = ProjectRef.of(project);
    }


    /**
     * ConfigurableEP is logging wrapped ProcessCancelledException when provider is
     * initialized in background and cancelled (e.g. on all-actions invocation)
     * (initialize the provider upfront)
     */
    @Workaround // https://youtrack.jetbrains.com/issue/IDEA-313711
    @Deprecated // TODO decommission
    public static void init(Project project) {
        Unsafe.silent(() -> {
            Object extensionArea = invokeMethod(project, "getExtensionArea");
            ExtensionPoint<ConfigurableEP<?>> projectConfigEP = invokeMethod(extensionArea, "getExtensionPoint", PROJECT_CONFIGURABLE);
            ConfigurableEP<?>[] extensions = projectConfigEP.getExtensions();
            for (ConfigurableEP<?> extension : extensions) {
                if (ProjectSettingsProvider.class.getName().equals(extension.providerClass)) {
                    ConfigurableWrapper.wrapConfigurable(extension, true);
                    break;
                }
            }
        });
    }

    @Nullable
    @Override
    public Configurable createConfigurable() {
        Project project = getProject();
        ProjectSettings projectSettings = ProjectSettings.get(project);
        return projectSettings.clone();
    }

    @NotNull
    public Project getProject() {
        return project.ensure();
    }
}
