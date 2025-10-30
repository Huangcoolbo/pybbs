package co.yiiu.pybbs.service;

import co.yiiu.pybbs.model.Message;

import java.util.List;

public interface IMessageService {
    // 获取会话的所有消息
    List<Message> selectByDialog(Integer dialogId);

    // 发送一条私信
    Message insert(Message message);

    // 删除一条私信
    void delete(Integer messageId, Integer UserId);

    // 删除会话下的所有消息
    void deleteByDialog(Integer dialogId);

    // 撤回一条私信,

    //Todo 标记消息为已读,感觉不需要，Message有getisRead，等下看作者写的通知已读
}
