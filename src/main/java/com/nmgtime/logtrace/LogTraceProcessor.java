package com.nmgtime.logtrace;

import com.google.auto.service.AutoService;
import com.nmgtime.logtrace.annos.TraceLog;
import com.nmgtime.logtrace.context.LogTraceContext;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Names;
import com.nmgtime.logtrace.processors.ProcessorFactory;

import javax.annotation.processing.*;
import javax.lang.model.element.Element;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.*;

@SupportedAnnotationTypes("com.nmgtime.logtrace.annos.TraceLog")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class LogTraceProcessor extends AbstractProcessor {
    private ProcessorFactory factory;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.factory = new ProcessorFactory(JavacTrees.instance(processingEnv), TreeMaker.instance(context), Names.instance(context));
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> set = new HashSet<>();
        String name = TraceLog.class.getName();
        set.add(name);
        return set;
    }

    /**
     * Attach trace log code.
     *
     * @param annotations the annotation types requested to be processed.
     * @param roundEnv    environment for information about the current and prior round.
     * @return whether the set of annotation types are claimed by this processor.
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement t : annotations) {
            for (Element e : roundEnv.getElementsAnnotatedWith(t)) {
                try {
                    LogTraceContext.currentElement.set(e);
                    factory.get(ProcessorFactory.Kind.CLASS).process();
                } finally {
                    LogTraceContext.remove();
                }
            }
        }
        return true;
    }
}
