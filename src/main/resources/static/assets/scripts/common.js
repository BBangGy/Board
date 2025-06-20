HTMLElement.VISIBLE_CLASS = '-visible';
HTMLElement.SELECTED_CLASS = '-selected';
/**
 * 요소에서 클래스를 제거하여 숨깁니다.
 * @returns {HTMLElement}
 */
HTMLElement.prototype.hide = function () {
    this.classList.remove(HTMLElement.VISIBLE_CLASS);
    return this;
}
/**
 * 요소가 선택된 상태인가의 여부를 반환합니다.
 * @returns {boolean}
 */
HTMLElement.prototype.isSelected = function () {
    return this.classList.contains(HTMLElement.SELECTED_CLASS);
}
/**
 * 요소가 보여지는 상태인가의 여부를 반환합니다.
 * @returns {boolean}
 */
HTMLElement.prototype.isVisible = function () {
    return this.classList.contains(HTMLElement.VISIBLE_CLASS);
}
/**
 * '-selected' 클래스를 통해 요소를 선택된 상태로 지정할지 설정합니다.
 * @param {boolean} b 'boolean'요소를 선택된 상태로 지정할지의 여부.
 * @returns {HTMLElement}
 */

/**
 *
 * @param {boolean} b
 * @return {HTMLElement}
 */
HTMLElement.prototype.setDisabled = function (b) {
    if (b === true) {
        this.setAttribute('disabled', '');
    } else if (b === false) {
        this.removeAttribute('disabled');
    }
    return this;
}

/**
 * '-selected' 클래스를 통해 요소를 선택된 상태로 지정할지 설정하빈다.
 * @param b 'boolean' 요소를 선택된 상태로 지정할지의 여부
 * @returns {HTMLElement}
 */
HTMLElement.prototype.setSelected = function (b) {
    if (b === true) {
        this.classList.add(HTMLElement.SELECTED_CLASS);
    } else if (b === false) {
        this.classList.remove(HTMLElement.SELECTED_CLASS);
    }
    return this;
}
/**
 * '-visible'클래스를 통해 요소를 보이게 할지 설정합니다.
 * @param {boolean}b 'boolean' 요소를 보이게 할지의 여부
 * @returns {HTMLElement}
 */
HTMLElement.prototype.setVisible = function (b) {
    if (b === true) {
        this.classList.add(HTMLElement.VISIBLE_CLASS);
    } else if (b === false) {
        this.classList.remove(HTMLElement.VISIBLE_CLASS);
    }
    return this;
}
/**
 * 요소에 '-visible' 클래스를 추가하여 보이게 합니다.
 * @returns {HTMLElement}
 */
HTMLElement.prototype.show = function () {
    this.classList.add(HTMLElement.VISIBLE_CLASS);
    return this;
}

HTMLLabelElement.INVALID_CLASS = '-invalid';

/**
 *
 * @param {boolean} b
 * @param {string| undefined} warningText
 * @returns {HTMLLabelElement}
 */
HTMLLabelElement.prototype.setValid = function (b, warningText = undefined) {
    if (b === true) {
        this.classList.remove(HTMLLabelElement.INVALID_CLASS);
    } else if (b === false) {
        const $warning = this.querySelector(':scope>.---warning');
        if ($warning != null && warningText != null) {
            $warning.innerText = warningText
        }
        this.classList.add(HTMLLabelElement.INVALID_CLASS);
    }
    return this;
}



class Dialog {
    /**
     * @private
     * @type {HTMLElement}
     */
    #$element;
    /**
     * @private
     * @type {HTMLElement}
     */
    #$modals = [];

    constructor() {
        let $element = document.body.querySelector('.--dialog');
        if ($element === null) {
            $element = document.createElement('div');
            $element.classList.add('--dialog');
            document.body.insertAdjacentElement('afterbegin', $element);
            //                                              ⬆ body의 첫번재 자식으로 만든다.
        }
        this.#$element = $element;
    }

    /**
     *
     * @param {HTMLElement} $modal
     * @returns {boolean}
     */
    hide($modal) {
        const index = this.#$modals.indexOf($modal);
        if (index < 0) {
            return false;
        }
        this.#$modals.splice(index, 1);
        if (this.#$modals.length === 0) {
            this.#$element.hide();
        }
        $modal.hide();
        setTimeout(() => $modal.remove(), 1000);
        return true;
    }

    /**
     *
     * @param{{
     *     title:string,
     *     content:string,
     *     buttons?:{
     *       caption:string,
     *       color?:'blue'|'gray'|'green'|'red',
     *       onclick?:function(HTMLElement)
     *     }[],
     *     isContentHtml?:boolean,
     *     delay?:number
     * }} args
     * @return {HTMLElement}
     */
    show(args) {
        const $title = document.createElement('div');
        $title.classList.add('---title');
        $title.innerText = args.title;
        const $content = document.createElement('div');
        $content.classList.add('---content');
        if (args.isContentHtml === true) {
            $content.innerHTML = args.content;
        } else {
            $content.innerText = args.content;
        }
        const $modal = document.createElement('div');
        $modal.classList.add('---modal');
        $modal.append($title, $content);
        if (args.buttons != null && args.buttons.length > 0) {
            const $buttonContainer = document.createElement('div');
            $buttonContainer.classList.add('---button-container');
            for (const button of args.buttons) {
                const $button = document.createElement('button');
                $button.classList.add('--object-button', `-color-${button.color ?? 'gray'}`, '---button');
                $button.setAttribute('type', 'button');
                $button.innerText = button.caption;
                if (typeof button.onclick === 'function') {
                    $button.addEventListener('click', () => button.onclick($modal));
                }
                $buttonContainer.append($button);
            }
            $modal.append($buttonContainer);
        }
        this.#$element.append($modal);
        this.#$modals.push($modal);
        setTimeout(() => {
            this.#$element.show();
            $modal.show();
        }, args.delay ?? 50);
        return $modal;
    }

    /**
     *
     * @param{string} title
     * @param {string}content
     * @param {{
     * buttonCaption?:string,
     *      isContentHtml?:boolean,
     *      delay?:number,
     *      buttonColor?:'blue'|'green'|'red'|'gray',
     *      onOkCallback?:function(HTMLElement?)
     * }?}args
     * @return {HTMLElement}
     */
    showSimpleOk(title, content, args) {
        args ??= {};
        return this.show({
            title: title,
            content: content,
            buttons: [
                {
                    caption: args.buttonCaption ?? '확인',
                    color: args.buttonColor ?? 'green',
                    onclick: ($modal) => {
                        if (typeof args.onOkCallback === 'function') {
                            args.onOkCallback($modal);
                        }
                        this.hide($modal);

                    }

                }
            ],
            isContentHtml: args.isContentHtml,
            delay: args.delay
        });

    }
}

window.addEventListener('DOMContentLoaded', () => {
    //                            ⬆defer와 같다.
    window.dialog = new Dialog();
    console.log(dialog)
})