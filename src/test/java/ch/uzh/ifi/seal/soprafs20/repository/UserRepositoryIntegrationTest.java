package ch.uzh.ifi.seal.soprafs20.repository;

import ch.uzh.ifi.seal.soprafs20.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import ch.uzh.ifi.seal.soprafs20.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class UserRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

    @Test
    public void findByUsername_success() {
        // given
        User user = new User();
        user.setUsername("firstname@lastname");
        user.setStatus(UserStatus.OFFLINE);
        user.setToken("1");
        user.setPassword("testPassword");
        user.setCakeDay(new Date());

        entityManager.persist(user);
        entityManager.flush();

        // when
        User foundUser;
        Optional<User> found = userRepository.findByUsername(user.getUsername());
        if (found.isPresent()) {
            foundUser = found.get();
        } else {
            throw new NotFoundException("The user with the username " + user.getUsername() + " could not be found.");
        }

        // then
        assertNotNull(foundUser.getId());
        assertEquals(foundUser.getUsername(), user.getUsername());
        assertEquals(foundUser.getToken(), user.getToken());
        assertEquals(foundUser.getStatus(), user.getStatus());
    }
}
