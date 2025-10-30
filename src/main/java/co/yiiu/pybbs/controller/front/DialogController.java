package co.yiiu.pybbs.controller.front;

import co.yiiu.pybbs.model.Dialog;
import co.yiiu.pybbs.model.Message;
import co.yiiu.pybbs.model.User;
import co.yiiu.pybbs.service.impl.DialogService;
import co.yiiu.pybbs.service.impl.MessageService;
import co.yiiu.pybbs.service.impl.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/dialogs")
public class DialogController extends BaseController{
    @Resource
    private DialogService dialogService;
    @Resource
    private UserService userService;
    @Resource
    private MessageService messageService;

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model, HttpServletRequest request) {
        Dialog dialog = dialogService.selectById(id);
        dialog.setUnreadCount(0);
        dialogService.update(dialog);

        Integer toUserId = dialog.getUserBId();
        User user = userService.selectById(getUser().getId());
        User toUser = userService.selectById(toUserId);
        // 如果当前用户等于接收用户
        if(user.getId().equals(dialog.getUserBId())) {
            toUserId = dialog.getUserAId();
            toUser = userService.selectById(toUserId);
        }
        List<Message> messages = messageService.selectByDialog(dialog.getId());

        model.addAttribute("messages", messages);
        model.addAttribute("user",user);
        model.addAttribute("dialog", dialog);
        model.addAttribute("toUser", toUser);
        return render("dialogs");
    }

    @GetMapping("/create")
    public String create(Model model, HttpServletRequest request) {
        User user = userService.selectById(getUser().getId());
        model.addAttribute("user", user);
        return render("dialog/create");
    }

}
