package com.example.memo.repositories;

import com.example.memo.models.entity.Memos;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MemoRepository extends JpaRepository<Memos, Long>, JpaSpecificationExecutor<Memos> {
	Optional<Memos> findByIdAndIsDeletedFalse(Long id);
	List<Memos> findAll(Specification<Memos> spec, Sort sort);
}
