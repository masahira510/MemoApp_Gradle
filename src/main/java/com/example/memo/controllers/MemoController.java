package com.example.memo.controllers;


import com.example.memo.exceptions.ResourceNotFoundException;
import com.example.memo.models.entity.Memos;
import com.example.memo.models.entity.Tags;
import com.example.memo.services.MemoService;
import com.example.memo.services.TagService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/")
public class MemoController {
	
	private final MemoService MEMO_SERVICE;
	private final TagService TAG_SERVICE;
	
	public MemoController(MemoService memoService, TagService tagService) {
		this.MEMO_SERVICE = memoService;
		this.TAG_SERVICE = tagService;
	}
	
	// ホーム画面
	@GetMapping
	public String getMypage(
			@RequestParam(name = "keyword", required = false) String keyword,
			@RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(name = "tags", required = false) List<String> tags,
			@RequestParam(name = "sort", required = false) String sort,
			Model model
	){
		List<Memos> memos;
		List<Tags> tagList = TAG_SERVICE.findAll();
		LocalDateTime fromDateTime = from != null ? from.atStartOfDay() : null;
		LocalDateTime toDateTime = to != null ? to.atTime(LocalTime.MAX) : null;
		
		if (fromDateTime != null || toDateTime != null || (tags != null && !tags.isEmpty())) {
			memos = MEMO_SERVICE.filterMemos(fromDateTime, toDateTime, tags);
		} else if (keyword != null && !keyword.isEmpty()) {
			memos = MEMO_SERVICE.searchByKeyword(keyword);
		} else if ("asc".equalsIgnoreCase(sort) || "desc".equalsIgnoreCase(sort)) {
			memos = MEMO_SERVICE.findAllSortedByDate(sort);
		} else {
			memos = MEMO_SERVICE.findAll();
		}
		
		boolean isFiltered =
				(keyword != null && !keyword.isEmpty()) ||
				(from != null) ||
				(to != null) ||
				(tags != null && !tags.isEmpty());
		
		model.addAttribute("memos", memos);
		model.addAttribute("tags", tagList);
		model.addAttribute("isFiltered", isFiltered);
		return "memo";
	}
	
	// 詳細画面
	@GetMapping("/detail/{id}")
	public String getMemoById(@PathVariable(name = "id") Long id, Model model) {
		Optional<Memos> optionalMemo = MEMO_SERVICE.findById(id);
	
		if (optionalMemo.isPresent()) {
			model.addAttribute("memo", optionalMemo.get());
		} else {
			return "redirect:/";
		}
		return "detail";
	}
	
	// 削除処理
	@PostMapping("/delete/{id}")
	public String deleteMemo(@PathVariable(name = "id") Long id) {
		MEMO_SERVICE.delete(id);
		return "redirect:/";
	}
	
	// 新規登録
	@GetMapping("/new")
	public String getCreatePage(Model model) {
		model.addAttribute("memo", new Memos());
		
		List<Tags> tagList = TAG_SERVICE.findAll();
		model.addAttribute("tags", tagList);
		return "memo_form";
	}
	
	// 登録処理
	@PostMapping("/create")
	public String postCreatePage(@ModelAttribute Memos memos) {
		MEMO_SERVICE.save(memos);
		return "redirect:/";
	}
	
	// 編集画面
	@GetMapping("/edit/{id}")
	public String getUpdatePage(@PathVariable(name = "id") Long id, Model model) {
		Optional<Memos> optionalMemo = MEMO_SERVICE.findById(id);
		List<Tags> tagList = TAG_SERVICE.findAll();
		
		optionalMemo.ifPresent(m -> model.addAttribute("memo", m));
		model.addAttribute("tags", tagList);
		return "memo_form.html";
	}
	
	// 編集処理
	@PostMapping("/update/{id}")
	public String postUpdatePage(@PathVariable(name = "id") Long id,@ModelAttribute Memos memos) {
		if (!id.equals(memos.getId())) {
			throw new ResourceNotFoundException("IDが一致しません");
		}
		
		MEMO_SERVICE.save(memos);
		return "redirect:/";
	}
}