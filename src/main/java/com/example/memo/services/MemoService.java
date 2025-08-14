package com.example.memo.services;

import com.example.memo.exceptions.ResourceNotFoundException;
import com.example.memo.models.entity.Memos;
import com.example.memo.repositories.MemoRepository;
import com.example.memo.specifications.MemoSpecification;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class MemoService {
	
	private final MemoRepository MEMO_REPOSITORY;
	
	public MemoService(MemoRepository memoRepository) {
		this.MEMO_REPOSITORY = memoRepository;
	}
	
	public Memos save(Memos memos) {
		if (memos.getId() != null) {
			// 編集の場合：既存データのcreateAtを取得して保持
			Memos exstingMemo = MEMO_REPOSITORY.findById(memos.getId()).orElse(null);
			if (exstingMemo != null) {
				memos.setCreatedAt(exstingMemo.getCreatedAt());
			}
		}
		return MEMO_REPOSITORY.save(memos);
	}
	
	public Memos delete(Long id) {
		Memos memos = MEMO_REPOSITORY.findByIdAndIsDeletedFalse(id).orElseThrow(() -> new ResourceNotFoundException("メモが見つかりません"));
		memos.setDeleted(true);
		return MEMO_REPOSITORY.save(memos);
	}
	
	public List<Memos> findAll() {
		return MEMO_REPOSITORY.findAll(MemoSpecification.isNotDeleted());
	}
	
	public Optional<Memos> findById(Long id) {
		return MEMO_REPOSITORY.findByIdAndIsDeletedFalse(id);
	}
	
	public List<Memos> searchByKeyword(String keyword) {
		Specification<Memos> spec = MemoSpecification.isNotDeleted()
				.and(MemoSpecification.titleContains(keyword)
						.or(MemoSpecification.detailContains(keyword)));
		return MEMO_REPOSITORY.findAll(spec);
	}
	
	public List<Memos> filterMemos(LocalDateTime from, LocalDateTime to, List<String> tagNameList) {
		Specification<Memos> spec = MemoSpecification.isNotDeleted().and(MemoSpecification.createdAtBetween(from, to)).and(MemoSpecification.hasAnyTagName(tagNameList));
		return MEMO_REPOSITORY.findAll(spec);
	}
	
	public List<Memos> findAllSortedByDate(String sortDirection) {
		Sort sort = Sort.by("createdAt");
		
		if ("desc".equalsIgnoreCase(sortDirection)) {
			sort = sort.descending(); // 降順なら新しい順に並べる
		} else {
			sort = sort.ascending(); // それ以外は昇順
		}
		
		Specification<Memos> spec = MemoSpecification.isNotDeleted();
		
		return MEMO_REPOSITORY.findAll(spec, sort);		
	}

}
