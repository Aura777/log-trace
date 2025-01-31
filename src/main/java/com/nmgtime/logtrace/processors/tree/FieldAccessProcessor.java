package com.nmgtime.logtrace.processors.tree;

import com.nmgtime.logtrace.processors.ProcessorFactory;
import com.nmgtime.logtrace.processors.TreeProcessor;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

/**
 * A recursive processor for {@link JCTree} of kind {@link com.sun.source.tree.Tree.Kind#MEMBER_SELECT}.
 * eg:
 * <pre>
 *     expression . identifier
 * </pre>
 */
public class FieldAccessProcessor extends TreeProcessor {
    public FieldAccessProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCFieldAccess)) {
            return;
        }
        JCTree.JCFieldAccess fieldAccess = (JCTree.JCFieldAccess) jcTree;
        JCTree.JCExpression selected = fieldAccess.getExpression();
        if (selected != null) {
            getFactory().get(selected.getKind()).process(selected);
        }
    }
}
