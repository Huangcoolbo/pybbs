package co.yiiu.pybbs.mapper;

import co.yiiu.pybbs.model.Dialog;
import co.yiiu.pybbs.model.vo.DialogSummaryVo;
import co.yiiu.pybbs.util.MyPage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface DialogMapper extends BaseMapper<Dialog> {
    // MyPage<Map<String, Object>> selectAll((MyPage<Map<String, Object>> iPage);
    MyPage<Map<String, Object>> selectByUserId(MyPage<Map<String, Object>> iPage, @Param("userId") Integer userId);
}
