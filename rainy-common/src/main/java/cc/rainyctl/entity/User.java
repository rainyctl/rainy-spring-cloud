package cc.rainyctl.entity;

import lombok.Data;

// we don't have a user table for this demo,
// just a POJO with mock data
@Data
public class User {
    private Long id;
    private String nickName;
    private String address;

    public static User aDio() {
        User dio = new User();
        dio.setId(1L);
        dio.setNickName("DIO");
        dio.setAddress("Cairo, Egypt");
        return dio;
    }
}
