package com.flair.bi.compiler.mysql;

import com.flair.bi.compiler.FlairCastData;
import com.flair.bi.compiler.SQLListener;
import com.flair.bi.compiler.utils.SqlTimeConverter;
import com.flair.bi.grammar.FQLParser;
import com.flair.bi.grammar.FQLParser.ExprContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.Writer;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

public class MySQLListener extends SQLListener {
    public MySQLListener(Writer writer) {
        super(writer);

        CAST_MAP.put("timestamp",
                (field1) -> new StringBuilder()
                        .append("STR_TO_DATE(")
                        .append(field1.getFieldName())
                        .append(",")
                        .append("'%Y-%m-%d %H:%i:%s.%f'")
                        .append(")"));
        CAST_MAP.put("datetime", CAST_MAP.get("timestamp"));
        CAST_MAP.put("date", CAST_MAP.get("timestamp"));

        CAST_MAP.put("flair_string",
                (field) -> new StringBuilder()
                        .append("CAST(")
                        .append(field.getFieldName())
                        .append(" as CHAR)")
        );
        CAST_MAP.put("varchar", CAST_MAP.get("flair_string"));
    }
    
    @Override
	public void exitExpr(ExprContext ctx) {
		  StringBuilder str = new StringBuilder();
		  
		 //literal
        Optional.ofNullable(ctx.literal())
                .map(property::get)
                .ifPresent(str::append);

        
        //BIND_PARAMETER
        Optional.ofNullable(ctx.BIND_PARAMETER())
                .ifPresent(str::append);
        
        
        //unary operator
        Optional.ofNullable(ctx.unary_operator())
                .map(property::get)
                .ifPresent(x -> str
                        .append(x)
                        .append(" ")
                        .append(property.get(ctx.expr(0))));

        
        //binary operator
        Optional.ofNullable(ctx.binary_operator())
                .map(property::get)
                .ifPresent(x -> str
                        .append(property.get(ctx.expr(0)))
                        .append(" ")
                        .append(x)
                        .append(" ")
                        .append(property.get(ctx.expr(1))));
        
        //func_call_expr 
        if (Optional.ofNullable(ctx.func_call_expr()).isPresent()
                && ("distinct_count".equalsIgnoreCase(ctx.func_call_expr().start.getText()))) {
            str.append("count(distinct ")
                    .append(ctx.func_call_expr().getChild(2).getChild(0).getText())
                    .append(")");
        } else if (Optional.ofNullable(ctx.func_call_expr()).isPresent()
                && ("datefmt".equalsIgnoreCase(ctx.func_call_expr().start.getText()))) {
            str.append("date_format(CAST(")
                    .append(ctx.func_call_expr().getChild(2).getChild(0).getText()).append(" AS TIMESTAMP), ")
                    .append(ctx.func_call_expr().getChild(2).getChild(2).getText())
                    .append(")");
        }
        else if(Optional.ofNullable(ctx.func_call_expr()).isPresent()
                && "YEARQUARTER".equalsIgnoreCase(ctx.func_call_expr().start.getText())) {
            str.append(extractCombinedDatePart(ctx.func_call_expr(), "YEAR-QUARTER"));
        }
        else if(Optional.ofNullable(ctx.func_call_expr()).isPresent()
                && "YEARMONTH".equalsIgnoreCase(ctx.func_call_expr().start.getText())) {
            str.append(extractCombinedDatePart(ctx.func_call_expr(), "YEAR-MONTH"));
        }
        else if(Optional.ofNullable(ctx.func_call_expr()).isPresent()
                && "YEARWEEK".equalsIgnoreCase(ctx.func_call_expr().start.getText())) {
            str.append(extractCombinedDatePart(ctx.func_call_expr(), "YEAR-WEEK"));
        }
        else if(Optional.ofNullable(ctx.func_call_expr()).isPresent()
                && Arrays.asList("YEAR", "QUARTER", "MONTH", "WEEK", "DAY").contains(ctx.func_call_expr().start.getText().toUpperCase())) {
            str.append(extractDatePart(ctx.func_call_expr(), ctx.func_call_expr().start.getText()));
        }
        else {
       	 Optional.ofNullable(ctx.func_call_expr())
            .map(property::get)
            .ifPresent(str::append);
       }
      
       
//        expr K_NOT? K_BETWEEN expr K_AND expr
        Optional.ofNullable(ctx.K_BETWEEN())
                .map(TerminalNode::getText)
                .ifPresent(x -> str.append(property.get(ctx.expr(0)))
                        .append(" ")
                        .append(ctx.K_NOT() == null ? "" : ctx.K_NOT().getText() + " ")
                        .append(ctx.K_BETWEEN().getText()).append(" ")
                        .append(property.get(ctx.expr(1)))
                        .append(" ").append(ctx.K_AND().getText()).append(" ")
                        .append(property.get(ctx.expr(2))));
        
        
        //expr K_NOT? K_IN ( '(' ( select_stmt
        //                          | expr ( ',' expr )*
        //                          )?
        //                      ')'
        //        | ( database_name '.' )? table_name )
        Optional.ofNullable(ctx.K_IN())
                .map(TerminalNode::getText)
                .ifPresent(x -> {
                    str.append(property.get(ctx.expr(0)))
                            .append(" ")
                            .append(ctx.K_NOT() == null ? "" : ctx.K_NOT().getText() + " ")
                            .append(x).append(" ");

                    String tableName = property.get(ctx.table_name());

                    if (tableName == null) {
                        str.append("(");
                        Optional.ofNullable(ctx.select_stmt())
                                .map(property::get)
                                .ifPresent(str::append);

                        Optional.ofNullable(ctx.comma_sep_expr())
                                .map(property::get)
                                .ifPresent(str::append);

                        str.append(")");

                    } else {
                        Optional.ofNullable(ctx.database_name())
                                .map(property::get)
                                .ifPresent(y ->
                                        str.append(y)
                                                .append(".")
                                );

                        str.append(tableName);
                    }

                });
        

//        ( K_NOT )? K_EXISTS )? '(' select_stmt ')'
        if (ctx.getText().contains("(") && ctx.select_stmt() != null) {
            Optional.ofNullable(ctx.select_stmt())
                    .map(property::get)
                    .ifPresent(x -> {
                        Optional.ofNullable(ctx.K_NOT())
                                .map(TerminalNode::getText)
                                .ifPresent(y -> str.append(y).append(" "));

                        Optional.ofNullable(ctx.K_EXISTS())
                                .map(TerminalNode::getText)
                                .ifPresent(y -> str.append(y).append(" "));

                        str.append("(")
                                .append(x)
                                .append(")");
                    });
        }
        
        //expr K_NOT? str_match_expr expr ( K_ESCAPE expr )?
        Optional.ofNullable(ctx.str_match_expr())
                .map(property::get)
                .ifPresent(x -> str.append(property.get(ctx.expr(0)))
                        .append(" ")
                        .append(ctx.K_NOT() == null ? "" : ctx.K_NOT().getText() + " ")
                        .append(x)
                        .append(" ")
                        .append(property.get(ctx.expr(1)))
                        .append(" ")
                        .append(ctx.K_ESCAPE() == null ? "" :
                                ctx.K_ESCAPE().getText() + " " + property.get(ctx.expr(2))));

        Optional.ofNullable(ctx.db_tbl_col_expr())
                .map(property::get)
                .ifPresent(str::append);

        Optional.ofNullable(ctx.expr_in_brackets())
                .map(item -> property.get(item.expr()))
                .ifPresent(expr_in_brackets ->
                        str.append("(")
                                .append(expr_in_brackets)
                                .append(")")
                );

        if (str.length() == 0) {
            str.append(ctx.getText());
        }

        property.put(ctx, str.toString());
        
	}

