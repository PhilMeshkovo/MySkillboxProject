package main.dto.response;

import lombok.Data;

@Data
public class ResultResponseWithErrors extends ResultResponse {
  private Errors errors;
}
