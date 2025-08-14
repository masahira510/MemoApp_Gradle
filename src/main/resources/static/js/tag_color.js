document.addEventListener('DOMContentLoaded', () => {
    
  // タグの種類ごとに色を替える
  const TAG_COLOR = document.querySelectorAll('.js-tag-color');

  TAG_COLOR.forEach(tagElem => {
    const TAG_LABEL = tagElem.textContent.trim();
    const LIGHT_BLUE = ['仕事', '勉強法', 'レシピ', '映画', 'スポーツ'];
    const VIOLET = ['タスク', '買い物', '健康', '音楽', 'その他'];

    if (LIGHT_BLUE.includes(TAG_LABEL)) {
      tagElem.classList.add('tag-color-lb');
    } else if (VIOLET.includes(TAG_LABEL)) {
      tagElem.classList.add('tag-color-vlt');
    } else {
      tagElem.classList.add('tag-color-blue');
    }
    });
    
});