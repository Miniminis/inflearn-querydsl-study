package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import study.querydsl.entities.Member;
import study.querydsl.entities.Team;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entities.QMember.member;
import static study.querydsl.entities.QTeam.team;

@SpringBootTest
@Transactional
public class QueryDslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory jpaQueryFactory;


    @BeforeEach
    void init() {
        jpaQueryFactory = new JPAQueryFactory(em);

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
    }

    @Test
    void startJPQL() {
        //member1 을 찾아라!
        String queryString = "select m from Member m where m.username = :username";
        Member member = em.createQuery(queryString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(member.getUsername()).isEqualTo("member1");
    }

    @Test
    void startQueryDsl() {
        //결국 실행되는 querydsl -> JPQL 로 변환된다.
        Member selected = jpaQueryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        assertThat(selected.getUsername()).isEqualTo("member1");
    }

    @Test
    void resultFetch() {
        QueryResults<Member> memberQueryResults = jpaQueryFactory
                .selectFrom(member)
                .limit(4)
                .offset(2)
                .fetchResults();        // 같은 쿼리에 fetch, fetchCount 정도만 다르게 붙기 때문에,
                                        // count 쿼리에 join 이나 조건절을 줄일 수 있다면
                                        // 성능상 이슈로 count 쿼리를 별도로 분리하는 것이 좋다.

        long total = memberQueryResults.getTotal();
        long limit = memberQueryResults.getLimit();
        long offset = memberQueryResults.getOffset();

        List<Member> results = memberQueryResults.getResults();

        assertThat(total).isEqualTo(4L);
        assertThat(limit).isEqualTo(4);
        assertThat(offset).isEqualTo(2);
        assertThat(results.size()).isEqualTo(2);

        assertThat(results.get(0).getUsername()).isEqualTo("member3");
        assertThat(results.get(1).getUsername()).isEqualTo("member4");
    }

    @Test
    void 회원정렬하기() {
        /**
         * 회원정렬순서
         * 1. 회원 나이 내림차순 desc
         * 2. 회원 이름 올림차순 asc
         * 단 2에서 회원 이름 없으면 마지막에 출력 : nullsLast
         * */

        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 123));

        List<Member> members = jpaQueryFactory
                .selectFrom(member)
                .orderBy(member.age.desc(),
                        member.username.asc().nullsLast())
                .fetch();

        Member member6 = members.get(0);
        Member member5 = members.get(1);
        Member memberNull = members.get(2);

        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    void 팀의이름과_각팀의평균연령_구하기() {
        List<Tuple> fetch = jpaQueryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .leftJoin(member.team, team)
                .groupBy(team.name)
                .fetch();

        assertThat(fetch.size()).isEqualTo(2);

        Tuple teamA = fetch.get(0);
        Tuple teamB = fetch.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("TEAM A");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        assertThat(teamB.get(team.name)).isEqualTo("TEAM B");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    @Test
    void 팀의이름과_각팀의평균연령_구하기_HAVING() {
        List<Tuple> fetch = jpaQueryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .leftJoin(member.team, team)
                .groupBy(team.name)
                .having(team.name.like("%TEAM A%"))
                .fetch();

        assertThat(fetch.size()).isEqualTo(1);

        Tuple teamA = fetch.get(0);

        assertThat(teamA.get(team.name)).isEqualTo("TEAM A");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);
    }
}
