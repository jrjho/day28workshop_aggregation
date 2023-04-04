package day28workshop.day28workshop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import day28workshop.day28workshop.repository.ReviewRepository;
import day28workshop.day28workshop.services.ReviewService;

@SpringBootApplication
public class Day28workshopApplication implements CommandLineRunner{

	@Autowired
	ReviewService reviewSvc;

	@Autowired
	ReviewRepository reviewRepo;

	public static void main(String[] args) {
		SpringApplication.run(Day28workshopApplication.class, args);
	}

	@Override
	public void run(String... args) {

		reviewRepo.getReviewByGameId("642670c5cb82a82cdd3caea6");
		// reviewSvc.getReviewByGameId("6");
		reviewRepo.sortedBoardgames("highest");

		
	}

}
