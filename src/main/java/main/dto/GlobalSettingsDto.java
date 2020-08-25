package main.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GlobalSettingsDto {
    @JsonProperty("MULTIUSER_MODE")
    private boolean MULTIUSER_MODE;
    @JsonProperty("POST_PREMODERATION")
    private boolean POST_PREMODERATION;
    @JsonProperty("STATISTICS_IS_PUBLIC")
    private boolean STATISTICS_IS_PUBLIC;
}
