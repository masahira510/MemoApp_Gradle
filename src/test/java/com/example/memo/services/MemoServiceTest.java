package com.example.memo.services;

import com.example.memo.exceptions.ResourceNotFoundException;
import com.example.memo.models.entity.Memos;
import com.example.memo.models.entity.Tags;
import com.example.memo.repositories.MemoRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.anyLong;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemoServiceの単体テスト")
class MemoServiceTest {
	
	@Mock
	private MemoRepository memoRepository;
	
	@InjectMocks
	private MemoService memoService;
	
	private Memos memo1;
	private Memos memo2;
	private Memos memo3;
	private List<Memos> sampleMemos;
	
	@BeforeEach
	void setUp() {
		memo1 = newMemo(1L, "買い物リスト", false);
		memo2 = newMemo(2L, "仕事メモ", false);
		memo3 = newMemo(3L, "今日のできごと", true);
		sampleMemos = List.of(memo1, memo2, memo3);
	}
	
	private Memos newMemo(Long id, String title, boolean isDeleted) {
		Memos m = new Memos();
		m.setId(id);
		m.setTitle(title);
		m.setDeleted(isDeleted);
		return m;
	}
	
	@Test
	@DisplayName("新規（id==null）はfindByIdを呼ばず、そのまま保存する")
	void save_whenNew_returnsSaved() {
		//Arrange
		Memos input = newMemo(null, "新規タイトル", false);
		when(memoRepository.save(any(Memos.class))).thenAnswer(inv -> inv.getArgument(0));
		
		//Act
		Memos result = memoService.save(input);
		
		// Assert
		assertNotNull(result);
		assertEquals("新規タイトル", result.getTitle());
		
		//verify
		verify(memoRepository, never()).findById(anyLong());
		verify(memoRepository, times(1)).save(any(Memos.class));
		verifyNoMoreInteractions(memoRepository);
	}
	
	@Test
	@DisplayName("編集（既存あり）はexistingのcreatedAtを引き継いで保存する")
	void save_whenUpdateAndExistingFound_keepsExistingsCreatedAt() {
		// Arrange
		Long id = 1L;
		LocalDateTime exstingCreated = LocalDateTime.of(2024, 5, 1, 12, 0);
		
		Memos exsting = new Memos();
		exsting.setId(id);
		exsting.setTitle("既存");
		exsting.setCreatedAt(exstingCreated);
		
		when(memoRepository.findById(id)).thenReturn(Optional.of(exsting));
		when(memoRepository.save(any(Memos.class))).thenAnswer(inv -> inv.getArgument(0));
		
		Memos input = new Memos();
		input.setId(id);
		input.setTitle("タイトル更新");
		input.setCreatedAt(LocalDateTime.of(2040, 1, 1, 0, 0)); // ←上書きされる想定
		
		// Act
		Memos result = memoService.save(input);
		
		// Assert
		assertEquals("タイトル更新", result.getTitle());
		assertEquals(exstingCreated, result.getCreatedAt(), "exstingのcreatedAtを引き継ぐ");
		verify(memoRepository, times(1)).findById(id);
		verify(memoRepository, times(1)).save(argThat(m -> exstingCreated.equals(m.getCreatedAt())));
		verifyNoMoreInteractions(memoRepository);
	}
	
	@Test
	@DisplayName("編集（既存なし）はcreatedAtを上書きせず保存する")
	void save_whenUpdateButExistingNotFound_doesNotOverrideCreatedAt() {
		// Arrange
		Long id = 999L;
		when(memoRepository.findById(id)).thenReturn(Optional.empty());
		when(memoRepository.save(any(Memos.class))).thenAnswer(inv -> inv.getArgument(0));
		
		Memos input = new Memos();
		input.setId(id);
		input.setTitle("見つからない");
		LocalDateTime inputCreatedAt = LocalDateTime.of(2025, 8, 1, 9, 0);
		input.setCreatedAt(inputCreatedAt);
		
		// Act
		Memos result = memoService.save(input);
		
		// Assert
		assertEquals(inputCreatedAt, result.getCreatedAt(), "createdAtは維持される");
		verify(memoRepository, times(1)).findById(id);
		verify(memoRepository, times(1)).save(argThat(m -> inputCreatedAt.equals(m.getCreatedAt())));
		verifyNoMoreInteractions(memoRepository);
	}
	
