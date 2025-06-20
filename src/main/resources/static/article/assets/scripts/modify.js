const $modifyForm = document.getElementById('modifyForm');
const titleRegex = new RegExp('^(.{1,100})$');
const contentRegex = new RegExp('^(.{1,100000})$');
const $loading = document.getElementById('loading');
$modifyForm.onsubmit=(e)=>{
    e.preventDefault();
    if ($modifyForm['title'].value === '') {
        dialog.showSimpleOk('게시글 작성','제목을 입력해 주세요.',{
            onOkCallback: () => $modifyForm['title'].focus(),
        });
        return;
    }
    if(!titleRegex.test($modifyForm['title'].value)){
        dialog.showSimpleOk('게시글 작성', '올바른 제목을 입력해 주세요,',{
            onOkCallback: () => $modifyForm['title'].focus(),
        })
        return;
    }
    if (editor.getData() === '') {
        dialog.showSimpleOk('게시글 작성', '내용을 입력해 주세요,',{
            onOkCallback: () => $modifyForm['content'].focus(),
        })
        return;
    }
    if (!contentRegex.test(editor.getData())) {
        dialog.showSimpleOk('게시글 작성', '올바른 내용을 입력해 주세요,',{
            onOkCallback: () => $modifyForm['content'].focus(),
        })
        return;
    }
    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append("id", $modifyForm['id'].value);
    formData.append("title", $modifyForm['title'].value);
    formData.append("content",editor.getData())
    xhr.onreadystatechange=()=>{
        if(xhr.readyState !== XMLHttpRequest.DONE){
            return;
        }
        $loading.hide();
        if (xhr.status < 200 || xhr.status >= 300){
            dialog.showSimpleOk('게시글 작성', `${xhr.status}`);
            return;
        }
        const response = JSON.parse(xhr.responseText);
        switch (response.result) {
            case 'success':
                location.href = `/article/?id=${$modifyForm["id"].value}`;
                break;
            case'failure_absent':
                dialog.showSimpleOk('게시글 수정', '존재하지 않는 게시글입니다.',{
                    onOkCallback:()=>{
                        if (history.length > 1) {
                            history.back();
                        }else{
                            close();
                        }
                    }
                });
                break;
            case'failure_session_expired':
                dialog.showSimpleOk('게시글 수정', '세션이 만료되었거나 게시글을 수정할 권한이 없습니다. 관리자에게 문의해 주세요.');
                break;
            default:
                dialog.showSimpleOk('게시글 수정', '알 수 없는 이유로 게시글을 수정하지 못하였습니다. 잠시 후 다시 시도해 주세요.');
                break;
        }
    };
    xhr.open('PATCH',"/article/");
    xhr.send(formData);
    $loading.show();
}
