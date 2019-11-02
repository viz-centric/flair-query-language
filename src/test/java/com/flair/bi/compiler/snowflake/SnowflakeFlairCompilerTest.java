package com.flair.bi.compiler.snowflake;

import com.flair.bi.compiler.postgres.PostgresFlairCompiler;
import com.project.bi.exceptions.CompilationException;
import com.project.bi.query.FlairQuery;
import org.junit.Assert;
import org.junit.Test;

import java.io.StringWriter;

/**
 * Unit tests for {@link PostgresFlairCompiler}
 */
public class SnowflakeFlairCompilerTest {

    private SnowflakeFlairCompiler compiler = new SnowflakeFlairCompiler();

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
    public void testSimpleSelectStmt() throws CompilationException {
        stmtTest("Select data from transactions");
    }

    @Test
    public void testSelectStmtGroupBy() throws CompilationException {
        stmtTest("Select data, sum(price) as price from transactions group by data");
    }

    @Test
    public void testSelectStmtGroupByOrderBy1() throws CompilationException {
        stmtTest("Select data, sum(price) as price from transactions group by data order by price asc");
    }

    @Test
    public void testSelectStmtGroupByOrderBy2() throws CompilationException {
        stmtTest("Select data, sum(price) as price from transactions group by data order by price desc");
    }

    @Test
    public void testSelectStmtOrderBy1() throws CompilationException {
        stmtTest("Select data, sum(price) as price from transactions order by price asc");
    }

    @Test
    public void testSelectStmtOrderBy2() throws CompilationException {
        stmtTest("Select data, sum(price) as price from transactions order by price desc");
    }

    @Test
    public void multipleTestCaseSimple() throws CompilationException {
        stmtTest("select * from transactions;select data from test");
    }

    @Test
    public void multipleTestCaseComplex() throws CompilationException {
        stmtTest("select * from transactions group by data order by price asc;select data where price between 10 and 200");
    }

    @Test
    public void whereTestCase1() throws CompilationException {
        stmtTest("select * from transactions where data in ('a','b')");
    }

    @Test
    public void whereTestCase2() throws CompilationException {
        stmtTest("select * from transactions where price between 100 and 200");
    }

    @Test
    public void whereTestCase3() throws CompilationException {
        stmtTest("select * from transactions where price between 100 and 200 and data in ('1','2')");
    }

    @Test
    public void whereTestCase4() throws CompilationException {
        stmtTest("select * from transactions where price between 100 and 200 or data in ('1','2')");
    }

    @Test
    public void whereTestCase5() throws CompilationException {
        stmtTest("select * from transactions where price = 500");
    }

    @Test
    public void whereTestCase6() throws CompilationException {
        stmtTest("select * from transactions where price != 500");
    }

    @Test
    public void whereTestCase7() throws CompilationException {
        stmtTest("select * from transactions where data not in ('1','2')");
    }

    @Test
    public void whereTestCase8() throws CompilationException {
        stmtTest("select * from transactions where data like '%pera%'");
    }

    @Test
    public void randTest() throws CompilationException {
        stmtTest("select sum(price * rand()) as price from transactions where data like '%pera%'",
                "select sum(price * random()) as price from transactions where data like '%pera%'");

        stmtTest("select sum(price * random()) as price from transactions where data like '%pera%'",
                "select sum(price * random()) as price from transactions where data like '%pera%'");
    }

    @Test
    public void selectWithQuotes() throws CompilationException {
        stmtTest("SELECT replace(product_name,'''','''Women''','Aster''isk','''More''test''') FROM ecommerce",
                "SELECT replace(product_name,'''','''Women''','Aster''isk','''More''test''') FROM ecommerce");
    }

    @Test
    public void selectHaving() throws CompilationException {
        stmtTest("SELECT product_name as product_name, COUNT(product_price) as product_price FROM Ecommerce GROUP BY product_name HAVING (COUNT(product_price) > 1000) LIMIT 20",
                "SELECT product_name as product_name, COUNT(product_price) as product_price FROM Ecommerce GROUP BY product_name HAVING (COUNT(product_price)>1000) LIMIT 20");
    }

    @Test
    public void selectHavingWithoutBrackets() throws CompilationException {
        stmtTest("SELECT product_name as product_name, COUNT(product_price) as product_price FROM Ecommerce GROUP BY product_name HAVING COUNT(product_price) > 1000 LIMIT 20",
                "SELECT product_name as product_name, COUNT(product_price) as product_price FROM Ecommerce GROUP BY product_name HAVING COUNT(product_price)>1000 LIMIT 20");
    }

    @Test
    public void showTables() throws CompilationException {
        stmtTest("show tables",
                "SHOW TABLES");
    }

    @Test
    public void showTablesLike() throws CompilationException {
        stmtTest("show tables like '%pera%'",
                "SHOW TABLES LIKE '%pera%'");
    }

    @Test
    public void showTablesLikeLimit() throws CompilationException {
        stmtTest("show tables like '%pera%' limit 4",
                "SHOW TABLES LIKE '%pera%'");
    }

    @Test
    public void showTablesLimit() throws CompilationException {
        stmtTest("show tables limit 5",
                "SHOW TABLES");
    }

    @Test
    public void parseDateFunction() throws CompilationException {
        stmtTest("select datefmt(custom_field, 'yyyy-MM-dd') from my_table where a = 1",
                "select to_char(custom_field::timestamp, 'yyyy-MM-dd') from my_table where a = 1");
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

    @Test
    public void parseLimitAndOffset() throws CompilationException {
        stmtTest("select column1 from my_table where a = 1 limit 10 offset 53",
                "select column1 from my_table where a = 1 limit 10 offset 53");
    }


}