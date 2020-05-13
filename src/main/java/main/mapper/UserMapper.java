package main.mapper;

import main.api.response.UserApi;
import main.api.response.UserApiWithPhoto;
import main.model.User;
import org.springframework.stereotype.Component;

@Component
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

  public User userApiToUser(UserApi userApi) {
    User user = new User();
    user.setId(userApi.getId());
    user.setName(userApi.getName());
    return user;
  }

}
