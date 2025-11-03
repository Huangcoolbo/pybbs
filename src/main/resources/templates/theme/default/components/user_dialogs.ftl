<#--会话列表-->
<#macro user_dialogs pageNo pageSize username isPaginate=false isFooter=false>
    <div class="card">
        <@tag_user_dialogs username=username pageNo=pageNo pageSize=pageSize>
            <div class="card-header">${username} 的会话</div>
            <#if dialogs.total == 0>
                <div class="card-body text-center text-muted">暂无会话</div>
            <#else>
                <div class="card-body paginate-bot p-0">
                    <ul class="list-group list-group-flush" id="dialogList">
                        <#list dialogs.records as dialog>
                            <li class="list-group-item d-flex justify-content-between align-items-start dialog-item"
                                data-dialog-id="${dialog.id}">
                                <div class="flex-grow-1">
                                    <div class="d-flex justify-content-between">
                                        <div class="font-weight-bold">
                                            <a href="/dialogs/${dialog.id}" class="text-body">
                                                <i class="fa fa-user"></i>
                                                ${dialog.username!?html}
                                            </a>
                                        </div>
<#--                                        <small class="text-muted">-->
<#--                                            ${model.friendlyTime(dialog.inTime)!""}-->
<#--                                        </small>-->
                                    </div>
                                    <div class="text-muted small text-truncate" style="max-width:220px;">
<#--                                        dialog没有username字段，为什么这里能用？-->
                                        ${dialog.lastusername!?html} · ${dialog.lastMessage!?html}
                                    </div>
                                </div>
                                <button type="button"
                                        class="btn btn-sm btn-outline-danger ml-2 dlt-btn"
                                        title="删除会话">
                                    <i class="fa fa-trash"></i>
                                </button>
                                <#if dialog_has_next>
                                    <div class="divide mt-1 mb-1"></div>
                                </#if>
                            </li>
                        </#list>
                    </ul>
                    <style>
                        /* 行本身给个基准定位和悬浮反馈（可选） */
                        #dialogList .dialog-item { position: relative; transition: background-color .2s; }
                        #dialogList .dialog-item:hover { background-color: #f8f9fa; }

                        /* 删除按钮：默认隐藏且不拦截鼠标 */
                        #dialogList .dialog-item .dlt-btn {
                            opacity: 0;
                            pointer-events: none;
                            transition: opacity .2s;
                        }

                        /* 悬浮该行时显示按钮并可点击 */
                        #dialogList .dialog-item:hover .dlt-btn {
                            opacity: 1;
                            pointer-events: auto;
                        }
                    </style>

                </div>

                <#if isPaginate>
                    <#include "paginate.ftl"/>
                    <@paginate currentPage=dialogs.current totalPage=dialogs.pages
                    actionUrl="/user/${username}/dialogs" urlParas=""/>
                </#if>

                <#if isFooter>
                    <div class="card-footer">
                        <a href="/user/${username}/dialogs">${username} 更多会话&gt;&gt;</a>
                    </div>
                </#if>
            </#if>
        </@tag_user_dialogs>
    </div>

<#-- 删除确认模态框 -->
    <div class="modal fade" id="confirmDeleteDialog" tabindex="-1" role="dialog" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered" role="document">
            <div class="modal-content">
                <div class="modal-header"><h5 class="modal-title">删除会话</h5></div>
                <div class="modal-body">确定要删除这个会话吗？该操作会清空该会话下的消息。</div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-dismiss="modal">取消</button>
                    <button type="button" class="btn btn-danger" data-dismiss="modal" id="confirmDeleteBtn">删除</button>
                </div>
            </div>
        </div>
    </div>

    <script>
        let toDeleteDialogId = null;

        // 点击垃圾桶按钮
        $(document).on('click', '.dialog-item .dlt-btn', function (e) {
            e.preventDefault();
            e.stopPropagation();
            const li = $(this).closest('.dialog-item');
            toDeleteDialogId = li.data('dialog-id');
            $('#confirmDeleteDialog').modal('show');
        });

        // 确认删除
        $('#confirmDeleteBtn').on('click', function () {
            if (!toDeleteDialogId) return;

            $.ajax({
                url: '/api/dialogs/' + toDeleteDialogId,
                method: 'DELETE',
                headers: { 'token': '${_user.token!''}' },
                success: function (resp) {
                    if (resp == null || resp.code === 200) {
                        const li = $('.dialog-item[data-dialog-id="'+ toDeleteDialogId +'"]');
                        li.slideUp(160, () => li.remove());
                        $('#confirmDeleteDialog').modal('hide');
                        toDeleteDialogId = null;
                    } else {
                        alert(resp.description || '删除失败');
                    }
                },
                error: function () {
                    err('删除失败');
                }
            });
        });
    </script>
</#macro>
