const $conatiner = document.getElementById('container')
const $commentForm = document.getElementById('commentForm');
const $commentContainer = document.getElementById('commentContainer');

const deleteArticle = () => {
    const url = new URL(location.href);
    const id = url.searchParams.get('id');

    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('id', id);
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        if (xhr.status < 200 || xhr.status >= 300) {
            dialog.showSimpleOk('게시글 삭제', '요청을 처리하는 도중 오류가 발생하였습니다. 잠시 후 다시 시도해 주세요.');
            return;
        }
        const response = JSON.parse(xhr.responseText);
        switch (response.result) {
            case 'success':
                dialog.showSimpleOk('게시글 삭제', '게시글을 성공적으로 삭제하였습니다.', {
                    onOkCallback: () => $conatiner.querySelector('.to-list').click(),
                })
                break;
            case 'failure_session_expired':
                dialog.showSimpleOk('게시글 삭제', '세션이 만료되었거나 게시글을 삭제할 권한이 없습니다. 관리자에게 문의해 주세요');
                break;
            case'failure_absent':
                dialog.showSimpleOk('게시글 삭제', '게시글이 더이상 존재하지 않습니다.', {
                    onOkCallback: () => $conatiner.querySelector('.to-list').click(),
                })
                break;
            default:
                dialog.showSimpleOk(
                    '게시글 삭제', '알수 없는 이유로 게시글을 삭제하지 못하였습니다. 잠시후 다시 시도해 주세요.'
                );
        }
    };
    xhr.open('DELETE', '/article/');
    xhr.send(formData);
}

const appendComments = (targetComments, wholeComments, step) => {
    for (const comment of targetComments) {
        $commentContainer.insertAdjacentHTML('beforeend', `
         <div class="comment ${comment['mine'] === true ? '-mine' : ''} ${comment['deleted'] === true ? '-deleted' : ''}" style="margin-left: ${step * 1.5}rem">
                <div class="head">
                    <span class="writer">${comment['userNickname']}</span>
                    <span class="timestamp">${comment['createdAt'].split('T').join(' ')}</span>
                    <span class="-flex-stretch"></span>
                    ${comment['deleted'] === true ? '' : `
                    <label class="action">
                        <input hidden name="modifyCheck" type="checkbox">
                        <span class="caption"></span>
                    </label>
                    <a class="action delete" data-id="${comment['id']}">삭제</a>
                    `}
                </div>
                <label class="body">
                    <input hidden name="replyCheck" type="checkbox">
                    <span class="content">${comment['deleted'] === true ? '(삭제된 댓글입니다.)' : comment['content']}</span>
                </label>
                <form class="modify-form">
                    <input hidden name="id" type="hidden" value="${comment['id']}">
                    <label class="--object-label -flex-stretch">
                        <textarea name="content" rows="4" class="--object-field ---field" minlength="1" maxlength="1000" placeholder="댓글을 입력해주세요." required>${comment['content']}</textarea>
                    </label>
                    <button class="--object-button -color-green" type="submit">댓글 수정하기</button>
                </form>
                <form class="reply-form">
                    <input type="hidden" hidden name="articleId" value="${comment['articleId']}">
                    <input hidden name="commentId" type="hidden" value="${comment['id']}">
                    <label class="--object-label -flex-stretch">
                        <textarea name="content" rows="4" class="--object-field ---field" minlength="1" maxlength="1000" placeholder="댓글을 입력해주세요." required></textarea>
                    </label>
                    <button class="--object-button -color-green" type="submit">댓글 등록하기</button>
                </form>
            </div>`);
        const nextComments = wholeComments.filter((nextComment) => nextComment.commentId === comment.id);
        if (nextComments.length > 0) {
            appendComments(nextComments, wholeComments, step + 1);
        }
    }
}

