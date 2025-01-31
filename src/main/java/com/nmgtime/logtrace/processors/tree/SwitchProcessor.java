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
 * A recursive processor for {@link JCTree} of kind {@link com.sun.source.tree.Tree.Kind#SWITCH}.
 * eg:
 * <pre>
 *     switch ( expression ) {
 *       cases
 *     }
 * </pre>
 */
public class SwitchProcessor extends TreeProcessor {

    public SwitchProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCSwitch)) {
            return;
        }
        JCTree.JCSwitch jcSwitch = (JCTree.JCSwitch) jcTree;
        if (jcSwitch.getExpression() != null) {
            getFactory().get(jcSwitch.getExpression().getKind()).process(jcSwitch.getExpression());
        }
        if (jcSwitch.getCases() != null && jcSwitch.getCases().size() > 0) {
            LogTraceContext.MethodConfig methodConfig = LogTraceContext.currentMethodConfig.get();
            for (JCTree.JCCase jcCase : jcSwitch.getCases()) {
                jcCase.accept(new JCTree.Visitor() {
                    @Override
                    public void visitCase(JCTree.JCCase that) {
                        if (methodConfig.isOnlyVar()) {
                            return;
                        }
                        that.stats = generateCode(jcCase.stats, new LogTraceContext.MethodConfig.NewCode(0,
                                methodConfig.getLogContent()
                                        .getNewCodeStatement(Tree.Kind.SWITCH, jcCase,
                                                String.format("Switch%s case %s is true!",
                                                        jcSwitch.selector,
                                                        jcCase.getExpression() == null ? "default" : jcCase.getExpression()),
                                                null, getTreeMaker(), getNames())));
                    }
                });
            }
        }

    }
}
