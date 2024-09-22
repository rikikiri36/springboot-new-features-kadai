package com.example.samuraitravel.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReviewEditForm {
	@NotNull
    private Integer id;

//	@NotNull
//    private Integer house_id;
//
//	@NotNull
//    private Integer user_id;

	@NotNull(message = "評価を入力してください。")
	private Integer starrank;

	@NotBlank(message = "aaコメントを入力してください。")
	private String reviewcomment;
}