	@Test
	@DisplayName("指定したIDのメモに削除フラグを立てて保存し、その保存結果を返す")
	void delete_whenMemoExists_returnsSaveDeletedMemo() {
		// Arrange
		Long id = 1L;
		Memos memo1 = sampleMemos.get(0);
		when(memoRepository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.of(memo1));
		when(memoRepository.save(any(Memos.class))).thenAnswer(inv -> inv.getArgument(0));
		
		// Act
		Memos result = memoService.delete(id);
		
		// Assert
		assertNotNull(result);
		assertEquals(id, result.getId());
		assertTrue(result.isDeleted());
		
		verify(memoRepository, times(1)).findByIdAndIsDeletedFalse(id);
		verify(memoRepository, times(1)).save(argThat(m -> m.getId().equals(id) && m.isDeleted()));
		verifyNoMoreInteractions(memoRepository);
	}
	
	@Test
	@DisplayName("対象が存在しない場合、ResourceNotFoundExceptionを投げる")
	void delete_whenMemoNotFound_throwsResourceNotFoundException() {
		// Arrange
		Long missingId = 999L;
		when(memoRepository.findByIdAndIsDeletedFalse(missingId)).thenReturn(Optional.empty());
		
		// Act&Assert
		assertThrows(ResourceNotFoundException.class, () -> memoService.delete(missingId));
		
		//Verify
		verify(memoRepository, times(1)).findByIdAndIsDeletedFalse(missingId);
		verify(memoRepository, never()).save(any(Memos.class));
		verifyNoMoreInteractions(memoRepository);
	}
	
	@Test
	@DisplayName("未削除のメモが存在する場合、全件取得できる")
	void findAll_whenMemosExist_returnsAllMemos() {
		// Arrange
		when(memoRepository.findAll(ArgumentMatchers.<Specification<Memos>>any())).thenReturn(List.of(memo1, memo2));
		
		// Act
		List<Memos> actual = memoService.findAll();
		
		// Assert
		assertNotNull(actual);
		assertEquals(2, actual.size());
		assertEquals("買い物リスト", actual.get(0).getTitle());
		assertEquals("仕事メモ", actual.get(1).getTitle());
		
		// Verify
		verify(memoRepository, times(1)).findAll(ArgumentMatchers.<Specification<Memos>>any());
		verifyNoMoreInteractions(memoRepository);
	}
	
	@Test
	@DisplayName("メモが存在しない場合、空リストを返す")
	void findAll_whenNoMemosExist_returnsEmptyList() {
		// Arrange
		when(memoRepository.findAll(ArgumentMatchers.<Specification<Memos>>any())).thenReturn(Collections.emptyList()); // 空リストを返すように設定
		
		// Act
		List<Memos> actual = memoService.findAll();
		
		// Assert
		assertNotNull(actual);
		assertTrue(actual.isEmpty());
		verify(memoRepository, times(1)).findAll(ArgumentMatchers.<Specification<Memos>>any());
		verifyNoMoreInteractions(memoRepository);
	}
	
	@Test
	@DisplayName("指定したメモIDが存在する場合、メモを取得できる")
	void findById_whenMemoExist_returnsMemo() {
		// Arrange
		Long id = 1L;
		Memos memo1 = sampleMemos.get(0);
		when(memoRepository.findByIdAndIsDeletedFalse(id)).thenReturn(Optional.of(memo1));
		
		// Act
		Optional<Memos> actual = memoService.findById(id);
		
		// Assert
		assertTrue(actual.isPresent(), "Optionalがemptyではないこと");
		assertEquals(1L, actual.get().getId());
		assertEquals("買い物リスト", actual.get().getTitle());
		
		// Verify
		verify(memoRepository, times(1)).findByIdAndIsDeletedFalse(id);
		verifyNoMoreInteractions(memoRepository);
	}
	
	@Test
	@DisplayName("指定したメモIDが存在しない場合、Optional.empty()を返す")
	void findById_whenMemoNotExist_returnsEmptyOptional() {
		// Arrange
		Long missingId = 999L;
		when(memoRepository.findByIdAndIsDeletedFalse(missingId)).thenReturn(Optional.empty());
		
		// Act
		Optional<Memos> actual = memoService.findById(missingId);
		
		// Assert
		assertTrue(actual.isEmpty(), "Optionalがemptyであること");
		verify(memoRepository, times(1)).findByIdAndIsDeletedFalse(missingId);
		verifyNoMoreInteractions(memoRepository);
	}
	
