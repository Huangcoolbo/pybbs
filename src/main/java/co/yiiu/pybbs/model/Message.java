package co.yiiu.pybbs.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer dialogId;
    private Integer senderId;
    private String content;
    private Boolean isRead;
    private Boolean fromDeleted;
    private Boolean toDeleted;
    private Date inTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getisRead() {
        return isRead;
    }

    public void setisRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public Boolean getfromDeleted() {
        return fromDeleted;
    }

    public void setfromDeleted(Boolean fromDeleted) {
        this.fromDeleted = fromDeleted;
    }
    public Boolean gettoDeleted() {
        return toDeleted;
    }
    public void settoDeleted(Boolean toDeleted) {
        this.toDeleted = toDeleted;
    }
    public Date getInTime() {
        return inTime;
    }

    public void setInTime(Date inTime) {
        this.inTime = inTime;
    }

    public Integer getDialogId() {
        return dialogId;
    }

    public void setDialogId(Integer dialogId) {
        this.dialogId = dialogId;
    }

    public Integer getSenderId() {
        return senderId;
    }

    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }
}
