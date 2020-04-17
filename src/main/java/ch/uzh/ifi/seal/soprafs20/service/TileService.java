package ch.uzh.ifi.seal.soprafs20.service;

import ch.uzh.ifi.seal.soprafs20.entity.Board;
import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.Tile;
import ch.uzh.ifi.seal.soprafs20.exceptions.NotFoundException;
import ch.uzh.ifi.seal.soprafs20.repository.PlayerRepository;
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
        Tile tile = new Tile();
        tile.setMultiplier(1);
        tileRepository.save(tile);
        tile.setMultiplier(2);
        tileRepository.save(tile);
        tile.setMultiplier(3);
        tileRepository.save(tile);
        tileRepository.flush();

        //add all other stone-tile combinations ot the repo

        for(int i = 0; i < alpha.length();i++){
            String symbol = Character.toString(alpha.charAt(i));
            for(int j = 1; j < 4; j++){
                tile.setMultiplier(j);
                tile.setStonesymbol(symbol);
                tileRepository.save(tile);
                tileRepository.flush();
            }
        }


    }

    public void creategrid(Board board){

        List<Tile> grid = board.getGrid();
        grid.clear();

        int[] doubles = {3, 11,16,20,24,28,32,36,38,42,45,48,52,56,59,64,70,76,80,84,88,92,96,98,102,108};
        int[] triples= {0,7,14,105};

        //create half of board, then flip and append testtest

        for(int i =0; i < 112; i++){

            //check if double field

            if(Arrays.asList(doubles).contains(i)){
                Optional<Tile> foundTile = tileRepository.findByCombo(2,null);

                // check if game exists
                if (foundTile.isEmpty()) {
                    throw new NotFoundException("grid cannot be initilazed since the tiles couldnt be found");
                } else {
                    grid.add(foundTile.get());
                }
            }

            //check if triple tile

            else if(Arrays.asList(triples).contains(i)){

                Optional<Tile> foundTile = tileRepository.findByCombo(3,null);

                // check if game exists
                if (foundTile.isEmpty()) {
                    throw new NotFoundException("grid cannot be initilazed since the tiles couldnt be found");
                } else {
                    grid.add(foundTile.get());
                }

            }

            //else its single tile
            else{


                Optional<Tile> foundTile = tileRepository.findByCombo(1,null);

                // check if game exists
                if (foundTile.isEmpty()) {
                    throw new NotFoundException("grid cannot be initilazed since the tiles couldnt be found");
                } else {
                    grid.add(foundTile.get());
                }

            }

        }

        //make a clone and reverse it
        List<Tile> clone = new ArrayList<Tile>(grid);
        Collections.reverse(clone);
        //add the middle star
        grid.add(tileRepository.findByCombo(2,null).get());
        grid.addAll(clone);


        board.setGrid(grid);
    }



}
