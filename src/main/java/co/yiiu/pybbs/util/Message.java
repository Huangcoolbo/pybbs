package co.yiiu.pybbs.util;

import java.io.Serializable;

public class Message implements Serializable {

    private static final long serialVersionUID = 1244988167184789309L;

    // 消息类型
    private String type;
    // 消息载荷
    private Object payload;

    public Message() {
    }

    public Message(String type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
