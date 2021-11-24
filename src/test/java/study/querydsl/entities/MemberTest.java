package study.querydsl.entities;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Commit
class MemberTest {

    @Autowired
//    @PersistenceContext
    EntityManager em;

    @Test
    void 엔티티_생성() {
        Team teamA = new Team("TEAM A");
        Team teamB = new Team("TEAM B");

        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);


        //초기화 - 쿼리 날리기 전에 깔끔하게 컨텍스트를 비워준다.
        em.flush();
        em.clear();

        //실행
        List<Member> members = em.createQuery("select m from Member m", Member.class)
                .getResultList();

        //검증
        for (int i = 0; i < members.size(); i++) {
            assertThat(members.get(i).getUsername()).isEqualTo("member"+(i+1));
        }

    }
}