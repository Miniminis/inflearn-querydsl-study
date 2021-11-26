package study.querydsl.dto;

import lombok.ToString;

@ToString
public class MemberDto {

    private String username;
    private Integer age;

    public MemberDto(String username, Integer age) {
        this.username = username;
        this.age = age;
    }
}
