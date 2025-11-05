package co.yiiu.pybbs.service.impl;

import co.yiiu.pybbs.config.websocket.MyWebSocket;
import co.yiiu.pybbs.mapper.DialogMapper;
import co.yiiu.pybbs.mapper.MessageMapper;
import co.yiiu.pybbs.model.Dialog;
import co.yiiu.pybbs.model.User;
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
    private DialogMapper dialogMapper;
    @Resource
    private UserService userService;
    @Resource
    private ISystemConfigService systemConfigService;
    @Resource
    private IndexedService indexedService;
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

        // 通知
        Dialog dialog = selectDialogByMessageId(message.getId());
        Integer toUserId;
        Integer userAId = dialog.getUserAId();
        Integer userBId = dialog.getUserBId();
        if(message.getSenderId().equals(userAId)) {
            toUserId = userBId;
        } else {
            toUserId = userAId;
        }
        User toUser = userService.selectById(toUserId);
        User senderUser = userService.selectById(message.getSenderId());
        String emailTitle = "%s 给你发送了一条信息";
        if(systemConfigService.selectAllConfig().get("websocket").equals("1")) {
            MyWebSocket.emit(toUser.getId(), new co.yiiu.pybbs.util.Message("dialog_message", String.format(emailTitle, senderUser.getUsername())));
        }
        // 添加会话消息索引
        indexedService.indexMessage(String.valueOf(message.getId()), String.valueOf(dialog.getId()), message.getContent());
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

    @Override
    public Message selectById(Integer MessageId) {
        return messageMapper.selectById(MessageId);
    }

    public Dialog selectDialogByMessageId(Integer messageId) {
        Message message = messageMapper.selectById(messageId);
        Integer dialogid = message.getDialogId();
        return dialogMapper.selectById(dialogid);
    }

}
