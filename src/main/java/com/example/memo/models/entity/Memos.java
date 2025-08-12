package com.example.memo.models.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "memos")
@Getter
@Setter
@ToString(exclude = "tags")
public class Memos extends BaseEntity {
	
	private String title;
	
	@Column(columnDefinition = "TEXT")
	private String detail;

	@ManyToMany
	@JoinTable(
			name = "memo_tag",
			joinColumns = @JoinColumn(name = "memo_id"),
			inverseJoinColumns = @JoinColumn(name = "tag_id")
			)
	private List<Tags> tags = new ArrayList<>();
}
