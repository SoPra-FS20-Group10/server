package ch.uzh.ifi.seal.soprafs20.repository;

import ch.uzh.ifi.seal.soprafs20.entity.Tile;
import ch.uzh.ifi.seal.soprafs20.exceptions.ConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class TileRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Qualifier("tileRepository")
    @Autowired
    private TileRepository tileRepository;

    @Test
    public void findByMultiplierEtc_success() {
        // given
        Tile tile1 = new Tile(2, null, "w");
        entityManager.persist(tile1);
        entityManager.flush();

        Tile tile2 = new Tile(3, "t", "l");
        entityManager.persist(tile2);
        entityManager.flush();

        Tile tile;
        Optional<Tile> foundTile = tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(2, null, "w");

        if (foundTile.isEmpty()) {
            throw new ConflictException("Tile could not be fetched");
        } else {
            tile = foundTile.get();
        }

        assertEquals(tile1.getId(), tile.getId());
        assertEquals(tile1.getMultiplier(), tile.getMultiplier());
        assertEquals(tile1.getMultivariant(), tile.getMultivariant());
        assertEquals(tile1.getStoneSymbol(), tile.getStoneSymbol());

        foundTile = tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(3, "t", "l");

        if (foundTile.isEmpty()) {
            throw new ConflictException("Tile could not be fetched");
        } else {
            tile = foundTile.get();
        }

        assertEquals(tile2.getId(), tile.getId());
        assertEquals(tile2.getMultiplier(), tile.getMultiplier());
        assertEquals(tile2.getMultivariant(), tile.getMultivariant());
        assertEquals(tile2.getStoneSymbol(), tile.getStoneSymbol());
    }
}