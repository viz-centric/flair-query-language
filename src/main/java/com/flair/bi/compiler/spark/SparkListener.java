package com.flair.bi.compiler.spark;

import com.flair.bi.compiler.SQLListener;
import com.flair.bi.grammar.FQLParser;

import java.io.Writer;
import java.util.Optional;

public class SparkListener extends SQLListener {
    public SparkListener(Writer writer) {
        super(writer);
    }

    @Override
    public void exitExpr(FQLParser.ExprContext ctx) {
        StringBuilder sb = new StringBuilder();

        Optional<FQLParser.Binary_operatorContext> optional = Optional
                .ofNullable(ctx.binary_operator())
                .filter(x -> x.K_LIKE() != null);
        if (optional.isPresent()) {
            sb
                    .append(property.get(ctx.expr(0)))
                    .append(" ")
                    .append(optional.get().getText())
                    .append(" ")
                    .append(property.get(ctx.expr(1)).replaceAll("%", "*"));
            property.put(ctx, sb.toString());
            return;
        }

        super.exitExpr(ctx);
    }
}
