package com.dbn.language.sql.dialect.oracle;

import com.dbn.language.common.DBLanguageDialectIdentifier;
import com.dbn.language.common.TokenTypeBundle;
import com.dbn.language.common.lexer.DBLanguageCompoundLexerBase;
import com.dbn.language.sql.dialect.oracle.OraclePLSQLBlockMonitor;
import com.intellij.psi.tree.IElementType;

import static com.dbn.language.sql.dialect.oracle.OraclePLSQLBlockMonitor.Marker;

%%

%class OracleSQLParserFlexLexer
%extends DBLanguageCompoundLexerBase
%final
%unicode
%ignorecase
%function advance
%type IElementType
%eof{ return;
%eof}

%{
    private final OraclePLSQLBlockMonitor pbm = new OraclePLSQLBlockMonitor(this, YYINITIAL, PSQL_BLOCK);

    public OracleSQLParserFlexLexer(TokenTypeBundle tt) {
        super(tt, DBLanguageDialectIdentifier.ORACLE_PLSQL);
    }

    public void setTokenStart(int tokenStart) {
        zzStartRead = tokenStart;
    }

    public int getCurrentPosition() {
        return zzCurrentPos;
    }

    public String getCurrentToken() {
        return ((String) zzBuffer).substring(zzStartRead, zzMarkedPos);
    }
%}


%include ../../../common/lexer/shared_elements.flext
%include ../../../common/lexer/shared_elements_oracle.flext

NON_PSQL_BLOCK_ENTER = ("grant"|"revoke"){ws}"create"
NON_PSQL_BLOCK_EXIT = "to"|"from"|";"

PSQL_STUB_OR_REPLACE = ({ws}"or"{ws}"replace")?
PSQL_STUB_EDITIONABLE = ({ws}("editionable"|"editioning"|'noneditionable'))?
PSQL_STUB_FORCE = ({ws}("no"{ws})?"force")?
PSQL_STUB_PUBLIC = ({ws}"public")?
PSQL_STUB_PROGRAM = {ws}("package"|"trigger"|"function"|"procedure"|"type")
PSQL_STUB_IDENTIFIER = ({ws}({IDENTIFIER}|{QUOTED_IDENTIFIER}))*

PSQL_BLOCK_START_CREATE = "create"{PSQL_STUB_OR_REPLACE}{PSQL_STUB_FORCE}{PSQL_STUB_EDITIONABLE}{PSQL_STUB_PUBLIC}{PSQL_STUB_PROGRAM}
PSQL_BLOCK_START_DECLARE = "declare"
PSQL_BLOCK_START_BEGIN = "begin"
//PSQL_BLOCK_END_IGNORE = "end"{ws}("if"|"loop"|"case"){PSQL_STUB_IDENTIFIER}{wso}";"
PSQL_BLOCK_END_IGNORE = "end"{ws}("if"|"loop"){PSQL_STUB_IDENTIFIER}{wso}";"
PSQL_BLOCK_END = "end"{PSQL_STUB_IDENTIFIER}({wso}";"({wso}"/")?)?

CT_SIZE_CLAUSE = {INTEGER}{wso}("k"|"m"|"g"|"t"|"p"|"e"){ws}
SELECT_AI_START = "select"{ws}"ai"

VARIABLE = ":"({IDENTIFIER}|{INTEGER})
SQLP_VARIABLE = "&""&"?({IDENTIFIER}|{INTEGER})
VARIABLE_IDENTIFIER={IDENTIFIER}"&""&"?({IDENTIFIER}|{INTEGER})|"<"{IDENTIFIER}({ws}{IDENTIFIER})*">"

%state PSQL_BLOCK
%state NON_PSQL_BLOCK
%state SELECT_AI
%%

<PSQL_BLOCK> {
    {BLOCK_COMMENT}                 {}
    {LINE_COMMENT}                  {}

    {PSQL_BLOCK_START_CREATE}       {if (pbm.isBlockStarted()) { pbm.pushBack(); pbm.end(true); return getChameleon(); }}
    {PSQL_BLOCK_END_IGNORE}         { pbm.ignore();}
    {PSQL_BLOCK_END}                { if (pbm.end(false)) return getChameleon();}

    "begin"                         { pbm.mark(Marker.BEGIN); }
    "type"{ws}{IDENTIFIER}          { pbm.mark(Marker.PROGRAM); }
    "function"{ws}{IDENTIFIER}      { pbm.mark(Marker.PROGRAM); }
    "procedure"{ws}{IDENTIFIER}     { pbm.mark(Marker.PROGRAM); }
    "trigger"{ws}{IDENTIFIER}       { pbm.mark(Marker.PROGRAM); }
    "case"                          { pbm.mark(Marker.CASE); }

    {IDENTIFIER}                    {}
    {INTEGER}                       {}
    {NUMBER}                        {}
    {STRING}                        {}
    {WHITE_SPACE}                   {}
    .                               {}
    <<EOF>>                         { pbm.end(true); return getChameleon(); }
}

<NON_PSQL_BLOCK> {
    {NON_PSQL_BLOCK_EXIT}          { yybegin(YYINITIAL); pbm.pushBack(); }
}


<YYINITIAL> {
    {NON_PSQL_BLOCK_ENTER}         { yybegin(NON_PSQL_BLOCK); pbm.pushBack(); }

    {PSQL_BLOCK_START_CREATE}      { pbm.start(Marker.CREATE); }
    {PSQL_BLOCK_START_DECLARE}     { pbm.start(Marker.DECLARE); }
    {PSQL_BLOCK_START_BEGIN}       { pbm.start(Marker.BEGIN); }

    {SELECT_AI_START}              { yybegin(SELECT_AI); yypushback(yylength()); }
}

<SELECT_AI> {
    "select"           { return tt.getTokenType("KW_SELECT"); }
    "ai"               { return tt.getTokenType("KW_AI"); }
    "showprompt"       { return tt.getTokenType("KW_SHOWPROMPT"); }
    "showsql"          { return tt.getTokenType("KW_SHOWSQL"); }
    "explainsql"       { return tt.getTokenType("KW_EXPLAINSQL"); }
    "executesql"       { return tt.getTokenType("KW_EXECUTESQL"); }
    "narrate"          { return tt.getTokenType("KW_NARRATE"); }
    "chat"             { return tt.getTokenType("KW_CHAT"); }
    {STRING}           { yybegin(YYINITIAL); return stt.getString(); }   // string is allowed to have eols
    {eol}              { yybegin(YYINITIAL); return stt.getWhiteSpace();} // end of line -> exit the SELECT_AI block
    ";"                { yybegin(YYINITIAL); return stt.getChrSemicolon();}
    "/"                { yybegin(YYINITIAL); return stt.getChrSlash();}
    [^\r\n\t\f ;/]+     { return stt.getIdentifier();}
    {wsc}+             { return stt.getWhiteSpace(); }
}

