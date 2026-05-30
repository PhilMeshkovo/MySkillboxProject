package main.model;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import main.dto.request.*;
import main.dto.response.*;
import main.model.enums.ModerationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class BeanContractTest {

  private final List<Class<?>> beanTypes = List.of(
      AddPostRequest.class, ChangePasswordRequest.class, GlobalSettingsRequest.class, LoginRequest.class,
      PostCommentRequest.class, PostLikeRequest.class, PostModerationRequest.class, PostProfileRequest.class,
      PostProfileRequestWithPhoto.class, RegisterFormRequest.class, CommentsApiResponse.class, Errors.class,
      ListTagsResponse.class, PostByIdResponse.class, PostListResponse.class, ResponseApiInit.class,
      ResponsePostApi.class, ResponsePostApiToModeration.class, ResponsePostApiWithAnnounce.class,
      ResultResponse.class, ResultResponseWithErrors.class, ResultResponseWithUserDto.class,
      TagResponse.class, UserApiWithPhoto.class, UserDto.class, UserResponse.class, CaptchaCode.class,
      GlobalSettings.class, Post.class, PostComment.class, PostVotes.class, Tag.class, User.class);

  @Test
  void lombokBeansExposeUsableConstructorsAccessorsEqualsHashCodeAndToString() throws Exception {
    for (Class<?> type : beanTypes) {
      Object bean = instantiate(type);
      for (Field field : allFields(type)) {
        if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
          continue;
        }
        Object value = sampleValue(field.getType(), field.getName());
        Method setter = findSetter(type, field);
        if (setter != null) {
          setter.invoke(bean, value);
        } else {
          field.setAccessible(true);
          field.set(bean, value);
        }
        Method getter = findGetter(type, field);
        if (getter != null) {
          assertNotNull(getter.invoke(bean), type.getSimpleName() + "." + field.getName());
        }
      }
      assertEquals(bean, bean);
      assertNotEquals(bean, new Object());
      assertNotEquals(0, bean.hashCode());
      assertNotNull(bean.toString());
    }
  }

  @Test
  void explicitDomainMethodsReturnExpectedValues() {
    PostCommentRequest noParent = new PostCommentRequest(7, "comment text");
    assertFalse(noParent.isParentExist());
    assertTrue(new PostCommentRequest(1, 7, "comment text").isParentExist());

    ResultResponse response = new ResultResponse();
    assertFalse(response.isResult());
    response.resultSuccess();
    assertTrue(response.isResult());

    Role role = new Role(1, "ROLE_USER");
    assertEquals("ROLE_USER", role.getAuthority());

    User user = new User();
    user.setEmail("mail@test");
    assertEquals("mail@test", user.getUsername());
    assertTrue(user.getAuthorities().isEmpty());
    assertTrue(user.isAccountNonExpired());
    assertTrue(user.isAccountNonLocked());
    assertTrue(user.isCredentialsNonExpired());
    assertTrue(user.isEnabled());

    assertEquals(ModerationStatus.NEW, ModerationStatus.valueOf("NEW"));
  }

  private Object instantiate(Class<?> type) throws Exception {
    for (Constructor<?> constructor : type.getDeclaredConstructors()) {
      if (constructor.getParameterCount() == 0) {
        constructor.setAccessible(true);
        return constructor.newInstance();
      }
    }
    Constructor<?> constructor = type.getDeclaredConstructors()[0];
    constructor.setAccessible(true);
    Object[] args = new Object[constructor.getParameterCount()];
    Class<?>[] parameterTypes = constructor.getParameterTypes();
    for (int i = 0; i < parameterTypes.length; i++) {
      args[i] = sampleValue(parameterTypes[i], "arg" + i);
    }
    return constructor.newInstance(args);
  }

  private List<Field> allFields(Class<?> type) {
    java.util.ArrayList<Field> fields = new java.util.ArrayList<>();
    for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
      fields.addAll(List.of(current.getDeclaredFields()));
    }
    return fields;
  }

  private Method findSetter(Class<?> type, Field field) {
    String name = "set" + capitalize(field.getName());
    for (Method method : type.getMethods()) {
      if (method.getName().equals(name) && method.getParameterCount() == 1) {
        return method;
      }
    }
    return null;
  }

  private Method findGetter(Class<?> type, Field field) {
    String suffix = capitalize(field.getName());
    for (String name : List.of("get" + suffix, "is" + suffix)) {
      for (Method method : type.getMethods()) {
        if (method.getName().equals(name) && method.getParameterCount() == 0) {
          return method;
        }
      }
    }
    return null;
  }

  private String capitalize(String value) {
    return value.substring(0, 1).toUpperCase() + value.substring(1);
  }

  private Object sampleValue(Class<?> type, String fieldName) {
    if (type == String.class) return fieldName + "Value";
    if (type == int.class || type == Integer.class) return 1;
    if (type == long.class || type == Long.class) return 123L;
    if (type == boolean.class || type == Boolean.class) return true;
    if (type == double.class || type == Double.class) return 0.5d;
    if (type == LocalDateTime.class) return LocalDateTime.of(2020, 1, 1, 12, 0);
    if (type == String[].class) return new String[] {"java", "spring"};
    if (type == List.class) return List.of("item");
    if (type == Set.class) return Set.of();
    if (type == org.springframework.web.multipart.MultipartFile.class) {
      return new MockMultipartFile("photo", "photo.jpg", "image/jpeg", new byte[] {1, 2, 3});
    }
    if (type == Role.class) return new Role(1, "ROLE_USER");
    if (type == User.class) return new User();
    if (type == Post.class) return new Post();
    if (type == PostComment.class) return new PostComment();
    if (type == ModerationStatus.class) return ModerationStatus.ACCEPTED;
    if (type == UserResponse.class) return new UserResponse();
    if (type == UserDto.class) return UserDto.builder().id(1).name("name").build();
    if (type == Errors.class) return new Errors();
    return null;
  }
}
