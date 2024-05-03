package com.dbn.common.ui.util;

import lombok.experimental.UtilityClass;

import java.awt.*;
import java.util.List;

@UtilityClass
public class ComponentAligner {


    public static void alignFormComponents(Container container) {
        int[] metrics = null;
        List<? extends Form> forms = container.getAlignableForms();
        for (Form form : forms) {
            if (metrics == null) metrics = new int[form.getAlignableComponents().length];
            readMetrics(form, metrics);
        }

        for (Form form : forms) {
            adjustMetrics(form, metrics);
        }
    }

    private static void readMetrics(Form form, int[] metrics) {
        Component[] components = form.getAlignableComponents();
        for (int i = 0; i < components.length; i++) {
            int width = (int) components[i].getPreferredSize().getWidth();
            metrics[i] = Math.max(metrics[i], width);
        }
    }

    private static void adjustMetrics(Form form, int[] metrics) {
        Component[] components = form.getAlignableComponents();
        for (int i = 0; i < components.length; i++) {
            Dimension dimension = new Dimension(metrics[i], components[i].getHeight());
            components[i].setPreferredSize(dimension);
        }
    }

    public interface Form {
        Component[] getAlignableComponents();
    }

    public interface Container {
        List<? extends Form> getAlignableForms();
    }
}
