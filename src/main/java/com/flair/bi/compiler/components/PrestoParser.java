package com.flair.bi.compiler.components;

import com.flair.bi.grammar.FQLParser;

public class PrestoParser {

    public static String exitDescribe_stmt(FQLParser.Describe_stmtContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT CONCAT(TABLE_SCHEMA, '.', TABLE_NAME) FROM information_schema.views WHERE table_schema NOT IN ('information_schema', 'pg_catalog') ");

        StringBuilder sbLike = new StringBuilder();
        if (ctx.describe_stmt_like() != null) {
            sbLike.append("AND UPPER(TABLE_NAME) LIKE UPPER(")
                    .append(ctx.describe_stmt_like().expr().getText())
                    .append(") ");
        }

        sb.append(sbLike)
                .append("UNION ALL SELECT CONCAT(TABLE_SCHEMA, '.', TABLE_NAME) FROM information_schema.TABLES WHERE table_schema NOT IN ('information_schema', 'pg_catalog') ")
                .append(sbLike);

        if (ctx.describe_stmt_limit() != null) {
            sb.append("LIMIT ")
                    .append(ctx.describe_stmt_limit().expr().getText());
        }

        return sb.toString().trim();
    }

}
