package main.dto.response;

import lombok.Data;

@Data
public class ResultResponse {
  private boolean result;

  public void resultSuccess(){
    result = true;
  }
}
