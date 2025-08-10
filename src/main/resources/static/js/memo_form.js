document.addEventListener('DOMContentLoaded', () => {
  const TITLE_INPUT = document.getElementById('js-title-input');
  const DETAIL_INPUT = document.getElementById('js-detail-input');
  const WARNING_TITLE = document.getElementById('js-title-warning');
  const WARNING_DETAIL = document.getElementById('js-detail-warning');
  const SUBMIT_BTN = document.getElementById('js-submit-btn');
  const MAX_TITLE_LENGTH = 60; // タイトル文字数上限
  const MAX_DETAIL_LENGTH = 10000; //本文文字数上限

  const VALIDATE_INPUT = () => {
    const TITLE = TITLE_INPUT.value.trim();
    const DETAIL = DETAIL_INPUT.value.trim();
    const TITLE_TOO_LONG = TITLE.length > MAX_TITLE_LENGTH;
    const DETAIL_TOO_LONG = DETAIL.length > MAX_DETAIL_LENGTH;
    const TITLE_EMPTY = TITLE.length === 0;
    const DETAIL_EMPTY = DETAIL.length === 0;

    // 警告制御
    WARNING_TITLE.classList.toggle('hidden', !TITLE_TOO_LONG);
    WARNING_DETAIL.classList.toggle('hidden', !DETAIL_TOO_LONG);
    TITLE_INPUT.classList.toggle('input-error', TITLE_TOO_LONG);
    DETAIL_INPUT.classList.toggle('input-error', DETAIL_TOO_LONG);

    // 登録ボタン制御（登録不可の条件）
    const INVALID = TITLE_EMPTY || DETAIL_EMPTY || TITLE_TOO_LONG || DETAIL_TOO_LONG;
    SUBMIT_BTN.disabled = INVALID;
    };

  // 入力監視
  TITLE_INPUT.addEventListener('input', VALIDATE_INPUT);
  DETAIL_INPUT.addEventListener('input', VALIDATE_INPUT);

  // ページロード
  VALIDATE_INPUT();
});