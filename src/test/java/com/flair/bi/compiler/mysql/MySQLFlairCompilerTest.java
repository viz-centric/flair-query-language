package com.flair.bi.compiler.mysql;

import com.flair.bi.compiler.AbstractSqlCompilerUnitTest;
import com.project.bi.exceptions.CompilationException;
import org.junit.Test;

public class MySQLFlairCompilerTest extends AbstractSqlCompilerUnitTest<MySQLFlairCompiler> {

	@Override
	protected MySQLFlairCompiler configureCompiler() {
		return new MySQLFlairCompiler();
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
		stmtTest(
				"select * from transactions group by data order by price asc;select data where price between 10 and 200");
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
	public void dateFormatYear() throws CompilationException {
		stmtTest("select year('2019-01-09 21:00:00') from transactions where price = 500",
				"select EXTRACT(year FROM STR_TO_DATE('2019-01-09 21:00:00','%Y-%m-%d H:%i:%s')) from transactions where price = 500");
	}

	@Test
	public void dateFormatYearWeek() throws CompilationException {
		stmtTest("select yearweek('2019-01-09 21:00:00') from transactions where price = 500",
				"select CONCAT(EXTRACT(YEAR FROM STR_TO_DATE('2019-01-09 21:00:00','%Y-%m-%d H:%i:%s')), '-', EXTRACT(WEEK FROM STR_TO_DATE('2019-01-09 21:00:00','%Y-%m-%d H:%i:%s'))) from transactions where price = 500");
	}

	@Test
	public void dateFormatYearQuarter() throws CompilationException {
		stmtTest("select yearquarter('2019-01-09 21:00:00') from transactions where price = 500",
				"select CONCAT(EXTRACT(YEAR FROM STR_TO_DATE('2019-01-09 21:00:00','%Y-%m-%d H:%i:%s')), '-', EXTRACT(QUARTER FROM STR_TO_DATE('2019-01-09 21:00:00','%Y-%m-%d H:%i:%s'))) from transactions where price = 500");
	}

	@Test
	public void dateFormatYearMonth() throws CompilationException {
		stmtTest("select yearmonth('2019-01-09 21:00:00') from transactions where price = 500",
				"select CONCAT(EXTRACT(YEAR FROM STR_TO_DATE('2019-01-09 21:00:00','%Y-%m-%d H:%i:%s')), '-', EXTRACT(MONTH FROM STR_TO_DATE('2019-01-09 21:00:00','%Y-%m-%d H:%i:%s'))) from transactions where price = 500");
	}

	@Test
	public void dateFormatQuarter() throws CompilationException {
		stmtTest("select quarter('2019-01-09 21:00:00') from transactions where price = 500",
				"select EXTRACT(quarter FROM STR_TO_DATE('2019-01-09 21:00:00','%Y-%m-%d H:%i:%s')) from transactions where price = 500");
	}

	@Test
	public void dateFormatMonth() throws CompilationException {
		stmtTest("select month('2019-01-09 21:00:00') from transactions where price = 500",
				"select EXTRACT(month FROM STR_TO_DATE('2019-01-09 21:00:00','%Y-%m-%d H:%i:%s')) from transactions where price = 500");
	}

	@Test
	public void dateFormatWeek() throws CompilationException {
		stmtTest("select week('2019-01-09 21:00:00') from transactions where price = 500",
				"select EXTRACT(week FROM STR_TO_DATE('2019-01-09 21:00:00','%Y-%m-%d H:%i:%s')) from transactions where price = 500");
	}

	@Test
	public void dateFormatDay() throws CompilationException {
		stmtTest("select day('2019-01-09 21:00:00') from transactions where price = 500",
				"select EXTRACT(day FROM STR_TO_DATE('2019-01-09 21:00:00','%Y-%m-%d H:%i:%s')) from transactions where price = 500");
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
	public void likeExpressionPercentTurnsToStar() throws CompilationException {
		stmtTest("select * from transactions where data like '%pera%'",
				"select * from transactions where data like '%pera%'");
	}

	@Test
	public void selectWithQuotes() throws CompilationException {
		stmtTest("SELECT replace(product_name,'''','''Women''','Aster''isk','''More''test''') FROM ecommerce",
				"SELECT replace(product_name,'''','''Women''','Aster''isk','''More''test''') FROM ecommerce");
	}

	@Test
	public void randTest() throws CompilationException {
		stmtTest("select sum(price * random()) as price from transactions where data like '%pera%'",
				"select sum(price * random()) as price from transactions where data like '%pera%'");
	}

	@Test
	public void showTables() throws CompilationException {
		stmtTest("show tables", "SHOW TABLES");
	}

	@Test
	public void showTablesLike() throws CompilationException {
		stmtTest("show tables like '%pera%'", "SHOW TABLES LIKE '%pera%'");
	}

	@Test
	public void showTablesLikeLimit() throws CompilationException {
		stmtTest("show tables like '%pera%' limit 4", "SHOW TABLES LIKE '%pera%'");
	}

	@Test
	public void showTablesLimit() throws CompilationException {
		stmtTest("show tables limit 5", "SHOW TABLES");
	}

	@Test
	public void parseDateFunction() throws CompilationException {
		stmtTest("select datefmt(custom_field, '%y %M %d') from my_table where a = 1",
				"select date_format(CAST(custom_field AS TIMESTAMP), '%y %M %d') from my_table where a = 1");
	}

	@Test
	public void parseAggregationFunction() throws CompilationException {
		stmtTest("select column1, count(column2) from my_table where a = 1",
				"select column1, count(column2) from my_table where a = 1");
	}

	@Test
	public void parseFlairTypeCast() throws CompilationException {
		stmtTest(
				"SELECT updated_on as updated_on,COUNT(transaction_quantity) as transaction_quantity FROM shipment3 WHERE updated_on >= __FLAIR_CAST(timestamp, '2019-11-03 22:00:00') GROUP BY updated_on ORDER BY transaction_quantity DESC,updated_on DESC LIMIT 20 OFFSET 0",
				"SELECT updated_on as updated_on, COUNT(transaction_quantity) as transaction_quantity FROM shipment3 WHERE updated_on >= STR_TO_DATE('2019-11-03 22:00:00','%Y-%m-%d H:%i:%s') GROUP BY updated_on ORDER BY transaction_quantity DESC,updated_on DESC LIMIT 20 OFFSET 0");
	}

	@Test
	public void parseFlairTypeCastLike() throws CompilationException {
		stmtTest(
				"SELECT updated_on as updated_on,COUNT(transaction_quantity) as transaction_quantity FROM shipment3 WHERE UPPER(__FLAIR_CAST(flair_string, product_id)) LIKE UPPER('%123%') GROUP BY updated_on ORDER BY transaction_quantity DESC,updated_on DESC LIMIT 20 OFFSET 0",
				"SELECT updated_on as updated_on, COUNT(transaction_quantity) as transaction_quantity FROM shipment3 WHERE UPPER(CAST(product_id as CHAR)) LIKE UPPER('%123%') GROUP BY updated_on ORDER BY transaction_quantity DESC,updated_on DESC LIMIT 20 OFFSET 0");
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

	@Test
	public void parseFlairIntervalOperation() throws CompilationException {
		stmtTest(
				"SELECT updated_on as updated_on, COUNT(transaction_quantity) as transaction_quantity FROM shipment3 WHERE updated_on BETWEEN NOW() AND __FLAIR_INTERVAL_OPERATION(NOW(), '-', '4 hours') GROUP BY updated_on ORDER BY transaction_quantity DESC,updated_on DESC LIMIT 20 OFFSET 0",
				"SELECT updated_on as updated_on, COUNT(transaction_quantity) as transaction_quantity FROM shipment3 WHERE updated_on BETWEEN NOW() AND (NOW() - interval 4 hour) GROUP BY updated_on ORDER BY transaction_quantity DESC,updated_on DESC LIMIT 20 OFFSET 0");
	}

	@Test
	public void parseFlairIntervalAndCastOperation() throws CompilationException {
		stmtTest(
				"SELECT updated_on as updated_on, COUNT(transaction_quantity) as transaction_quantity FROM shipment3 WHERE updated_on BETWEEN NOW() AND __FLAIR_INTERVAL_OPERATION(__FLAIR_CAST(timestamp, '2019-11-03 22:00:00'), '-', '4 hours') GROUP BY updated_on ORDER BY transaction_quantity DESC,updated_on DESC LIMIT 20 OFFSET 0",
				"SELECT updated_on as updated_on, COUNT(transaction_quantity) as transaction_quantity FROM shipment3 WHERE updated_on BETWEEN NOW() AND (STR_TO_DATE('2019-11-03 22:00:00','%Y-%m-%d H:%i:%s') - interval 4 hour) GROUP BY updated_on ORDER BY transaction_quantity DESC,updated_on DESC LIMIT 20 OFFSET 0");
	}

	@Test
	public void parseHaving() throws CompilationException {
		stmtTest(
				"SELECT order_date as order_date, COUNT(order_item_quantity) as order_item_quantity FROM ecommerce WHERE order_date >= __FLAIR_INTERVAL_OPERATION(NOW(), '-', '6 hours') GROUP BY order_date HAVING COUNT(order_item_quantity) = 1 ORDER BY order_item_quantity DESC,order_date DESC LIMIT 20 OFFSET 0",
				"SELECT order_date as order_date, COUNT(order_item_quantity) as order_item_quantity FROM ecommerce WHERE order_date >= (NOW() - interval 6 hour) GROUP BY order_date HAVING COUNT(order_item_quantity) = 1 ORDER BY order_item_quantity DESC,order_date DESC LIMIT 20 OFFSET 0");
	}

	@Test
	public void parseWhereIn() throws CompilationException {
		stmtTest(
				"SELECT customer_city as customer_city,COUNT(order_item_quantity) as order_item_quantity FROM ecommerce WHERE product_id IN (1073) GROUP BY customer_city ORDER BY order_item_quantity DESC,customer_city DESC LIMIT 20 OFFSET 0",
				"SELECT customer_city as customer_city, COUNT(order_item_quantity) as order_item_quantity FROM ecommerce WHERE product_id IN (1073) GROUP BY customer_city ORDER BY order_item_quantity DESC,customer_city DESC LIMIT 20 OFFSET 0");
	}

	@Test
	public void parseWhereInLongExpression() throws CompilationException {
		stmtTest(
				"SELECT customer_city as customer_city FROM ecommerce WHERE product_id IN (__FLAIR_CAST(timestamp, '2019-11-03 22:00:00'), 1231) GROUP BY customer_city",
				"SELECT customer_city as customer_city FROM ecommerce WHERE product_id IN (STR_TO_DATE('2019-11-03 22:00:00','%Y-%m-%d H:%i:%s'),1231) GROUP BY customer_city");
	}

	@Test
	public void parseWhereInOneCondition() throws CompilationException {
		stmtTest(
				"SELECT customer_city as customer_city FROM ecommerce WHERE product_id IN ( __FLAIR_CAST(timestamp, 121) ) GROUP BY customer_city",
				"SELECT customer_city as customer_city FROM ecommerce WHERE product_id IN (STR_TO_DATE(121,'%Y-%m-%d H:%i:%s')) GROUP BY customer_city");
	}

	@Test
	public void parseFlairCast() throws CompilationException {
		stmtTest(
				"SELECT updated_on as updated_on FROM shipment3 WHERE column1 = __FLAIR_CAST(bigint, product_id)",
				"SELECT updated_on as updated_on FROM shipment3 WHERE column1 = CAST(product_id as bigint)");
	}

	@Test
	public void selectHavingWithInnerSelect() throws CompilationException {
		stmtTest("SELECT product_name as product_name, COUNT(product_price) as product_price FROM Ecommerce GROUP BY product_name HAVING COUNT(product_price) > (SELECT avg(transaction_quantity) as avg_quantity FROM order_summary WHERE inserted_on BETWEEN __FLAIR_INTERVAL_OPERATION(NOW(), '-', '4 hours') AND NOW()) LIMIT 20",
				"SELECT product_name as product_name, COUNT(product_price) as product_price FROM Ecommerce GROUP BY product_name HAVING COUNT(product_price) > ( SELECT avg(transaction_quantity) as avg_quantity FROM order_summary WHERE inserted_on BETWEEN (NOW() - interval 4 hour) AND NOW()) LIMIT 20");
	}

	@Test
	public void flairTruncWithTimestamp() throws CompilationException {
		stmtTest("select __FLAIR_TRUNC(inserted_on, timestamp) from transactions where price = 500 and __FLAIR_TRUNC(udpated_on, timestamp) > 0",
				"select inserted_on from transactions where price = 500 and udpated_on > 0");
	}

	@Test
	public void flairTruncWithVarchar() throws CompilationException {
		stmtTest("select __FLAIR_TRUNC(inserted_on, varchar) from transactions where price = 500 and __FLAIR_TRUNC(udpated_on, int) > 0",
				"select inserted_on from transactions where price = 500 and udpated_on > 0");
	}

}
