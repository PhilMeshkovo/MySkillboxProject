package main.mapper;

import main.api.response.UserApi;
import main.api.response.UserApiWithPhoto;
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

  public UserApiWithPhoto userToUserWithPhoto(User user) {
    UserApiWithPhoto userApi = new UserApiWithPhoto();
    userApi.setId(user.getId());
    userApi.setName(user.getName());
    userApi.setPhoto(user.getPhoto());

    return userApi;
  }
}
