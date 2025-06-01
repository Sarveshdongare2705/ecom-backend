package com.bewakoof.bewakoof.dto;

import com.bewakoof.bewakoof.model.AppUser;
import com.bewakoof.bewakoof.model.Review;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDTO {
    private Long reviewId;
    private int rating;
    private String comment;
    private Date createdAt;

    private String userName;
    private String city;
    private String profilePic;

    public ReviewDTO(AppUser user , Review review) {
        this.userName = user.getUserName();
        this.city = user.getCity();
        this.profilePic = user.getProfilePic();

        this.reviewId = review.getReviewId();
        this.rating = review.getRating();
        this.comment = review.getComment();
        this.createdAt = review.getCreatedAt();
    }
}
