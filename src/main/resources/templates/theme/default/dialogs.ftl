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
                            <#if dialog??>
                                 ${model.formatDate(messages[messages?size-1].inTime)}
                            </#if>
                        </small>
                        <#-- 未读徽章之类你也可以放在这里，比如 activeDialog.unreadCount -->
                        <#if activeDialog?? && activeDialog.unreadCount?? && activeDialog.unreadCount?number gt 0>
                            <span class="badge badge-success">${activeDialog.unreadCount}</span>
                        </#if>
                    </div>

                    <!-- 中部：消息区域（可滚动，占据剩余空间） -->
<#--                    查询返回类型
                        dialogId:Integer
                        searchPage:Page<Message>
-->
                    <#if dialog??>
                        <form class="form-inline my-2 my-lg-0 ml-2 d-none d-md-block" action="/messages/search_message" method="get" style="position:relative;">
                            <input type="hidden" name="dialogId" value="${dialog.id}">
                            <div class="input-group" id="msg-search-box">
                                <input class="form-control" type="search" name="kw" placeholder="回车搜索" value="${kw!}" required aria-label="Search">
                                <div class="input-group-append">
                                    <button class="btn btn-outline-success" type="submit">${i18n.getMessage("search")}</button>
                                </div>
                            </div>

                            <div id="msg-search-result"
                                 class="list-group position-absolute w-100 shadow-sm"
                                 style="top:100%; left:0; max-height:320px; overflow:auto; z-index:1000; display:${(showSearch?? && showSearch)?string('block','none')}">
                                <#if searchPage?? && searchPage.records?size gt 0>
                                    <#list searchPage.records as m>
                                        <a class="list-group-item list-group-item-action"
                                           href="/dialogs/${dialog.id}?goto=${m.id}">
                                            ${m.message?html}
                                        </a>
                                    </#list>
                                <#else>
                                    <div class="list-group-item text-muted">没有结果</div>
                                </#if>
                            </div>
                        </form>
                    </#if>

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
                                        <div id="msg-${msg.id}" class="p-2 rounded bg-success text-white" style="max-width:70%;">
                                            ${msg.content!?html}
                                        </div>
                                    </div>
                                <#else>
                                    <div class="d-flex flex-column align-items-start mb-3">
                                        <div class="small text-muted mb-1">
                                            ${msg.senderUsername!toUser.username} · ${model.formatDate(msg.inTime)!}
                                        </div>
                                        <div id="msg-${msg.id}" class="p-2 rounded border bg-white" style="max-width:70%;">
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
<#--    搜素结果-消息高亮跳转-->
    <style>
        /* 明显的高亮闪烁动画 */
        @keyframes highlightFlash {
            0%   { background-color: #fff3cd; }   /* 亮黄 */
            50%  { background-color: #ffe082; }   /* 更亮一点 */
            100% { background-color: #fff3cd; }   /* 保持亮黄 */
        }

        /* 给目标消息添加阴影和边框 */
        .highlight-msg {
            animation: highlightFlash 1.5s ease-in-out infinite alternate;
            border: 2px solid #ffca28;
            box-shadow: 0 0 10px rgba(255, 202, 40, 0.8);
            border-radius: 12px;
            transition: all 0.3s ease;
        }
    </style>

    <script>
        (function() {
            var params = new URLSearchParams(location.search);
            var gotoId = params.get('goto');
            if (gotoId) {
                var el = document.getElementById('msg-' + gotoId);
                if (el) {
                    el.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    el.classList.add('highlight-msg');

                    // 5 秒后自动移除高亮
                    setTimeout(function() {
                        el.classList.remove('highlight-msg');
                    }, 5000);
                }
            }
        })();
    </script>
<#--    搜素结果-消息高亮跳转-->

    <!-- 消息搜索结果面板交互脚本 -->
    <script>
        $(function () {
            var $box = $("#msg-search-box");          // 包住输入框的容器
            var $panel = $("#msg-search-result");     // 下拉结果面板

            // 1) 点击页面空白处时关闭
            $(document).on("click", function (e) {
                if (!$(e.target).closest($box).length && !$(e.target).closest($panel).length) {
                    $panel.hide();
                }
            });

            // 2) 点击输入框或面板内部，阻止冒泡（避免被上面的 document 点击给关掉）
            $box.on("click", function (e) { e.stopPropagation(); });
            $panel.on("click", function (e) { e.stopPropagation(); });

            // 3) 输入框聚焦时显示（你也可以在有关键字且有结果时才显示）
            $box.find('input[name="kw"]').on("focus", function () {
                if ($panel.children().length > 0) $panel.show();
            });

            // 4) 按 ESC 关闭
            $(document).on("keydown", function (e) {
                if (e.key === "Escape") $panel.hide();
            });

            // 5) 点击某条结果后关闭（延时是为了让链接跳转不被阻断）
            $panel.on("click", "a.list-group-item", function () {
                setTimeout(function(){ $panel.hide(); }, 0);
            });

            // 6) 可选：窗口滚动/尺寸变化也关闭
            $(window).on("scroll resize", function(){ $panel.hide(); });
        });
    </script>


</@html>
