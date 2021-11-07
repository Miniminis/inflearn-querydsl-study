package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entities.Hello;
import study.querydsl.entities.QHello;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class QuerydslApplicationTests {

	@Autowired 				//spring 쓴다면 이거
//	@PersistenceContext		//spring framework 를 안쓴다면 이거!
	EntityManager em;

	@Test
	void contextLoads() {
		Hello hello = new Hello();
		em.persist(hello);

		JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);

		Hello result = jpaQueryFactory
				.selectFrom(QHello.hello)
				.fetchOne();

		assertThat(result).isEqualTo(hello);
		assertThat(result.getId()).isEqualTo(hello.getId());
	}

}
