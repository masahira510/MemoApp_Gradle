package com.example.memo.services;

import com.example.memo.models.entity.Tags;
import com.example.memo.repositories.TagRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TagService {
	
	private final TagRepository TAG_REPOSITORY;
	
	public TagService(TagRepository tagRepository) {
		this.TAG_REPOSITORY = tagRepository;
	}
	
	public List<Tags> findAll() {
		return TAG_REPOSITORY.findAll();
	}
	
}
