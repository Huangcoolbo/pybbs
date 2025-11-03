<#if _user??>
<#if site.websocket == "1">
    <script>
        if (window.WebSocket) {
            var documentTitle = document.title;
            var socket = new WebSocket('${site.websocket_url}');

            function emit(type, payload) {
                socket.send(JSON.stringify({type: type, payload: payload}));
            }

            // 连接建立时触发
            socket.onopen = function (event) {
                emit('bind', {userId: ${_user.id}, username: '${_user.username!}'});
            }

            socket.onclose = function (event) {
            }

            socket.onerror = function (event) {
                console.log('websocket error: ', event);
            }
            // 客户端接收服务端发送(sendObject())的消息
            socket.onmessage = function (event) {
                var data = JSON.parse(event.data);
                if (data.type === 'bind') {
                    // suc('ws与当前登录用户绑定成功');
                    console.log("ws与当前登录用户绑定成功");
                    // 绑定成功后找server要当前未读消息数量
                    emit('notReadCount', {});
                } else if (data.type === 'dialog_message') {
                    // 私信消息
                    tip(data.payload);
                    setInterval(function() {
                        location.reload();
                    }, 1000);
                } else if (data.type === 'notifications') {
                    // 弹出消息提示
                    tip(data.payload);
                } else if (data.type === 'notification_notread') {
                    var n_count = $("#n_count");
                    var nh_count = $("#nh_count");
                    // 加上新收到的未读消息数量
                    if (n_count) n_count.text(parseInt(n_count.text()) + data.payload);
                    var notReadTotalCount = 0;
                    if (nh_count.text().length === 0) {
                        notReadTotalCount = data.payload;
                    } else {
                        notReadTotalCount = parseInt(nh_count.text()) + data.payload;
                    }
                    if (notReadTotalCount > 0) {
                        nh_count.text(notReadTotalCount);
                        // 网页标签页（tab）上显示的标题文本
                        document.title = "(" + notReadTotalCount + ") " + documentTitle;
                    }
                } else if (data.type === 'notification_group') {
                    // 按组群发
                    tip(data.payload);
                }

            }

        }
    </script>
<#else>
    <script>
        var title = document.title;

        function notificationCount() {
            req("get", "/api/notification/notRead", "${_user.token!}", function (data) {
                if (data.code === 200 && data.detail > 0) {
                    $("#n_count").text(data.detail);
                    document.title = "(" + data.detail + ") " + title;
                }
            });
        }

        notificationCount();
        // setInterval(function () {
        //   notificationCount();
        // }, 120000);
        </#if>
    </script>
</#if>
