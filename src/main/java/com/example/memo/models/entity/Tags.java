package com.example.memo.models.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "tags")
@Getter
@Setter
@ToString(exclude = "memos")
public class Tags extends BaseEntity {
	
	private String name;
	
	@ManyToMany(mappedBy = "tags")
	private List<Memos> memos = new ArrayList<>();
}
