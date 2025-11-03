package co.yiiu.pybbs.controller.api;

import co.yiiu.pybbs.model.Dialog;
import co.yiiu.pybbs.model.Message;
import co.yiiu.pybbs.service.impl.DialogService;
import co.yiiu.pybbs.service.impl.MessageService;
import co.yiiu.pybbs.util.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageApiController extends BaseApiController{
    @Resource
    private MessageService messageService;
    @Resource
    private DialogService dialogService;

    @PostMapping("/{id}")
    public Result insert(@RequestBody Map<String, Object> body) {
        Object d = body.get("dialogId");
        Integer dialogId = Integer.valueOf(String.valueOf(d));
        String content = (String) body.get("content");
        Message message = new Message();
        message.setDialogId(dialogId);
        message.setContent(content);
        message.setSenderId(getUser().getId());
        message.setInTime(new Date());

        message = messageService.insert(message);

        Dialog dialog = dialogService.selectById(dialogId);
        dialog.setLastMessageId(message.getId());
        dialog.setLastMessage(message.getContent());
        dialogService.update(dialog);
        return success(message);
    }
}
