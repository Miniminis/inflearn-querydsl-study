package study.querydsl.entities;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
//@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)      //기본생성자 막기. JPA 스펙상 protected 는 열어 두어야 함
@ToString(of = {"id", "name"})      //연관관계 없는 내부 필드만 toString
public class Team {

    @Id @GeneratedValue
    private Long id;
    private String name;

    @OneToMany(mappedBy = "team")       //연관관계 주인이 아님 (거울)
    private List<Member> members = new ArrayList<>();

    public Team(String name) {
        this.name = name;
    }

}
