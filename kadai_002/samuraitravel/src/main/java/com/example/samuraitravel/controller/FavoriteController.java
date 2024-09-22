package com.example.samuraitravel.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.samuraitravel.entity.Favorite;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.repository.FavoriteRepository;
import com.example.samuraitravel.security.UserDetailsImpl;

@Controller
@RequestMapping("/favorites")
public class FavoriteController {
    private final FavoriteRepository favoriteRepository;        
    
    public FavoriteController(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;            
    }     
  
    @GetMapping
    public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.DESC) Pageable pageable,  Model model) {
    	User user = userDetailsImpl.getUser();
    	Page<Favorite> favoritePage = favoriteRepository.findByUserOrderByCreatedAtDesc(user, pageable);       
        
        model.addAttribute("favoritePage", favoritePage);             
        
        return "favorites/index";
        
//        public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable, Model model) {
//            User user = userDetailsImpl.getUser();
//            Page<Reservation> reservationPage = reservationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
//            
//            model.addAttribute("reservationPage", reservationPage);   
        
    }  

//    @GetMapping("/{id}")
//    public String show(@PathVariable(name = "id") Integer id, Model model) {
//        House house = houseRepository.getReferenceById(id);
//        
//        model.addAttribute("house", house);
//        model.addAttribute("reservationInputForm", new ReservationInputForm());
//        
//        return "houses/show";
//    }
}
