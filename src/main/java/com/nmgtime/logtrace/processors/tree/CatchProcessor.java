package com.nmgtime.logtrace.processors.tree;

import com.nmgtime.logtrace.processors.ProcessorFactory;
import com.nmgtime.logtrace.processors.TreeProcessor;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

public class CatchProcessor extends TreeProcessor {
    public CatchProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCCatch)) {
            return;
        }
        JCTree.JCCatch jcCatch = (JCTree.JCCatch) jcTree;
        if (jcCatch.getBlock() != null) {
            getFactory().get(jcCatch.getBlock().getKind()).process(jcCatch.getBlock());
        }
    }
}