	@Test
	@DisplayName("タイトルにキーワードが含まれるメモだけを返す")
	void searchByKeyword_whenKeywordMatchesTitle_returnsMatchedMemos () {
		// Arrange
		String inputKeyword = "買い物";
		Memos memo1 = sampleMemos.get(0);
		List<Memos> matched = List.of(memo1);
		when(memoRepository.findAll(ArgumentMatchers.<Specification<Memos>>any())).thenReturn(matched);
		
		// Act
		List<Memos> result = memoService.searchByKeyword(inputKeyword);
		
		// Assert
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(1L, result.get(0).getId());
		assertEquals("買い物リスト", result.get(0).getTitle());
		
		// Verify
		verify(memoRepository, times(1)).findAll(ArgumentMatchers.<Specification<Memos>>any());
		verifyNoMoreInteractions(memoRepository);
	}
	
	@Test
	@DisplayName("本文にキーワードが含まれるメモを返す")
	void searchByKeyword_whenKeywordMatchesDetail_returnsMatchedMemos() {
		// Arrange
		Memos memo4 = new Memos();
		memo4.setId(4L);
		memo4.setTitle("果物");
		memo4.setDetail("バナナ");
		
		String inputKeyword = "バナナ";
		when(memoRepository.findAll(ArgumentMatchers.<Specification<Memos>>any())).thenReturn(List.of(memo4));
		
		// Act
		List<Memos> result = memoService.searchByKeyword(inputKeyword);
		
		// Assert
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(4L, result.get(0).getId());
		assertEquals("バナナ", result.get(0).getDetail());
		
		// Verify
		verify(memoRepository, times(1)).findAll(ArgumentMatchers.<Specification<Memos>>any());
		verifyNoMoreInteractions(memoRepository);
	}
	
	@Test
	@DisplayName("キーワードに含まれるメモが該当なしなら空リストを返す")
	void searchByKeyword_whenDoesNotMatch_returnsEmptyList() {
		// Arrange
		String inputKeyword = "存在しない";
		when(memoRepository.findAll(ArgumentMatchers.<Specification<Memos>>any())).thenReturn(Collections.emptyList());
		
		// Act
		List<Memos> result = memoService.searchByKeyword(inputKeyword);
		
		// Assert
		assertTrue(result.isEmpty());
		
		// Verify
		verify(memoRepository, times(1)).findAll(ArgumentMatchers.<Specification<Memos>>any());
		verifyNoMoreInteractions(memoRepository);
	}

	@Test
	@DisplayName("from指定あり、to/タグ名指定なしの場合、範囲内に一致するメモを返す")
	void filterMemos_whenMatchesDateRangeFrom_returnsMatchedMemos() {
		// Arrange
		Memos memo4 = new Memos();
		memo4.setId(4L);
		memo4.setTitle("3月の仕事");
		memo4.setCreatedAt(LocalDateTime.of(2025, 3, 31, 0, 0));
		
		LocalDateTime inputFromDateTime = LocalDateTime.of(2025, 2, 1, 0, 0);
		when(memoRepository.findAll(ArgumentMatchers.<Specification<Memos>>any())).thenReturn(List.of(memo4));
		
		// Act
		List<Memos> result = memoService.filterMemos(inputFromDateTime, null, null);
		
		// Assert
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(4L, result.get(0).getId());
		assertEquals("3月の仕事", result.get(0).getTitle());
		assertEquals(LocalDateTime.of(2025, 3, 31, 0, 0), result.get(0).getCreatedAt());
		
		// Verify
		verify(memoRepository, times(1)).findAll(ArgumentMatchers.<Specification<Memos>>any());
		verifyNoMoreInteractions(memoRepository);
	}
	
	@Test
	@DisplayName("to指定あり、from/タグ名指定なしの場合、範囲内に一致するメモを返す")
	void filterMemos_whenMatchesDateRangeTo_returnsMatchedMemos() {
		// Arrange
		Memos memo4 = new Memos();
		memo4.setId(4L);
		memo4.setTitle("3月の仕事");
		memo4.setCreatedAt(LocalDateTime.of(2025, 3, 31, 0, 0));
		
		LocalDateTime inputToDateTime = LocalDateTime.of(2025, 4, 1, 0, 0);
		when(memoRepository.findAll(ArgumentMatchers.<Specification<Memos>>any())).thenReturn(List.of(memo4));
		
		// Act
		List<Memos> result = memoService.filterMemos(null, inputToDateTime, null);
		
		// Assert
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(4L, result.get(0).getId());
		assertEquals("3月の仕事", result.get(0).getTitle());
		assertEquals(LocalDateTime.of(2025, 3, 31, 0, 0), result.get(0).getCreatedAt());
		
		// Verify
		verify(memoRepository, times(1)).findAll(ArgumentMatchers.<Specification<Memos>>any());
		verifyNoMoreInteractions(memoRepository);
	}
	
