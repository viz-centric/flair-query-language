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
        FlairQuery query = new FlairQuery();

        query.setStatement(stmt);
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
    public void showTablesLikeLimit() throws CompilationException {
        stmtTest("show tables like '%pera%' limit 4",
                "SELECT table_name FROM all_tables WHERE table_name LIKE '%pera%' FETCH NEXT 4 ROWS ONLY");
    }
}
