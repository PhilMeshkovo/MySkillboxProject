package main.mapper;

import main.dto.response.UserApiWithPhoto;
import main.dto.response.UserResponse;
import main.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public UserResponse userToUserApi(User user) {
    UserResponse userApi = new UserResponse();
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

  public User userApiToUser(UserResponse userApi) {
    User user = new User();
    user.setId(userApi.getId());
    user.setName(userApi.getName());
    return user;
  }

}
