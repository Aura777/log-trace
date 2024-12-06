package com.nmgtime.logtrace.processors.tree;

import com.nmgtime.logtrace.context.LogTraceContext;
import com.nmgtime.logtrace.processors.ProcessorFactory;
import com.nmgtime.logtrace.processors.TreeProcessor;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

/**
 * A recursive processor for {@link JCTree} of kind {@link com.sun.source.tree.Tree.Kind#LAMBDA_EXPRESSION}.
 * eg:
 * <pre>
 *      ()->{}
 *      (List<String> ls)->ls.size()
 *      (x,y)-> { return x + y; }
 * </pre>
 */
public class LambdaExpressionProcessor extends TreeProcessor {

    public LambdaExpressionProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCLambda)) {
            return;
        }

        JCTree.JCLambda jcLambda = (JCTree.JCLambda) jcTree;

        // TODO JCTree.JCLambda.getParameters() may should be processed.
        /*if(jcLambda.getParameters() != null && jcLambda.getParameters().size() > 0){
            jcLambda.getParameters().forEach(p-> getFactory().get(p.getKind()).process(p));
        }*/
        if (jcLambda.getBody() != null) {
            LogTraceContext.MethodConfig methodConfig = LogTraceContext.currentMethodConfig.get();
            methodConfig.setInClassOrLambda(true);
            getFactory().get(jcLambda.getBody().getKind()).process(jcLambda.getBody());
            methodConfig.setInClassOrLambda(false);
        }
    }
}
