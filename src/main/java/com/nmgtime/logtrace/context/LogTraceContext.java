package com.nmgtime.logtrace.context;

import com.nmgtime.logtrace.processors.custom.ClassProcessor;
import com.nmgtime.logtrace.processors.custom.MethodProcessor;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Position;
import lombok.Getter;

import javax.lang.model.element.Element;
import java.util.*;

public class LogTraceContext {

    /**
     * The code line map.
     * Refresh in {@link ClassProcessor#process()}
     */
    public static final ThreadLocal<Position.LineMap> lineMap = new ThreadLocal<>();

    /**
     * The log obj ident.
     * Refresh in {@link ClassProcessor#process()}
     */
    public static final ThreadLocal<String> currentLogIdentName = new ThreadLocal<>();

    public static final ThreadLocal<String> classIsOpenFieldName = new ThreadLocal<>();

    /**
     * Master switch obj ident.
     * Refresh in {@link ClassProcessor#process()}
     */
    public static final ThreadLocal<Map<String, String>> allIsOpenMap = new ThreadLocal<>();

    /**
     * The method annotation config.
     * Refresh in {@link MethodProcessor#process(JCTree)}
     */
    public static final ThreadLocal<MethodConfig> currentMethodConfig = new ThreadLocal<>();

    public static final ThreadLocal<Element> currentElement = new ThreadLocal<>();

    public static void remove() {
        lineMap.remove();
        currentLogIdentName.remove();
        classIsOpenFieldName.remove();
        allIsOpenMap.remove();
        currentMethodConfig.remove();
        currentElement.remove();
    }

    public static class MethodConfig {
        @Getter
        private final LogContent logContent;

        @Getter
        private final boolean onlyVar;

        private boolean isInClassOrLambda = false;

        @Getter
        private final Stack<OriginCode> blockStack = new Stack<>(); // Block stack.

        public MethodConfig(String methodName, Map<String, JCTree.JCExpression> argMap, String traceLevel,
                            boolean onlyVar, String mIsOpen) {
            this.logContent = new LogContent(methodName, traceLevel, mIsOpen, argMap);
            this.onlyVar = onlyVar;
        }

        public void setInClassOrLambda(boolean inClassOrLambda) {
            isInClassOrLambda = inClassOrLambda;
        }

        public boolean isInClassOrLambda() {
            return isInClassOrLambda;
        }

        /**
         * The attr of {@link com.nmgtime.logtrace.annos.VarLog}
         */
        public static class VarConfig {
            private final boolean dur;

            public VarConfig(boolean dur) {
                this.dur = dur;
            }

            public boolean isDur() {
                return dur;
            }
        }

        @Getter
        public static class OriginCode {
            private int offset = 0;

            // Need insert into current block when pop stack.
            private final List<NewCode> newCodes = new ArrayList<>();

            private final JCTree.JCBlock block;

            // The collection of all variables annotated by @VarLog in the current block.
            private final Map<String, VarConfig> vars = new LinkedHashMap<>();

            public OriginCode(JCTree.JCBlock block) {
                this.block = block;
            }

            public void incrOffset() {
                offset++;
            }

            public void addNewCode(NewCode newCode) {
                newCodes.add(newCode);
            }

        }

        @Getter
        public static class NewCode {
            private final int offset;
            private final JCTree.JCStatement statement;

            public NewCode(int offset, JCTree.JCStatement statement) {
                this.offset = offset;
                this.statement = statement;
            }

        }
    }

}
