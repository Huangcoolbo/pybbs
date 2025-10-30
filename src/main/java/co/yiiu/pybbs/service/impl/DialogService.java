package co.yiiu.pybbs.service.impl;

import co.yiiu.pybbs.mapper.DialogMapper;
import co.yiiu.pybbs.mapper.MessageMapper;
import co.yiiu.pybbs.mapper.UserMapper;
import co.yiiu.pybbs.model.Dialog;
import co.yiiu.pybbs.model.Message;
import co.yiiu.pybbs.model.User;
import co.yiiu.pybbs.model.vo.DialogSummaryVo;
import co.yiiu.pybbs.service.IDialogService;
import co.yiiu.pybbs.service.ISystemConfigService;
import co.yiiu.pybbs.util.MyPage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class DialogService implements IDialogService {
    @Resource
    private DialogMapper dialogMapper;
    @Resource
    private MessageService messageService;
    @Resource
    private UserService userService;
    @Resource
    private ISystemConfigService systemConfigService;
    // 查询所有会话
    @Override
    public List<Dialog> selectAll() {
        return dialogMapper.selectList(null);
    }

    // 根据用户ID查询会话
    @Override
    public MyPage<Map<String, Object>> selectByUserId(Integer userId, Integer pageNo, Integer pageSize) {
        MyPage<Map<String, Object>> iPage = new MyPage<>(pageNo, pageSize == null ? Integer.parseInt(systemConfigService
                .selectAllConfig().get("page_size").toString()) : pageSize);
        MyPage<Map<String, Object>> page = dialogMapper.selectByUserId(iPage, userId);

        return page;
    }

    // 根据用户Id查询会话
    @Override
    public MyPage<Map<String, Object>> selectVoByUserId(Integer userId, Integer pageNo, Integer pageSize) {
        MyPage<Map<String, Object>> iPage = new MyPage<>(pageNo, pageSize == null ? Integer.parseInt(systemConfigService
                .selectAllConfig().get("page_size").toString()) : pageSize);
        MyPage<Map<String, Object>> page = dialogMapper.selectByUserId(iPage, userId);
        for(Map<String, Object> d : page.getRecords()) {
            Integer dialogId = (Integer) d.get("id");
            Integer userBId = (Integer) d.get("userBId");
            User toUser = new User();
            if(userId.equals(userBId)) {
                Integer userAId = (Integer) d.get("userAId");
                toUser = userService.selectById(userAId);
            } else {
                toUser = userService.selectById(userBId);
            }
            String username = toUser.getUsername();
            d.put("username", username);
        }
        return page;
    }

    // 新建会话
    @Override
    public Dialog insert(Dialog dialog) {
         dialogMapper.insert(dialog);
         return dialog;
    }

    public Dialog insert(User toUser, User fromUser, String content) {
        Dialog dialog = new Dialog();
        dialog.setUserAId(fromUser.getId());
        dialog.setUserBId(toUser.getId());
        dialog.setMessageCount(1);
        dialog.setInTime(new Date());
        // 这个要写在message.setDialogId前,MyBatis 回填主键到dialogId
        dialogMapper.insert(dialog);
        System.out.println("新建对话ID：" + dialog.getId());

        Message message = new Message();
        message.setSenderId(fromUser.getId());
        message.setContent(content);
        message.setDialogId(dialog.getId());
        message.setInTime(new Date());

        messageService.insert(message);

        return dialog;
    }

    // 根据ID查询会话
    @Override
    public Dialog selectById(Integer dialogId) {
        return dialogMapper.selectById(dialogId);
    }
    // 删除会话
    @Override
    public void delete(Integer dialogId) {
        messageService.deleteByDialog(dialogId);
        dialogMapper.deleteById(dialogId);
    }
    // 更新会话
    @Override
    public Dialog update(Dialog dialog) {
        dialogMapper.updateById(dialog);
        return dialog;
    }
    // 标记消息全部已读
    @Override
    public void readAll(Integer dialogId) {
        Dialog dialog = dialogMapper.selectById(dialogId);
        dialog.setUnreadCount(0);
        dialogMapper.updateById(dialog);
    }

    @Override
    public Dialog selectByUser(User apiUser, User toUser) {
        LambdaQueryWrapper<Dialog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dialog::getUserAId, apiUser.getId())
                .eq(Dialog::getUserBId, toUser.getId());
        Dialog dialog = dialogMapper.selectOne(queryWrapper);
        if (dialog == null) {
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Dialog::getUserAId, toUser.getId())
                    .eq(Dialog::getUserBId, apiUser.getId());
            dialog = dialogMapper.selectOne(queryWrapper);
        }
        return dialog;
    }
}
