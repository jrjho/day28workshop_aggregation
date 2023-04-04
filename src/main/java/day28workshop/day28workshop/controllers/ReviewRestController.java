package day28workshop.day28workshop.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import day28workshop.day28workshop.services.ReviewService;
import jakarta.json.Json;
import jakarta.json.JsonObject;

@RestController
@RequestMapping
public class ReviewRestController {

    @Autowired
    ReviewService reviewSvc;

    @GetMapping(path="game/{game_id}/reviews", produces=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getGameWithReview(@PathVariable String game_id){

        try{
            JsonObject response = reviewSvc.getReviewByGameId(game_id);
            return ResponseEntity.ok(response.toString());

        } catch (Exception e) {
            JsonObject err = Json.createObjectBuilder()
                    .add("message", e.getMessage())
                    .build();
            return ResponseEntity.status(400).body(err.toString());
        }
    }

        @GetMapping(path="games/{sorting_method}", produces=MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<String> listOfBoardGames(@PathVariable String sorting_method){

            try{
                JsonObject response = reviewSvc.getSortedBoardgames(sorting_method);
                return ResponseEntity.ok(response.toString());
    
            } catch (Exception e) {
                JsonObject err = Json.createObjectBuilder()
                        .add("message", e.getMessage())
                        .build();
                return ResponseEntity.status(400).body(err.toString());
            }    }
    
}
