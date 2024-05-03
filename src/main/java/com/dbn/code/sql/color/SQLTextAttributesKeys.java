package com.dbn.code.sql.color;

import com.intellij.codeInsight.template.impl.TemplateColors;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;

public interface SQLTextAttributesKeys {
    TextAttributesKey LINE_COMMENT       = TextAttributesKey.createTextAttributesKey("DBNavigator.TextAttributes.SQL.LineComment",       DefaultLanguageHighlighterColors.LINE_COMMENT);
    TextAttributesKey BLOCK_COMMENT      = TextAttributesKey.createTextAttributesKey("DBNavigator.TextAttributes.SQL.BlockComment",      DefaultLanguageHighlighterColors.BLOCK_COMMENT);
    TextAttributesKey STRING             = TextAttributesKey.createTextAttributesKey("DBNavigator.TextAttributes.SQL.String",            DefaultLanguageHighlighterColors.STRING);
    TextAttributesKey NUMBER             = TextAttributesKey.createTextAttributesKey("DBNavigator.TextAttributes.SQL.Number",            DefaultLanguageHighlighterColors.NUMBER);
    TextAttributesKey DATA_TYPE          = TextAttributesKey.createTextAttributesKey("DBNavigator.TextAttributes.SQL.DataType",          DefaultLanguageHighlighterColors.CONSTANT);
    TextAttributesKey ALIAS              = TextAttributesKey.createTextAttributesKey("DBNavigator.TextAttributes.SQL.Alias",             DefaultLanguageHighlighterColors.METADATA);
    TextAttributesKey IDENTIFIER         = TextAttributesKey.createTextAttributesKey("DBNavigator.TextAttributes.SQL.Identifier",        DefaultLanguageHighlighterColors.IDENTIFIER);
    TextAttributesKey QUOTED_IDENTIFIER  = TextAttributesKey.createTextAttributesKey("DBNavigator.TextAttributes.SQL.QuotedIdentifier",  DefaultLanguageHighlighterColors.IDENTIFIER);
    TextAttributesKey KEYWORD            = TextAttributesKey.createTextAttributesKey("DBNavigator.TextAttributes.SQL.Keyword",           DefaultLanguageHighlighterColors.KEYWORD);
    TextAttributesKey FUNCTION           = TextAttributesKey.createTextAttributesKey("DBNavigator.TextAttributes.SQL.Function",          DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL);
    TextAttributesKey PARAMETER          = TextAttributesKey.createTextAttributesKey("DBNavigator.TextAttributes.SQL.Parameter",         TemplateColors.TEMPLATE_VARIABLE_ATTRIBUTES);
    TextAttributesKey OPERATOR           = TextAttributesKey.createTextAttributesKey("DBNavigator.TextAttributes.SQL.Operator",          DefaultLanguageHighlighterColors.OPERATION_SIGN);
    TextAttributesKey PARENTHESIS        = TextAttributesKey.createTextAttributesKey("DBNavigator.TextAttributes.SQL.Parenthesis",       DefaultLanguageHighlighterColors.PARENTHESES);
    TextAttributesKey BRACKET            = TextAttributesKey.createTextAttributesKey("DBNavigator.TextAttributes.SQL.Brackets",          DefaultLanguageHighlighterColors.BRACKETS);
    TextAttributesKey UNKNOWN_IDENTIFIER = TextAttributesKey.createTextAttributesKey("DBNavigator.TextAttributes.SQL.UnknownIdentifier", CodeInsightColors.WARNINGS_ATTRIBUTES);
    TextAttributesKey CHAMELEON          = TextAttributesKey.createTextAttributesKey("DBNavigator.TextAttributes.SQL.Chameleon",         (TextAttributesKey) null);
    TextAttributesKey VARIABLE           = TextAttributesKey.createTextAttributesKey("DBNavigator.TextAttributes.SQL.Variable",          TemplateColors.TEMPLATE_VARIABLE_ATTRIBUTES);
    TextAttributesKey BAD_CHARACTER      = HighlighterColors.BAD_CHARACTER;
}