document.addEventListener('DOMContentLoaded', () => {

  // タグ選択ボックスをクリックするとダイアログを表示
  const TAG_BOX = document.querySelector('.js-tag-box');
  const TAG_DIALOG = document.getElementById('js-tag-dialog');
  const CHECKBOXES = document.querySelectorAll('.js-tag-checkbox');
  const SELECTED_TAGS = document.getElementById('js-selected-tags');

  //　表示する上限タグ数
  const MAX_VISIBLE = 4;

  // タグ選択ダイアログを展開
  TAG_BOX.addEventListener('click', () => {
    TAG_DIALOG.classList.toggle('hidden');
  });

  // 各チェックボックスにイベントを付与
  CHECKBOXES.forEach(checkbox => {
    checkbox.addEventListener('change', updateSelectedTags);
  });

  // 初期描画
  updateSelectedTags();

  // 選択されたタグを表示（＋N件ロジック付き）
  function updateSelectedTags() {
    // 一度表示をリセット
    SELECTED_TAGS.innerHTML = '';
    const SELECTED = Array.from(CHECKBOXES).filter(cb => cb.checked);
    const VISIBLE_TAGS = SELECTED.slice(0, MAX_VISIBLE); // 表示タグ対象
    const HIDDEN_COUNT = SELECTED.length - VISIBLE_TAGS.length;

    VISIBLE_TAGS.forEach(cb => {
      const TAG_ELEM = document.createElement('span');
      TAG_ELEM.className = 'tag-item';

      // タグの種類ごとに色を替える
      const LIGHT_BLUE = ['仕事', '勉強法', 'レシピ', '映画', 'スポーツ'];
      const VIOLET = ['タスク', '買い物', '健康', '音楽', 'その他'];

      if (LIGHT_BLUE.includes(cb.dataset.label)) {
        TAG_ELEM.classList.add('tag-color-lb');
      } else if (VIOLET.includes(cb.dataset.label)) {
        TAG_ELEM.classList.add('tag-color-vlt');
      } else {
        TAG_ELEM.classList.add('tag-color-blue');
      }

      const REMOVE_BTN = document.createElement('span');
      REMOVE_BTN.className = 'remove-btn';
      REMOVE_BTN.textContent = '';

      const TAG_LABEL = document.createTextNode(cb.dataset.label);

      // クリックイベントでチェックを外す
      REMOVE_BTN.addEventListener('click', (event) => {
        event.stopPropagation();
        cb.checked = false;
        updateSelectedTags(); // 再描画
      });

      TAG_ELEM.appendChild(REMOVE_BTN);
      TAG_ELEM.appendChild(TAG_LABEL);
      SELECTED_TAGS.appendChild(TAG_ELEM);
    });

    // +N件　の省略表示
    if (HIDDEN_COUNT > 0) {
      const MORE_TAG = document.createElement('span');
      MORE_TAG.className = 'tag-item more-tags';
      MORE_TAG.textContent = `+${HIDDEN_COUNT}件`;
      SELECTED_TAGS.appendChild(MORE_TAG);
    }
  }
  
});