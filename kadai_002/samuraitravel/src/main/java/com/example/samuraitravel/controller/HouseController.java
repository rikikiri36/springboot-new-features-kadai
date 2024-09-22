package com.example.samuraitravel.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.samuraitravel.entity.Favorite;
import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.Review;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.form.ReservationInputForm;
import com.example.samuraitravel.form.ReviewEditForm;
import com.example.samuraitravel.form.ReviewRegisterForm;
import com.example.samuraitravel.repository.FavoriteRepository;
import com.example.samuraitravel.repository.HouseRepository;
import com.example.samuraitravel.repository.ReviewRepository;
import com.example.samuraitravel.security.UserDetailsImpl;
import com.example.samuraitravel.service.FavoriteService;
import com.example.samuraitravel.service.ReviewService;

@Controller
@RequestMapping("/houses")
public class HouseController {
    private final HouseRepository houseRepository;        
    private final ReviewRepository reviewRepository;      
    private final FavoriteRepository favoriteRepository;      
    private final ReviewService reviewService;      
    private final FavoriteService favoriteService;      
    
    
    public HouseController(HouseRepository houseRepository, ReviewRepository reviewRepository, FavoriteRepository favoriteRepository, ReviewService reviewService, FavoriteService favoriteService) {
        this.houseRepository = houseRepository;
        this.reviewRepository = reviewRepository;
        this.favoriteRepository = favoriteRepository;
        this.reviewService = reviewService;
        this.favoriteService = favoriteService;
    }     
  
    @GetMapping
    public String index(@RequestParam(name = "keyword", required = false) String keyword,
                        @RequestParam(name = "area", required = false) String area,
                        @RequestParam(name = "price", required = false) Integer price,  
                        @RequestParam(name = "order", required = false) String order,
                        @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable,
                        Model model) 
    {
        Page<House> housePage;
                
        if (keyword != null && !keyword.isEmpty()) {
            if (order != null && order.equals("priceAsc")) {
                housePage = houseRepository.findByNameLikeOrAddressLikeOrderByPriceAsc("%" + keyword + "%", "%" + keyword + "%", pageable);
            } else {
                housePage = houseRepository.findByNameLikeOrAddressLikeOrderByCreatedAtDesc("%" + keyword + "%", "%" + keyword + "%", pageable);
            }            
        } else if (area != null && !area.isEmpty()) {
            if (order != null && order.equals("priceAsc")) {
                housePage = houseRepository.findByAddressLikeOrderByPriceAsc("%" + area + "%", pageable);
            } else {
                housePage = houseRepository.findByAddressLikeOrderByCreatedAtDesc("%" + area + "%", pageable);
            }            
        } else if (price != null) {
            if (order != null && order.equals("priceAsc")) {
                housePage = houseRepository.findByPriceLessThanEqualOrderByPriceAsc(price, pageable);
            } else {
                housePage = houseRepository.findByPriceLessThanEqualOrderByCreatedAtDesc(price, pageable);
            }            
        } else {
            if (order != null && order.equals("priceAsc")) {
                housePage = houseRepository.findAllByOrderByPriceAsc(pageable);
            } else {
                housePage = houseRepository.findAllByOrderByCreatedAtDesc(pageable);   
            }            
        }                
        
        model.addAttribute("housePage", housePage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("area", area);
        model.addAttribute("price", price);
        model.addAttribute("order", order);
        
        return "houses/index";
    }

    @GetMapping("/{id}")
    public String show(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, @PathVariable(name = "id") Integer id, Model model) {
    	House house = houseRepository.getReferenceById(id);
    	long totalReviewCount = 0;
    	boolean hasReviewed = false;
    	
//    	ログイン中のときだけ行う
    	if (userDetailsImpl != null) {
        	User user = userDetailsImpl.getUser();
//        	ログインユーザーIDを取得
        	Integer loggedInUserId = user.getId();

//        	レビュー存在チェック
        	hasReviewed = reviewService.hasUserAlreadyReviewed(user, id);
        	model.addAttribute("hasReviewed", hasReviewed);
        	model.addAttribute("loginUserId", loggedInUserId);
//System.out.println("hasReviewed "+hasReviewed);

//          気になる情報を取得
    		Optional<Favorite> favorite = favoriteRepository.findFirstByUserAndHouseId(user, id);
            model.addAttribute("favorite", favorite);
//    	List<Favorite> favorite = favoriteRepository.findByUserAndHouseId(user, id);
    	}
        model.addAttribute("house", house);
        model.addAttribute("reservationInputForm", new ReservationInputForm());

//    	新着６件のレビューを取得
    	List<Review> newReviews = reviewRepository.findTop6ByHouseOrderByUpdatedAtDesc(house);
        model.addAttribute("newReviews", newReviews);

//      レビューの件数を取得
        totalReviewCount = reviewRepository.countByHouse(house);
        model.addAttribute("totalReviewCount", totalReviewCount);
//System.out.println("totalReviewCount "+totalReviewCount);
        
        return "houses/show";
    }
    
//	  レビュー登録フォーム
    @GetMapping("/register/{id}")
    public String register(@PathVariable(name = "id") Integer id, Model model) {
    	House house = houseRepository.getReferenceById(id);
 
    	model.addAttribute("house", house);
        model.addAttribute("reviewRegisterForm", new ReviewRegisterForm());
        return "houses/register";
    }

//	  レビュー編集フォーム
	@GetMapping("/edit/{houseId}/{reviewId}")
    public String edit(@PathVariable(name = "houseId") Integer houseId, @PathVariable(name = "reviewId") Integer reviewId, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {        
		Review review = reviewRepository.getReferenceById(reviewId);
		House house = houseRepository.getReferenceById(houseId);
		ReviewEditForm reviewEditForm = new ReviewEditForm(review.getId(), review.getStarrank(), review.getReviewcomment());

//		ReviewEditForm reviewEditForm = new ReviewEditForm(review.getId(), house.getId(), user.getId(), review.getStarrank(), review.getReviewcomment());
		model.addAttribute("reviewEditForm", reviewEditForm);
		model.addAttribute("house", house);
//System.out.println("OK"+house.getId());       
        return "houses/edit";
    }    

    
//    お気に入り登録・解除
    @GetMapping("/editfavorite/{id}")
    public String toggleFavorite(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, @PathVariable(name = "id") Integer id, RedirectAttributes redirectAttributes) {

    	User user = userDetailsImpl.getUser();
 
    	// お気に入りの追加または削除処理
        try {
    	favoriteService.favoriteEdit(user, id);
        }
        catch(Exception e) {
        	System.out.println("エラー");
        }
        // 処理後にリダイレクト
        return "redirect:/houses/{id}";
    }
}
