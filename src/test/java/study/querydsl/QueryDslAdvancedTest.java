package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entities.Member;
import study.querydsl.entities.QMember;
import study.querydsl.entities.Team;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import java.util.List;

import static study.querydsl.entities.QMember.member;

@SpringBootTest
@Transactional
public class QueryDslAdvancedTest {

    @Autowired
    private EntityManager em;

    private JPAQueryFactory jpaQueryFactory;

    @BeforeEach
    private void init() {
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
    void simpleProjection() {
        List<String> fetch = jpaQueryFactory
                .select(member.username)
                .from(member)
                .fetch();

        fetch.forEach(System.out::println);
    }

    @Test
    @DisplayName("하지만 tuple 은 querydsl 에 종속적인 타입이기 때문에 웬만하면 dto 를 이용해서 쓰는 편이 좋다. 그래야 나중에 db 쿼리 관련 기술이 바뀌어도 이에 종속되지않고 쓸 수 있다. ")
    void tupleProjection() {
        List<Tuple> fetch = jpaQueryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        for (Tuple tuple : fetch) {
            String userName = tuple.get(member.username);
            Integer age = tuple.get(member.age);

            System.out.println("userName" + userName);
            System.out.println("userAge" + age);
        }
    }

    @Test
    @DisplayName("순수 JPA 에서 DTO 타입으로 원하는 컬럼만 받기")
    void returnDtoJPA() {
        List<MemberDto> fetch = em
                .createQuery("select new study.querydsl.dto.MemberDto(m.username, m.age) from Member m", MemberDto.class)
                .getResultList();

        fetch.forEach(System.out::println);
    }

    @Test
    @DisplayName("QueryDsl 에서 DTO 타입으로 원하는 컬럼만 결과 받기 > bean")
    void returnDtoQueryDslBean() {

        //필수사항 : @Setter + @NoArgsConstructor
        List<MemberDto> fetch = jpaQueryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age
                ))
                .from(member)
                .fetch();

        fetch.forEach(System.out::println);
    }

    @Test
    @DisplayName("QueryDsl 에서 DTO 타입으로 원하는 컬럼만 결과 받기 > fields")
    void returnDtoQueryDslFields() {

        //필수사항 : @NoArgsConstructor
        //setter 없이 필드에 바로 주입한다.
        List<MemberDto> fetch = jpaQueryFactory
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age
                ))
                .from(member)
                .fetch();

        fetch.forEach(System.out::println);
    }


    @Test
    @DisplayName("QueryDsl 에서 DTO 타입으로 원하는 컬럼만 결과 받기 > constructor")
    void returnDtoQueryDslConstructor() {

        //필수사항 : @AllArgsConstructor
        List<MemberDto> fetch = jpaQueryFactory
                .select(Projections.constructor(MemberDto.class,
                        member.username,
                        member.age
                ))
                .from(member)
                .fetch();

        fetch.forEach(System.out::println);
    }

    @Test
    @DisplayName("QueryDsl 에서 DTO 타입으로 원하는 컬럼만 결과 받기 > fields, alias")
    void returnDtoQueryDslFieldsAlias() {

        QMember memberSub = new QMember("memberSub");

        List<UserDto> fetch = jpaQueryFactory
                .select(Projections.fields(UserDto.class,
                        QMember.member.username.as("userDtoName"),
                        ExpressionUtils.as(
                            JPAExpressions.select(memberSub.age.max())
                            .from(memberSub), "userDtoAge")
                ))
                .from(QMember.member)
                .fetch();

        fetch.forEach(System.out::println);
    }

    @Test
    @DisplayName("ConstructorQueryProjection")
    void returnDtoQueryDslConstructorQueryProjection() {

        //필수사항 : @AllArgsConstructor + @QueryProjection
        List<MemberDto> fetch = jpaQueryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        fetch.forEach(System.out::println);
    }

    @Test
    void booleanBuilderSearch1() {

        String userName = "member1";
        Integer userAge = null;

        List<Member> members = searchMember1(userName, userAge);
        members.stream().forEach(System.out::println);
    }

    private List<Member> searchMember1(String userName, Integer userAge) {
        BooleanBuilder builder = new BooleanBuilder();

        if (userName != null) {
            builder.and(member.username.eq(userName));
        }

        if (userAge != null) {
            builder.and(member.age.eq(userAge));
        }

        return jpaQueryFactory.selectFrom(member)
                .where(builder)
                .fetch();
    }

    @Test
    void where다중파라미터사용() {
        String userName = "member1";
        Integer userAge = null;

        List<Member> members = searchMember2(userName, userAge);
        members.stream().forEach(System.out::println);
    }

    private List<Member> searchMember2(String userName, Integer userAge) {
        return jpaQueryFactory
                .selectFrom(member)
                .where(eqAll(userName, userAge))
                .fetch();
    }

    //조립해서 사용할 수 있다...!!!
    private BooleanExpression eqAll(String userName, Integer userAge) {
        return eqUserName(userName).and(eqUserAge(userAge));
    }

    private BooleanExpression eqUserAge(Integer userAge) {
        return userAge != null ? member.age.eq(userAge) : null;
    }

    private BooleanExpression eqUserName(String userName) {
        return userName != null ? member.username.eq(userName) : null;
    }

    @Test
    void 쿼리한번으로_대량데이터_수정() {
        long rowNum = jpaQueryFactory.update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(28))
                .execute();

        //member1, member 2 -> 비회원
        //member3, member4 -> 변경없음

        System.out.println("rowNum = " + rowNum);

        em.flush();
        em.clear();

        //조심해야할 것!
        //영속성 context 와 db 데이터 사이의 싱크가 안맞을 수 있다.
        //영속성 context 에서는 아직 member1 인데, db에서는 비회원으로 바뀌었다.
        //bulk 연산은 영속성 컨텍스트, 1차캐시 무시하고 바로 DB 로 접근하여 데이터를 바꿔버린다.

        //JPA 는 기본적으로 영속성컨택스트를 우선적으로 반영하고, 값이 없을 경우에만 DB 데이터를 반영한다.

        List<Member> fetch = jpaQueryFactory.selectFrom(member)
                .fetch();

        fetch.stream().forEach(System.out::println);
    }

    @Test
    void bulkAdd() {
        long rowNum = jpaQueryFactory.update(member)
                .set(member.age, member.age.add(1))     //minus, multiply
                .execute();

        System.out.println("rowNum = " + rowNum);
    }

    @Test
    void bulkDelete() {
        long execute = jpaQueryFactory.delete(member)
                .where(member.age.lt(20))
                .execute();

        System.out.println("execute = " + execute);
    }
}
