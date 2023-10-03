package com.TMF.Forum.Project.Organiser.DTO;

import lombok.Data;

@Data
public class SettingDefaultAndCustomValuesToUserJsonDTO {
    public String fieldName;
    public Object fieldValue;
    public boolean hasBeenGivenUserDefinedValue = false;
    public String dataType;
    public boolean isRequired = false;
    public boolean isTMF_ForumDefined;
    public String includedInJson="Not included";
}
