package day28workshop.day28workshop.repository;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import day28workshop.day28workshop.exceptions.Exceptions;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;

@Repository
public class ReviewRepository {

    private static final String C_GAMES = "game";

    private static final String C_COMMENT = "comment";

    @Autowired
    MongoTemplate mongoTemplate;

    // db.game.aggregate([
    // { $match: {gid: 6}},

    // { $lookup:{
    // from:'reviews',
    // foreignField:'ID',
    // localField:'gid',
    // as:'comments',
    // }},
    // {$addFields:{comments:["$comments"]}}
    // ])

    public JsonObject getReviewByGameId(String id) {
        // public Document getReviewByGameId(String id){

        try {
            // get game info
            ObjectId objId = new ObjectId(id);
            // stages
            MatchOperation matchOp = Aggregation.match(Criteria.where("_id").is(objId));
            // projection to filter our desired fields
            LookupOperation lookupOp = Aggregation.lookup(C_COMMENT, "gid", "gid", "reviews");
            // create the pipeline from stages
            Aggregation pipeline = Aggregation.newAggregation(matchOp, lookupOp);
            // perform the query
            AggregationResults<Document> results = mongoTemplate.aggregate(pipeline, C_GAMES, Document.class);

            printResults(results);
            List<Document> doc = results.getMappedResults();
            // get the first document as we only need 1 to get the game info
            Optional<Document> doc1 = Optional.ofNullable(doc.get(0));

            /// get average rating
            if (null == doc1) {
                throw new Exceptions("ID not found");
            }

            Integer gid = doc1.get().getInteger("gid");
            MatchOperation getAvrMatchOp = Aggregation.match(Criteria.where("gid").is(gid));
            GroupOperation groupOp = Aggregation.group("gid").count().as("count").avg("rating").as("average");
            Aggregation getAvrPipeline = Aggregation.newAggregation(getAvrMatchOp, groupOp);
            AggregationResults<Document> getAvrResults = mongoTemplate.aggregate(getAvrPipeline, C_COMMENT,
                    Document.class);

            Float averageRating = getAvrResults.getUniqueMappedResult().getDouble("average").floatValue();
            // convert doc1 to string and then to json object
            String jsonStr = doc1.get().toJson();
            JsonReader reader = Json.createReader(new StringReader(jsonStr));
            JsonObject jsonObject = reader.readObject();
            // create jsonobject by identifying going into _id
            // {"_id": {"$oid": "642670c5cb82a82cdd3caea6"}, "gid": 6, "name": "Mare
            // Mediterraneum", "year": 1989.......}
            JsonObject oid = jsonObject.getJsonObject("_id");
            // from {"$oid": "642670c5cb82a82cdd3caea6"}, "gid": 6, "name": "Mare
            // Mediterraneum", "year": 1989.......}
            // use object builder and add all the necessary fields to return
            JsonObjectBuilder builder = Json.createObjectBuilder();
            builder.add("gid", oid.getString("$oid"));
            builder.add("name", jsonObject.getString("name"));
            builder.add("year", jsonObject.getInt("year"));
            builder.add("rank", jsonObject.getInt("ranking"));
            builder.add("average", averageRating);
            builder.add("users_rated", jsonObject.getInt("users_rated"));
            builder.add("url", jsonObject.getString("url"));
            builder.add("thumbnail", jsonObject.getString("image"));
            // reviews=[Document{{_id=64267055004694c27acbfe50, c_id=0fbb7913, user=gobbeg,
            // rating=7, c_text=Very nicely produced., gid=6}}]
            // access the reviews array and create array to add to the json object
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            JsonArray jsonArray = jsonObject.getJsonArray("reviews");

            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject obj = jsonArray.getJsonObject(i);
                oid = obj.getJsonObject("_id");
                String strToAdd = "/review/" + oid.getString("$oid");
                arrayBuilder.add(strToAdd);
            }

            builder.add("reviews", arrayBuilder);
            builder.add("timestamp", LocalDateTime.now().toString());

            JsonObject response = builder.build();
            // System.out.println("toreturn is: " + response);

            // } else
            // throw new Exceptions("Average not found or null");

            return response;

        } catch (Exception e) {
            throw new Exceptions("Review with the given Id not found!");

        }
    }

    public JsonObject sortedBoardgames(String sortingType) {
        // limit to arbituary integer as the DB is too huge. Takes a long time to go through the whole DB
        Criteria matchCriteria = Criteria.where("gid").lte(1);
        // search in comment collection for gid and match it with gid in game collection
        LookupOperation lookupOp = Aggregation.lookup(C_COMMENT, "gid", "gid", "reviews");
        // unwind the reviews array
        UnwindOperation unwindReviews = Aggregation.unwind("reviews");
        // check sorting type and sort accordingly
        SortOperation sortOp = null;
        if (sortingType.equals("highest"))
            sortOp = Aggregation.sort(Sort.by(Direction.DESC, "reviews.rating"));
        else if (sortingType.equals("lowest"))
            sortOp = Aggregation.sort(Sort.by(Direction.ASC, "reviews.rating"));
        else
            throw new Exceptions("Only highest and lowest are allowed");
        // group by game id and get the first review for each game (first review is
        // always the highest or lowest after sorting)
        GroupOperation groupOp = Aggregation.group("_id")
                .first("name").as("name")
                .first("reviews.rating").as("rating")
                .first("reviews.user").as("user")
                .first("reviews.c_text").as("comment")
                .first("reviews._id").as("review_id");
        // create the pipeline from stages
        Aggregation pipeline = Aggregation.newAggregation(Aggregation.match(matchCriteria), lookupOp, unwindReviews,
                sortOp, groupOp);
        // perform the query
        AggregationResults<Document> results = mongoTemplate.aggregate(pipeline, C_GAMES, Document.class);
        List<Document> doc = results.getMappedResults();
        // System.out.println("doc: " + doc);
        printResults(results);

        //convert into optional to use get()
        Optional<Document> doc1 = Optional.ofNullable(doc.get(0));
        // convert doc1 to string and then to json object
        String jsonStr = doc1.get().toJson();
        JsonReader reader = Json.createReader(new StringReader(jsonStr));
        JsonObject jsonObject = reader.readObject();
        //"reviews":[{"gid":"642670c5cb82a82cdd3caed1",}]
        JsonObject gid = jsonObject.getJsonObject("_id");
        //"reviews":[{"$oid":"64267054004694c27acbc1cf"}]
        JsonObject rid = jsonObject.getJsonObject("review_id");

        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for (Document document : results) {
            JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
            jsonObjectBuilder.add("gid", gid.getString("$oid"));
            jsonObjectBuilder.add("name", document.getString("name"));
            jsonObjectBuilder.add("rating", document.getInteger("rating"));
            jsonObjectBuilder.add("user", document.getString("user"));
            jsonObjectBuilder.add("comment", document.getString("comment"));
            jsonObjectBuilder.add("review_id", rid.getString("$oid"));
            jsonArrayBuilder.add(jsonObjectBuilder);
        }
    
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("rating", sortingType.toString());
        builder.add("timestamp", LocalDateTime.now().toString());
        builder.add("games", jsonArrayBuilder);
        

        JsonObject response = builder.build();
                System.out.println("response: " + response);


        return response;
    }

    private void printResults(AggregationResults<Document> results) {
        for (Document doc : results) {
            System.out.printf(">>> %s\n", doc.toJson());
        }
    }

}
