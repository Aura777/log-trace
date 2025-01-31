package com.nmgtime.logtrace.processors.tree;

import com.nmgtime.logtrace.processors.ProcessorFactory;
import com.nmgtime.logtrace.processors.TreeProcessor;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

/**
 * A recursive processor for {@link JCTree} of kind {@link com.sun.source.tree.Tree.Kind#TYPE_CAST}.
 * eg:
 * <pre>
 *     ( type ) expression
 * </pre>
 */
public class TypeCastProcessor extends TreeProcessor {
    public TypeCastProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCTypeCast)) {
            return;
        }
        JCTree.JCTypeCast typeCast = (JCTree.JCTypeCast) jcTree;
        getFactory().get(typeCast.getExpression().getKind()).process(typeCast.getExpression());
    }
}
