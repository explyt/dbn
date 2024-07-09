package com.dbn.code.sql.style.options;

import com.dbn.language.sql.SQLLanguage;
import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.CodeStyleConfigurable;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SQLCodeStyleSettingsProvider extends CodeStyleSettingsProvider {

    @Override
    public CustomCodeStyleSettings createCustomSettings(CodeStyleSettings codeStyleSettings) {
        return new SQLCodeStyleSettingsWrapper(codeStyleSettings);
    }

    @NotNull
    public CodeStyleConfigurable createConfigurable(@NotNull CodeStyleSettings settings, @NotNull CodeStyleSettings modelSettings) {
        SQLCodeStyleSettingsWrapper settingsProvider = settings.getCustomSettings(SQLCodeStyleSettingsWrapper.class);
        return settingsProvider.getSettings();
    }

    @Override
    public String getConfigurableDisplayName() {
        return "SQL (DBN)";
    }

    @Nullable
    @Override
    public Language getLanguage() {
        return SQLLanguage.INSTANCE;
    }
}
