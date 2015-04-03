package com.dci.intellij.dbn.language.sql.dialect.oracle;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.dci.intellij.dbn.language.common.TokenTypeBundle;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.DBLanguageDialectIdentifier;
import com.dci.intellij.dbn.language.sql.dialect.oracle.OraclePLSQLBlockMonitor;
import com.dci.intellij.dbn.language.sql.dialect.oracle.OraclePLSQLBlockMonitor.Marker;
%%

%class OracleSQLParserFlexLexer
%implements FlexLexer
%public
%pack
%final
%unicode
%ignorecase
%function advance
%type IElementType
%eof{ return;
%eof}

%{
    private TokenTypeBundle tt;
    public OracleSQLParserFlexLexer(TokenTypeBundle tt) {
        this.tt = tt;
    }

    private int blockNesting = 0;
    private int blockStartPos = 0;

    /*
    public void startPsqlBlock(boolean isDeclareBloc) {
        blockNesting = 0;
        yybegin(PSQL_BLOCK);
        blockStartPos = zzStartRead;
        if (incNesting) blockNesting++;
    }

    public IElementType endPsqlBlock() {
        blockNesting = 0;
        yybegin(YYINITIAL);
        zzStartRead = blockStartPos;
        return tt.getChameleon(DBLanguageDialectIdentifier.ORACLE_PLSQL);
    }
    */

    public IElementType getChameleon() {
        return tt.getChameleon(DBLanguageDialectIdentifier.ORACLE_PLSQL);
    }

    OraclePLSQLBlockMonitor plsqlBlockMonitor = new OraclePLSQLBlockMonitor() {
        protected void lexerStart() {
            //yypushback(yylength());
            yybegin(PSQL_BLOCK);
            blockStartPos = zzStartRead;
        }
        protected void lexerEnd() {
            yybegin(YYINITIAL);
            zzStartRead = blockStartPos;
        }
    };

%}


//PLSQL_BLOCK_START = "create"({ws}"or"{ws}"replace")? {ws} ("function"|"procedure"|"type"|"trigger"|"package") | "declare" | "begin"
//PLSQL_BLOCK_END = ";"{wso}"/"
//PLSQL_BLOCK = {PLSQL_BLOCK_START}{ws}([^/;] | ";"{wso}([^/] | "/*") | [^;]{wso}"/")*{PLSQL_BLOCK_END}?

PSQL_BLOCK_START_CREATE_OR_REPLACE = "create"({ws}"or"{ws}"replace")? {ws}
PSQL_BLOCK_START_CREATE_PACKAGE = {PSQL_BLOCK_START_CREATE_OR_REPLACE}"package"
PSQL_BLOCK_START_CREATE_TRIGGER = {PSQL_BLOCK_START_CREATE_OR_REPLACE}"trigger"
PSQL_BLOCK_START_CREATE_METHOD  = {PSQL_BLOCK_START_CREATE_OR_REPLACE}("function"|"procedure")
PSQL_BLOCK_START_CREATE_TYPE    = {PSQL_BLOCK_START_CREATE_OR_REPLACE}"type"
PSQL_BLOCK_START_CREATE = {PSQL_BLOCK_START_CREATE_PACKAGE}|{PSQL_BLOCK_START_CREATE_TRIGGER}|{PSQL_BLOCK_START_CREATE_METHOD}|{PSQL_BLOCK_START_CREATE_TYPE}
PSQL_BLOCK_START_DECLARE = "declare"
PSQL_BLOCK_START_BEGIN = "begin"
PSQL_BLOCK_START = {PSQL_BLOCK_START_CREATE}|{PSQL_BLOCK_START_DECLARE}|{PSQL_BLOCK_START_BEGIN}
PSQL_BLOCK_END_IGNORE = "end"{ws}("if"|"loop")";"
PSQL_BLOCK_END = "end"({ws}({IDENTIFIER}|{QUOTED_IDENTIFIER}))*{wso}";"

WHITE_SPACE= {white_space_char}|{line_terminator}
line_terminator = \r|\n|\r\n
input_character = [^\r\n]
white_space = [ \t\f]
white_space_char = [ \n\r\t\f]
ws  = {WHITE_SPACE}+
wso = {WHITE_SPACE}*

BLOCK_COMMENT = "/*"([^*/] | "*"[^/]? | [^*]"/" | {ws})*"*/"
LINE_COMMENT = "--" {input_character}*
REM_LINE_COMMENT = "rem"({white_space}+{input_character}*|{line_terminator})


