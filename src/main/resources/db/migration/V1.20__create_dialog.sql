DROP TABLE IF EXISTS `message`;
DROP TABLE IF EXISTS `dialog`;

CREATE TABLE `dialog` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `user_a_id` int(11) NOT NULL COMMENT '发送用户ID',
    `user_b_id` int(11) NOT NULL COMMENT '接收用户ID',
    `unread_count` int(11) NOT NULL DEFAULT 0 COMMENT '未读消息数',
    `message_count` int(11) NOT NULL DEFAULT 0 COMMENT '消息总数',
    `in_time` datetime NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `user_a_id` (`user_a_id`),
    KEY `user_b_id` (`user_b_id`),
    CONSTRAINT `dialog_ibfk_1` FOREIGN KEY (`user_a_id`) REFERENCES `user` (`id`),
    CONSTRAINT `dialog_ibfk_2` FOREIGN KEY (`user_b_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内私信对话表';
