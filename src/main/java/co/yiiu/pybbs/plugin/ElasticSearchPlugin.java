package co.yiiu.pybbs.plugin;

import co.yiiu.pybbs.mapper.TopicMapper;
import co.yiiu.pybbs.model.Message;
import co.yiiu.pybbs.model.Topic;
import co.yiiu.pybbs.service.impl.MessageService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by tomoya.
 * Copyright (c) 2018, All Rights Reserved.
 * https://atjiu.github.io
 */
@Component
@Aspect
public class ElasticSearchPlugin {

    @Resource
    private ElasticSearchService elasticSearchService;
    @Resource
    private TopicMapper topicMapper;
    @Resource
    private MessageService messageService;

    @Around("co.yiiu.pybbs.hook.TopicServiceHook.search()")
    public Object search(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object[] args = proceedingJoinPoint.getArgs();
        if (elasticSearchService.instance() == null) return proceedingJoinPoint.proceed(args);
        return elasticSearchService.searchDocument("topic", 0, (Integer) args[0], (Integer) args[1], (String) args[2], "title", "content");
    }

    @After("co.yiiu.pybbs.hook.IndexedServiceHook.indexAllTopic()")
    public void indexAllTopic() {
        if (elasticSearchService.instance() != null) {
            List<Topic> topics = topicMapper.selectList(null);
            Map<String, Map<String, Object>> sources = topics.stream().collect(Collectors.toMap(key -> String.valueOf(key
                    .getId()), value -> {
                Map<String, Object> map = new HashMap<>();
                map.put("title", value.getTitle());
                map.put("content", value.getContent());
                return map;
            }));
            elasticSearchService.bulkDocument("topic","topic", sources);
        }
    }

    @After("co.yiiu.pybbs.hook.IndexedServiceHook.indexAllMessage()")
    public void indexAllMessage(JoinPoint joinPoint) {
        if (elasticSearchService.instance() != null) {
            Object[] args = joinPoint.getArgs();
            Integer dialogid = (Integer) args[0];
            List<Message> messages = messageService.selectByDialog(dialogid);
            Map<String, Map<String, Object>> sources = messages.stream().collect(Collectors.toMap(key -> String.valueOf(key
                    .getId()), value -> {
                Map<String, Object> map = new HashMap<>();
                map.put("dialogId", value.getDialogId());
                map.put("message", value.getContent());
                return map;
            }));
            elasticSearchService.bulkDocument("message","message", sources);
        }
    }

    @After("co.yiiu.pybbs.hook.IndexedServiceHook.indexTopic()")
    public void indexTopic(JoinPoint joinPoint) {
        if (elasticSearchService.instance() != null) {
            Object[] args = joinPoint.getArgs();
            Map<String, Object> source = new HashMap<>();
            source.put("title", args[1]);
            source.put("content", args[2]);
            elasticSearchService.createDocument("topic","topic", (String) args[0], source);
        }
    }

    @After("co.yiiu.pybbs.hook.IndexedServiceHook.indexMessage()")
    public void indexMessage(JoinPoint joinPoint) {
        if (elasticSearchService.instance() != null) {
            Object[] args = joinPoint.getArgs();
            Map<String, Object> source = new HashMap<>();
            source.put("dialogId", args[1]);
            source.put("message", args[2]);
            elasticSearchService.createDocument("message","message", (String) args[0], source);
        }
    }


    @After("co.yiiu.pybbs.hook.IndexedServiceHook.deleteTopicIndex()")
    public void deleteTopicIndex(JoinPoint joinPoint) {
        if (elasticSearchService.instance() != null) {
            elasticSearchService.deleteDocument("topic","topic", (String) joinPoint.getArgs()[0]);
        }
    }

    @After("co.yiiu.pybbs.hook.IndexedServiceHook.deleteMessageIndex()")
    public void deleteMessageIndex(JoinPoint joinPoint) {
        if (elasticSearchService.instance() != null) {
            elasticSearchService.deleteDocument("message","message", (String) joinPoint.getArgs()[0]);
        }
    }

    @After("co.yiiu.pybbs.hook.IndexedServiceHook.batchDeleteIndex()")
    public void batchDeleteIndex(JoinPoint joinPoint) {
        if (elasticSearchService.instance() != null) {
            List<Topic> topics = topicMapper.selectList(null);
            List<Integer> ids = topics.stream().map(Topic::getId).collect(Collectors.toList());
            elasticSearchService.bulkDeleteDocument("topic","topic", ids);
        }
    }

    @After("co.yiiu.pybbs.hook.IndexedServiceHook.batchDeleteMessageIndex()")
    public void batchDeleteMessageIndex(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Integer dialogid = (Integer) args[1];
        if (elasticSearchService.instance() != null) {
            List<Message> messages = messageService.selectByDialog(dialogid);
            List<Integer> ids = messages.stream().map(Message::getId).collect(Collectors.toList());
            elasticSearchService.bulkDeleteDocument("meessage" ,"message", ids);
        }
    }
}