IDENTIFIER = [:jletter:] ([:jletterdigit:]|"#")*
QUOTED_IDENTIFIER = "\""[^\"]*"\""?

string_simple_quoted      = "'"([^\']|"''"|"\\'")*"'"?
string_alternative_quoted = "q'["[^\[\]]*"]'"? | "q'("[^\(\)]*")'"? | "q'{"[^\{\}]*"}'"? | "q'!"[^\!]*"!'"? | "q'<"[^\<\>]*">'"? | "q'|"[^|]*"|'"?
STRING = "n"?({string_alternative_quoted}|{string_simple_quoted})

sign = "+"|"-"
digit = [0-9]
INTEGER = {digit}+("e"{sign}?{digit}+)?
NUMBER = {INTEGER}?"."{digit}+(("e"{sign}?{digit}+)|(("f"|"d"){ws}))?

VARIABLE = ":"({IDENTIFIER}|{INTEGER})
SQLP_VARIABLE = "&""&"?{IDENTIFIER}

CT_SIZE_CLAUSE = {INTEGER}{wso}("k"|"m"|"g"|"t"|"p"|"e"){ws}

%state PSQL_BLOCK
%%

<PSQL_BLOCK> {
    {BLOCK_COMMENT}                 {}
    {LINE_COMMENT}                  {}
    {STRING}                        {}

    {PSQL_BLOCK_START_CREATE}       { if (blockStartPos < zzCurrentPos) {yypushback(yylength()); plsqlBlockMonitor.end(true); return getChameleon();}}
    {PSQL_BLOCK_END_IGNORE}         {}
    {PSQL_BLOCK_END}                { if (plsqlBlockMonitor.end(false)) return getChameleon();}

    "begin"                         { plsqlBlockMonitor.mark(Marker.BEGIN); }
    "function"{ws}{IDENTIFIER}      { plsqlBlockMonitor.mark(Marker.METHOD); }
    "procedure"{ws}{IDENTIFIER}     { plsqlBlockMonitor.mark(Marker.METHOD); }
    {IDENTIFIER}                    {}
    {INTEGER}                       {}
    {NUMBER}                        {}
    {WHITE_SPACE}+                  {}
    \n|\r|.                         {}
    <<EOF>>                         { plsqlBlockMonitor.end(true); return getChameleon(); }
}


<YYINITIAL> {
    {WHITE_SPACE}+   { return tt.getSharedTokenTypes().getWhiteSpace(); }

    {BLOCK_COMMENT}      { return tt.getSharedTokenTypes().getBlockComment(); }
    {LINE_COMMENT}       { return tt.getSharedTokenTypes().getLineComment(); }
    {REM_LINE_COMMENT}   { return tt.getSharedTokenTypes().getLineComment(); }

    "administer"{ws}"any"{ws}"sql"{ws}"tuning"{ws}"set" {return tt.getObjectTokenType(0);}
    "administer"{ws}"database"{ws}"trigger" {return tt.getObjectTokenType(1);}
    "administer"{ws}"resource"{ws}"manager" {return tt.getObjectTokenType(2);}
    "administer"{ws}"sql"{ws}"management"{ws}"object" {return tt.getObjectTokenType(3);}
    "administer"{ws}"sql"{ws}"tuning"{ws}"set" {return tt.getObjectTokenType(4);}
    "advisor" {return tt.getObjectTokenType(5);}
    "alter"{ws}"any"{ws}"assembly" {return tt.getObjectTokenType(6);}
    "alter"{ws}"any"{ws}"cluster" {return tt.getObjectTokenType(7);}
    "alter"{ws}"any"{ws}"cube" {return tt.getObjectTokenType(8);}
    "alter"{ws}"any"{ws}"cube"{ws}"dimension" {return tt.getObjectTokenType(9);}
    "alter"{ws}"any"{ws}"dimension" {return tt.getObjectTokenType(10);}
    "alter"{ws}"any"{ws}"edition" {return tt.getObjectTokenType(11);}
    "alter"{ws}"any"{ws}"evaluation"{ws}"context" {return tt.getObjectTokenType(12);}
    "alter"{ws}"any"{ws}"index" {return tt.getObjectTokenType(13);}
    "alter"{ws}"any"{ws}"indextype" {return tt.getObjectTokenType(14);}
    "alter"{ws}"any"{ws}"library" {return tt.getObjectTokenType(15);}
    "alter"{ws}"any"{ws}"materialized"{ws}"view" {return tt.getObjectTokenType(16);}
    "alter"{ws}"any"{ws}"mining"{ws}"model" {return tt.getObjectTokenType(17);}
    "alter"{ws}"any"{ws}"operator" {return tt.getObjectTokenType(18);}
    "alter"{ws}"any"{ws}"outline" {return tt.getObjectTokenType(19);}
    "alter"{ws}"any"{ws}"procedure" {return tt.getObjectTokenType(20);}
    "alter"{ws}"any"{ws}"role" {return tt.getObjectTokenType(21);}
    "alter"{ws}"any"{ws}"rule" {return tt.getObjectTokenType(22);}
    "alter"{ws}"any"{ws}"rule"{ws}"set" {return tt.getObjectTokenType(23);}
    "alter"{ws}"any"{ws}"sequence" {return tt.getObjectTokenType(24);}
    "alter"{ws}"any"{ws}"sql"{ws}"profile" {return tt.getObjectTokenType(25);}
    "alter"{ws}"any"{ws}"table" {return tt.getObjectTokenType(26);}
    "alter"{ws}"any"{ws}"trigger" {return tt.getObjectTokenType(27);}
    "alter"{ws}"any"{ws}"type" {return tt.getObjectTokenType(28);}
    "alter"{ws}"database" {return tt.getObjectTokenType(29);}
    "alter"{ws}"profile" {return tt.getObjectTokenType(30);}
    "alter"{ws}"resource"{ws}"cost" {return tt.getObjectTokenType(31);}
    "alter"{ws}"rollback"{ws}"segment" {return tt.getObjectTokenType(32);}
    "alter"{ws}"session" {return tt.getObjectTokenType(33);}
    "alter"{ws}"system" {return tt.getObjectTokenType(34);}
    "alter"{ws}"tablespace" {return tt.getObjectTokenType(35);}
    "alter"{ws}"user" {return tt.getObjectTokenType(36);}
    "analyze"{ws}"any" {return tt.getObjectTokenType(37);}
    "analyze"{ws}"any"{ws}"dictionary" {return tt.getObjectTokenType(38);}
    "audit"{ws}"any" {return tt.getObjectTokenType(39);}
    "audit"{ws}"system" {return tt.getObjectTokenType(40);}
    "backup"{ws}"any"{ws}"table" {return tt.getObjectTokenType(41);}
    "become"{ws}"user" {return tt.getObjectTokenType(42);}
    "change"{ws}"notification" {return tt.getObjectTokenType(43);}
    "comment"{ws}"any"{ws}"mining"{ws}"model" {return tt.getObjectTokenType(44);}
    "comment"{ws}"any"{ws}"table" {return tt.getObjectTokenType(45);}
    "create"{ws}"any"{ws}"assembly" {return tt.getObjectTokenType(46);}
    "create"{ws}"any"{ws}"cluster" {return tt.getObjectTokenType(47);}
    "create"{ws}"any"{ws}"context" {return tt.getObjectTokenType(48);}
    "create"{ws}"any"{ws}"cube" {return tt.getObjectTokenType(49);}
    "create"{ws}"any"{ws}"cube"{ws}"build"{ws}"process" {return tt.getObjectTokenType(50);}
    "create"{ws}"any"{ws}"cube"{ws}"dimension" {return tt.getObjectTokenType(51);}
    "create"{ws}"any"{ws}"dimension" {return tt.getObjectTokenType(52);}
    "create"{ws}"any"{ws}"directory" {return tt.getObjectTokenType(53);}
    "create"{ws}"any"{ws}"edition" {return tt.getObjectTokenType(54);}
    "create"{ws}"any"{ws}"evaluation"{ws}"context" {return tt.getObjectTokenType(55);}
    "create"{ws}"any"{ws}"index" {return tt.getObjectTokenType(56);}
    "create"{ws}"any"{ws}"indextype" {return tt.getObjectTokenType(57);}
    "create"{ws}"any"{ws}"job" {return tt.getObjectTokenType(58);}
    "create"{ws}"any"{ws}"library" {return tt.getObjectTokenType(59);}
    "create"{ws}"any"{ws}"materialized"{ws}"view" {return tt.getObjectTokenType(60);}
    "create"{ws}"any"{ws}"measure"{ws}"folder" {return tt.getObjectTokenType(61);}
    "create"{ws}"any"{ws}"mining"{ws}"model" {return tt.getObjectTokenType(62);}
    "create"{ws}"any"{ws}"operator" {return tt.getObjectTokenType(63);}
    "create"{ws}"any"{ws}"outline" {return tt.getObjectTokenType(64);}
    "create"{ws}"any"{ws}"procedure" {return tt.getObjectTokenType(65);}
    "create"{ws}"any"{ws}"rule" {return tt.getObjectTokenType(66);}
    "create"{ws}"any"{ws}"rule"{ws}"set" {return tt.getObjectTokenType(67);}
    "create"{ws}"any"{ws}"sequence" {return tt.getObjectTokenType(68);}
    "create"{ws}"any"{ws}"sql"{ws}"profile" {return tt.getObjectTokenType(69);}
    "create"{ws}"any"{ws}"synonym" {return tt.getObjectTokenType(70);}
    "create"{ws}"any"{ws}"table" {return tt.getObjectTokenType(71);}
    "create"{ws}"any"{ws}"trigger" {return tt.getObjectTokenType(72);}
    "create"{ws}"any"{ws}"type" {return tt.getObjectTokenType(73);}
    "create"{ws}"any"{ws}"view" {return tt.getObjectTokenType(74);}
    "create"{ws}"assembly" {return tt.getObjectTokenType(75);}
    "create"{ws}"cluster" {return tt.getObjectTokenType(76);}
    "create"{ws}"cube" {return tt.getObjectTokenType(77);}
    "create"{ws}"cube"{ws}"build"{ws}"process" {return tt.getObjectTokenType(78);}
    "create"{ws}"cube"{ws}"dimension" {return tt.getObjectTokenType(79);}
    "create"{ws}"database"{ws}"link" {return tt.getObjectTokenType(80);}
    "create"{ws}"dimension" {return tt.getObjectTokenType(81);}
    "create"{ws}"evaluation"{ws}"context" {return tt.getObjectTokenType(82);}
    "create"{ws}"external"{ws}"job" {return tt.getObjectTokenType(83);}
    "create"{ws}"indextype" {return tt.getObjectTokenType(84);}
    "create"{ws}"job" {return tt.getObjectTokenType(85);}
    "create"{ws}"library" {return tt.getObjectTokenType(86);}
    "create"{ws}"materialized"{ws}"view" {return tt.getObjectTokenType(87);}
    "create"{ws}"measure"{ws}"folder" {return tt.getObjectTokenType(88);}
    "create"{ws}"mining"{ws}"model" {return tt.getObjectTokenType(89);}
    "create"{ws}"operator" {return tt.getObjectTokenType(90);}
    "create"{ws}"procedure" {return tt.getObjectTokenType(91);}
    "create"{ws}"profile" {return tt.getObjectTokenType(92);}
    "create"{ws}"public"{ws}"database"{ws}"link" {return tt.getObjectTokenType(93);}
    "create"{ws}"public"{ws}"synonym" {return tt.getObjectTokenType(94);}
    "create"{ws}"role" {return tt.getObjectTokenType(95);}
    "create"{ws}"rollback"{ws}"segment" {return tt.getObjectTokenType(96);}
    "create"{ws}"rule" {return tt.getObjectTokenType(97);}
    "create"{ws}"rule"{ws}"set" {return tt.getObjectTokenType(98);}
    "create"{ws}"sequence" {return tt.getObjectTokenType(99);}
    "create"{ws}"session" {return tt.getObjectTokenType(100);}
    "create"{ws}"synonym" {return tt.getObjectTokenType(101);}
    "create"{ws}"table" {return tt.getObjectTokenType(102);}
    "create"{ws}"tablespace" {return tt.getObjectTokenType(103);}
    "create"{ws}"trigger" {return tt.getObjectTokenType(104);}
    "create"{ws}"type" {return tt.getObjectTokenType(105);}
    "create"{ws}"user" {return tt.getObjectTokenType(106);}
    "create"{ws}"view" {return tt.getObjectTokenType(107);}
    "debug"{ws}"any"{ws}"procedure" {return tt.getObjectTokenType(108);}
    "debug"{ws}"connect"{ws}"session" {return tt.getObjectTokenType(109);}
    "delete"{ws}"any"{ws}"cube"{ws}"dimension" {return tt.getObjectTokenType(110);}
    "delete"{ws}"any"{ws}"measure"{ws}"folder" {return tt.getObjectTokenType(111);}
    "delete"{ws}"any"{ws}"table" {return tt.getObjectTokenType(112);}
    "dequeue"{ws}"any"{ws}"queue" {return tt.getObjectTokenType(113);}
    "drop"{ws}"any"{ws}"assembly" {return tt.getObjectTokenType(114);}
    "drop"{ws}"any"{ws}"cluster" {return tt.getObjectTokenType(115);}
    "drop"{ws}"any"{ws}"context" {return tt.getObjectTokenType(116);}
    "drop"{ws}"any"{ws}"cube" {return tt.getObjectTokenType(117);}
    "drop"{ws}"any"{ws}"cube"{ws}"build"{ws}"process" {return tt.getObjectTokenType(118);}
    "drop"{ws}"any"{ws}"cube"{ws}"dimension" {return tt.getObjectTokenType(119);}
    "drop"{ws}"any"{ws}"dimension" {return tt.getObjectTokenType(120);}
    "drop"{ws}"any"{ws}"directory" {return tt.getObjectTokenType(121);}
    "drop"{ws}"any"{ws}"edition" {return tt.getObjectTokenType(122);}
    "drop"{ws}"any"{ws}"evaluation"{ws}"context" {return tt.getObjectTokenType(123);}
    "drop"{ws}"any"{ws}"index" {return tt.getObjectTokenType(124);}
    "drop"{ws}"any"{ws}"indextype" {return tt.getObjectTokenType(125);}
    "drop"{ws}"any"{ws}"library" {return tt.getObjectTokenType(126);}
    "drop"{ws}"any"{ws}"materialized"{ws}"view" {return tt.getObjectTokenType(127);}
    "drop"{ws}"any"{ws}"measure"{ws}"folder" {return tt.getObjectTokenType(128);}
    "drop"{ws}"any"{ws}"mining"{ws}"model" {return tt.getObjectTokenType(129);}
    "drop"{ws}"any"{ws}"operator" {return tt.getObjectTokenType(130);}
    "drop"{ws}"any"{ws}"outline" {return tt.getObjectTokenType(131);}
    "drop"{ws}"any"{ws}"procedure" {return tt.getObjectTokenType(132);}
    "drop"{ws}"any"{ws}"role" {return tt.getObjectTokenType(133);}
    "drop"{ws}"any"{ws}"rule" {return tt.getObjectTokenType(134);}
    "drop"{ws}"any"{ws}"rule"{ws}"set" {return tt.getObjectTokenType(135);}
    "drop"{ws}"any"{ws}"sequence" {return tt.getObjectTokenType(136);}
    "drop"{ws}"any"{ws}"sql"{ws}"profile" {return tt.getObjectTokenType(137);}
    "drop"{ws}"any"{ws}"synonym" {return tt.getObjectTokenType(138);}
    "drop"{ws}"any"{ws}"table" {return tt.getObjectTokenType(139);}
    "drop"{ws}"any"{ws}"trigger" {return tt.getObjectTokenType(140);}
    "drop"{ws}"any"{ws}"type" {return tt.getObjectTokenType(141);}
    "drop"{ws}"any"{ws}"view" {return tt.getObjectTokenType(142);}
    "drop"{ws}"profile" {return tt.getObjectTokenType(143);}
    "drop"{ws}"public"{ws}"database"{ws}"link" {return tt.getObjectTokenType(144);}
    "drop"{ws}"public"{ws}"synonym" {return tt.getObjectTokenType(145);}
    "drop"{ws}"rollback"{ws}"segment" {return tt.getObjectTokenType(146);}
    "drop"{ws}"tablespace" {return tt.getObjectTokenType(147);}
    "drop"{ws}"user" {return tt.getObjectTokenType(148);}
    "enqueue"{ws}"any"{ws}"queue" {return tt.getObjectTokenType(149);}
    "execute"{ws}"any"{ws}"assembly" {return tt.getObjectTokenType(150);}
    "execute"{ws}"any"{ws}"class" {return tt.getObjectTokenType(151);}
    "execute"{ws}"any"{ws}"evaluation"{ws}"context" {return tt.getObjectTokenType(152);}
    "execute"{ws}"any"{ws}"indextype" {return tt.getObjectTokenType(153);}
    "execute"{ws}"any"{ws}"library" {return tt.getObjectTokenType(154);}
    "execute"{ws}"any"{ws}"operator" {return tt.getObjectTokenType(155);}
    "execute"{ws}"any"{ws}"procedure" {return tt.getObjectTokenType(156);}
    "execute"{ws}"any"{ws}"program" {return tt.getObjectTokenType(157);}
    "execute"{ws}"any"{ws}"rule" {return tt.getObjectTokenType(158);}
    "execute"{ws}"any"{ws}"rule"{ws}"set" {return tt.getObjectTokenType(159);}
    "execute"{ws}"any"{ws}"type" {return tt.getObjectTokenType(160);}
    "execute"{ws}"assembly" {return tt.getObjectTokenType(161);}
    "exempt"{ws}"access"{ws}"policy" {return tt.getObjectTokenType(162);}
    "export"{ws}"full"{ws}"database" {return tt.getObjectTokenType(163);}
    "flashback"{ws}"any"{ws}"table" {return tt.getObjectTokenType(164);}
    "flashback"{ws}"archive"{ws}"administer" {return tt.getObjectTokenType(165);}
    "force"{ws}"any"{ws}"transaction" {return tt.getObjectTokenType(166);}
    "force"{ws}"transaction" {return tt.getObjectTokenType(167);}
    "global"{ws}"query"{ws}"rewrite" {return tt.getObjectTokenType(168);}
    "grant"{ws}"any"{ws}"object"{ws}"privilege" {return tt.getObjectTokenType(169);}
    "grant"{ws}"any"{ws}"privilege" {return tt.getObjectTokenType(170);}
    "grant"{ws}"any"{ws}"role" {return tt.getObjectTokenType(171);}
    "import"{ws}"full"{ws}"database" {return tt.getObjectTokenType(172);}
    "insert"{ws}"any"{ws}"cube"{ws}"dimension" {return tt.getObjectTokenType(173);}
    "insert"{ws}"any"{ws}"measure"{ws}"folder" {return tt.getObjectTokenType(174);}
    "insert"{ws}"any"{ws}"table" {return tt.getObjectTokenType(175);}
    "lock"{ws}"any"{ws}"table" {return tt.getObjectTokenType(176);}
    "manage"{ws}"any"{ws}"file"{ws}"group" {return tt.getObjectTokenType(177);}
    "manage"{ws}"any"{ws}"queue" {return tt.getObjectTokenType(178);}
    "manage"{ws}"file"{ws}"group" {return tt.getObjectTokenType(179);}
    "manage"{ws}"scheduler" {return tt.getObjectTokenType(180);}
    "manage"{ws}"tablespace" {return tt.getObjectTokenType(181);}
    "merge"{ws}"any"{ws}"view" {return tt.getObjectTokenType(182);}
    "on"{ws}"commit"{ws}"refresh" {return tt.getObjectTokenType(183);}
    "query"{ws}"rewrite" {return tt.getObjectTokenType(184);}
    "read"{ws}"any"{ws}"file"{ws}"group" {return tt.getObjectTokenType(185);}
    "restricted"{ws}"session" {return tt.getObjectTokenType(186);}
    "resumable" {return tt.getObjectTokenType(187);}
    "select"{ws}"any"{ws}"cube" {return tt.getObjectTokenType(188);}
    "select"{ws}"any"{ws}"cube"{ws}"dimension" {return tt.getObjectTokenType(189);}
    "select"{ws}"any"{ws}"dictionary" {return tt.getObjectTokenType(190);}
    "select"{ws}"any"{ws}"mining"{ws}"model" {return tt.getObjectTokenType(191);}
    "select"{ws}"any"{ws}"sequence" {return tt.getObjectTokenType(192);}
    "select"{ws}"any"{ws}"table" {return tt.getObjectTokenType(193);}
    "select"{ws}"any"{ws}"transaction" {return tt.getObjectTokenType(194);}
    "under"{ws}"any"{ws}"table" {return tt.getObjectTokenType(195);}
    "under"{ws}"any"{ws}"type" {return tt.getObjectTokenType(196);}
    "under"{ws}"any"{ws}"view" {return tt.getObjectTokenType(197);}
    "unlimited"{ws}"tablespace" {return tt.getObjectTokenType(198);}
    "update"{ws}"any"{ws}"cube" {return tt.getObjectTokenType(199);}
    "update"{ws}"any"{ws}"cube"{ws}"build"{ws}"process" {return tt.getObjectTokenType(200);}
    "update"{ws}"any"{ws}"cube"{ws}"dimension" {return tt.getObjectTokenType(201);}
    "update"{ws}"any"{ws}"table" {return tt.getObjectTokenType(202);}

    //{PSQL_BLOCK_START}  {  }
    //{PSQL_BLOCK_START_CREATE_PACKAGE}  { startPsqlBlock(true); }
    //{PSQL_BLOCK_START_CREATE_TRIGGER}  { startPsqlBlock(false); }
    //{PSQL_BLOCK_START_CREATE_METHOD}   { startPsqlBlock(false); }
    //{PSQL_BLOCK_START_CREATE_TYPE}     { startPsqlBlock(false); }
    //{PSQL_BLOCK_START_CREATE_TYPE}     { startPsqlBlock(false); }
    {PSQL_BLOCK_START_CREATE}          { plsqlBlockMonitor.start(Marker.CREATE); }
    {PSQL_BLOCK_START_DECLARE}         { plsqlBlockMonitor.start(Marker.DECLARE); }
    {PSQL_BLOCK_START_BEGIN}           { plsqlBlockMonitor.start(Marker.BEGIN); }

    {VARIABLE}          {return tt.getSharedTokenTypes().getVariable(); }
    {SQLP_VARIABLE}     {return tt.getSharedTokenTypes().getVariable(); }

    //{PLSQL_BLOCK}    {return tt.getChameleon(DBLanguageDialectIdentifier.ORACLE_PLSQL);}

    "("{wso}"+"{wso}")"  {return tt.getTokenType("CT_OUTER_JOIN");}

    "||" {return tt.getOperatorTokenType(0);}

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
    "access" {return tt.getKeywordTokenType(2);}
    "activate" {return tt.getKeywordTokenType(3);}
    "active" {return tt.getKeywordTokenType(4);}
    "add" {return tt.getKeywordTokenType(5);}
    "admin" {return tt.getKeywordTokenType(6);}
    "advise" {return tt.getKeywordTokenType(7);}
    "alias" {return tt.getKeywordTokenType(8);}
    "all" {return tt.getKeywordTokenType(9);}
    "allocate" {return tt.getKeywordTokenType(10);}
    "allow" {return tt.getKeywordTokenType(11);}
    "alter" {return tt.getKeywordTokenType(12);}
    "always" {return tt.getKeywordTokenType(13);}
    "ancillary" {return tt.getKeywordTokenType(14);}
    "and" {return tt.getKeywordTokenType(15);}
    "any" {return tt.getKeywordTokenType(16);}
    "apply" {return tt.getKeywordTokenType(17);}
    "archive" {return tt.getKeywordTokenType(18);}
    "archivelog" {return tt.getKeywordTokenType(19);}
    "array" {return tt.getKeywordTokenType(20);}
    "as" {return tt.getKeywordTokenType(21);}
    "asc" {return tt.getKeywordTokenType(22);}
    "at" {return tt.getKeywordTokenType(23);}
    "attribute" {return tt.getKeywordTokenType(24);}
    "attributes" {return tt.getKeywordTokenType(25);}
    "authid" {return tt.getKeywordTokenType(26);}
    "autoextend" {return tt.getKeywordTokenType(27);}
    "automatic" {return tt.getKeywordTokenType(28);}
    "availability" {return tt.getKeywordTokenType(29);}
    "backup" {return tt.getKeywordTokenType(30);}
    "begin" {return tt.getKeywordTokenType(31);}
    "between" {return tt.getKeywordTokenType(32);}
    "bigfile" {return tt.getKeywordTokenType(33);}
    "binding" {return tt.getKeywordTokenType(34);}
    "block" {return tt.getKeywordTokenType(35);}
    "body" {return tt.getKeywordTokenType(36);}
    "both" {return tt.getKeywordTokenType(37);}
    "buffer_cache" {return tt.getKeywordTokenType(38);}
    "buffer_pool" {return tt.getKeywordTokenType(39);}
    "by" {return tt.getKeywordTokenType(40);}
    "cache" {return tt.getKeywordTokenType(41);}
    "cancel" {return tt.getKeywordTokenType(42);}
    "canonical" {return tt.getKeywordTokenType(43);}
    "cascade" {return tt.getKeywordTokenType(44);}
    "case" {return tt.getKeywordTokenType(45);}
    "category" {return tt.getKeywordTokenType(46);}
    "change" {return tt.getKeywordTokenType(47);}
    "char_cs" {return tt.getKeywordTokenType(48);}
    "check" {return tt.getKeywordTokenType(49);}
    "checkpoint" {return tt.getKeywordTokenType(50);}
    "child" {return tt.getKeywordTokenType(51);}
    "chisq_df" {return tt.getKeywordTokenType(52);}
    "chisq_obs" {return tt.getKeywordTokenType(53);}
    "chisq_sig" {return tt.getKeywordTokenType(54);}
    "chunk" {return tt.getKeywordTokenType(55);}
    "class" {return tt.getKeywordTokenType(56);}
    "clear" {return tt.getKeywordTokenType(57);}
    "clone" {return tt.getKeywordTokenType(58);}
    "close" {return tt.getKeywordTokenType(59);}
    "cluster" {return tt.getKeywordTokenType(60);}
    "coalesce" {return tt.getKeywordTokenType(61);}
    "coarse" {return tt.getKeywordTokenType(62);}
    "coefficient" {return tt.getKeywordTokenType(63);}
    "cohens_k" {return tt.getKeywordTokenType(64);}
    "column" {return tt.getKeywordTokenType(65);}
    "column_value" {return tt.getKeywordTokenType(66);}
    "columns" {return tt.getKeywordTokenType(67);}
    "comment" {return tt.getKeywordTokenType(68);}
    "commit" {return tt.getKeywordTokenType(69);}
    "committed" {return tt.getKeywordTokenType(70);}
    "compact" {return tt.getKeywordTokenType(71);}
    "compatibility" {return tt.getKeywordTokenType(72);}
    "compile" {return tt.getKeywordTokenType(73);}
    "complete" {return tt.getKeywordTokenType(74);}
    "compress" {return tt.getKeywordTokenType(75);}
    "compute" {return tt.getKeywordTokenType(76);}
    "connect" {return tt.getKeywordTokenType(77);}
    "consider" {return tt.getKeywordTokenType(78);}
    "consistent" {return tt.getKeywordTokenType(79);}
    "constraint" {return tt.getKeywordTokenType(80);}
    "constraints" {return tt.getKeywordTokenType(81);}
    "cont_coefficient" {return tt.getKeywordTokenType(82);}
    "content" {return tt.getKeywordTokenType(83);}
    "contents" {return tt.getKeywordTokenType(84);}
    "context" {return tt.getKeywordTokenType(85);}
    "continue" {return tt.getKeywordTokenType(86);}
    "controlfile" {return tt.getKeywordTokenType(87);}
    "corruption" {return tt.getKeywordTokenType(88);}
    "cost" {return tt.getKeywordTokenType(89);}
    "cramers_v" {return tt.getKeywordTokenType(90);}
    "create" {return tt.getKeywordTokenType(91);}
    "cross" {return tt.getKeywordTokenType(92);}
    "cube" {return tt.getKeywordTokenType(93);}
    "current" {return tt.getKeywordTokenType(94);}
    "current_user" {return tt.getKeywordTokenType(95);}
    "currval" {return tt.getKeywordTokenType(96);}
    "cursor" {return tt.getKeywordTokenType(97);}
    "cycle" {return tt.getKeywordTokenType(98);}
    "data" {return tt.getKeywordTokenType(99);}
    "database" {return tt.getKeywordTokenType(100);}
    "datafile" {return tt.getKeywordTokenType(101);}
    "datafiles" {return tt.getKeywordTokenType(102);}
    "day" {return tt.getKeywordTokenType(103);}
    "ddl" {return tt.getKeywordTokenType(104);}
    "deallocate" {return tt.getKeywordTokenType(105);}
    "debug" {return tt.getKeywordTokenType(106);}
    "decrement" {return tt.getKeywordTokenType(107);}
    "default" {return tt.getKeywordTokenType(108);}
    "defaults" {return tt.getKeywordTokenType(109);}
    "deferrable" {return tt.getKeywordTokenType(110);}
    "deferred" {return tt.getKeywordTokenType(111);}
    "definer" {return tt.getKeywordTokenType(112);}
    "delay" {return tt.getKeywordTokenType(113);}
    "delete" {return tt.getKeywordTokenType(114);}
    "demand" {return tt.getKeywordTokenType(115);}
    "dense_rank" {return tt.getKeywordTokenType(116);}
    "desc" {return tt.getKeywordTokenType(117);}
    "determines" {return tt.getKeywordTokenType(118);}
    "df" {return tt.getKeywordTokenType(119);}
    "df_between" {return tt.getKeywordTokenType(120);}
    "df_den" {return tt.getKeywordTokenType(121);}
    "df_num" {return tt.getKeywordTokenType(122);}
    "df_within" {return tt.getKeywordTokenType(123);}
    "dimension" {return tt.getKeywordTokenType(124);}
    "directory" {return tt.getKeywordTokenType(125);}
    "disable" {return tt.getKeywordTokenType(126);}
    "disconnect" {return tt.getKeywordTokenType(127);}
    "disk" {return tt.getKeywordTokenType(128);}
    "diskgroup" {return tt.getKeywordTokenType(129);}
    "disks" {return tt.getKeywordTokenType(130);}
    "dismount" {return tt.getKeywordTokenType(131);}
    "distinct" {return tt.getKeywordTokenType(132);}
    "distributed" {return tt.getKeywordTokenType(133);}
    "dml" {return tt.getKeywordTokenType(134);}
    "document" {return tt.getKeywordTokenType(135);}
    "downgrade" {return tt.getKeywordTokenType(136);}
    "drop" {return tt.getKeywordTokenType(137);}
    "dump" {return tt.getKeywordTokenType(138);}
    "edition" {return tt.getKeywordTokenType(139);}
    "editioning" {return tt.getKeywordTokenType(140);}
    "element" {return tt.getKeywordTokenType(141);}
    "else" {return tt.getKeywordTokenType(142);}
    "empty" {return tt.getKeywordTokenType(143);}
    "enable" {return tt.getKeywordTokenType(144);}
    "encoding" {return tt.getKeywordTokenType(145);}
    "encrypt" {return tt.getKeywordTokenType(146);}
    "end" {return tt.getKeywordTokenType(147);}
    "enforced" {return tt.getKeywordTokenType(148);}
    "entityescaping" {return tt.getKeywordTokenType(149);}
    "entry" {return tt.getKeywordTokenType(150);}
    "equals_path" {return tt.getKeywordTokenType(151);}
    "errors" {return tt.getKeywordTokenType(152);}
    "escape" {return tt.getKeywordTokenType(153);}
    "evalname" {return tt.getKeywordTokenType(154);}
    "exact_prob" {return tt.getKeywordTokenType(155);}
    "exceptions" {return tt.getKeywordTokenType(156);}
    "exchange" {return tt.getKeywordTokenType(157);}
    "exclude" {return tt.getKeywordTokenType(158);}
    "excluding" {return tt.getKeywordTokenType(159);}
    "exclusive" {return tt.getKeywordTokenType(160);}
    "execute" {return tt.getKeywordTokenType(161);}
    "exists" {return tt.getKeywordTokenType(162);}
    "expire" {return tt.getKeywordTokenType(163);}
    "explain" {return tt.getKeywordTokenType(164);}
    "extent" {return tt.getKeywordTokenType(165);}
    "externally" {return tt.getKeywordTokenType(166);}
    "f_ratio" {return tt.getKeywordTokenType(167);}
    "failed" {return tt.getKeywordTokenType(168);}
    "failgroup" {return tt.getKeywordTokenType(169);}
    "fast" {return tt.getKeywordTokenType(170);}
    "file" {return tt.getKeywordTokenType(171);}
    "fine" {return tt.getKeywordTokenType(172);}
    "finish" {return tt.getKeywordTokenType(173);}
    "first" {return tt.getKeywordTokenType(174);}
    "flashback" {return tt.getKeywordTokenType(175);}
    "flush" {return tt.getKeywordTokenType(176);}
    "following" {return tt.getKeywordTokenType(177);}
    "for" {return tt.getKeywordTokenType(178);}
    "force" {return tt.getKeywordTokenType(179);}
    "foreign" {return tt.getKeywordTokenType(180);}
    "freelist" {return tt.getKeywordTokenType(181);}
    "freelists" {return tt.getKeywordTokenType(182);}
    "freepools" {return tt.getKeywordTokenType(183);}
    "fresh" {return tt.getKeywordTokenType(184);}
    "from" {return tt.getKeywordTokenType(185);}
    "full" {return tt.getKeywordTokenType(186);}
    "function" {return tt.getKeywordTokenType(187);}
    "global" {return tt.getKeywordTokenType(188);}
    "global_name" {return tt.getKeywordTokenType(189);}
    "globally" {return tt.getKeywordTokenType(190);}
    "grant" {return tt.getKeywordTokenType(191);}
    "group" {return tt.getKeywordTokenType(192);}
    "groups" {return tt.getKeywordTokenType(193);}
    "guard" {return tt.getKeywordTokenType(194);}
    "hash" {return tt.getKeywordTokenType(195);}
    "having" {return tt.getKeywordTokenType(196);}
    "hide" {return tt.getKeywordTokenType(197);}
    "hierarchy" {return tt.getKeywordTokenType(198);}
    "history" {return tt.getKeywordTokenType(199);}
    "hour" {return tt.getKeywordTokenType(200);}
    "id" {return tt.getKeywordTokenType(201);}
    "identified" {return tt.getKeywordTokenType(202);}
    "identifier" {return tt.getKeywordTokenType(203);}
    "ignore" {return tt.getKeywordTokenType(204);}
    "immediate" {return tt.getKeywordTokenType(205);}
    "in" {return tt.getKeywordTokenType(206);}
    "include" {return tt.getKeywordTokenType(207);}
    "including" {return tt.getKeywordTokenType(208);}
    "increment" {return tt.getKeywordTokenType(209);}
    "indent" {return tt.getKeywordTokenType(210);}
    "index" {return tt.getKeywordTokenType(211);}
    "indexes" {return tt.getKeywordTokenType(212);}
    "indextype" {return tt.getKeywordTokenType(213);}
    "infinite" {return tt.getKeywordTokenType(214);}
    "initial" {return tt.getKeywordTokenType(215);}
    "initially" {return tt.getKeywordTokenType(216);}
    "initrans" {return tt.getKeywordTokenType(217);}
    "inner" {return tt.getKeywordTokenType(218);}
    "insert" {return tt.getKeywordTokenType(219);}
    "instance" {return tt.getKeywordTokenType(220);}
    "intermediate" {return tt.getKeywordTokenType(221);}
    "intersect" {return tt.getKeywordTokenType(222);}
    "into" {return tt.getKeywordTokenType(223);}
    "invalidate" {return tt.getKeywordTokenType(224);}
    "is" {return tt.getKeywordTokenType(225);}
    "iterate" {return tt.getKeywordTokenType(226);}
    "java" {return tt.getKeywordTokenType(227);}
    "join" {return tt.getKeywordTokenType(228);}
    "keep" {return tt.getKeywordTokenType(229);}
    "key" {return tt.getKeywordTokenType(230);}
    "kill" {return tt.getKeywordTokenType(231);}
    "last" {return tt.getKeywordTokenType(232);}
    "leading" {return tt.getKeywordTokenType(233);}
    "left" {return tt.getKeywordTokenType(234);}
    "less" {return tt.getKeywordTokenType(235);}
    "level" {return tt.getKeywordTokenType(236);}
    "levels" {return tt.getKeywordTokenType(237);}
    "library" {return tt.getKeywordTokenType(238);}
    "like" {return tt.getKeywordTokenType(239);}
    "like2" {return tt.getKeywordTokenType(240);}
    "like4" {return tt.getKeywordTokenType(241);}
    "likec" {return tt.getKeywordTokenType(242);}
    "limit" {return tt.getKeywordTokenType(243);}
    "link" {return tt.getKeywordTokenType(244);}
    "lob" {return tt.getKeywordTokenType(245);}
    "local" {return tt.getKeywordTokenType(246);}
    "locator" {return tt.getKeywordTokenType(247);}
    "lock" {return tt.getKeywordTokenType(248);}
    "log" {return tt.getKeywordTokenType(249);}
    "logfile" {return tt.getKeywordTokenType(250);}
    "logging" {return tt.getKeywordTokenType(251);}
    "logical" {return tt.getKeywordTokenType(252);}
    "main" {return tt.getKeywordTokenType(253);}
    "managed" {return tt.getKeywordTokenType(254);}
    "manual" {return tt.getKeywordTokenType(255);}
    "mapping" {return tt.getKeywordTokenType(256);}
    "master" {return tt.getKeywordTokenType(257);}
    "matched" {return tt.getKeywordTokenType(258);}
    "materialized" {return tt.getKeywordTokenType(259);}
    "maxextents" {return tt.getKeywordTokenType(260);}
    "maximize" {return tt.getKeywordTokenType(261);}
    "maxsize" {return tt.getKeywordTokenType(262);}
    "maxvalue" {return tt.getKeywordTokenType(263);}
    "mean_squares_between" {return tt.getKeywordTokenType(264);}
    "mean_squares_within" {return tt.getKeywordTokenType(265);}
    "measures" {return tt.getKeywordTokenType(266);}
    "member" {return tt.getKeywordTokenType(267);}
    "memory" {return tt.getKeywordTokenType(268);}
    "merge" {return tt.getKeywordTokenType(269);}
    "minextents" {return tt.getKeywordTokenType(270);}
    "mining" {return tt.getKeywordTokenType(271);}
    "minus" {return tt.getKeywordTokenType(272);}
    "minute" {return tt.getKeywordTokenType(273);}
    "minutes" {return tt.getKeywordTokenType(274);}
    "minvalue" {return tt.getKeywordTokenType(275);}
    "mirror" {return tt.getKeywordTokenType(276);}
    "mlslabel" {return tt.getKeywordTokenType(277);}
    "mode" {return tt.getKeywordTokenType(278);}
    "model" {return tt.getKeywordTokenType(279);}
    "modify" {return tt.getKeywordTokenType(280);}
    "monitoring" {return tt.getKeywordTokenType(281);}
    "month" {return tt.getKeywordTokenType(282);}
    "mount" {return tt.getKeywordTokenType(283);}
    "move" {return tt.getKeywordTokenType(284);}
    "multiset" {return tt.getKeywordTokenType(285);}
    "name" {return tt.getKeywordTokenType(286);}
    "nan" {return tt.getKeywordTokenType(287);}
    "natural" {return tt.getKeywordTokenType(288);}
    "nav" {return tt.getKeywordTokenType(289);}
    "nchar_cs" {return tt.getKeywordTokenType(290);}
    "nested" {return tt.getKeywordTokenType(291);}
    "new" {return tt.getKeywordTokenType(292);}
    "next" {return tt.getKeywordTokenType(293);}
    "nextval" {return tt.getKeywordTokenType(294);}
    "no" {return tt.getKeywordTokenType(295);}
    "noarchivelog" {return tt.getKeywordTokenType(296);}
    "noaudit" {return tt.getKeywordTokenType(297);}
    "nocache" {return tt.getKeywordTokenType(298);}
    "nocompress" {return tt.getKeywordTokenType(299);}
    "nocycle" {return tt.getKeywordTokenType(300);}
    "nodelay" {return tt.getKeywordTokenType(301);}
    "noentityescaping" {return tt.getKeywordTokenType(302);}
    "noforce" {return tt.getKeywordTokenType(303);}
    "nologging" {return tt.getKeywordTokenType(304);}
    "nomapping" {return tt.getKeywordTokenType(305);}
    "nomaxvalue" {return tt.getKeywordTokenType(306);}
    "nominvalue" {return tt.getKeywordTokenType(307);}
    "nomonitoring" {return tt.getKeywordTokenType(308);}
    "none" {return tt.getKeywordTokenType(309);}
    "noorder" {return tt.getKeywordTokenType(310);}
    "noparallel" {return tt.getKeywordTokenType(311);}
    "norely" {return tt.getKeywordTokenType(312);}
    "norepair" {return tt.getKeywordTokenType(313);}
    "noresetlogs" {return tt.getKeywordTokenType(314);}
    "noreverse" {return tt.getKeywordTokenType(315);}
    "noschemacheck" {return tt.getKeywordTokenType(316);}
    "noswitch" {return tt.getKeywordTokenType(317);}
    "not" {return tt.getKeywordTokenType(318);}
    "nothing" {return tt.getKeywordTokenType(319);}
    "notimeout" {return tt.getKeywordTokenType(320);}
    "novalidate" {return tt.getKeywordTokenType(321);}
    "nowait" {return tt.getKeywordTokenType(322);}
    "null" {return tt.getKeywordTokenType(323);}
    "nulls" {return tt.getKeywordTokenType(324);}
    "object" {return tt.getKeywordTokenType(325);}
    "of" {return tt.getKeywordTokenType(326);}
    "off" {return tt.getKeywordTokenType(327);}
    "offline" {return tt.getKeywordTokenType(328);}
    "on" {return tt.getKeywordTokenType(329);}
    "one_sided_prob_or_less" {return tt.getKeywordTokenType(330);}
    "one_sided_prob_or_more" {return tt.getKeywordTokenType(331);}
    "one_sided_sig" {return tt.getKeywordTokenType(332);}
    "online" {return tt.getKeywordTokenType(333);}
    "only" {return tt.getKeywordTokenType(334);}
    "open" {return tt.getKeywordTokenType(335);}
    "operator" {return tt.getKeywordTokenType(336);}
    "optimal" {return tt.getKeywordTokenType(337);}
    "option" {return tt.getKeywordTokenType(338);}
    "or" {return tt.getKeywordTokenType(339);}
    "order" {return tt.getKeywordTokenType(340);}
    "ordinality" {return tt.getKeywordTokenType(341);}
    "outer" {return tt.getKeywordTokenType(342);}
    "outline" {return tt.getKeywordTokenType(343);}
    "over" {return tt.getKeywordTokenType(344);}
    "overflow" {return tt.getKeywordTokenType(345);}
    "package" {return tt.getKeywordTokenType(346);}
    "parallel" {return tt.getKeywordTokenType(347);}
    "parameters" {return tt.getKeywordTokenType(348);}
    "partition" {return tt.getKeywordTokenType(349);}
    "partitions" {return tt.getKeywordTokenType(350);}
    "passing" {return tt.getKeywordTokenType(351);}
    "path" {return tt.getKeywordTokenType(352);}
    "pctfree" {return tt.getKeywordTokenType(353);}
    "pctincrease" {return tt.getKeywordTokenType(354);}
    "pctthreshold" {return tt.getKeywordTokenType(355);}
    "pctused" {return tt.getKeywordTokenType(356);}
    "pctversion" {return tt.getKeywordTokenType(357);}
    "performance" {return tt.getKeywordTokenType(358);}
    "phi_coefficient" {return tt.getKeywordTokenType(359);}
    "physical" {return tt.getKeywordTokenType(360);}
    "pivot" {return tt.getKeywordTokenType(361);}
    "plan" {return tt.getKeywordTokenType(362);}
    "post_transaction" {return tt.getKeywordTokenType(363);}
    "power" {return tt.getKeywordTokenType(364);}
    "preceding" {return tt.getKeywordTokenType(365);}
    "prepare" {return tt.getKeywordTokenType(366);}
    "present" {return tt.getKeywordTokenType(367);}
    "preserve" {return tt.getKeywordTokenType(368);}
    "primary" {return tt.getKeywordTokenType(369);}
    "prior" {return tt.getKeywordTokenType(370);}
    "private" {return tt.getKeywordTokenType(371);}
    "privileges" {return tt.getKeywordTokenType(372);}
    "procedure" {return tt.getKeywordTokenType(373);}
    "profile" {return tt.getKeywordTokenType(374);}
    "protection" {return tt.getKeywordTokenType(375);}
    "public" {return tt.getKeywordTokenType(376);}
    "purge" {return tt.getKeywordTokenType(377);}
    "query" {return tt.getKeywordTokenType(378);}
    "quiesce" {return tt.getKeywordTokenType(379);}
    "range" {return tt.getKeywordTokenType(380);}
    "read" {return tt.getKeywordTokenType(381);}
    "reads" {return tt.getKeywordTokenType(382);}
    "rebalance" {return tt.getKeywordTokenType(383);}
    "rebuild" {return tt.getKeywordTokenType(384);}
    "recover" {return tt.getKeywordTokenType(385);}
    "recovery" {return tt.getKeywordTokenType(386);}
    "recycle" {return tt.getKeywordTokenType(387);}
    "ref" {return tt.getKeywordTokenType(388);}
    "reference" {return tt.getKeywordTokenType(389);}
    "references" {return tt.getKeywordTokenType(390);}
    "refresh" {return tt.getKeywordTokenType(391);}
    "regexp_like" {return tt.getKeywordTokenType(392);}
    "register" {return tt.getKeywordTokenType(393);}
    "reject" {return tt.getKeywordTokenType(394);}
    "rely" {return tt.getKeywordTokenType(395);}
    "remainder" {return tt.getKeywordTokenType(396);}
    "rename" {return tt.getKeywordTokenType(397);}
    "repair" {return tt.getKeywordTokenType(398);}
    "replace" {return tt.getKeywordTokenType(399);}
    "reset" {return tt.getKeywordTokenType(400);}
    "resetlogs" {return tt.getKeywordTokenType(401);}
    "resize" {return tt.getKeywordTokenType(402);}
    "resolve" {return tt.getKeywordTokenType(403);}
    "resolver" {return tt.getKeywordTokenType(404);}
    "resource" {return tt.getKeywordTokenType(405);}
    "restrict" {return tt.getKeywordTokenType(406);}
    "restricted" {return tt.getKeywordTokenType(407);}
    "resume" {return tt.getKeywordTokenType(408);}
    "retention" {return tt.getKeywordTokenType(409);}
    "return" {return tt.getKeywordTokenType(410);}
    "returning" {return tt.getKeywordTokenType(411);}
    "reuse" {return tt.getKeywordTokenType(412);}
    "reverse" {return tt.getKeywordTokenType(413);}
    "revoke" {return tt.getKeywordTokenType(414);}
    "rewrite" {return tt.getKeywordTokenType(415);}
    "right" {return tt.getKeywordTokenType(416);}
    "role" {return tt.getKeywordTokenType(417);}
    "rollback" {return tt.getKeywordTokenType(418);}
    "rollup" {return tt.getKeywordTokenType(419);}
    "row" {return tt.getKeywordTokenType(420);}
    "rownum" {return tt.getKeywordTokenType(421);}
    "rows" {return tt.getKeywordTokenType(422);}
    "rules" {return tt.getKeywordTokenType(423);}
    "salt" {return tt.getKeywordTokenType(424);}
    "sample" {return tt.getKeywordTokenType(425);}
    "savepoint" {return tt.getKeywordTokenType(426);}
    "scan" {return tt.getKeywordTokenType(427);}
    "schemacheck" {return tt.getKeywordTokenType(428);}
    "scn" {return tt.getKeywordTokenType(429);}
    "scope" {return tt.getKeywordTokenType(430);}
    "second" {return tt.getKeywordTokenType(431);}
    "seed" {return tt.getKeywordTokenType(432);}
    "segment" {return tt.getKeywordTokenType(433);}
    "select" {return tt.getKeywordTokenType(434);}
    "sequence" {return tt.getKeywordTokenType(435);}
    "sequential" {return tt.getKeywordTokenType(436);}
    "serializable" {return tt.getKeywordTokenType(437);}
    "session" {return tt.getKeywordTokenType(438);}
    "set" {return tt.getKeywordTokenType(439);}
    "sets" {return tt.getKeywordTokenType(440);}
    "settings" {return tt.getKeywordTokenType(441);}
    "share" {return tt.getKeywordTokenType(442);}
    "shared_pool" {return tt.getKeywordTokenType(443);}
    "show" {return tt.getKeywordTokenType(444);}
    "shrink" {return tt.getKeywordTokenType(445);}
    "shutdown" {return tt.getKeywordTokenType(446);}
    "siblings" {return tt.getKeywordTokenType(447);}
    "sid" {return tt.getKeywordTokenType(448);}
    "sig" {return tt.getKeywordTokenType(449);}
    "single" {return tt.getKeywordTokenType(450);}
    "size" {return tt.getKeywordTokenType(451);}
    "skip" {return tt.getKeywordTokenType(452);}
    "smallfile" {return tt.getKeywordTokenType(453);}
    "some" {return tt.getKeywordTokenType(454);}
    "sort" {return tt.getKeywordTokenType(455);}
    "source" {return tt.getKeywordTokenType(456);}
    "space" {return tt.getKeywordTokenType(457);}
    "specification" {return tt.getKeywordTokenType(458);}
    "spfile" {return tt.getKeywordTokenType(459);}
    "split" {return tt.getKeywordTokenType(460);}
    "standalone" {return tt.getKeywordTokenType(461);}
    "standby" {return tt.getKeywordTokenType(462);}
    "start" {return tt.getKeywordTokenType(463);}
    "statistic" {return tt.getKeywordTokenType(464);}
    "statistics" {return tt.getKeywordTokenType(465);}
    "stop" {return tt.getKeywordTokenType(466);}
    "storage" {return tt.getKeywordTokenType(467);}
    "store" {return tt.getKeywordTokenType(468);}
    "submultiset" {return tt.getKeywordTokenType(469);}
    "subpartition" {return tt.getKeywordTokenType(470);}
    "subpartitions" {return tt.getKeywordTokenType(471);}
    "substitutable" {return tt.getKeywordTokenType(472);}
    "successful" {return tt.getKeywordTokenType(473);}
    "sum_squares_between" {return tt.getKeywordTokenType(474);}
    "sum_squares_within" {return tt.getKeywordTokenType(475);}
    "supplemental" {return tt.getKeywordTokenType(476);}
    "suspend" {return tt.getKeywordTokenType(477);}
    "switch" {return tt.getKeywordTokenType(478);}
    "switchover" {return tt.getKeywordTokenType(479);}
    "synonym" {return tt.getKeywordTokenType(480);}
    "system" {return tt.getKeywordTokenType(481);}
    "table" {return tt.getKeywordTokenType(482);}
    "tables" {return tt.getKeywordTokenType(483);}
    "tablespace" {return tt.getKeywordTokenType(484);}
    "tempfile" {return tt.getKeywordTokenType(485);}
    "template" {return tt.getKeywordTokenType(486);}
    "temporary" {return tt.getKeywordTokenType(487);}
    "test" {return tt.getKeywordTokenType(488);}
    "than" {return tt.getKeywordTokenType(489);}
    "then" {return tt.getKeywordTokenType(490);}
    "thread" {return tt.getKeywordTokenType(491);}
    "through" {return tt.getKeywordTokenType(492);}
    "time" {return tt.getKeywordTokenType(493);}
    "time_zone" {return tt.getKeywordTokenType(494);}
    "timeout" {return tt.getKeywordTokenType(495);}
    "timezone_abbr" {return tt.getKeywordTokenType(496);}
    "timezone_hour" {return tt.getKeywordTokenType(497);}
    "timezone_minute" {return tt.getKeywordTokenType(498);}
    "timezone_region" {return tt.getKeywordTokenType(499);}
    "to" {return tt.getKeywordTokenType(500);}
    "trace" {return tt.getKeywordTokenType(501);}
    "tracking" {return tt.getKeywordTokenType(502);}
    "trailing" {return tt.getKeywordTokenType(503);}
    "transaction" {return tt.getKeywordTokenType(504);}
    "trigger" {return tt.getKeywordTokenType(505);}
    "truncate" {return tt.getKeywordTokenType(506);}
    "trusted" {return tt.getKeywordTokenType(507);}
    "two_sided_prob" {return tt.getKeywordTokenType(508);}
    "two_sided_sig" {return tt.getKeywordTokenType(509);}
    "type" {return tt.getKeywordTokenType(510);}
    "u_statistic" {return tt.getKeywordTokenType(511);}
    "uid" {return tt.getKeywordTokenType(512);}
    "unarchived" {return tt.getKeywordTokenType(513);}
    "unbounded" {return tt.getKeywordTokenType(514);}
    "under" {return tt.getKeywordTokenType(515);}
    "under_path" {return tt.getKeywordTokenType(516);}
    "undrop" {return tt.getKeywordTokenType(517);}
    "union" {return tt.getKeywordTokenType(518);}
    "unique" {return tt.getKeywordTokenType(519);}
    "unlimited" {return tt.getKeywordTokenType(520);}
    "unpivot" {return tt.getKeywordTokenType(521);}
    "unprotected" {return tt.getKeywordTokenType(522);}
    "unquiesce" {return tt.getKeywordTokenType(523);}
    "unrecoverable" {return tt.getKeywordTokenType(524);}
    "until" {return tt.getKeywordTokenType(525);}
    "unusable" {return tt.getKeywordTokenType(526);}
    "unused" {return tt.getKeywordTokenType(527);}
    "update" {return tt.getKeywordTokenType(528);}
    "updated" {return tt.getKeywordTokenType(529);}
    "upgrade" {return tt.getKeywordTokenType(530);}
    "upsert" {return tt.getKeywordTokenType(531);}
    "usage" {return tt.getKeywordTokenType(532);}
    "use" {return tt.getKeywordTokenType(533);}
    "user" {return tt.getKeywordTokenType(534);}
    "using" {return tt.getKeywordTokenType(535);}
    "validate" {return tt.getKeywordTokenType(536);}
    "validation" {return tt.getKeywordTokenType(537);}
    "values" {return tt.getKeywordTokenType(538);}
    "varray" {return tt.getKeywordTokenType(539);}
    "version" {return tt.getKeywordTokenType(540);}
    "versions" {return tt.getKeywordTokenType(541);}
    "view" {return tt.getKeywordTokenType(542);}
    "wait" {return tt.getKeywordTokenType(543);}
    "wellformed" {return tt.getKeywordTokenType(544);}
    "when" {return tt.getKeywordTokenType(545);}
    "whenever" {return tt.getKeywordTokenType(546);}
    "where" {return tt.getKeywordTokenType(547);}
    "with" {return tt.getKeywordTokenType(548);}
    "within" {return tt.getKeywordTokenType(549);}
    "without" {return tt.getKeywordTokenType(550);}
    "work" {return tt.getKeywordTokenType(551);}
    "write" {return tt.getKeywordTokenType(552);}
    "xml" {return tt.getKeywordTokenType(553);}
    "xmlnamespaces" {return tt.getKeywordTokenType(554);}
    "xmlschema" {return tt.getKeywordTokenType(555);}
    "xmltype" {return tt.getKeywordTokenType(556);}
    "year" {return tt.getKeywordTokenType(557);}
    "yes" {return tt.getKeywordTokenType(558);}
    "zone" {return tt.getKeywordTokenType(559);}
    "false" {return tt.getKeywordTokenType(560);}
    "true" {return tt.getKeywordTokenType(561);}





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
    "lag" {return tt.getFunctionTokenType(67);}
    "last_day" {return tt.getFunctionTokenType(68);}
    "last_value" {return tt.getFunctionTokenType(69);}
    "lead" {return tt.getFunctionTokenType(70);}
    "least" {return tt.getFunctionTokenType(71);}
    "length" {return tt.getFunctionTokenType(72);}
    "length2" {return tt.getFunctionTokenType(73);}
    "length4" {return tt.getFunctionTokenType(74);}
    "lengthb" {return tt.getFunctionTokenType(75);}
    "lengthc" {return tt.getFunctionTokenType(76);}
    "listagg" {return tt.getFunctionTokenType(77);}
    "ln" {return tt.getFunctionTokenType(78);}
    "lnnvl" {return tt.getFunctionTokenType(79);}
    "localtimestamp" {return tt.getFunctionTokenType(80);}
    "lower" {return tt.getFunctionTokenType(81);}
    "lpad" {return tt.getFunctionTokenType(82);}
    "ltrim" {return tt.getFunctionTokenType(83);}
    "make_ref" {return tt.getFunctionTokenType(84);}
    "max" {return tt.getFunctionTokenType(85);}
    "median" {return tt.getFunctionTokenType(86);}
    "min" {return tt.getFunctionTokenType(87);}
    "mod" {return tt.getFunctionTokenType(88);}
    "months_between" {return tt.getFunctionTokenType(89);}
    "nanvl" {return tt.getFunctionTokenType(90);}
    "nchr" {return tt.getFunctionTokenType(91);}
    "new_time" {return tt.getFunctionTokenType(92);}
    "next_day" {return tt.getFunctionTokenType(93);}
    "nls_charset_decl_len" {return tt.getFunctionTokenType(94);}
    "nls_charset_id" {return tt.getFunctionTokenType(95);}
    "nls_charset_name" {return tt.getFunctionTokenType(96);}
    "nls_initcap" {return tt.getFunctionTokenType(97);}
    "nls_lower" {return tt.getFunctionTokenType(98);}
    "nls_upper" {return tt.getFunctionTokenType(99);}
    "nlssort" {return tt.getFunctionTokenType(100);}
    "ntile" {return tt.getFunctionTokenType(101);}
    "nullif" {return tt.getFunctionTokenType(102);}
    "numtodsinterval" {return tt.getFunctionTokenType(103);}
    "numtoyminterval" {return tt.getFunctionTokenType(104);}
    "nvl" {return tt.getFunctionTokenType(105);}
    "nvl2" {return tt.getFunctionTokenType(106);}
    "ora_hash" {return tt.getFunctionTokenType(107);}
    "percent_rank" {return tt.getFunctionTokenType(108);}
    "percentile_cont" {return tt.getFunctionTokenType(109);}
    "percentile_disc" {return tt.getFunctionTokenType(110);}
    "powermultiset" {return tt.getFunctionTokenType(111);}
    "powermultiset_by_cardinality" {return tt.getFunctionTokenType(112);}
    "presentnnv" {return tt.getFunctionTokenType(113);}
    "presentv" {return tt.getFunctionTokenType(114);}
    "previous" {return tt.getFunctionTokenType(115);}
    "rank" {return tt.getFunctionTokenType(116);}
    "ratio_to_report" {return tt.getFunctionTokenType(117);}
    "rawtohex" {return tt.getFunctionTokenType(118);}
    "rawtonhex" {return tt.getFunctionTokenType(119);}
    "reftohex" {return tt.getFunctionTokenType(120);}
    "regexp_instr" {return tt.getFunctionTokenType(121);}
    "regexp_replace" {return tt.getFunctionTokenType(122);}
    "regexp_substr" {return tt.getFunctionTokenType(123);}
    "regr_avgx" {return tt.getFunctionTokenType(124);}
    "regr_avgy" {return tt.getFunctionTokenType(125);}
    "regr_count" {return tt.getFunctionTokenType(126);}
    "regr_intercept" {return tt.getFunctionTokenType(127);}
    "regr_r2" {return tt.getFunctionTokenType(128);}
    "regr_slope" {return tt.getFunctionTokenType(129);}
    "regr_sxx" {return tt.getFunctionTokenType(130);}
    "regr_sxy" {return tt.getFunctionTokenType(131);}
    "regr_syy" {return tt.getFunctionTokenType(132);}
    "round" {return tt.getFunctionTokenType(133);}
    "row_number" {return tt.getFunctionTokenType(134);}
    "rowidtochar" {return tt.getFunctionTokenType(135);}
    "rowidtonchar" {return tt.getFunctionTokenType(136);}
    "rpad" {return tt.getFunctionTokenType(137);}
    "rtrim" {return tt.getFunctionTokenType(138);}
    "scn_to_timestamp" {return tt.getFunctionTokenType(139);}
    "sessiontimezone" {return tt.getFunctionTokenType(140);}
    "sign" {return tt.getFunctionTokenType(141);}
    "sin" {return tt.getFunctionTokenType(142);}
    "sinh" {return tt.getFunctionTokenType(143);}
    "soundex" {return tt.getFunctionTokenType(144);}
    "sqrt" {return tt.getFunctionTokenType(145);}
    "stats_binomial_test" {return tt.getFunctionTokenType(146);}
    "stats_crosstab" {return tt.getFunctionTokenType(147);}
    "stats_f_test" {return tt.getFunctionTokenType(148);}
    "stats_ks_test" {return tt.getFunctionTokenType(149);}
    "stats_mode" {return tt.getFunctionTokenType(150);}
    "stats_mw_test" {return tt.getFunctionTokenType(151);}
    "stats_one_way_anova" {return tt.getFunctionTokenType(152);}
    "stats_t_test_indep" {return tt.getFunctionTokenType(153);}
    "stats_t_test_indepu" {return tt.getFunctionTokenType(154);}
    "stats_t_test_one" {return tt.getFunctionTokenType(155);}
    "stats_t_test_paired" {return tt.getFunctionTokenType(156);}
    "stats_wsr_test" {return tt.getFunctionTokenType(157);}
    "stddev" {return tt.getFunctionTokenType(158);}
    "stddev_pop" {return tt.getFunctionTokenType(159);}
    "stddev_samp" {return tt.getFunctionTokenType(160);}
    "substr" {return tt.getFunctionTokenType(161);}
    "substr2" {return tt.getFunctionTokenType(162);}
    "substr4" {return tt.getFunctionTokenType(163);}
    "substrb" {return tt.getFunctionTokenType(164);}
    "substrc" {return tt.getFunctionTokenType(165);}
    "sum" {return tt.getFunctionTokenType(166);}
    "sys_connect_by_path" {return tt.getFunctionTokenType(167);}
    "sys_context" {return tt.getFunctionTokenType(168);}
    "sys_dburigen" {return tt.getFunctionTokenType(169);}
    "sys_extract_utc" {return tt.getFunctionTokenType(170);}
    "sys_guid" {return tt.getFunctionTokenType(171);}
    "sys_typeid" {return tt.getFunctionTokenType(172);}
    "sys_xmlagg" {return tt.getFunctionTokenType(173);}
    "sys_xmlgen" {return tt.getFunctionTokenType(174);}
    "sysdate" {return tt.getFunctionTokenType(175);}
    "systimestamp" {return tt.getFunctionTokenType(176);}
    "tan" {return tt.getFunctionTokenType(177);}
    "tanh" {return tt.getFunctionTokenType(178);}
    "timestamp_to_scn" {return tt.getFunctionTokenType(179);}
    "to_binary_double" {return tt.getFunctionTokenType(180);}
    "to_binary_float" {return tt.getFunctionTokenType(181);}
    "to_char" {return tt.getFunctionTokenType(182);}
    "to_clob" {return tt.getFunctionTokenType(183);}
    "to_date" {return tt.getFunctionTokenType(184);}
    "to_dsinterval" {return tt.getFunctionTokenType(185);}
    "to_lob" {return tt.getFunctionTokenType(186);}
    "to_multi_byte" {return tt.getFunctionTokenType(187);}
    "to_nchar" {return tt.getFunctionTokenType(188);}
    "to_nclob" {return tt.getFunctionTokenType(189);}
    "to_number" {return tt.getFunctionTokenType(190);}
    "to_single_byte" {return tt.getFunctionTokenType(191);}
    "to_timestamp" {return tt.getFunctionTokenType(192);}
    "to_timestamp_tz" {return tt.getFunctionTokenType(193);}
    "to_yminterval" {return tt.getFunctionTokenType(194);}
    "translate" {return tt.getFunctionTokenType(195);}
    "treat" {return tt.getFunctionTokenType(196);}
    "trim" {return tt.getFunctionTokenType(197);}
    "trunc" {return tt.getFunctionTokenType(198);}
    "tz_offset" {return tt.getFunctionTokenType(199);}
    "unistr" {return tt.getFunctionTokenType(200);}
    "updatexml" {return tt.getFunctionTokenType(201);}
    "upper" {return tt.getFunctionTokenType(202);}
    "userenv" {return tt.getFunctionTokenType(203);}
    "value" {return tt.getFunctionTokenType(204);}
    "var_pop" {return tt.getFunctionTokenType(205);}
    "var_samp" {return tt.getFunctionTokenType(206);}
    "variance" {return tt.getFunctionTokenType(207);}
    "vsize" {return tt.getFunctionTokenType(208);}
    "width_bucket" {return tt.getFunctionTokenType(209);}
    "xmlagg" {return tt.getFunctionTokenType(210);}
    "xmlattributes" {return tt.getFunctionTokenType(211);}
    "xmlcast" {return tt.getFunctionTokenType(212);}
    "xmlcdata" {return tt.getFunctionTokenType(213);}
    "xmlcolattval" {return tt.getFunctionTokenType(214);}
    "xmlcomment" {return tt.getFunctionTokenType(215);}
    "xmlconcat" {return tt.getFunctionTokenType(216);}
    "xmldiff" {return tt.getFunctionTokenType(217);}
    "xmlelement" {return tt.getFunctionTokenType(218);}
    "xmlforest" {return tt.getFunctionTokenType(219);}
    "xmlisvalid" {return tt.getFunctionTokenType(220);}
    "xmlparse" {return tt.getFunctionTokenType(221);}
    "xmlpatch" {return tt.getFunctionTokenType(222);}
    "xmlpi" {return tt.getFunctionTokenType(223);}
    "xmlquery" {return tt.getFunctionTokenType(224);}
    "xmlroot" {return tt.getFunctionTokenType(225);}
    "xmlsequence" {return tt.getFunctionTokenType(226);}
    "xmlserialize" {return tt.getFunctionTokenType(227);}
    "xmltable" {return tt.getFunctionTokenType(228);}
    "xmltransform" {return tt.getFunctionTokenType(229);}







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
    "db_create_online_log_dest_n" {return tt.getParameterTokenType(35);}
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
    "log_archive_dest_n" {return tt.getParameterTokenType(97);}
    "log_archive_dest_state_n" {return tt.getParameterTokenType(98);}
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

    {INTEGER}     { return tt.getSharedTokenTypes().getInteger(); }
    {NUMBER}      { return tt.getSharedTokenTypes().getNumber(); }
    {STRING}      { return tt.getSharedTokenTypes().getString(); }

    {IDENTIFIER}         { yybegin(YYINITIAL); return tt.getSharedTokenTypes().getIdentifier(); }
    {QUOTED_IDENTIFIER}  { yybegin(YYINITIAL); return tt.getSharedTokenTypes().getQuotedIdentifier(); }

    .                    { yybegin(YYINITIAL); return tt.getSharedTokenTypes().getIdentifier(); }
}
