package com.flair.bi.compiler.athena;

import com.flair.bi.compiler.mysql.MySQLListener;
import com.flair.bi.grammar.FQLParser;

import java.io.Writer;

public class AthenaListener extends MySQLListener {

    public AthenaListener(Writer writer) {
        super(writer);

        CAST_MAP.put("timestamp",
                (field1) -> new StringBuilder()
                        .append("parse_datetime(")
                        .append(field1.getFieldName())
                        .append(",")
                        .append("'yyyy-MM-dd HH:mm:ss.SSSSSS'")
                        .append(")"));
        CAST_MAP.put("datetime", CAST_MAP.get("timestamp"));
        CAST_MAP.put("date", CAST_MAP.get("timestamp"));

        CAST_MAP.put("flair_string",
                (field) -> new StringBuilder()
                        .append("CAST(")
                        .append(field.getFieldName())
                        .append(" as VARCHAR)")
        );
    }

    @Override
    public void exitDescribe_stmt(FQLParser.Describe_stmtContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("SHOW TABLES");

        if (ctx.describe_stmt_like() != null) {
            sb.append(" ").append(ctx.describe_stmt_like().expr().getText().replaceAll("%", "*"));
        }

        property.put(ctx, sb.toString());
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitLimit_expr(FQLParser.Limit_exprContext ctx) {
        StringBuilder str = new StringBuilder();

        str
                .append(ctx.K_LIMIT().getText())
                .append(" ")
                .append(property.get(ctx.expr(0)));

        property.put(ctx, str.toString());
    }

    @Override
    protected String composeFlairInterval(String expression, String operator, String hourOrDays, String number) {
        return "(" +
                expression +
                " " +
                operator +
                " " + "interval '" + number + "' " + hourOrDays +
                ")";
    }

    @Override
    protected String onDateTruncate(String finalFieldName) {
        return "date_trunc('second', " + finalFieldName + ")";
    }

}
