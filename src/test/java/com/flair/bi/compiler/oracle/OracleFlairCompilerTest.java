package com.flair.bi.compiler.oracle;

import com.project.bi.exceptions.CompilationException;
import com.project.bi.query.FlairQuery;
import org.junit.Assert;
import org.junit.Test;

import java.io.StringWriter;

public class OracleFlairCompilerTest {


    private OracleFlairCompiler compiler = new OracleFlairCompiler();

    private void stmtTest(String stmt) throws CompilationException {
        stmtTest(stmt, stmt);
    }

    private void stmtTest(String stmt, String result) throws CompilationException {
        FlairQuery query = new FlairQuery(stmt, false);
        StringWriter writer = new StringWriter();
        compiler.compile(query, writer);


        Assert.assertEquals(result, writer.toString());
    }

    @Test
    public void showTables() throws CompilationException {
        stmtTest("show tables",
                "SELECT table_name FROM all_tables");
    }

    @Test
    public void showTablesLike() throws CompilationException {
        stmtTest("show tables like '%pera%'",
                "SELECT table_name FROM all_tables WHERE table_name LIKE '%pera%'");
    }

    @Test
    public void showTablesLimit() throws CompilationException {
        stmtTest("show tables limit 4",
                "SELECT table_name FROM all_tables FETCH NEXT 4 ROWS ONLY");
    }

    @Test
    public void selectWithLimit() throws CompilationException {
        stmtTest("SELECT * FROM ORDERS LIMIT 1",
                "SELECT * FROM ORDERS FETCH NEXT 1 ROWS ONLY");
    }

    @Test
    public void selectWithLimitAndOffset() throws CompilationException {
        stmtTest("SELECT * FROM ORDERS LIMIT 1 OFFSET 5",
                "SELECT * FROM ORDERS OFFSET 5 ROWS FETCH NEXT 1 ROWS ONLY");
    }

    @Test
    public void showTablesLikeLimit() throws CompilationException {
        stmtTest("show tables like '%pera%' limit 4",
                "SELECT table_name FROM all_tables WHERE table_name LIKE '%pera%' FETCH NEXT 4 ROWS ONLY");
    }

    @Test
    public void parseDateFunction() throws CompilationException {
        stmtTest("select datefmt(custom_field, 'yyyy-MM-dd') from my_table where a = 1",
                "select to_char(custom_field, 'yyyy-MM-dd') from my_table where a = 1");
    }

    @Test
    public void parseAggregationFunction() throws CompilationException {
        stmtTest("select column1, count(column2) from my_table where a = 1",
                "select column1, count(column2) from my_table where a = 1");
    }

    @Test
    public void parseDistinctCountFunction() throws CompilationException {
        stmtTest("select column1, distinct_count(column2) from my_table where a = 1",
                "select column1, count(distinct column2) from my_table where a = 1");
    }
}
