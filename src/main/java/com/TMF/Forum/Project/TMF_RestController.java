package com.TMF.Forum.Project;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonException;

import com.TMF.Forum.Project.Organiser.DTO.*;
import com.TMF.Forum.Project.Organiser.Document.*;
import com.TMF.Forum.Project.Organiser.Repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.mongodb.core.query.Query;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@CrossOrigin("*")
@RestController
public class TMF_RestController {
    @Autowired
    TMF_RequestInputDocument_Repository tmf_requestInputDocument_repository;

    @Autowired
    TmfToCustomerDataMappingRepository tmfToCustomerDataMappingRepository;
    @Autowired
    CellDataRepository cellDataRepository;
    @Autowired
    DatabaseSearchCriteriaRepository databaseSearchCriteriaRepository;
    @Autowired
    private AdminCredentialsRepository adminCredentialsRepository;
    private final MongoTemplate mongoTemplate;
    @Autowired
    private PathRelationDefinerDocument_AllRelationsRepository pathRelationDefinerDocumentAllRelationsRepository;
    @Autowired
    AllJsonInputsRepository allJsonInputsRepository;
    @Autowired
    FinalRequestStructureRepository finalRequestStructureRepository;
    @Autowired
    FinalResponseStructureRepository finalResponseStructureRepository;
    @Autowired
    PathHttpMethodMappedRepository pathHttpMethodMappedRepository;
    @Autowired
    AllUserInputRepository allUserInputRepository;
    @Autowired
    FinalUserJsonOutputRepository finalUserJsonOutputRepository;
    @Autowired
    JsonUriRepository jsonUriRepository;

    @Autowired
    JsonNodeRepository jsonNodeRepository;

