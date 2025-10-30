package co.yiiu.pybbs.service.impl;

import co.yiiu.pybbs.mapper.MessageMapper;
import co.yiiu.pybbs.service.IMessageService;
import co.yiiu.pybbs.model.Message;
import co.yiiu.pybbs.service.ISystemConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class MessageService implements IMessageService {
    @Resource
    private MessageMapper messageMapper;
    @Resource
    private ISystemConfigService systemConfigService;
    // 获取会话的所有消息
    @Override
    public List<Message> selectByDialog(Integer dialogId) {
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getDialogId, dialogId);
        return messageMapper.selectList(queryWrapper);
    }

    // 发送一条私信
    @Override
    public Message insert(Message message) {
        messageMapper.insert(message);
        return message;
    }

    // 删除一条私信
    // 怎么判断当前用户是发送者还是接收者呢？需要传入当前用户ID吗？
    @Override
    public void delete(Integer messageId, Integer UserId) {
        Message message = messageMapper.selectById(messageId);
        if(message.getSenderId().equals(UserId)) {
            message.setfromDeleted(true);
        } else {
            message.settoDeleted(true);
        }
        if(message.getfromDeleted() && message.gettoDeleted()) {
            messageMapper.deleteById(messageId);
        } else {
            messageMapper.updateById(message);
        }
    }

    // 删除会话下的所有消息
    @Override
    public void deleteByDialog(Integer dialogId) {
        List<Message> messages = selectByDialog(dialogId);
        for(Message message : messages) {
            messageMapper.deleteById(message.getId());
        }
    }

}
