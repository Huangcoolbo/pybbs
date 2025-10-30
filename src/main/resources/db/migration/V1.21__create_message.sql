# Dump of table message
# ------------------------------------------------------------

DROP TABLE IF EXISTS `message`;

CREATE TABLE `message` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `dialog_id` int(11) NOT NULL COMMENT '对话ID',
    `sender_id` int(11) NOT NULL COMMENT '发送者ID',
    `content` varchar(1000) NOT NULL COMMENT '消息内容（纯文本）',
    `is_read` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否已读：0未读 1已读',
    `from_deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '发送者删除标志：0未删除 1已删除',
    `to_deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '接收者删除标志：0未删除 1已删除',
    `in_time` datetime NOT NULL COMMENT '发送时间',
    `read_time` datetime NULL DEFAULT NULL COMMENT '读取时间（可空）',
   PRIMARY KEY (`id`),
   KEY `dialog_id` (`dialog_id`),
   KEY `sender_id` (`sender_id`),
   CONSTRAINT `message_ibfk_1` FOREIGN KEY (`dialog_id`) REFERENCES `dialog` (`id`),
   CONSTRAINT `message_ibfk_2` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内私信消息表';