    public TMF_RestController(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @GetMapping("/giveBackArray1")
    public List<String> giveBackArray1() {
        List<String> list1 = new ArrayList<>();
        list1.add("Apple");
        list1.add("Pineapple");
        list1.add("Banana");
        return list1;

    }

    @GetMapping("/giveBackArray2")
    public List<String> giveBackArray2() {
        List<String> list1 = new ArrayList<>();
        list1.add("Gamma");
        list1.add("Alpha");
        list1.add("Beta");
        return list1;
    }

    // ****************************************** FINALISED CODE*******************************************
    // To store all input json
    public String saveJson(Map<String, JsonNode> jsonData) {
        AllJsonInputsDocument allJsonInputsDocument = new AllJsonInputsDocument();
        allJsonInputsDocument.setData(jsonData);
        allJsonInputsRepository.save(allJsonInputsDocument);
        return "JSON document stored successfully!";
    }

    // To refresh all data in the easy-to-use format
    @GetMapping("/persistAllTMF_FormatsBegins")
    public String persistAllTMF_FormatsBegins() {
        try {
            deleteAllForRefresh();
            ObjectMapper objectMapper = new ObjectMapper();
            Query query = new Query();
            List<String> jsonStrings = mongoTemplate.find(query, String.class, "AllJsonInputs");
            List<JsonNode> jsonNodes = new ArrayList<>();
            JsonNode jsonNode = null;
            List<PathRelationDefinerDocument_AllRelationsDocument> pathRelationDefinerDTOSFinalList = new ArrayList<>();
            for (String jsonString : jsonStrings) {
                try {
                    jsonNode = objectMapper.readTree(jsonString);
                    pathRelationDefinerDTOSFinalList.addAll(consolidateDataInRightFormat(jsonNode.get("data")));
                    jsonNodes.add(jsonNode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            pathRelationDefinerDocumentAllRelationsRepository.saveAll(pathRelationDefinerDTOSFinalList);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Not refreshed");
        }
        System.out.println("Refreshed");
        return "All json structures refreshed";
    }

    public void deleteAllForRefresh() {
        finalResponseStructureRepository.deleteAll();
        pathRelationDefinerDocumentAllRelationsRepository.deleteAll();
        pathHttpMethodMappedRepository.deleteAll();
        finalRequestStructureRepository.deleteAll();
    }

    public List<PathHttpMethodMappingDocument> getAllHTTP_MethodList_PerPath(JsonNode pathsNode, String titleName) {
        List<PathHttpMethodMappingDocument> PathHttpMethodMappingDocuments = new ArrayList<>();
        Iterator<String> pathNames = pathsNode.fieldNames();
        while (pathNames.hasNext()) {
            PathHttpMethodMappingDocument pathHttpMethodMappingDocument = new PathHttpMethodMappingDocument();
            String pathName = pathNames.next();
            pathHttpMethodMappingDocument.endPoint = titleName;
            pathHttpMethodMappingDocument.pathName = pathName;
            JsonNode pathNode = pathsNode.get(pathName);
            List<String> allMethodTypes = new ArrayList<>();
            Iterator<String> methodNames = pathNode.fieldNames();
            while (methodNames.hasNext()) {
                String methodName = methodNames.next();
                allMethodTypes.add(methodName);
            }
            pathHttpMethodMappingDocument.allMethodTypes = allMethodTypes;
            PathHttpMethodMappingDocuments.add(pathHttpMethodMappingDocument);
        }
        pathHttpMethodMappedRepository.saveAll(PathHttpMethodMappingDocuments);
        return PathHttpMethodMappingDocuments;
    }

    public List<PathRelationDefinerDocument_AllRelationsDocument> consolidateDataInRightFormat(JsonNode dataNode) {
        List<PathRelationDefinerDocument_AllRelationsDocument> pathRelationDefiners = new ArrayList<>();
        JsonNode pathsNode = dataNode.get("paths");
        String jsonTitle = dataNode.get("info").get("title").asText();
        PathRelationDefinerDocument_AllRelationsDocument pathRelationDefinerDocumentAllRelations = new PathRelationDefinerDocument_AllRelationsDocument();
        pathRelationDefinerDocumentAllRelations.pathMethodTypeMapping = getAllHTTP_MethodList_PerPath(pathsNode, jsonTitle);
        pathRelationDefinerDocumentAllRelations.endPoint = jsonTitle;
        pathRelationDefinerDocumentAllRelations.allJsonStructures = getRequestStructure_Evaluated(dataNode, jsonTitle);
        pathRelationDefinerDocumentAllRelations.allResponseStructure = getResponseStructure_Evaluated(dataNode, jsonTitle);
        pathRelationDefiners.add(pathRelationDefinerDocumentAllRelations);
        return pathRelationDefiners;
    }

    public List<FinalRequestStructureDocument> getRequestStructure_Evaluated(JsonNode dataNode, String titleName) {
        JsonNode pathsNode = dataNode.get("paths");
        List<FinalRequestStructureDocument> finalList = new ArrayList<>();
        Iterator<String> pathNames = pathsNode.fieldNames();
        while (pathNames.hasNext()) {
            String pathName = pathNames.next();
            JsonNode pathNode = pathsNode.get(pathName);
            Iterator<String> httpMethods = pathNode.fieldNames();
            while (httpMethods.hasNext()) {
                FinalRequestStructureDocument finalRequestStructureDocument = new FinalRequestStructureDocument();
                String httpMethod = httpMethods.next();
                finalRequestStructureDocument.setEndPoint(titleName);
                finalRequestStructureDocument.methodType = httpMethod;
                finalRequestStructureDocument.pathName = pathName;
                JsonNode methodNode = pathNode.get(httpMethod);
                finalRequestStructureDocument.operationId = methodNode.get("operationId").asText();
                ArrayList<String> tags = new ArrayList<>();
                for (JsonNode tagNode : methodNode.get("tags")) {
                    tags.add(tagNode.asText());
                }
                finalRequestStructureDocument.tags = tags;
                ArrayList<ParameterDTO> parameters = new ArrayList<>();
                for (JsonNode paramNode : methodNode.get("parameters")) {
                    ParameterDTO parameterDTO = new ParameterDTO();
                    parameterDTO.name = paramNode.get("name").asText();
                    parameterDTO.description = paramNode.get("description").asText();
                    if (paramNode.has("type")) {
                        parameterDTO.type = paramNode.get("type").asText();
                    } else {
                        parameterDTO.type = "SchemaObject";
                    }
                    parameterDTO.required = paramNode.get("required").asBoolean();
                    parameters.add(parameterDTO);
                }
                finalRequestStructureDocument.parameters = parameters;
                finalList.add(finalRequestStructureDocument);
            }
        }
        finalRequestStructureRepository.saveAll(finalList);
        return finalList;
    }

    public List<FinalResponseStructureDocument> getResponseStructure_Evaluated(JsonNode dataNode, String titleName) {
        List<FinalResponseStructureDocument> finalList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode pathsNode = dataNode.get("paths");
        Iterator<String> pathNames = pathsNode.fieldNames();
        while (pathNames.hasNext()) {
            String pathName = pathNames.next();
            JsonNode pathNode = pathsNode.get(pathName);
            Iterator<String> methodTypes = pathNode.fieldNames();
            while (methodTypes.hasNext()) {
                String methodType = methodTypes.next();
                JsonNode methodNode = pathNode.get(methodType);
                List<ResponseSchemaDTO> responseSchemaDTOSList = new ArrayList<>();
                JsonNode responsesNode = methodNode.get("responses");
                Iterator<String> responseCodes = responsesNode.fieldNames();
                while (responseCodes.hasNext()) {
                    String statusCode = responseCodes.next();
                    JsonNode responseNode = responsesNode.get(statusCode);
                    ResponseSchemaDTO responseSchemaDTO = objectMapper.convertValue(responseNode, ResponseSchemaDTO.class);
                    responseSchemaDTO.statusCode = statusCode;
                    responseSchemaDTOSList.add(responseSchemaDTO);
                    System.out.println(responseSchemaDTO);
                }
                FinalResponseStructureDocument finalResponseStructureDocument = new FinalResponseStructureDocument();
                finalResponseStructureDocument.setResponseSchemaDTOSList(responseSchemaDTOSList);
                finalResponseStructureDocument.methodType = methodType;
                finalResponseStructureDocument.pathName = pathName;
                finalResponseStructureDocument.endPoint = titleName;
                finalList.add(finalResponseStructureDocument);
            }
        }
        finalResponseStructureRepository.saveAll(finalList);
        return finalList;
    }

    // Actual APIs
    @GetMapping("/getAllPathNames")
    public List<String> getAllPathNames(@RequestParam String endPoint) {
        List<String> allPathNames = new ArrayList<>();
        List<PathHttpMethodMappingDocument> pathHttpMethodMappingDocuments = pathHttpMethodMappedRepository.findOneByEndPoint(endPoint);
        for (int i = 0; i < pathHttpMethodMappingDocuments.size(); i++) {
            allPathNames.add(pathHttpMethodMappingDocuments.get(i).getPathName());
        }
        return allPathNames;
    }

    @GetMapping("/getAllMethodTypes")
    public List<String> getAllMethodTypes(@RequestParam String endPoint, @RequestParam String pathName) {
        List<String> allStatusCodes = new ArrayList<>();
        List<String> allMethodTypes = new ArrayList<>();
        List<PathHttpMethodMappingDocument> pathHttpMethodMappingDocuments = pathHttpMethodMappedRepository.findOneByEndPointAndPathName(endPoint, pathName);
        PathHttpMethodMappingDocument pathHttpMethodMappingDocument = null;
        if (pathHttpMethodMappingDocuments != null && pathHttpMethodMappingDocuments.size() != 0) {
            pathHttpMethodMappingDocument = pathHttpMethodMappingDocuments.get(0);
        }
        allMethodTypes.addAll(pathHttpMethodMappingDocument.getAllMethodTypes());
        return allMethodTypes;
    }

    @GetMapping("/getAllResponseStatuses")
    public List<String> getAllResponseStatuses(@RequestParam String endPoint, @RequestParam String pathName, @RequestParam String methodType) {
        List<String> allStatusCodes = new ArrayList<>();
        List<FinalResponseStructureDocument> finalResponseStructureDocumentList = finalResponseStructureRepository.findOneByEndPointAndPathNameAndMethodType(endPoint, pathName, methodType);
        FinalResponseStructureDocument finalResponseStructureDocument = null;
        if (finalResponseStructureDocumentList != null && finalResponseStructureDocumentList.size() != 0) {
            finalResponseStructureDocument = finalResponseStructureDocumentList.get(0);
        }
        for (int i = 0; i < finalResponseStructureDocument.getResponseSchemaDTOSList().size(); i++) {
            allStatusCodes.add(finalResponseStructureDocument.getResponseSchemaDTOSList().get(i).getStatusCode());
        }
        return allStatusCodes;
    }

    @GetMapping("showTMF_StructureForSelectedUserInput")
    public ArrayList<ParameterDTO> showTMF_StructureForSelectedUserInput(@RequestParam String endpoint, @RequestParam String pathName, @RequestParam String methodType) {
        List<FinalRequestStructureDocument> finalRequestStructureDocuments = finalRequestStructureRepository.findOneByEndPointAndPathNameAndMethodType(endpoint, pathName, methodType);
        FinalRequestStructureDocument finalRequestStructureDocument = null;
        if (finalRequestStructureDocuments != null && finalRequestStructureDocuments.size() != 0) {
            finalRequestStructureDocument = finalRequestStructureDocuments.get(0);
        }

        return finalRequestStructureDocument.getParameters();
    }


    @PostMapping("storeTheUserChosenParameters")
    public TMF_RequestInputDocument storeTheUserChosenParameters(@RequestBody TMF_RequestInputDTO tmf_requestInputDTO) {

        tmf_requestInputDocument_repository.deleteAll();
        TMF_RequestInputDocument tmf_requestInputDocument = new TMF_RequestInputDocument();
        tmf_requestInputDocument.setEndPoint(tmf_requestInputDTO.getEndPoint());
        tmf_requestInputDocument.setPathName(tmf_requestInputDTO.getPathName());
        tmf_requestInputDocument.setMethodType(tmf_requestInputDTO.getMethodType());
        tmf_requestInputDocument.setResponseType(tmf_requestInputDTO.getResponseType());
        TMF_RequestInputDocument result = mongoTemplate.insert(tmf_requestInputDocument, "UserChosenTMFParameters");
        return tmf_requestInputDocument;
    }

    public static boolean validateJson(String jsonString) {
        JsonReaderFactory factory = Json.createReaderFactory(null);

        try (JsonReader reader = factory.createReader(new StringReader(jsonString))) {
            JsonObject jsonObject = reader.readObject();
            return true;
        } catch (JsonException e) {
            return false;
        }
    }

    @PostMapping("/storeUserInput")
    public String storeUserInput(@RequestBody String inputJson) {
        try {
            boolean isJsonValid = validateJson(inputJson);
            if (!isJsonValid) {
                return "Invalid Json";
            }
            AllUserInputsDocument allUserInputsDocument = new AllUserInputsDocument();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootJsonNode = objectMapper.readTree(inputJson);
            allUserInputsDocument.setTableName(rootJsonNode.get("table_Name").toString());
            rootJsonNode = objectMapper.readTree(rootJsonNode.get("textareaContent").textValue());
            List<UserJsonInputKeyValuePairDTO> resultList = convertJsonNodeToDTO(rootJsonNode);
            allUserInputsDocument.setUserData(resultList);
//            allUserInputRepository.deleteAll();
            allUserInputRepository.save(allUserInputsDocument);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "File uploaded";
    }


    public List<UserJsonInputKeyValuePairDTO> flattenJsonMapToList(List<Map<Integer, List<UserJsonInputKeyValuePairDTO>>> jsonMapList) {
        List<UserJsonInputKeyValuePairDTO> resultList = new ArrayList<>();

        for (Map<Integer, List<UserJsonInputKeyValuePairDTO>> jsonMap : jsonMapList) {
            for (List<UserJsonInputKeyValuePairDTO> employeeFields : jsonMap.values()) {
                resultList.addAll(employeeFields);
            }
        }

        return resultList;
    }


    private static JsonNode processJsonNode(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            List<String> fieldsToModify = new ArrayList<>();

            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                JsonNode value = entry.getValue();
                if (value.isObject() || value.isArray()) {
                    processJsonNode(value);
                }
                if (entry.getKey().equals("fieldName")) {
                    fieldsToModify.add(entry.getKey());
                }
            }

            for (String fieldName : fieldsToModify) {
                JsonNode fieldValueNode = objectNode.get("fieldValue");
                if (fieldValueNode != null && fieldValueNode.isTextual()) {
                    String fieldValue = fieldValueNode.asText();
                    objectNode.remove("fieldName");
                    objectNode.remove("fieldValue");
                    objectNode.put(fieldName, fieldValue);
                }
            }
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (JsonNode element : arrayNode) {
                processJsonNode(element);
            }
        }
        return node;
    }

    public List<UserJsonInputKeyValuePairDTO> convertJsonNodeToDTO(JsonNode jsonNode) {
        List<Map<Integer, List<UserJsonInputKeyValuePairDTO>>> result = new ArrayList<>();

        if (jsonNode.isArray()) {
            int index = 0;
            for (JsonNode employeeNode : jsonNode) {
                if (employeeNode.isObject()) {
                    Map<Integer, List<UserJsonInputKeyValuePairDTO>> employeeMap = new HashMap<>();
                    List<UserJsonInputKeyValuePairDTO> employeeFields = new ArrayList<>();

                    Iterator<Map.Entry<String, JsonNode>> fields = employeeNode.fields();
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> field = fields.next();
                        UserJsonInputKeyValuePairDTO dto = new UserJsonInputKeyValuePairDTO();
                        dto.setFieldName(field.getKey());
                        dto.setFieldValue(convertJsonValue(field.getValue()));
                        employeeFields.add(dto);
                    }

                    employeeMap.put(index, employeeFields);
                    result.add(employeeMap);
                    index++;
                }
            }
        }
        List<UserJsonInputKeyValuePairDTO> flattenedResult = flattenJsonMapToList(result);
        return flattenedResult;
    }

    public Object convertJsonValue(JsonNode jsonValue) {
        if (jsonValue.isObject() || jsonValue.isArray()) {
            return convertJsonNodeToDTO(jsonValue);
        } else if (jsonValue.isTextual()) {
            return jsonValue.textValue();
        } else if (jsonValue.isNumber()) {
            return jsonValue.numberValue();
        } else if (jsonValue.isBoolean()) {
            return jsonValue.booleanValue();
        } else {
            return null;
        }
    }

    @GetMapping("/deleteAllUserInput")
    public String deleteAllUserInput() {
        allUserInputRepository.deleteAll();
        tmfToCustomerDataMappingRepository.deleteAll();
        databaseSearchCriteriaRepository.deleteAll();
        cellDataRepository.deleteAll();
        return "All inputs deleted";
    }

    @GetMapping("/AddMandatoryFields")
    public List<SettingDefaultAndCustomValuesToUserJsonDTO> settingDefaultOrCustomValues(@RequestBody GetAllFieldNamesForUserEdittingDTO dynamicallyModifyingJsonInputDTO) {
        List<String> listOfFinalFieldNamesForTracking = new ArrayList<>();
        List<SettingDefaultAndCustomValuesToUserJsonDTO> listOfFinalJsonStructureDetails = new ArrayList<>();
        List<FinalRequestStructureDocument> tmfStructures = finalRequestStructureRepository.findOneByEndPointAndPathNameAndMethodType(dynamicallyModifyingJsonInputDTO.getEndPoint(), dynamicallyModifyingJsonInputDTO.getPathName(), dynamicallyModifyingJsonInputDTO.getMethodType());
        FinalRequestStructureDocument tmfStructure = null;
        if (tmfStructures != null && tmfStructures.size() != 0) {
            tmfStructure = tmfStructures.get(0);
        }
        boolean flag_presentInTMF_Structure = false;
        SettingDefaultAndCustomValuesToUserJsonDTO obj = null;
        for (int j = 0; j < dynamicallyModifyingJsonInputDTO.getFieldNames().size(); j++) {
            for (int i = 0; i < tmfStructure.getParameters().size(); i++) {
                obj = new SettingDefaultAndCustomValuesToUserJsonDTO();
                if (dynamicallyModifyingJsonInputDTO.getFieldNames().get(j).getFieldName().equalsIgnoreCase(tmfStructure.getParameters().get(i).getName())) {
                    obj.setFieldName(tmfStructure.getParameters().get(i).getName());
                    obj.setDataType(tmfStructure.getParameters().get(i).getType());
                    if (dynamicallyModifyingJsonInputDTO.getFieldNames().get(j).getFieldValue() != null) {
                        obj.setFieldValue(dynamicallyModifyingJsonInputDTO.getFieldNames().get(j).getFieldValue());
                        obj.setHasBeenGivenUserDefinedValue(true);
                    }
                    if (tmfStructure.getParameters().get(i).required) {
                        obj.isRequired = true;
                    }
                    obj.isTMF_ForumDefined = true;
                    obj.setIncludedInJson("Included");
                    flag_presentInTMF_Structure = true;
                    listOfFinalJsonStructureDetails.add(obj);
                    listOfFinalFieldNamesForTracking.add(obj.getFieldName());
                } else if (tmfStructure.getParameters().get(i).required && !listOfFinalFieldNamesForTracking.contains(tmfStructure.getParameters().get(i).getName())) {
                    obj.setFieldName(tmfStructure.getParameters().get(i).getName());
                    obj.setFieldValue(null);
                    obj.isTMF_ForumDefined = true;
                    obj.isRequired = true;
                    obj.hasBeenGivenUserDefinedValue = false;
                    obj.setIncludedInJson("Included");
                    obj.setDataType(tmfStructure.getParameters().get(i).getType());
                    listOfFinalJsonStructureDetails.add(obj);
                    listOfFinalFieldNamesForTracking.add(obj.getFieldName());
                }
            }
            if (!flag_presentInTMF_Structure) {
                obj.setFieldName(dynamicallyModifyingJsonInputDTO.getFieldNames().get(j).getFieldName());
                obj.setFieldValue(dynamicallyModifyingJsonInputDTO.getFieldNames().get(j).getFieldValue());
                obj.setTMF_ForumDefined(false);
                if (obj.getFieldValue() != null) {
                    obj.hasBeenGivenUserDefinedValue = true;
                }
                obj.setIncludedInJson("Included");
                listOfFinalJsonStructureDetails.add(obj);
                listOfFinalFieldNamesForTracking.add(obj.getFieldName());
            }
            flag_presentInTMF_Structure = false;
        }
        String type = "";
        for (int i = 0; i < listOfFinalJsonStructureDetails.size(); i++) {
            type = listOfFinalJsonStructureDetails.get(i).getDataType();
            if (listOfFinalJsonStructureDetails.get(i).getFieldValue() == null && listOfFinalJsonStructureDetails.get(i).isRequired) {
                if (("integer").equalsIgnoreCase(type) || ("short").equalsIgnoreCase(type) || ("long").equalsIgnoreCase(type)) {
                    listOfFinalJsonStructureDetails.get(i).setFieldValue(0);
                } else if (("double").equalsIgnoreCase(type) || ("short").equalsIgnoreCase(type) || ("float").equalsIgnoreCase(type)) {
                    listOfFinalJsonStructureDetails.get(i).setFieldValue(0.0);
                } else if (("string").equalsIgnoreCase(type)) {
                    listOfFinalJsonStructureDetails.get(i).setFieldValue("");
                } else if (("boolean").equalsIgnoreCase(type)) {
                    listOfFinalJsonStructureDetails.get(i).setFieldValue(false);
                } else {
                    listOfFinalJsonStructureDetails.get(i).setFieldValue(null);
                }
            }
        }
        return listOfFinalJsonStructureDetails;
    }


    // APIs specifically for the UI

    @PostMapping("/createFinalUserJsonResponse")
    public JsonNode createFinalUserJsonResponse(@RequestBody List<UserJsonInputKeyValuePairDTO> userChosenTMF_Fields) {
        GetAllFieldNamesForUserEdittingDTO dynamicFieldInput = new GetAllFieldNamesForUserEdittingDTO();
        dynamicFieldInput.setFieldNames(userChosenTMF_Fields);
        TMF_RequestInputDTO tmf_requestInputDTO = getChosenUserInputParameters();
        dynamicFieldInput.setMethodType(tmf_requestInputDTO.getMethodType());
        dynamicFieldInput.setPathName(tmf_requestInputDTO.getPathName());
        AllUserInputsDocument allUserInputsDocument = allUserInputRepository.findAll().get(0);
        List<SettingDefaultAndCustomValuesToUserJsonDTO> settingDefaultAndCustomValuesToUserJsonDTOS = settingDefaultOrCustomValues(dynamicFieldInput);
        List<UserJsonInputKeyValuePairDTO> userJsonInputKeyValuePairDTOS = new ArrayList<>();
        UserJsonInputKeyValuePairDTO userJsonInputKeyValuePairDTO = null;
        for (int i = 0; i < settingDefaultAndCustomValuesToUserJsonDTOS.size(); i++) {
            userJsonInputKeyValuePairDTO = new UserJsonInputKeyValuePairDTO();
            userJsonInputKeyValuePairDTO.setFieldName(settingDefaultAndCustomValuesToUserJsonDTOS.get(i).getFieldName());
            userJsonInputKeyValuePairDTO.setFieldValue(settingDefaultAndCustomValuesToUserJsonDTOS.get(i).getFieldValue());
            userJsonInputKeyValuePairDTOS.add(userJsonInputKeyValuePairDTO);
        }
        allUserInputsDocument.getUserData().addAll(userJsonInputKeyValuePairDTOS);
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode resultNode = objectMapper.createObjectNode();
        for (UserJsonInputKeyValuePairDTO dto : allUserInputsDocument.getUserData()) {
            resultNode.set(dto.fieldName, objectMapper.valueToTree(dto.fieldValue));
        }
        JsonNode finalResult = processJsonNode(resultNode);

        return finalResult;
    }

    @GetMapping("getAllUserInput")
    public List<UserJsonInputKeyValuePairDTO> getAllUserInput() {
        return allUserInputRepository.findAll().get(0).getUserData();
    }

    @GetMapping("getChosenUserInputParameters")
    public TMF_RequestInputDTO getChosenUserInputParameters() {
        TMF_RequestInputDocument tmf_requestInputDocument = tmf_requestInputDocument_repository.findAll().get(0);
        TMF_RequestInputDTO tmf_requestInputDTO = new TMF_RequestInputDTO();
        tmf_requestInputDTO.setMethodType(tmf_requestInputDocument.getMethodType());
        tmf_requestInputDTO.setPathName(tmf_requestInputDocument.getPathName());
        tmf_requestInputDTO.setEndPoint(tmf_requestInputDocument.getEndPoint());
        return tmf_requestInputDTO;
    }

    @GetMapping("showTMF_Structure_AfterChoosingParameters")
    public ArrayList<ParameterDTO> showTMF_Structure_AfterChoosingParameters() {
        TMF_RequestInputDTO tmf_requestInputDTO = getChosenUserInputParameters();
        List<FinalRequestStructureDocument> finalRequestStructureDocuments = finalRequestStructureRepository.findOneByEndPointAndPathNameAndMethodType(tmf_requestInputDTO.getEndPoint(), tmf_requestInputDTO.getPathName(), tmf_requestInputDTO.getMethodType());
        FinalRequestStructureDocument finalRequestStructureDocument = null;
        if (finalRequestStructureDocuments != null && finalRequestStructureDocuments.size() != 0) {
            finalRequestStructureDocument = finalRequestStructureDocuments.get(0);
        }
        return finalRequestStructureDocument.getParameters();
    }


    private FinalRequestStructureDocument getTheJsonRequestStructureChosenByCustomer() {
        TMF_RequestInputDTO tmf_requestInputDTO = getChosenUserInputParameters();
        List<FinalRequestStructureDocument> tmfStructureRequestDocs = finalRequestStructureRepository.findOneByEndPointAndPathNameAndMethodType(tmf_requestInputDTO.getEndPoint(), tmf_requestInputDTO.getPathName(), tmf_requestInputDTO.getMethodType());
        FinalRequestStructureDocument tmfStructureRequestDoc = null;
        if (tmfStructureRequestDocs != null && tmfStructureRequestDocs.size() != 0) {
            tmfStructureRequestDoc = tmfStructureRequestDocs.get(0);
        }
        return tmfStructureRequestDoc;

    }

    @GetMapping("/getAllTMF_Forum_FieldsToChooseFrom")
    public List<String> getAllTMF_Forum_FieldsToChooseFrom() {
        List<String> listOfAllTMF_ForumFields = new ArrayList<>();
        FinalRequestStructureDocument tmfStructureRequestDoc = getTheJsonRequestStructureChosenByCustomer();
        if (tmfStructureRequestDoc != null) {
            for (int i = 0; i < tmfStructureRequestDoc.getParameters().size(); i++) {
                listOfAllTMF_ForumFields.add(tmfStructureRequestDoc.getParameters().get(i).getName());
            }
        }
        return listOfAllTMF_ForumFields;
    }


    @GetMapping("/getAllTMF_Forum_FieldsToChooseFromMandatory")
    public List<String> getAllTMF_Forum_FieldsToChooseFromMandatory() {
        TMF_RequestInputDTO tmf_requestInputDTO = getChosenUserInputParameters();
        List<String> listOfAllTMF_ForumFields = new ArrayList<>();
        List<FinalRequestStructureDocument> tmfStructureRequestDocs = finalRequestStructureRepository.findOneByEndPointAndPathNameAndMethodType(tmf_requestInputDTO.getEndPoint(), tmf_requestInputDTO.getPathName(), tmf_requestInputDTO.getMethodType());
        FinalRequestStructureDocument tmfStructureRequestDoc = null;
        if (tmfStructureRequestDocs != null && tmfStructureRequestDocs.size() != 0) {
            tmfStructureRequestDoc = tmfStructureRequestDocs.get(0);
            for (int i = 0; i < tmfStructureRequestDoc.getParameters().size(); i++) {
                if (tmfStructureRequestDoc.getParameters().get(i).isRequired() == true) {
                    listOfAllTMF_ForumFields.add(tmfStructureRequestDoc.getParameters().get(i).getName());
                }
            }
        }
        return listOfAllTMF_ForumFields;
    }


    @PostMapping("/postTMFJsonByAdmin")
    public String postTMFJsonByAdmin(@RequestBody String requestBody) {
        JsonUriDocuments jsonUriDocuments = new JsonUriDocuments();
        ObjectMapper objectMapper = new ObjectMapper();
        HttpClient httpClient = HttpClient.newHttpClient();
        try {
            List<JsonUriDocuments> jsonUriDocumentsList = jsonUriRepository.findAll();

            for (JsonUriDocuments jsonUriDocumentObj : jsonUriDocumentsList) {
                if ((requestBody).equalsIgnoreCase(jsonUriDocumentObj.getJsonUri())) {
                    return "This TMF Forum schema already exists";
                }
            }
            jsonUriDocuments.setJsonUri(requestBody);
            jsonUriRepository.save(jsonUriDocuments);
            JsonNode requestNode = objectMapper.readTree(requestBody);
            String url = requestNode.get("schema").asText();
            URI uri = new URI(url);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String responseBody = response.body();

                Map<String, JsonNode> jsonMap = objectMapper.readValue(responseBody, Map.class);

                saveJson(jsonMap);
            } else {
                return ("Request failed with status code: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Something went wrong. Please try again later";
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Saved");
        return "Input saved successfully";
    }

    @GetMapping("getResponseJson")
    public SchemaDTO getRequestJson(@RequestParam String endPoint, @RequestParam String pathName, @RequestParam String methodType, @RequestParam String statusCode) {
        List<FinalResponseStructureDocument> finalResponseStructureDocumentList = finalResponseStructureRepository.findOneByEndPointAndPathNameAndMethodType(endPoint, pathName, methodType);
        FinalResponseStructureDocument finalResponseStructureDocument = null;
        if (finalResponseStructureDocumentList != null && finalResponseStructureDocumentList.size() != 0) {
            finalResponseStructureDocument = finalResponseStructureDocumentList.get(0);
        }
        SchemaDTO schemaDTO = new SchemaDTO();
        for (int i = 0; i < finalResponseStructureDocument.getResponseSchemaDTOSList().size(); i++) {
            if ((statusCode).equalsIgnoreCase(finalResponseStructureDocument.getResponseSchemaDTOSList().get(i).getStatusCode())) {
                schemaDTO = finalResponseStructureDocument.getResponseSchemaDTOSList().get(i).getSchema();
            }
        }
        System.out.println(schemaDTO);
        return schemaDTO;
    }

    @GetMapping("getAdminCredentials")
    public AdminLoginCredentialsDocument getAdminCredentials() {
        return adminCredentialsRepository.findAll().get(0);
    }


    // ************************************* Created after changes suggested by sudeep sir ******************************


    @PostMapping("/storeJsonToMongo")
    public String storeJsonToMongo(@RequestBody JsonNode inputValue) throws Exception {
        try {

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode inputJson = objectMapper.readTree(inputValue.get("textareaContent").textValue());
//            boolean isJsonValid = validateJson(inputJson.toString());
//            if (!isJsonValid) {
//                return "Invalid Json";
//            }
            String tableName = inputValue.get("table_Name").asText();
            if (inputJson.isArray()) {
                Iterator<JsonNode> arrayIterator = inputJson.iterator();
                while (arrayIterator.hasNext()) {
                    JsonNode object = arrayIterator.next();
                    Map<String, Object> jsonMap = objectMapper.convertValue(object, Map.class);
                    jsonMap.put("tableName", tableName);
                    mongoTemplate.save(jsonMap, "AllUserInputs");
                }
            } else {
                Map<String, Object> jsonMap = objectMapper.convertValue(inputJson, Map.class);
                Set<String> customerTableNamesAlreadyExisiting = getTableNames();
                if (customerTableNamesAlreadyExisiting.contains(tableName)) {
                    return "This table name already exists. Please give a different table name";
                }
                jsonMap.put("tableName", tableName);
                mongoTemplate.save(jsonMap, "AllUserInputs");
            }
        } catch (Exception e) {
            return "Something went wrong. Please contact the developer";
        }
        return "Data saved successfully";
    }

    @GetMapping("/get-Table-name")
    public Set<String> getTableNames() {
        List<AllUserInputsDocument> allUserInputsDocumentsList = allUserInputRepository.findAll();
        Set<String> tableNamesList = new HashSet();
        for (AllUserInputsDocument allUserInputsDocument : allUserInputsDocumentsList) {
            tableNamesList.add(allUserInputsDocument.getTableName());
        }
        return tableNamesList;
    }

    @GetMapping("/getTableNameCorrespondingToFirst")
    public Set<String> getTableNameCorrespondingToFirst(@RequestParam String tableName) {
        List<AllUserInputsDocument> allUserInputsDocumentsList = allUserInputRepository.findAll();
        Set<String> tableNamesList = new HashSet();
        for (AllUserInputsDocument allUserInputsDocument : allUserInputsDocumentsList) {
            if (!allUserInputsDocument.getTableName().equalsIgnoreCase(tableName)) {
                tableNamesList.add(allUserInputsDocument.getTableName());
            }
        }
        return tableNamesList;
    }


    @GetMapping("/get-field-names")
    public Set<String> getAllFieldNamesFromDocument(@RequestParam String tableName) {
        Query query = new Query(Criteria.where("tableName").is(tableName));
        List<Map> documents = mongoTemplate.find(query, Map.class, "AllUserInputs");
        return commonLinesInEvaluatingFields(documents);
    }

    @GetMapping("/getAllFieldNames")
    public Set<String> getAllFieldNames() {
        List<Map> documents = mongoTemplate.findAll(Map.class, "AllUserInputs");
        return commonLinesInEvaluatingFields(documents);
    }


    private Set<String> commonLinesInEvaluatingFields(List<Map> documents) {
        Set<String> fieldNames = new HashSet<>();
        for (Map<String, Object> document : documents) {
            extractFieldNames(document, "", fieldNames);
        }
        return fieldNames;
    }

    private void extractFieldNames(Map<String, Object> document, String prefix, Set<String> fieldNames) {
        for (Map.Entry<String, Object> entry : document.entrySet()) {
            String fieldName = prefix + entry.getKey();
            Object fieldValue = entry.getValue();
            if (fieldValue instanceof Map) {
                extractFieldNames((Map<String, Object>) fieldValue, fieldName + ".", fieldNames);
            } else if (fieldValue instanceof List) {
                List<Object> list = (List<Object>) fieldValue;
                for (int i = 0; i < list.size(); i++) {
                    Object listElement = list.get(i);
                    if (listElement instanceof Map) {
                        extractFieldNames((Map<String, Object>) listElement, fieldName + "[" + i + "].", fieldNames);
                    }
                }
            } else {
                fieldNames.add(fieldName);
            }
        }
    }

    @PostMapping("/saveTableToTableMapping")
    public String saveTableToTableMapping(@RequestBody List<TableMappingDocument> cellDataList) {
        try {
            cellDataRepository.saveAll(cellDataList);
            return "Data saved successfully.";
        } catch (Exception e) {
            return "Error saving data: " + e.getMessage();
        }
    }

    @PostMapping("/saveTmfToCustomerMapping")
    public List<String> saveTmfToCustomerMapping(@RequestBody TmfToCustomerDataMappingDocument fieldDataList) {
        List<String> listOfAllMandatoryTMF_ForumFields = new ArrayList<>();
        try {
            Set<String> uniqueTmfFieldNameForChosenParamenters = new HashSet<>();
            FinalRequestStructureDocument tmfStructureRequestDoc = getTheJsonRequestStructureChosenByCustomer();
            Query query = Query.query(
                    Criteria.where("endPoint").is(fieldDataList.getEndPoint())
                            .and("pathName").is(fieldDataList.getPathName())
                            .and("methodType").is(fieldDataList.getMethodType())
            );
            List<CustomerTableDetailsMappingWithTMF> mappingWithTMFList = new ArrayList<>();
            for (CustomerTableDetailsMappingWithTMF mappingWithTMF : fieldDataList.getMapping()) {
                CustomerTableDetailsMappingWithTMF customerTableDetailsMappingWithTMF = new CustomerTableDetailsMappingWithTMF();
                customerTableDetailsMappingWithTMF.setCustomerTableName(mappingWithTMF.getCustomerTableName());
                customerTableDetailsMappingWithTMF.setTmfFieldName(mappingWithTMF.getTmfFieldName());
                customerTableDetailsMappingWithTMF.setCustomerFieldName(mappingWithTMF.getCustomerFieldName());
                mappingWithTMFList.add(mappingWithTMF);
                uniqueTmfFieldNameForChosenParamenters.add(mappingWithTMF.getTmfFieldName());
            }
            if (tmfStructureRequestDoc != null) {
                for (int i = 0; i < tmfStructureRequestDoc.getParameters().size(); i++) {
                    if (tmfStructureRequestDoc.getParameters().get(i).isRequired() == true && !uniqueTmfFieldNameForChosenParamenters.contains(tmfStructureRequestDoc.getParameters().get(i).getName())) {
                        listOfAllMandatoryTMF_ForumFields.add(tmfStructureRequestDoc.getParameters().get(i).getName());
                    }
                }
            }
            Update update = new Update()
                    .set("mapping", mappingWithTMFList);
            mongoTemplate.upsert(query, update, TmfToCustomerDataMappingDocument.class);


            return listOfAllMandatoryTMF_ForumFields;
        } catch (Exception e) {
            e.printStackTrace();
            return listOfAllMandatoryTMF_ForumFields;
        }
    }

    @PostMapping("/saveDefaults")
    public String saveDefaults(@RequestBody TmfToCustomerDataMappingDocument tmfToCustomerDataMappingDocument) {
        try {
            Query query = Query.query(
                    Criteria.where("endPoint").is(tmfToCustomerDataMappingDocument.getEndPoint())
                            .and("pathName").is(tmfToCustomerDataMappingDocument.getPathName())
                            .and("methodType").is(tmfToCustomerDataMappingDocument.getMethodType())
            );
            Update update = new Update().set("defaults", tmfToCustomerDataMappingDocument.getDefaults());
            mongoTemplate.upsert(query, update, TmfToCustomerDataMappingDocument.class);
            return "Defaults saved successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "Unable to save the defaults due to some error.";
        }
    }

    @PostMapping("/saveDatabaseSearchCriteria")
    public String saveDatabaseSearchCriteria(@RequestBody List<DatabaseSearchCriteriaDocument> criteriaList) {
        try {
            databaseSearchCriteriaRepository.saveAll(criteriaList);
            return "Data saved successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error saving data: " + e.getMessage();
        }
    }

    @GetMapping("/deleteAllSearchCriteria")
    public String deleteAllSearchCriteria() {
        try {
            databaseSearchCriteriaRepository.deleteAll();
            return "Data deleted successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error deleting data: " + e.getMessage();
        }
    }


    public Set<String> getFieldNames(
    ) {
        Set<String> fieldNames = new HashSet<>();
        Set<String> finalset = new HashSet<>();
        try {
            List<Object> documentList = mongoTemplate.findAll(Object.class, "AllUserInputs");
            for (Object document : documentList) {
                fieldNames = extractFieldNames(document);
            }
            finalset.addAll(fieldNames);
            return finalset;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Set<String> extractFieldNames(Object object) {
        Set<String> fieldNames = new HashSet<>();

        if (object instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) object;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                fieldNames.add(entry.getKey().toString());
                fieldNames.addAll(extractFieldNames(entry.getValue()));
            }
        } else if (object instanceof List) {
            List<?> list = (List<?>) object;
            for (Object item : list) {
                fieldNames.addAll(extractFieldNames(item));
            }
        } else if (object instanceof Document) {
            Document document = (Document) object;
            for (String key : document.keySet()) {
                fieldNames.add(key);
                fieldNames.addAll(extractFieldNames(document.get(key)));
            }
        }

        return fieldNames;
    }


    public Document getFieldFromMongoDocument(String tableName, String fieldName) {
        Query query = new Query();
        query.addCriteria(Criteria.where("tableName").is(tableName));
        query.addCriteria(Criteria.where(fieldName).exists(true));
        List<Document> result = mongoTemplate.find(query, Document.class, "AllUserInputs");
        if (result != null) {
            // The field exists in the document, retrieve its value
            return result.get(0);
        } else {
            // The field does not exist in any document
            return null;
        }
    }

    @GetMapping("/getFinalResponse")
    public Map<String, Object> getFinalResponse() {
        TMF_RequestInputDTO tmf_requestInputDTO = getChosenUserInputParameters();
        List<String> outputTMF_FieldsList = getAllTMF_Forum_FieldsToChooseFrom();
        Set<String> tMF_fieldsWhoseValueWasMapped = new HashSet<>();
        TmfToCustomerDataMappingDocument tmfToCustomerDataMappingDocument = tmfToCustomerDataMappingRepository.findOneByEndPointAndPathNameAndMethodType(tmf_requestInputDTO.getEndPoint(), tmf_requestInputDTO.getPathName(), tmf_requestInputDTO.getMethodType());
        List<DatabaseSearchCriteriaDocument> databaseSearchCriteriaDocumentList = databaseSearchCriteriaRepository.findAll();
        Map<String, Object> finalResponse = new HashMap<>();
        for (DatabaseSearchCriteriaDocument databaseSearchCriteriaDocument : databaseSearchCriteriaDocumentList) {
            Query query = new Query();
            query.addCriteria(Criteria.where("tableName").is(databaseSearchCriteriaDocument.getTableName()));
            query.addCriteria(Criteria.where(databaseSearchCriteriaDocument.getFieldName()).is(databaseSearchCriteriaDocument.getSearchCriteria()));
            List<Document> documentList = mongoTemplate.find(query, Document.class, "AllUserInputs");
            for (String tmf_field : outputTMF_FieldsList) {
                for (CustomerTableDetailsMappingWithTMF customerTableDetailsMappingWithTMF : tmfToCustomerDataMappingDocument.getMapping()) {
                    if (customerTableDetailsMappingWithTMF.getTmfFieldName().equalsIgnoreCase(tmf_field)) {
                        if (documentList != null && documentList.size() > 0) {
                            if (documentList.get(0).containsKey(customerTableDetailsMappingWithTMF.getCustomerFieldName()))
                            {
                                finalResponse.put(tmf_field, documentList.get(0).get(customerTableDetailsMappingWithTMF.getCustomerFieldName()));
                                if (documentList.get(0).get(customerTableDetailsMappingWithTMF.getCustomerFieldName()) != null) {
                                    tMF_fieldsWhoseValueWasMapped.add(tmf_field);
                                }
                            }
                        }
                    }
                }
            }
        }
        outputTMF_FieldsList.removeAll(tMF_fieldsWhoseValueWasMapped);
        for (String tmf_field : outputTMF_FieldsList) {
            if (tmfToCustomerDataMappingDocument.getDefaults()!=null) {
                for (DefaultValuesDTO defaultValuesDTO : tmfToCustomerDataMappingDocument.getDefaults()) {
                    if (defaultValuesDTO.getTmfFieldName().equalsIgnoreCase(tmf_field)) {
                        finalResponse.put(tmf_field, defaultValuesDTO.getDefaultValue());
                    }
                }
            }
        }
        return finalResponse;
    }

    @GetMapping("/getAllTitles")
    public Set<String> getAllTitles() {
        List<PathRelationDefinerDocument_AllRelationsDocument> PathRelationDefinerDocument_AllRelationsDocumentList = pathRelationDefinerDocumentAllRelationsRepository.findAll();
        Set<String> listOfAllTitles = new HashSet<>();
        for (PathRelationDefinerDocument_AllRelationsDocument pathRelationDefinerDocument_allRelationsDocument : PathRelationDefinerDocument_AllRelationsDocumentList) {
            listOfAllTitles.add(pathRelationDefinerDocument_allRelationsDocument.getEndPoint());
        }
        return listOfAllTitles;
    }


}







