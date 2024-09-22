package com.example.samuraitravel.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.Review;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.form.ReviewEditForm;
import com.example.samuraitravel.form.ReviewRegisterForm;
import com.example.samuraitravel.repository.HouseRepository;
import com.example.samuraitravel.repository.ReviewRepository;

@Service
public class ReviewService {
	private final ReviewRepository reviewRepository;
	private final HouseRepository houseRepository;

	public ReviewService(HouseRepository houseRepository, ReviewRepository reviewRepository) {
		this.houseRepository = houseRepository;
		this.reviewRepository = reviewRepository;
	}

//	ログインユーザー、対象民宿でレビューが存在するか
	public boolean hasUserAlreadyReviewed(User user, Integer houseid) {
		return reviewRepository.existsByUserAndHouseId(user, houseid);
	}

//	登録
	@Transactional
	public void create(ReviewRegisterForm reviewRegisterForm, Integer houseid, User user) {
		House house = houseRepository.getReferenceById(houseid);
		Review review = new Review();

		review.setHouse(house);
		review.setUser(user);
		review.setStarrank(reviewRegisterForm.getStarrank());
		review.setReviewcomment(reviewRegisterForm.getReviewcomment());

		reviewRepository.save(review);
	}

	//更新
    @Transactional
    public void update(ReviewEditForm reviewEditForm) {
        Review review = reviewRepository.getReferenceById(reviewEditForm.getId());

        review.setStarrank(reviewEditForm.getStarrank());                
        review.setReviewcomment(reviewEditForm.getReviewcomment());

        reviewRepository.save(review);
    }    

}
