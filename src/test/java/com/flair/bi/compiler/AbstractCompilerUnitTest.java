package com.flair.bi.compiler;

import java.io.StringWriter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.project.bi.exceptions.CompilationException;
import com.project.bi.query.FlairCompiler;
import com.project.bi.query.FlairQuery;

public abstract class AbstractCompilerUnitTest<T extends FlairCompiler> {

	protected T sut;

	@Before
	public void AbstractCompilerUnitTestInit() {
		sut = configureCompiler();
	}

	protected abstract T configureCompiler();

	protected void stmtTest(String stmt) throws CompilationException {
		stmtTest(stmt, stmt);
	}

	protected void stmtTest(String stmt, String result) throws CompilationException {
		final FlairQuery query = new FlairQuery(stmt, false);
		final StringWriter writer = new StringWriter();
		sut.compile(query, writer);

		Assert.assertEquals(result, writer.toString());
	}

	@Test(expected = CompilationException.class)
	public void dropTableStatementShouldFail() throws CompilationException {
		stmtTest("drop table transactions");
	}

	@Test(expected = CompilationException.class)
	public void dropTableAfterSelectShouldFail() throws CompilationException {
		stmtTest("select * from transactions; drop table transactions");
	}

	@Test(expected = CompilationException.class)
	public void updateTableStatementShouldFail() throws CompilationException {
		stmtTest("UPDATE table_name SET column1 = value1, column2 = value2");
	}

	@Test(expected = CompilationException.class)
	public void deleteStatementShouldFail() throws CompilationException {
		stmtTest("DELETE FROM table_name WHERE condition;");
	}

}
