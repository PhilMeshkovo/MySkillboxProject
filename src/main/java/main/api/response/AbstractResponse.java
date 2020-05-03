package main.api.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class AbstractResponse {

  @JsonIgnore
  private boolean isSuccess;

  @JsonIgnore
  public boolean isSuccess() {
    return isSuccess;
  }

  public void setSuccess(boolean success) {
    isSuccess = success;
  }
}