	@Test
	@DisplayName("タグ名が一致し、期間指定なし(null,null)なら一致メモを返す")
	void filterMemos_whenMatchesTagAndNoDateRange_returnsMatchedMemos() {
		// Arrange
		Tags tag1 = new Tags();
		tag1.setName("仕事");
		List<Tags> tags = List.of(tag1);
		
		Memos memo4 = new Memos();
		memo4.setId(4L);
		memo4.setTitle("3月の仕事");
		memo4.setTags(tags);
		
		List<String> inputTagNames = List.of("仕事");
		when(memoRepository.findAll(ArgumentMatchers.<Specification<Memos>>any())).thenReturn(List.of(memo4));
		
		// Act
		List<Memos> result = memoService.filterMemos(null, null, inputTagNames);
		
		// Assert
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(4L, result.get(0).getId());
		assertEquals("3月の仕事", result.get(0).getTitle());
		
		// タグ名が一致しているかを確認
		assertEquals(1, result.get(0).getTags().size());
		assertEquals("仕事", result.get(0).getTags().get(0).getName());
		
		// Verify
		verify(memoRepository, times(1)).findAll(ArgumentMatchers.<Specification<Memos>>any());
		verifyNoMoreInteractions(memoRepository);
	}
	
	@Test
	@DisplayName("期間指定(from/to)あり、タグ名の指定がなし(null)の場合、範囲内に一致するメモを返す")
	void filterMemos_whenMatchesDateRangeAndNoTag_returnsMatchedMemos() {
		// Arrange
		Memos memo4 = new Memos();
		memo4.setId(4L);
		memo4.setTitle("3月の仕事");
		memo4.setCreatedAt(LocalDateTime.of(2025, 3, 31, 0, 0));
		
		LocalDateTime inputFromDateTime = LocalDateTime.of(2025, 2, 1, 0, 0);
		LocalDateTime inputToDateTime = LocalDateTime.of(2025, 4, 1, 0, 0);
		when(memoRepository.findAll(ArgumentMatchers.<Specification<Memos>>any())).thenReturn(List.of(memo4));
		
		// Act
		List<Memos> result = memoService.filterMemos(inputFromDateTime, inputToDateTime, null);
		
		// Assert
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(4L, result.get(0).getId());
		assertEquals("3月の仕事", result.get(0).getTitle());
		assertEquals(LocalDateTime.of(2025, 3, 31, 0, 0), result.get(0).getCreatedAt());
		
		// verify
		verify(memoRepository, times(1)).findAll(ArgumentMatchers.<Specification<Memos>>any());
		verifyNoMoreInteractions(memoRepository);
	}
	
	@Test
	@DisplayName("期間指定(from/to)とタグ名を指定し、一致するメモを返す")
	void filterMemos_whenMatchesDateRangeAndTag_returnsMatchedMemos() {
		// Arrange
		Tags tag1 = new Tags();
		tag1.setName("仕事");
		List<Tags> tags = List.of(tag1);
		
		Memos memo4 = new Memos();
		memo4.setId(4L);
		memo4.setTitle("3月の仕事");
		memo4.setCreatedAt(LocalDateTime.of(2025, 3, 31, 0, 0));
		memo4.setTags(tags);
		
		LocalDateTime inputFromDateTime = LocalDateTime.of(2025, 2, 1, 0, 0);
		LocalDateTime inputToDateTime = LocalDateTime.of(2025, 4, 1, 0, 0);
		List<String> inputTagNames = List.of("仕事");
		
		when(memoRepository.findAll(ArgumentMatchers.<Specification<Memos>>any())).thenReturn(List.of(memo4));
		
		// Act
		List<Memos> result = memoService.filterMemos(inputFromDateTime, inputToDateTime, inputTagNames);
		
		// Assert
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(4L, result.get(0).getId());
		assertEquals("3月の仕事", result.get(0).getTitle());
		assertEquals(LocalDateTime.of(2025, 3, 31, 0, 0), result.get(0).getCreatedAt());
		
		// タグ名が一致しているかを確認
		assertEquals(1, result.get(0).getTags().size());
		assertEquals("仕事", result.get(0).getTags().get(0).getName());
		
		// Verify
		verify(memoRepository, times(1)).findAll(ArgumentMatchers.<Specification<Memos>>any());
		verifyNoMoreInteractions(memoRepository);
	}
	
