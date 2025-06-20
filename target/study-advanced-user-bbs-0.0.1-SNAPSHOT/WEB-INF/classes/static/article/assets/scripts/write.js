const $writeForm = document.getElementById('writeForm');
const titleRegex = new RegExp('^(.{1,100})$');
const contentRegex = new RegExp('^(.{1,100000})$');
const $loading = document.getElementById('loading');
$writeForm.onsubmit = (e) => {
    e.preventDefault();
    if ($writeForm['title'].value === '') {
        dialog.showSimpleOk('게시글 작성', '제목을 입력해 주세요.', {
            onOkCallback: () => $writeForm['title'].focus(),
        });
        return;
    }
    if (!titleRegex.test($writeForm['title'].value)) {
        dialog.showSimpleOk('게시글 작성', '올바른 제목을 입력해 주세요,', {
            onOkCallback: () => $writeForm['title'].focus(),
        })
        return;
    }
    if (editor.getData() === '') {
        dialog.showSimpleOk('게시글 작성', '내용을 입력해 주세요,', {
            onOkCallback: () => $writeForm['content'].focus(),
        })
        return;
    }
    if (!contentRegex.test(editor.getData())) {
        dialog.showSimpleOk('게시글 작성', '올바른 내용을 입력해 주세요,', {
            onOkCallback: () => $writeForm['content'].focus(),
        })
        return;
    }
    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append("boardId", $writeForm['boardId'].value);
    formData.append("title", $writeForm['title'].value);
    formData.append("content", editor.getData())
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        $loading.hide();
        if (xhr.status < 200 || xhr.status >= 300) {
            dialog.showSimpleOk('게시글 작성', `${xhr.status}`);
            return;
        }
        const response = JSON.parse(xhr.responseText);
        switch (response.result) {
            case 'success':
                location.href = `/article/?id=${response.id}`;
                break;
            case'failure_session_expired':
                dialog.showSimpleOk('게시글 작성', '세션이 만료되었거나 게시글을 작성할 권한이 없습니다. 관리자에게 문의해 주세요.');
                break;
            default:
                dialog.showSimpleOk('게시글 작성', '알 수 없는 이유로 게시글을 작성하지 못하였습니다. 잠시 후 다시 시도해 주세요.');
                break;
        }
    };
    xhr.open('POST', "/article/");
    xhr.send(formData);
    $loading.show();
}
$writeForm['cancel'].addEventListener('click', () => {
    history.back();
})
