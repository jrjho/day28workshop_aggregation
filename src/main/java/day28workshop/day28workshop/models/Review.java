package day28workshop.day28workshop.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Review {
    
    private String game_id;
    private Integer gid;
    private String name;
    private Integer year;
    private Integer rank;
    private Float average;
    private Integer users_rated;
    private String url;
    private String thumbnail;
    private List<String> reviews = new ArrayList<>();
    private LocalDateTime timestamp;





}
