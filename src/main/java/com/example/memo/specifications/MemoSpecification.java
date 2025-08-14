package com.example.memo.specifications;

import com.example.memo.models.entity.Memos;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class MemoSpecification {
	
	public static Specification<Memos> isNotDeleted() {
		return (root, query, cb) -> cb.isFalse(root.get("isDeleted"));
	}
	
	// タイトルにキーワードが含まれるメモを検索
	// キーワードが空文字やnullなら条件を無視
	public static Specification<Memos> titleContains(String keyword) {
		return (root, query, cb) -> {
			if (keyword == null || keyword.isEmpty()) {
				return cb.disjunction(); // キーワードが空なら無視
			}
			return cb.like(root.get("title"), "%" + keyword + "%");
		};
	}
	
	// メモ本文にキーワードが含まれるメモを検索
	// キーワードが空文字やnullなら条件を無視
	public static Specification<Memos> detailContains(String keyword) {
		return (root, query, cb) -> {
			if (keyword == null || keyword.isEmpty()) {
				return cb.disjunction();// キーワードが空なら無視
			}
			return cb.like(root.get("detail"), "%" + keyword + "%");
		};
	}
	
	// 登録日が指定範囲内にあるメモを検索
	// from/toの指定がない場合は条件を省略
	public static Specification<Memos> createdAtBetween(LocalDateTime from, LocalDateTime to) {
		return (root, query, cb) -> {
			if (from != null && to != null) {
				return cb.between(root.get("createdAt"), from, to);
			} else if (from != null) {
				return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
			} else if (to != null) {
				return cb.lessThanOrEqualTo(root.get("createdAt"), to);
			} else {
				return cb.conjunction(); //　常にtrue ダミー条件
			}
		};
	}
	
	// 指定されたタグ名のいずれかを持つメモを検索
	// タグ未指定時は全件対象
	public static Specification<Memos> hasAnyTagName(List<String> tagNames) {
		return (root, query, cb) -> {
			if (tagNames != null && !tagNames.isEmpty()) {
				return root.join("tags").get("name").in(tagNames);
			} else {
				return cb.conjunction(); //常にtrue ダミー条件
			}
		};
	}
}
