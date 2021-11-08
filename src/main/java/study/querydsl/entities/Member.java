package study.querydsl.entities;

import lombok.*;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Getter
//@Setter     //가급적이면 실무에서 쓰지 않기
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "username", "age"})
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;
    private String username;
    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")       //연관관계 주인. member에서만 team의 외래키값을 변경할 수 있다. team에서는 읽기만 가능
    private Team team;

    public Member(String username, int age, Team team) {
        this.username = username;
        this.age = age;
        if (team != null) {
            changeTeam(team);       //양방향 연관관계 한번에 처리
        }
    }

    private void changeTeam(Team team) {
        this.team = team;                   //member 에서 team add
        team.getMembers().add(this);        //team 속의 member add
    }

}
