package dev.cuongnq.moives.repository;

import dev.cuongnq.moives.model.Role;
import dev.cuongnq.moives.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RoleCustomRepo {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public RoleCustomRepo(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<Role> getRole(User user) {
        LookupOperation lookupOperation = LookupOperation.newLookup()
                .from("user_role")
                .localField("id")
                .foreignField("roleId")
                .as("role");

        Criteria criteria = Criteria.where("email").is(user.getEmail());

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                lookupOperation,
                Aggregation.unwind("role"),
                Aggregation.replaceRoot("role")
        );

        return mongoTemplate.aggregate(aggregation, "users", Role.class).getMappedResults();
    }
}
