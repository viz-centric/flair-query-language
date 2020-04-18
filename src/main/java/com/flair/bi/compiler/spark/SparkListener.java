package com.flair.bi.compiler.spark;

import com.flair.bi.compiler.SQLListener;
import com.flair.bi.grammar.FQLParser;

import java.io.Writer;
import java.util.Optional;

public class SparkListener extends SQLListener {
    public SparkListener(Writer writer) {
        super(writer);

        CAST_MAP.put("timestamp",
                (field1) -> new StringBuilder()
                        .append("to_timestamp(")
                        .append(field1.getFieldName())
                        .append(",")
                        .append("'YYYY-MM-DD HH24:MI:SS.SSS'")
                        .append(")"));
        CAST_MAP.put("datetime", CAST_MAP.get("timestamp"));
        CAST_MAP.put("date", CAST_MAP.get("timestamp"));
    }

    @Override
    public void exitExpr(FQLParser.ExprContext ctx) {
        StringBuilder sb = new StringBuilder();

        Optional<FQLParser.Binary_operatorContext> optional = Optional
                .ofNullable(ctx.binary_operator())
                .filter(x -> x.K_LIKE() != null);
        if (Optional.ofNullable(ctx.func_call_expr()).isPresent()
                && ("distinct_count".equalsIgnoreCase(ctx.func_call_expr().start.getText()))) {
            sb.append("count(distinct ")
                    .append(ctx.func_call_expr().getChild(2).getChild(0).getText())
                    .append(")");
            property.put(ctx, sb.toString());
        } else if (optional.isPresent()) {
            sb
                    .append(property.get(ctx.expr(0)))
                    .append(" ")
                    .append(optional.get().getText())
                    .append(" ")
                    .append(property.get(ctx.expr(1)).replaceAll("%", "*"));
            property.put(ctx, sb.toString());
        } else {
            super.exitExpr(ctx);
        }
    }

    @Override
    public void exitDescribe_stmt(FQLParser.Describe_stmtContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("SHOW TABLES");

        if (ctx.describe_stmt_like() != null) {
            sb.append(" ")
                    .append(ctx.describe_stmt_like().K_LIKE().getText())
                    .append(' ')
                    .append(ctx.describe_stmt_like().expr().getText().replaceAll("%", "*"));
        }

        property.put(ctx, sb.toString());
    }
}
