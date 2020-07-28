package main.dto;

import lombok.Data;

@Data
public class AddPostDto {
    private Long timestamp;
    private Integer active;
    private String title;
    private String[] tags;
    private String text;
}
