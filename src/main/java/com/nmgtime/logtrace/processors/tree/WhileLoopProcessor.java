package com.nmgtime.logtrace.processors.tree;

import com.nmgtime.logtrace.processors.ProcessorFactory;
import com.nmgtime.logtrace.processors.TreeProcessor;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

/**
 * A recursive processor for {@link JCTree} of kind {@link com.sun.source.tree.Tree.Kind#WHILE_LOOP}.
 * eg:
 * <pre>
 *     while ( condition )
 *       statement
 * </pre>
 */
public class WhileLoopProcessor extends TreeProcessor {
    public WhileLoopProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCWhileLoop)) {
            return;
        }
        JCTree.JCWhileLoop whileLoop = (JCTree.JCWhileLoop) jcTree;
        if (whileLoop.getCondition() != null) {
            getFactory().get(whileLoop.getCondition().getKind()).process(whileLoop.getCondition());
        }
        if (whileLoop.getStatement() != null) {
            getFactory().get(whileLoop.getStatement().getKind()).process(whileLoop.getStatement());
        }
    }
}
