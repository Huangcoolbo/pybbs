package co.yiiu.pybbs.plugin;

import co.yiiu.pybbs.model.vo.CommentsByTopic;
import co.yiiu.pybbs.service.impl.SystemConfigService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tomoya.
 * Copyright (c) 2018, All Rights Reserved.
 * https://atjiu.github.io
 */
@Component
@Aspect
public class CommentLayerPlugin {

    @Resource
    private SystemConfigService systemConfigService;

    @Around("co.yiiu.pybbs.hook.CommentServiceHook.selectByTopicId()")
    public Object selectByTopicId(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        List<CommentsByTopic> newComments = (List<CommentsByTopic>) proceedingJoinPoint.proceed(proceedingJoinPoint.getArgs());
        if (systemConfigService.selectAllConfig().get("comment_layer").equals("1")) {
            // 盖楼显示评论
            return this.sortByLayer(newComments);
        }
        return newComments;
    }

    // 盖楼排序
    // ！！！想来想去还是要用到两层for循环，求大神优化这部分代码，越快越好！！！
    private List<CommentsByTopic> sortByLayer(List<CommentsByTopic> comments) {
        List<CommentsByTopic> newComments = new ArrayList<>();
        for (CommentsByTopic comment : comments) {
            if (comment.getCommentId() == null) {
                newComments.add(comment);
            } else {
                int idIndex = -1, commentIdIndex = -1;
                boolean idIndexFlag = false, commentIdIndexFlag = false;
                for (int i = newComments.size() - 1; i >= 0; i--) {
                    if (!idIndexFlag && comment.getCommentId().equals(newComments.get(i).getId())) {
                        idIndex = i;   // 父评论
                        idIndexFlag = true;
                    }
                    // 注意这里不能用else if，因为可能出现评论回复自己的情况
                    if (!commentIdIndexFlag && comment.getCommentId().equals(newComments.get(i).getCommentId())) {
                        commentIdIndex = i;  // 兄弟评论
                        commentIdIndexFlag = true;
                    }
                }
                if (idIndex == -1) {
                    newComments.add(comment);
                } else {
                    int layer = newComments.get(idIndex).getLayer();
                    comment.setLayer(layer + 1);
                    int count = 0;
                    // 评论里有可能出现多级回复同一个评论的情况
                    if (commentIdIndex != -1) {
                        for (CommentsByTopic newComment : newComments) {
                            // 在已标记层级的评论里找出同级的评论数量
                            if (newComments.get(commentIdIndex).getId().equals(newComment.getCommentId())) count++;
                        }
                    }
                    newComments.add(commentIdIndex == -1 ? idIndex + 1 : commentIdIndex + 1 + count, comment);
                }
            }
        }
        return newComments;
    }
}
