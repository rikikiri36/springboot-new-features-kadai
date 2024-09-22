package com.example.samuraitravel.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.Review;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.form.ReviewEditForm;
import com.example.samuraitravel.form.ReviewRegisterForm;
import com.example.samuraitravel.repository.HouseRepository;
import com.example.samuraitravel.repository.ReviewRepository;
import com.example.samuraitravel.security.UserDetailsImpl;
import com.example.samuraitravel.service.ReviewService;

@Controller
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewRepository reviewRepository;
    private final HouseRepository houseRepository;
    private final ReviewService reviewService;
    
    
    public ReviewController(ReviewRepository reviewRepository, HouseRepository houseRepository, ReviewService reviewService) {
        this.reviewRepository = reviewRepository;
        this.houseRepository = houseRepository;
        this.reviewService = reviewService;
    }

	@GetMapping("/{id}")
    public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, @PathVariable(name = "id") Integer id, @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.DESC) Pageable pageable,  Model model) {
		boolean hasReviewed = false;
		
		Page<Review> reviewPage = reviewRepository.findByHouseIdOrderByUpdatedAtDesc(id, pageable);
    	House house = houseRepository.getReferenceById(id);
 
       	if (userDetailsImpl != null) {
        	User user = userDetailsImpl.getUser();
//        	ログインユーザーIDを取得
        	Integer loggedInUserId = user.getId();
//    	レビュー存在チェック
	    	hasReviewed = reviewService.hasUserAlreadyReviewed(user, id);
	    	model.addAttribute("hasReviewed", hasReviewed);
	    	model.addAttribute("loginUserId", loggedInUserId);
       	}

        model.addAttribute("reviewPage", reviewPage); 
        model.addAttribute("house", house);
        return "reviews/index";       
    }

	@PostMapping("/create/{id}")
    public String create(@PathVariable(name = "id") Integer id, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl, @ModelAttribute @Validated ReviewRegisterForm reviewRegisterForm, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {        
		User user = userDetailsImpl.getUser();
    	House house = houseRepository.getReferenceById(id);
    	 
    	model.addAttribute("house", house);
        model.addAttribute("reviewRegisterForm", new ReviewRegisterForm());

		
		if (bindingResult.hasErrors()) {
			System.out.println("bindingResult::: "+bindingResult);
			
			model.addAttribute("errorMessage", "コメントを入力してください。");
            return "houses/register";
        }
        
        reviewService.create(reviewRegisterForm, id, user);
        redirectAttributes.addFlashAttribute("successMessage", "レビューを登録しました。");    
        return "redirect:/houses/{id}";
    }

    @PostMapping("/update/{houseId}/{reviewId}")
    public String update(@ModelAttribute @Validated ReviewEditForm reviewEditForm, BindingResult bindingResult, RedirectAttributes redirectAttributes) {        
        if (bindingResult.hasErrors()) {
            return "admin/houses/edit";
        }
        
        reviewService.update(reviewEditForm);
        redirectAttributes.addFlashAttribute("successMessage", "レビューを編集しました。");
        
        return "redirect:/houses/{houseId}";
    }
	
    @PostMapping("/delete/{houseId}/{reviewId}")
    public String delete(@PathVariable(name = "reviewId") Integer reviewId, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl, RedirectAttributes redirectAttributes) {        
		reviewRepository.deleteById(reviewId);
        redirectAttributes.addFlashAttribute("successMessage", "レビューを削除しました。");
 
        return "redirect:/houses/{houseId}";
    }

}
