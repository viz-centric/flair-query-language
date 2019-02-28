package com.flair.bi.compiler;

import com.flair.bi.grammar.FQLLexer;
import com.flair.bi.grammar.FQLParser;
import com.flair.bi.grammar.FQLParserListener;
import com.project.bi.exceptions.CompilationException;
import com.project.bi.query.FlairCompiler;
import com.project.bi.query.FlairQuery;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.Writer;

/**
 * Abstract flair compiler that implements base logic of compilation
 */
public abstract class AbstractFlairCompiler implements FlairCompiler {

    private FQLParserListener listener;

    /**
     * Compiles input to write to to the writer
     *
     * @param query  input used for compiling
     * @param writer writer that receives the result of compilation
     * @throws CompilationException if compilation fails
     */
    @Override
    public void compile(FlairQuery query, Writer writer) throws CompilationException {
        try {
            ANTLRInputStream input = new ANTLRInputStream(query.getStatement());
            FQLLexer lexer = new FQLLexer(input);

            CommonTokenStream tokens = new CommonTokenStream(lexer);

            FQLParser parser = new FQLParser(tokens);

            ParseTree tree = parser.parse(); // begin parsing at init rule
            // Create a generic parse tree walker that can trigger callbacks
            ParseTreeWalker walker = new ParseTreeWalker();
            // Walk the tree created during the parse, trigger callbacks
            listener = getListener(writer);
            walker.walk(listener, tree);
        } catch (Exception e) {
            throw new CompilationException(e);
        }
    }

    public FQLParserListener getListener() {
        return listener;
    }

    protected abstract FQLParserListener getListener(Writer writer);
}
