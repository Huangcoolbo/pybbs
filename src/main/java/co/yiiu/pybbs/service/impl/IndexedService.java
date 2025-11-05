package co.yiiu.pybbs.service.impl;

import co.yiiu.pybbs.service.IIndexedService;
import org.springframework.stereotype.Service;

/**
 * Created by tomoya.
 * Copyright (c) 2018, All Rights Reserved.
 * https://atjiu.github.io
 */
@Service
public class IndexedService implements IIndexedService {

    // 索引全部话题
    @Override
    public void indexAllTopic() {
    }

    // 索引话题
    @Override
    public void indexTopic(String id, String title, String content) {
    }

    // 删除话题索引
    @Override
    public void deleteTopicIndex(String id) {
    }

    // 删除所有话题索引
    @Override
    public void batchDeleteIndex() {
    }

    // 索引会话所有消息
    @Override
    public void indexAllMessage(Integer dialogId) {
    }

    // 索引绘画消息
    @Override
    public void indexMessage(String id, String dialogId, String message) {
    }

    // 删除消息索引
    @Override
    public void deleteMessageIndex(String id) {
    }

    // 删除所有消息索引
    @Override
    public void batchDeleteMessageIndex() {
    }
}
