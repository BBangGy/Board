const $myPageForm = document.getElementById('myPageForm');

$myPageForm.onsubmit=(e)=>{
    e.preventDefault();
    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('email', $myPageForm['email'].value);
    formData.append('name', $myPageForm['name'].value);
    formData.append('nickname', $myPageForm['nickname'].value);
    formData.append('birth', $myPageForm['birth'].value);
    formData.append('contactFirst', $myPageForm['contactFirst'].value);
    formData.append('contactSecond', $myPageForm['contactSecond'].value);
    formData.append('contactThird', $myPageForm['contactThird'].value);
    formData.append('addressPostal', $myPageForm['addressPostal'].value);
    formData.append('addressPrimary', $myPageForm['addressPrimary'].value);
    formData.append('addressSecondary', $myPageForm['addressSecondary'].value);
    xhr.onreadystatechange=()=>{
        if(xhr.readyState !== XMLHttpRequest.DONE){
            return;
        }
        if (xhr.status < 200 || xhr.status >= 300){

            return;
        }
        const response = JSON.parse(xhr.responseText);
        switch (response.result) {
            case 'success':
                dialog.showSimpleOk('개인정보 수정','개인정보 수정이 성공적으로 실행되었습니다.',{
                    buttonCaption:"확인",
                    buttonColor:'green',
                    onOkCallback:()=>{
                        location.href = '/board/list?id=free';
                    }
                });
                break;
            case 'failure':
                dialog.showSimpleOk('개인정보 수정', '알수 없는 이유로 개인정보를 수정하지 못하였습니다.');
                break;
            default:
                dialog.showSimpleOk('개인정보 수정', `${xhr.status}`);
                break;
        }

    };
    xhr.open('PATCH',"/user/my");
    xhr.send(formData);
}

$myPageForm['findAddress'].addEventListener('click',()=>{
    const $addressFindDialog = document.getElementById('addressFindDialog');
    const $modal = $addressFindDialog.querySelector(':scope>.modal');
    $addressFindDialog.onclick=()=>{
        $addressFindDialog.hide();
    }
    new daum.Postcode({
        width: '100%',
        height: '100%',
        oncomplete: (data) => {
            console.log(data);
            $addressFindDialog.hide();
            $myPageForm['addressPostal'].value = data['zonecode'];
            $myPageForm['addressPrimary'].value = data['roadAddress'];
            $myPageForm['addressSecondary'].focus();
            $myPageForm['addressSecondary'].select();
        }
    }).embed($modal);
    $addressFindDialog.show();
})

$myPageForm['nicknameCheck'].addEventListener('click',()=>{
    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('nickname', $myPageForm['nickname'].value);
    xhr.onreadystatechange=()=>{
        if(xhr.readyState !== XMLHttpRequest.DONE){
            return;
        }
        if (xhr.status < 200 || xhr.status >= 300){
            dialog.showSimpleOk('중복 검사', `${xhr.status}`);
            return;
        }
        const response = JSON.parse(xhr.responseText);
        switch (response.result) {
            case 'success':
                dialog.show({
                    title: '닉네임 중복 확인',
                    content: `압력하신 닉네임 '${$myPageForm['nickname'].value}' 은/는 사용가능합니다. 이 닉네임을 사용할까요?`,
                    buttons: [
                        {
                            caption: '아니요',
                            onclick: ($modal) => {
                                dialog.hide($modal)
                            }
                        },
                        {
                            caption: '네',
                            color: 'green',
                            onclick: ($modal) => {
                                dialog.hide($modal);
                                $myPageForm['nickname'].setDisabled(true);
                                $myPageForm['nicknameCheckButton'].setDisabled(true);
                            }
                        }
                    ]
                });
                break;
            case 'failure_duplicate':
                dialog.showSimpleOk('닉네임 중복 확인', `입력하신 닉네임 '${$myPageForm['nickname'].value}'는 이미 사용중입니다.`, () => {
                    $myPageForm['nickname'].focus();
                    $myPageForm['nickname'].select();
                });
                break;
            default:
                dialog.showSimpleOk('닉네임 중복 확인', '알 수 없는 이유로 닉네임 중복을 확인하지 못하였습니다. 잠시후 다시 시도해 주세요.');
                break;
        }
    };
    xhr.open('POST',"/user/nickname-check");
    xhr.send(formData);
})