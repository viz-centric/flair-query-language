package com.flair.bi.compiler.bigquery;

import com.flair.bi.compiler.postgres.PostgresListener;
import com.flair.bi.grammar.FQLParser;

import java.io.Writer;

public class BigqueryListener extends PostgresListener {

    public BigqueryListener(Writer writer) {
        super(writer);
    }

    @Override
    public void exitDescribe_stmt(FQLParser.Describe_stmtContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT CONCAT(table_catalog, '.', table_schema, '.', TABLE_NAME) FROM ");

        String schema = ctx.describe_stmt_schema().expr().getText();

        sb.append("`")
                .append(schema)
                .append(".INFORMATION_SCHEMA.TABLES")
                .append("` ")
         .append("WHERE table_schema NOT IN ('information_schema', 'pg_catalog') ");

        StringBuilder sbLike = new StringBuilder();
        if (ctx.describe_stmt_like() != null) {
            sbLike.append("AND UPPER(TABLE_NAME) LIKE UPPER(")
                    .append(ctx.describe_stmt_like().expr().getText())
                    .append(") ");
        }

        sb.append(sbLike);

        if (ctx.describe_stmt_limit() != null) {
            sb.append("LIMIT ")
                    .append(ctx.describe_stmt_limit().expr().getText());
        }

        property.put(ctx, sb.toString().trim());
    }
}
