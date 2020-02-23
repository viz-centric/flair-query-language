package com.flair.bi.compiler;

import com.flair.bi.compiler.utils.SqlTimeConverter;
import com.flair.bi.grammar.FQLParser;
import com.flair.bi.grammar.FQLParser.Value_exprContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public abstract class SQLListener extends AbstractFQLListener {

    public enum ParseResult {
        SELECT_COLUMNS
    }

	protected final Map<ParseResult, List<String>> parseResults = new ConcurrentHashMap<>();

	public SQLListener(Writer writer) {
        super(writer);
    }

	public List<String> getParseResults(ParseResult parseResult) {
		return parseResults.get(parseResult);
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
	        List<String> colList = ctx.result_column().stream()
			        .filter(i -> i.column_alias() != null || i.expr() != null)
			        .map(i -> {
				        if (i.column_alias() != null) {
					        return i.column_alias().getText();
				        }
				        return i.expr().getText();
			        })
			        .collect(Collectors.toList());

	        parseResults.put(ParseResult.SELECT_COLUMNS, colList);

	        str.append(ctx.K_SELECT().getText())
                    .append(" ")
                    .append(ctx.K_DISTINCT() == null ? "" : ctx.K_DISTINCT().getText() + " ")
                    .append(ctx.K_ALL() == null ? "" : ctx.K_ALL().getText() + " ")
                    .append(ctx.result_column().stream()
                            .map(property::get).collect(Collectors.joining(", ")));

            if (ctx.K_FROM() != null) {
                str.append(" ")
                        .append(ctx.K_FROM().getText())
                        .append(" ");

                if (ctx.table_or_subquery().isEmpty()) {
                    str.append(property.get(ctx.join_clause()));
                } else {
                    str.append(ctx.table_or_subquery().stream()
                            .map(property::get).collect(Collectors.joining(", ")));
                }

            }

            if (ctx.K_WHERE() != null) {
                str.append(" ")
                        .append(ctx.K_WHERE().getText())
                        .append(" ")
                        .append(property.get(ctx.expr(0)));
            }


            if (ctx.K_GROUP() != null) {
                str.append(" ")
                        .append(ctx.K_GROUP().getText()).append(" ")
                        .append(ctx.K_BY().getText())
                        .append(" ")
                        .append(property.get(ctx.comma_sep_expr()));

                if (ctx.K_HAVING() != null) {
                    str.append(" ")
                            .append(ctx.K_HAVING().getText())
                            .append(" ")
                            .append(ctx.expr().size() == 2 ? property.get(ctx.expr(1)) :
                                    ctx.expr(0).getText());
                }

            }

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

    @Override
    public void exitError(FQLParser.ErrorContext ctx) {
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

    @Override
    public void exitDescribe_stmt_like(FQLParser.Describe_stmt_likeContext ctx) {
        property.put(ctx, ctx.getText());
    }

    @Override
    public void exitDescribe_stmt_limit(FQLParser.Describe_stmt_limitContext ctx) {
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
        for(Value_exprContext c:ctx.value_expr()) {
        	
        }
        
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
                            .ifPresent(y -> str.append(y).append(". "));

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

    @Override
    public void exitJoin_clause(FQLParser.Join_clauseContext ctx) {
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
        Optional.ofNullable(ctx.binary_operator())
                .map(property::get)
                .ifPresent(x -> str
                        .append(property.get(ctx.expr(0)))
                        .append(" ")
                        .append(x)
                        .append(" ")
                        .append(property.get(ctx.expr(1))));

        //func_call_expr
        Optional.ofNullable(ctx.func_call_expr())
                .map(property::get)
                .ifPresent(str::append);
        
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
        Optional<String> funcName = getFunctionName(ctx);
        if (funcName.isPresent()) {
            Optional<String> result = Optional.empty();
            if ("__FLAIR_CAST".equalsIgnoreCase(funcName.get())) {
                result = Optional.ofNullable(onFlairCastFunction(ctx));
            } else if ("__FLAIR_INTERVAL_OPERATION".equalsIgnoreCase(funcName.get())) {
                result = Optional.ofNullable(onFlairIntervalOperationFunction(ctx));
            } else if ("__FLAIR_NOW".equalsIgnoreCase(funcName.get())
                || "NOW".equalsIgnoreCase(funcName.get())) {
                result = Optional.ofNullable(onFlairNowFunction(ctx));
            }
            if (result.isPresent()) {
                property.put(ctx, result.get());
                return;
            }
        }

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

    protected String onFlairNowFunction(FQLParser.Func_call_exprContext ctx) {
        return "NOW(" + (ctx.comma_sep_expr() != null ? ctx.comma_sep_expr().getText() : "") + ")";
    }

    protected Optional<String> getFunctionName(FQLParser.Func_call_exprContext ctx) {
        if (ctx.function_name() != null
                && ctx.function_name().any_name() != null
                && ctx.function_name().any_name().IDENTIFIER() != null
                && ctx.function_name().any_name().IDENTIFIER().getSymbol() != null) {
            return Optional.ofNullable(ctx.function_name().any_name().IDENTIFIER().getSymbol().getText());
        }
        return Optional.empty();
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

    @Override
    public void exitJoin_constraint(FQLParser.Join_constraintContext ctx) {
        property.put(ctx, ctx.getText());
    }

    @Override
    public void exitJoin_operator(FQLParser.Join_operatorContext ctx) {
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
        String stmt = Optional.ofNullable(property.get(ctx.describe_stmt()))
                .orElse(Optional.ofNullable(property.get(ctx.select_stmt()))
                .orElse(property.get(ctx.simple_select_stmt())));

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

    @Override
    public void exitDescribe_stmt(FQLParser.Describe_stmtContext ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("SHOW TABLES");

        if (ctx.describe_stmt_like() != null) {
            sb.append(" LIKE ")
                    .append(ctx.describe_stmt_like().expr().getText());
        }

        property.put(ctx, sb.toString());
    }

    protected String onFlairCastFunction(FQLParser.Func_call_exprContext func_call_expr) {
        StringBuilder str = new StringBuilder();
        String dataType = func_call_expr.getChild(2).getChild(0).getText();
        ParseTree fieldName = func_call_expr.getChild(2).getChild(2);
        if (asList("timestamp", "datetime", "date").contains(dataType.toLowerCase())) {
            str.append("to_timestamp(")
                    .append(property.get(fieldName) != null ? property.get(fieldName) : fieldName.getText())
                    .append(",")
                    .append("'YYYY-MM-DDTHH24:MI:SS.FF3Z'")
                    .append(")");
        } else {
            str.append("CAST(")
                    .append(property.get(fieldName) != null ? property.get(fieldName) : fieldName.getText())
                    .append(" as TEXT)");
        }
        return str.toString();
    }

    protected String onFlairIntervalOperationFunction(FQLParser.Func_call_exprContext func_call_expr) {
        FQLParser.ExprContext firstArgument = func_call_expr.comma_sep_expr().expr(0);
        String firstArgumentText = property.get(firstArgument) != null ? property.get(firstArgument) : firstArgument.getText();
        String operator = func_call_expr.comma_sep_expr().expr(1).literal().STRING_LITERAL().getSymbol().getText();
        String secondArgument = func_call_expr.comma_sep_expr().expr(2).getText();
        String rawOperator = getRawStringValue(operator);
        String rawSecondArgument = getRawStringValue(secondArgument);
        String letter = rawSecondArgument.split(" ")[1];
        String hourOrDays = getHourOrDaysFromLetter(letter);
        String number = rawSecondArgument.split(" ")[0];

        return composeFlairInterval(firstArgumentText, rawOperator, hourOrDays, number);
    }

    protected String composeFlairInterval(String expression, String operator, String hourOrDays, String number) {
        return "(" +
                expression +
                " " +
                operator +
                " " + "interval '" + number + " " + hourOrDays + "'" +
                ")";
    }

    protected String getRawStringValue(String operator) {
        return operator.substring(1, operator.length() - 1);
    }

    protected String getHourOrDaysFromLetter(String letter) {
        return SqlTimeConverter.toPlural(letter);
    }

}