const deleteComment = (id) => {
    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('id', id);
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        if (xhr.status < 200 || xhr.status >= 300) {
            dialog.showSimpleOk('댓글 삭제', '요청을 처리하는 도중 오류가 발생하였습니다. 잠시 후 다시 시도해 주세요.');
            return;
        }
        const response = JSON.parse(xhr.responseText);
        switch (response.result) {
            case'failure_absent':
                dialog.showSimpleOk('댓글 삭제', '삭제하려는 댓글이 더이상 존재하지 않습니다.')
                break;
            case'failure_session_expired':
                dialog.showSimpleOk('댓글 삭제', '세션이 만료되었거나 댓글을 삭제할 권한이 없습니다. 관리자에게 문의해 주세요.')
                break;
            case 'success':
                dialog.showSimpleOk('댓글 삭제', '댓글을 삭제하였습니다.', {
                    onOkCallback: () => loadComments(),
                })
                break;
            default:
                dialog.showSimpleOk('댓글 삭제', '일 수 없는 이유로 댓글을 삭제하지 못하였습니다. 잠시후 다시 시도해 주세요.');
                break;

        }
    };
    xhr.open('DELETE', '/article/comment');
    xhr.send(formData);
}
/** @param{{id:number,content:string}} args */
const modifyComment = (args) => {
    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('id', args.id);
    formData.append('content', args.content);
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        if (xhr.status < 200 || xhr.status >= 300) {
            dialog.showSimpleOk('댓글 수정', '요청을 처리하는 도중 오류가 발생하였습니다. 잠시 후 다시 시도해 주세요.');
            return;
        }
        const response = JSON.parse(xhr.responseText);
        switch (response.result) {
            case'failure_absent':
                dialog.showSimpleOk('댓글 수정', '수정하려는 댓글이 더이상 존재하지 않습니다.')
                break;
            case'failure_session_expired':
                dialog.showSimpleOk('댓글 수정', '세션이 만료되었거나 댓글을 수정할 권한이 없습니다. 관리자에게 문의해 주세요.')
                break;
            case 'success':
                dialog.showSimpleOk('댓글 수정', '댓글을 수정하였습니다.', {
                    onOkCallback: () => loadComments(),
                })
                break;
            default:
                dialog.showSimpleOk('댓글 수정', '일 수 없는 이유로 댓글을 수정하지 못하였습니다. 잠시후 다시 시도해 주세요.');
                break;
        }
    };
    xhr.open('PATCH', '/article/comment');
    xhr.send(formData);
}
const loadComments = () => {
    const $title = $commentForm.querySelector(':scope>.title');
    $title.innerText = '댓글(불러오는 중)';
    $commentContainer.innerHTML = '';
    const xhr = new XMLHttpRequest();
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        if (xhr.status < 200 || xhr.status >= 300) {
            $commentContainer.innerText = '댓글을 불러오지 못하였습니다. 잠시후 다시 시도해 주세요.';
            return;
        }

        const comments = JSON.parse(xhr.responseText);
        if (comments.length === 0) {
            $commentContainer.innerHTML = '등록된 댓글이 없습니다.';
            return;
        }

        const targetComments = comments.filter((comment) => comment.commentId === null); //원댓글을 부른다.
        appendComments(targetComments, comments, 0);
        $commentContainer.querySelectorAll('.modify-form').forEach(($modifyForm) => {
            $modifyForm.addEventListener('submit', (e) => {
                e.preventDefault();
                if ($modifyForm['content'].value === '') {
                    dialog.showSimpleOk('댓글 작성', '내용을 입력해 주세요.');
                    return;
                }
                modifyComment({
                    id: $modifyForm['id'].value,
                    content: $modifyForm['content'].value
                });
            })
        })

        $commentContainer.querySelectorAll('.action.delete').forEach(($anchor) => $anchor.addEventListener('click', (e) => {
            e.preventDefault();
            dialog.show({
                title: '댓글삭제',
                content: '정말로 댓글을 삭제할까요? 삭제된 댓글은 복구가 어렵습니다.',
                buttons: [
                    {
                        caption: '취소',
                        onclick: ($modal) => dialog.hide($modal)
                    },
                    {
                        caption: '삭제',
                        color: 'red',
                        onclick: ($modal) => {
                            dialog.hide($modal);
                            deleteComment($anchor.dataset['id'])
                        }
                    }]
            })
        }))

        $commentContainer.querySelectorAll('.reply-form').forEach(($replyForm) => {
            $replyForm.addEventListener('submit', (e) => {
                e.preventDefault();
                if ($replyForm['content'].value === '') {
                    dialog.showSimpleOk('댓글 작성', '내용을 입력해 주세요.');
                }
                $writeComment({
                    //대댓글이기에 commentid가 필요
                    articleId: $replyForm['articleId'].value,
                    commentId: $replyForm['commentId'].value,
                    content: $replyForm['content'].value
                })
            })
        })
        $title.innerText = `댓글 (${comments.length})`
    }
    xhr.open('GET', `${origin}/article/comment?id=` + new URL(location.href).searchParams.get('id'));
    xhr.send();
}

const $writeComment = (args) => {
    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('articleId', args['articleId']);
    formData.append("content", args['content']);
    if (args['commentId'] != null) {
        formData.append('commentId', args['commentId']);
    }
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        if (xhr.status < 200 || xhr.status >= 300) {
            dialog.showSimpleOk('댓글 작성', '요청을 처리하는 도중 오류가 발생하였습니다. 잠시 후 다시 시도해 주세요.')
            return;
        }
        const response = JSON.parse(xhr.responseText)
        switch (response.result) {
            case 'success':
                $commentForm['content'].value = '';
                $commentForm['content'].focus();
                loadComments();
                break;
            case'failure_session_expired':
                dialog.showSimpleOk('댓글 작성', '세션이 만료되었거나 댓글을 작성할 권한이 없습니다. 관리자에게 문의해 주세요.');
                break;
            default:
                dialog.showSimpleOk('댓글 작성', '알 수 없는 이유로 댓글을 작성하지 못하였습니다. 잠시 후 다시 시도해 주세요.');
                break;

        }
    };
    xhr.open('POST', '/article/comment');
    xhr.send(formData);
}

$conatiner.querySelector('[name="delete"]')?.addEventListener('click', () => {
    dialog.show({
        title: '게시글 삭제',
        content: '정말로 게시글을 삭제할까요? 삭제된 게시글은 복구가 어렵습니다.',
        buttons: [
            {
                caption: '취소', onclick: ($modal) => {
                    dialog.hide($modal);
                }
            },
            {
                caption: '삭제', color: 'red', onclick: ($modal) => {
                    dialog.hide($modal);
                    deleteArticle();
                }
            }
        ]
    })
})

$commentForm.addEventListener('submit', (e) => {
        e.preventDefault();
        if ($commentForm['content'].value === '') {
            dialog.showSimpleOk('댓글 작성', '내용을 입력해주세요.'
            );
            return;
        }
        $writeComment({
            //대댓글이 아니기에 commentId가 필요 없음.
            articleId: new URL(location.href).searchParams.get('id'),
            content: $commentForm['content'].value
        });
    }
);
loadComments();