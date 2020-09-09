package main.dto.request;

import lombok.Data;

@Data
public class AddPostRequest {
    private Long timestamp;
    private Integer active;
    private String title;
    private String[] tags;
    private String text;
}
