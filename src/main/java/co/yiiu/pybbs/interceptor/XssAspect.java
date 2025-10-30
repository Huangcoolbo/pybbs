package co.yiiu.pybbs.interceptor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class XssAspect {

    @Around("@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();

        // 获取方法签名和注解信息
        String methodName = joinPoint.getSignature().getName();
        Class<?> targetClass = joinPoint.getTarget().getClass();

        boolean isMarkdownEndpoint = isMarkdownEndpoint(joinPoint);

        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Map && !(args[i] instanceof RedirectAttributes)) {
                if (isMarkdownEndpoint) {
                    args[i] = sanitizeMapForMarkdown((Map<String, String>) args[i]);
                } else {
                    args[i] = sanitizeMap((Map<String, String>) args[i]);
                }
            } else if (args[i] instanceof String) {
                if (isMarkdownEndpoint) {

                } else {
                    args[i] = Jsoup.clean((String) args[i], Whitelist.basic());
                }
            }
        }
        return joinPoint.proceed(args);
    }

    /**
     * 判断是否为需要保留Markdown格式的端点
     */
    private boolean isMarkdownEndpoint(ProceedingJoinPoint joinPoint) {
        try {
            // 获取类级别的RequestMapping注解
            Class<?> targetClass = joinPoint.getTarget().getClass();
            RequestMapping classMapping = targetClass.getAnnotation(RequestMapping.class);

            // 获取方法级别的注解
            String methodName = joinPoint.getSignature().getName();
            Class<?>[] parameterTypes = ((org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature()).getParameterTypes();
            java.lang.reflect.Method method = targetClass.getMethod(methodName, parameterTypes);

            // 构建完整的请求路径
            // 根据类级别 + 方法级别的映射注解，拼出一个完整的接口路径字符串。
            String fullPath = buildRequestPath(classMapping, method);

            return isMarkdownPath(fullPath, method);

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 构建完整的请求路径
     */
    private String buildRequestPath(RequestMapping classMapping, java.lang.reflect.Method method) {
        StringBuilder path = new StringBuilder();

        // 添加类级别的路径
        if (classMapping != null && classMapping.value().length > 0) {
            path.append(classMapping.value()[0]);
        }

        // 添加方法级别的路径
        PostMapping postMapping = method.getAnnotation(PostMapping.class);
        PutMapping putMapping = method.getAnnotation(PutMapping.class);
        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);

        if (postMapping != null && postMapping.value().length > 0) {
            path.append(postMapping.value()[0]);
        } else if (putMapping != null && putMapping.value().length > 0) {
            path.append(putMapping.value()[0]);
        } else if (requestMapping != null && requestMapping.value().length > 0) {
            path.append(requestMapping.value()[0]);
        }

        return path.toString();
    }

    /**
     * 判断路径是否为需要保留Markdown的端点
     */
    private boolean isMarkdownPath(String fullPath, java.lang.reflect.Method method) {
        if (fullPath == null) return false;

        boolean isTopicPost = fullPath.matches("/api/topic") &&
                method.getAnnotation(PostMapping.class) != null;

        boolean isTopicPut = fullPath.matches("/api/topic/\\{id}") &&
                method.getAnnotation(PutMapping.class) != null;

        boolean isAdminTopicPut = fullPath.matches("/admin/topic/edit") &&
                method.getAnnotation(PutMapping.class) != null;

        boolean isCommentPost = fullPath.matches("/api/comment") &&
                method.getAnnotation(PostMapping.class) != null;

        boolean isCommentPut = fullPath.matches("/api/comment/\\{id}") &&
                method.getAnnotation(PutMapping.class) != null;

        boolean isAdminCommentPut = fullPath.matches("/admin/comment/edit") &&
                method.getAnnotation(PutMapping.class) != null;

        return isTopicPost || isTopicPut || isAdminTopicPut || isCommentPost || isCommentPut || isAdminCommentPut;
    }

    /**
     * 原来的基本清理方法，过滤HTML标签
     */
    private Map<String, String> sanitizeMap(Map<String, String> map) {
        Map<String, String> sanitized = new HashMap<>();
        Whitelist whitelist = Whitelist.basic();
        map.forEach((k, v) -> sanitized.put(k, v == null ? null : Jsoup.clean(v, whitelist)));
        return sanitized;
    }

    /**
     * 专门为Markdown内容设计的清理方法
     */
    private Map<String, String> sanitizeMapForMarkdown(Map<String, String> map) {
//        Map<String, String> sanitized = new HashMap<>();
//        map.forEach((k, v) -> sanitized.put(k, v == null ? null : Jsoup.clean(v, Whitelist.basic())));
        return map;
    }

}