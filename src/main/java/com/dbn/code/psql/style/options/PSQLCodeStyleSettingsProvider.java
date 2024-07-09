package com.dbn.code.psql.style.options;

import com.dbn.language.psql.PSQLLanguage;
import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.CodeStyleConfigurable;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PSQLCodeStyleSettingsProvider extends CodeStyleSettingsProvider {

    @Override
    public CustomCodeStyleSettings createCustomSettings(CodeStyleSettings codeStyleSettings) {
        return new PSQLCodeStyleSettingsWrapper(codeStyleSettings);
    }

    @NotNull
    public CodeStyleConfigurable createConfigurable(CodeStyleSettings settings, @NotNull CodeStyleSettings modelSettings) {
        PSQLCodeStyleSettingsWrapper settingsProvider = settings.getCustomSettings(PSQLCodeStyleSettingsWrapper.class);
        return settingsProvider.getSettings();
    }

    @Override
    public String getConfigurableDisplayName() {
        return "PL/SQL (DBN)";
    }

    @Nullable
    @Override
    public Language getLanguage() {
        return PSQLLanguage.INSTANCE;
    }
}