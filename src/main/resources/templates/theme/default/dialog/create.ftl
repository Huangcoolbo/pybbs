<#include "../layout/layout.ftl"/>
<@html page_title="发私信" page_tab="">
    <div class="row">
        <!-- 左侧：发起会话 -->
        <div class="col-md-9">
            <div class="card">
                <div class="card-header">发起会话 / 私信</div>
                <div class="card-body">
                    <form action="" onsubmit="return false;" id="dialogForm" method="post">
                        <!-- 收件人用户名 -->
                        <div class="form-group">
                            <label for="receiverUsername">收件人用户名</label>
                            <input type="text"
                                   name="receiverUsername"
                                   id="receiverUsername"
                                   class="form-control"
                                   placeholder="请输入对方的用户名（必填）"/>
                            <small id="receiverHint" class="form-text text-muted"></small>
                        </div>
<#--                        Todo: 判断填入的用户是否已存在会话列表中
                            Todo: 如果填入的用户是自己-->

<#--                        <!-- 收件人ID（可选辅助/调试） &ndash;&gt;-->
<#--                        <div class="form-group">-->
<#--                            <label for="receiverUserId">收件人ID（可选）</label>-->
<#--                            <input type="text"-->
<#--                                   name="receiverUserId"-->
<#--                                   id="receiverUserId"-->
<#--                                   class="form-control"-->
<#--                                   placeholder="如果你知道对方的用户ID，可以填；否则留空，由后端自动解析"/>-->
<#--                            <small class="text-muted">-->
<#--                                如果留空，后端会通过用户名查询 user.id。-->
<#--                            </small>-->
<#--                        </div>-->

                        <!-- 收件人邮箱（可选） -->
                        <div class="form-group">
                            <label for="receiverEmail">收件人邮箱（可选）</label>
                            <input type="email"
                                   name="receiverEmail"
                                   id="receiverEmail"
                                   class="form-control"
                                   placeholder="可用于校验你有没有发错人，比如 someone@example.com"/>
                            <small class="text-muted">
                                如果和系统中该用户邮箱不一致，可以提示你可能选错人（由后端决定怎么处理）。
                            </small>
                        </div>

                        <!-- 第一条消息正文 -->
                        <div class="form-group">
                            <label for="content">消息内容</label>
                            <#-- 根据站点配置选择 MD 编辑器或富文本编辑器，和发帖复用 -->
                            <#if site?? && site.content_style?? && site.content_style == "MD">
                                <span class="pull-right">
                                    <a href="javascript:uploadFile('topic')">上传图片</a>&nbsp;
                                    <a href="javascript:uploadFile('video')">上传视频</a>
                                </span>
                            </#if>

                            <#include "../components/editor.ftl"/>
                            <@editor _type="dialog" style="${site.content_style!'MD'}"/>

                            <small class="text-muted">
                                这条消息会作为该会话的第一条私信发送给对方。
                            </small>
                        </div>

                        <!-- 隐藏字段之类（如果你想预留 tag、扩展字段也可以类似加） -->
                        <input type="hidden" name="token" id="token" value="${_user.token!}"/>

                        <!-- 提交按钮 -->
                        <div class="form-group">
                            <button type="button" id="sendBtn" class="btn btn-info">发送私信</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <!-- 右侧：一些发送小提示 / 规则说明 -->
        <div class="col-md-3 hidden-xs">
            <div class="card">
                <div class="card-header">发送说明</div>
                <div class="card-body small text-muted">
                    <ul class="pl-3 mb-0">
                        <li>请确认收件人用户名拼写正确。</li>
                        <li>首次发送会自动创建会话；之后双方的消息都会进入该会话。</li>
                        <li>禁止发送违法、广告、垃圾信息。</li>
                    </ul>
                </div>
            </div>

            <#-- 如果你有 markdown_guide 或 create_topic_guide 这类组件，也可以在这里 include -->
            <#if site?? && site.content_style?? && site.content_style == "MD">
                <#include "../components/markdown_guide.ftl"/>
            </#if>
        </div>
    </div>

    <script>
        // 简单防抖
        function debounce(fn, delay) {
            let t; return function (...args) {
                clearTimeout(t); t = setTimeout(() => fn.apply(this, args), delay);
            }
        }

        const $input = $('#receiverUsername');
        const $hint  = $('#receiverHint');
        const $submitBtn = $('#sendBtn'); // 你的“发起会话”按钮id（自己改成实际id）

        function setState(ok, text, linkHtml) {
            $hint
                .removeClass('text-success text-danger text-warning')
                .addClass(ok ? 'text-success' : 'text-danger')
                .html(linkHtml || text || '');
            $submitBtn.prop('disabled', !ok);
        }

        // 输入时做校验
        $input.on('input', debounce(function () {
            const name = $input.val().trim();
            if (!name) { setState(false, '请输入用户名'); return; }

            // 立即拦截：不能给自己发（前端就可以先判一次，后端仍要再判）
            const me = '${user.username!}';
            if (name.toLowerCase() === (me || '').toLowerCase()) {
                setState(false, '不能给自己发私信');
                return;
            }
            // 远端校验
            $.ajax({
                url: '/api/dialogs/check',
                method: 'GET',
                data: { username: name },
                headers: { 'token': '${_user.token!""}' },
                success: function (resp) {
                    // 期望返回结构见下文后端
                    if (resp.code !== 200) {
                        setState(false, resp.description || '校验失败');
                        return;
                    }
                    const d = resp.detail || {};
                    if (!d.userBId) {
                        setState(false, '用户不存在');
                        return;
                    }
                    if (d.userBId === ${user.id!0}) {
                        setState(false, '不能自己发私信' + ${user.id!0});
                        return;
                    }
                    if (d.id) {
                        const linkHtml = '已存在会话<a href="/dialogs/' + d.id + '">点击进入</a>';
                        // 允许继续发起（复用旧会话），也可以选择禁用按钮，这里默认允许
                        $hint.removeClass('text-danger').addClass('text-warning').html(linkHtml);
                        $submitBtn.prop('disabled', true);
                        return;
                    }
                    setState(true, '可以发起会话');
                },
                error: function () {
                    setState(false, '网络错误');
                }
            });
        }, 300));
    </script>

    <script>
        $(function () {
            $("#sendBtn").click(function () {
                var receiverUsername = $("#receiverUsername").val();
                var receiverEmail    = $("#receiverEmail").val();

                // 内容根据站点编辑器类型取值（和发话题那段逻辑一致）
                var content = window.editor
                    ? window.editor.getDoc().getValue()       // Markdown 模式（CodeMirror）
                    : window._E.txt.html();                   // 富文本模式（wangEditor 之类）

                if (!receiverUsername) {
                    err("请输入收件人用户名");
                    return;
                }

                if (!content || content.trim().length === 0) {
                    err("请输入消息内容");
                    return;
                }

                var _this = this;
                $(_this).button("loading");

                // 这里走你的后端接口，比如 /api/dialog/create
                // 你后端要做的事情通常是：
                // 1. 根据 receiverUsername / receiverUserId 定位对方用户
                // 2. 创建/查找 dialog 记录（dialog表：user_a_id,user_b_id,...）
                // 3. 在 message 表里插入第一条消息
                // 4. 返回 { code:200, detail:{dialogId:xxx} }
                req("post", "/api/dialogs/create", {
                    username: receiverUsername,
                    email:    receiverEmail,
                    content:          content
                }, "${_user.token!}", function (data) {
                    if (data.code === 200) {
                        suc("发送成功");
                        setTimeout(function () {
                            // 跳转到这个会话的详情页，比如 /dialogs/{id}
                            window.location.href = "/dialogs/" + data.detail.id;
                        }, 700);
                    } else {
                        err(data.description || "发送失败");
                        $(_this).button("reset");
                    }
                });
            });
        });
    </script>


</@html>
