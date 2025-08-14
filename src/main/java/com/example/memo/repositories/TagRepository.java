package com.example.memo.repositories;

import com.example.memo.models.entity.Tags;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tags, Long> {
	boolean existsByName(String name);
}
