package com.nmgtime.logtrace.processors.tree;

import com.nmgtime.logtrace.context.LogTraceContext;
import com.nmgtime.logtrace.processors.ProcessorFactory;
import com.nmgtime.logtrace.processors.TreeProcessor;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

/**
 * A recursive processor for {@link JCTree} of kind {@link com.sun.source.tree.Tree.Kind#IF}.
 * eg:
 * <pre>
 *     if ( condition )
 *        thenStatement
 *
 *     if ( condition )
 *         thenStatement
 *     else
 *         elseStatement
 * </pre>
 */
public class IfProcessor extends TreeProcessor {

    public IfProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCIf) && !(jcTree instanceof JCTree.JCBlock)) {
            return;
        }
        LogTraceContext.MethodConfig methodConfig = LogTraceContext.currentMethodConfig.get();
        if (jcTree instanceof JCTree.JCIf) {
            JCTree.JCIf jcIf = (JCTree.JCIf) jcTree;
            if (jcIf.getThenStatement() != null) {
                getFactory().get(jcIf.getThenStatement().getKind()).process(jcIf.getThenStatement());
                if (jcIf.getThenStatement() instanceof JCTree.JCBlock) {
                    JCTree.JCBlock then = (JCTree.JCBlock) jcIf.getThenStatement();
                    then.accept(new JCTree.Visitor() {
                        @Override
                        public void visitBlock(JCTree.JCBlock that) {
                            if (methodConfig.isOnlyVar()) {
                                return;
                            }
                            that.stats = generateCode(that.getStatements(), new LogTraceContext.MethodConfig.NewCode(0,
                                    methodConfig.getLogContent().getNewCodeStatement(Tree.Kind.IF, jcIf,
                                            String.format("The condition: %s is true!", jcIf.getCondition()),
                                            null, getTreeMaker(), getNames())));
                        }
                    });
                }
            }
            if (jcIf.getElseStatement() != null) {
                process(jcIf.getElseStatement());
            }
            if (jcIf.getCondition() != null) {
                getFactory().get(jcIf.getCondition().getKind()).process(jcIf.getCondition());
            }
        } else {
            getFactory().get(jcTree.getKind()).process(jcTree);
            JCTree.JCBlock elsePart = (JCTree.JCBlock) jcTree;
            elsePart.accept(new JCTree.Visitor() {
                @Override
                public void visitBlock(JCTree.JCBlock that) {
                    if (methodConfig.isOnlyVar()) {
                        return;
                    }
                    that.stats = generateCode(that.getStatements(), new LogTraceContext.MethodConfig.NewCode(0,
                            methodConfig.getLogContent().getNewCodeStatement(Tree.Kind.IF, jcTree,
                                    "The condition: else is true!",
                                    null, getTreeMaker(), getNames())));
                }
            });
        }
    }
}
