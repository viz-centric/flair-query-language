package com.flair.bi.compiler.mongodb;

import com.flair.bi.compiler.AbstractFQLListener;
import com.flair.bi.grammar.FQLParser;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.IOException;
import java.io.Writer;
import java.util.Optional;
import java.util.stream.Collectors;

public class MongoDBListener extends AbstractFQLListener {
    public MongoDBListener(Writer writer) {
        super(writer);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitSelect_core(FQLParser.Select_coreContext ctx) {
        StringBuilder str = new StringBuilder();

        if (ctx.K_SELECT() != null) {
           
            if (ctx.K_FROM() != null) {

                if (ctx.table_or_subquery().isEmpty()) {
                    str.append(property.get(ctx.join_clause()));
                } else {
                    str.append(ctx.table_or_subquery().stream()
                            .map(property::get).collect(Collectors.joining(", ")));
                }

            }
            
            str.append(".aggregate([");

            if(ctx.result_column().size()>0 && !ctx.result_column().get(0).getText().equals("*")) {
            	
            for(int i=0;i<ctx.result_column().size();i++) {
            	if(i==0) {
            		 str.append("{$project:{");
            	}
            	 if(Optional.ofNullable(ctx.result_column().get(i).expr()).isPresent() && ctx.result_column().get(i).expr().getText().contains("(") &&  ctx.result_column().get(i).expr().getText().contains(")")) {
                 	String fuctionExp=ctx.result_column().get(i).expr().start.getText().toLowerCase();

                 	switch(fuctionExp) {
                 	case "year":
                 	case "month":	
                 	case "hour":
                 	case "week":	
	                 	{
	             			str.append(fuctionExp+":{$").append(fuctionExp).append(":")
	             			.append("'$"+ctx.result_column().get(i).expr().getChild(0).getChild(2).getText()+"'")
	             			.append("}");
	             		}
                 	break;
                 	case "day":{
             			str.append(fuctionExp+":{$").append("dayOfMonth").append(":")
             			.append("'$"+ctx.result_column().get(i).expr().getChild(0).getChild(2).getText()+"'")
             			.append("}");
             			}
                 	break;
                 	case "quarter":{
             			str.append(fuctionExp+":{$substr: [{$add: [{$divide: [{$subtract: [{$month:")
             			.append("'$"+ctx.result_column().get(i).expr().getChild(0).getChild(2).getText()+"'")
             			.append("}, 1]}, 3]}, 1]}, 0, 1]")
             			.append("}");
             			}
                 	break;
                 	case "yearmonth":{
                 		str.append(fuctionExp+":{$dateToString: { format: '%Y-%m', date:")
             			.append("'$"+ctx.result_column().get(i).expr().getChild(0).getChild(2).getText()+"'")
             			.append("}}");
             			}
                 	break;
                 	case "yearweek":{
                 		str.append(fuctionExp+":{$dateToString: { format: '%Y-%V', date:")
             			.append("'$"+ctx.result_column().get(i).expr().getChild(0).getChild(2).getText()+"'")
             			.append("}}");
             			}
                 	break;
                 	case "yearquarter":{
                 		str.append(fuctionExp+":{$concat: [{$toString:{$year:'$"+ctx.result_column().get(i).expr().getChild(0).getChild(2).getText()+"'}}")
                 			.append(",'-',")
                 			.append("{$substr: [{$add: [{$divide: [{$subtract: [{$month:")
                 			.append("'$"+ctx.result_column().get(i).expr().getChild(0).getChild(2).getText()+"'")
                 			.append("}, 1]}, 3]}, 1]}, 0, 1]}")
             			.append("]}");
             			}
                 	break;
                 	case "ltrim":
                 	case "rtrim":{
                 		str.append(fuctionExp+":{$"+fuctionExp+": { input: ");
                 		if(ctx.result_column().get(i).expr().getChild(0).getChild(2).getChild(0).getText().contains("'") || ctx.result_column().get(i).expr().getChild(0).getChild(2).getChild(0).getText().contains("\"")) 
                 			str.append(ctx.result_column().get(i).expr().getChild(0).getChild(2).getText());
                 		else 
             				str.append("'$"+ctx.result_column().get(i).expr().getChild(0).getChild(2).getChild(0).getText()+"'");
                 		
                 		if(ctx.result_column().get(i).expr().getChild(0).getChild(2).getChildCount()>1) {
                 			str.append(",chars:"+ctx.result_column().get(i).expr().getChild(0).getChild(2).getChild(2).getText());
                 		}
             			str.append("}}");
             			}
                 	break;
                 	default:
                 		 str.append(ctx.result_column().get(i).getText()+":1");
                 	}
                 }
            	 else {
            		 str.append(ctx.result_column().get(i).getText()+":1");
            	}
            	
            	str.append((i== ctx.result_column().size()-1)?"}}":","); 
            	
            }   
            }

            if (ctx.K_WHERE() != null) {
            	str.append(str.charAt(str.length()-1)=='}'?",":"")
                	.append("{$match:{")
                        .append(property.get(ctx.expr(0)))
                        .append(ctx.K_ALL() == null ? "}}":"}");
            }


            if (ctx.K_GROUP() != null) {
                str.append(" ")
                        .append(ctx.K_GROUP().getText()).append(" ")
                        .append(ctx.K_BY().getText())
                        .append(" ")
                        .append(property.get(ctx.comma_sep_expr()));

                if (ctx.K_HAVING() != null) {
                    str.append(ctx.K_HAVING().getText())
                            .append(" ")
                            .append(ctx.expr().size() == 2 ? property.get(ctx.expr(1)) : property.get(ctx.expr(0)));
                }

            }
            
            str.append("])");

        } else {
            str.append(property.get(ctx.values_expr()));
        }

        property.put(ctx, str.toString());
        
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitParse(FQLParser.ParseContext ctx) {
        for (FQLParser.Statement_listContext ct : ctx.statement_list()) {
            try {
                writer.write(property.get(ct));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //remove all properties
        property = null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitSimple_select_stmt(FQLParser.Simple_select_stmtContext ctx) {
        StringBuilder str = new StringBuilder();

        Optional.ofNullable(ctx.with_rec_table_expr())
                .map(property::get)
                .ifPresent(x -> str.append(x).append(" "));
       
        str.append(property.get(ctx.select_core()));
        
        Optional.ofNullable(ctx.order_expr())
                .map(property::get)
                .ifPresent(x -> str.append(" ").append(x));
        
        Optional.ofNullable(ctx.limit_expr())
                .map(property::get)
                .ifPresent(x -> str.append(" ").append(x));
        
        property.put(ctx, str.toString());
    

    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitStatement_list(FQLParser.Statement_listContext ctx) {
        String str = ctx.statement().stream().map(property::get)
                .collect(Collectors.joining(";"));
        property.put(ctx, str);
       
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitSelect_stmt(FQLParser.Select_stmtContext ctx) {
        StringBuilder builder = new StringBuilder();

        String recExpr = property.get(ctx.with_rec_table_expr());

        if (recExpr != null) {
            builder.append(recExpr);
        }

        builder.append(" ")
                .append(property.get(ctx.select_core(0)));

        if (ctx.select_core().size() > 1) {
            for (int i = 0; i < ctx.compound_operator().size(); ++i) {
                builder.append(" ")
                        .append(property.get(ctx.compound_operator(i)))
                        .append(" ")
                        .append(property.get(ctx.select_core(i + 1)));
               
            }
        }
        
        Optional.ofNullable(property.get(ctx.order_expr()))
                .ifPresent(x -> builder.append(" ").append(x));

        Optional.ofNullable(property.get(ctx.limit_expr()))
                .ifPresent(x -> builder.append(" ").append(x));

        property.put(ctx, builder.toString());
      

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


        str.append(ctx.K_LIMIT().getText())
                .append(" ")
                .append(property.get(ctx.expr(0)));

        if (ctx.expr().size() == 2) {
            str
                    .append(" ")
                    .append(ctx.K_OFFSET() == null ? "," : ctx.K_OFFSET().getText())
                    .append(" ")
                    .append(property.get(ctx.expr(1)));

        }

        property.put(ctx, str.toString());
   
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitOrder_expr(FQLParser.Order_exprContext ctx) {
        StringBuilder str = new StringBuilder();
        str.append(ctx.K_ORDER().getText())
                .append(" ")
                .append(ctx.K_BY().getText())
                .append(" ")
                .append(ctx.ordering_term().stream().map(property::get)
                        .collect(Collectors.joining(",")));

        property.put(ctx, str.toString());
       
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitCommon_table_expression(FQLParser.Common_table_expressionContext ctx) {
        StringBuilder str = new StringBuilder();

        str.append(property.get(ctx.table_name()))
                .append(" ")
                .append("(")
                .append(ctx.column_name().stream().map(property::get).collect(Collectors.joining(",")))
                .append(") ")
                .append(ctx.K_AS().getText())
                .append("(").append(property.get(ctx.select_stmt())).append(")");
       
        
        property.put(ctx, str.toString());
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitWith_rec_table_expr(FQLParser.With_rec_table_exprContext ctx) {
        StringBuilder str = new StringBuilder();

        str.append(ctx.K_WITH().getText())
                .append(" ")
                .append(ctx.K_RECURSIVE() == null ? "" : ctx.K_RECURSIVE().getText() + " ")
                .append(ctx.common_table_expression()
                        .stream().map(property::get)
                        .collect(Collectors.joining(",")));
        
        property.put(ctx, str.toString());
       
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitValues_expr(FQLParser.Values_exprContext ctx) {
        property.put(ctx, ctx
                .value_expr()
                .stream()
                .map(property::get)
                .collect(Collectors.joining(",")));
      
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitOrdering_term(FQLParser.Ordering_termContext ctx) {
        StringBuilder str = new StringBuilder();

        str.append(property.get(ctx.expr()))
                .append(ctx.K_COLLATE() == null ? "" :
                        ctx.K_COLLATE().getText() + " " + property.get(ctx.collation_name()))
                .append(ctx.K_ASC() == null ? "" : " " + ctx.K_ASC().getText())
                .append(ctx.K_DESC() == null ? "" : " " + ctx.K_DESC().getText());

        property.put(ctx, str.toString());
    
        
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitResult_column(FQLParser.Result_columnContext ctx) {
        StringBuilder str = new StringBuilder();
        String text = ctx.getText();

        if (text.equalsIgnoreCase("*")) {
            str.append("*");
        } else {
            String table = property.get(ctx.table_name());

            if (table == null) {
                str.append(property.get(ctx.expr()))
                        .append(ctx.K_AS() == null ? "" : " " + ctx.K_AS().getText());

                String columnAlias = property.get(ctx.column_alias());

                if (columnAlias != null) {
                    str.append(" ")
                            .append(columnAlias);
                }
            } else {
                str.append(property.get(ctx.table_name()))
                        .append(".")
                        .append("*");
            }
        }

        property.put(ctx, str.toString());
   
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitTable_or_subquery(FQLParser.Table_or_subqueryContext ctx) {
        StringBuilder str = new StringBuilder();

        //'(' select_stmt ')' ( K_AS? table_alias )?
        Optional.ofNullable(property.get(ctx.select_stmt()))
                .ifPresent(x -> {
                    str.append("(")
                            .append(x)
                            .append(")")
                            .append(" ")
                            .append(ctx.K_AS() == null ? "" : ctx.K_AS().getText() + " ");

                    Optional.ofNullable(property.get(ctx.table_alias()))
                            .ifPresent(str::append);
                });


        //( database_name '.' )? table_name ( K_AS? table_alias )?
        Optional.ofNullable(ctx.table_name())
                .map(property::get)
                .ifPresent(x -> {
                    Optional.ofNullable(ctx.database_name())
                            .map(property::get)
                            .ifPresent(y -> str.append(y).append("."));

                    str.append(x)
                            .append(ctx.K_AS() == null ? "" : ctx.K_AS().getText() + " ");
                    Optional.ofNullable(property.get(ctx.table_alias()))
                            .ifPresent(y -> str.append(" ").append(y));

                });

        // '(' ( table_or_subquery ( ',' table_or_subquery )*
        //       | join_clause )
        //   ')' ( K_AS? table_alias )?

        if (!ctx.table_or_subquery().isEmpty()) {
            str.append("(")
                    .append(ctx.table_or_subquery().stream().map(property::get)
                            .collect(Collectors.joining(",")))
                    .append(")")
                    .append(ctx.K_AS() == null ? "" : " " + ctx.K_AS().getText() + " ");

            Optional.ofNullable(property.get(ctx.table_alias()))
                    .ifPresent(str::append);
        }

        Optional.ofNullable(ctx.join_clause())
                .map(property::get)
                .ifPresent(x -> {
                    str
                            .append(x)
                            .append(ctx.K_AS() == null ? "" : " " + ctx.K_AS().getText() + " ");

                    Optional.ofNullable(property.get(ctx.table_alias()))
                            .ifPresent(str::append);

                });


        property.put(ctx, str.toString());
      
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitExpr(FQLParser.ExprContext ctx) {
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
        if(Optional.ofNullable(ctx.binary_operator()).isPresent()) {
        	switch(property.get(ctx.binary_operator()).toLowerCase()) {
        	
        	case "=": {
        				if(property.get(ctx.expr(0)).contains("$and") || property.get(ctx.expr(0)).contains("$or")) {
        					str.append(property.get(ctx.expr(0)).substring(0, property.get(ctx.expr(0)).length() - 2))
        					.append(":").append(property.get(ctx.expr(1))).append("}]");
        				}else {
        					str.append(property.get(ctx.expr(0)))
        					.append(":").append(property.get(ctx.expr(1)));
        				}
        				
        			}
        	break;
        	case "<>": {
        		if(property.get(ctx.expr(0)).contains("$and") || property.get(ctx.expr(0)).contains("$or")) {
        			str.append(property.get(ctx.expr(0)).substring(0, property.get(ctx.expr(0)).length() - 2))
        			.append(":{$ne:").append(property.get(ctx.expr(1))).append("}").append("}]");
        		}
        		else {
        			str.append(property.get(ctx.expr(0))).append(":{$ne:").append(property.get(ctx.expr(1))).append("}");
        		}
        		}
        	break;
        	case ">": {
        		if(property.get(ctx.expr(0)).contains("$and") || property.get(ctx.expr(0)).contains("$or")) {
        			str.append(property.get(ctx.expr(0)).substring(0, property.get(ctx.expr(0)).length() - 2))
        			.append(":{$gt:").append(property.get(ctx.expr(1))).append("}").append("}]");
        		}
        		else {
        			str.append(property.get(ctx.expr(0))).append(":{$gt:").append(property.get(ctx.expr(1))).append("}");
        		}
        		}
        	break;
        	case "<": {
        		if(property.get(ctx.expr(0)).contains("$and") || property.get(ctx.expr(0)).contains("$or")) {
    			str.append(property.get(ctx.expr(0)).substring(0, property.get(ctx.expr(0)).length() - 2))
    			.append(":{$lt:").append(property.get(ctx.expr(1))).append("}").append("}]");
    		}
    		else {
    			str.append(property.get(ctx.expr(0))).append(":{$lt:").append(property.get(ctx.expr(1))).append("}");
    		}
    		}
        	break;
        	case ">=": {
        		if(property.get(ctx.expr(0)).contains("$and") || property.get(ctx.expr(0)).contains("$or")) {
    			str.append(property.get(ctx.expr(0)).substring(0, property.get(ctx.expr(0)).length() - 2))
    			.append(":{$gte:").append(property.get(ctx.expr(1))).append("}").append("}]");
    		}
    		else {
    			str.append(property.get(ctx.expr(0))).append(":{$gte:").append(property.get(ctx.expr(1))).append("}");
    		}
    		}
        	break;
        	case "<=": {
        		if(property.get(ctx.expr(0)).contains("$and") || property.get(ctx.expr(0)).contains("$or")) {
    			str.append(property.get(ctx.expr(0)).substring(0, property.get(ctx.expr(0)).length() - 2))
    			.append(":{$lte:").append(property.get(ctx.expr(1))).append("}").append("}]");
    		}
    		else {
    			str.append(property.get(ctx.expr(0))).append(":{$lte:").append(property.get(ctx.expr(1))).append("}");
    		}
    		}
        	break;
        	case "in": {
        		if(property.get(ctx.expr(0)).contains("$and") || property.get(ctx.expr(0)).contains("$or")) {
        			str.append(property.get(ctx.expr(0)).substring(0, property.get(ctx.expr(0)).length() - 2))
        			.append(":{$in:[").append(property.get(ctx.expr(1))).append("]}").append("}]");
        		}
        		else {
        			str.append(property.get(ctx.expr(0)))
        			.append(":{$in:[").append(property.get(ctx.expr(1)).substring(1,property.get(ctx.expr(1)).length()-1)).append("]}");
        		}
        		}	
        	break;
        	case "like": {
        		if(property.get(ctx.expr(0)).contains("$and") || property.get(ctx.expr(0)).contains("$or")) {
    			str.append(property.get(ctx.expr(0)).substring(0, property.get(ctx.expr(0)).length() - 2))
    			.append(":{$regex:").append(property.get(ctx.expr(1))).append("}").append("}]");
    		}
    		else {
    			str.append(property.get(ctx.expr(0))).append(":{$regex:").append(property.get(ctx.expr(1))).append("}");
    		}
    		}
        	break;
        	case "and": {
        		str.append("$and:[{").append(property.get(ctx.expr(0))).append("},{").append(property.get(ctx.expr(1))).append("}]");
        		}	
        	break;
        	case "or": {
        		str.append("$or:[{").append(property.get(ctx.expr(0))).append("},{").append(property.get(ctx.expr(1))).append("}]");
        		}	
        	break;
        	}
        }

        //func_call_expr
        if(Optional.ofNullable(ctx.func_call_expr()).isPresent()) {
        	String fuctionExp=ctx.func_call_expr().start.getText().toUpperCase();
        	
        	switch(fuctionExp) {
        	case "RTRIM": str.append("rtrim:{$substr:{[").append("'$"+ctx.func_call_expr().getChild(2).getText()+"'").append(",0, 2]}");
        	break;
        	}
        }
               
        
//        expr K_NOT? K_BETWEEN expr K_AND expr
        Optional.ofNullable(ctx.K_BETWEEN())
                .map(TerminalNode::getText)
                .ifPresent(x -> str.append(property.get(ctx.expr(0)))
                        .append(":{")
                        .append(ctx.K_NOT() == null ? "" : "$not:{")
                        .append("$gte:")
                        .append(property.get(ctx.expr(1)))
                        .append(",")
                        .append("$lte:")
                        .append(property.get(ctx.expr(2)))
        				.append(ctx.K_NOT() == null ? "" : "}")
        				.append("}"));
        //expr K_NOT? K_IN ( '(' ( select_stmt
        //                          | expr ( ',' expr )*
        //                          )?
        //                      ')'
        //        | ( database_name '.' )? table_name )
        Optional.ofNullable(ctx.K_IN())
                .map(TerminalNode::getText)
                .ifPresent(x -> {
                	
                    str.append(property.get(ctx.expr(0)))
                            .append(":")
                            .append(ctx.K_NOT() == null ? "{$in:" : "{$nin:");
//                            .append(x).append("");

                    String tableName = property.get(ctx.table_name());

                    if (tableName == null) {
                        str.append("[");
                        Optional.ofNullable(ctx.select_stmt())
                                .map(property::get)
                                .ifPresent(str::append);

                        Optional.ofNullable(ctx.comma_sep_expr())
                                .map(property::get)
                                .ifPresent(str::append);

                        str.append("]}");

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
        
        property.put(ctx, str.toString());
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitKeyword(FQLParser.KeywordContext ctx) {
        property.put(ctx, ctx.getText());
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitDb_tbl_col_expr(FQLParser.Db_tbl_col_exprContext ctx) {
        StringBuilder str = new StringBuilder();

        Optional.ofNullable(ctx.database_name())
                .map(property::get)
                .ifPresent(x -> str.append(x).append(".")
                );

        Optional.ofNullable(ctx.table_name())
                .map(property::get)
                .ifPresent(x -> str.append(x).append("."));

        str.append(property.get(ctx.column_name()));

        property.put(ctx, str.toString());
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitFunc_call_expr(FQLParser.Func_call_exprContext ctx) {
        StringBuilder str = new StringBuilder();
        
        str.append(property.get(ctx.function_name()))
                .append("(");
        String commaSep = property.get(ctx.comma_sep_expr());

        if (commaSep != null) {
            str.append(ctx.K_DISTINCT() == null ? "" : ctx.K_DISTINCT().getText() + " ")
                    .append(commaSep);
        } else if (ctx.getText().contains("*")) {
            str.append("*");
        }

        str.append(")");
       
        property.put(ctx, str.toString());
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitColumn_alias(FQLParser.Column_aliasContext ctx) {
        property.put(ctx, ctx.getText());
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitName(FQLParser.NameContext ctx) {
        property.put(ctx, ctx.getText());
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitTable_alias(FQLParser.Table_aliasContext ctx) {
        property.put(ctx, ctx.getText());
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitFunction_name(FQLParser.Function_nameContext ctx) {
        property.put(ctx, ctx.getText());
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitIndex_name(FQLParser.Index_nameContext ctx) {
        property.put(ctx, ctx.getText());
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitTable_name(FQLParser.Table_nameContext ctx) {
        property.put(ctx, ctx.getText());
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitDatabase_name(FQLParser.Database_nameContext ctx) {
        property.put(ctx, ctx.getText());
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitColumn_name(FQLParser.Column_nameContext ctx) {
        property.put(ctx, ctx.getText());
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitCollation_name(FQLParser.Collation_nameContext ctx) {
        property.put(ctx, ctx.getText());
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitUnary_operator(FQLParser.Unary_operatorContext ctx) {
        property.put(ctx, ctx.getText());
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitNull_check_op(FQLParser.Null_check_opContext ctx) {
        property.put(ctx, ctx.getText());
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitStr_match_expr(FQLParser.Str_match_exprContext ctx) {
        property.put(ctx, ctx.getText());
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitComma_sep_expr(FQLParser.Comma_sep_exprContext ctx) {
        property.put(ctx, ctx.expr().stream().map(property::get).collect(Collectors.joining(",")));
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitBinary_operator(FQLParser.Binary_operatorContext ctx) {
        property.put(ctx, ctx.getText());
    }


    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitAny_name(FQLParser.Any_nameContext ctx) {
        property.put(ctx, ctx.getText());
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitLiteral(FQLParser.LiteralContext ctx) {
        property.put(ctx, ctx.getText());
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitValue_expr(FQLParser.Value_exprContext ctx) {
        String str = "(" +
                property.get(ctx.comma_sep_expr()) +
                ")";

        property.put(ctx, str);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitStatement(FQLParser.StatementContext ctx) {
        String stmt = Optional.ofNullable(property.get(ctx.select_stmt()))
                .orElse(property.get(ctx.simple_select_stmt()));


        property.put(ctx, stmt);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation does nothing.</p>
     *
     * @param ctx
     */
    @Override
    public void exitCompound_operator(FQLParser.Compound_operatorContext ctx) {
        property.put(ctx, ctx.getText());
    }
}
