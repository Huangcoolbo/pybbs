package co.yiiu.pybbs.controller.api;

import co.yiiu.pybbs.exception.ApiAssert;
import co.yiiu.pybbs.model.Dialog;
import co.yiiu.pybbs.model.User;
import co.yiiu.pybbs.service.impl.DialogService;
import co.yiiu.pybbs.service.impl.MessageService;
import co.yiiu.pybbs.service.impl.UserService;
import co.yiiu.pybbs.util.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/api/dialogs")
public class DialogApiController extends BaseApiController{
    @Resource
    private DialogService dialogService;
    @Resource
    private MessageService messageService;
    @Resource
    UserService userService;

    @PostMapping("/create")
    public Result create(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String email = body.get("email");
        String content = body.get("content");
        ApiAssert.notEmpty(username, "请输入用户名");
        ApiAssert.notEmpty(email, "请输入邮箱");
        ApiAssert.notEmpty(content, "请输入内容");

        User user = userService.selectByUsername(username);
        ApiAssert.isTrue(email.equals(user.getEmail()), "用户名和邮箱不匹配");
        Dialog dialog = dialogService.insert(user, getApiUser(), content);
        return success(dialog);
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        Dialog dialog = dialogService.selectById(id);
        ApiAssert.notNull(dialog, "会话不存在");
        ApiAssert.isTrue(dialog.getUserAId().equals(getApiUser().getId()) || dialog.getUserBId().equals(getApiUser().getId()),
                "没有权限删除该会话");

        dialogService.delete(id);
        return success();
    }

    @GetMapping("/check")
    public Result check(@RequestParam String username) {
        User toUser = userService.selectByUsername(username);
        ApiAssert.notNull(toUser, "用户不存在");
        Dialog dialog = dialogService.selectByUser(getApiUser(), toUser);
        if(dialog == null) {
            return success();
        }
        dialog.setUserAId(getApiUser().getId());
        dialog.setUserBId(toUser.getId());
        return success(dialog);
    }
}
