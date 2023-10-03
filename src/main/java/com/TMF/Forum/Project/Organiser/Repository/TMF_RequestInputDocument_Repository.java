package com.TMF.Forum.Project.Organiser.Repository;

import com.TMF.Forum.Project.Organiser.Document.FinalUserJsonOutputDocument;
import com.TMF.Forum.Project.Organiser.Document.TMF_RequestInputDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TMF_RequestInputDocument_Repository extends MongoRepository<TMF_RequestInputDocument, String> {
}
