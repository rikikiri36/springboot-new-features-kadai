package com.example.samuraitravel.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.samuraitravel.entity.Favorite;
import com.example.samuraitravel.entity.House;
import com.example.samuraitravel.entity.User;
import com.example.samuraitravel.form.HouseEditForm;
import com.example.samuraitravel.repository.FavoriteRepository;
import com.example.samuraitravel.repository.HouseRepository;
import com.example.samuraitravel.repository.UserRepository;

@Service
public class FavoriteService {
	private final FavoriteRepository favoriteRepository;
	private final HouseRepository houseRepository;  
    private final UserRepository userRepository;

	public FavoriteService(FavoriteRepository favoriteRepository, HouseRepository houseRepository, UserRepository userRepository) {
		this.favoriteRepository = favoriteRepository;
		this.houseRepository = houseRepository;  
        this.userRepository = userRepository;
	}

    @Transactional
    public void favoriteEdit(User user, Integer houseId) { 
//        Favorite favorite = new Favorite();
        
//    	UserId、HouseIdで存在チェック
    	Optional<Favorite> favoriteOpt = favoriteRepository.findFirstByUserAndHouseId(user, houseId);

//    	存在する場合は気になる解除（削除）
    	if (favoriteOpt.isPresent()) {
    		Favorite favorite = favoriteOpt.get();
            favoriteRepository.deleteById(favorite.getId());	//idは上記から取得
//System.out.println("存在する");
//System.out.println("user" + user);
//System.out.println("house" + houseId);
//		存在しない場合は気になる登録（新規作成）
    	} else {
            Favorite newFavorite = new Favorite();
            newFavorite.setUser(user);
            
            // Houseエンティティの取得
            House house = houseRepository.findById(houseId).orElse(null);
            newFavorite.setHouse(house);

            favoriteRepository.save(newFavorite);
//System.out.println("存在しない");
//System.out.println("user" + user);
//System.out.println("house" + houseId);
        }
    }
	
	

    @Transactional
    public void update(HouseEditForm houseEditForm) {
        House house = houseRepository.getReferenceById(houseEditForm.getId());
        MultipartFile imageFile = houseEditForm.getImageFile();
        
        if (!imageFile.isEmpty()) {
            String imageName = imageFile.getOriginalFilename(); 
            String hashedImageName = generateNewFileName(imageName);
            Path filePath = Paths.get("src/main/resources/static/storage/" + hashedImageName);
            copyImageFile(imageFile, filePath);
            house.setImageName(hashedImageName);
        }
        
        house.setName(houseEditForm.getName());                
        house.setDescription(houseEditForm.getDescription());
        house.setPrice(houseEditForm.getPrice());
        house.setCapacity(houseEditForm.getCapacity());
        house.setPostalCode(houseEditForm.getPostalCode());
        house.setAddress(houseEditForm.getAddress());
        house.setPhoneNumber(houseEditForm.getPhoneNumber());
                    
        houseRepository.save(house);
    }    

	// UUIDを使って生成したファイル名を返す
	public String generateNewFileName(String fileName) {
		String[] fileNames = fileName.split("\\.");
		for (int i = 0; i < fileNames.length - 1; i++) {
			fileNames[i] = UUID.randomUUID().toString();
		}
		String hashedFileName = String.join(".", fileNames);
		return hashedFileName;
	}

	// 画像ファイルを指定したファイルにコピーする
	public void copyImageFile(MultipartFile imageFile, Path filePath) {
		try {
			Files.copy(imageFile.getInputStream(), filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