	@Test
	@DisplayName("期間指定(from/to)とタグ名に一致しない場合、空リスト返す")
	void filterMemos_whenDoesNotMatch_returnsEmptyList() {
		// Arrange
		Tags tag1 = new Tags();
		tag1.setName("該当なし");
		
		Memos memo4 = new Memos();
		memo4.setCreatedAt(LocalDateTime.of(2099, 1, 1, 0, 0));
		
		LocalDateTime inputFromDateTime = LocalDateTime.of(2025, 2, 1, 0, 0);
		LocalDateTime inputToDateTime = LocalDateTime.of(2025, 4, 1, 0, 0);
		List<String> inputTagNames = List.of("仕事");
		
		when(memoRepository.findAll(ArgumentMatchers.<Specification<Memos>>any())).thenReturn(Collections.emptyList());
		
		// Act
		List<Memos> result = memoService.filterMemos(inputFromDateTime, inputToDateTime, inputTagNames);
		
		// Assert
		assertNotNull(result);
		assertTrue(result.isEmpty());
		
		// Verify
		verify(memoRepository, times(1)).findAll(ArgumentMatchers.<Specification<Memos>>any());
		verifyNoMoreInteractions(memoRepository);
	}
	
	@Test
	@DisplayName("desc指定ならcreatedAtの降順で検索する")
	void findAllSortedByDate_whenDesc_returnsSortedDesc() {
		// Arrange
		List<Memos> stub = List.of(new Memos());
		when(memoRepository.findAll(ArgumentMatchers.<Specification<Memos>>any(), any(Sort.class))).thenReturn(stub);
		
		ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
		
		// Act
		List<Memos> result = memoService.findAllSortedByDate("desc");
		
		// Assert
		assertNotNull(result);
		assertEquals(1, result.size());
		
		// Verify
		verify(memoRepository, times(1)).findAll(ArgumentMatchers.<Specification<Memos>>any(), sortCaptor.capture());
		verifyNoMoreInteractions(memoRepository);
		
		// 方向のAssert
		Sort.Order order = sortCaptor.getValue().getOrderFor("createdAt");
		assertNotNull(order, "createdAtのSort.Orderが存在すること");
		assertEquals(Sort.Direction.DESC, order.getDirection(), "降順であること");
	}
	
	@Test
	@DisplayName("desc以外ならcreatedAtの昇順で検索する（例：asc）")
	void findAllSortedByDate_whenAsc_returnsSortedAsc() {
		// Arrange
		when(memoRepository.findAll(ArgumentMatchers.<Specification<Memos>>any(), any(Sort.class))).thenReturn(Collections.emptyList());
		ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
		
		// Act
		List<Memos> result = memoService.findAllSortedByDate("asc");
		
		// Assert
		assertNotNull(result);
		assertTrue(result.isEmpty());
		
		// Verify
		verify(memoRepository, times(1)).findAll(ArgumentMatchers.<Specification<Memos>>any(), sortCaptor.capture());
		verifyNoMoreInteractions(memoRepository);
		
		// 方向のAssert
		Sort.Order order = sortCaptor.getValue().getOrderFor("createdAt");
		assertNotNull(order);
		assertEquals(Sort.Direction.ASC, order.getDirection(), "昇順であること");
	}
	
	@Test
	@DisplayName("desc以外（null含む）の入力はcreatedAt昇順で検索する")
	void findAllSortedByDate_whenNull_returnsSortedAsc() {
		// Arrange
		when(memoRepository.findAll(ArgumentMatchers.<Specification<Memos>>any(), any(Sort.class))).thenReturn(Collections.emptyList());
		ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
		
		// Act
		memoService.findAllSortedByDate(null);
		
		// Verify&Assert
		verify(memoRepository, times(1)).findAll(ArgumentMatchers.<Specification<Memos>>any(), sortCaptor.capture());
		Sort.Order order = sortCaptor.getValue().getOrderFor("createdAt");
		assertNotNull(order);
		assertEquals(Sort.Direction.ASC, order.getDirection());
	}
}
