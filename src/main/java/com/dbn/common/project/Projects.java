package com.dbn.common.project;

import com.dbn.common.dispose.Disposer;
import com.dbn.common.event.ApplicationEvents;
import com.dbn.common.routine.Consumer;
import com.dbn.common.thread.Dispatch;
import com.dbn.common.util.Unsafe;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.wm.impl.welcomeScreen.WelcomeFrame;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import static com.dbn.common.dispose.Failsafe.guarded;
import static com.dbn.common.util.Conditional.when;

@UtilityClass
public final class Projects {

    public static final Project[] EMPTY_PROJECT_ARRAY = new Project[0];

    public static void closeProject(@NotNull Project project) {
        Dispatch.run(() -> {
            ProjectManager.getInstance().closeProject(project);
            WelcomeFrame.showIfNoProjectOpened();
        });
    }

    public static void projectOpened(Consumer<Project> consumer) {
        ApplicationEvents.subscribe(null, ProjectManager.TOPIC,
                new ProjectManagerListener() {
                    @Override
                    public void projectOpened(@NotNull Project project) {
                        guarded(() -> consumer.accept(project));
                    }
                });
    }

    public static void projectClosing(Consumer<Project> consumer) {
        ApplicationEvents.subscribe(null, ProjectManager.TOPIC,
                new ProjectManagerListener() {
                    @Override
                    public void projectClosing(@NotNull Project project) {
                        guarded(() -> consumer.accept(project));
                    }
                });

    }

    public static void projectClosed(Consumer<Project> runnable) {
        ApplicationEvents.subscribe(null, ProjectManager.TOPIC,
                new ProjectManagerListener() {
                    @Override
                    public void projectClosed(@NotNull Project project) {
                        guarded(() -> runnable.accept(project));
                    }
                });

    }

    public static @NotNull Project[] getOpenProjects() {
        return Unsafe.silent(EMPTY_PROJECT_ARRAY, () -> ProjectManager.getInstance().getOpenProjects());
    }

    public static Project getDefaultProject() {
        return ProjectManager.getInstance().getDefaultProject();
    }


    public static Disposable closeAwareDisposable(Project project) {
        // void disposable - nothing to dispose, used in the disposal chains as parent-disposable
        Disposable disposable = () -> {};
        projectClosed(p -> when(p == project, () -> Disposer.dispose(disposable)));
        return disposable;
    }
}
