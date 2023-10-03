package com.TMF.Forum.Project.Organiser.Repository;

import com.TMF.Forum.Project.Organiser.Document.FinalUserJsonOutputDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinalUserJsonOutputRepository extends MongoRepository<FinalUserJsonOutputDocument, String> {
}
