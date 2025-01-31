package com.nmgtime.logtrace.processors.custom;

import com.nmgtime.logtrace.annos.ParamLog;
import com.nmgtime.logtrace.annos.MethodLog;
import com.nmgtime.logtrace.context.LogTraceContext;
import com.nmgtime.logtrace.processors.ProcessorFactory;
import com.nmgtime.logtrace.processors.TreeProcessor;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Name;

import java.util.*;
import java.util.stream.Collectors;

public class MethodProcessor extends TreeProcessor {

    static final String METHOD_LOG = MethodLog.class.getName();

    static final String PARAMLOG = ParamLog.class.getName();

    static final String METHOD_LOG_EXCEPTION = "exceptionLog";

    static final String METHOD_NO_THROW = "noThrow";

    static final String METHOD_LOG_DUR = "dur";

    static final String METHOD_LOG_ONLY_VAR = "onlyVar";

    static final String METHOD_LOG_LEVEL = "traceLevel";

    static final String METHOD_LOG_IS_OPEN = "isOpen";

    public MethodProcessor(ProcessorFactory factory, JavacTrees javacTrees, TreeMaker treeMaker, Names names) {
        super(factory, javacTrees, treeMaker, names);
    }

    @Override
    public void process(JCTree jcTree) {
        if (!(jcTree instanceof JCTree.JCMethodDecl)) {
            return;
        }
        JCTree.JCMethodDecl methodDecl = (JCTree.JCMethodDecl) jcTree;

        if (methodDecl.getBody() == null
                || methodDecl.getBody().getStatements() == null
                || methodDecl.getBody().getStatements().isEmpty()) {
            return;
        }

        JCTree.JCAnnotation traceAnno = methodDecl.getModifiers().getAnnotations().stream()
                .filter(a -> METHOD_LOG.equals(a.getAnnotationType().type.toString()))
                .collect(Collectors.toList())
                .get(0);

        boolean exceptionLog = false;
        boolean noThrow = false;
        boolean dur = false;
        boolean onlyVar = false;
        String level = "Level.INFO";
        String isOpen = null;
        // 获取注解的属性
        if (traceAnno.getArguments() != null && !traceAnno.getArguments().isEmpty()) {
            for (JCTree.JCExpression arg : traceAnno.getArguments()) {
                if (!(arg instanceof JCTree.JCAssign)) {
                    continue;
                }
                JCTree.JCAssign assign = (JCTree.JCAssign) arg;
                if (METHOD_LOG_EXCEPTION.equals(assign.lhs.toString())) {
                    exceptionLog = "true".equals(assign.rhs.toString());
                }
                if (METHOD_NO_THROW.equals(assign.lhs.toString())) {
                    noThrow = "true".equals(assign.rhs.toString());
                }
                if (METHOD_LOG_DUR.equals(assign.lhs.toString())) {
                    dur = "true".equals(assign.rhs.toString());
                }
                if (METHOD_LOG_ONLY_VAR.equals(assign.lhs.toString())) {
                    onlyVar = "true".equals(assign.rhs.toString());
                }
                if (METHOD_LOG_LEVEL.equals(assign.lhs.toString())) {
                    level = assign.rhs.toString();
                }
                if (METHOD_LOG_IS_OPEN.equals(assign.lhs.toString())) {
                    isOpen = assign.rhs.toString();
                }
            }
        }

        Set<String> allVarsInMethod = methodDecl.getBody().getStatements().stream()
                .filter(s -> s instanceof JCTree.JCVariableDecl)
                .map(s -> ((JCTree.JCVariableDecl) s).getName().toString()).collect(Collectors.toSet());

        // 取出ParamLog注解的字段
        Map<String, JCTree.JCExpression> finalParamsMap = new LinkedHashMap<>();
        java.util.List<JCTree.JCVariableDecl> finalVars = new ArrayList<>();
        // Generate new final params.
        if (methodDecl.getParameters() != null && !methodDecl.getParameters().isEmpty()) {
            List<JCTree.JCVariableDecl> originParams = List.from(methodDecl.getParameters().stream().filter(p -> {
                if (p.getModifiers()!=null&& p.getModifiers().getAnnotations()!=null&&!p.getModifiers().getAnnotations().isEmpty()) {
                    for (JCTree.JCAnnotation annotation : p.getModifiers().getAnnotations()) {
                        if (PARAMLOG.equals(annotation.getAnnotationType().type.toString())) {
                            return true;
                        }
                    }
                }
                return false;
            }).collect(Collectors.toList()));

            if (!originParams.isEmpty()) {
                originParams.forEach(op -> {
                    String newParamName = String.format("final_%s", op.getName().toString());
                    int num = 0;
                    while (allVarsInMethod.contains(newParamName)) { // Preventing naming conflicts.
                        newParamName = String.format("final_%s_%d", op.getName().toString(), num);
                        num++;
                    }
                    JCTree.JCVariableDecl finalParam = getTreeMaker().VarDef(
                            getTreeMaker().Modifiers(Flags.FINAL, com.sun.tools.javac.util.List.nil()),
                            getNames().fromString(newParamName),
                            getTreeMaker().Ident(getNames().fromString("Object")),
                            getTreeMaker().Ident(getNames().fromString(op.getName().toString()))
                    );
                    finalVars.add(finalParam);
                    finalParamsMap.put(op.getName().toString(), getTreeMaker().Ident(getNames().fromString(newParamName)));
                });
            }
        }

        LogTraceContext.MethodConfig methodConfig = new LogTraceContext.MethodConfig(
                methodDecl.getName().toString(), finalParamsMap, level, onlyVar, isOpen);
        //methodConfig.getBlockStack().push(new Context.MethodConfig.OriginCode(methodDecl.getBody()));
        LogTraceContext.currentMethodConfig.set(methodConfig);

        getFactory().get(methodDecl.getBody().getKind()).process(methodDecl.getBody());

        // Generate try-block statement.
        JCTree.JCCatch jcCatch = null;
        if (exceptionLog) {

            Name e = getNames().fromString("e");
            JCTree.JCIdent eIdent = getTreeMaker().Ident(e);
            Map<String, JCTree.JCExpression> newArgs = new HashMap<>();
            newArgs.put(null, eIdent);

            List<JCTree.JCStatement> statements = List.of(methodConfig.getLogContent()
                    .getNewCodeStatement(Tree.Kind.TRY, methodDecl.getBody(),
                            "Error!", newArgs, getTreeMaker(), getNames(), "Level.ERROR"));
            if (!noThrow) {
                statements = statements.append(getTreeMaker().Throw(eIdent));
            }
            jcCatch = getTreeMaker().Catch(getTreeMaker().VarDef(getTreeMaker().Modifiers(0), e,
                            getTreeMaker().Ident(getNames().fromString("Exception")), null),
                    getTreeMaker().Block(0L, statements));
        }

        JCTree.JCBlock jcFinally = null;
        JCTree.JCVariableDecl jcStartTime = null;
        if (dur) {
            Name newParamName = getNames().fromString(getNewVarName("start_"));

            // Code: System.nanoTime()
            JCTree.JCMethodInvocation nanoTimeInvocation = getTreeMaker().Apply(null, getTreeMaker().Select(
                    getTreeMaker().Ident(getNames().fromString("System")),
                    getNames().fromString("nanoTime")), List.nil());

            // Code: final long log_trace_start = System.nanoTime()
            jcStartTime = getTreeMaker().VarDef(getTreeMaker().Modifiers(Flags.FINAL, com.sun.tools.javac.util.List.nil()),
                    newParamName,
                    getTreeMaker().TypeIdent(TypeTag.LONG),
                    nanoTimeInvocation);

            // Code: (System.nanoTime() - log_trace_start) / 1000000L
            Map<String, JCTree.JCExpression> newParams = new LinkedHashMap<>();
            newParams.put("duration", getTreeMaker().Binary(JCTree.Tag.DIV,
                    getTreeMaker().Parens(getTreeMaker().Binary(JCTree.Tag.MINUS,
                            nanoTimeInvocation, getTreeMaker().Ident(newParamName))),
                    getTreeMaker().Literal(1000000L)));

            // Code: finally { trace_logger.debug("xxxx Finished! duration = 25") }
            jcFinally = getTreeMaker().Block(0L, List.of(methodConfig.getLogContent().getNewCodeStatement(
                    Tree.Kind.TRY, methodDecl.getBody(), "Finished!",
                    newParams, getTreeMaker(), getNames())));
        }

        final JCTree.JCCatch finalJcCatch = jcCatch;
        final JCTree.JCBlock finalJcFinally = jcFinally;
        final JCTree.JCVariableDecl finalStartTime = jcStartTime;
        methodDecl.getBody().accept(new JCTree.Visitor() {
            @Override
            public void visitBlock(JCTree.JCBlock that) {
                that.stats = List.of(getTreeMaker().Try(getTreeMaker().Block(that.flags, that.stats),
                        finalJcCatch == null ? List.nil() : List.of(finalJcCatch), finalJcFinally));
            }
        });

        // Generate top variables.
        java.util.List<JCTree.JCVariableDecl> topVars = new ArrayList<>(finalVars);
        if (finalStartTime != null) {
            topVars.add(finalStartTime);
        }
        topVars.forEach(tv -> methodDecl.getBody().accept(new JCTree.Visitor() {
            @Override
            public void visitBlock(JCTree.JCBlock that) {
                that.stats = generateCode(that.getStatements(), new LogTraceContext.MethodConfig.NewCode(0, tv));
            }
        }));
    }
}
