package com.example.memo.services;

import com.example.memo.models.entity.Tags;
import com.example.memo.repositories.TagRepository;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TagServiceの単体テスト")
class TagServiceTest {
	
	@Mock
	private TagRepository tagRepository;
	
	@InjectMocks
	private TagService tagService;
	
	private List<Tags> tags;
	
	@BeforeEach
	void setUp() {
		Tags tag = new Tags();
		tag.setId(1L);
		tag.setName("仕事");
		tags = List.of(tag);
	}
	
	@Test
	@DisplayName("タグが存在する場合、全件取得する")
	void findAll_whenTagsExist_returnsAllTags() {
		// Arrange
		when(tagRepository.findAll()).thenReturn(tags);
		
		// Act
		List<Tags> actual = tagService.findAll();
		
		// Assert
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(1L, actual.get(0).getId());
		assertEquals("仕事", actual.get(0).getName());
		
		// Verify
		verify(tagRepository, times(1)).findAll();
		verifyNoMoreInteractions(tagRepository);
	}
	
	@Test
	@DisplayName("タグが存在しない場合、空リストを返す")
	void findAll_whenNoTagsExist_returnsEmptyList() {
		// Arrange
		when(tagRepository.findAll()).thenReturn(Collections.emptyList());
		
		// Act
		List<Tags> actual = tagService.findAll();
		
		// Assert
		assertNotNull(actual);
		assertTrue(actual.isEmpty());
		
		// Verify
		verify(tagRepository, times(1)).findAll();
		verifyNoMoreInteractions(tagRepository);
	}
	
	@Test
	@DisplayName("Repositoryエラー時は例外が伝播する")
	void findAll_whenRepositoryThrows_propagatesException() {
		// Arrange
		when(tagRepository.findAll()).thenThrow(new RuntimeException("DB error"));
		
		// Act&Assert
		assertThrows(RuntimeException.class, () -> tagService.findAll());
		
		// verify
		verify(tagRepository, times(1)).findAll();
		verifyNoMoreInteractions(tagRepository);
	}

}
