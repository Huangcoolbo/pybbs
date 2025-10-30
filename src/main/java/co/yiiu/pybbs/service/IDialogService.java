package co.yiiu.pybbs.service;

import co.yiiu.pybbs.model.Dialog;
import co.yiiu.pybbs.model.User;
import co.yiiu.pybbs.util.MyPage;

import java.util.List;
import java.util.Map;

public interface IDialogService {
    // 查询所有会话
    List<Dialog> selectAll();
    // 根据用户ID查询会话
    MyPage<Map<String, Object>> selectByUserId(Integer userId, Integer pageNo, Integer pageSize);
    MyPage<Map<String, Object>> selectVoByUserId(Integer userId, Integer pageNo, Integer pageSize);
    // 新建会话
    Dialog insert(Dialog dialog);

    public Dialog selectById(Integer dialogId);
    // 更新会话
    Dialog update(Dialog dialog);
    // 删除会话
    void delete(Integer dialogId);
    // 标记消息全部已读
    void readAll(Integer dialogId);

    Dialog selectByUser(User apiUser, User toUser);
}
