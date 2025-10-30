package co.yiiu.pybbs.model.vo;

import co.yiiu.pybbs.model.Dialog;

import java.io.Serializable;
import java.util.Date;

public class DialogSummaryVo implements Serializable {
    private Integer id;
    private String  username;      // 对方用户名
    private String  lastMessageContent;// 最近一条消息内容
    private Date lastMessageTime;   // 最近消息时间
    private Integer unreadCount;

    public Date getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(Date lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLastMessageContent() {
        return lastMessageContent;
    }

    public void setLastMessageContent(String lastMessageContent) {
        this.lastMessageContent = lastMessageContent;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }
}
