<#include "layout/layout.ftl"/>
<@html page_title="私信" page_tab="dialog">

    <div class="container mt-4">
        <div class="row">

            <!-- 左侧：会话列表 -->
            <div class="col-md-4 mb-3">
                <div class="card h-100">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <span><i class="fa fa-comments"></i> 我的会话</span>
                        <a href="/dialogs/create" class="btn btn-sm btn-outline-primary">
                            <i class="fa fa-pencil"></i> 新建
                        </a>
                    </div>

                    <ul class="list-group list-group-flush" id="dialogList" style="overflow-y:auto; max-height:480px;">
                        <#-- 这里你可以用自己的 user_dialogs 宏，或者直接循环 dialogs.records -->
                        <#include "./components/user_dialogs.ftl"/>
                        <@user_dialogs pageNo=1 pageSize=10 username="${user.username!}" isFooter=true/>
                    </ul>
                </div>
            </div>

            <!-- 右侧：聊天面板 -->
            <div class="col-md-8 mb-3">
                <div class="card h-100 d-flex flex-column" style="height:520px;">

                    <!-- 顶部：聊天对象 -->
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <div>
                            <i class="fa fa-user"></i>
                            <span id="chatWithName">
                            <#if toUser??>
                                ${toUser.username!?html}
                            <#else>
                                选择一个会话
                            </#if>
                        </span>
                        </div>
                        <small class="text-muted" id="chatWithMeta">
                            <#if activeDialog??>
                                我 · ${model.formatDate(activeDialog.lastMessageTime)!}
                            </#if>
                        </small>
                        <#-- 未读徽章之类你也可以放在这里，比如 activeDialog.unreadCount -->
                        <#if activeDialog?? && activeDialog.unreadCount?? && activeDialog.unreadCount?number gt 0>
                            <span class="badge badge-success">${activeDialog.unreadCount}</span>
                        </#if>
                    </div>

                    <!-- 中部：消息区域（可滚动，占据剩余空间） -->
                    <div class="card-body flex-grow-1"
                         id="messageArea"
                         style="overflow-y:auto; background-color:#fafafa;">

                        <#if messages?? && (messages?size > 0)>
                            <#list messages as msg>
                                <#assign isMe = (msg.senderId == user.id) />

                                <#if isMe>
                                    <div class="d-flex flex-column align-items-end mb-3">
                                        <div class="small text-muted mb-1">
                                            我 · ${model.formatDate(msg.inTime)!}
                                        </div>
                                        <div class="p-2 rounded bg-success text-white" style="max-width:70%;">
                                            ${msg.content!?html}
                                        </div>
                                    </div>
                                <#else>
                                    <div class="d-flex flex-column align-items-start mb-3">
                                        <div class="small text-muted mb-1">
                                            ${msg.senderUsername!toUser.username} · ${model.formatDate(msg.inTime)!}
                                        </div>
                                        <div class="p-2 rounded border bg-white" style="max-width:70%;">
                                            ${msg.content!?html}
                                        </div>
                                    </div>
                                </#if>
                            </#list>
                        <#else>
                            <div class="text-muted text-center mt-5" id="noDialogHint">
                                请选择左侧的一个会话开始私信
                            </div>
                        </#if>

                    </div>

                    <!-- 底部：发送栏（永远在卡片最底） -->
                    <div class="card-footer">
                        <form id="sendForm" class="d-flex">
                            <input type="hidden" id="dialogId" name="dialogId"
                                   value="<#if dialog??>${dialog.id}</#if>"/>

                            <input type="text"
                                   class="form-control mr-2"
                                   id="messageInput"
                                   placeholder="输入消息，回车发送"
                                   autocomplete="off"
                                   <#if !dialog??>disabled</#if>
                            />

                            <button class="btn btn-primary"
                                    id="sendBtn"
                                    type="button"
                                    <#if !dialog??>disabled</#if>>
                                发送
                            </button>
                        </form>
                    </div>

                </div><!-- /.card -->
            </div><!-- /.col-md-8 -->

        </div><!-- /.row -->
    </div><!-- /.container -->
    <script>
        var messageCount = ${(messages?size)!0};
    </script>

    <script>
        $(function () {
            $("#sendBtn").click(function () {
                var dialogId = $("#dialogId").val();
                var content = $("#messageInput").val().trim();

                if (!dialogId) {
                    alert("请先选择一个会话");
                    return;
                }
                if (!content) return;

                var _this = this;
                $(_this).button("loading");

                // 这里走你的后端接口，比如 /api/dialog/create
                // 你后端要做的事情通常是：
                // 1. 根据 receiverUsername / receiverUserId 定位对方用户
                // 2. 创建/查找 dialog 记录（dialog表：user_a_id,user_b_id,...）
                // 3. 在 message 表里插入第一条消息
                // 4. 返回 { code:200, detail:{dialogId:xxx} }
                req("post", "/api/messages/" + messageCount, {
                    dialogId:         dialogId,
                    content:          content
                }, "${_user.token!}", function (data) {
                    if (data.code === 200) {
                        suc("发送成功");
                        $("#messageArea").append(
                            '<div class="d-flex flex-column align-items-end mb-3">'
                            + '<div class="small text-muted mb-1">我 · 刚刚</div>'
                            + '<div class="p-2 rounded bg-success text-white" style="max-width:70%;">'
                            + $('<div/>').text(content).html()
                            + '</div></div>'
                        );
                        $("#messageInput").val("");
                        $("#messageArea").scrollTop($("#messageArea")[0].scrollHeight);
                    } else {
                        err(data.description || "发送失败");
                        $(_this).button("reset");
                    }
                });
            });
            // 回车发送
            $("#messageInput").keypress(function (e) {
                if (e.which === 13) {
                    e.preventDefault();
                    $("#sendBtn").click();
                }
            });
        });
    </script>

</@html>
