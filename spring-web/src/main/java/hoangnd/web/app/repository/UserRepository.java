package hoangnd.web.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import hoangnd.web.app.domain.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(value = "User.userAndRoles", type = EntityGraph.EntityGraphType.LOAD)
    Optional<User> findByUsername (String username);

    Boolean existsByUsername (String username);

    Boolean existsByEmail (String email);
}
