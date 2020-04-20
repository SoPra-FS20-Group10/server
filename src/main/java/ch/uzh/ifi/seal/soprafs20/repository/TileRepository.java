package ch.uzh.ifi.seal.soprafs20.repository;

import ch.uzh.ifi.seal.soprafs20.entity.Tile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TileRepository extends JpaRepository<Tile, Long> {
    Optional<Tile> findByMultiplierAndStonesymbol(int multiplier, String stonesymbol);
}