    private CharSequence extractCombinedDatePart(FQLParser.Func_call_exprContext func_call_expr, String type) {
        String[] split = type.split("-");
        StringBuilder str = new StringBuilder();
        str.append("CONCAT(")
                .append(extractDatePartAndCast(func_call_expr, split[0]))
                .append(", '-', ")
                .append(extractDatePartAndCast(func_call_expr, split[1]))
                .append(")");
        return str;
    }

    private CharSequence extractDatePart(FQLParser.Func_call_exprContext funcExpr, String datePart) {
        Function<FlairCastData, CharSequence> flair_string = CAST_MAP.get("timestamp");
        FlairCastData flairCastData = new FlairCastData();
        flairCastData.setFieldName(funcExpr.getChild(2).getText());
        flairCastData.setDataType("");
        CharSequence newCharSeq = flair_string.apply(flairCastData);

        StringBuilder str = new StringBuilder();
        str.append("EXTRACT(")
                .append(datePart)
                .append(" FROM ");
        str.append(newCharSeq);
        str.append(")");
        return str;
    }

    private CharSequence extractDatePartAndCast(FQLParser.Func_call_exprContext funcExpr, String datePart) {
        return extractDatePart(funcExpr, datePart);
    }

    @Override
    protected String composeFlairInterval(String expression, String operator, String hourOrDays, String number) {
        return "(" +
                expression +
                " " +
                operator +
                " " + "interval " + number + " " + hourOrDays +
                ")";
    }

    @Override
    protected String getHourOrDaysFromLetter(String letter, String number) {
        return SqlTimeConverter.toSingular(letter);
    }

}
