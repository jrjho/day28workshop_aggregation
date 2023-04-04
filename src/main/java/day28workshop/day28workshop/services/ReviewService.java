package day28workshop.day28workshop.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import day28workshop.day28workshop.repository.ReviewRepository;
import jakarta.json.JsonObject;

@Service
public class ReviewService {
    
    @Autowired
    ReviewRepository reviewRepo;

    public JsonObject getReviewByGameId(String id){
        JsonObject response = reviewRepo.getReviewByGameId(id);
        return response;
    }

    public JsonObject getSortedBoardgames(String sortingType){
        JsonObject response = reviewRepo.sortedBoardgames(sortingType);
        return response;
    }
   
    
}

