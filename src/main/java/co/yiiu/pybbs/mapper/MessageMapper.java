package co.yiiu.pybbs.mapper;

import co.yiiu.pybbs.model.Message;
import co.yiiu.pybbs.util.MyPage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

public interface MessageMapper extends BaseMapper<Message> {

    List<Message> selectAllMessage(Integer fromUserId, Integer toUserId);
    MyPage<Map<String, Object>> selectByUserId(MyPage<Map<String, Object>> iPage, Integer fromUserId, Integer toUserId);
}
