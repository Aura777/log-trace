package com.nmgtime.logtrace.processors.tree;

import com.nmgtime.logtrace.context.LogTraceContext;
import com.nmgtime.logtrace.processors.ProcessorFactory;
import com.nmgtime.logtrace.processors.TreeProcessor;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

public class AssignProcessor extends TreeProcessor {

    public AssignProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCAssign)) {
            return;
        }
        JCTree.JCAssign assign = (JCTree.JCAssign) jcTree;
        if (assign.getVariable() != null) {

            LogTraceContext.MethodConfig methodConfig = LogTraceContext.currentMethodConfig.get();
            LogTraceContext.MethodConfig.OriginCode originCode = methodConfig.getBlockStack().peek();
            String varName = assign.getVariable().toString();
            if (originCode.getVars().containsKey(varName)) {
                VariableProcessor.attachVarLog(varName, originCode.getVars().get(varName).isDur(), methodConfig,
                        originCode, assign.getVariable(), getTreeMaker(), getNames());
            }
        }
        if (assign.getExpression() != null) {
            getFactory().get(assign.getExpression().getKind()).process(assign.getExpression());
        }
    }
}
