package com.interface21.webmvc.servlet.mvc.tobe;

import com.interface21.context.stereotype.Controller;
import com.interface21.web.bind.annotation.RequestMapping;
import com.interface21.web.bind.annotation.RequestMethod;
import jakarta.servlet.http.HttpServletRequest;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AnnotationHandlerMapping {

    private static final Logger log = LoggerFactory.getLogger(AnnotationHandlerMapping.class);

    private final Object[] basePackage;
    private final Map<HandlerKey, HandlerExecution> handlerExecutions;

    public AnnotationHandlerMapping(final Object... basePackage) {
        this.basePackage = basePackage;
        this.handlerExecutions = new HashMap<>();
    }

    public void initialize() {
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Controller.class);
        for (Class<?> clazz : classes) {
            initializeController(clazz);
        }
        log.info("Initialized AnnotationHandlerMapping!");
    }

    private void initializeController(Class<?> clazz) {
        try {
            Object controller = clazz.getDeclaredConstructor().newInstance();
            Arrays.stream(clazz.getMethods())
                    .filter(method -> method.isAnnotationPresent(RequestMapping.class))
                    .forEach(method -> initializeMethod(method, controller));
        } catch (Exception e) {
            log.error("Failed to instantiate controller: {}", clazz.getName(), e);
        }
    }

    private void initializeMethod(Method method, Object controller) {
        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        HandlerKey key = new HandlerKey(requestMapping.value(), requestMapping.method()[0]);
        handlerExecutions.put(key, new HandlerExecution(controller, method));
    }


    public HandlerExecution getHandler(final HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        RequestMethod requestMethod = RequestMethod.valueOf(request.getMethod());
        HandlerKey key = new HandlerKey(requestUri, requestMethod);
        return handlerExecutions.get(key);
    }
}
