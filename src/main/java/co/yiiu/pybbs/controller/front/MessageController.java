package co.yiiu.pybbs.controller.front;

import co.yiiu.pybbs.model.Dialog;
import co.yiiu.pybbs.model.Message;
import co.yiiu.pybbs.model.User;
import co.yiiu.pybbs.plugin.ElasticSearchService;
import co.yiiu.pybbs.service.impl.DialogService;
import co.yiiu.pybbs.service.impl.MessageService;
import co.yiiu.pybbs.service.impl.UserService;
import co.yiiu.pybbs.util.MyPage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/messages")
public class MessageController extends BaseController{
    @Resource
    private MessageService messageService;
    @Resource
    private DialogService dialogService;
    @Resource
    private UserService userService;
    @Resource
    private ElasticSearchService elasticSearchService;

    @GetMapping("/search_message")
    public String search(@RequestParam Integer dialogId,
                         @RequestParam String kw,
                         @RequestParam(defaultValue = "1") Integer pageNo,
                         Model model) {
        String keyw = kw.replace("\"", "").replace("'", "");
        // 查当前会话
        Dialog dialog = dialogService.selectById(dialogId);
        // 在当前会话里搜消息（你已有ES/SQL都行）
        MyPage<Map<String, Object>> mapMyPage = elasticSearchService.searchDocument("message", dialogId, pageNo, 20, kw, "message");
        List<Message> messages = messageService.selectByDialog(dialog.getId());

        User user = userService.selectById(getUser().getId());
        Integer toUserId = dialog.getUserBId();
        User toUser = userService.selectById(toUserId);
        model.addAttribute("kw", keyw);
        model.addAttribute("searchPage", mapMyPage);   // 搜索结果页
        model.addAttribute("showSearch", true);   // 打开下拉面板
        model.addAttribute("message", null);

        model.addAttribute("dialog", dialog);
        model.addAttribute("user", user);
        model.addAttribute("toUser", toUser);
        model.addAttribute("messages", messages);
        return render("dialogs");
    }
}
