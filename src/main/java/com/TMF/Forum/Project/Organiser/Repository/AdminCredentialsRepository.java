package com.TMF.Forum.Project.Organiser.Repository;

import com.TMF.Forum.Project.Organiser.Document.AdminLoginCredentialsDocument;
import com.TMF.Forum.Project.Organiser.Document.AllJsonInputsDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminCredentialsRepository extends MongoRepository<AdminLoginCredentialsDocument, String> {

}
