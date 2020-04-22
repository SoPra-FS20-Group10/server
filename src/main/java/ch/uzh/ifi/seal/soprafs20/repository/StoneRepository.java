package ch.uzh.ifi.seal.soprafs20.repository;

import ch.uzh.ifi.seal.soprafs20.entity.Stone;
import ch.uzh.ifi.seal.soprafs20.entity.Tile;
import ch.uzh.ifi.seal.soprafs20.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoneRepository extends JpaRepository<Stone, Long> {
    Optional<Stone> findByIdIs(long id);

}