package main.dto.response;

import lombok.Data;

@Data
public class ResultResponse {
  private boolean result;

  public static ResultResponse resultSuccess(){
    ResultResponse resultResponse = new ResultResponse();
    resultResponse.setResult(true);
    return resultResponse;
  }
}
