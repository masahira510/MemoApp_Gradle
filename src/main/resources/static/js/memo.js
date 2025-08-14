document.addEventListener('DOMContentLoaded', () => {

  // メモ削除・編集メニュー表示
  const MENUS_TOGGLE_BUTTONS = document.querySelectorAll('.js-toggle-menu');
  MENUS_TOGGLE_BUTTONS.forEach((btn) => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      const PARENT = btn.closest('.memo-card');
      const MENU = PARENT.querySelector('.js-menu-dialog');

      // 他のメニューを閉じる
      document.querySelectorAll('.js-menu-dialog').forEach((otherMenu) => {
        if (otherMenu !== MENU) {
          otherMenu.classList.remove('block');
        }
      });

      MENU.classList.toggle('block');
    });;
  });

  // メニュー外をクリックしたら全部閉じる
  document.addEventListener('click', (event) => {
    const IS_MENU_BUTTON = event.target.closest('.js-toggle-menu');
    const IS_MENU_DIALOG = event.target.closest('.js-menu-dialog');

    if (!IS_MENU_BUTTON && !IS_MENU_DIALOG) {
      document.querySelectorAll('.js-menu-dialog').forEach((dialog) => {
        dialog.classList.remove('block');
      });
    }
  });

  // メモ削除ダイアログ表示
  const OPEN_BUTTONS = document.querySelectorAll('.js-open-delete-modal');
  const MODAL = document.getElementById('delete-modal');
  const MASK = document.getElementById('modal-mask');
  const CANCEL_BUTTONS = document.querySelectorAll('.js-cancel-btn');

  OPEN_BUTTONS.forEach(button => {
    button.addEventListener('click', (event) => {
      event.stopPropagation();
      const ID = button.dataset.id;
      const TITLE = button.dataset.title;

      // タイトルとフォームの動的設定
      MODAL.querySelector('h3').textContent = TITLE;
      MODAL.querySelector('form').setAttribute('action', `/delete/${ID}`);

      // ダイアログ表示
      MODAL.classList.remove('hidden');
      MASK.classList.remove('hidden');

      // メモメニューを閉じる
      document.querySelectorAll('.js-menu-dialog').forEach((dialog) => {
        dialog.classList.remove('block');
      });
    });
  });

  // メモ編集遷移
  const MEMO_EDIT_BUTTTONS = document.querySelectorAll('.js-redirect-edit');

  MEMO_EDIT_BUTTTONS.forEach(btn => {
    btn.addEventListener('click', (event) => {
      event.stopPropagation();
      const URL = btn.dataset.url;

      if (URL) {
        window.location.href = URL;
      }
    });
  });

  // フィルターダイアログ表示
  const FILTER_OPEN_BUTTON = document.getElementById('filter-btn');
  const FILTER_DIALOG = document.getElementById('filter-dialog');

  FILTER_OPEN_BUTTON.addEventListener('click', () => {
    FILTER_DIALOG.classList.remove('hidden');

    if (!SORT_DIALOG.classList.contains('hidden')) {
      SORT_DIALOG.classList.add('hidden');
    }
  });

  // ソートダイアログ表示
  const SORT_OPEN_BUTTON = document.getElementById('sort-btn');
  const SORT_DIALOG = document.getElementById('sort-dialog');

  SORT_OPEN_BUTTON.addEventListener('click', () => {
    SORT_DIALOG.classList.remove('hidden');

    if (!FILTER_DIALOG.classList.contains('hidden')) {
      FILTER_DIALOG.classList.add('hidden');
    }
  });

  // キャンセルボタン押下後の各ダイアログの非表示
  CANCEL_BUTTONS.forEach(button => {
    button.addEventListener('click', () => {
      if (!MODAL.classList.contains('hidden') && !MASK.classList.contains('hidden')) {
        MODAL.classList.add('hidden');
        MASK.classList.add('hidden');
      } else if (!FILTER_DIALOG.classList.contains('hidden')) {
        FILTER_DIALOG.classList.add('hidden');
      } else {
        SORT_DIALOG.classList.add('hidden');
      }
    });
  });

  // 各メモをクリックし、メモ詳細に遷移
  const MEMO_LIST = document.querySelectorAll('.js-memo');

  MEMO_LIST.forEach(memo => {
    memo.addEventListener('click', () => {
      const URL = memo.dataset.url; // data-urlを取得
      if (URL) {
        window.location.href = URL; // URLへ遷移
      }
    });
  });

  // 各メモ内のタグ上限の処理
  const MAX_VISIBLE = 2;

  document.querySelectorAll('.js-tag-list').forEach(tagList => {
    const TAGS = tagList.querySelectorAll('.js-tag');

    if (TAGS.length > MAX_VISIBLE) {
      // 3件目以降は非表示にする
      TAGS.forEach((tag, index) => {
        if (index >= MAX_VISIBLE) {
          tag.style.display = 'none';
        }
      });

      // "+n件"　を追加
      const MORE_COUNT = TAGS.length - MAX_VISIBLE;
      const MORE_TAG = document.createElement('li');
      MORE_TAG.textContent = `+${MORE_COUNT}件`;
      MORE_TAG.className = 'more-tags flex-center rounded-40 card-tag-item text-3xs';
      tagList.appendChild(MORE_TAG);
    }
  });

});
