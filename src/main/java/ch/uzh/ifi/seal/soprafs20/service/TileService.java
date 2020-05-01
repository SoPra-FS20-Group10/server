package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.Tile;
import ch.uzh.ifi.seal.soprafs20.exceptions.NotFoundException;
import ch.uzh.ifi.seal.soprafs20.repository.TileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TileService {
    private final TileRepository tileRepository;

    @Autowired
    public TileService(@Qualifier("tileRepository") TileRepository tileRepository) {
        this.tileRepository = tileRepository;
    }

    public void initTiles(){
        String alpha = "abcdefghijklmnopqrstuvwxyz";

        //create and save empty tiles
        for (int i = 1; i < 4; ++i) {
            tileRepository.save(new Tile(i, null,"l"));
            tileRepository.flush();

            tileRepository.save(new Tile(i, null,"w"));
            tileRepository.flush();
        }

        //add all other stone-tile combinations ot the repo
        for(int i = 0; i < alpha.length(); i++){
            String symbol = Character.toString(alpha.charAt(i));
            for(int j = 1; j < 4; j++){
                tileRepository.save(new Tile(j, symbol,"w"));
                tileRepository.flush();

                tileRepository.save(new Tile(j, symbol,"l"));
                tileRepository.flush();
            }
        }
    }

    public void createGrid(Game game){
        List<Tile> grid = new ArrayList<>();

        Integer[] doubles = {3, 11,16,28,32,36,38,42,45,48,52,56,59,64,70,84,92,96,98,102,108};
        Integer[] triples= {0,7,14,20,24,76,80,84,88,105};
        Integer[] doubleWord = {16,28,32,42,48,56,64,70};
        Integer[] tripleWord = {0,7,14,105};

        String error = "Grid cannot be initialized since the tiles couldn't be found";

        //create half of board, then flip and append testTest
        for(int i =0; i < 112; i++){

            //check if double field
            if(Arrays.asList(doubles).contains(i)){

                //check if word or letter
                Optional<Tile> foundTile;
                if(Arrays.asList(doubleWord).contains(i)){
                    foundTile = tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(2, null, "w");
                } else {
                    foundTile = tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(2, null, "l");
                }

                // check if tile exists and add to grid if yes
                if (foundTile.isEmpty()) {
                    throw new NotFoundException(error);
                } else {
                    grid.add(foundTile.get());
                }
            }

            //check if triple tile
            else if(Arrays.asList(triples).contains(i)){
                Optional<Tile> foundTile;

                //check if word or letter
                if(Arrays.asList(tripleWord).contains(i)){
                    foundTile = tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(3, null, "w");
                } else {
                    foundTile = tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(3, null, "l");
                }

                // check if tile exists and add to grid if yes
                if (foundTile.isEmpty()) {
                    throw new NotFoundException(error);
                } else {
                    grid.add(foundTile.get());
                }
            }

            //else its single tile
            else {
                Optional<Tile> foundTile = tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(1,null,"l");

                // check if tile exists
                if (foundTile.isEmpty()) {
                    throw new NotFoundException(error);
                } else {
                    grid.add(foundTile.get());
                }
            }
        }

        //make a clone and reverse it
        List<Tile> clone = new ArrayList<>(grid);
        Collections.reverse(clone);

        //add the middle star
        Optional<Tile> foundTile = tileRepository.findByMultiplierAndStoneSymbolAndMultivariant(2,null,"w");

        // check if tile exists
        if (foundTile.isEmpty()) {
            throw new NotFoundException(error);
        } else {
            grid.add(foundTile.get());
        }

        // fill second half of the grid
        grid.addAll(clone);

        // add grid to game
        game.setGrid(grid);
    }

}
