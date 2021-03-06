package com.flair.bi.compiler.athena;

import com.flair.bi.compiler.components.PrestoParser;
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
        CAST_MAP.put("string", CAST_MAP.get("flair_string"));
        CAST_MAP.remove("varchar");
        CAST_MAP.remove("int");
        CAST_MAP.remove("bigint");
    }

    @Override
    protected StringBuilder onDateFmt(FQLParser.ExprContext ctx) {
        return new StringBuilder().append("date_format(CAST(")
                .append(ctx.func_call_expr().getChild(2).getChild(0).getText()).append(" AS TIMESTAMP), ")
                .append(ctx.func_call_expr().getChild(2).getChild(2).getText())
                .append(")");
    }

    @Override
    protected StringBuilder onTime(FQLParser.ExprContext ctx) {
        return new StringBuilder().append("date_format(CAST(")
                .append(ctx.func_call_expr().getChild(2).getChild(0).getText()).append(" AS TIMESTAMP), ")
                .append("'%H:%i'")
                .append(")");
    }

    @Override
    protected StringBuilder onDateTime(FQLParser.ExprContext ctx) {
        return new StringBuilder()
                .append("date_format(CAST(")
                .append(ctx.func_call_expr().getChild(2).getChild(0).getText()).append(" AS TIMESTAMP), ")
                .append("'%d-%b-%Y %H:%i'")
                .append(")");
    }

    @Override
    public void exitDescribe_stmt(FQLParser.Describe_stmtContext ctx) {
        property.put(ctx, PrestoParser.exitDescribe_stmt(ctx));
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
    protected String onDateTruncate(String finalFieldName, String timeUnit) {
        // second, day
        return "date_trunc(" + timeUnit + ", " + finalFieldName + ")";
    }

}
