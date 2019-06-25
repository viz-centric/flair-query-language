package com.flair.bi.compiler.mysql;

import com.flair.bi.compiler.SQLListener;
import com.flair.bi.grammar.FQLParser.ExprContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.Writer;
import java.util.Optional;

public class MySQLListener extends SQLListener {
    public MySQLListener(Writer writer) {
        super(writer);
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
        } else if(Optional.ofNullable(ctx.func_call_expr()).isPresent() && "YEARMONTH".equalsIgnoreCase(ctx.func_call_expr().start.getText())) {
        	str.append("EXTRACT(")       	
        	.append("YEAR_MONTH")
        	.append(" FROM ");
        	if(ctx.func_call_expr().getChild(2).getText().contains(",")) {
        		str.append("STR_TO_DATE( ")
        		.append(ctx.func_call_expr().getChild(2).getText()+")");
        		
        	}
        	else {
        	str.append(ctx.func_call_expr().getChild(2).getText());
        	}
        	str.append(")");
        }
        else if(Optional.ofNullable(ctx.func_call_expr()).isPresent() && "YEARQUARTER".equalsIgnoreCase(ctx.func_call_expr().start.getText())) {
        	str.append("CONCAT(YEAR(");       	
        	if(ctx.func_call_expr().getChild(2).getText().contains(",")) {
        		str.append("STR_TO_DATE( ")
        		.append(ctx.func_call_expr().getChild(2).getText()+")");
        		
        	}
        	else {
        	str.append(ctx.func_call_expr().getChild(2).getText());
        	}
        	str.append("),'-',QUARTER(");
        	if(ctx.func_call_expr().getChild(2).getText().contains(",")) {
        		str.append("STR_TO_DATE( ")
        		.append(ctx.func_call_expr().getChild(2).getText()+")");
        		
        	}
        	else {
        	str.append(ctx.func_call_expr().getChild(2).getText());
        	}
        	str.append("))");
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

        property.put(ctx, str.toString());
        
	}

}
