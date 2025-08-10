package com.example.memo.init;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.example.memo.models.entity.Tags;
import com.example.memo.repositories.TagRepository;

@Configuration
public class DataInitializer {
	
	@Bean
	CommandLineRunner seedTags(TagRepository tagRepository) {
		return args -> {
			// 初期データ（マスタタグ一覧）
			String[] masterTags = {
					"仕事", "タスク", "報告書", "勉強法", "買い物", "趣味", "レシピ", "健康", "旅行", "映画", "音楽", "読書", "スポーツ", "その他"
			};
			
			int insertedCount = 0; // 何件追加したか記録
			for (String name : masterTags) {
				// すでに同盟タグがあればスキップ
				if (!tagRepository.existsByName(name)) {
					Tags tag = new Tags();
					tag.setName(name);
					tag.setDeleted(false);
					tagRepository.save(tag);
					insertedCount++;
				}
			}
			System.out.println("[DataInitializer] タグ初期化完了: " + insertedCount + " 件追加");
		};
	}
}
