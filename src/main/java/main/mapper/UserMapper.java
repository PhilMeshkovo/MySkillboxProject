package main.mapper;

import main.api.response.UserApi;
import main.model.User;
import org.mapstruct.Mapper;

@Mapper
public class UserMapper {

  public UserApi userToUserApi(User user) {
    UserApi userApi = new UserApi();
    userApi.setId(user.getId());
    userApi.setName(user.getName());
    return userApi;
  }
}