<YYINITIAL, NON_PSQL_BLOCK> {

{BLOCK_COMMENT}        { return stt.getBlockComment(); }
{LINE_COMMENT}         { return stt.getLineComment(); }

{VARIABLE}             { return stt.getVariable(); }
{VARIABLE_IDENTIFIER}  { return stt.getIdentifier(); }
{SQLP_VARIABLE}        { return stt.getVariable(); }

"("{wso}"+"{wso}")"  {return tt.getTokenType("CT_OUTER_JOIN");}


"="{wso}"=" {return tt.getOperatorTokenType(0);}
"|"{wso}"|" {return tt.getOperatorTokenType(1);}
"<"{wso}"=" {return tt.getOperatorTokenType(2);}
">"{wso}"=" {return tt.getOperatorTokenType(3);}
"<"{wso}">" {return tt.getOperatorTokenType(4);}
"!"{wso}"=" {return tt.getOperatorTokenType(5);}
":"{wso}"=" {return tt.getOperatorTokenType(6);}
"="{wso}">" {return tt.getOperatorTokenType(7);}
".."        {return tt.getOperatorTokenType(8);}
"::"        {return tt.getOperatorTokenType(9);}


"@" {return tt.getCharacterTokenType(0);}
":" {return tt.getCharacterTokenType(1);}
"," {return tt.getCharacterTokenType(2);}
"." {return tt.getCharacterTokenType(3);}
"=" {return tt.getCharacterTokenType(4);}
"!" {return tt.getCharacterTokenType(5);}
">" {return tt.getCharacterTokenType(6);}
"#" {return tt.getCharacterTokenType(7);}
"[" {return tt.getCharacterTokenType(8);}
"{" {return tt.getCharacterTokenType(9);}
"(" {return tt.getCharacterTokenType(10);}
"<" {return tt.getCharacterTokenType(11);}
"-" {return tt.getCharacterTokenType(12);}
"%" {return tt.getCharacterTokenType(13);}
"+" {return tt.getCharacterTokenType(14);}
"]" {return tt.getCharacterTokenType(15);}
"}" {return tt.getCharacterTokenType(16);}
")" {return tt.getCharacterTokenType(17);}
";" {return tt.getCharacterTokenType(18);}
"/" {return tt.getCharacterTokenType(19);}
"*" {return tt.getCharacterTokenType(20);}
"|" {return tt.getCharacterTokenType(21);}



"varchar2" {return tt.getDataTypeTokenType(0);}
"bfile" {return tt.getDataTypeTokenType(1);}
"binary_double" {return tt.getDataTypeTokenType(2);}
"binary_float" {return tt.getDataTypeTokenType(3);}
"blob" {return tt.getDataTypeTokenType(4);}
"boolean" {return tt.getDataTypeTokenType(5);}
"byte" {return tt.getDataTypeTokenType(6);}
"char" {return tt.getDataTypeTokenType(7);}
"character" {return tt.getDataTypeTokenType(8);}
"character"{ws}"varying" {return tt.getDataTypeTokenType(9);}
"clob" {return tt.getDataTypeTokenType(10);}
"date" {return tt.getDataTypeTokenType(11);}
"decimal" {return tt.getDataTypeTokenType(12);}
"double"{ws}"precision" {return tt.getDataTypeTokenType(13);}
"float" {return tt.getDataTypeTokenType(14);}
"int" {return tt.getDataTypeTokenType(15);}
"integer" {return tt.getDataTypeTokenType(16);}
"interval" {return tt.getDataTypeTokenType(17);}
"long" {return tt.getDataTypeTokenType(18);}
"long"{ws}"raw" {return tt.getDataTypeTokenType(19);}
"long"{ws}"varchar" {return tt.getDataTypeTokenType(20);}
"national"{ws}"char" {return tt.getDataTypeTokenType(21);}
"national"{ws}"char"{ws}"varying" {return tt.getDataTypeTokenType(22);}
"national"{ws}"character" {return tt.getDataTypeTokenType(23);}
"national"{ws}"character"{ws}"varying" {return tt.getDataTypeTokenType(24);}
"nchar" {return tt.getDataTypeTokenType(25);}
"nchar"{ws}"varying" {return tt.getDataTypeTokenType(26);}
"nclob" {return tt.getDataTypeTokenType(27);}
"number" {return tt.getDataTypeTokenType(28);}
"numeric" {return tt.getDataTypeTokenType(29);}
"nvarchar2" {return tt.getDataTypeTokenType(30);}
"raw" {return tt.getDataTypeTokenType(31);}
"real" {return tt.getDataTypeTokenType(32);}
"rowid" {return tt.getDataTypeTokenType(33);}
"smallint" {return tt.getDataTypeTokenType(34);}
"timestamp" {return tt.getDataTypeTokenType(35);}
"urowid" {return tt.getDataTypeTokenType(36);}
"varchar" {return tt.getDataTypeTokenType(37);}
"with"{ws}"local"{ws}"time"{ws}"zone" {return tt.getDataTypeTokenType(38);}
"with"{ws}"time"{ws}"zone" {return tt.getDataTypeTokenType(39);}



"a set" {return tt.getKeywordTokenType(0);}
"abort" {return tt.getKeywordTokenType(1);}
"absent" {return tt.getKeywordTokenType(2);}
"access" {return tt.getKeywordTokenType(3);}
"accessed" {return tt.getKeywordTokenType(4);}
"account" {return tt.getKeywordTokenType(5);}
"activate" {return tt.getKeywordTokenType(6);}
"active" {return tt.getKeywordTokenType(7);}
"add" {return tt.getKeywordTokenType(8);}
"admin" {return tt.getKeywordTokenType(9);}
"administer" {return tt.getKeywordTokenType(10);}
"advise" {return tt.getKeywordTokenType(11);}
"advisor" {return tt.getKeywordTokenType(12);}
"after" {return tt.getKeywordTokenType(13);}
"agent" {return tt.getKeywordTokenType(14);}
"ai" {return tt.getKeywordTokenType(15);}
"alias" {return tt.getKeywordTokenType(16);}
"all" {return tt.getKeywordTokenType(17);}
"allocate" {return tt.getKeywordTokenType(18);}
"allow" {return tt.getKeywordTokenType(19);}
"alter" {return tt.getKeywordTokenType(20);}
"always" {return tt.getKeywordTokenType(21);}
"analyze" {return tt.getKeywordTokenType(22);}
"ancillary" {return tt.getKeywordTokenType(23);}
"and" {return tt.getKeywordTokenType(24);}
"any" {return tt.getKeywordTokenType(25);}
"apply" {return tt.getKeywordTokenType(26);}
"archive" {return tt.getKeywordTokenType(27);}
"archivelog" {return tt.getKeywordTokenType(28);}
"array" {return tt.getKeywordTokenType(29);}
"as" {return tt.getKeywordTokenType(30);}
"asc" {return tt.getKeywordTokenType(31);}
"asynchronous" {return tt.getKeywordTokenType(32);}
"assembly" {return tt.getKeywordTokenType(33);}
"at" {return tt.getKeywordTokenType(34);}
"attribute" {return tt.getKeywordTokenType(35);}
"attributes" {return tt.getKeywordTokenType(36);}
"audit" {return tt.getKeywordTokenType(37);}
"authid" {return tt.getKeywordTokenType(38);}
"authentication" {return tt.getKeywordTokenType(39);}
"auto" {return tt.getKeywordTokenType(40);}
"autoextend" {return tt.getKeywordTokenType(41);}
"automatic" {return tt.getKeywordTokenType(42);}
"availability" {return tt.getKeywordTokenType(43);}
"backup" {return tt.getKeywordTokenType(44);}
"become" {return tt.getKeywordTokenType(45);}
"before" {return tt.getKeywordTokenType(46);}
"begin" {return tt.getKeywordTokenType(47);}
"beginning" {return tt.getKeywordTokenType(48);}
"bequeath" {return tt.getKeywordTokenType(49);}
"between" {return tt.getKeywordTokenType(50);}
"bigfile" {return tt.getKeywordTokenType(51);}
"binding" {return tt.getKeywordTokenType(52);}
"bitmap" {return tt.getKeywordTokenType(53);}
"block" {return tt.getKeywordTokenType(54);}
"blockchain" {return tt.getKeywordTokenType(55);}
"body" {return tt.getKeywordTokenType(56);}
"both" {return tt.getKeywordTokenType(57);}
"buffer_cache" {return tt.getKeywordTokenType(58);}
"buffer_pool" {return tt.getKeywordTokenType(59);}
"build" {return tt.getKeywordTokenType(60);}
"by" {return tt.getKeywordTokenType(61);}
"cache" {return tt.getKeywordTokenType(62);}
"cancel" {return tt.getKeywordTokenType(63);}
"canonical" {return tt.getKeywordTokenType(64);}
"capacity" {return tt.getKeywordTokenType(65);}
"cascade" {return tt.getKeywordTokenType(66);}
"case" {return tt.getKeywordTokenType(67);}
"category" {return tt.getKeywordTokenType(68);}
"change" {return tt.getKeywordTokenType(69);}
"char_cs" {return tt.getKeywordTokenType(70);}
"chat" {return tt.getKeywordTokenType(71);}
"check" {return tt.getKeywordTokenType(72);}
"checkpoint" {return tt.getKeywordTokenType(73);}
"child" {return tt.getKeywordTokenType(74);}
"chisq_df" {return tt.getKeywordTokenType(75);}
"chisq_obs" {return tt.getKeywordTokenType(76);}
"chisq_sig" {return tt.getKeywordTokenType(77);}
"chunk" {return tt.getKeywordTokenType(78);}
"class" {return tt.getKeywordTokenType(79);}
"clear" {return tt.getKeywordTokenType(80);}
"clone" {return tt.getKeywordTokenType(81);}
"close" {return tt.getKeywordTokenType(82);}
"cluster" {return tt.getKeywordTokenType(83);}
"coalesce" {return tt.getKeywordTokenType(84);}
"coarse" {return tt.getKeywordTokenType(85);}
"coefficient" {return tt.getKeywordTokenType(86);}
"cohens_k" {return tt.getKeywordTokenType(87);}
"collation" {return tt.getKeywordTokenType(88);}
"column" {return tt.getKeywordTokenType(89);}
"column_value" {return tt.getKeywordTokenType(90);}
"columns" {return tt.getKeywordTokenType(91);}
"comment" {return tt.getKeywordTokenType(92);}
"commit" {return tt.getKeywordTokenType(93);}
"committed" {return tt.getKeywordTokenType(94);}
"compact" {return tt.getKeywordTokenType(95);}
"compatibility" {return tt.getKeywordTokenType(96);}
"compile" {return tt.getKeywordTokenType(97);}
"complete" {return tt.getKeywordTokenType(98);}
"compress" {return tt.getKeywordTokenType(99);}
"computation" {return tt.getKeywordTokenType(100);}
"compute" {return tt.getKeywordTokenType(101);}
"conditional" {return tt.getKeywordTokenType(102);}
"connect" {return tt.getKeywordTokenType(103);}
"consider" {return tt.getKeywordTokenType(104);}
"consistent" {return tt.getKeywordTokenType(105);}
"constraint" {return tt.getKeywordTokenType(106);}
"constraints" {return tt.getKeywordTokenType(107);}
"cont_coefficient" {return tt.getKeywordTokenType(108);}
"container" {return tt.getKeywordTokenType(109);}
"container_map" {return tt.getKeywordTokenType(110);}
"containers_default" {return tt.getKeywordTokenType(111);}
"content" {return tt.getKeywordTokenType(112);}
"contents" {return tt.getKeywordTokenType(113);}
"context" {return tt.getKeywordTokenType(114);}
"continue" {return tt.getKeywordTokenType(115);}
"controlfile" {return tt.getKeywordTokenType(116);}
"conversion" {return tt.getKeywordTokenType(117);}
"corruption" {return tt.getKeywordTokenType(118);}
"cost" {return tt.getKeywordTokenType(119);}
"cramers_v" {return tt.getKeywordTokenType(120);}
"create" {return tt.getKeywordTokenType(121);}
"creation" {return tt.getKeywordTokenType(122);}
"credential" {return tt.getKeywordTokenType(123);}
"critical" {return tt.getKeywordTokenType(124);}
"cross" {return tt.getKeywordTokenType(125);}
"cube" {return tt.getKeywordTokenType(126);}
"current" {return tt.getKeywordTokenType(127);}
"current_user" {return tt.getKeywordTokenType(128);}
"currval" {return tt.getKeywordTokenType(129);}
"cursor" {return tt.getKeywordTokenType(130);}
"cycle" {return tt.getKeywordTokenType(131);}
"data" {return tt.getKeywordTokenType(132);}
"database" {return tt.getKeywordTokenType(133);}
"datafile" {return tt.getKeywordTokenType(134);}
"datafiles" {return tt.getKeywordTokenType(135);}
"day" {return tt.getKeywordTokenType(136);}
"days" {return tt.getKeywordTokenType(137);}
"ddl" {return tt.getKeywordTokenType(138);}
"deallocate" {return tt.getKeywordTokenType(139);}
"debug" {return tt.getKeywordTokenType(140);}
"decrement" {return tt.getKeywordTokenType(141);}
"default" {return tt.getKeywordTokenType(142);}
"defaults" {return tt.getKeywordTokenType(143);}
"deferrable" {return tt.getKeywordTokenType(144);}
"deferred" {return tt.getKeywordTokenType(145);}
"definer" {return tt.getKeywordTokenType(146);}
"delay" {return tt.getKeywordTokenType(147);}
"delegate" {return tt.getKeywordTokenType(148);}
"delete" {return tt.getKeywordTokenType(149);}
"demand" {return tt.getKeywordTokenType(150);}
"dense_rank" {return tt.getKeywordTokenType(151);}
"dequeue" {return tt.getKeywordTokenType(152);}
"desc" {return tt.getKeywordTokenType(153);}
"determines" {return tt.getKeywordTokenType(154);}
"df" {return tt.getKeywordTokenType(155);}
"df_between" {return tt.getKeywordTokenType(156);}
"df_den" {return tt.getKeywordTokenType(157);}
"df_num" {return tt.getKeywordTokenType(158);}
"df_within" {return tt.getKeywordTokenType(159);}
"dictionary" {return tt.getKeywordTokenType(160);}
"digest" {return tt.getKeywordTokenType(161);}
"dimension" {return tt.getKeywordTokenType(162);}
"directory" {return tt.getKeywordTokenType(163);}
"disable" {return tt.getKeywordTokenType(164);}
"disconnect" {return tt.getKeywordTokenType(165);}
"disk" {return tt.getKeywordTokenType(166);}
"diskgroup" {return tt.getKeywordTokenType(167);}
"disks" {return tt.getKeywordTokenType(168);}
"dismount" {return tt.getKeywordTokenType(169);}
"distinct" {return tt.getKeywordTokenType(170);}
"distribute" {return tt.getKeywordTokenType(171);}
"distributed" {return tt.getKeywordTokenType(172);}
"dml" {return tt.getKeywordTokenType(173);}
"document" {return tt.getKeywordTokenType(174);}
"downgrade" {return tt.getKeywordTokenType(175);}
"drop" {return tt.getKeywordTokenType(176);}
"dump" {return tt.getKeywordTokenType(177);}
"duplicate" {return tt.getKeywordTokenType(178);}
"duplicated" {return tt.getKeywordTokenType(179);}
"edition" {return tt.getKeywordTokenType(180);}
"editions" {return tt.getKeywordTokenType(181);}
"editionable" {return tt.getKeywordTokenType(182);}
"editioning" {return tt.getKeywordTokenType(183);}
"element" {return tt.getKeywordTokenType(184);}
"else" {return tt.getKeywordTokenType(185);}
"empty" {return tt.getKeywordTokenType(186);}
"enable" {return tt.getKeywordTokenType(187);}
"encoding" {return tt.getKeywordTokenType(188);}
"encrypt" {return tt.getKeywordTokenType(189);}
"end" {return tt.getKeywordTokenType(190);}
"enforced" {return tt.getKeywordTokenType(191);}
"entityescaping" {return tt.getKeywordTokenType(192);}
"entry" {return tt.getKeywordTokenType(193);}
"equals_path" {return tt.getKeywordTokenType(194);}
"error" {return tt.getKeywordTokenType(195);}
"errors" {return tt.getKeywordTokenType(196);}
"escape" {return tt.getKeywordTokenType(197);}
"evalname" {return tt.getKeywordTokenType(198);}
"evaluate" {return tt.getKeywordTokenType(199);}
"evaluation" {return tt.getKeywordTokenType(200);}
"exact_prob" {return tt.getKeywordTokenType(201);}
"except" {return tt.getKeywordTokenType(202);}
"exceptions" {return tt.getKeywordTokenType(203);}
"exchange" {return tt.getKeywordTokenType(204);}
"exclude" {return tt.getKeywordTokenType(205);}
"excluding" {return tt.getKeywordTokenType(206);}
"exclusive" {return tt.getKeywordTokenType(207);}
"execute" {return tt.getKeywordTokenType(208);}
"executesql" {return tt.getKeywordTokenType(209);}
"exempt" {return tt.getKeywordTokenType(210);}
"exists" {return tt.getKeywordTokenType(211);}
"expire" {return tt.getKeywordTokenType(212);}
"explain" {return tt.getKeywordTokenType(213);}
"explainsql" {return tt.getKeywordTokenType(214);}
"export" {return tt.getKeywordTokenType(215);}
"extended" {return tt.getKeywordTokenType(216);}
"extends" {return tt.getKeywordTokenType(217);}
"extensions" {return tt.getKeywordTokenType(218);}
"extent" {return tt.getKeywordTokenType(219);}
"external" {return tt.getKeywordTokenType(220);}
"externally" {return tt.getKeywordTokenType(221);}
"f_ratio" {return tt.getKeywordTokenType(222);}
"failed" {return tt.getKeywordTokenType(223);}
"failgroup" {return tt.getKeywordTokenType(224);}
"fast" {return tt.getKeywordTokenType(225);}
"fetch" {return tt.getKeywordTokenType(226);}
"file" {return tt.getKeywordTokenType(227);}
"filesystem_like_logging" {return tt.getKeywordTokenType(228);}
"fine" {return tt.getKeywordTokenType(229);}
"finish" {return tt.getKeywordTokenType(230);}
"first" {return tt.getKeywordTokenType(231);}
"flashback" {return tt.getKeywordTokenType(232);}
"flush" {return tt.getKeywordTokenType(233);}
"folder" {return tt.getKeywordTokenType(234);}
"following" {return tt.getKeywordTokenType(235);}
"for" {return tt.getKeywordTokenType(236);}
"force" {return tt.getKeywordTokenType(237);}
"foreign" {return tt.getKeywordTokenType(238);}
"format" {return tt.getKeywordTokenType(239);}
"freelist" {return tt.getKeywordTokenType(240);}
"freelists" {return tt.getKeywordTokenType(241);}
"freepools" {return tt.getKeywordTokenType(242);}
"fresh" {return tt.getKeywordTokenType(243);}
"from" {return tt.getKeywordTokenType(244);}
"full" {return tt.getKeywordTokenType(245);}
"function" {return tt.getKeywordTokenType(246);}
"global" {return tt.getKeywordTokenType(247);}
"global_name" {return tt.getKeywordTokenType(248);}
"globally" {return tt.getKeywordTokenType(249);}
"grant" {return tt.getKeywordTokenType(250);}
"group" {return tt.getKeywordTokenType(251);}
"groups" {return tt.getKeywordTokenType(252);}
"guard" {return tt.getKeywordTokenType(253);}
"hash" {return tt.getKeywordTokenType(254);}
"having" {return tt.getKeywordTokenType(255);}
"heap" {return tt.getKeywordTokenType(256);}
"hide" {return tt.getKeywordTokenType(257);}
"hierarchy" {return tt.getKeywordTokenType(258);}
"high" {return tt.getKeywordTokenType(259);}
"history" {return tt.getKeywordTokenType(260);}
"hour" {return tt.getKeywordTokenType(261);}
"http" {return tt.getKeywordTokenType(262);}
"id" {return tt.getKeywordTokenType(263);}
"identified" {return tt.getKeywordTokenType(264);}
"identifier" {return tt.getKeywordTokenType(265);}
"idle" {return tt.getKeywordTokenType(266);}
"ignore" {return tt.getKeywordTokenType(267);}
"ilm" {return tt.getKeywordTokenType(268);}
"immediate" {return tt.getKeywordTokenType(269);}
"immutable" {return tt.getKeywordTokenType(270);}
"import" {return tt.getKeywordTokenType(271);}
"in" {return tt.getKeywordTokenType(272);}
"include" {return tt.getKeywordTokenType(273);}
"including" {return tt.getKeywordTokenType(274);}
"increment" {return tt.getKeywordTokenType(275);}
"indent" {return tt.getKeywordTokenType(276);}
"index" {return tt.getKeywordTokenType(277);}
"indexes" {return tt.getKeywordTokenType(278);}
"indexing" {return tt.getKeywordTokenType(279);}
"indextype" {return tt.getKeywordTokenType(280);}
"infinite" {return tt.getKeywordTokenType(281);}
"initial" {return tt.getKeywordTokenType(282);}
"initialized" {return tt.getKeywordTokenType(283);}
"initially" {return tt.getKeywordTokenType(284);}
"initrans" {return tt.getKeywordTokenType(285);}
"inmemory" {return tt.getKeywordTokenType(286);}
"inner" {return tt.getKeywordTokenType(287);}
"insert" {return tt.getKeywordTokenType(288);}
"instance" {return tt.getKeywordTokenType(289);}
"intermediate" {return tt.getKeywordTokenType(290);}
"intersect" {return tt.getKeywordTokenType(291);}
"into" {return tt.getKeywordTokenType(292);}
"invalidate" {return tt.getKeywordTokenType(293);}
"invisible" {return tt.getKeywordTokenType(294);}
"is" {return tt.getKeywordTokenType(295);}
"iterate" {return tt.getKeywordTokenType(296);}
"java" {return tt.getKeywordTokenType(297);}
"job" {return tt.getKeywordTokenType(298);}
"join" {return tt.getKeywordTokenType(299);}
"json" {return tt.getKeywordTokenType(300);}
"keep" {return tt.getKeywordTokenType(301);}
"key" {return tt.getKeywordTokenType(302);}
"keys" {return tt.getKeywordTokenType(303);}
"kill" {return tt.getKeywordTokenType(304);}
"last" {return tt.getKeywordTokenType(305);}
"leading" {return tt.getKeywordTokenType(306);}
"left" {return tt.getKeywordTokenType(307);}
"less" {return tt.getKeywordTokenType(308);}
"level" {return tt.getKeywordTokenType(309);}
"levels" {return tt.getKeywordTokenType(310);}
"library" {return tt.getKeywordTokenType(311);}
"like" {return tt.getKeywordTokenType(312);}
"like2" {return tt.getKeywordTokenType(313);}
"like4" {return tt.getKeywordTokenType(314);}
"likec" {return tt.getKeywordTokenType(315);}
"limit" {return tt.getKeywordTokenType(316);}
"link" {return tt.getKeywordTokenType(317);}
"lob" {return tt.getKeywordTokenType(318);}
"local" {return tt.getKeywordTokenType(319);}
"location" {return tt.getKeywordTokenType(320);}
"locator" {return tt.getKeywordTokenType(321);}
"lock" {return tt.getKeywordTokenType(322);}
"lockdown" {return tt.getKeywordTokenType(323);}
"locked" {return tt.getKeywordTokenType(324);}
"log" {return tt.getKeywordTokenType(325);}
"logfile" {return tt.getKeywordTokenType(326);}
"logging" {return tt.getKeywordTokenType(327);}
"logical" {return tt.getKeywordTokenType(328);}
"low" {return tt.getKeywordTokenType(329);}
"low_cost_tbs" {return tt.getKeywordTokenType(330);}
"main" {return tt.getKeywordTokenType(331);}
"manage" {return tt.getKeywordTokenType(332);}
"managed" {return tt.getKeywordTokenType(333);}
"manager" {return tt.getKeywordTokenType(334);}
"management" {return tt.getKeywordTokenType(335);}
"manual" {return tt.getKeywordTokenType(336);}
"mapping" {return tt.getKeywordTokenType(337);}
"master" {return tt.getKeywordTokenType(338);}
"matched" {return tt.getKeywordTokenType(339);}
"materialized" {return tt.getKeywordTokenType(340);}
"maxextents" {return tt.getKeywordTokenType(341);}
"maximize" {return tt.getKeywordTokenType(342);}
"maxsize" {return tt.getKeywordTokenType(343);}
"maxvalue" {return tt.getKeywordTokenType(344);}
"mean_squares_between" {return tt.getKeywordTokenType(345);}
"mean_squares_within" {return tt.getKeywordTokenType(346);}
"measure" {return tt.getKeywordTokenType(347);}
"measures" {return tt.getKeywordTokenType(348);}
"medium" {return tt.getKeywordTokenType(349);}
"member" {return tt.getKeywordTokenType(350);}
"memcompress" {return tt.getKeywordTokenType(351);}
"memory" {return tt.getKeywordTokenType(352);}
"merge" {return tt.getKeywordTokenType(353);}
"metadata" {return tt.getKeywordTokenType(354);}
"minextents" {return tt.getKeywordTokenType(355);}
"mining" {return tt.getKeywordTokenType(356);}
"minus" {return tt.getKeywordTokenType(357);}
"minute" {return tt.getKeywordTokenType(358);}
"minutes" {return tt.getKeywordTokenType(359);}
"minvalue" {return tt.getKeywordTokenType(360);}
"mirror" {return tt.getKeywordTokenType(361);}
"mismatch" {return tt.getKeywordTokenType(362);}
"mlslabel" {return tt.getKeywordTokenType(363);}
"mode" {return tt.getKeywordTokenType(364);}
"model" {return tt.getKeywordTokenType(365);}
"modification" {return tt.getKeywordTokenType(366);}
"modify" {return tt.getKeywordTokenType(367);}
"monitoring" {return tt.getKeywordTokenType(368);}
"month" {return tt.getKeywordTokenType(369);}
"months" {return tt.getKeywordTokenType(370);}
"mount" {return tt.getKeywordTokenType(371);}
"move" {return tt.getKeywordTokenType(372);}
"multiset" {return tt.getKeywordTokenType(373);}
"multivalue" {return tt.getKeywordTokenType(374);}
"name" {return tt.getKeywordTokenType(375);}
"nan" {return tt.getKeywordTokenType(376);}
"narrate" {return tt.getKeywordTokenType(377);}
"natural" {return tt.getKeywordTokenType(378);}
"nav" {return tt.getKeywordTokenType(379);}
"nchar_cs" {return tt.getKeywordTokenType(380);}
"nested" {return tt.getKeywordTokenType(381);}
"never" {return tt.getKeywordTokenType(382);}
"new" {return tt.getKeywordTokenType(383);}
"next" {return tt.getKeywordTokenType(384);}
"nextval" {return tt.getKeywordTokenType(385);}
"no" {return tt.getKeywordTokenType(386);}
"noarchivelog" {return tt.getKeywordTokenType(387);}
"noaudit" {return tt.getKeywordTokenType(388);}
"nocache" {return tt.getKeywordTokenType(389);}
"nocompress" {return tt.getKeywordTokenType(390);}
"nocycle" {return tt.getKeywordTokenType(391);}
"nodelay" {return tt.getKeywordTokenType(392);}
"noentityescaping" {return tt.getKeywordTokenType(393);}
"noforce" {return tt.getKeywordTokenType(394);}
"nologging" {return tt.getKeywordTokenType(395);}
"nomapping" {return tt.getKeywordTokenType(396);}
"nomaxvalue" {return tt.getKeywordTokenType(397);}
"nominvalue" {return tt.getKeywordTokenType(398);}
"nomonitoring" {return tt.getKeywordTokenType(399);}
"none" {return tt.getKeywordTokenType(400);}
"noneditionable" {return tt.getKeywordTokenType(401);}
"noorder" {return tt.getKeywordTokenType(402);}
"noparallel" {return tt.getKeywordTokenType(403);}
"norely" {return tt.getKeywordTokenType(404);}
"norepair" {return tt.getKeywordTokenType(405);}
"noresetlogs" {return tt.getKeywordTokenType(406);}
"noreverse" {return tt.getKeywordTokenType(407);}
"noschemacheck" {return tt.getKeywordTokenType(408);}
"nosort" {return tt.getKeywordTokenType(409);}
"noswitch" {return tt.getKeywordTokenType(410);}
"not" {return tt.getKeywordTokenType(411);}
"nothing" {return tt.getKeywordTokenType(412);}
"notification" {return tt.getKeywordTokenType(413);}
"notimeout" {return tt.getKeywordTokenType(414);}
"novalidate" {return tt.getKeywordTokenType(415);}
"nowait" {return tt.getKeywordTokenType(416);}
"null" {return tt.getKeywordTokenType(417);}
"nulls" {return tt.getKeywordTokenType(418);}
"object" {return tt.getKeywordTokenType(419);}
"of" {return tt.getKeywordTokenType(420);}
"off" {return tt.getKeywordTokenType(421);}
"offline" {return tt.getKeywordTokenType(422);}
"offset" {return tt.getKeywordTokenType(423);}
"on" {return tt.getKeywordTokenType(424);}
"one_sided_prob_or_less" {return tt.getKeywordTokenType(425);}
"one_sided_prob_or_more" {return tt.getKeywordTokenType(426);}
"one_sided_sig" {return tt.getKeywordTokenType(427);}
"online" {return tt.getKeywordTokenType(428);}
"only" {return tt.getKeywordTokenType(429);}
"open" {return tt.getKeywordTokenType(430);}
"operator" {return tt.getKeywordTokenType(431);}
"optimal" {return tt.getKeywordTokenType(432);}
"optimize" {return tt.getKeywordTokenType(433);}
"option" {return tt.getKeywordTokenType(434);}
"or" {return tt.getKeywordTokenType(435);}
"order" {return tt.getKeywordTokenType(436);}
"ordinality" {return tt.getKeywordTokenType(437);}
"organization" {return tt.getKeywordTokenType(438);}
"outer" {return tt.getKeywordTokenType(439);}
"outline" {return tt.getKeywordTokenType(440);}
"over" {return tt.getKeywordTokenType(441);}
"overflow" {return tt.getKeywordTokenType(442);}
"overlaps" {return tt.getKeywordTokenType(443);}
"package" {return tt.getKeywordTokenType(444);}
"parallel" {return tt.getKeywordTokenType(445);}
"parameters" {return tt.getKeywordTokenType(446);}
"partial" {return tt.getKeywordTokenType(447);}
"partition" {return tt.getKeywordTokenType(448);}
"partitions" {return tt.getKeywordTokenType(449);}
"passing" {return tt.getKeywordTokenType(450);}
"password" {return tt.getKeywordTokenType(451);}
"path" {return tt.getKeywordTokenType(452);}
"pctfree" {return tt.getKeywordTokenType(453);}
"pctincrease" {return tt.getKeywordTokenType(454);}
"pctthreshold" {return tt.getKeywordTokenType(455);}
"pctused" {return tt.getKeywordTokenType(456);}
"pctversion" {return tt.getKeywordTokenType(457);}
"percent" {return tt.getKeywordTokenType(458);}
"performance" {return tt.getKeywordTokenType(459);}
"period" {return tt.getKeywordTokenType(460);}
"phi_coefficient" {return tt.getKeywordTokenType(461);}
"physical" {return tt.getKeywordTokenType(462);}
"pivot" {return tt.getKeywordTokenType(463);}
"plan" {return tt.getKeywordTokenType(464);}
"pluggable" {return tt.getKeywordTokenType(465);}
"policy" {return tt.getKeywordTokenType(466);}
"post_transaction" {return tt.getKeywordTokenType(467);}
"power" {return tt.getKeywordTokenType(468);}
"prebuilt" {return tt.getKeywordTokenType(469);}
"preceding" {return tt.getKeywordTokenType(470);}
"precision" {return tt.getKeywordTokenType(471);}
"prepare" {return tt.getKeywordTokenType(472);}
"present" {return tt.getKeywordTokenType(473);}
"preserve" {return tt.getKeywordTokenType(474);}
"pretty" {return tt.getKeywordTokenType(475);}
"primary" {return tt.getKeywordTokenType(476);}
"prior" {return tt.getKeywordTokenType(477);}
"priority" {return tt.getKeywordTokenType(478);}
"private" {return tt.getKeywordTokenType(479);}
"privilege" {return tt.getKeywordTokenType(480);}
"privileges" {return tt.getKeywordTokenType(481);}
"procedure" {return tt.getKeywordTokenType(482);}
"process" {return tt.getKeywordTokenType(483);}
"profile" {return tt.getKeywordTokenType(484);}
"program" {return tt.getKeywordTokenType(485);}
"protection" {return tt.getKeywordTokenType(486);}
"public" {return tt.getKeywordTokenType(487);}
"purge" {return tt.getKeywordTokenType(488);}
"query" {return tt.getKeywordTokenType(489);}
"queue" {return tt.getKeywordTokenType(490);}
"quiesce" {return tt.getKeywordTokenType(491);}
"quota" {return tt.getKeywordTokenType(492);}
"range" {return tt.getKeywordTokenType(493);}
"read" {return tt.getKeywordTokenType(494);}
"reads" {return tt.getKeywordTokenType(495);}
"rebalance" {return tt.getKeywordTokenType(496);}
"rebuild" {return tt.getKeywordTokenType(497);}
"recover" {return tt.getKeywordTokenType(498);}
"recovery" {return tt.getKeywordTokenType(499);}
"recycle" {return tt.getKeywordTokenType(500);}
"redefine" {return tt.getKeywordTokenType(501);}
"reduced" {return tt.getKeywordTokenType(502);}
"ref" {return tt.getKeywordTokenType(503);}
"reference" {return tt.getKeywordTokenType(504);}
"references" {return tt.getKeywordTokenType(505);}
"refresh" {return tt.getKeywordTokenType(506);}
"regexp_like" {return tt.getKeywordTokenType(507);}
"register" {return tt.getKeywordTokenType(508);}
"reject" {return tt.getKeywordTokenType(509);}
"rely" {return tt.getKeywordTokenType(510);}
"remainder" {return tt.getKeywordTokenType(511);}
"rename" {return tt.getKeywordTokenType(512);}
"repair" {return tt.getKeywordTokenType(513);}
"repeat" {return tt.getKeywordTokenType(514);}
"replace" {return tt.getKeywordTokenType(515);}
"reset" {return tt.getKeywordTokenType(516);}
"resetlogs" {return tt.getKeywordTokenType(517);}
"resize" {return tt.getKeywordTokenType(518);}
"resolve" {return tt.getKeywordTokenType(519);}
"resolver" {return tt.getKeywordTokenType(520);}
"resource" {return tt.getKeywordTokenType(521);}
"restrict" {return tt.getKeywordTokenType(522);}
"restricted" {return tt.getKeywordTokenType(523);}
"resumable" {return tt.getKeywordTokenType(524);}
"resume" {return tt.getKeywordTokenType(525);}
"retention" {return tt.getKeywordTokenType(526);}
"return" {return tt.getKeywordTokenType(527);}
"returning" {return tt.getKeywordTokenType(528);}
"reuse" {return tt.getKeywordTokenType(529);}
"reverse" {return tt.getKeywordTokenType(530);}
"revoke" {return tt.getKeywordTokenType(531);}
"rewrite" {return tt.getKeywordTokenType(532);}
"right" {return tt.getKeywordTokenType(533);}
"role" {return tt.getKeywordTokenType(534);}
"rollback" {return tt.getKeywordTokenType(535);}
"rollover" {return tt.getKeywordTokenType(536);}
"rollup" {return tt.getKeywordTokenType(537);}
"row" {return tt.getKeywordTokenType(538);}
"rownum" {return tt.getKeywordTokenType(539);}
"rows" {return tt.getKeywordTokenType(540);}
"rule" {return tt.getKeywordTokenType(541);}
"rules" {return tt.getKeywordTokenType(542);}
"salt" {return tt.getKeywordTokenType(543);}
"sample" {return tt.getKeywordTokenType(544);}
"savepoint" {return tt.getKeywordTokenType(545);}
"scan" {return tt.getKeywordTokenType(546);}
"scheduler" {return tt.getKeywordTokenType(547);}
"schemacheck" {return tt.getKeywordTokenType(548);}
"scn" {return tt.getKeywordTokenType(549);}
"scope" {return tt.getKeywordTokenType(550);}
"second" {return tt.getKeywordTokenType(551);}
"seed" {return tt.getKeywordTokenType(552);}
"segment" {return tt.getKeywordTokenType(553);}
"select" {return tt.getKeywordTokenType(554);}
"sequence" {return tt.getKeywordTokenType(555);}
"sequential" {return tt.getKeywordTokenType(556);}
"serializable" {return tt.getKeywordTokenType(557);}
"service" {return tt.getKeywordTokenType(558);}
"session" {return tt.getKeywordTokenType(559);}
"set" {return tt.getKeywordTokenType(560);}
"sets" {return tt.getKeywordTokenType(561);}
"settings" {return tt.getKeywordTokenType(562);}
"share" {return tt.getKeywordTokenType(563);}
"shared" {return tt.getKeywordTokenType(564);}
"shared_pool" {return tt.getKeywordTokenType(565);}
"sharing" {return tt.getKeywordTokenType(566);}
"show" {return tt.getKeywordTokenType(567);}
"showprompt" {return tt.getKeywordTokenType(568);}
"showsql" {return tt.getKeywordTokenType(569);}
"shrink" {return tt.getKeywordTokenType(570);}
"shutdown" {return tt.getKeywordTokenType(571);}
"siblings" {return tt.getKeywordTokenType(572);}
"sid" {return tt.getKeywordTokenType(573);}
"sig" {return tt.getKeywordTokenType(574);}
"single" {return tt.getKeywordTokenType(575);}
"size" {return tt.getKeywordTokenType(576);}
"skip" {return tt.getKeywordTokenType(577);}
"smallfile" {return tt.getKeywordTokenType(578);}
"snapshot" {return tt.getKeywordTokenType(579);}
"some" {return tt.getKeywordTokenType(580);}
"sort" {return tt.getKeywordTokenType(581);}
"source" {return tt.getKeywordTokenType(582);}
"space" {return tt.getKeywordTokenType(583);}
"specification" {return tt.getKeywordTokenType(584);}
"spfile" {return tt.getKeywordTokenType(585);}
"split" {return tt.getKeywordTokenType(586);}
"sql" {return tt.getKeywordTokenType(587);}
"standalone" {return tt.getKeywordTokenType(588);}
"standby" {return tt.getKeywordTokenType(589);}
"start" {return tt.getKeywordTokenType(590);}
"statement" {return tt.getKeywordTokenType(591);}
"statistic" {return tt.getKeywordTokenType(592);}
"statistics" {return tt.getKeywordTokenType(593);}
"stop" {return tt.getKeywordTokenType(594);}
"storage" {return tt.getKeywordTokenType(595);}
"store" {return tt.getKeywordTokenType(596);}
"strict" {return tt.getKeywordTokenType(597);}
"submultiset" {return tt.getKeywordTokenType(598);}
"subpartition" {return tt.getKeywordTokenType(599);}
"subpartitions" {return tt.getKeywordTokenType(600);}
"substitutable" {return tt.getKeywordTokenType(601);}
"successful" {return tt.getKeywordTokenType(602);}
"sum_squares_between" {return tt.getKeywordTokenType(603);}
"sum_squares_within" {return tt.getKeywordTokenType(604);}
"supplemental" {return tt.getKeywordTokenType(605);}
"suspend" {return tt.getKeywordTokenType(606);}
"switch" {return tt.getKeywordTokenType(607);}
"switchover" {return tt.getKeywordTokenType(608);}
"synchronous" {return tt.getKeywordTokenType(609);}
"synonym" {return tt.getKeywordTokenType(610);}
"sysbackup" {return tt.getKeywordTokenType(611);}
"sysdba" {return tt.getKeywordTokenType(612);}
"sysdg" {return tt.getKeywordTokenType(613);}
"syskm" {return tt.getKeywordTokenType(614);}
"sysoper" {return tt.getKeywordTokenType(615);}
"system" {return tt.getKeywordTokenType(616);}
"table" {return tt.getKeywordTokenType(617);}
"tables" {return tt.getKeywordTokenType(618);}
"tablespace" {return tt.getKeywordTokenType(619);}
"tempfile" {return tt.getKeywordTokenType(620);}
"template" {return tt.getKeywordTokenType(621);}
"temporary" {return tt.getKeywordTokenType(622);}
"test" {return tt.getKeywordTokenType(623);}
"than" {return tt.getKeywordTokenType(624);}
"then" {return tt.getKeywordTokenType(625);}
"thread" {return tt.getKeywordTokenType(626);}
"through" {return tt.getKeywordTokenType(627);}
"tier" {return tt.getKeywordTokenType(628);}
"ties" {return tt.getKeywordTokenType(629);}
"time" {return tt.getKeywordTokenType(630);}
"time_zone" {return tt.getKeywordTokenType(631);}
"timeout" {return tt.getKeywordTokenType(632);}
"timezone_abbr" {return tt.getKeywordTokenType(633);}
"timezone_hour" {return tt.getKeywordTokenType(634);}
"timezone_minute" {return tt.getKeywordTokenType(635);}
"timezone_region" {return tt.getKeywordTokenType(636);}
"to" {return tt.getKeywordTokenType(637);}
"trace" {return tt.getKeywordTokenType(638);}
"tracking" {return tt.getKeywordTokenType(639);}
"trailing" {return tt.getKeywordTokenType(640);}
"transaction" {return tt.getKeywordTokenType(641);}
"translation" {return tt.getKeywordTokenType(642);}
"trigger" {return tt.getKeywordTokenType(643);}
"truncate" {return tt.getKeywordTokenType(644);}
"trusted" {return tt.getKeywordTokenType(645);}
"tuning" {return tt.getKeywordTokenType(646);}
"two_sided_prob" {return tt.getKeywordTokenType(647);}
"two_sided_sig" {return tt.getKeywordTokenType(648);}
"type" {return tt.getKeywordTokenType(649);}
"u_statistic" {return tt.getKeywordTokenType(650);}
"uid" {return tt.getKeywordTokenType(651);}
"unarchived" {return tt.getKeywordTokenType(652);}
"unbounded" {return tt.getKeywordTokenType(653);}
"unconditional" {return tt.getKeywordTokenType(654);}
"under" {return tt.getKeywordTokenType(655);}
"under_path" {return tt.getKeywordTokenType(656);}
"undrop" {return tt.getKeywordTokenType(657);}
"union" {return tt.getKeywordTokenType(658);}
"unique" {return tt.getKeywordTokenType(659);}
"unlimited" {return tt.getKeywordTokenType(660);}
"unlock" {return tt.getKeywordTokenType(661);}
"unpivot" {return tt.getKeywordTokenType(662);}
"unprotected" {return tt.getKeywordTokenType(663);}
"unquiesce" {return tt.getKeywordTokenType(664);}
"unrecoverable" {return tt.getKeywordTokenType(665);}
"until" {return tt.getKeywordTokenType(666);}
"unusable" {return tt.getKeywordTokenType(667);}
"unused" {return tt.getKeywordTokenType(668);}
"update" {return tt.getKeywordTokenType(669);}
"updated" {return tt.getKeywordTokenType(670);}
"upgrade" {return tt.getKeywordTokenType(671);}
"upsert" {return tt.getKeywordTokenType(672);}
"usage" {return tt.getKeywordTokenType(673);}
"use" {return tt.getKeywordTokenType(674);}
"user" {return tt.getKeywordTokenType(675);}
"using" {return tt.getKeywordTokenType(676);}
"validate" {return tt.getKeywordTokenType(677);}
"validation" {return tt.getKeywordTokenType(678);}
"value" {return tt.getKeywordTokenType(679);}
"values" {return tt.getKeywordTokenType(680);}
"varray" {return tt.getKeywordTokenType(681);}
"version" {return tt.getKeywordTokenType(682);}
"versions" {return tt.getKeywordTokenType(683);}
"view" {return tt.getKeywordTokenType(684);}
"visible" {return tt.getKeywordTokenType(685);}
"wait" {return tt.getKeywordTokenType(686);}
"wellformed" {return tt.getKeywordTokenType(687);}
"when" {return tt.getKeywordTokenType(688);}
"whenever" {return tt.getKeywordTokenType(689);}
"where" {return tt.getKeywordTokenType(690);}
"with" {return tt.getKeywordTokenType(691);}
"within" {return tt.getKeywordTokenType(692);}
"without" {return tt.getKeywordTokenType(693);}
"work" {return tt.getKeywordTokenType(694);}
"wrapper" {return tt.getKeywordTokenType(695);}
"write" {return tt.getKeywordTokenType(696);}
"xml" {return tt.getKeywordTokenType(697);}
"xmlnamespaces" {return tt.getKeywordTokenType(698);}
"xmlschema" {return tt.getKeywordTokenType(699);}
"xmltype" {return tt.getKeywordTokenType(700);}
"year" {return tt.getKeywordTokenType(701);}
"years" {return tt.getKeywordTokenType(702);}
"yes" {return tt.getKeywordTokenType(703);}
"zone" {return tt.getKeywordTokenType(704);}
"false" {return tt.getKeywordTokenType(705);}
"true" {return tt.getKeywordTokenType(706);}





"abs" {return tt.getFunctionTokenType(0);}
"acos" {return tt.getFunctionTokenType(1);}
"add_months" {return tt.getFunctionTokenType(2);}
"appendchildxml" {return tt.getFunctionTokenType(3);}
"ascii" {return tt.getFunctionTokenType(4);}
"asciistr" {return tt.getFunctionTokenType(5);}
"asin" {return tt.getFunctionTokenType(6);}
"atan" {return tt.getFunctionTokenType(7);}
"atan2" {return tt.getFunctionTokenType(8);}
"avg" {return tt.getFunctionTokenType(9);}
"bfilename" {return tt.getFunctionTokenType(10);}
"bin_to_num" {return tt.getFunctionTokenType(11);}
"bitand" {return tt.getFunctionTokenType(12);}
"cardinality" {return tt.getFunctionTokenType(13);}
"cast" {return tt.getFunctionTokenType(14);}
"ceil" {return tt.getFunctionTokenType(15);}
"chartorowid" {return tt.getFunctionTokenType(16);}
"chr" {return tt.getFunctionTokenType(17);}
"collect" {return tt.getFunctionTokenType(18);}
"compose" {return tt.getFunctionTokenType(19);}
"concat" {return tt.getFunctionTokenType(20);}
"convert" {return tt.getFunctionTokenType(21);}
"corr" {return tt.getFunctionTokenType(22);}
"corr_k" {return tt.getFunctionTokenType(23);}
"corr_s" {return tt.getFunctionTokenType(24);}
"cos" {return tt.getFunctionTokenType(25);}
"cosh" {return tt.getFunctionTokenType(26);}
"count" {return tt.getFunctionTokenType(27);}
"covar_pop" {return tt.getFunctionTokenType(28);}
"covar_samp" {return tt.getFunctionTokenType(29);}
"cume_dist" {return tt.getFunctionTokenType(30);}
"current_date" {return tt.getFunctionTokenType(31);}
"current_timestamp" {return tt.getFunctionTokenType(32);}
"cv" {return tt.getFunctionTokenType(33);}
"dbtimezone" {return tt.getFunctionTokenType(34);}
"dbtmezone" {return tt.getFunctionTokenType(35);}
"decode" {return tt.getFunctionTokenType(36);}
"decompose" {return tt.getFunctionTokenType(37);}
"deletexml" {return tt.getFunctionTokenType(38);}
"depth" {return tt.getFunctionTokenType(39);}
"deref" {return tt.getFunctionTokenType(40);}
"empty_blob" {return tt.getFunctionTokenType(41);}
"empty_clob" {return tt.getFunctionTokenType(42);}
"existsnode" {return tt.getFunctionTokenType(43);}
"exp" {return tt.getFunctionTokenType(44);}
"extract" {return tt.getFunctionTokenType(45);}
"extractvalue" {return tt.getFunctionTokenType(46);}
"first_value" {return tt.getFunctionTokenType(47);}
"floor" {return tt.getFunctionTokenType(48);}
"from_tz" {return tt.getFunctionTokenType(49);}
"greatest" {return tt.getFunctionTokenType(50);}
"group_id" {return tt.getFunctionTokenType(51);}
"grouping" {return tt.getFunctionTokenType(52);}
"grouping_id" {return tt.getFunctionTokenType(53);}
"hextoraw" {return tt.getFunctionTokenType(54);}
"initcap" {return tt.getFunctionTokenType(55);}
"insertchildxml" {return tt.getFunctionTokenType(56);}
"insertchildxmlafter" {return tt.getFunctionTokenType(57);}
"insertchildxmlbefore" {return tt.getFunctionTokenType(58);}
"insertxmlafter" {return tt.getFunctionTokenType(59);}
"insertxmlbefore" {return tt.getFunctionTokenType(60);}
"instr" {return tt.getFunctionTokenType(61);}
"instr2" {return tt.getFunctionTokenType(62);}
"instr4" {return tt.getFunctionTokenType(63);}
"instrb" {return tt.getFunctionTokenType(64);}
"instrc" {return tt.getFunctionTokenType(65);}
"iteration_number" {return tt.getFunctionTokenType(66);}
"json_array" {return tt.getFunctionTokenType(67);}
"json_arrayagg" {return tt.getFunctionTokenType(68);}
"json_dataguide" {return tt.getFunctionTokenType(69);}
"json_object" {return tt.getFunctionTokenType(70);}
"json_objectagg" {return tt.getFunctionTokenType(71);}
"json_query" {return tt.getFunctionTokenType(72);}
"json_table" {return tt.getFunctionTokenType(73);}
"json_value" {return tt.getFunctionTokenType(74);}
"lag" {return tt.getFunctionTokenType(75);}
"last_day" {return tt.getFunctionTokenType(76);}
"last_value" {return tt.getFunctionTokenType(77);}
"lateral" {return tt.getFunctionTokenType(78);}
"lead" {return tt.getFunctionTokenType(79);}
"least" {return tt.getFunctionTokenType(80);}
"length" {return tt.getFunctionTokenType(81);}
"length2" {return tt.getFunctionTokenType(82);}
"length4" {return tt.getFunctionTokenType(83);}
"lengthb" {return tt.getFunctionTokenType(84);}
"lengthc" {return tt.getFunctionTokenType(85);}
"listagg" {return tt.getFunctionTokenType(86);}
"ln" {return tt.getFunctionTokenType(87);}
"lnnvl" {return tt.getFunctionTokenType(88);}
"localtimestamp" {return tt.getFunctionTokenType(89);}
"lower" {return tt.getFunctionTokenType(90);}
"lpad" {return tt.getFunctionTokenType(91);}
"ltrim" {return tt.getFunctionTokenType(92);}
"make_ref" {return tt.getFunctionTokenType(93);}
"max" {return tt.getFunctionTokenType(94);}
"median" {return tt.getFunctionTokenType(95);}
"min" {return tt.getFunctionTokenType(96);}
"mod" {return tt.getFunctionTokenType(97);}
"months_between" {return tt.getFunctionTokenType(98);}
"nanvl" {return tt.getFunctionTokenType(99);}
"nchr" {return tt.getFunctionTokenType(100);}
"new_time" {return tt.getFunctionTokenType(101);}
"next_day" {return tt.getFunctionTokenType(102);}
"nls_charset_decl_len" {return tt.getFunctionTokenType(103);}
"nls_charset_id" {return tt.getFunctionTokenType(104);}
"nls_charset_name" {return tt.getFunctionTokenType(105);}
"nls_initcap" {return tt.getFunctionTokenType(106);}
"nls_lower" {return tt.getFunctionTokenType(107);}
"nls_upper" {return tt.getFunctionTokenType(108);}
"nlssort" {return tt.getFunctionTokenType(109);}
"ntile" {return tt.getFunctionTokenType(110);}
"nullif" {return tt.getFunctionTokenType(111);}
"numtodsinterval" {return tt.getFunctionTokenType(112);}
"numtoyminterval" {return tt.getFunctionTokenType(113);}
"nvl" {return tt.getFunctionTokenType(114);}
"nvl2" {return tt.getFunctionTokenType(115);}
"ora_hash" {return tt.getFunctionTokenType(116);}
"percent_rank" {return tt.getFunctionTokenType(117);}
"percentile_cont" {return tt.getFunctionTokenType(118);}
"percentile_disc" {return tt.getFunctionTokenType(119);}
"powermultiset" {return tt.getFunctionTokenType(120);}
"powermultiset_by_cardinality" {return tt.getFunctionTokenType(121);}
"presentnnv" {return tt.getFunctionTokenType(122);}
"presentv" {return tt.getFunctionTokenType(123);}
"previous" {return tt.getFunctionTokenType(124);}
"rank" {return tt.getFunctionTokenType(125);}
"ratio_to_report" {return tt.getFunctionTokenType(126);}
"rawtohex" {return tt.getFunctionTokenType(127);}
"rawtonhex" {return tt.getFunctionTokenType(128);}
"reftohex" {return tt.getFunctionTokenType(129);}
"regexp_instr" {return tt.getFunctionTokenType(130);}
"regexp_replace" {return tt.getFunctionTokenType(131);}
"regexp_substr" {return tt.getFunctionTokenType(132);}
"regr_avgx" {return tt.getFunctionTokenType(133);}
"regr_avgy" {return tt.getFunctionTokenType(134);}
"regr_count" {return tt.getFunctionTokenType(135);}
"regr_intercept" {return tt.getFunctionTokenType(136);}
"regr_r2" {return tt.getFunctionTokenType(137);}
"regr_slope" {return tt.getFunctionTokenType(138);}
"regr_sxx" {return tt.getFunctionTokenType(139);}
"regr_sxy" {return tt.getFunctionTokenType(140);}
"regr_syy" {return tt.getFunctionTokenType(141);}
"round" {return tt.getFunctionTokenType(142);}
"row_number" {return tt.getFunctionTokenType(143);}
"rowidtochar" {return tt.getFunctionTokenType(144);}
"rowidtonchar" {return tt.getFunctionTokenType(145);}
"rpad" {return tt.getFunctionTokenType(146);}
"rtrim" {return tt.getFunctionTokenType(147);}
"scn_to_timestamp" {return tt.getFunctionTokenType(148);}
"sessiontimezone" {return tt.getFunctionTokenType(149);}
"sign" {return tt.getFunctionTokenType(150);}
"sin" {return tt.getFunctionTokenType(151);}
"sinh" {return tt.getFunctionTokenType(152);}
"soundex" {return tt.getFunctionTokenType(153);}
"sqrt" {return tt.getFunctionTokenType(154);}
"stats_binomial_test" {return tt.getFunctionTokenType(155);}
"stats_crosstab" {return tt.getFunctionTokenType(156);}
"stats_f_test" {return tt.getFunctionTokenType(157);}
"stats_ks_test" {return tt.getFunctionTokenType(158);}
"stats_mode" {return tt.getFunctionTokenType(159);}
"stats_mw_test" {return tt.getFunctionTokenType(160);}
"stats_one_way_anova" {return tt.getFunctionTokenType(161);}
"stats_t_test_indep" {return tt.getFunctionTokenType(162);}
"stats_t_test_indepu" {return tt.getFunctionTokenType(163);}
"stats_t_test_one" {return tt.getFunctionTokenType(164);}
"stats_t_test_paired" {return tt.getFunctionTokenType(165);}
"stats_wsr_test" {return tt.getFunctionTokenType(166);}
"stddev" {return tt.getFunctionTokenType(167);}
"stddev_pop" {return tt.getFunctionTokenType(168);}
"stddev_samp" {return tt.getFunctionTokenType(169);}
"substr" {return tt.getFunctionTokenType(170);}
"substr2" {return tt.getFunctionTokenType(171);}
"substr4" {return tt.getFunctionTokenType(172);}
"substrb" {return tt.getFunctionTokenType(173);}
"substrc" {return tt.getFunctionTokenType(174);}
"sum" {return tt.getFunctionTokenType(175);}
"sys_connect_by_path" {return tt.getFunctionTokenType(176);}
"sys_context" {return tt.getFunctionTokenType(177);}
"sys_dburigen" {return tt.getFunctionTokenType(178);}
"sys_extract_utc" {return tt.getFunctionTokenType(179);}
"sys_guid" {return tt.getFunctionTokenType(180);}
"sys_typeid" {return tt.getFunctionTokenType(181);}
"sys_xmlagg" {return tt.getFunctionTokenType(182);}
"sys_xmlgen" {return tt.getFunctionTokenType(183);}
"sysdate" {return tt.getFunctionTokenType(184);}
"systimestamp" {return tt.getFunctionTokenType(185);}
"tan" {return tt.getFunctionTokenType(186);}
"tanh" {return tt.getFunctionTokenType(187);}
"timestamp_to_scn" {return tt.getFunctionTokenType(188);}
"to_binary_double" {return tt.getFunctionTokenType(189);}
"to_binary_float" {return tt.getFunctionTokenType(190);}
"to_char" {return tt.getFunctionTokenType(191);}
"to_clob" {return tt.getFunctionTokenType(192);}
"to_date" {return tt.getFunctionTokenType(193);}
"to_dsinterval" {return tt.getFunctionTokenType(194);}
"to_lob" {return tt.getFunctionTokenType(195);}
"to_multi_byte" {return tt.getFunctionTokenType(196);}
"to_nchar" {return tt.getFunctionTokenType(197);}
"to_nclob" {return tt.getFunctionTokenType(198);}
"to_number" {return tt.getFunctionTokenType(199);}
"to_single_byte" {return tt.getFunctionTokenType(200);}
"to_timestamp" {return tt.getFunctionTokenType(201);}
"to_timestamp_tz" {return tt.getFunctionTokenType(202);}
"to_yminterval" {return tt.getFunctionTokenType(203);}
"translate" {return tt.getFunctionTokenType(204);}
"treat" {return tt.getFunctionTokenType(205);}
"trim" {return tt.getFunctionTokenType(206);}
"trunc" {return tt.getFunctionTokenType(207);}
"tz_offset" {return tt.getFunctionTokenType(208);}
"unistr" {return tt.getFunctionTokenType(209);}
"updatexml" {return tt.getFunctionTokenType(210);}
"upper" {return tt.getFunctionTokenType(211);}
"userenv" {return tt.getFunctionTokenType(212);}
"validate_conversion" {return tt.getFunctionTokenType(213);}
"var_pop" {return tt.getFunctionTokenType(214);}
"var_samp" {return tt.getFunctionTokenType(215);}
"variance" {return tt.getFunctionTokenType(216);}
"vsize" {return tt.getFunctionTokenType(217);}
"width_bucket" {return tt.getFunctionTokenType(218);}
"xmlagg" {return tt.getFunctionTokenType(219);}
"xmlattributes" {return tt.getFunctionTokenType(220);}
"xmlcast" {return tt.getFunctionTokenType(221);}
"xmlcdata" {return tt.getFunctionTokenType(222);}
"xmlcolattval" {return tt.getFunctionTokenType(223);}
"xmlcomment" {return tt.getFunctionTokenType(224);}
"xmlconcat" {return tt.getFunctionTokenType(225);}
"xmldiff" {return tt.getFunctionTokenType(226);}
"xmlelement" {return tt.getFunctionTokenType(227);}
"xmlforest" {return tt.getFunctionTokenType(228);}
"xmlisvalid" {return tt.getFunctionTokenType(229);}
"xmlparse" {return tt.getFunctionTokenType(230);}
"xmlpatch" {return tt.getFunctionTokenType(231);}
"xmlpi" {return tt.getFunctionTokenType(232);}
"xmlquery" {return tt.getFunctionTokenType(233);}
"xmlroot" {return tt.getFunctionTokenType(234);}
"xmlsequence" {return tt.getFunctionTokenType(235);}
"xmlserialize" {return tt.getFunctionTokenType(236);}
"xmltable" {return tt.getFunctionTokenType(237);}
"xmltransform" {return tt.getFunctionTokenType(238);}







"aq_tm_processes" {return tt.getParameterTokenType(0);}
"archive_lag_target" {return tt.getParameterTokenType(1);}
"audit_file_dest" {return tt.getParameterTokenType(2);}
"audit_sys_operations" {return tt.getParameterTokenType(3);}
"audit_trail" {return tt.getParameterTokenType(4);}
"background_core_dump" {return tt.getParameterTokenType(5);}
"background_dump_dest" {return tt.getParameterTokenType(6);}
"backup_tape_io_slaves" {return tt.getParameterTokenType(7);}
"bitmap_merge_area_size" {return tt.getParameterTokenType(8);}
"blank_trimming" {return tt.getParameterTokenType(9);}
"circuits" {return tt.getParameterTokenType(10);}
"cluster_database" {return tt.getParameterTokenType(11);}
"cluster_database_instances" {return tt.getParameterTokenType(12);}
"cluster_interconnects" {return tt.getParameterTokenType(13);}
"commit_point_strength" {return tt.getParameterTokenType(14);}
"compatible" {return tt.getParameterTokenType(15);}
"composite_limit" {return tt.getParameterTokenType(16);}
"connect_time" {return tt.getParameterTokenType(17);}
"control_file_record_keep_time" {return tt.getParameterTokenType(18);}
"control_files" {return tt.getParameterTokenType(19);}
"core_dump_dest"{wso}"=" { yybegin(YYINITIAL); yypushback(1); return tt.getParameterTokenType(20);}
"cpu_count" {return tt.getParameterTokenType(21);}
"cpu_per_call" {return tt.getParameterTokenType(22);}
"cpu_per_session" {return tt.getParameterTokenType(23);}
"create_bitmap_area_size" {return tt.getParameterTokenType(24);}
"create_stored_outlines" {return tt.getParameterTokenType(25);}
"current_schema" {return tt.getParameterTokenType(26);}
"cursor_sharing" {return tt.getParameterTokenType(27);}
"cursor_space_for_time" {return tt.getParameterTokenType(28);}
"db_block_checking" {return tt.getParameterTokenType(29);}
"db_block_checksum" {return tt.getParameterTokenType(30);}
"db_block_size" {return tt.getParameterTokenType(31);}
"db_cache_advice" {return tt.getParameterTokenType(32);}
"db_cache_size" {return tt.getParameterTokenType(33);}
"db_create_file_dest" {return tt.getParameterTokenType(34);}
"db_create_online_log_dest_"{digit}+ {return tt.getParameterTokenType(35);}
"db_domain" {return tt.getParameterTokenType(36);}
"db_file_multiblock_read_count" {return tt.getParameterTokenType(37);}
"db_file_name_convert" {return tt.getParameterTokenType(38);}
"db_files" {return tt.getParameterTokenType(39);}
"db_flashback_retention_target" {return tt.getParameterTokenType(40);}
"db_keep_cache_size" {return tt.getParameterTokenType(41);}
"db_name" {return tt.getParameterTokenType(42);}
"db_nk_cache_size" {return tt.getParameterTokenType(43);}
"db_recovery_file_dest" {return tt.getParameterTokenType(44);}
"db_recovery_file_dest_size" {return tt.getParameterTokenType(45);}
"db_recycle_cache_size" {return tt.getParameterTokenType(46);}
"db_unique_name" {return tt.getParameterTokenType(47);}
"db_writer_processes" {return tt.getParameterTokenType(48);}
"dbwr_io_slaves" {return tt.getParameterTokenType(49);}
"ddl_wait_for_locks" {return tt.getParameterTokenType(50);}
"dg_broker_config_filen" {return tt.getParameterTokenType(51);}
"dg_broker_start" {return tt.getParameterTokenType(52);}
"disk_asynch_io" {return tt.getParameterTokenType(53);}
"dispatchers" {return tt.getParameterTokenType(54);}
"distributed_lock_timeout" {return tt.getParameterTokenType(55);}
"dml_locks" {return tt.getParameterTokenType(56);}
"enqueue_resources" {return tt.getParameterTokenType(57);}
"error_on_overlap_time" {return tt.getParameterTokenType(58);}
"event" {return tt.getParameterTokenType(59);}
"failed_login_attempts" {return tt.getParameterTokenType(60);}
"fal_client" {return tt.getParameterTokenType(61);}
"fal_server" {return tt.getParameterTokenType(62);}
"fast_start_mttr_target" {return tt.getParameterTokenType(63);}
"fast_start_parallel_rollback" {return tt.getParameterTokenType(64);}
"file_mapping" {return tt.getParameterTokenType(65);}
"fileio_network_adapters" {return tt.getParameterTokenType(66);}
"filesystemio_options" {return tt.getParameterTokenType(67);}
"fixed_date" {return tt.getParameterTokenType(68);}
"flagger" {return tt.getParameterTokenType(69);}
"gc_files_to_locks" {return tt.getParameterTokenType(70);}
"gcs_server_processes" {return tt.getParameterTokenType(71);}
"global_names" {return tt.getParameterTokenType(72);}
"hash_area_size" {return tt.getParameterTokenType(73);}
"hi_shared_memory_address" {return tt.getParameterTokenType(74);}
"hs_autoregister" {return tt.getParameterTokenType(75);}
"idle_time" {return tt.getParameterTokenType(76);}
"ifile" {return tt.getParameterTokenType(77);}
"instance"{wso}"=" { yybegin(YYINITIAL); yypushback(1); return tt.getParameterTokenType(78);}
"instance_groups" {return tt.getParameterTokenType(79);}
"instance_name" {return tt.getParameterTokenType(80);}
"instance_number" {return tt.getParameterTokenType(81);}
"instance_type" {return tt.getParameterTokenType(82);}
"isolation_level" {return tt.getParameterTokenType(83);}
"java_max_sessionspace_size" {return tt.getParameterTokenType(84);}
"java_pool_size" {return tt.getParameterTokenType(85);}
"java_soft_sessionspace_limit" {return tt.getParameterTokenType(86);}
"job_queue_processes" {return tt.getParameterTokenType(87);}
"large_pool_size" {return tt.getParameterTokenType(88);}
"ldap_directory_access" {return tt.getParameterTokenType(89);}
"license_max_sessions" {return tt.getParameterTokenType(90);}
"license_max_users" {return tt.getParameterTokenType(91);}
"license_sessions_warning" {return tt.getParameterTokenType(92);}
"local_listener" {return tt.getParameterTokenType(93);}
"lock_sga" {return tt.getParameterTokenType(94);}
"log_archive_config" {return tt.getParameterTokenType(95);}
"log_archive_dest" {return tt.getParameterTokenType(96);}
"log_archive_dest_"{digit}+ {return tt.getParameterTokenType(97);}
"log_archive_dest_state_"{digit}+ {return tt.getParameterTokenType(98);}
"log_archive_duplex_dest" {return tt.getParameterTokenType(99);}
"log_archive_format" {return tt.getParameterTokenType(100);}
"log_archive_local_first" {return tt.getParameterTokenType(101);}
"log_archive_max_processes" {return tt.getParameterTokenType(102);}
"log_archive_min_succeed_dest" {return tt.getParameterTokenType(103);}
"log_archive_trace" {return tt.getParameterTokenType(104);}
"log_buffer" {return tt.getParameterTokenType(105);}
"log_checkpoint_interval" {return tt.getParameterTokenType(106);}
"log_checkpoint_timeout" {return tt.getParameterTokenType(107);}
"log_checkpoints_to_alert" {return tt.getParameterTokenType(108);}
"log_file_name_convert" {return tt.getParameterTokenType(109);}
"logical_reads_per_call" {return tt.getParameterTokenType(110);}
"logical_reads_per_session" {return tt.getParameterTokenType(111);}
"logmnr_max_persistent_sessions" {return tt.getParameterTokenType(112);}
"max_commit_propagation_delay" {return tt.getParameterTokenType(113);}
"max_dispatchers" {return tt.getParameterTokenType(114);}
"max_dump_file_size" {return tt.getParameterTokenType(115);}
"max_shared_servers" {return tt.getParameterTokenType(116);}
"nls_calendar" {return tt.getParameterTokenType(117);}
"nls_comp" {return tt.getParameterTokenType(118);}
"nls_currency" {return tt.getParameterTokenType(119);}
"nls_date_format" {return tt.getParameterTokenType(120);}
"nls_date_language" {return tt.getParameterTokenType(121);}
"nls_dual_currency" {return tt.getParameterTokenType(122);}
"nls_iso_currency" {return tt.getParameterTokenType(123);}
"nls_language" {return tt.getParameterTokenType(124);}
"nls_length_semantics" {return tt.getParameterTokenType(125);}
"nls_nchar_conv_excp" {return tt.getParameterTokenType(126);}
"nls_numeric_characters" {return tt.getParameterTokenType(127);}
"nls_sort" {return tt.getParameterTokenType(128);}
"nls_territory" {return tt.getParameterTokenType(129);}
"nls_timestamp_format" {return tt.getParameterTokenType(130);}
"nls_timestamp_tz_format" {return tt.getParameterTokenType(131);}
"o7_dictionary_accessibility" {return tt.getParameterTokenType(132);}
"object_cache_max_size_percent" {return tt.getParameterTokenType(133);}
"object_cache_optimal_size" {return tt.getParameterTokenType(134);}
"olap_page_pool_size" {return tt.getParameterTokenType(135);}
"open_cursors" {return tt.getParameterTokenType(136);}
"open_links" {return tt.getParameterTokenType(137);}
"open_links_per_instance" {return tt.getParameterTokenType(138);}
"optimizer_dynamic_sampling" {return tt.getParameterTokenType(139);}
"optimizer_features_enable" {return tt.getParameterTokenType(140);}
"optimizer_index_caching" {return tt.getParameterTokenType(141);}
"optimizer_index_cost_adj" {return tt.getParameterTokenType(142);}
"optimizer_mode" {return tt.getParameterTokenType(143);}
"os_authent_prefix" {return tt.getParameterTokenType(144);}
"os_roles" {return tt.getParameterTokenType(145);}
"osm_diskgroups" {return tt.getParameterTokenType(146);}
"osm_diskstring" {return tt.getParameterTokenType(147);}
"osm_power_limit" {return tt.getParameterTokenType(148);}
"parallel_adaptive_multi_user" {return tt.getParameterTokenType(149);}
"parallel_execution_message_size" {return tt.getParameterTokenType(150);}
"parallel_instance_group" {return tt.getParameterTokenType(151);}
"parallel_max_servers" {return tt.getParameterTokenType(152);}
"parallel_min_percent" {return tt.getParameterTokenType(153);}
"parallel_min_servers" {return tt.getParameterTokenType(154);}
"parallel_threads_per_cpu" {return tt.getParameterTokenType(155);}
"password_grace_time" {return tt.getParameterTokenType(156);}
"password_life_time" {return tt.getParameterTokenType(157);}
"password_lock_time" {return tt.getParameterTokenType(158);}
"password_reuse_max" {return tt.getParameterTokenType(159);}
"password_reuse_time" {return tt.getParameterTokenType(160);}
"password_verify_function" {return tt.getParameterTokenType(161);}
"pga_aggregate_target" {return tt.getParameterTokenType(162);}
"plsql_code_type" {return tt.getParameterTokenType(163);}
"plsql_compiler_flags" {return tt.getParameterTokenType(164);}
"plsql_debug" {return tt.getParameterTokenType(165);}
"plsql_native_library_dir" {return tt.getParameterTokenType(166);}
"plsql_native_library_subdir_count" {return tt.getParameterTokenType(167);}
"plsql_optimize_level" {return tt.getParameterTokenType(168);}
"plsql_v2_compatibility" {return tt.getParameterTokenType(169);}
"plsql_warnings" {return tt.getParameterTokenType(170);}
"pre_page_sga" {return tt.getParameterTokenType(171);}
"private_sga" {return tt.getParameterTokenType(172);}
"processes" {return tt.getParameterTokenType(173);}
"query_rewrite_enabled" {return tt.getParameterTokenType(174);}
"query_rewrite_integrity" {return tt.getParameterTokenType(175);}
"rdbms_server_dn" {return tt.getParameterTokenType(176);}
"read_only_open_delayed" {return tt.getParameterTokenType(177);}
"recovery_parallelism" {return tt.getParameterTokenType(178);}
"remote_archive_enable" {return tt.getParameterTokenType(179);}
"remote_dependencies_mode" {return tt.getParameterTokenType(180);}
"remote_listener" {return tt.getParameterTokenType(181);}
"remote_login_passwordfile" {return tt.getParameterTokenType(182);}
"remote_os_authent" {return tt.getParameterTokenType(183);}
"remote_os_roles" {return tt.getParameterTokenType(184);}
"replication_dependency_tracking" {return tt.getParameterTokenType(185);}
"resource_limit" {return tt.getParameterTokenType(186);}
"resource_manager_plan" {return tt.getParameterTokenType(187);}
"resumable_timeout" {return tt.getParameterTokenType(188);}
"rollback_segments" {return tt.getParameterTokenType(189);}
"serial_reuse" {return tt.getParameterTokenType(190);}
"service_names" {return tt.getParameterTokenType(191);}
"session_cached_cursors" {return tt.getParameterTokenType(192);}
"session_max_open_files" {return tt.getParameterTokenType(193);}
"sessions" {return tt.getParameterTokenType(194);}
"sessions_per_user" {return tt.getParameterTokenType(195);}
"sga_max_size" {return tt.getParameterTokenType(196);}
"sga_target" {return tt.getParameterTokenType(197);}
"shadow_core_dump" {return tt.getParameterTokenType(198);}
"shared_memory_address" {return tt.getParameterTokenType(199);}
"shared_pool_reserved_size" {return tt.getParameterTokenType(200);}
"shared_pool_size" {return tt.getParameterTokenType(201);}
"shared_server_sessions" {return tt.getParameterTokenType(202);}
"shared_servers" {return tt.getParameterTokenType(203);}
"skip_unusable_indexes" {return tt.getParameterTokenType(204);}
"smtp_out_server" {return tt.getParameterTokenType(205);}
"sort_area_retained_size" {return tt.getParameterTokenType(206);}
"sort_area_size" {return tt.getParameterTokenType(207);}
"spfile"{wso}"=" { yybegin(YYINITIAL); yypushback(1); return tt.getParameterTokenType(208);}
"sql_trace" {return tt.getParameterTokenType(209);}
"sql92_security" {return tt.getParameterTokenType(210);}
"sqltune_category" {return tt.getParameterTokenType(211);}
"standby_archive_dest" {return tt.getParameterTokenType(212);}
"standby_file_management" {return tt.getParameterTokenType(213);}
"star_transformation_enabled" {return tt.getParameterTokenType(214);}
"statement_id" {return tt.getParameterTokenType(215);}
"statistics_level" {return tt.getParameterTokenType(216);}
"streams_pool_size" {return tt.getParameterTokenType(217);}
"tape_asynch_io" {return tt.getParameterTokenType(218);}
"thread"{wso}"=" { yybegin(YYINITIAL); yypushback(1); return tt.getParameterTokenType(219);}
"timed_os_statistics" {return tt.getParameterTokenType(220);}
"timed_statistics" {return tt.getParameterTokenType(221);}
"trace_enabled" {return tt.getParameterTokenType(222);}
"tracefile_identifier" {return tt.getParameterTokenType(223);}
"transactions" {return tt.getParameterTokenType(224);}
"transactions_per_rollback_segment" {return tt.getParameterTokenType(225);}
"undo_management" {return tt.getParameterTokenType(226);}
"undo_retention" {return tt.getParameterTokenType(227);}
"undo_tablespace" {return tt.getParameterTokenType(228);}
"use_indirect_data_buffers" {return tt.getParameterTokenType(229);}
"use_private_outlines" {return tt.getParameterTokenType(230);}
"use_stored_outlines" {return tt.getParameterTokenType(231);}
"user_dump_dest" {return tt.getParameterTokenType(232);}
"utl_file_dir" {return tt.getParameterTokenType(233);}
"workarea_size_policy" {return tt.getParameterTokenType(234);}


{CT_SIZE_CLAUSE} {return tt.getTokenType("CT_SIZE_CLAUSE");}

{INTEGER}     { return stt.getInteger(); }
{NUMBER}      { return stt.getNumber(); }
{STRING}      { return stt.getString(); }

{IDENTIFIER}         { return stt.getIdentifier(); }
{QUOTED_IDENTIFIER}  { return stt.getQuotedIdentifier(); }

{WHITE_SPACE}        { return stt.getWhiteSpace(); }
.                    { return stt.getIdentifier(); }
}
